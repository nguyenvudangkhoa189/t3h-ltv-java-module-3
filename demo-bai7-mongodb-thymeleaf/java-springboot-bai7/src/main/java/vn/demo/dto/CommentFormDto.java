package vn.demo.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * Form bình luận khách — không cần đăng nhập.
 * Chỉ bắt buộc tên + nội dung; email tùy chọn.
 */
@Data
public class CommentFormDto {

	@NotBlank(message = "Vui lòng nhập tên")
	@Size(max = 80)
	private String name;

	@Email(message = "Email không hợp lệ")
	@Size(max = 120)
	private String email;

	@NotBlank(message = "Vui lòng nhập nội dung")
	@Size(max = 1000)
	private String message;

	private String movieId;

}
