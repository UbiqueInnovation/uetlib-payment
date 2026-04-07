package ch.ubique.backend.payment.result;

import ch.ubique.backend.payment.PaymentFailureCode;
import ch.ubique.backend.payment.PaymentMethod;
import ch.ubique.backend.payment.TransactionStatus;
import ch.ubique.backend.payment.model.PaymentFailure;

import java.math.BigDecimal;

import org.jspecify.annotations.NonNull;
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
            @NonNull TransactionStatus paymentStatus,
            @Nullable String rawProviderStatus,
            @Nullable String currency,
            @Nullable BigDecimal authorizationAmount,
            @NonNull PaymentMethod paymentMethod,
            @Nullable String failureReason,
            @Nullable String failureMessage) {
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
            @NonNull PaymentFailureCode code, @NonNull String message) {
        return new PaymentStatusResult(
                false, null, null, null, null, null, null, null, new PaymentFailure(code, message));
    }
}
