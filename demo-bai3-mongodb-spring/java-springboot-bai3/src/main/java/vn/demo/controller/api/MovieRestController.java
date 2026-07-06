package vn.demo.controller.api;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.demo.dto.MovieDto;
import vn.demo.dto.PagedResponse;
import vn.demo.service.MovieService;

import java.util.List;

/**
 * PHẦN 1 — CRUD qua REST API.
 *
 * <p>Đây là tầng Controller dành cho API. Nhiệm vụ: nhận HTTP request, gọi
 * {@link MovieService} xử lý, rồi trả dữ liệu.</p>
 *
 * <p>{@code @RestController} = {@code @Controller} + {@code @ResponseBody}: giá trị
 * trả về được Spring tự chuyển thành <b>JSON</b> (không phải tên view). Dùng để
 * test bằng trình duyệt / Postman / curl.</p>
 */
@RestController
@RequestMapping("/api/movies")
@RequiredArgsConstructor
public class MovieRestController {

	// Tiêm MovieService qua constructor (nhờ @RequiredArgsConstructor sinh constructor cho field final)
	private final MovieService movieService;

	/** GET /api/movies — lấy tất cả phim. */
	@GetMapping
	public ResponseEntity<List<MovieDto>> getAll() {
		return ResponseEntity.ok(movieService.getAllMovies());
	}

	/** GET /api/movies/{id} — lấy 1 phim theo id (trả 404 nếu không có). */
	@GetMapping("/{id}")
	public ResponseEntity<MovieDto> getById(@PathVariable String id) {
		return ResponseEntity.ok(movieService.getById(id));
	}

	/** GET /api/movies/search?keyword=incept — tìm theo từ khóa của title. */
	@GetMapping("/search")
	public ResponseEntity<List<MovieDto>> search(@RequestParam String keyword) {
		return ResponseEntity.ok(movieService.searchByKeyword(keyword));
	}

	/** GET /api/movies/good?rating=7&year=2015 — bài tập: lọc theo rating và năm. */
	@GetMapping("/good")
	public ResponseEntity<List<MovieDto>> goodMovies(
			@RequestParam(defaultValue = "7") Double rating,
			@RequestParam(defaultValue = "2015") Integer year) {
		return ResponseEntity.ok(movieService.findGoodMovies(rating, year));
	}

	/**
	 * GET /api/movies/page — demo §6.1.1: trả {@code Page&lt;MovieDto&gt;} (Spring Data).
	 *
	 * <p>Ví dụ: {@code /api/movies/page?keyword=incept&page=0&size=5}</p>
	 */
	@GetMapping("/page")
	public ResponseEntity<Page<MovieDto>> page(
			@RequestParam(required = false) String keyword,
			@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "5") int size) {
		Pageable pageable = PageRequest.of(Math.max(page, 0), size, Sort.by("title").ascending());
		return ResponseEntity.ok(movieService.findPageAsSpringPage(keyword, pageable));
	}

	/**
	 * GET /api/movies/paged — demo §6.1.3: trả {@link PagedResponse} generic (enterprise REST).
	 *
	 * <p>Ví dụ: {@code /api/movies/paged?keyword=incept&page=0&size=5}</p>
	 */
	@GetMapping("/paged")
	public ResponseEntity<PagedResponse<MovieDto>> paged(
			@RequestParam(required = false) String keyword,
			@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "5") int size) {
		Pageable pageable = PageRequest.of(Math.max(page, 0), size, Sort.by("title").ascending());
		return ResponseEntity.ok(movieService.findPageAsPagedResponse(keyword, pageable));
	}

	/**
	 * POST /api/movies — tạo mới.
	 *
	 * <p>{@code @Valid} kích hoạt kiểm tra ràng buộc trên {@link MovieDto} (vd @NotBlank title).
	 * Trả về HTTP 201 (CREATED) kèm object vừa tạo.</p>
	 */
	@PostMapping
	public ResponseEntity<MovieDto> create(@Valid @RequestBody MovieDto movie) {
		MovieDto created = movieService.create(movie);
		return new ResponseEntity<>(created, HttpStatus.CREATED);
	}

	/** PUT /api/movies/{id} — cập nhật (chỉ ghi đè field có giá trị). */
	@PutMapping("/{id}")
	public ResponseEntity<MovieDto> update(@PathVariable String id, @RequestBody MovieDto movie) {
		return ResponseEntity.ok(movieService.update(id, movie));
	}

	/** DELETE /api/movies/{id} — xóa theo id, trả về HTTP 204 (No Content). */
	@DeleteMapping("/{id}")
	public ResponseEntity<Void> delete(@PathVariable String id) {
		movieService.delete(id);
		return ResponseEntity.noContent().build();
	}

}
