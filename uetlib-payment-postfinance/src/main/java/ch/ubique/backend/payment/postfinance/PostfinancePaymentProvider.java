package ch.ubique.backend.payment.postfinance;

import ch.postfinancecheckout.sdk.ApiClient;
import ch.postfinancecheckout.sdk.ApiException;
import ch.postfinancecheckout.sdk.model.TransactionState;
import ch.postfinancecheckout.sdk.service.TransactionsService;
import ch.postfinancecheckout.sdk.service.WebhookEncryptionKeysService;
import ch.ubique.backend.payment.PaymentFailureCode;
import ch.ubique.backend.payment.PaymentProvider;
import ch.ubique.backend.payment.PaymentProviderType;
import ch.ubique.backend.payment.TransactionStatus;
import ch.ubique.backend.payment.command.PaymentCancelCommand;
import ch.ubique.backend.payment.command.PaymentCaptureCommand;
import ch.ubique.backend.payment.command.PaymentInitCommand;
import ch.ubique.backend.payment.model.PaymentWebhookEvent;
import ch.ubique.backend.payment.postfinance.internal.PostfinanceAuthKeyValidator;
import ch.ubique.backend.payment.postfinance.internal.PostfinanceOperationsClient;
import ch.ubique.backend.payment.postfinance.internal.PostfinanceStatusMapper;
import ch.ubique.backend.payment.postfinance.internal.PostfinanceTransactionFactory;
import ch.ubique.backend.payment.postfinance.internal.PostfinanceWebhookParser;
import ch.ubique.backend.payment.result.PaymentInitResult;
import ch.ubique.backend.payment.result.PaymentOperationResult;
import ch.ubique.backend.payment.result.PaymentStatusResult;
import ch.ubique.backend.payment.result.PaymentWebhookResult;

import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import tools.jackson.databind.ObjectMapper;

import java.util.Map;

public class PostfinancePaymentProvider implements PaymentProvider {

    private static final Logger logger = LoggerFactory.getLogger(PostfinancePaymentProvider.class);

    private final long spaceId;
    private final long userId;
    private final String authKey;
    private final CloseableHttpClient httpClient;
    private final boolean signatureValidationEnabled;
    private final PostfinanceOperationsClient operationsClient;
    private final PostfinanceTransactionFactory transactionFactory;
    private final PostfinanceStatusMapper statusMapper;
    private final PostfinanceWebhookParser webhookParser;

    public PostfinancePaymentProvider(
            final ObjectMapper objectMapper,
            final long spaceId,
            final long userId,
            final String authKey,
            final String productSku,
            final String productUniqueId,
            final boolean signatureValidationEnabled) {
        this(
                objectMapper,
                spaceId,
                userId,
                authKey,
                productSku,
                productUniqueId,
                signatureValidationEnabled,
                HttpClients.createDefault());
    }

    @SuppressWarnings("java:S107")
    public PostfinancePaymentProvider(
            final ObjectMapper objectMapper,
            final long spaceId,
            final long userId,
            final String authKey,
            final String productSku,
            final String productUniqueId,
            final boolean signatureValidationEnabled,
            final CloseableHttpClient httpClient) {
        PostfinanceAuthKeyValidator.validate(authKey);
        this.spaceId = spaceId;
        this.userId = userId;
        this.authKey = authKey;
        this.httpClient = httpClient;
        this.signatureValidationEnabled = signatureValidationEnabled;
        this.operationsClient = new PostfinanceOperationsClient(spaceId, productUniqueId);
        this.transactionFactory = new PostfinanceTransactionFactory(productSku, productUniqueId);
        this.statusMapper = new PostfinanceStatusMapper();
        this.webhookParser = new PostfinanceWebhookParser(objectMapper, spaceId);
    }

    public TransactionsService createTransactionsService() {
        return new TransactionsService(new ApiClient(userId, authKey, httpClient));
    }

    public WebhookEncryptionKeysService createWebhookEncryptionKeysService() {
        return new WebhookEncryptionKeysService(new ApiClient(userId, authKey, httpClient));
    }

    @Override
    public PaymentProviderType getType() {
        return PaymentProviderType.POSTFINANCE;
    }

