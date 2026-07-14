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
import vn.demo.dto.MovieDto;
import vn.demo.service.MovieService;

/**
 * REST Controller — tầng HTTP (syllabus §6 — test bằng {@code @WebMvcTest} + MockMvc).
 *
 * <p><b>Lưu ý thứ tự mapping:</b> {@code /highly-rated} phải khai báo <b>trước</b>
 * {@code /{id}}, nếu không Spring sẽ coi chữ {@code highly-rated} là path variable id.</p>
 */
@RestController
@RequestMapping("/api/movies")
@RequiredArgsConstructor
public class MovieRestController {

	private final MovieService movieService;

	/** GET /api/movies — danh sách. */
	@GetMapping
	public ResponseEntity<List<MovieDto>> getAll() {
		return ResponseEntity.ok(movieService.getAllMovies());
	}

	/**
	 * GET /api/movies/highly-rated?min=8.0 — endpoint chính cho MockMvc lab (syllabus §6).
	 */
	@GetMapping("/highly-rated")
	public ResponseEntity<List<MovieDto>> highlyRated(
			@RequestParam(defaultValue = "8.0") double min) {
		return ResponseEntity.ok(movieService.findHighlyRated(min));
	}

	/** GET /api/movies/search?keyword=incept */
	@GetMapping("/search")
	public ResponseEntity<List<MovieDto>> search(@RequestParam String keyword) {
		return ResponseEntity.ok(movieService.searchByKeyword(keyword));
	}

	/** GET /api/movies/{id} — 404 nếu không có (qua RestExceptionHandler). */
	@GetMapping("/{id}")
	public ResponseEntity<MovieDto> getById(@PathVariable String id) {
		return ResponseEntity.ok(movieService.getById(id));
	}

	/** POST /api/movies — tạo mới (201). */
	@PostMapping
	public ResponseEntity<MovieDto> create(@Valid @RequestBody MovieDto movie) {
		return new ResponseEntity<>(movieService.create(movie), HttpStatus.CREATED);
	}

	/** PUT /api/movies/{id} — cập nhật (200); 404 nếu không có. */
	@PutMapping("/{id}")
	public ResponseEntity<MovieDto> update(@PathVariable String id, @RequestBody MovieDto movie) {
		return ResponseEntity.ok(movieService.update(id, movie));
	}

	/** DELETE /api/movies/{id} — xóa (204); 404 nếu không có. */
	@DeleteMapping("/{id}")
	public ResponseEntity<Void> delete(@PathVariable String id) {
		movieService.delete(id);
		return ResponseEntity.noContent().build();
	}

}
