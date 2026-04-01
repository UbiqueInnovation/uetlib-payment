package ch.ubique.backend.payment;

/** Neutral payment-provider error information. */
public record PaymentFailure(PaymentFailureCode code, String message) {}
