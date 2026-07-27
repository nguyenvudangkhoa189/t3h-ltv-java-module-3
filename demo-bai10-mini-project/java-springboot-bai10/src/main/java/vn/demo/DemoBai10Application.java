package vn.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * ENTRY — Mini Project Bài 10 (public Anime + Admin Security/CRUD/Dashboard).
 *
 * <p>Flow khởi động: load context → {@code AdminUserSeeder} / {@code MovieNormalizeRunner}
 * → nhận request qua Spring Security filter → Controller.</p>
 */
@SpringBootApplication
public class DemoBai10Application {

	public static void main(String[] args) {
		SpringApplication.run(DemoBai10Application.class, args);
	}

}
