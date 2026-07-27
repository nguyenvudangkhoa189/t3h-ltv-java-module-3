package vn.demo.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * DTO phân trang generic (offset) — tái dùng từ Bài 4.
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class PagedResponse<T> {

	private List<T> content;
	private int page;
	private int size;
	private long totalElements;
	private int totalPages;
	private boolean first;
	private boolean last;

	public int getNumber() {
		return page;
	}

}
