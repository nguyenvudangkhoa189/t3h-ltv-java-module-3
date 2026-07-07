package vn.demo.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * DTO phân trang generic (offset) — contract ứng dụng, không phụ thuộc Spring Data {@code Page}.
 *
 * <p><b>Vì sao cần class này?</b> Repository trả {@code Page&lt;Model&gt;} (framework).
 * Service chuyển sang {@code PagedResponse&lt;Dto&gt;} trước khi trả Controller — Controller/REST
 * không lộ entity hay type của Spring Data.</p>
 *
 * <p>{@code page} đánh số từ <b>0</b> (chuẩn Spring Data). Trên UI hiển thị {@code page + 1}.</p>
 *
 * @param <T> kiểu phần tử trong trang (thường là DTO)
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class PagedResponse<T> {

	private List<T> content;
	/** Chỉ số trang hiện tại, bắt đầu từ 0. */
	private int page;
	private int size;
	private long totalElements;
	private int totalPages;
	private boolean first;
	private boolean last;

	/** Alias tương thích Thymeleaf/Spring Page ({@code page.number}). */
	public int getNumber() {
		return page;
	}

}
