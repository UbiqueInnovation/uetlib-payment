package ch.uetlib.payment.postfinance.internal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;

import com.sun.net.httpserver.HttpServer;

import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.apache.hc.core5.http.io.support.ClassicRequestBuilder;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;

class PostfinanceHttpClientFactoryTest {

    @Test
    void logsFullBodiesAndRedactsSensitiveHeaders() throws Exception {
        final var logger = (Logger) LoggerFactory.getLogger(PostfinanceHttpLogWriter.LOGGER_NAME);
        final var previousLevel = logger.getLevel();
        final var previousAdditive = logger.isAdditive();
        final var appender = new ListAppender<ILoggingEvent>();
        appender.start();
        logger.setLevel(Level.TRACE);
        logger.setAdditive(false);
        logger.addAppender(appender);

        final var server = HttpServer.create(new InetSocketAddress("localhost", 0), 0);
        server.createContext(
                "/payment",
                exchange -> {
                    exchange.getRequestBody().readAllBytes();
                    final var body = "{\"response\":\"complete\"}".getBytes(StandardCharsets.UTF_8);
                    exchange.getResponseHeaders().add("Content-Type", "application/json");
                    exchange.getResponseHeaders().add("Set-Cookie", "session=response-secret");
                    exchange.sendResponseHeaders(200, body.length);
                    exchange.getResponseBody().write(body);
                    exchange.close();
                });
        server.start();

        final var baseUrl = "http://localhost:" + server.getAddress().getPort();
        try (final var client = PostfinanceHttpClientFactory.create(true, baseUrl + "/**")) {
            final var request =
                    ClassicRequestBuilder.post(baseUrl + "/payment")
                            .addHeader("Authorization", "Bearer request-secret")
                            .addHeader("Cookie", "session=request-secret")
                            .setEntity(
                                    new StringEntity(
                                            "{\"request\":\"complete\"}",
                                            ContentType.APPLICATION_JSON))
                            .build();

            final var responseBody =
                    client.execute(request, response -> EntityUtils.toString(response.getEntity()));
            assertEquals("{\"response\":\"complete\"}", responseBody);

            assertEquals(2, appender.list.size());
            final var logOutput =
                    String.join(
                            "\n",
                            appender.list.stream()
                                    .map(ILoggingEvent::getFormattedMessage)
                                    .toList());
            assertTrue(logOutput.contains("{\"request\":\"complete\"}"));
            assertTrue(logOutput.contains("{\"response\":\"complete\"}"));
            assertTrue(logOutput.contains("Authorization=[XXX]"));
            assertTrue(logOutput.contains("Cookie=[XXX]"));
            assertTrue(logOutput.contains("Set-cookie=[XXX]"));
            assertFalse(logOutput.contains("request-secret"));
            assertFalse(logOutput.contains("response-secret"));
        } finally {
            server.stop(0);
            logger.detachAppender(appender);
            logger.setLevel(previousLevel);
            logger.setAdditive(previousAdditive);
            appender.stop();
        }
    }
}