    @Override
    public PaymentInitResult initiate(final PaymentInitCommand command) {
        final var service = createTransactionsService();
        final var merchantReference = command.reference();
        final var transactionCreate = transactionFactory.create(command);

        try {
            final var created = service.postPaymentTransactions(spaceId, transactionCreate, null);
            final var transactionId = created.getId();
            final var status = statusMapper.getRequiredTransactionStatus(created);
            if (transactionId == null || status == null) {
                logger.error(
                        "PostFinance returned an incomplete transaction for reference {}",
                        merchantReference);
                return PaymentInitResult.failure(
                        PaymentFailureCode.INVALID_PROVIDER_RESPONSE,
                        "PostFinance returned an incomplete transaction");
            }

            // Use the Lightbox integration exclusively. Requesting the Payment Page URL as a
            // second URL for the same transaction can switch PostFinance out of Lightbox mode.
            final var jsUrl =
                    service.getPaymentTransactionsIdLightboxJavascriptUrl(transactionId, spaceId);

            return PaymentInitResult.success(
                    transactionId.toString(),
                    jsUrl,
                    null,
                    merchantReference,
                    status,
                    created.getCurrency(),
                    created.getAuthorizationAmount(),
                    created.getVersion());
        } catch (ApiException e) {
            logger.error(
                    "Failed to initiate transaction for customerReference {}",
                    command.payer() != null ? command.payer().customerReference() : null,
                    e);
            return PaymentInitResult.failure(
                    PaymentFailureCode.PROVIDER_API_ERROR,
                    "Failed to initiate PostFinance transaction");
        }
    }

    @Override
    public PaymentStatusResult getStatus(final String providerTransactionId) {
        final var service = createTransactionsService();
        try {
            final var postfinanceId = Long.valueOf(providerTransactionId);
            final var transaction = service.getPaymentTransactionsId(postfinanceId, spaceId, null);
            final var result = statusMapper.toStatusResult(transaction);
            if (!result.successful()) {
                logger.error(
                        "PostFinance returned a transaction without state for providerTransactionId"
                                + " {}",
                        providerTransactionId);
            }
            return result;
        } catch (ApiException e) {
            logger.error(
                    "Failed to get transaction status for providerTransactionId {}",
                    providerTransactionId,
                    e);
            return PaymentStatusResult.failure(
                    PaymentFailureCode.PROVIDER_API_ERROR,
                    "Failed to fetch PostFinance transaction status");
        } catch (NumberFormatException e) {
            logger.error(
                    "Invalid provider transaction id for PostFinance: {}",
                    providerTransactionId,
                    e);
            return PaymentStatusResult.failure(
                    PaymentFailureCode.INVALID_PROVIDER_REFERENCE,
                    "Invalid PostFinance transaction id");
        }
    }

    @Override
    public PaymentOperationResult capture(final PaymentCaptureCommand command) {
        final var transactionId =
                operationsClient.parseProviderTransactionId(command.providerTransactionId());
        if (transactionId == null) {
            return PaymentOperationResult.failure(
                    PaymentFailureCode.INVALID_PROVIDER_REFERENCE,
                    "Invalid PostFinance transaction id");
        }

        try {
            final var completion =
                    operationsClient.capture(createTransactionsService(), transactionId, command);
            if (!statusMapper.isCaptureSuccessful(completion)) {
                logger.error(
                        "PostFinance capture failed for providerTransactionId {} with state {}",
                        command.providerTransactionId(),
                        completion == null ? null : completion.getState());
                return PaymentOperationResult.failure(
                        PaymentFailureCode.INVALID_PROVIDER_RESPONSE,
                        "Failed to capture PostFinance transaction");
            }
            return PaymentOperationResult.success();
        } catch (ApiException e) {
            logger.error(
                    "Failed to capture PostFinance transaction for providerTransactionId {}",
                    command.providerTransactionId(),
                    e);
            return PaymentOperationResult.failure(
                    PaymentFailureCode.PROVIDER_API_ERROR,
                    "Failed to capture PostFinance transaction");
        }
    }

