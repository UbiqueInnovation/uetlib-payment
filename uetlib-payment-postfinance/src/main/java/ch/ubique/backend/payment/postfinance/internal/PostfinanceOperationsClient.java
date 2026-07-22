package ch.ubique.backend.payment.postfinance.internal;

import ch.postfinancecheckout.sdk.ApiException;
import ch.postfinancecheckout.sdk.model.CompletionLineItemCreate;
import ch.postfinancecheckout.sdk.model.TransactionCompletion;
import ch.postfinancecheckout.sdk.model.TransactionCompletionDetails;
import ch.postfinancecheckout.sdk.model.TransactionVoid;
import ch.postfinancecheckout.sdk.service.TransactionsService;
import ch.ubique.backend.payment.command.PaymentCaptureCommand;

import org.jspecify.annotations.Nullable;

import java.math.BigDecimal;
import java.util.List;

public class PostfinanceOperationsClient {

    private final long spaceId;
    private final @Nullable String productUniqueId;

    public PostfinanceOperationsClient(final long spaceId, final @Nullable String productUniqueId) {
        this.spaceId = spaceId;
        this.productUniqueId = productUniqueId;
    }

    public @Nullable Long parseProviderTransactionId(final String providerTransactionId) {
        try {
            return Long.valueOf(providerTransactionId);
        } catch (final NumberFormatException e) {
            return null;
        }
    }

    public @Nullable TransactionCompletion capture(
            final TransactionsService transactionsService,
            final Long transactionId,
            final PaymentCaptureCommand command)
            throws ApiException {
        if (requiresPartialCompletion(command.amount())) {
            final var completionDetails = new TransactionCompletionDetails();
            completionDetails.setLastCompletion(true);
            completionDetails.setExternalId(command.providerReference());
            completionDetails.setLineItems(List.of(createCompletionLineItem(command.amount())));
            return transactionsService.postPaymentTransactionsIdCompletePartiallyOnline(
                    transactionId, spaceId, completionDetails, null);
        }
        return transactionsService.postPaymentTransactionsIdCompleteOnline(
                transactionId, spaceId, null);
    }

    public @Nullable TransactionVoid cancel(
            final TransactionsService transactionsService, final Long transactionId)
            throws ApiException {
        return transactionsService.postPaymentTransactionsIdVoidOnline(
                transactionId, spaceId, null);
    }

    private CompletionLineItemCreate createCompletionLineItem(final BigDecimal amount) {
        final var lineItem = new CompletionLineItemCreate();
        lineItem.setAmount(amount);
        lineItem.setQuantity(BigDecimal.ONE);
        if (productUniqueId != null && !productUniqueId.isBlank()) {
            lineItem.setUniqueId(productUniqueId);
        }
        return lineItem;
    }

    private boolean requiresPartialCompletion(final @Nullable BigDecimal amount) {
        return amount != null && amount.signum() > 0;
    }
}
