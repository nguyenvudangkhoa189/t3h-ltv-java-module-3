package vn.demo.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * DTO phân trang cursor generic — contract ứng dụng, không phụ thuộc MongoDB/Spring Data.
 *
 * <p><b>Khác offset ({@link PagedResponse}):</b> client gửi {@code afterId} (cursor) thay vì
 * số trang. Phù hợp feed/infinite scroll, tránh skip chậm khi offset lớn.</p>
 *
 * <p>{@code lastSeenId} = giá trị cursor của bản ghi cuối trang hiện tại (ở demo: {@code _id}).
 * Client gửi lại làm {@code afterId} để lấy trang tiếp theo.</p>
 *
 * @param <T> kiểu phần tử trong trang (thường là DTO)
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CursorPageDto<T> {

	private List<T> items;
	/** Cursor của bản ghi cuối; {@code null} nếu trang rỗng. */
	private String lastSeenId;
	/** {@code true} nếu còn dữ liệu sau cursor (lấy {@code limit + 1} để kiểm tra). */
	private boolean hasMore;

}
