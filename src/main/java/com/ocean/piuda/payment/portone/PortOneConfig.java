package com.ocean.piuda.payment.portone;

import io.portone.sdk.server.payment.PaymentClient;
import io.portone.sdk.server.webhook.WebhookVerifier;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class PortOneConfig {
    private final PortOneSecretProperties secret;

    @Bean
    public PaymentClient paymentClient() {
        return new PaymentClient(secret.getApi(), "https://api.portone.io", null);
    }

    @Bean
    public WebhookVerifier webhookVerifier() {
        return new WebhookVerifier(secret.getWebhook());
    }
}
