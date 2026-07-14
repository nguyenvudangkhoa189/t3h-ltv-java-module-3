package vn.demo.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import vn.demo.controller.api.MovieRestController;
import vn.demo.controller.api.RestExceptionHandler;
import vn.demo.dto.MovieDto;
import vn.demo.exception.ResourceNotFoundException;
import vn.demo.service.MovieService;

/**
 * SLICE TEST Controller — syllabus §6 (P0).
 *
 * <p><b>Mục tiêu học viên:</b></p>
 * <ul>
 *   <li>{@code @WebMvcTest}: chỉ load tầng web của controller chỉ định</li>
 *   <li>{@code MockMvc}: giả lập HTTP GET/POST/PUT/DELETE <b>không cần server thật</b></li>
 *   <li>{@code @MockitoBean}: mock {@link MovieService} — không cần Mongo</li>
 *   <li>Assert status + JSON path (contract API)</li>
 * </ul>
 *
 * <p>{@code @Import(RestExceptionHandler.class)} để case 404 / 400 hoạt động trong slice test.</p>
 *
 * <p>Chạy: {@code ./mvnw test -Dtest=MovieRestControllerTest}</p>
 */
@WebMvcTest(controllers = MovieRestController.class)
@Import(RestExceptionHandler.class)
class MovieRestControllerTest {

	@Autowired
	private MockMvc mockMvc;

	/**
	 * Spring Boot 3.4+: {@code @MockitoBean} thay thế {@code @MockBean} (đã deprecated).
	 * Mock Service trong context slice web — không gọi Mongo.
	 */
	@MockitoBean
	private MovieService movieService;

	@Test
	@DisplayName("GET /api/movies/highly-rated → 200 + JSON title")
	void getHighlyRated_returnsOkAndJsonArray() throws Exception {
		// Arrange: giả Service trả 1 phim
		when(movieService.findHighlyRated(8.0))
				.thenReturn(List.of(
						new MovieDto("1", "Inception", 2010, List.of("Sci-Fi"), "Nolan", 8.8)));

		// Act + Assert: gọi GET, kỳ vọng 200 và JSON đúng
		mockMvc.perform(get("/api/movies/highly-rated").param("min", "8.0"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[0].title").value("Inception"))
				.andExpect(jsonPath("$[0].rating").value(8.8));
	}

	@Test
	@DisplayName("GET /api/movies/{id} khi thiếu → 404 (RestExceptionHandler)")
	void getById_returns404_whenServiceThrowsNotFound() throws Exception {
		// Arrange: Service ném not-found → RestExceptionHandler map sang HTTP 404
		when(movieService.getById("nope"))
				.thenThrow(new ResourceNotFoundException("Không tìm thấy phim với id: nope"));

		mockMvc.perform(get("/api/movies/nope"))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.status").value(404))
				.andExpect(jsonPath("$.message").value("Không tìm thấy phim với id: nope"));
	}

	@Test
	@DisplayName("POST /api/movies → 201 + body id/title")
	void create_returns201_andBody() throws Exception {
		// Arrange: create() nhận DTO bất kỳ (any), trả phim đã có id như sau khi save
		when(movieService.create(any(MovieDto.class)))
				.thenReturn(new MovieDto("99", "Tenet", 2020, List.of("Action"), "Nolan", 7.4));

		// Body JSON gửi lên API
		String body = """
				{
				  "title": "Tenet",
				  "year": 2020,
				  "genre": ["Action"],
				  "director": "Nolan",
				  "rating": 7.4
				}
				""";

		// Act: POST + Content-Type JSON
		mockMvc.perform(post("/api/movies")
						.contentType(MediaType.APPLICATION_JSON)
						.content(body))
				.andExpect(status().isCreated()) // 201 Created
				.andExpect(jsonPath("$.id").value("99"))
				.andExpect(jsonPath("$.title").value("Tenet"));

		// Assert thêm: Controller đã gọi Service.create đúng 1 lần
		verify(movieService).create(any(MovieDto.class));
	}

	@Test
	@DisplayName("POST /api/movies thiếu title → 400 (@Valid)")
	void create_returns400_whenTitleBlank() throws Exception {
		// title rỗng → @Valid fail trước khi vào Service, nên không cần when(...)
		String body = """
				{
				  "title": "",
				  "year": 2020,
				  "rating": 7.0
				}
				""";

		mockMvc.perform(post("/api/movies")
						.contentType(MediaType.APPLICATION_JSON)
						.content(body))
				.andExpect(status().isBadRequest()) // 400
				.andExpect(jsonPath("$.status").value(400))
				.andExpect(jsonPath("$.fields.title").exists()); // field lỗi từ RestExceptionHandler
	}

	@Test
	@DisplayName("PUT /api/movies/{id} → 200 + title mới")
	void update_returns200_andUpdatedTitle() throws Exception {
		// eq("1"): id path phải khớp; any(): body DTO không cần so từng field
		when(movieService.update(eq("1"), any(MovieDto.class)))
				.thenReturn(new MovieDto("1", "Inception Remastered", 2010,
						List.of("Sci-Fi"), "Nolan", 8.8));

		String body = """
				{
				  "title": "Inception Remastered"
				}
				""";

		mockMvc.perform(put("/api/movies/1")
						.contentType(MediaType.APPLICATION_JSON)
						.content(body))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id").value("1"))
				.andExpect(jsonPath("$.title").value("Inception Remastered"));

		verify(movieService).update(eq("1"), any(MovieDto.class));
	}

	@Test
	@DisplayName("PUT /api/movies/{id} không tồn tại → 404")
	void update_returns404_whenMissing() throws Exception {
		// Service báo thiếu dữ liệu → advice trả 404
		when(movieService.update(eq("missing"), any(MovieDto.class)))
				.thenThrow(new ResourceNotFoundException("Không tìm thấy phim với id: missing"));

		mockMvc.perform(put("/api/movies/missing")
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"title\":\"X\"}"))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.status").value(404));
	}

	@Test
	@DisplayName("DELETE /api/movies/{id} → 204 No Content")
	void delete_returns204() throws Exception {
		// delete() là void → dùng doNothing(), không dùng when(...).thenReturn(...)
		doNothing().when(movieService).delete("1");

		mockMvc.perform(delete("/api/movies/1"))
				.andExpect(status().isNoContent()); // 204 — body rỗng

		verify(movieService).delete("1");
	}

	@Test
	@DisplayName("DELETE /api/movies/{id} không tồn tại → 404")
	void delete_returns404_whenMissing() throws Exception {
		// void + exception → doThrow(...).when(service).method(...)
		doThrow(new ResourceNotFoundException("Không tìm thấy phim với id: missing"))
				.when(movieService).delete("missing");

		mockMvc.perform(delete("/api/movies/missing"))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.status").value(404));
	}

}
