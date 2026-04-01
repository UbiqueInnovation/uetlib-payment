package ch.ubique.backend.payment;

/** Neutral webhook event emitted by a payment provider adapter. */
public record PaymentWebhookEvent(String providerTransactionId, TransactionStatus status) {}
