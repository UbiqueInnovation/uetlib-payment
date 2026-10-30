package ch.ubique.uetlib.payment.datatrans.client.model.transactions;

import ch.ubique.uetlib.core.validator.NullOrNotBlank;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import org.jspecify.annotations.Nullable;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public record DatatransInitializeTransactionRedirect(
        @Nullable @NullOrNotBlank String successUrl,
        @Nullable @NullOrNotBlank String cancelUrl,
        @Nullable @NullOrNotBlank String errorUrl,
        @JsonProperty(required = true) @NotNull String method) {}
