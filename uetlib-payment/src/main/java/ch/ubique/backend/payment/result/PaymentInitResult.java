package ch.ubique.backend.payment.result;

import ch.ubique.backend.payment.PaymentFailureCode;
import ch.ubique.backend.payment.TransactionStatus;
import ch.ubique.backend.payment.model.PaymentFailure;

import java.math.BigDecimal;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

/** Provider-neutral response returned after successful payment initialization. */
public record PaymentInitResult(
        boolean successful,
        @Nullable String providerTransactionId,
        @Nullable String javascriptUrl,
        @Nullable String paymentPageUrl,
        @Nullable String providerReference,
        @Nullable TransactionStatus status,
        @Nullable String currency,
        @Nullable BigDecimal authorizationAmount,
        @Nullable Integer version,
        @Nullable PaymentFailure failure) {

    public static PaymentInitResult success(
            @NonNull String providerTransactionId,
            @Nullable String javascriptUrl,
            @Nullable String paymentPageUrl,
            @NonNull String providerReference,
            @NonNull TransactionStatus status,
            @Nullable String currency,
            @Nullable BigDecimal authorizationAmount,
            @Nullable Integer version) {
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
            @NonNull PaymentFailureCode code, @NonNull String message) {
        return new PaymentInitResult(
                false, null, null, null, null, null, null, null, null, new PaymentFailure(code, message));
    }
}
