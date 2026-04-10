package ch.ubique.backend.payment.result;

import ch.ubique.backend.payment.PaymentFailureCode;
import ch.ubique.backend.payment.TransactionStatus;
import ch.ubique.backend.payment.model.PaymentFailure;

import java.math.BigDecimal;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/** Provider-neutral response returned after successful payment initialization. */
public record PaymentInitResult(
        boolean successful,
        String providerTransactionId,
        String javascriptUrl,
        String paymentPageUrl,
        String providerReference,
        TransactionStatus status,
        String currency,
        BigDecimal authorizationAmount,
        Integer version,
        PaymentFailure failure) {

    public static PaymentInitResult success(
            @NotBlank String providerTransactionId,
            String javascriptUrl,
            String paymentPageUrl,
            @NotBlank String providerReference,
            @NotNull TransactionStatus status,
            String currency,
            BigDecimal authorizationAmount,
            Integer version) {
        return new PaymentInitResult(
                true,
                providerTransactionId,
                javascriptUrl,
                paymentPageUrl,
                providerReference,
                status,
                currency,
                authorizationAmount,
                version,
                null);
    }

    public static PaymentInitResult failure(
            @NotNull PaymentFailureCode code, @NotBlank String message) {
        return new PaymentInitResult(
                false, null, null, null, null, null, null, null, null, new PaymentFailure(code, message));
    }
}
