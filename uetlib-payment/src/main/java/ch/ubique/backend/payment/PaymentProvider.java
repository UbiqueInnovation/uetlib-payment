package ch.ubique.backend.payment;

import ch.ubique.backend.payment.command.PaymentCancelCommand;
import ch.ubique.backend.payment.command.PaymentCaptureCommand;
import ch.ubique.backend.payment.command.PaymentInitCommand;
import ch.ubique.backend.payment.result.PaymentInitResult;
import ch.ubique.backend.payment.result.PaymentOperationResult;
import ch.ubique.backend.payment.result.PaymentStatusResult;
import ch.ubique.backend.payment.result.PaymentWebhookResult;

import org.jspecify.annotations.Nullable;

import java.util.Map;

/**
 * Provider-neutral payment boundary.
 *
 * <p>Application services own persistence and business workflow. Provider implementations own the
 * protocol details of talking to a concrete PSP such as PostFinance or Datatrans.
 */
public interface PaymentProvider {

    /** Returns the concrete provider type implemented by this adapter. */
    PaymentProviderType getType();

    /** Initiates a provider-side payment transaction from a neutral payment command. */
    PaymentInitResult initiate(PaymentInitCommand command);

    /** Fetches the current provider-side status for a known provider transaction id. */
    PaymentStatusResult getStatus(String providerTransactionId);

    /** Validates and parses a raw provider webhook into a neutral webhook result. */
    PaymentWebhookResult processWebhook(String rawPayload, @Nullable Map<String, String> headers);

    /**
     * Captures or settles a previously authorized provider-side payment transaction.
     *
     * <p>Providers that do not support a separate capture step may keep the default unsupported
     * implementation.
     */
    default PaymentOperationResult capture(final PaymentCaptureCommand command) {
        return PaymentOperationResult.failure(
                PaymentFailureCode.UNSUPPORTED_OPERATION,
                "Payment capture is not supported by provider " + getType());
    }

    /**
     * Cancels or voids a previously initialized or authorized provider-side payment transaction.
     *
     * <p>Providers that do not support a separate cancel step may keep the default unsupported
     * implementation.
     */
    default PaymentOperationResult cancel(final PaymentCancelCommand command) {
        return PaymentOperationResult.failure(
                PaymentFailureCode.UNSUPPORTED_OPERATION,
                "Payment cancel is not supported by provider " + getType());
    }
}
