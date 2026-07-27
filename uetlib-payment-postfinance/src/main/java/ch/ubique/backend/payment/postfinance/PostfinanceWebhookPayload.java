package ch.ubique.backend.payment.postfinance;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import org.jspecify.annotations.Nullable;

@JsonIgnoreProperties(ignoreUnknown = true)
public record PostfinanceWebhookPayload(
        @Nullable Long entityId,
        @Nullable String listenerEntityTechnicalName,
        @Nullable Long spaceId,
        @Nullable String state) {}
