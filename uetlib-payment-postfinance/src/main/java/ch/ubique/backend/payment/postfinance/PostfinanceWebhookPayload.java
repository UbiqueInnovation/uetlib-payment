package ch.ubique.backend.payment.postfinance;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import org.jspecify.annotations.Nullable;

@JsonIgnoreProperties(ignoreUnknown = true)
public record PostfinanceWebhookPayload(
        Long entityId, String listenerEntityTechnicalName, Long spaceId, @Nullable String state) {}
