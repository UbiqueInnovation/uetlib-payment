package ch.ubique.backend.payment.postfinance.internal;

import ch.ubique.backend.payment.PaymentFailureCode;
import ch.ubique.backend.payment.postfinance.PostfinanceWebhookPayload;
import ch.ubique.backend.payment.result.PaymentWebhookResult;

import org.jspecify.annotations.Nullable;

import tools.jackson.databind.ObjectMapper;

public class PostfinanceWebhookParser {

    private final ObjectMapper objectMapper;
    private final long spaceId;

    public PostfinanceWebhookParser(final ObjectMapper objectMapper, final long spaceId) {
        this.objectMapper = objectMapper;
        this.spaceId = spaceId;
    }

    public @Nullable PostfinanceWebhookPayload parse(final @Nullable String rawPayload) {
        if (rawPayload == null) {
            return null;
        }
        try {
            return objectMapper.readValue(rawPayload, PostfinanceWebhookPayload.class);
        } catch (Exception e) {
            return null;
        }
    }

    public @Nullable PaymentWebhookResult validatePayload(
            final @Nullable PostfinanceWebhookPayload payload) {
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
