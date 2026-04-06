package ch.ubique.backend.payment;

/** Neutral provider failure categories exposed by the shared payment boundary. */
public enum PaymentFailureCode {
    PROVIDER_API_ERROR,
    INVALID_PROVIDER_RESPONSE,
    INVALID_PROVIDER_REFERENCE,
    UNSUPPORTED_OPERATION
}
