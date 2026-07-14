package vn.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Entry point demo Bài 8 — Kiểm thử Spring Boot.
 *
 * <p>App này cố ý <b>gọn</b> (REST API Movie, không Thymeleaf) để học viên tập trung
 * vào 3 lớp test trong syllabus: Service (Mockito), Repository ({@code @DataMongoTest}),
 * Controller ({@code @WebMvcTest}).</p>
 *
 * @see vn.demo.service.MovieServiceTest
 * @see vn.demo.repository.MovieRepositoryTest
 * @see vn.demo.controller.MovieRestControllerTest
 */
@SpringBootApplication
public class DemoBai8UnitTestingApplication {

	public static void main(String[] args) {
		SpringApplication.run(DemoBai8UnitTestingApplication.class, args);
	}

}
