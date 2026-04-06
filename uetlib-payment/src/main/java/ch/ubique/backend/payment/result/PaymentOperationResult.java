package ch.ubique.backend.payment.result;

import ch.ubique.backend.payment.PaymentFailureCode;
import ch.ubique.backend.payment.model.PaymentFailure;

/** Neutral result returned for payment operations such as capture and cancel. */
public record PaymentOperationResult(boolean successful, PaymentFailure failure) {

    public static PaymentOperationResult success() {
        return new PaymentOperationResult(true, null);
    }

    public static PaymentOperationResult failure(PaymentFailureCode code, String message) {
        return new PaymentOperationResult(false, new PaymentFailure(code, message));
    }
}
