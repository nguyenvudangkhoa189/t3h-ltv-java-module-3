package vn.demo.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import vn.demo.client.PayPalClient;
import vn.demo.config.PayPalProperties;
import vn.demo.dto.PaymentResultDto;
import vn.demo.model.MovieModel;
import vn.demo.model.PaymentModel;
import vn.demo.repository.MovieRepository;
import vn.demo.repository.PaymentRepository;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

	@Mock
	PayPalClient paypal;

	@Mock
	PaymentRepository paymentRepository;

	@Mock
	MovieRepository movieRepository;

	@Mock
	PayPalProperties props;

	@InjectMocks
	PaymentService paymentService;

	@Test
	void createOrder_luuTrangThaiCreated() {
		MovieModel movie = new MovieModel("m1", "Inception", 2010, "Sci-Fi", "Demo", new BigDecimal("9.99"));

		when(props.getCurrency()).thenReturn("USD");
		when(movieRepository.findById("m1")).thenReturn(Optional.of(movie));
		when(paypal.createOrder(any(), eq("USD"), eq("m1")))
				.thenReturn(Map.of("id", "PP-ORDER-123", "status", "CREATED"));

		String orderId = paymentService.createOrder("m1");

		assertThat(orderId).isEqualTo("PP-ORDER-123");
		verify(paymentRepository).save(any(PaymentModel.class));
	}

	@Test
	void createOrder_movieKhongTonTai_khongGoiPaypal() {
		when(movieRepository.findById("missing")).thenReturn(Optional.empty());

		assertThatThrownBy(() -> paymentService.createOrder("missing"))
				.isInstanceOf(IllegalArgumentException.class);

		verify(paypal, never()).createOrder(any(), any(), any());
	}

	@Test
	void capture_completed_luuCaptureId() {
		PaymentModel existing = new PaymentModel();
		existing.setPaypalOrderId("PP-ORDER-123");
		existing.setStatus("CREATED");

		when(paymentRepository.findByPaypalOrderId("PP-ORDER-123"))
				.thenReturn(Optional.of(existing));
		when(paypal.captureOrder("PP-ORDER-123")).thenReturn(Map.of(
				"status", "COMPLETED",
				"purchase_units", List.of(Map.of(
						"payments", Map.of(
								"captures", List.of(Map.of("id", "CAP-999")))))));

		PaymentResultDto result = paymentService.capture("PP-ORDER-123");

		assertThat(result.status()).isEqualTo("COMPLETED");
		assertThat(result.captureId()).isEqualTo("CAP-999");
		verify(paymentRepository).save(existing);
	}

	@Test
	void refund_chiChoPhepCompleted() {
		PaymentModel failed = new PaymentModel();
		failed.setStatus("FAILED");
		when(paymentRepository.findById("p1")).thenReturn(Optional.of(failed));

		assertThatThrownBy(() -> paymentService.refund("p1"))
				.isInstanceOf(IllegalStateException.class);

		verify(paypal, never()).refund(any());
	}
}
