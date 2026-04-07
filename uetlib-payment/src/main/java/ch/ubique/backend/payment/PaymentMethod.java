package ch.ubique.backend.payment;

/** Neutral payment method categories exposed by payment providers. */
public enum PaymentMethod {
    MASTERCARD,
    VISA,
    TWINT,
    APPLE_PAY,
    GOOGLE_PAY,
    PAYPAL,
    POSTFINANCE_CARD,
    POSTFINANCE_EFINANCE,
    UNKNOWN
}
