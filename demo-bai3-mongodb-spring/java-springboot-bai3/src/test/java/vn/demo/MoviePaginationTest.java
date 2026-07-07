package vn.demo;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import vn.demo.dto.MovieDto;
import vn.demo.dto.PageMapper;
import vn.demo.dto.PagedListView;
import vn.demo.dto.PagedResponse;
import vn.demo.model.MovieModel;

/**
 * Kiểm tra 3 lớp phân trang syllabus §6.1 (Page → PagedResponse → PagedListView).
 */
class MoviePaginationTest {

	@Test
	void pageMapper_convertsSpringPageToPagedResponse() {
		MovieModel model = new MovieModel("1", "Inception", 2010, List.of("Sci-Fi"), "Nolan", 8.8);
		Page<MovieModel> page = new PageImpl<>(List.of(model), PageRequest.of(0, 5), 8);

		PagedResponse<MovieDto> result = PageMapper.map(page, MovieDto::fromEntity);

		assertThat(result.getContent()).hasSize(1);
		assertThat(result.getContent().get(0).getTitle()).isEqualTo("Inception");
		assertThat(result.getPage()).isZero();
		assertThat(result.getTotalElements()).isEqualTo(8);
		assertThat(result.getTotalPages()).isEqualTo(2);
		assertThat(result.isFirst()).isTrue();
		assertThat(result.isLast()).isFalse();
	}

	@Test
	void pagedListView_wrapsPagedResponseWithKeyword() {
		MovieModel model = new MovieModel("1", "Inception", 2010, List.of("Sci-Fi"), "Nolan", 8.8);
		Page<MovieModel> page = new PageImpl<>(List.of(model), PageRequest.of(0, 5), 1);

		PagedListView<MovieDto> result = PagedListView.from(page, MovieDto::fromEntity, "incept");

		assertThat(result.getKeyword()).isEqualTo("incept");
		assertThat(result.getContent()).hasSize(1);
		assertThat(result.getPagination().getNumber()).isZero();
	}

	@Test
	void springPage_map_producesPageOfDto() {
		MovieModel model = new MovieModel("1", "Inception", 2010, List.of("Sci-Fi"), "Nolan", 8.8);
		Page<MovieModel> page = new PageImpl<>(List.of(model), PageRequest.of(0, 5), 1);

		Page<MovieDto> result = page.map(MovieDto::fromEntity);

		assertThat(result.getContent()).hasSize(1);
		assertThat(result.getContent().get(0).getTitle()).isEqualTo("Inception");
		assertThat(result.getTotalPages()).isEqualTo(1);
	}

}
