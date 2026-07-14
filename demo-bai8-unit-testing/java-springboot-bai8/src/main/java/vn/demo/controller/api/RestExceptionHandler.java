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
 * Bắt lỗi tập trung cho REST — map exception → HTTP status + JSON.
 *
 * <p>Khi viết {@code @WebMvcTest} case 404, cần {@code @Import(RestExceptionHandler.class)}
 * (xem {@code MovieRestControllerTest}) vì slice web không tự load advice này.</p>
 */
@RestControllerAdvice(basePackageClasses = MovieRestController.class)
public class RestExceptionHandler {

	@ExceptionHandler(ResourceNotFoundException.class)
	public ResponseEntity<Map<String, Object>> handleNotFound(ResourceNotFoundException ex) {
		return build(HttpStatus.NOT_FOUND, ex.getMessage());
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<Map<String, Object>> handleValidation(MethodArgumentNotValidException ex) {
		Map<String, Object> body = baseBody(HttpStatus.BAD_REQUEST, "Dữ liệu không hợp lệ");
		Map<String, String> fields = new HashMap<>();
		ex.getBindingResult().getFieldErrors()
				.forEach(err -> fields.put(err.getField(), err.getDefaultMessage()));
		body.put("fields", fields);
		return ResponseEntity.badRequest().body(body);
	}

	private ResponseEntity<Map<String, Object>> build(HttpStatus status, String message) {
		return ResponseEntity.status(status).body(baseBody(status, message));
	}

	private Map<String, Object> baseBody(HttpStatus status, String message) {
		Map<String, Object> body = new HashMap<>();
		body.put("timestamp", Instant.now().toString());
		body.put("status", status.value());
		body.put("error", status.getReasonPhrase());
		body.put("message", message);
		return body;
	}

}
