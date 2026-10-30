package ch.ubique.uetlib.payment.datatrans;

import ch.ubique.uetlib.payment.PaymentFailureCode;
import ch.ubique.uetlib.payment.PaymentProvider;
import ch.ubique.uetlib.payment.PaymentProviderType;
import ch.ubique.uetlib.payment.TransactionStatus;
import ch.ubique.uetlib.payment.command.PaymentCancelCommand;
import ch.ubique.uetlib.payment.command.PaymentCaptureCommand;
import ch.ubique.uetlib.payment.command.PaymentInitCommand;
import ch.ubique.uetlib.payment.datatrans.client.model.transactions.DatatransInitializeTransactionResponse;
import ch.ubique.uetlib.payment.datatrans.client.service.DatatransTransactionClient;
import ch.ubique.uetlib.payment.datatrans.internal.DatatransOperationsClient;
import ch.ubique.uetlib.payment.datatrans.internal.DatatransPaymentRequestFactory;
import ch.ubique.uetlib.payment.datatrans.internal.DatatransPaymentStatusMapper;
import ch.ubique.uetlib.payment.datatrans.internal.DatatransWebhookParser;
import ch.ubique.uetlib.payment.result.PaymentInitResult;
import ch.ubique.uetlib.payment.result.PaymentOperationResult;
import ch.ubique.uetlib.payment.result.PaymentStatusResult;
import ch.ubique.uetlib.payment.result.PaymentWebhookResult;
import java.util.Map;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.client.RestClient;
import tools.jackson.databind.ObjectMapper;

public class DatatransPaymentProvider implements PaymentProvider {

    private static final Logger logger = LoggerFactory.getLogger(DatatransPaymentProvider.class);

    private final DatatransTransactionClient datatransTransactionClient;
    private final DatatransPaymentRequestFactory requestFactory;
    private final DatatransPaymentStatusMapper statusMapper;
    private final DatatransWebhookParser webhookParser;
    private final DatatransOperationsClient operationsClient;

    public DatatransPaymentProvider(
            final DatatransTransactionClient datatransTransactionClient,
            final RestClient restClient,
            final ObjectMapper objectMapper,
            final String paymentWebhookUrl,
            final String datatransBaseUrl,
            final String datatransMerchantId,
            final String datatransPassword) {
        this.datatransTransactionClient = datatransTransactionClient;
        this.statusMapper = new DatatransPaymentStatusMapper();
        this.requestFactory = new DatatransPaymentRequestFactory(paymentWebhookUrl);
        this.webhookParser =
                new DatatransWebhookParser(objectMapper, datatransMerchantId, statusMapper);
        this.operationsClient =
                new DatatransOperationsClient(
                        restClient, datatransBaseUrl, datatransMerchantId, datatransPassword);
    }

    @Override
    public PaymentProviderType getType() {
        return PaymentProviderType.DATATRANS;
    }

    @Override
    public PaymentInitResult initiate(final PaymentInitCommand command) {
        final var request = requestFactory.createInitRequest(command);
        try {
            final DatatransInitializeTransactionResponse response =
                    datatransTransactionClient.initializeTransaction(request);
            return PaymentInitResult.success(
                    response.transactionId(),
                    response.redirectUrl(),
                    null,
                    command.reference(),
                    TransactionStatus.PENDING,
                    request.currency().name(),
                    command.amount(),
                    null);
        } catch (final Exception e) {
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
            return statusMapper.toStatusResult(response);
        } catch (final Exception e) {
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
            final String rawPayload, final @Nullable Map<String, String> headers) {
        final var result = webhookParser.parse(rawPayload);
        if (!result.successful() && !result.ignored()) {
            logger.info("Rejected Datatrans webhook");
        }
        return result;
    }

    @Override
    public PaymentOperationResult capture(final PaymentCaptureCommand command) {
        return operationsClient.capture(command);
    }

    @Override
    public PaymentOperationResult cancel(final PaymentCancelCommand command) {
        return operationsClient.cancel(command);
    }
}
