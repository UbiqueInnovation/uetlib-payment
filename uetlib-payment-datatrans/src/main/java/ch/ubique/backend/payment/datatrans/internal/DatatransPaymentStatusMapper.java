package ch.ubique.backend.payment.datatrans.internal;

import ch.ubique.backend.payment.PaymentMethod;
import ch.ubique.backend.payment.TransactionStatus;
import ch.ubique.backend.payment.model.PaymentWebhookEvent;
import ch.ubique.backend.payment.result.PaymentStatusResult;
import ch.ubique.datatrans.model.transactions.DatatransPaymentMethod;
import ch.ubique.datatrans.model.transactions.DatatransTransactionStatus;
import ch.ubique.datatrans.model.transactions.DatatransTransactionStatusResponse;

public class DatatransPaymentStatusMapper {

    public PaymentStatusResult toStatusResult(final DatatransTransactionStatusResponse response) {
        return PaymentStatusResult.success(
                mapStatus(response.status()),
                response.status().name(),
                response.currency().name(),
                null,
                mapPaymentMethod(response.paymentMethod()),
                extractFailureReason(response),
                extractFailureMessage(response));
    }

    public PaymentWebhookEvent toWebhookEvent(final DatatransTransactionStatusResponse response) {
        return new PaymentWebhookEvent(
                response.transactionId(),
                mapStatus(response.status()),
                response.status().name(),
                mapPaymentMethod(response.paymentMethod()),
                response.currency().name(),
                extractFailureReason(response),
                extractFailureMessage(response));
    }

    public TransactionStatus mapStatus(final DatatransTransactionStatus status) {
        if (status == null) {
            return TransactionStatus.FAILED;
        }
        return switch (status) {
            case INITIALIZED, CHALLENGE_REQUIRED, CHALLENGE_ONGOING -> TransactionStatus.PENDING;
            case AUTHENTICATED, AUTHORIZED -> TransactionStatus.AUTHORIZED;
            case SETTLED, TRANSMITTED -> TransactionStatus.COMPLETED;
            case CANCELED -> TransactionStatus.VOIDED;
            case FAILED -> TransactionStatus.FAILED;
        };
    }

    private PaymentMethod mapPaymentMethod(final DatatransPaymentMethod paymentMethod) {
        if (paymentMethod == null) {
            return null;
        }
        return switch (paymentMethod) {
            case ECA, VIS -> PaymentMethod.CARD;
            case TWI -> PaymentMethod.TWINT;
            case APL -> PaymentMethod.APPLE_PAY;
            case PAY -> PaymentMethod.PAYPAL;
            case PFC -> PaymentMethod.POSTFINANCE_CARD;
            case PEF -> PaymentMethod.POSTFINANCE_EFINANCE;
            default -> PaymentMethod.UNKNOWN;
        };
    }

    private String extractFailureReason(final DatatransTransactionStatusResponse response) {
        if (response.detail().fail() == null) {
            return null;
        }
        return response.detail().fail().reason().name();
    }

    private String extractFailureMessage(final DatatransTransactionStatusResponse response) {
        if (response.detail().fail() == null) {
            return null;
        }
        if (response.detail().fail().fail() != null && !response.detail().fail().fail().isBlank()) {
            return response.detail().fail().fail();
        }
        return null;
    }
}
