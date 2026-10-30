package ch.ubique.uetlib.payment.datatrans.internal;

import ch.ubique.uetlib.payment.PaymentFailureCode;
import ch.ubique.uetlib.payment.datatrans.client.model.transactions.DatatransTransactionStatusResponse;
import ch.ubique.uetlib.payment.result.PaymentWebhookResult;
import tools.jackson.databind.ObjectMapper;

public class DatatransWebhookParser {

    private final ObjectMapper objectMapper;
    private final String datatransMerchantId;
    private final DatatransPaymentStatusMapper statusMapper;

    public DatatransWebhookParser(
            final ObjectMapper objectMapper,
            final String datatransMerchantId,
            final DatatransPaymentStatusMapper statusMapper) {
        this.objectMapper = objectMapper;
        this.datatransMerchantId = datatransMerchantId;
        this.statusMapper = statusMapper;
    }

    public PaymentWebhookResult parse(final String rawPayload) {
        final DatatransTransactionStatusResponse webhookStatus;
        try {
            webhookStatus =
                    objectMapper.readValue(rawPayload, DatatransTransactionStatusResponse.class);
        } catch (Exception e) {
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

        return PaymentWebhookResult.success(statusMapper.toWebhookEvent(webhookStatus));
    }
}
