package ch.uetlib.payment.datatrans.internal;

import java.math.BigDecimal;

public final class DatatransAmountConverter {

    private DatatransAmountConverter() {}

    public static long toMinorUnits(final BigDecimal amount) {
        return amount.movePointRight(2).longValueExact();
    }
}
