package vn.demo.model;

import java.time.LocalDateTime;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Data;

/**
 * MODEL — bình luận khách, collection {@code comments}.
 *
 * <p>Liên kết phim bằng field {@code movieId} (reference, không {@code @DBRef}).
 * Không cần đăng nhập: chỉ bắt buộc name + message.</p>
 */
@Data
@Document(collection = "comments")
public class CommentModel {

	@Id
	private String id;

	private String movieId;
	private String name;
	/** Tùy chọn — khách không bắt buộc email. */
	private String email;
	private String message;
	private LocalDateTime createdAt = LocalDateTime.now();

}
