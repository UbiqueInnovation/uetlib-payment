package ch.uetlib.payment.datatrans.client.model.transactions;

import com.fasterxml.jackson.annotation.JsonProperty;

public enum DatatransTransactionType {
    @JsonProperty("payment")
    PAYMENT,
    @JsonProperty("credit")
    CREDIT,
    @JsonProperty("card_check")
    CARD_CHECK
}
