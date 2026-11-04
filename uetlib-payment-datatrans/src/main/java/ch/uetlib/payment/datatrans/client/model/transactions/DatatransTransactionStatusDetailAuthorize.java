package ch.uetlib.payment.datatrans.client.model.transactions;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.constraints.NotBlank;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public record DatatransTransactionStatusDetailAuthorize(
        @JsonProperty(required = true) long amount,
        @JsonProperty(required = true) @NotBlank String acquirerAuthorizationCode) {}
