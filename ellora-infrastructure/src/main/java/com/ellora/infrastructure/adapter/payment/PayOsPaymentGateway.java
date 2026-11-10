package com.ellora.infrastructure.adapter.payment;

import com.ellora.application.common.port.PaymentGateway;
import com.ellora.domain.exception.BusinessRuleException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.HexFormat;
import java.util.Map;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PayOsPaymentGateway implements PaymentGateway {
    private final ObjectMapper mapper;
    private final HttpClient client = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build();

    @Value("${app.payos.client-id:}") private String clientId;
    @Value("${app.payos.api-key:}") private String apiKey;
    @Value("${app.payos.checksum-key:}") private String checksumKey;
    @Value("${app.payos.base-url:https://api-merchant.payos.vn}") private String baseUrl;
    @Value("${app.payos.return-url:http://localhost:5173/owner/subscription}") private String returnUrl;
    @Value("${app.payos.cancel-url:http://localhost:5173/owner/subscription}") private String cancelUrl;

    @Override
    public PaymentLink createSubscriptionLink(long orderCode, long amount, String description) {
        return createPaymentLink(orderCode, amount, description, this.returnUrl, this.cancelUrl);
    }

    @Override
    public PaymentLink createPaymentLink(long orderCode, long amount, String description, String retUrl, String canUrl) {
        if (clientId.isBlank() || apiKey.isBlank() || checksumKey.isBlank())
            throw new BusinessRuleException("PAYMENT_UNAVAILABLE", "PayOS chưa được cấu hình");
        if (amount <= 0 || amount > Integer.MAX_VALUE)
            throw new BusinessRuleException("INVALID_SUBSCRIPTION_PRICE", "Dữ liệu thanh toán không hợp lệ");
        String safeDesc = (description != null && !description.isBlank())
                ? (description.length() > 25 ? description.substring(0, 25) : description)
                : "Thanh toan";
        String effectiveReturnUrl = (retUrl != null && !retUrl.isBlank()) ? retUrl : this.returnUrl;
        String effectiveCancelUrl = (canUrl != null && !canUrl.isBlank()) ? canUrl : this.cancelUrl;

        String canonical = "amount=" + amount + "&cancelUrl=" + effectiveCancelUrl + "&description=" + safeDesc
                + "&orderCode=" + orderCode + "&returnUrl=" + effectiveReturnUrl;
        try {
            String signature = sign(canonical);
            String body = mapper.writeValueAsString(Map.of("orderCode", orderCode, "amount", amount,
                    "description", safeDesc, "cancelUrl", effectiveCancelUrl, "returnUrl", effectiveReturnUrl, "signature", signature));
            HttpRequest request = HttpRequest.newBuilder(URI.create(baseUrl + "/v2/payment-requests"))
                    .timeout(Duration.ofSeconds(20)).header("Content-Type", "application/json")
                    .header("x-client-id", clientId).header("x-api-key", apiKey)
                    .POST(HttpRequest.BodyPublishers.ofString(body)).build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            JsonNode result = mapper.readTree(response.body());
            JsonNode data = result.path("data");
            if (response.statusCode() != 200 || !"00".equals(result.path("code").asText())
                    || data.path("paymentLinkId").asText().isBlank() || data.path("checkoutUrl").asText().isBlank())
                throw new BusinessRuleException("PAYMENT_UNAVAILABLE", "PayOS không tạo được liên kết thanh toán: " + result.path("desc").asText("Lỗi không xác định"));
            return new PaymentLink(data.path("paymentLinkId").asText(), data.path("checkoutUrl").asText(), data.path("qrCode").asText());
        } catch (BusinessRuleException e) { throw e; }
        catch (Exception e) { throw new BusinessRuleException("PAYMENT_UNAVAILABLE", "Không kết nối được PayOS: " + e.getMessage()); }
    }

    @Override
    public PaymentStatus getSubscriptionPayment(long orderCode) {
        if (clientId.isBlank() || apiKey.isBlank())
            throw new BusinessRuleException("PAYMENT_UNAVAILABLE", "PayOS chưa được cấu hình");
        try {
            HttpRequest request = HttpRequest.newBuilder(URI.create(baseUrl + "/v2/payment-requests/" + orderCode))
                    .timeout(Duration.ofSeconds(20)).header("x-client-id", clientId).header("x-api-key", apiKey).GET().build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            JsonNode result = mapper.readTree(response.body());
            JsonNode data = result.path("data");
            if (response.statusCode() != 200 || !"00".equals(result.path("code").asText()) || data.isMissingNode())
                throw new BusinessRuleException("PAYMENT_UNAVAILABLE", "Không đọc được trạng thái thanh toán PayOS");
            JsonNode transactions = data.path("transactions");
            String reference = transactions.isArray() && !transactions.isEmpty()
                    ? transactions.get(0).path("reference").asText(null) : null;
            return new PaymentStatus(data.path("orderCode").asLong(), data.path("id").asText(),
                    data.path("amount").asLong(), data.path("amountPaid").asLong(), data.path("status").asText(), reference);
        } catch (BusinessRuleException e) { throw e; }
        catch (Exception e) { throw new BusinessRuleException("PAYMENT_UNAVAILABLE", "Không kết nối được PayOS"); }
    }

    private String sign(String canonical) throws Exception {
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(checksumKey.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
        return HexFormat.of().formatHex(mac.doFinal(canonical.getBytes(StandardCharsets.UTF_8)));
    }
}
