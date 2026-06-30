package vn.demo.controller.api;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import vn.demo.model.MovieModel;
import vn.demo.service.MovieService;

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
	public ResponseEntity<List<MovieModel>> getAll() {
		return ResponseEntity.ok(movieService.getAllMovies());
	}

	/** GET /api/movies/{id} — lấy 1 phim theo id (trả 404 nếu không có). */
	@GetMapping("/{id}")
	public ResponseEntity<MovieModel> getById(@PathVariable String id) {
		return ResponseEntity.ok(movieService.getById(id));
	}

	/** GET /api/movies/search?keyword=incept — tìm theo từ khóa của title. */
	@GetMapping("/search")
	public ResponseEntity<List<MovieModel>> search(@RequestParam String keyword) {
		return ResponseEntity.ok(movieService.searchByKeyword(keyword));
	}

	/** GET /api/movies/good?rating=7&year=2015 — bài tập: lọc theo rating và năm. */
	@GetMapping("/good")
	public ResponseEntity<List<MovieModel>> goodMovies(
			@RequestParam(defaultValue = "7") Double rating,
			@RequestParam(defaultValue = "2015") Integer year) {
		return ResponseEntity.ok(movieService.findGoodMovies(rating, year));
	}

	/**
	 * POST /api/movies — tạo mới.
	 *
	 * <p>{@code @Valid} kích hoạt kiểm tra ràng buộc trên {@link MovieModel} (vd @NotBlank title).
	 * Trả về HTTP 201 (CREATED) kèm object vừa tạo.</p>
	 */
	@PostMapping
	public ResponseEntity<MovieModel> create(@Valid @RequestBody MovieModel movie) {
		MovieModel created = movieService.create(movie);
		return new ResponseEntity<>(created, HttpStatus.CREATED);
	}

	/** PUT /api/movies/{id} — cập nhật (chỉ ghi đè field có giá trị). */
	@PutMapping("/{id}")
	public ResponseEntity<MovieModel> update(@PathVariable String id, @RequestBody MovieModel movie) {
		return ResponseEntity.ok(movieService.update(id, movie));
	}

	/** DELETE /api/movies/{id} — xóa theo id, trả về HTTP 204 (No Content). */
	@DeleteMapping("/{id}")
	public ResponseEntity<Void> delete(@PathVariable String id) {
		movieService.delete(id);
		return ResponseEntity.noContent().build();
	}

}
