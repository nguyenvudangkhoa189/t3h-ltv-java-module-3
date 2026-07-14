package vn.demo.exception;

/**
 * Ném khi không tìm thấy tài nguyên — {@code RestExceptionHandler} map sang HTTP 404.
 *
 * <p>Unit test Service dùng {@code assertThrows(ResourceNotFoundException.class, ...)}
 * (syllabus §4) để kiểm tra nhánh "không có dữ liệu".</p>
 */
public class ResourceNotFoundException extends RuntimeException {

	public ResourceNotFoundException(String message) {
		super(message);
	}

}
