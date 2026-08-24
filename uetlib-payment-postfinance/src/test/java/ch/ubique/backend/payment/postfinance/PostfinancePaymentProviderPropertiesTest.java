package ch.ubique.backend.payment.postfinance;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.boot.context.properties.source.MapConfigurationPropertySource;

import java.util.HashMap;
import java.util.Map;

class PostfinancePaymentProviderPropertiesTest {

    private static final String PREFIX = "ch.ubique.backend.payment.postfinance";

    @Test
    void environmentSelectionStrategyDefaultsToUseConfiguration() {
        PostfinancePaymentProviderProperties properties = bind(Map.of());

        assertEquals(
                PostfinanceEnvironmentSelection.USE_CONFIGURATION,
                properties.environmentSelectionStrategy());
    }

    @Test
    void environmentSelectionStrategyCanForceTestEnvironment() {
        PostfinancePaymentProviderProperties properties =
                bind(Map.of("environment-selection-strategy", "FORCE_TEST"));

        assertEquals(
                PostfinanceEnvironmentSelection.FORCE_TEST,
                properties.environmentSelectionStrategy());
    }

    @Test
    void environmentSelectionStrategyCanForceProductionEnvironment() {
        PostfinancePaymentProviderProperties properties =
                bind(Map.of("environment-selection-strategy", "FORCE_PRODUCTION"));

        assertEquals(
                PostfinanceEnvironmentSelection.FORCE_PRODUCTION,
                properties.environmentSelectionStrategy());
    }

    private PostfinancePaymentProviderProperties bind(Map<String, Object> overrides) {
        Map<String, Object> values = new HashMap<>();
        values.put(PREFIX + ".space-id", "1");
        values.put(PREFIX + ".user-id", "1");
        values.put(PREFIX + ".auth-key", "a".repeat(64));
        values.put(PREFIX + ".product-sku", "sku");
        values.put(PREFIX + ".product-unique-id", "unique-id");
        overrides.forEach((key, value) -> values.put(PREFIX + "." + key, value));

        return new Binder(new MapConfigurationPropertySource(values))
                .bind(PREFIX, Bindable.of(PostfinancePaymentProviderProperties.class))
                .orElseThrow(IllegalStateException::new);
    }
}
