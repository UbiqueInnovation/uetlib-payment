package ch.ubique.uetlib.payment.result;

import ch.ubique.uetlib.payment.PaymentFailureCode;
import ch.ubique.uetlib.payment.PaymentMethod;
import ch.ubique.uetlib.payment.TransactionStatus;
import ch.ubique.uetlib.payment.model.PaymentFailure;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import org.jspecify.annotations.Nullable;

/** Neutral result returned when fetching a payment status from a provider. */
public record PaymentStatusResult(
        boolean successful,
        @Nullable TransactionStatus paymentStatus,
        @Nullable String rawProviderStatus,
        @Nullable String currency,
        @Nullable BigDecimal authorizationAmount,
        @Nullable PaymentMethod paymentMethod,
        @Nullable String failureReason,
        @Nullable String failureMessage,
        @Nullable PaymentFailure failure) {

    public static PaymentStatusResult success(
            @NotNull final TransactionStatus paymentStatus,
            @Nullable final String rawProviderStatus,
            @Nullable final String currency,
            @Nullable final BigDecimal authorizationAmount,
            @Nullable final PaymentMethod paymentMethod,
            @Nullable final String failureReason,
            @Nullable final String failureMessage) {
        return new PaymentStatusResult(
                true,
                paymentStatus,
                rawProviderStatus,
                currency,
                authorizationAmount,
                paymentMethod,
                failureReason,
                failureMessage,
                null);
    }

    public static PaymentStatusResult failure(
            @NotNull final PaymentFailureCode code, @NotBlank final String message) {
        return new PaymentStatusResult(
                false, null, null, null, null, null, null, null, new PaymentFailure(code, message));
    }
}
