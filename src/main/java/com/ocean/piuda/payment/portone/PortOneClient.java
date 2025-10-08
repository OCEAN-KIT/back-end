package com.ocean.piuda.payment.portone;

import com.ocean.piuda.payment.enums.PaymentStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Component
@RequiredArgsConstructor
public class PortOneClient {
    private static final String API_BASE = "https://api.portone.io";
    private final RestTemplate restTemplate = new RestTemplate();

    private final PortOneSecretProperties secret; // apiKey, apiSecret 보관

    /** 결제 단건 조회 */
    public PortOnePaymentResponse getPayment(String paymentId) {
        String url = API_BASE + "/payments/" + paymentId;

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBasicAuth(secret.getApi(), secret.getSecret()); // Basic Auth

        HttpEntity<Void> request = new HttpEntity<>(headers);

        ResponseEntity<PortOnePaymentResponse> response =
                restTemplate.exchange(url, HttpMethod.GET, request, PortOnePaymentResponse.class);

        return response.getBody();
    }

    /** 포트원 응답을 담는 DTO */
    public record PortOnePaymentResponse(
            String id,
            String merchantUid,
            Long totalAmount,
            String currency,
            PaymentStatus status
    ) {}
}
