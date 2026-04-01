package ch.ubique.backend.payment;

import java.math.BigDecimal;

/** Provider-neutral response returned after successful payment initialization. */
public record PaymentInitResult(
        boolean successful,
        String providerTransactionId,
        String javascriptUrl,
        String providerReference,
        TransactionStatus status,
        String currency,
        BigDecimal authorizationAmount,
        Integer version,
        PaymentFailure failure) {

    public static PaymentInitResult success(
            String providerTransactionId,
            String javascriptUrl,
            String providerReference,
            TransactionStatus status,
            String currency,
            BigDecimal authorizationAmount,
            Integer version) {
        return new PaymentInitResult(
                true,
                providerTransactionId,
                javascriptUrl,
                providerReference,
                status,
                currency,
                authorizationAmount,
                version,
                null);
    }

    public static PaymentInitResult failure(PaymentFailureCode code, String message) {
        return new PaymentInitResult(
                false, null, null, null, null, null, null, null, new PaymentFailure(code, message));
    }
}
