package ch.uetlib.payment.result;

import ch.uetlib.payment.PaymentFailureCode;
import ch.uetlib.payment.TransactionStatus;
import ch.uetlib.payment.model.PaymentFailure;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import org.jspecify.annotations.Nullable;

import java.math.BigDecimal;

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
            @NotBlank final String providerTransactionId,
            @Nullable final String javascriptUrl,
            @Nullable final String paymentPageUrl,
            @NotBlank final String providerReference,
            @NotNull final TransactionStatus status,
            @Nullable final String currency,
            @Nullable final BigDecimal authorizationAmount,
            @Nullable final Integer version) {
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
            @NotNull final PaymentFailureCode code, @NotBlank final String message) {
        return new PaymentInitResult(
                false,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                new PaymentFailure(code, message));
    }
}
