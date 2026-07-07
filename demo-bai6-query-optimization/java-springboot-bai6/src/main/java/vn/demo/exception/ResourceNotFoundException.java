package vn.demo.exception;

/** Ném khi không tìm thấy resource — REST handler map sang HTTP 404. */
public class ResourceNotFoundException extends RuntimeException {

	public ResourceNotFoundException(String message) {
		super(message);
	}

}
