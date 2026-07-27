package vn.demo.dto;

import java.util.function.Function;

import org.springframework.data.domain.Page;

import lombok.Getter;

@Getter
public class PagedListView<T> {

	private final PagedResponse<T> pagination;
	private final String keyword;
	private final String sortBy;
	private final String dir;
	private final int startPage;
	private final int endPage;

	public PagedListView(PagedResponse<T> pagination, String keyword, String sortBy, String dir,
			int startPage, int endPage) {
		this.pagination = pagination;
		this.keyword = keyword == null ? "" : keyword;
		this.sortBy = sortBy;
		this.dir = dir;
		this.startPage = startPage;
		this.endPage = endPage;
	}

	public java.util.List<T> getContent() {
		return pagination.getContent();
	}

	public static <E, D> PagedListView<D> from(Page<E> entityPage, Function<E, D> mapper,
			String keyword, String sortBy, String dir, int maxPagesToShow) {
		PagedResponse<D> pagination = PageMapper.map(entityPage, mapper);
		int totalPages = pagination.getTotalPages();
		int currentPage = pagination.getPage();
		int startPage = Math.max(0, currentPage - maxPagesToShow / 2);
		int endPage = Math.min(totalPages - 1, startPage + maxPagesToShow - 1);
		if ((endPage - startPage) < (maxPagesToShow - 1)) {
			startPage = Math.max(0, endPage - (maxPagesToShow - 1));
		}
		return new PagedListView<>(pagination, keyword, sortBy, dir, startPage, endPage);
	}

}
