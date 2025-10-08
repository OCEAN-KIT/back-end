package com.ocean.piuda.payment.portone;

import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@Getter
@ConfigurationProperties("portone")
public class PortOneSecretProperties {
    private String api;
    private String secret;
    private String webhook;

    public void setApi(String api) { this.api = api; }
    public void setSecret(String secret) { this.secret = secret; }
    public void setWebhook(String webhook) { this.webhook = webhook; }
}
