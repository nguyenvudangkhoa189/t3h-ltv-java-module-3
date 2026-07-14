package vn.demo.advanced;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * NÂNG CAO — syllabus §9.2: {@code @SpringBootTest}.
 *
 * <p>Khác slice test:</p>
 * <ul>
 *   <li>{@code @WebMvcTest} / {@code @DataMongoTest}: load <b>một phần</b> context → nhanh</li>
 *   <li>{@code @SpringBootTest}: load gần như <b>toàn bộ</b> app → chậm hơn, gần production hơn</li>
 * </ul>
 *
 * <p>Thường kết hợp {@code @AutoConfigureMockMvc} để gọi HTTP trong process,
 * và DB test (testdb hoặc Testcontainers).</p>
 *
 * <pre>{@code
 * @SpringBootTest
 * @AutoConfigureMockMvc
 * @ActiveProfiles("test")
 * class MovieApiIntegrationTest {
 *
 *   @Autowired MockMvc mockMvc;
 *
 *   @Test
 *   void highlyRated_endToEnd() throws Exception {
 *       // Cần data trên testdb hoặc seed trong @BeforeEach
 *       mockMvc.perform(get("/api/movies/highly-rated").param("min", "8.0"))
 *              .andExpect(status().isOk());
 *   }
 * }
 * }</pre>
 *
 * <p><b>Trade-off:</b> chậm, dễ flaky nếu phụ thuộc môi trường → hạn chế số lượng trên CI.</p>
 */
@Disabled("Syllabus §9.2 — mẫu đọc hiểu; bật khi HV viết integration test thật")
class SpringBootTestOverviewTest {

	@Test
	@DisplayName("Placeholder: đọc javadoc — so sánh với @WebMvcTest")
	void compareWithWebMvcTest_inJavadoc() {
		// intentional no-op
	}

}
