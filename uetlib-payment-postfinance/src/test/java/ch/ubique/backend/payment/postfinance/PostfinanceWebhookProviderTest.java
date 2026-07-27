package ch.ubique.backend.payment.postfinance;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import ch.postfinancecheckout.sdk.ApiException;
import ch.postfinancecheckout.sdk.service.WebhookEncryptionKeysService;
import ch.ubique.backend.payment.PaymentFailureCode;
import ch.ubique.backend.payment.TransactionStatus;
import ch.ubique.backend.payment.result.PaymentStatusResult;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;

import tools.jackson.databind.ObjectMapper;

import java.util.Map;

class PostfinanceWebhookProviderTest {

    private static final String TEST_PRODUCT_SKU = "CHMOB-SUB";
    private static final String TEST_PRODUCT_UNIQUE_ID = "CHMOB-SUB";

    private PostfinancePaymentProvider provider;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        provider =
                spy(
                        new PostfinancePaymentProvider(
                                new ObjectMapper(),
                                34121L,
                                1L,
                                "a".repeat(64),
                                TEST_PRODUCT_SKU,
                                TEST_PRODUCT_UNIQUE_ID,
                                false));
    }

    @Test
    void processWebhook_supportedPayload_returnsWebhookEvent() {
        String payload =
                """
                {"entityId":123,"listenerEntityTechnicalName":"Transaction","spaceId":34121,"state":"COMPLETED"}
                """;

        var result = provider.processWebhook(payload, Map.of());

        assertTrue(result.successful());
        assertEquals("123", result.event().providerTransactionId());
        assertEquals(TransactionStatus.COMPLETED, result.event().paymentStatus());
    }

    @Test
    void processWebhook_unsupportedPayload_isIgnored() {
        String payload =
                """
                {"entityId":123,"listenerEntityTechnicalName":"Refund","spaceId":34121,"state":"COMPLETED"}
                """;

        var result = provider.processWebhook(payload, Map.of());

        assertTrue(result.ignored());
        assertFalse(result.successful());
    }

    @Test
    void processWebhook_unknownState_fallsBackToProviderStatus() {
        String payload =
                """
                {"entityId":123,"listenerEntityTechnicalName":"Transaction","spaceId":34121,"state":"UNKNOWN"}
                """;
        doReturn(
                        PaymentStatusResult.success(
                                TransactionStatus.AUTHORIZED,
                                "AUTHORIZED",
                                "CHF",
                                null,
                                ch.ubique.backend.payment.PaymentMethod.UNKNOWN,
                                null,
                                null))
                .when(provider)
                .getStatus("123");

        var result = provider.processWebhook(payload, Map.of());

        assertTrue(result.successful());
        assertEquals(TransactionStatus.AUTHORIZED, result.event().paymentStatus());
    }

    @Test
    void processWebhook_invalidSignature_returnsFailureWhenValidationEnabled() throws ApiException {
        PostfinancePaymentProvider validatingProvider =
                spy(
                        new PostfinancePaymentProvider(
                                new ObjectMapper(),
                                34121L,
                                1L,
                                "a".repeat(64),
                                TEST_PRODUCT_SKU,
                                TEST_PRODUCT_UNIQUE_ID,
                                true));
        WebhookEncryptionKeysService encryptionKeysService =
                mock(WebhookEncryptionKeysService.class);
        doReturn(encryptionKeysService)
                .when(validatingProvider)
                .createWebhookEncryptionKeysService();
        when(encryptionKeysService.isContentValid(eq("invalid-signature"), any()))
                .thenReturn(false);

        var result =
                validatingProvider.processWebhook("{}", Map.of("x-signature", "invalid-signature"));

        assertFalse(result.successful());
        assertEquals(PaymentFailureCode.INVALID_PROVIDER_REFERENCE, result.failure().code());
    }
}
