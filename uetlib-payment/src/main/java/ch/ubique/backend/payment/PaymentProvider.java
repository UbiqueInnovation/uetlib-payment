package ch.ubique.backend.payment;

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
    PaymentWebhookResult processWebhook(String rawPayload, Map<String, String> headers);
}
