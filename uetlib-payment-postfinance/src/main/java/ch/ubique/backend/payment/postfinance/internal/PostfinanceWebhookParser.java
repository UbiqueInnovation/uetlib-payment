package ch.ubique.backend.payment.postfinance.internal;

import ch.ubique.backend.payment.PaymentFailureCode;
import ch.ubique.backend.payment.result.PaymentWebhookResult;
import ch.ubique.backend.payment.postfinance.PostfinanceWebhookPayload;

import com.fasterxml.jackson.databind.ObjectMapper;

public class PostfinanceWebhookParser {

    private final ObjectMapper objectMapper;
    private final long spaceId;

    public PostfinanceWebhookParser(final ObjectMapper objectMapper, final long spaceId) {
        this.objectMapper = objectMapper;
        this.spaceId = spaceId;
    }

    public PostfinanceWebhookPayload parse(final String rawPayload) {
        try {
            return objectMapper.readValue(rawPayload, PostfinanceWebhookPayload.class);
        } catch (Exception e) {
            return null;
        }
    }

    public PaymentWebhookResult validatePayload(final PostfinanceWebhookPayload payload) {
        if (payload == null) {
            return PaymentWebhookResult.failure(
                    PaymentFailureCode.INVALID_PROVIDER_RESPONSE,
                    "Invalid PostFinance webhook payload");
        }
        if (!isSupportedTransactionWebhook(payload)) {
            return PaymentWebhookResult.ignoredResult();
        }
        return null;
    }

    private boolean isSupportedTransactionWebhook(final PostfinanceWebhookPayload payload) {
        if (payload.entityId() == null) {
            return false;
        }
        if (payload.spaceId() != null && payload.spaceId() != spaceId) {
            return false;
        }
        final var technicalName = payload.listenerEntityTechnicalName();
        return technicalName == null || "Transaction".equalsIgnoreCase(technicalName);
    }
}
