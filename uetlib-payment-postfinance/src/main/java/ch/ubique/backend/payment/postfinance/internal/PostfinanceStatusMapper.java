package ch.ubique.backend.payment.postfinance.internal;

import ch.postfinancecheckout.sdk.model.Transaction;
import ch.postfinancecheckout.sdk.model.TransactionCompletion;
import ch.postfinancecheckout.sdk.model.TransactionCompletionState;
import ch.postfinancecheckout.sdk.model.TransactionState;
import ch.postfinancecheckout.sdk.model.TransactionVoid;
import ch.postfinancecheckout.sdk.model.TransactionVoidState;
import ch.ubique.backend.payment.PaymentFailureCode;
import ch.ubique.backend.payment.PaymentMethod;
import ch.ubique.backend.payment.TransactionStatus;
import ch.ubique.backend.payment.result.PaymentStatusResult;

import org.apache.commons.lang3.StringUtils;

import java.util.Map;

public class PostfinanceStatusMapper {

    public TransactionStatus getRequiredTransactionStatus(final Transaction transaction) {
        if (transaction == null || transaction.getState() == null) {
            return null;
        }
        return mapTransactionState(transaction.getState());
    }

    public PaymentStatusResult toStatusResult(final Transaction transaction) {
        final var status = getRequiredTransactionStatus(transaction);
        if (status == null) {
            return PaymentStatusResult.failure(
                    PaymentFailureCode.INVALID_PROVIDER_RESPONSE,
                    "PostFinance returned a transaction without state");
        }
        final var failureReason = extractFailureReason(transaction);
        return PaymentStatusResult.success(
                status,
                transaction.getState().name(),
                transaction.getCurrency(),
                transaction.getAuthorizationAmount(),
                PaymentMethod.UNKNOWN,
                failureReason,
                failureReason);
    }

    public boolean isCaptureSuccessful(final TransactionCompletion completion) {
        if (completion == null || completion.getState() == null) {
            return false;
        }
        return completion.getState() == TransactionCompletionState.SUCCESSFUL
                || completion.getState() == TransactionCompletionState.PENDING
                || completion.getState() == TransactionCompletionState.SCHEDULED;
    }

    public boolean isCancelSuccessful(final TransactionVoid transactionVoid) {
        if (transactionVoid == null || transactionVoid.getState() == null) {
            return false;
        }
        return transactionVoid.getState() == TransactionVoidState.SUCCESSFUL
                || transactionVoid.getState() == TransactionVoidState.PENDING;
    }

    public TransactionStatus mapTransactionState(final TransactionState state) {
        if (state == null) {
            return null;
        }
        return switch (state) {
            case CREATE, PENDING, CONFIRMED, PROCESSING -> TransactionStatus.PENDING;
            case AUTHORIZED -> TransactionStatus.AUTHORIZED;
            case COMPLETED, FULFILL -> TransactionStatus.COMPLETED;
            case VOIDED -> TransactionStatus.VOIDED;
            case FAILED, DECLINE -> TransactionStatus.FAILED;
        };
    }

    public String extractFailureReason(final Transaction transaction) {
        if (transaction.getFailureReason() == null) {
            return null;
        }
        final var description =
                getPreferredLocalizedValue(transaction.getFailureReason().getDescription());
        if (StringUtils.isNotBlank(description)) {
            return description;
        }
        return getPreferredLocalizedValue(transaction.getFailureReason().getName());
    }

    private String getPreferredLocalizedValue(final Map<String, String> values) {
        if (values == null || values.isEmpty()) {
            return null;
        }
        final var english = values.get("en-US");
        if (StringUtils.isNotBlank(english)) {
            return english;
        }
        return values.values().stream().filter(StringUtils::isNotBlank).findFirst().orElse(null);
    }
}
