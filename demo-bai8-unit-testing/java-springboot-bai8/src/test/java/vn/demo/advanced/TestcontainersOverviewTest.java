package vn.demo.advanced;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * NÂNG CAO — syllabus §9 (đọc hiểu, <b>không bắt buộc chạy</b>).
 *
 * <p>File này cố ý {@code @Disabled} để {@code ./mvnw test} mặc định vẫn xanh
 * khi máy HV chưa có Docker / chưa cấu hình Testcontainers.</p>
 *
 * <h2>Khi nào cần Testcontainers?</h2>
 * <ul>
 *   <li>Muốn {@code @DataMongoTest} / {@code @SpringBootTest} chạy giống nhau trên mọi máy + CI</li>
 *   <li>Không muốn phụ thuộc Mongo cài local / sợ {@code deleteAll} nhầm DB</li>
 * </ul>
 *
 * <h2>Ý tưởng code (học viên tự thêm dependency khi làm bài nâng cao)</h2>
 * <pre>{@code
 * // pom.xml (test scope):
 * //   org.testcontainers:junit-jupiter
 * //   org.testcontainers:mongodb
 *
 * @Testcontainers
 * @DataMongoTest
 * @ActiveProfiles("test")
 * class MovieRepositoryTestcontainersDemo {
 *
 *   @Container
 *   static MongoDBContainer mongo = new MongoDBContainer("mongo:7");
 *
 *   @DynamicPropertySource
 *   static void mongoProps(DynamicPropertyRegistry registry) {
 *       registry.add("spring.data.mongodb.uri", mongo::getReplicaSetUrl);
 *   }
 * }
 * }</pre>
 *
 * @see <a href="https://testcontainers.com/">testcontainers.com</a>
 */
@Disabled("Syllabus §9 — bật khi HV đã cài Docker + thêm dependency Testcontainers")
class TestcontainersOverviewTest {

	@Test
	@DisplayName("Placeholder: đọc javadoc class — không chạy logic")
	void readJavadoc_thenEnableWhenReady() {
		// Không có assert — class chỉ mang mục đích tài liệu sống trong repo.
	}

}
