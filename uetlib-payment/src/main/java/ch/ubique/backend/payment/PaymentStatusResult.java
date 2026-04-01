package ch.ubique.backend.payment;

import java.math.BigDecimal;

/** Neutral result returned when fetching a payment status from a provider. */
public record PaymentStatusResult(
        boolean successful,
        TransactionStatus status,
        String currency,
        BigDecimal authorizationAmount,
        String failureReason,
        PaymentFailure failure) {

    public static PaymentStatusResult success(
            TransactionStatus status,
            String currency,
            BigDecimal authorizationAmount,
            String failureReason) {
        return new PaymentStatusResult(
                true, status, currency, authorizationAmount, failureReason, null);
    }

    public static PaymentStatusResult failure(PaymentFailureCode code, String message) {
        return new PaymentStatusResult(
                false, null, null, null, null, new PaymentFailure(code, message));
    }
}
