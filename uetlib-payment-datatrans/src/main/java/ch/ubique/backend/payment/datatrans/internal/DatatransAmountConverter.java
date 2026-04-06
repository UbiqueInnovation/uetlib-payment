package ch.ubique.backend.payment.datatrans.internal;

import java.math.BigDecimal;

public final class DatatransAmountConverter {

    private DatatransAmountConverter() {}

    public static int toMinorUnits(final BigDecimal amount) {
        return amount.movePointRight(2).intValueExact();
    }
}
