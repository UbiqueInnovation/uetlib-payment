package ch.ubique.backend.payment.result;

import ch.ubique.backend.payment.PaymentFailureCode;
import ch.ubique.backend.payment.PaymentMethod;
import ch.ubique.backend.payment.TransactionStatus;
import ch.ubique.backend.payment.model.PaymentFailure;

import java.math.BigDecimal;

/** Neutral result returned when fetching a payment status from a provider. */
public record PaymentStatusResult(
        boolean successful,
        TransactionStatus paymentStatus,
        String rawProviderStatus,
        String currency,
        BigDecimal authorizationAmount,
        PaymentMethod paymentMethod,
        String failureReason,
        String failureMessage,
        PaymentFailure failure) {

    public static PaymentStatusResult success(
            TransactionStatus paymentStatus,
            String rawProviderStatus,
            String currency,
            BigDecimal authorizationAmount,
            PaymentMethod paymentMethod,
            String failureReason,
            String failureMessage) {
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

    public static PaymentStatusResult failure(PaymentFailureCode code, String message) {
        return new PaymentStatusResult(
                false, null, null, null, null, null, null, null, new PaymentFailure(code, message));
    }
}
