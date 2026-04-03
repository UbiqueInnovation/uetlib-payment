package ch.ubique.backend.payment.model;

import ch.ubique.backend.payment.PaymentMethod;
import ch.ubique.backend.payment.TransactionStatus;

/** Neutral webhook event emitted by a payment provider adapter. */
public record PaymentWebhookEvent(
        String providerTransactionId,
        TransactionStatus paymentStatus,
        String rawProviderStatus,
        PaymentMethod paymentMethod,
        String currency,
        String failureReason,
        String failureMessage) {

    public PaymentWebhookEvent(String providerTransactionId, TransactionStatus paymentStatus) {
        this(providerTransactionId, paymentStatus, null, null, null, null, null);
    }
}
