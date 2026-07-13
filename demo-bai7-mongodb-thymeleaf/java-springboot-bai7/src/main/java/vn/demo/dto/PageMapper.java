package vn.demo.dto;

import java.util.List;
import java.util.function.Function;

import org.springframework.data.domain.Page;

public final class PageMapper {

	private PageMapper() {
	}

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
