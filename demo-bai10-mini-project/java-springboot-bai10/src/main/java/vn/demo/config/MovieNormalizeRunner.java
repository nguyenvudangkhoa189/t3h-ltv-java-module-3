package vn.demo.config;

import java.time.LocalDate;
import java.util.List;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import vn.demo.model.MovieModel;
import vn.demo.repository.MovieRepository;
import vn.demo.util.MovieDataNormalizer;

/**
 * CONFIG — chuẩn hóa {@code dateAddedAt} / {@code durationMinutes} khi start.
 *
 * <p><b>Flow:</b> Query document chưa có {@code dateAddedAt} → parse chuỗi Netflix
 * → save từng batch. Parse fail → sentinel {@code 1900-01-01} (loại khỏi D1/D5).</p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class MovieNormalizeRunner implements ApplicationRunner {

	private static final int BATCH = 300;
	/** Sentinel khi parse fail — loại khỏi thống kê D1/D5. */
	private static final LocalDate UNKNOWN = LocalDate.of(1900, 1, 1);

	private final MongoTemplate mongoTemplate;
	private final MovieRepository movieRepository;

	@Override
	public void run(ApplicationArguments args) {
		// --- Chỉ lấy bản ghi chưa chuẩn hóa ---
		Query q = Query.query(Criteria.where("dateAddedAt").exists(false)).limit(BATCH);
		List<MovieModel> batch = mongoTemplate.find(q, MovieModel.class, "mymoviedb");
		int updated = 0;
		int guard = 0;

		// --- Lặp batch đến hết (có guard chống vòng vô hạn) ---
		while (!batch.isEmpty() && guard++ < 100) {
			for (MovieModel m : batch) {
				LocalDate parsed = MovieDataNormalizer.parseDateAdded(m.getDateAdded());
				m.setDateAddedAt(parsed != null ? parsed : UNKNOWN);
				if ("Movie".equalsIgnoreCase(m.getType())) {
					m.setDurationMinutes(MovieDataNormalizer.parseDurationMinutes(m.getDuration()));
				}
				movieRepository.save(m);
				updated++;
			}
			batch = mongoTemplate.find(q, MovieModel.class, "mymoviedb");
		}

		if (updated > 0) {
			log.info("MovieNormalizeRunner: updated {} documents", updated);
		}
	}

}
