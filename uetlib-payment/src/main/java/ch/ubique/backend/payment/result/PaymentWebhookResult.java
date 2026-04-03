package ch.ubique.backend.payment.result;

import ch.ubique.backend.payment.PaymentFailureCode;
import ch.ubique.backend.payment.model.PaymentFailure;
import ch.ubique.backend.payment.model.PaymentWebhookEvent;

/** Neutral result returned when parsing and validating a provider webhook. */
public record PaymentWebhookResult(
        boolean successful, boolean ignored, PaymentWebhookEvent event, PaymentFailure failure) {

    public static PaymentWebhookResult success(PaymentWebhookEvent event) {
        return new PaymentWebhookResult(true, false, event, null);
    }

    public static PaymentWebhookResult ignoredResult() {
        return new PaymentWebhookResult(false, true, null, null);
    }

    public static PaymentWebhookResult failure(PaymentFailureCode code, String message) {
        return new PaymentWebhookResult(false, false, null, new PaymentFailure(code, message));
    }
}
