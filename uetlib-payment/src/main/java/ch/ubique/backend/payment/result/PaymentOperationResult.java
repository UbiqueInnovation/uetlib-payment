package ch.ubique.backend.payment.result;

import ch.ubique.backend.payment.PaymentFailureCode;
import ch.ubique.backend.payment.model.PaymentFailure;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import org.jspecify.annotations.Nullable;

/** Neutral result returned for payment operations such as capture and cancel. */
public record PaymentOperationResult(boolean successful, @Nullable PaymentFailure failure) {

    public static PaymentOperationResult success() {
        return new PaymentOperationResult(true, null);
    }

    public static PaymentOperationResult failure(
            @NotNull final PaymentFailureCode code, @NotBlank final String message) {
        return new PaymentOperationResult(false, new PaymentFailure(code, message));
    }
}
