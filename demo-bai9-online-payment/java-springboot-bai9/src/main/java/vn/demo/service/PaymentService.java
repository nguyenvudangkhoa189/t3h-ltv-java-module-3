package vn.demo.service;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import vn.demo.client.PayPalClient;
import vn.demo.config.PayPalProperties;
import vn.demo.dto.PaymentResultDto;
import vn.demo.model.MovieModel;
import vn.demo.model.PaymentModel;
import vn.demo.repository.MovieRepository;
import vn.demo.repository.PaymentRepository;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentService {

	private final PayPalClient paypal;
	private final PaymentRepository paymentRepository;
	private final MovieRepository movieRepository;
	private final PayPalProperties props;

	public List<PaymentModel> findRecentPayments() {
		List<PaymentModel> list = paymentRepository.findAll().stream()
				.sorted((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()))
				.toList();
		log.info("[PaymentService] findRecentPayments — count={}", list.size());
		return list;
	}

	public String createOrder(String movieId) {
		log.info("[PaymentService] createOrder START — movieId={}", movieId);

		MovieModel movie = movieRepository.findById(movieId)
				.orElseThrow(() -> {
					log.warn("[PaymentService] createOrder FAIL — không tìm thấy phim movieId={}", movieId);
					return new IllegalArgumentException("Không tìm thấy phim: " + movieId);
				});

		BigDecimal amount = movie.getRentalPrice();
		log.info("[PaymentService] createOrder — movie='{}', amount={} {}",
				movie.getTitle(), amount, props.getCurrency());

		Map<String, Object> order = paypal.createOrder(amount, props.getCurrency(), movieId);
		String orderId = (String) order.get("id");
		String paypalStatus = (String) order.get("status");

		PaymentModel payment = new PaymentModel();
		payment.setMovieId(movieId);
		payment.setPaypalOrderId(orderId);
		payment.setAmount(amount);
		payment.setCurrency(props.getCurrency());
		payment.setStatus("CREATED");
		payment.setCreatedAt(Instant.now());
		payment.setUpdatedAt(Instant.now());
		paymentRepository.save(payment);

		log.info("[PaymentService] createOrder DONE — paymentId={}, paypalOrderId={}, paypalStatus={}, "
						+ "mongoStatus=CREATED, amount={} {}",
				payment.getId(), orderId, paypalStatus, amount, props.getCurrency());
		return orderId;
	}

	public PaymentResultDto capture(String orderId) {
		log.info("[PaymentService] capture START — paypalOrderId={}", orderId);

		PaymentModel payment = paymentRepository.findByPaypalOrderId(orderId)
				.orElseThrow(() -> {
					log.warn("[PaymentService] capture FAIL — không có document payments cho orderId={}", orderId);
					return new IllegalStateException("Không tìm thấy payment cho order: " + orderId);
				});

		log.info("[PaymentService] capture — tìm thấy paymentId={}, mongoStatus hiện tại={}, amount={} {}",
				payment.getId(), payment.getStatus(), payment.getAmount(), payment.getCurrency());

		if ("COMPLETED".equals(payment.getStatus())) {
			log.info("[PaymentService] capture SKIP — đã COMPLETED trước đó, captureId={}", payment.getCaptureId());
			return new PaymentResultDto(orderId, payment.getStatus(), payment.getCaptureId());
		}

		Map<String, Object> res = paypal.captureOrder(orderId);
		String status = (String) res.get("status");
		log.info("[PaymentService] capture — PayPal trả status={}", status);

		if ("COMPLETED".equals(status)) {
			String captureId = extractCaptureId(res);
			payment.setStatus("COMPLETED");
			payment.setCaptureId(captureId);
			log.info("[PaymentService] capture — extract captureId={}", captureId);
		} else {
			payment.setStatus("FAILED");
			log.warn("[PaymentService] capture — PayPal status không phải COMPLETED → ghi FAILED. rawStatus={}",
					status);
		}
		payment.setUpdatedAt(Instant.now());
		paymentRepository.save(payment);

		log.info("[PaymentService] capture DONE — paymentId={}, paypalOrderId={}, mongoStatus={}, captureId={}",
				payment.getId(), orderId, payment.getStatus(), payment.getCaptureId());
		return new PaymentResultDto(orderId, payment.getStatus(), payment.getCaptureId());
	}

	public void cancel(String orderId) {
		log.info("[PaymentService] cancel START — paypalOrderId={}", orderId);

		paymentRepository.findByPaypalOrderId(orderId).ifPresentOrElse(payment -> {
			log.info("[PaymentService] cancel — paymentId={}, mongoStatus hiện tại={}",
					payment.getId(), payment.getStatus());
			if ("CREATED".equals(payment.getStatus())) {
				payment.setStatus("CANCELLED");
				payment.setUpdatedAt(Instant.now());
				paymentRepository.save(payment);
				log.info("[PaymentService] cancel DONE — paymentId={} → CANCELLED", payment.getId());
			} else {
				log.info("[PaymentService] cancel SKIP — chỉ hủy khi CREATED, hiện tại={}", payment.getStatus());
			}
		}, () -> log.warn("[PaymentService] cancel — không tìm thấy payment cho orderId={}", orderId));
	}

	public void refund(String paymentId) {
		log.info("[PaymentService] refund START — paymentId={}", paymentId);

		PaymentModel payment = paymentRepository.findById(paymentId)
				.orElseThrow(() -> {
					log.warn("[PaymentService] refund FAIL — không tìm thấy paymentId={}", paymentId);
					return new IllegalArgumentException("Không tìm thấy payment: " + paymentId);
				});

		log.info("[PaymentService] refund — paypalOrderId={}, captureId={}, mongoStatus={}, amount={} {}",
				payment.getPaypalOrderId(), payment.getCaptureId(), payment.getStatus(),
				payment.getAmount(), payment.getCurrency());

		if (!"COMPLETED".equals(payment.getStatus())) {
			log.warn("[PaymentService] refund REJECT — status phải COMPLETED, hiện tại={}", payment.getStatus());
			throw new IllegalStateException("Chỉ hoàn tiền giao dịch đã COMPLETED");
		}

		paypal.refund(payment.getCaptureId());
		payment.setStatus("REFUNDED");
		payment.setUpdatedAt(Instant.now());
		paymentRepository.save(payment);

		log.info("[PaymentService] refund DONE — paymentId={} → REFUNDED (captureId={})",
				paymentId, payment.getCaptureId());
	}

	@SuppressWarnings("unchecked")
	private String extractCaptureId(Map<String, Object> res) {
		try {
			var units = (List<Map<String, Object>>) res.get("purchase_units");
			var payments = (Map<String, Object>) units.get(0).get("payments");
			var captures = (List<Map<String, Object>>) payments.get("captures");
			String captureId = (String) captures.get(0).get("id");
			log.debug("[PaymentService] extractCaptureId OK — captureId={}", captureId);
			return captureId;
		} catch (Exception ex) {
			log.error("[PaymentService] extractCaptureId FAIL — không parse được captureId từ response. cause={}",
					ex.getMessage(), ex);
			throw ex;
		}
	}
}
