package vn.demo.config;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import vn.demo.model.MovieModel;
import vn.demo.repository.MovieRepository;

@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

	private final MovieRepository movieRepository;

	@Override
	public void run(String... args) {
		if (movieRepository.count() > 0) {
			return;
		}
		movieRepository.saveAll(List.of(
				new MovieModel(null, "Inception", 2010, "Sci-Fi",
						"Một tên trộm chuyên đánh cắp bí mật trong giấc mơ nhận nhiệm vụ gieo ý tưởng.",
						new BigDecimal("9.99")),
				new MovieModel(null, "The Dark Knight", 2008, "Action",
						"Batman đối mặt Joker trong cuộc chiến giữa hỗn loạn và công lý.",
						new BigDecimal("12.99")),
				new MovieModel(null, "Interstellar", 2014, "Adventure",
						"Nhóm phi hành gia đi qua hố sâu để tìm tương lai mới cho nhân loại.",
						new BigDecimal("14.99")),
				new MovieModel(null, "Spirited Away", 2001, "Animation",
						"Cô bé Chihiro lạc vào thế giới linh hồn và tìm cách cứu cha mẹ.",
						new BigDecimal("8.99"))));
	}
}
