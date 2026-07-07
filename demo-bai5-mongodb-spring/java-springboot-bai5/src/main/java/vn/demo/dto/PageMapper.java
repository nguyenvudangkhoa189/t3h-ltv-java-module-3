package vn.demo.dto;

import java.util.List;
import java.util.function.Function;

import org.springframework.data.domain.Page;

/**
 * Utility chuyển {@code Page&lt;Entity&gt;} → {@link PagedResponse}&lt;Dto&gt;.
 *
 * <p>Đặt logic map tập trung một chỗ — Service gọi {@link #map(Page, Function)} thay vì
 * lặp lại stream + constructor ở mỗi method.</p>
 */
public final class PageMapper {

	private PageMapper() {
	}

	/**
	 * Map metadata phân trang từ Spring Data và convert từng phần tử entity → DTO.
	 *
	 * @param page   kết quả từ Repository (chỉ dùng nội bộ Service)
	 * @param mapper hàm chuyển 1 entity sang DTO (vd {@code MovieDto::fromEntity})
	 */
	public static <E, D> PagedResponse<D> map(Page<E> page, Function<E, D> mapper) {
		List<D> content = page.getContent().stream().map(mapper).toList();
		return new PagedResponse<>(
				content,
				page.getNumber(),
				page.getSize(),
				page.getTotalElements(),
				page.getTotalPages(),
				page.isFirst(),
				page.isLast());
	}

}
