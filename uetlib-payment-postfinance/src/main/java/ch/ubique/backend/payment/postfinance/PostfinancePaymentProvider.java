package ch.ubique.backend.payment.postfinance;

import ch.postfinancecheckout.sdk.ApiClient;
import ch.postfinancecheckout.sdk.ApiException;
import ch.postfinancecheckout.sdk.model.Transaction;
import ch.postfinancecheckout.sdk.model.TransactionState;
import ch.postfinancecheckout.sdk.service.TransactionCompletionsService;
import ch.postfinancecheckout.sdk.service.TransactionsService;
import ch.postfinancecheckout.sdk.service.WebhookEncryptionKeysService;
import ch.ubique.backend.payment.PaymentFailureCode;
import ch.ubique.backend.payment.PaymentProvider;
import ch.ubique.backend.payment.PaymentProviderType;
import ch.ubique.backend.payment.TransactionStatus;
import ch.ubique.backend.payment.command.PaymentInitCommand;
import ch.ubique.backend.payment.postfinance.internal.PostfinanceStatusMapper;
import ch.ubique.backend.payment.postfinance.internal.PostfinanceTransactionFactory;
import ch.ubique.backend.payment.postfinance.internal.PostfinanceWebhookParser;
import ch.ubique.backend.payment.result.PaymentInitResult;
import ch.ubique.backend.payment.result.PaymentStatusResult;
import ch.ubique.backend.payment.result.PaymentWebhookResult;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

public class PostfinancePaymentProvider implements PaymentProvider {

    private static final Logger logger = LoggerFactory.getLogger(PostfinancePaymentProvider.class);
    private static final String UNKNOWN_TRANSACTION_STATE_MESSAGE =
            "PostFinance transaction is missing a state";

    private final long spaceId;
    private final long userId;
    private final String authKey;
    private final boolean signatureValidationEnabled;
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
        this.spaceId = spaceId;
        this.userId = userId;
        this.authKey = authKey;
        this.signatureValidationEnabled = signatureValidationEnabled;
        this.transactionFactory = new PostfinanceTransactionFactory(productSku, productUniqueId);
        this.statusMapper = new PostfinanceStatusMapper();
        this.webhookParser = new PostfinanceWebhookParser(objectMapper, spaceId);
    }

    public TransactionsService createTransactionsService() {
        return new TransactionsService(new ApiClient(userId, authKey));
    }

    public WebhookEncryptionKeysService createWebhookEncryptionKeysService() {
        return new WebhookEncryptionKeysService(new ApiClient(userId, authKey));
    }

    /**
     * Factory hook kept public for future deferred-completion flows and test overrides.
     */
    public TransactionCompletionsService createTransactionCompletionsService() {
        return new TransactionCompletionsService(new ApiClient(userId, authKey));
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
            final Transaction created = service.postPaymentTransactions(spaceId, transactionCreate, null);
            final Long transactionId = created.getId();
            final TransactionStatus status = statusMapper.getRequiredTransactionStatus(created);
            if (transactionId == null || status == null) {
                logger.error(
                        "PostFinance returned an incomplete transaction for reference {}",
                        merchantReference);
                return PaymentInitResult.failure(
                        PaymentFailureCode.INVALID_PROVIDER_RESPONSE,
                        "PostFinance returned an incomplete transaction");
            }

            final String jsUrl =
                    service.getPaymentTransactionsIdLightboxJavascriptUrl(transactionId, spaceId);

            return PaymentInitResult.success(
                    transactionId.toString(),
                    jsUrl,
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
            final Long postfinanceId = Long.valueOf(providerTransactionId);
            final Transaction transaction =
                    service.getPaymentTransactionsId(postfinanceId, spaceId, null);
            final var result = statusMapper.toStatusResult(transaction);
            if (!result.successful()) {
                logger.error(
                        "PostFinance returned a transaction without state for providerTransactionId {}",
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
    public PaymentWebhookResult processWebhook(
            final String rawPayload, final Map<String, String> headers) {
        final String signatureHeader = headers != null ? headers.get("x-signature") : null;
        if (signatureValidationEnabled && !isSignatureValid(rawPayload, signatureHeader)) {
            logger.info("Rejected PostFinance webhook due to invalid or missing signature");
            return PaymentWebhookResult.failure(
                    PaymentFailureCode.INVALID_PROVIDER_REFERENCE,
                    "Invalid or missing PostFinance webhook signature");
        }

        final var webhookPayload = webhookParser.parse(rawPayload);
        final var payloadValidation = webhookParser.validatePayload(webhookPayload);
        if (payloadValidation != null) {
            return payloadValidation;
        }

        final TransactionStatus transactionStatus = resolveWebhookTransactionStatus(webhookPayload);
        if (transactionStatus == null) {
            logger.info(
                    "Unable to resolve PostFinance transaction state for entityId {}",
                    webhookPayload.entityId());
            return PaymentWebhookResult.failure(
                    PaymentFailureCode.INVALID_PROVIDER_RESPONSE,
                    "Unable to resolve PostFinance transaction state");
        }

        return PaymentWebhookResult.success(
                new ch.ubique.backend.payment.model.PaymentWebhookEvent(
                        webhookPayload.entityId().toString(),
                        transactionStatus,
                        webhookPayload.state(),
                        null,
                        null,
                        null,
                        null));
    }

    private boolean isSignatureValid(final String payload, final String signatureHeader) {
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

    private TransactionStatus resolveWebhookTransactionStatus(
            final PostfinanceWebhookPayload webhookPayload) {
        if (webhookPayload.state() != null && !webhookPayload.state().isBlank()) {
            try {
                return statusMapper.mapTransactionState(TransactionState.valueOf(webhookPayload.state()));
            } catch (IllegalArgumentException e) {
                logger.info("Unknown PostFinance transaction state {}", webhookPayload.state());
            }
        }

        final PaymentStatusResult statusResult = getStatus(webhookPayload.entityId().toString());
        if (!statusResult.successful()) {
            return null;
        }
        return statusResult.paymentStatus();
    }
}
