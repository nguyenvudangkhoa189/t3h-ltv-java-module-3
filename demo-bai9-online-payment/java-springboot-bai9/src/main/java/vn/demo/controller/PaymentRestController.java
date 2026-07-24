package vn.demo.controller;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import vn.demo.dto.CreateOrderRequest;
import vn.demo.dto.PaymentResultDto;
import vn.demo.service.PaymentService;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/payments")
public class PaymentRestController {

	private final PaymentService paymentService;

	@PostMapping("/orders")
	public Map<String, String> create(@RequestBody CreateOrderRequest req) {
		log.info("[API] POST /api/payments/orders — body.movieId={}", req.movieId());
		String orderId = paymentService.createOrder(req.movieId());
		log.info("[API] POST /api/payments/orders — response.id(orderId)={}", orderId);
		return Map.of("id", orderId);
	}

	@PostMapping("/orders/{orderId}/capture")
	public PaymentResultDto capture(@PathVariable String orderId) {
		log.info("[API] POST /api/payments/orders/{}/capture", orderId);
		PaymentResultDto result = paymentService.capture(orderId);
		log.info("[API] capture response — orderId={}, status={}, captureId={}",
				result.orderId(), result.status(), result.captureId());
		return result;
	}

	@PostMapping("/orders/{orderId}/cancel")
	public ResponseEntity<Void> cancel(@PathVariable String orderId) {
		log.info("[API] POST /api/payments/orders/{}/cancel", orderId);
		paymentService.cancel(orderId);
		log.info("[API] cancel DONE — orderId={}", orderId);
		return ResponseEntity.noContent().build();
	}

	@PostMapping("/{paymentId}/refund")
	public ResponseEntity<Void> refund(@PathVariable String paymentId) {
		log.info("[API] POST /api/payments/{}/refund", paymentId);
		paymentService.refund(paymentId);
		log.info("[API] refund DONE — paymentId={}", paymentId);
		return ResponseEntity.noContent().build();
	}

	@ExceptionHandler({ IllegalArgumentException.class, IllegalStateException.class })
	public ResponseEntity<Map<String, String>> handleBadRequest(RuntimeException ex) {
		log.warn("[API] 400 Bad Request — {}", ex.getMessage());
		return ResponseEntity.status(HttpStatus.BAD_REQUEST)
				.body(Map.of("error", ex.getMessage()));
	}

	@ExceptionHandler(Exception.class)
	public ResponseEntity<Map<String, String>> handleServerError(Exception ex) {
		log.error("[API] 500 Internal Error — {}", ex.getMessage(), ex);
		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
				.body(Map.of("error", "PayPal/API lỗi: " + ex.getMessage()));
	}
}
