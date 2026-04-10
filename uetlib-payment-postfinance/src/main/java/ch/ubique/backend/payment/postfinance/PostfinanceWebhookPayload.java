package ch.ubique.backend.payment.postfinance;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record PostfinanceWebhookPayload(
        Long entityId, String listenerEntityTechnicalName, Long spaceId, String state) {}
