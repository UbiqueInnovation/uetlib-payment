package ch.ubique.backend.payment.model;

import ch.ubique.backend.payment.PaymentMethod;
import ch.ubique.backend.payment.TransactionStatus;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/** Neutral webhook event emitted by a payment provider adapter. */
public record PaymentWebhookEvent(
        @NotBlank String providerTransactionId,
        @NotNull TransactionStatus paymentStatus,
        String rawProviderStatus,
        PaymentMethod paymentMethod,
        String currency,
        String failureReason,
        String failureMessage) {

    public PaymentWebhookEvent(
            @NotBlank String providerTransactionId, @NotNull TransactionStatus paymentStatus) {
        this(providerTransactionId, paymentStatus, null, null, null, null, null);
    }
}