    @Override
    public PaymentOperationResult cancel(final PaymentCancelCommand command) {
        final var transactionId =
                operationsClient.parseProviderTransactionId(command.providerTransactionId());
        if (transactionId == null) {
            return PaymentOperationResult.failure(
                    PaymentFailureCode.INVALID_PROVIDER_REFERENCE,
                    "Invalid PostFinance transaction id");
        }

        try {
            final var transactionVoid =
                    operationsClient.cancel(createTransactionsService(), transactionId);
            if (!statusMapper.isCancelSuccessful(transactionVoid)) {
                logger.error(
                        "PostFinance cancel failed for providerTransactionId {} with state {}",
                        command.providerTransactionId(),
                        transactionVoid == null ? null : transactionVoid.getState());
                return PaymentOperationResult.failure(
                        PaymentFailureCode.INVALID_PROVIDER_RESPONSE,
                        "Failed to cancel PostFinance transaction");
            }
            return PaymentOperationResult.success();
        } catch (ApiException e) {
            logger.error(
                    "Failed to cancel PostFinance transaction for providerTransactionId {}",
                    command.providerTransactionId(),
                    e);
            return PaymentOperationResult.failure(
                    PaymentFailureCode.PROVIDER_API_ERROR,
                    "Failed to cancel PostFinance transaction");
        }
    }

    @Override
    public PaymentWebhookResult processWebhook(
            final String rawPayload, final @Nullable Map<String, String> headers) {
        final var signatureHeader = headers != null ? headers.get("x-signature") : null;
        if (signatureValidationEnabled && !isSignatureValid(rawPayload, signatureHeader)) {
            logger.info("Rejected PostFinance webhook due to invalid or missing signature");
            return PaymentWebhookResult.failure(
                    PaymentFailureCode.INVALID_PROVIDER_REFERENCE,
                    "Invalid or missing PostFinance webhook signature");
        }

        final var webhookPayload = webhookParser.parse(rawPayload);
        if (webhookPayload == null) {
            return PaymentWebhookResult.failure(
                    PaymentFailureCode.INVALID_PROVIDER_RESPONSE,
                    "Invalid PostFinance webhook payload");
        }
        final var payloadValidation = webhookParser.validatePayload(webhookPayload);
        if (payloadValidation != null) {
            return payloadValidation;
        }

        final var transactionStatus = resolveWebhookTransactionStatus(webhookPayload);
        if (transactionStatus == null || webhookPayload.entityId() == null) {
            logger.info(
                    "Unable to resolve PostFinance transaction state for entityId {}",
                    webhookPayload.entityId());
            return PaymentWebhookResult.failure(
                    PaymentFailureCode.INVALID_PROVIDER_RESPONSE,
                    "Unable to resolve PostFinance transaction state");
        }

        return PaymentWebhookResult.success(
                new PaymentWebhookEvent(
                        webhookPayload.entityId().toString(),
                        transactionStatus,
                        webhookPayload.state(),
                        null,
                        null,
                        null,
                        null));
    }

    private boolean isSignatureValid(final String payload, final @Nullable String signatureHeader) {
        if (signatureHeader == null || signatureHeader.isBlank()) {
            return false;
        }
        try {
            return createWebhookEncryptionKeysService().isContentValid(signatureHeader, payload);
        } catch (ApiException e) {
            logger.info("PostFinance webhook signature validation failed");
            logger.debug("PostFinance webhook signature validation exception", e);
            return false;
        }
    }

    private @Nullable TransactionStatus resolveWebhookTransactionStatus(
            final PostfinanceWebhookPayload webhookPayload) {
        if (webhookPayload.state() != null && !webhookPayload.state().isBlank()) {
            try {
                return statusMapper.mapTransactionState(
                        TransactionState.valueOf(webhookPayload.state()));
            } catch (IllegalArgumentException e) {
                logger.info("Unknown PostFinance transaction state {}", webhookPayload.state());
            }
        }

        if (webhookPayload.entityId() == null) {
            return null;
        }

        final var statusResult = getStatus(webhookPayload.entityId().toString());
        if (!statusResult.successful()) {
            return null;
        }
        return statusResult.paymentStatus();
    }
}
