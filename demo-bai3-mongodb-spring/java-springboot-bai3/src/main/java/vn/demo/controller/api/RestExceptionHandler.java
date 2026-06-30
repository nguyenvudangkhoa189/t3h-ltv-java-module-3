package vn.demo.controller.api;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import vn.demo.exception.ResourceNotFoundException;

/**
 * Bắt lỗi tập trung cho các REST controller (chỉ áp dụng cho package controller.api).
 *
 * <p>Thay vì viết try/catch lặp lại trong từng handler, ta xử lý lỗi tại MỘT nơi:
 * {@link ResourceNotFoundException} -&gt; trả 404; lỗi validation -&gt; trả 400 kèm
 * chi tiết field sai.</p>
 */
@RestControllerAdvice(basePackageClasses = MovieRestController.class)
public class RestExceptionHandler {

	/** Không tìm thấy dữ liệu -> HTTP 404. */
	@ExceptionHandler(ResourceNotFoundException.class)
	public ResponseEntity<Map<String, Object>> handleNotFound(ResourceNotFoundException ex) {
		return build(HttpStatus.NOT_FOUND, ex.getMessage());
	}

	/** Dữ liệu gửi lên vi phạm ràng buộc (@Valid) -> HTTP 400 + danh sách field lỗi. */
	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<Map<String, Object>> handleValidation(MethodArgumentNotValidException ex) {
		Map<String, Object> body = baseBody(HttpStatus.BAD_REQUEST, "Dữ liệu không hợp lệ");
		Map<String, String> fields = new HashMap<>();
		// Gom từng field bị lỗi cùng thông báo tương ứng
		ex.getBindingResult().getFieldErrors()
				.forEach(err -> fields.put(err.getField(), err.getDefaultMessage()));
		body.put("fields", fields);
		return ResponseEntity.badRequest().body(body);
	}

	private ResponseEntity<Map<String, Object>> build(HttpStatus status, String message) {
		return ResponseEntity.status(status).body(baseBody(status, message));
	}

	/** Tạo phần thân JSON chung cho mọi lỗi (timestamp, status, error, message). */
	private Map<String, Object> baseBody(HttpStatus status, String message) {
		Map<String, Object> body = new HashMap<>();
		body.put("timestamp", Instant.now().toString());
		body.put("status", status.value());
		body.put("error", status.getReasonPhrase());
		body.put("message", message);
		return body;
	}

}
