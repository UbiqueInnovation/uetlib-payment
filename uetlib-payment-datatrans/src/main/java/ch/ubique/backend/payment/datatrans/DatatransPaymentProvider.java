package ch.ubique.backend.payment.datatrans;

import ch.ubique.backend.payment.PaymentCancelCommand;
import ch.ubique.backend.payment.PaymentCaptureCommand;
import ch.ubique.backend.payment.PaymentFailureCode;
import ch.ubique.backend.payment.PaymentInitCommand;
import ch.ubique.backend.payment.PaymentInitResult;
import ch.ubique.backend.payment.PaymentMethod;
import ch.ubique.backend.payment.PaymentOperationResult;
import ch.ubique.backend.payment.PaymentProvider;
import ch.ubique.backend.payment.PaymentProviderType;
import ch.ubique.backend.payment.PaymentStatusResult;
import ch.ubique.backend.payment.PaymentWebhookEvent;
import ch.ubique.backend.payment.PaymentWebhookResult;
import ch.ubique.backend.payment.TransactionStatus;
import ch.ubique.datatrans.model.transactions.DatatransCurrency;
import ch.ubique.datatrans.model.transactions.DatatransInitializeTransaction;
import ch.ubique.datatrans.model.transactions.DatatransInitializeTransactionRedirect;
import ch.ubique.datatrans.model.transactions.DatatransInitializeTransactionResponse;
import ch.ubique.datatrans.model.transactions.DatatransInitializeTransactionTheme;
import ch.ubique.datatrans.model.transactions.DatatransInitializeTransactionWebhook;
import ch.ubique.datatrans.model.transactions.DatatransPaymentMethod;
import ch.ubique.datatrans.model.transactions.DatatransTransactionStatus;
import ch.ubique.datatrans.model.transactions.DatatransTransactionStatusResponse;
import ch.ubique.datatrans.service.DatatransTransactionClient;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class DatatransPaymentProvider implements PaymentProvider {

    private static final Logger logger = LoggerFactory.getLogger(DatatransPaymentProvider.class);

    private static final List<DatatransPaymentMethod> DEFAULT_PAYMENT_METHODS =
            List.of(
                    DatatransPaymentMethod.ECA,
                    DatatransPaymentMethod.VIS,
                    DatatransPaymentMethod.TWI,
                    DatatransPaymentMethod.APL,
                    DatatransPaymentMethod.PAY,
                    DatatransPaymentMethod.PFC,
                    DatatransPaymentMethod.PEF);

    private final DatatransTransactionClient datatransTransactionClient;
    private final RestClient restClient;
    private final ObjectMapper objectMapper;
    private final String paymentWebhookUrl;
    private final String datatransBaseUrl;
    private final String datatransMerchantId;
    private final String datatransPassword;

    public DatatransPaymentProvider(
            final DatatransTransactionClient datatransTransactionClient,
            final RestClient restClient,
            final ObjectMapper objectMapper,
            final String paymentWebhookUrl,
            final String datatransBaseUrl,
            final String datatransMerchantId,
            final String datatransPassword) {
        this.datatransTransactionClient = datatransTransactionClient;
        this.restClient = restClient;
        this.objectMapper = objectMapper;
        this.paymentWebhookUrl = paymentWebhookUrl;
        this.datatransBaseUrl = datatransBaseUrl;
        this.datatransMerchantId = datatransMerchantId;
        this.datatransPassword = datatransPassword;
    }

    @Override
    public PaymentProviderType getType() {
        return PaymentProviderType.DATATRANS;
    }

    @Override
    public PaymentInitResult initiate(final PaymentInitCommand command) {
        final var request =
                DatatransInitializeTransaction.builder()
                        .currency(DatatransCurrency.CHF)
                        .refno(command.reference())
                        .amount(toDatatransAmount(command.amount()))
                        .paymentMethods(DEFAULT_PAYMENT_METHODS)
                        .autoSettle(false)
                        .language(normalizeDatatransLanguage(LocaleContextHolder.getLocale()))
                        .theme(new DatatransInitializeTransactionTheme())
                        .redirect(buildRedirect(command))
                        .webhook(new DatatransInitializeTransactionWebhook(paymentWebhookUrl))
                        .build();

        try {
            final DatatransInitializeTransactionResponse response =
                    datatransTransactionClient.initializeTransaction(request);
            return PaymentInitResult.success(
                    response.transactionId(),
                    response.redirectUrl(),
                    command.reference(),
                    TransactionStatus.PENDING,
                    request.currency().name(),
                    command.amount(),
                    null);
        } catch (Exception e) {
            logger.error(
                    "Failed to initialize Datatrans transaction for reference {}",
                    command.reference(),
                    e);
            return PaymentInitResult.failure(
                    PaymentFailureCode.PROVIDER_API_ERROR,
                    "Failed to initialize Datatrans transaction");
        }
    }

    @Override
    public PaymentStatusResult getStatus(final String providerTransactionId) {
        try {
            final var response =
                    datatransTransactionClient.getTransactionStatus(providerTransactionId);
            return PaymentStatusResult.success(
                    mapStatus(response.status()),
                    response.status().name(),
                    response.currency().name(),
                    null,
                    mapPaymentMethod(response.paymentMethod()),
                    extractFailureReason(response),
                    extractFailureMessage(response));
        } catch (Exception e) {
            logger.error(
                    "Failed to fetch Datatrans transaction status for providerTransactionId {}",
                    providerTransactionId,
                    e);
            return PaymentStatusResult.failure(
                    PaymentFailureCode.PROVIDER_API_ERROR,
                    "Failed to fetch Datatrans transaction status");
        }
    }

    @Override
    public PaymentWebhookResult processWebhook(
            final String rawPayload, final Map<String, String> headers) {
        final DatatransTransactionStatusResponse webhookStatus;
        try {
            webhookStatus =
                    objectMapper.readValue(rawPayload, DatatransTransactionStatusResponse.class);
        } catch (Exception e) {
            logger.info("Rejected Datatrans webhook due to invalid payload");
            logger.debug("Invalid Datatrans webhook payload", e);
            return PaymentWebhookResult.failure(
                    PaymentFailureCode.INVALID_PROVIDER_RESPONSE,
                    "Invalid Datatrans webhook payload");
        }

        if (webhookStatus.transactionId() == null || webhookStatus.transactionId().isBlank()) {
            return PaymentWebhookResult.failure(
                    PaymentFailureCode.INVALID_PROVIDER_RESPONSE,
                    "Payment webhook does not contain transactionId");
        }
        if (webhookStatus.merchantId() != null
                && !webhookStatus.merchantId().isBlank()
                && !webhookStatus.merchantId().equals(datatransMerchantId)) {
            return PaymentWebhookResult.failure(
                    PaymentFailureCode.INVALID_PROVIDER_REFERENCE,
                    "Datatrans webhook merchantId mismatch");
        }

        return PaymentWebhookResult.success(
                new PaymentWebhookEvent(
                        webhookStatus.transactionId(),
                        mapStatus(webhookStatus.status()),
                        webhookStatus.status().name(),
                        mapPaymentMethod(webhookStatus.paymentMethod()),
                        webhookStatus.currency().name(),
                        extractFailureReason(webhookStatus),
                        extractFailureMessage(webhookStatus)));
    }

    @Override
    public PaymentOperationResult capture(final PaymentCaptureCommand command) {
        try {
            restClient
                    .post()
                    .uri(
                            datatransBaseUrl + "/v1/transactions/{transactionId}/settle",
                            command.providerTransactionId())
                    .header(HttpHeaders.AUTHORIZATION, basicAuthHeader())
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(
                            Map.of(
                                    "refno",
                                    command.providerReference(),
                                    "amount",
                                    toDatatransAmount(command.amount()),
                                    "currency",
                                    command.currency().name()))
                    .retrieve()
                    .toBodilessEntity();
            return PaymentOperationResult.success();
        } catch (Exception e) {
            logger.error(
                    "Failed to capture Datatrans transaction {}",
                    command.providerTransactionId(),
                    e);
            return PaymentOperationResult.failure(
                    PaymentFailureCode.PROVIDER_API_ERROR,
                    "Failed to capture Datatrans transaction");
        }
    }

    @Override
    public PaymentOperationResult cancel(final PaymentCancelCommand command) {
        try {
            restClient
                    .post()
                    .uri(
                            datatransBaseUrl + "/v1/transactions/{transactionId}/cancel",
                            command.providerTransactionId())
                    .header(HttpHeaders.AUTHORIZATION, basicAuthHeader())
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(Map.of("refno", command.providerReference()))
                    .retrieve()
                    .toBodilessEntity();
            return PaymentOperationResult.success();
        } catch (Exception e) {
            logger.error(
                    "Failed to cancel Datatrans transaction {}",
                    command.providerTransactionId(),
                    e);
            return PaymentOperationResult.failure(
                    PaymentFailureCode.PROVIDER_API_ERROR,
                    "Failed to cancel Datatrans transaction");
        }
    }

    private DatatransInitializeTransactionRedirect buildRedirect(final PaymentInitCommand command) {
        return new DatatransInitializeTransactionRedirect(
                command.successUrl(),
                command.failedUrl(),
                command.failedUrl(),
                HttpMethod.GET.name());
    }

    private String extractFailureReason(final DatatransTransactionStatusResponse response) {
        if (response.detail().fail() == null) {
            return null;
        }
        return response.detail().fail().reason().name();
    }

    private String extractFailureMessage(final DatatransTransactionStatusResponse response) {
        if (response.detail().fail() == null) {
            return null;
        }
        if (response.detail().fail().fail() != null && !response.detail().fail().fail().isBlank()) {
            return response.detail().fail().fail();
        }
        return null;
    }

    private TransactionStatus mapStatus(final DatatransTransactionStatus status) {
        if (status == null) {
            return TransactionStatus.FAILED;
        }
        return switch (status) {
            case INITIALIZED, CHALLENGE_REQUIRED, CHALLENGE_ONGOING -> TransactionStatus.PENDING;
            case AUTHENTICATED, AUTHORIZED -> TransactionStatus.AUTHORIZED;
            case SETTLED, TRANSMITTED -> TransactionStatus.COMPLETED;
            case CANCELED -> TransactionStatus.VOIDED;
            case FAILED -> TransactionStatus.FAILED;
        };
    }

    private PaymentMethod mapPaymentMethod(final DatatransPaymentMethod paymentMethod) {
        if (paymentMethod == null) {
            return null;
        }
        return switch (paymentMethod) {
            case ECA, VIS -> PaymentMethod.CARD;
            case TWI -> PaymentMethod.TWINT;
            case APL -> PaymentMethod.APPLE_PAY;
            case PAY -> PaymentMethod.PAYPAL;
            case PFC -> PaymentMethod.POSTFINANCE_CARD;
            case PEF -> PaymentMethod.POSTFINANCE_EFINANCE;
            default -> PaymentMethod.UNKNOWN;
        };
    }

    private String normalizeDatatransLanguage(final Locale locale) {
        if (locale == null) {
            return "en";
        }
        final var language = locale.getLanguage();
        if (language.length() != 2) {
            return "en";
        }
        return language;
    }

    private String basicAuthHeader() {
        final var token = datatransMerchantId + ":" + datatransPassword;
        return "Basic "
                + Base64.getEncoder().encodeToString(token.getBytes(StandardCharsets.UTF_8));
    }

    private int toDatatransAmount(final BigDecimal amount) {
        return amount.movePointRight(2).intValueExact();
    }
}
