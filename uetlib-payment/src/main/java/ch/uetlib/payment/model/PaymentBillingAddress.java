package ch.uetlib.payment.model;

import org.jspecify.annotations.Nullable;

/** Neutral billing address passed to a payment provider. */
public record PaymentBillingAddress(
        @Nullable String street,
        @Nullable String houseNumber,
        @Nullable String addendum,
        @Nullable String city,
        @Nullable String zip,
        @Nullable String countryCode) {}
