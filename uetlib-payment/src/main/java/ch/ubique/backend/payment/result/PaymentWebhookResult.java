package ch.ubique.backend.payment.result;

import ch.ubique.backend.payment.PaymentFailureCode;
import ch.ubique.backend.payment.model.PaymentFailure;
import ch.ubique.backend.payment.model.PaymentWebhookEvent;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/** Neutral result returned when parsing and validating a provider webhook. */
public record PaymentWebhookResult(
        boolean successful, boolean ignored, PaymentWebhookEvent event, PaymentFailure failure) {

    public static PaymentWebhookResult success(@NotNull PaymentWebhookEvent event) {
        return new PaymentWebhookResult(true, false, event, null);
    }

    public static PaymentWebhookResult ignoredResult() {
        return new PaymentWebhookResult(false, true, null, null);
    }

    public static PaymentWebhookResult failure(
            @NotNull PaymentFailureCode code, @NotBlank String message) {
        return new PaymentWebhookResult(false, false, null, new PaymentFailure(code, message));
    }
}
