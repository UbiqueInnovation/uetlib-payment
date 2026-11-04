package ch.uetlib.payment.model;

/** Neutral billing address passed to a payment provider. */
public record PaymentBillingAddress(
        String street,
        String houseNumber,
        String addendum,
        String city,
        String zip,
        String countryCode) {}
