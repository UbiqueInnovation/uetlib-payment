package ch.uetlib.payment.datatrans.client.model.transactions;

import static java.util.Objects.requireNonNull;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import org.jspecify.annotations.Nullable;

import java.util.List;

/**
 * @param currency
 * @param refno
 * @param amount Specified in the currency smallest unit. E.g. 1000 for 10.00 CHF
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public record DatatransInitializeTransaction(
        @JsonProperty(required = true) @NotNull DatatransCurrency currency,
        @JsonProperty(required = true) @NotBlank @Size(min = 1, max = 40) String refno,
        @Nullable @Positive Long amount,
        @Nullable List<DatatransPaymentMethod> paymentMethods,
        @Nullable Boolean autoSettle,
        @Nullable String language,
        @Nullable DatatransInitializeTransactionTheme theme,
        @Nullable DatatransInitializeTransactionRedirect redirect,
        @Nullable DatatransInitializeTransactionWebhook webhook) {

    public static DatatransInitializeTransactionBuilder builder() {
        return new DatatransInitializeTransactionBuilder();
    }

    public DatatransInitializeTransactionBuilder toBuilder() {
        return new DatatransInitializeTransactionBuilder(this);
    }

    public static final class DatatransInitializeTransactionBuilder {
        private @Nullable DatatransCurrency currency;
        private @Nullable @Size(min = 1, max = 40) String refno;
        private @Nullable @Positive Long amount;
        private @Nullable List<DatatransPaymentMethod> paymentMethods;
        private @Nullable Boolean autoSettle;
        private @Nullable String language;
        private @Nullable DatatransInitializeTransactionTheme theme;
        private @Nullable DatatransInitializeTransactionRedirect redirect;
        private @Nullable DatatransInitializeTransactionWebhook webhook;

        private DatatransInitializeTransactionBuilder() {}

        private DatatransInitializeTransactionBuilder(final DatatransInitializeTransaction other) {
            this.currency = other.currency();
            this.refno = other.refno();
            this.amount = other.amount();
            this.paymentMethods = other.paymentMethods();
            this.autoSettle = other.autoSettle();
            this.language = other.language();
            this.theme = other.theme();
            this.redirect = other.redirect();
            this.webhook = other.webhook();
        }

        public DatatransInitializeTransactionBuilder currency(final DatatransCurrency currency) {
            this.currency = currency;
            return this;
        }

        public DatatransInitializeTransactionBuilder refno(final String refno) {
            this.refno = refno;
            return this;
        }

        public DatatransInitializeTransactionBuilder amount(final Long amount) {
            this.amount = amount;
            return this;
        }

        public DatatransInitializeTransactionBuilder paymentMethods(
                final List<DatatransPaymentMethod> paymentMethods) {
            this.paymentMethods = paymentMethods;
            return this;
        }

        public DatatransInitializeTransactionBuilder autoSettle(final Boolean autoSettle) {
            this.autoSettle = autoSettle;
            return this;
        }

        public DatatransInitializeTransactionBuilder language(final String language) {
            this.language = language;
            return this;
        }

        public DatatransInitializeTransactionBuilder theme(
                final DatatransInitializeTransactionTheme theme) {
            this.theme = theme;
            return this;
        }

        public DatatransInitializeTransactionBuilder redirect(
                final DatatransInitializeTransactionRedirect redirect) {
            this.redirect = redirect;
            return this;
        }

        public DatatransInitializeTransactionBuilder webhook(
                final DatatransInitializeTransactionWebhook webhook) {
            this.webhook = webhook;
            return this;
        }

        public DatatransInitializeTransaction build() {
            requireNonNull(currency, "currency must not be null");
            requireNonNull(refno, "refno must not be null");

            return new DatatransInitializeTransaction(
                    currency,
                    refno,
                    amount,
                    paymentMethods,
                    autoSettle,
                    language,
                    theme,
                    redirect,
                    webhook);
        }
    }
}
