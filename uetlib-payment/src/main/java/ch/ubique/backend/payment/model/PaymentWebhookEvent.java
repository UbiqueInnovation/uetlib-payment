package ch.ubique.backend.payment.model;

import ch.ubique.backend.payment.PaymentMethod;
import ch.ubique.backend.payment.TransactionStatus;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

/** Neutral webhook event emitted by a payment provider adapter. */
public record PaymentWebhookEvent(
        @NonNull String providerTransactionId,
        @NonNull TransactionStatus paymentStatus,
        @Nullable String rawProviderStatus,
        @Nullable PaymentMethod paymentMethod,
        @Nullable String currency,
        @Nullable String failureReason,
        @Nullable String failureMessage) {

    public PaymentWebhookEvent(
            @NonNull String providerTransactionId, @NonNull TransactionStatus paymentStatus) {
        this(providerTransactionId, paymentStatus, null, null, null, null, null);
    }
}
