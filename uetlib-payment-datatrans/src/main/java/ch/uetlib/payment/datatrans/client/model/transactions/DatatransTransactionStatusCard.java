package ch.uetlib.payment.datatrans.client.model.transactions;

import ch.uetlib.core.validator.NullOrNotBlank;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import org.jspecify.annotations.Nullable;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public record DatatransTransactionStatusCard(
        @Nullable @NullOrNotBlank String alias,
        @Nullable @Size(min = 24, max = 24) @Pattern(regexp = "F-[A-Za-z0-9_-]{22}")
                String fingerprint,
        @Nullable String masked,
        @Nullable @Size(min = 2, max = 2) @Pattern(regexp = "\\d{2}") String expiryMonth,
        @Nullable @Size(min = 2, max = 2) @Pattern(regexp = "\\d{2}") String expiryYear) {}
