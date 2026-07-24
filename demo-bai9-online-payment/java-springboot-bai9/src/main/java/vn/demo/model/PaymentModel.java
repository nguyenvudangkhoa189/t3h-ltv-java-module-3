package vn.demo.model;

import java.math.BigDecimal;
import java.time.Instant;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Document(collection = "payments")
public class PaymentModel {

	@Id
	private String id;

	private String movieId;
	private String paypalOrderId;
	private String captureId;
	private BigDecimal amount;
	private String currency;
	private String status;
	private String payerEmail;
	private Instant createdAt;
	private Instant updatedAt;
}
