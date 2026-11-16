package ch.uetlib.payment.datatrans.client.model.transactions;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.constraints.NotNull;

import org.jspecify.annotations.Nullable;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public record DatatransTransactionStatusDetailFail(
        @JsonProperty(required = true) @NotNull DatatransTransactionStatusDetailFailReason reason,
        @Nullable String message) {}
