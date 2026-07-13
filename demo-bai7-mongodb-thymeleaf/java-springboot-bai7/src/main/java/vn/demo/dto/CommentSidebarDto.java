package vn.demo.dto;

import java.time.LocalDateTime;

import lombok.Builder;
import lombok.Data;

/** Sidebar "New Comment" — comment thật từ collection {@code comments}. */
@Data
@Builder
public class CommentSidebarDto {

	private String movieId;
	private String movieTitle;
	private String authorName;
	private String messagePreview;
	private String posterUrl;
	private LocalDateTime createdAt;

}
