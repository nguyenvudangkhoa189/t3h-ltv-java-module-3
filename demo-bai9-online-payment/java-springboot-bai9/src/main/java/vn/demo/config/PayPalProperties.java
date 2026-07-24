package vn.demo.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@ConfigurationProperties(prefix = "paypal")
public class PayPalProperties {

	private String baseUrl;
	private String clientId;
	private String clientSecret;
	private String currency = "USD";

	/**
	 * true khi đã dán Client ID / Secret thật (không còn placeholder).
	 * Dùng để FE biết có nên load PayPal SDK hay hiện hướng dẫn cấu hình.
	 */
	public boolean isConfigured() {
		return isRealValue(clientId) && isRealValue(clientSecret);
	}

	private static boolean isRealValue(String value) {
		if (value == null || value.isBlank()) {
			return false;
		}
		String v = value.trim();
		return !v.contains("REPLACE_WITH")
				&& !v.contains("...")
				&& !v.contains("cua-ban")
				&& !v.contains("của-bạn")
				&& v.length() > 20;
	}
}
