package vn.demo.config;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import vn.demo.model.MovieModel;
import vn.demo.repository.MovieRepository;

/**
 * Nạp sẵn vài phim mẫu khi collection còn rỗng, để học viên mở app là có dữ liệu.
 *
 * <p>{@link CommandLineRunner} chạy 1 lần ngay sau khi app khởi động. Chỉ chạy khi
 * {@code app.movies.seed-on-startup=true} và collection chưa có document nào (không
 * xóa dữ liệu cũ).</p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

	private final MovieRepository movieRepository;

	@Value("${app.movies.seed-on-startup:true}")
	private boolean seedOnStartup;

	@Override
	public void run(String... args) {
		// Bỏ qua nếu đã tắt seed hoặc collection đã có dữ liệu
		if (!seedOnStartup || movieRepository.count() > 0) {
			return;
		}
		List<MovieModel> samples = List.of(
				new MovieModel(null, "Inception", 2010, List.of("Action", "Sci-Fi", "Thriller"), "Christopher Nolan", 8.8),
				new MovieModel(null, "The Shawshank Redemption", 1994, List.of("Drama"), "Frank Darabont", 9.3),
				new MovieModel(null, "Interstellar", 2014, List.of("Adventure", "Drama", "Sci-Fi"), "Christopher Nolan", 8.6),
				new MovieModel(null, "Parasite", 2019, List.of("Drama", "Thriller"), "Bong Joon-ho", 8.5),
				new MovieModel(null, "Mad Max: Fury Road", 2015, List.of("Action", "Adventure"), "George Miller", 8.1),
				new MovieModel(null, "The Dark Knight", 2008, List.of("Action", "Crime", "Drama"), "Christopher Nolan", 9.0),
				new MovieModel(null, "Spirited Away", 2001, List.of("Animation", "Adventure"), "Hayao Miyazaki", 8.6),
				new MovieModel(null, "Dune", 2021, List.of("Adventure", "Sci-Fi"), "Denis Villeneuve", 8.0));
		movieRepository.saveAll(samples);
		log.info("Đã nạp {} phim mẫu", samples.size());
	}

}
