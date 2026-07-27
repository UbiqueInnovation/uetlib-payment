package ch.ubique.backend.payment.datatrans.internal;

import ch.ubique.backend.payment.PaymentMethod;
import ch.ubique.backend.payment.TransactionStatus;
import ch.ubique.backend.payment.model.PaymentWebhookEvent;
import ch.ubique.backend.payment.result.PaymentStatusResult;
import ch.ubique.datatrans.model.transactions.DatatransPaymentMethod;
import ch.ubique.datatrans.model.transactions.DatatransTransactionStatus;
import ch.ubique.datatrans.model.transactions.DatatransTransactionStatusDetailFail;
import ch.ubique.datatrans.model.transactions.DatatransTransactionStatusResponse;

import org.jspecify.annotations.Nullable;

public class DatatransPaymentStatusMapper {

    public PaymentStatusResult toStatusResult(
            final @Nullable DatatransTransactionStatusResponse response) {
        final var status = mapStatus(response == null ? null : response.status());
        final var rawProviderStatus = response == null ? null : response.status().name();
        final var currency = response == null ? null : response.currency().name();
        final var paymentMethod =
                response == null ? null : mapPaymentMethod(response.paymentMethod());
        return PaymentStatusResult.success(
                status,
                rawProviderStatus,
                currency,
                null,
                paymentMethod,
                extractFailureReason(response),
                extractFailureMessage(response));
    }

    public PaymentWebhookEvent toWebhookEvent(
            final @Nullable DatatransTransactionStatusResponse response) {
        final var status = mapStatus(response == null ? null : response.status());
        final var rawProviderStatus = response == null ? null : response.status().name();
        final var transactionId = response == null ? "" : response.transactionId();
        final var currency = response == null ? null : response.currency().name();
        final var paymentMethod =
                response == null ? null : mapPaymentMethod(response.paymentMethod());
        return new PaymentWebhookEvent(
                transactionId,
                status,
                rawProviderStatus,
                paymentMethod,
                currency,
                extractFailureReason(response),
                extractFailureMessage(response));
    }

    public TransactionStatus mapStatus(final @Nullable DatatransTransactionStatus status) {
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

    private PaymentMethod mapPaymentMethod(final @Nullable DatatransPaymentMethod paymentMethod) {
        if (paymentMethod == null) {
            return PaymentMethod.UNKNOWN;
        }
        return switch (paymentMethod) {
            case ECA -> PaymentMethod.MASTERCARD;
            case VIS -> PaymentMethod.VISA;
            case TWI -> PaymentMethod.TWINT;
            case APL -> PaymentMethod.APPLE_PAY;
            case PAY -> PaymentMethod.GOOGLE_PAY;
            case PAP -> PaymentMethod.PAYPAL;
            case PFC -> PaymentMethod.POSTFINANCE_CARD;
            case PEF -> PaymentMethod.POSTFINANCE_EFINANCE;
            default -> PaymentMethod.UNKNOWN;
        };
    }

    private @Nullable String extractFailureReason(
            final @Nullable DatatransTransactionStatusResponse response) {
        final var failDetail = extractFailDetail(response);
        if (failDetail == null || failDetail.reason() == null) {
            return null;
        }
        return failDetail.reason().name();
    }

    private @Nullable String extractFailureMessage(
            final @Nullable DatatransTransactionStatusResponse response) {
        final var failDetail = extractFailDetail(response);
        if (failDetail == null || failDetail.message() == null || failDetail.message().isBlank()) {
            return null;
        }
        return failDetail.message();
    }

    private @Nullable DatatransTransactionStatusDetailFail extractFailDetail(
            final @Nullable DatatransTransactionStatusResponse response) {
        if (response == null) {
            return null;
        }
        final var detail = response.detail();
        if (detail == null) {
            return null;
        }
        return detail.fail();
    }
}
