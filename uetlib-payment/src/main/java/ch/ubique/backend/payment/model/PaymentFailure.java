package ch.ubique.backend.payment.model;

import ch.ubique.backend.payment.PaymentFailureCode;

/** Neutral payment-provider error information. */
public record PaymentFailure(PaymentFailureCode code, String message) {}
