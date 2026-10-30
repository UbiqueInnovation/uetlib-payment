package ch.ubique.uetlib.payment.result;

import ch.ubique.uetlib.payment.PaymentFailureCode;
import ch.ubique.uetlib.payment.model.PaymentFailure;
import ch.ubique.uetlib.payment.model.PaymentWebhookEvent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.jspecify.annotations.Nullable;

/** Neutral result returned when parsing and validating a provider webhook. */
public record PaymentWebhookResult(
        boolean successful,
        boolean ignored,
        @Nullable PaymentWebhookEvent event,
        @Nullable PaymentFailure failure) {

    public static PaymentWebhookResult success(@NotNull final PaymentWebhookEvent event) {
        return new PaymentWebhookResult(true, false, event, null);
    }

    public static PaymentWebhookResult ignoredResult() {
        return new PaymentWebhookResult(false, true, null, null);
    }

    public static PaymentWebhookResult failure(
            @NotNull final PaymentFailureCode code, @NotBlank final String message) {
        return new PaymentWebhookResult(false, false, null, new PaymentFailure(code, message));
    }
}
