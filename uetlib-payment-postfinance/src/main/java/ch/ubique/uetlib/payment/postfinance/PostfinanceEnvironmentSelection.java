package ch.ubique.uetlib.payment.postfinance;

/** Selects the PostFinance transaction environment independently of the SDK API. */
public enum PostfinanceEnvironmentSelection {
    USE_CONFIGURATION,
    FORCE_TEST,
    FORCE_PRODUCTION
}
