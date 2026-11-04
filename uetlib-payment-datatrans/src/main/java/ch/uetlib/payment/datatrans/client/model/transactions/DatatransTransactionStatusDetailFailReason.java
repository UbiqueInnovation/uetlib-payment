package ch.uetlib.payment.datatrans.client.model.transactions;

import com.fasterxml.jackson.annotation.JsonProperty;

public enum DatatransTransactionStatusDetailFailReason {
    @JsonProperty("card_invalid")
    CARD_INVALID,
    @JsonProperty("declined")
    DECLINED,
    @JsonProperty("soft_declined")
    SOFT_DECLINED,
    @JsonProperty("incomplete")
    INCOMPLETE,
    @JsonProperty("timeout")
    TIMEOUT,
    @JsonProperty("invalid_setup")
    INVALID_SETUP,
    @JsonProperty("secure_authentication")
    SECURE_AUTHENTICATION,
    @JsonProperty("error_on_start")
    ERROR_ON_START,
    @JsonProperty("referral")
    REFERRAL,
    @JsonProperty("error")
    ERROR
}
