package ch.ubique.uetlib.payment.postfinance.internal;

import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zalando.logbook.Correlation;
import org.zalando.logbook.HttpLogWriter;
import org.zalando.logbook.Precorrelation;

public final class PostfinanceHttpLogWriter implements HttpLogWriter {

    public static final String LOGGER_NAME = "ch.ubique.uetlib.payment.postfinance.http";

    private static final Logger logger = LoggerFactory.getLogger(LOGGER_NAME);

    @Override
    public boolean isActive() {
        return logger.isTraceEnabled();
    }

    @Override
    public void write(final @NonNull Precorrelation precorrelation, final @NonNull String request) {
        logger.trace(request);
    }

    @Override
    public void write(final @NonNull Correlation correlation, final @NonNull String response) {
        logger.trace(response);
    }
}
