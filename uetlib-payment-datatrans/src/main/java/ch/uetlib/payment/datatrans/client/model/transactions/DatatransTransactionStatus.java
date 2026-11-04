package ch.uetlib.payment.datatrans.client.model.transactions;

import com.fasterxml.jackson.annotation.JsonProperty;

public enum DatatransTransactionStatus {
    @JsonProperty("initialized")
    INITIALIZED,
    @JsonProperty("challenge_required")
    CHALLENGE_REQUIRED,
    @JsonProperty("challenge_ongoing")
    CHALLENGE_ONGOING,
    @JsonProperty("authenticated")
    AUTHENTICATED,
    @JsonProperty("authorized")
    AUTHORIZED,
    @JsonProperty("settled")
    SETTLED,
    @JsonProperty("canceled")
    CANCELED,
    @JsonProperty("transmitted")
    TRANSMITTED,
    @JsonProperty("failed")
    FAILED
}
