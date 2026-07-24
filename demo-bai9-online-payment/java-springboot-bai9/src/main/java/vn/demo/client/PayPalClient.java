package vn.demo.client;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import lombok.extern.slf4j.Slf4j;
import vn.demo.config.PayPalProperties;

@Slf4j
@Component
public class PayPalClient {

	private final RestClient rest;
	private final PayPalProperties props;
	private String cachedToken;
	private Instant tokenExpiry = Instant.EPOCH;

	public PayPalClient(PayPalProperties props) {
		this.props = props;
		this.rest = RestClient.builder().baseUrl(props.getBaseUrl()).build();
		log.info("[PayPalClient] Khởi tạo RestClient baseUrl={}", props.getBaseUrl());
	}

	private synchronized String accessToken() {
		if (Instant.now().isBefore(tokenExpiry)) {
			log.debug("[PayPalClient] Dùng access token cache (còn hiệu lực đến {})", tokenExpiry);
			return cachedToken;
		}

		log.info("[PayPalClient] Lấy access token mới từ {}/v1/oauth2/token (clientId prefix={})",
				props.getBaseUrl(), maskPrefix(props.getClientId()));

		String basic = Base64.getEncoder().encodeToString(
				(props.getClientId() + ":" + props.getClientSecret()).getBytes(StandardCharsets.UTF_8));

		try {
			@SuppressWarnings("unchecked")
			Map<String, Object> res = rest.post()
					.uri("/v1/oauth2/token")
					.header(HttpHeaders.AUTHORIZATION, "Basic " + basic)
					.contentType(MediaType.APPLICATION_FORM_URLENCODED)
					.body("grant_type=client_credentials")
					.retrieve()
					.body(Map.class);

			this.cachedToken = (String) res.get("access_token");
			long expiresIn = ((Number) res.get("expires_in")).longValue();
			this.tokenExpiry = Instant.now().plusSeconds(expiresIn - 60);

			log.info("[PayPalClient] Lấy access token OK — expires_in={}s, cache đến {}, tokenPrefix={}",
					expiresIn, tokenExpiry, maskPrefix(cachedToken));
			return cachedToken;
		} catch (Exception ex) {
			log.error("[PayPalClient] Lấy access token THẤT BẠI — kiểm tra client-id/secret Sandbox. cause={}",
					ex.getMessage(), ex);
			throw ex;
		}
	}

	@SuppressWarnings("unchecked")
	public Map<String, Object> createOrder(BigDecimal amount, String currency, String referenceId) {
		Map<String, Object> body = Map.of(
				"intent", "CAPTURE",
				"purchase_units", List.of(Map.of(
						"reference_id", referenceId,
						"amount", Map.of(
								"currency_code", currency,
								"value", amount.toPlainString()))));

		log.info("[PayPalClient] POST /v2/checkout/orders — referenceId(movieId)={}, amount={} {}, intent=CAPTURE, body={}",
				referenceId, amount.toPlainString(), currency, body);

		try {
			Map<String, Object> res = rest.post()
					.uri("/v2/checkout/orders")
					.header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken())
					.contentType(MediaType.APPLICATION_JSON)
					.body(body)
					.retrieve()
					.body(Map.class);

			log.info("[PayPalClient] createOrder OK — orderId={}, status={}, responseKeys={}",
					res.get("id"), res.get("status"), res.keySet());
			log.debug("[PayPalClient] createOrder raw response={}", res);
			return res;
		} catch (Exception ex) {
			log.error("[PayPalClient] createOrder THẤT BẠI — referenceId={}, amount={} {}. cause={}",
					referenceId, amount, currency, ex.getMessage(), ex);
			throw ex;
		}
	}

	@SuppressWarnings("unchecked")
	public Map<String, Object> captureOrder(String orderId) {
		log.info("[PayPalClient] POST /v2/checkout/orders/{}/capture", orderId);

		try {
			Map<String, Object> res = rest.post()
					.uri("/v2/checkout/orders/{id}/capture", orderId)
					.header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken())
					.contentType(MediaType.APPLICATION_JSON)
					.body("{}")
					.retrieve()
					.body(Map.class);

			log.info("[PayPalClient] captureOrder OK — orderId={}, status={}, responseKeys={}",
					orderId, res.get("status"), res.keySet());
			log.debug("[PayPalClient] captureOrder raw response={}", res);
			return res;
		} catch (Exception ex) {
			log.error("[PayPalClient] captureOrder THẤT BẠI — orderId={}. cause={}", orderId, ex.getMessage(), ex);
			throw ex;
		}
	}

	@SuppressWarnings("unchecked")
	public Map<String, Object> refund(String captureId) {
		log.info("[PayPalClient] POST /v2/payments/captures/{}/refund", captureId);

		try {
			Map<String, Object> res = rest.post()
					.uri("/v2/payments/captures/{id}/refund", captureId)
					.header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken())
					.contentType(MediaType.APPLICATION_JSON)
					.body("{}")
					.retrieve()
					.body(Map.class);

			log.info("[PayPalClient] refund OK — captureId={}, status={}, refundId={}",
					captureId, res.get("status"), res.get("id"));
			log.debug("[PayPalClient] refund raw response={}", res);
			return res;
		} catch (Exception ex) {
			log.error("[PayPalClient] refund THẤT BẠI — captureId={}. cause={}", captureId, ex.getMessage(), ex);
			throw ex;
		}
	}

	/** Chỉ log vài ký tự đầu — không log secret/token đầy đủ. */
	private static String maskPrefix(String value) {
		if (value == null || value.isBlank()) {
			return "(empty)";
		}
		int n = Math.min(8, value.length());
		return value.substring(0, n) + "...";
	}
}
