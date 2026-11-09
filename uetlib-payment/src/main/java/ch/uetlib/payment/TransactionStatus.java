package ch.uetlib.payment;

/**
 * Provider-neutral payment lifecycle states.
 *
 * <p>Adapters map provider-specific states into this enum so consuming applications can reason
 * about payment progress without depending on PSP-specific terminology.
 */
public enum TransactionStatus {
    /** Payment exists at the provider but is still awaiting user or provider action. */
    PENDING,
    /** Payment failed and will not complete successfully without a new attempt. */
    FAILED,
    /** Funds were authorized but not yet captured/settled. */
    AUTHORIZED,
    /** Payment was canceled or voided before completion. */
    VOIDED,
    /** Payment completed successfully, typically after capture or settlement. */
    COMPLETED
}
