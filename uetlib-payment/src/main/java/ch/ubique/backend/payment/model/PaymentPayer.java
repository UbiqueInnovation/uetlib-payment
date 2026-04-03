package ch.ubique.backend.payment.model;

/** Provider-neutral payer data passed to a payment provider. */
public record PaymentPayer(
        String customerReference, String givenName, String familyName, String email) {}
