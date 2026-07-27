package vn.demo.service;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import vn.demo.dto.HomePageView;
import vn.demo.dto.MovieCardDto;
import vn.demo.dto.MovieDetailDto;
import vn.demo.dto.PagedListView;
import vn.demo.model.MovieModel;
import vn.demo.repository.MovieRepository;

/**
 * SERVICE — nghiệp vụ đọc phim từ MongoDB, trả DTO cho Thymeleaf (không lộ entity ra view).
 */
@Service
@RequiredArgsConstructor
public class MovieService {

	private static final int MAX_PAGES_TO_SHOW = 5;

	private final MovieRepository movieRepository;

	/** Phân trang danh sách Categories ({@code /movies/home}). */
	public PagedListView<MovieCardDto> findPage(int page, int pageSize) {
		// --- Lấy 1 trang document từ MongoDB ---
		Page<MovieModel> moviePage = movieRepository.findAll(
				PageRequest.of(Math.max(page, 0), pageSize));

		// --- Map entity → DTO + tính startPage/endPage cho UI phân trang ---
		return PagedListView.from(moviePage, MovieCardDto::fromEntity, null,
				null, null, MAX_PAGES_TO_SHOW);
	}

	public Optional<MovieModel> findById(String id) {
		return movieRepository.findById(id);
	}

	/** Chi tiết / watching — map sang {@link MovieDetailDto}. */
	public Optional<MovieDetailDto> findDetailById(String id) {
		return movieRepository.findById(id).map(MovieDetailDto::fromEntity);
	}

	/** Sidebar "you might like" — vài title khác id hiện tại. */
	public List<MovieCardDto> findRelated(String excludeId, int limit) {
		// --- Lấy dư vài bản ghi rồi lọc bỏ phim đang xem ---
		Sort byYearDesc = Sort.by(Sort.Direction.DESC, "release_year");
		return movieRepository.findAll(PageRequest.of(0, limit + 5, byYearDesc)).getContent()
				.stream()
				.filter(m -> excludeId == null || !excludeId.equals(m.getId()))
				.limit(limit)
				.map(m -> MovieCardDto.fromEntity(m, "sidebar", "tv"))
				.toList();
	}

	/** Sidebar Top Views trên Categories / Index. */
	public List<MovieCardDto> findTopViews(int limit) {
		Sort byYearDesc = Sort.by(Sort.Direction.DESC, "release_year");
		return mapList(
				movieRepository.findAll(PageRequest.of(0, limit, byYearDesc)).getContent(),
				m -> MovieCardDto.fromEntity(m, "sidebar", "tv"));
	}

	/**
	 * Gom dữ liệu trang {@code index.html} — mỗi section template = 1 query MongoDB.
	 *
	 * <ul>
	 *   <li>Hero: 3 title mới nhất</li>
	 *   <li>Trending: 6 Movie</li>
	 *   <li>Popular: 6 TV Show</li>
	 *   <li>Recent: 6 title (page 1, tránh trùng hero)</li>
	 *   <li>Live Action: listed_in chứa Action</li>
	 *   <li>Top Views: sidebar</li>
	 * </ul>
	 */
	public HomePageView buildHomePage() {
		Sort byYearDesc = Sort.by(Sort.Direction.DESC, "release_year");

		// --- Hero slider (3 slide) ---
		List<MovieCardDto> hero = mapList(
				movieRepository.findAll(PageRequest.of(0, 3, byYearDesc)).getContent(),
				m -> MovieCardDto.fromEntity(m, "hero", "hero"));

		// --- Trending Now = type Movie ---
		List<MovieCardDto> trending = mapList(
				movieRepository.findByTypeIgnoreCase("Movie", PageRequest.of(0, 6, byYearDesc)),
				m -> MovieCardDto.fromEntity(m, "trending", "trend"));

		// --- Popular Shows = type TV Show ---
		List<MovieCardDto> popular = mapList(
				movieRepository.findByTypeIgnoreCase("TV Show", PageRequest.of(0, 6, byYearDesc)),
				m -> MovieCardDto.fromEntity(m, "popular", "popular"));

		// --- Recently Added (offset page 1) ---
		List<MovieCardDto> recent = mapList(
				movieRepository.findAll(PageRequest.of(1, 6, byYearDesc)).getContent(),
				m -> MovieCardDto.fromEntity(m, "recent", "recent"));

		// --- Live Action theo genre ---
		List<MovieCardDto> liveAction = mapList(
				movieRepository.findByListedInRegex("Action", PageRequest.of(0, 6, byYearDesc)),
				m -> MovieCardDto.fromEntity(m, "live", "live"));

		// --- Fallback khi CSV mẫu ít TV Show / Action ---
		if (popular.isEmpty()) {
			popular = mapList(
					movieRepository.findAll(PageRequest.of(0, 6, byYearDesc)).getContent(),
					m -> MovieCardDto.fromEntity(m, "popular", "popular"));
		}
		if (liveAction.isEmpty()) {
			liveAction = mapList(
					movieRepository.findAll(PageRequest.of(0, 6, byYearDesc)).getContent(),
					m -> MovieCardDto.fromEntity(m, "live", "live"));
		}
		if (trending.isEmpty()) {
			trending = mapList(
					movieRepository.findAll(PageRequest.of(0, 6, byYearDesc)).getContent(),
					m -> MovieCardDto.fromEntity(m, "trending", "trend"));
		}

		// --- Sidebar Top Views ---
		List<MovieCardDto> topViews = mapList(
				movieRepository.findAll(PageRequest.of(0, 5, byYearDesc)).getContent(),
				m -> MovieCardDto.fromEntity(m, "sidebar", "tv"));

		return HomePageView.builder()
				.hero(hero)
				.trending(trending)
				.popular(popular)
				.recent(recent)
				.liveAction(liveAction)
				.topViews(topViews)
				.build();
	}

	private static List<MovieCardDto> mapList(List<MovieModel> movies, Function<MovieModel, MovieCardDto> mapper) {
		return movies.stream().map(mapper).toList();
	}

}
