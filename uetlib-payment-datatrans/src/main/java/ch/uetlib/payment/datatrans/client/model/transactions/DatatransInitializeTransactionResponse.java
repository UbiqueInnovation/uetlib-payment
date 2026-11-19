package ch.uetlib.payment.datatrans.client.model.transactions;

import static java.util.Objects.requireNonNull;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.constraints.NotBlank;

import org.jspecify.annotations.Nullable;

/**
 * @param redirectUrl not set in response from datatrans, but returned as header `Location` and set
 *     by the DatatransTransactionsClient
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public record DatatransInitializeTransactionResponse(
        @JsonProperty(required = true) @NotBlank String transactionId,
        @JsonProperty(required = true) @NotBlank String redirectUrl) {

    public static DatatransInitializeTransactionResponseBuilder builder() {
        return DatatransInitializeTransactionResponseBuilder.builder();
    }

    public DatatransInitializeTransactionResponseBuilder toBuilder() {
        return new DatatransInitializeTransactionResponseBuilder(this);
    }

    public static final class DatatransInitializeTransactionResponseBuilder {
        private @Nullable String transactionId;
        private @Nullable String redirectUrl;

        private DatatransInitializeTransactionResponseBuilder() {}

        private DatatransInitializeTransactionResponseBuilder(
                final DatatransInitializeTransactionResponse other) {
            this.transactionId = other.transactionId();
            this.redirectUrl = other.redirectUrl();
        }

        public static DatatransInitializeTransactionResponseBuilder builder() {
            return new DatatransInitializeTransactionResponseBuilder();
        }

        public DatatransInitializeTransactionResponseBuilder transactionId(
                final String transactionId) {
            this.transactionId = transactionId;
            return this;
        }

        public DatatransInitializeTransactionResponseBuilder redirectUrl(final String redirectUrl) {
            this.redirectUrl = redirectUrl;
            return this;
        }

        public DatatransInitializeTransactionResponse build() {
            requireNonNull(transactionId, "transactionId must not be null");
            requireNonNull(redirectUrl, "redirectUrl must not be null");

            return new DatatransInitializeTransactionResponse(transactionId, redirectUrl);
        }
    }
}
