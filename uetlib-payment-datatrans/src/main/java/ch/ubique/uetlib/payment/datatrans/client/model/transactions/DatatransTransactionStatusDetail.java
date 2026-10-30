package ch.ubique.uetlib.payment.datatrans.client.model.transactions;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import org.jspecify.annotations.Nullable;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public record DatatransTransactionStatusDetail(
        @Nullable DatatransTransactionStatusDetailInit init,
        @Nullable DatatransTransactionStatusDetailAuthorize authorize,
        @Nullable DatatransTransactionStatusDetailSettle settle,
        @Nullable DatatransTransactionStatusDetailCredit credit,
        @Nullable DatatransTransactionStatusDetailCancel cancel,
        @Nullable DatatransTransactionStatusDetailFail fail) {}
