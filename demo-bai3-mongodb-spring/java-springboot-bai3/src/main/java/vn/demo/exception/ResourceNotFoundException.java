package vn.demo.exception;

/**
 * Ngoại lệ ném ra khi không tìm thấy dữ liệu trong database.
 *
 * <p>Là {@code RuntimeException} (unchecked) nên không cần khai báo throws.
 * Được bắt tập trung tại {@code RestExceptionHandler} để trả về HTTP 404.</p>
 */
public class ResourceNotFoundException extends RuntimeException {

	public ResourceNotFoundException(String message) {
		super(message);
	}

}
