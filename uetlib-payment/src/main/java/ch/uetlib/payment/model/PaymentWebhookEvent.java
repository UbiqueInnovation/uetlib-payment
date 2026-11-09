package ch.uetlib.payment.model;

import ch.uetlib.payment.PaymentMethod;
import ch.uetlib.payment.TransactionStatus;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import org.jspecify.annotations.Nullable;

/** Neutral webhook event emitted by a payment provider adapter. */
public record PaymentWebhookEvent(
        @NotBlank String providerTransactionId,
        @NotNull TransactionStatus paymentStatus,
        @Nullable String rawProviderStatus,
        @Nullable PaymentMethod paymentMethod,
        @Nullable String currency,
        @Nullable String failureReason,
        @Nullable String failureMessage) {

    public PaymentWebhookEvent(
            @NotBlank final String providerTransactionId,
            @NotNull final TransactionStatus paymentStatus) {
        this(providerTransactionId, paymentStatus, null, null, null, null, null);
    }
}
