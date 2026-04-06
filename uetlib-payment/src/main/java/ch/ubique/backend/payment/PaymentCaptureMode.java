package ch.ubique.backend.payment;

/**
 * Controls when the provider should complete/capture a payment.
 *
 * <p>{@link #AUTOMATIC} means the provider should complete/capture the payment as part of its
 * normal checkout flow without a separate backend capture/completion call.
 *
 * <p>{@link #MANUAL} means the provider should authorize first and wait for a separate backend
 * capture/completion step.
 */
public enum PaymentCaptureMode {
    AUTOMATIC,
    MANUAL
}
