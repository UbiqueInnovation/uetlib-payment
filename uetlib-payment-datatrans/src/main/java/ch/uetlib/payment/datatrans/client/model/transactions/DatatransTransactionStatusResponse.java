package ch.uetlib.payment.datatrans.client.model.transactions;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import org.jspecify.annotations.Nullable;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public record DatatransTransactionStatusResponse(
        @JsonProperty(required = true) @NotBlank String transactionId,
        @JsonProperty(required = true) @NotBlank String merchantId,
        @JsonProperty(required = true) @NotNull DatatransTransactionType type,
        @JsonProperty(required = true) @NotNull DatatransTransactionStatus status,
        @JsonProperty(required = true) @NotNull DatatransCurrency currency,
        @JsonProperty(required = true) @NotBlank @Size(min = 1, max = 40) String refno,
        @Nullable @Size(min = 1, max = 40) String refno2,
        @JsonProperty(required = true) @NotNull DatatransPaymentMethod paymentMethod,
        @JsonProperty(required = true) @NotNull DatatransTransactionStatusDetail detail,
        @Nullable DatatransTransactionStatusCard card) {}
