package ch.ubique.backend.payment.postfinance.internal;

import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.zalando.logbook.Logbook;
import org.zalando.logbook.core.Conditions;
import org.zalando.logbook.core.DefaultSink;
import org.zalando.logbook.core.DefaultStrategy;
import org.zalando.logbook.core.HeaderFilters;
import org.zalando.logbook.core.SplunkHttpLogFormatter;
import org.zalando.logbook.httpclient5.LogbookHttpExecHandler;

import java.util.Set;

public final class PostfinanceHttpClientFactory {

    private static final String POSTFINANCE_API_PATTERN = "https://checkout.postfinance.ch/**";
    private static final Set<String> SENSITIVE_HEADERS =
            Set.of("Authorization", "Cookie", "Set-Cookie");

    private PostfinanceHttpClientFactory() {}

    public static CloseableHttpClient create(final boolean loggingEnabled) {
        return create(loggingEnabled, POSTFINANCE_API_PATTERN);
    }

    static CloseableHttpClient create(final boolean loggingEnabled, final String apiPattern) {
        if (!loggingEnabled) {
            return HttpClients.createDefault();
        }

        final var logbook =
                Logbook.builder()
                        .condition(Conditions.requestTo(apiPattern))
                        .headerFilter(HeaderFilters.replaceHeaders(SENSITIVE_HEADERS, "XXX"))
                        .strategy(new DefaultStrategy())
                        .sink(
                                new DefaultSink(
                                        new SplunkHttpLogFormatter(),
                                        new PostfinanceHttpLogWriter()))
                        .build();

        return HttpClients.custom()
                .addExecInterceptorFirst("postfinance-logbook", new LogbookHttpExecHandler(logbook))
                .build();
    }
}
