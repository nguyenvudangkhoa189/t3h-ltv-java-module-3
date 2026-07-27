package vn.demo.service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.DateOperators;
import org.springframework.data.mongodb.core.aggregation.StringOperators;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import vn.demo.dto.CountryCountDto;
import vn.demo.dto.GenreCountDto;
import vn.demo.dto.MonthCountDto;
import vn.demo.dto.MovieRankDto;
import vn.demo.model.MovieModel;

/**
 * SERVICE — 6 thống kê Admin Dashboard (D1–D6).
 *
 * <p><b>Flow:</b> Controller gọi method → aggregation / query giới hạn trên MongoDB
 * → map DTO → Thymeleaf / Chart.js. <b>Không</b> {@code findAll} rồi group trên JVM.</p>
 */
@Service
@RequiredArgsConstructor
public class DashboardStatsService {

	private static final LocalDate UNKNOWN_DATE = LocalDate.of(1900, 1, 1);

	private final MongoTemplate mongoTemplate;

	/**
	 * D1 — Top 7 tháng có số lượng <b>phim</b> phát hành nhiều nhất.
	 *
	 * <p><b>Ý đúng:</b> đếm {@code Movie} theo tháng phát hành (không đếm TV Show;
	 * không diễn đạt thành “Netflix thêm mọi title”).</p>
	 *
	 * <p><b>Flow:</b> match type=Movie + date hợp lệ → group yyyy-MM từ {@code dateAddedAt}
	 * (map từ {@code date_added}, vì CSV không có release_date đủ tháng)
	 * → sort count DESC → limit.</p>
	 */
	public List<MonthCountDto> topMovieReleaseMonths(int limit) {
		// --- Chỉ Movie + ngày phát hành hợp lệ (loại sentinel 1900-01-01) ---
		Aggregation agg = Aggregation.newAggregation(
				Aggregation.match(Criteria.where("type").regex("^Movie$", "i")
						.and("dateAddedAt").gt(UNKNOWN_DATE)),
				// --- Tháng phát hành = yyyy-MM ---
				Aggregation.project()
						.and(DateOperators.DateToString.dateOf("dateAddedAt").toString("%Y-%m"))
						.as("month"),
				Aggregation.group("month").count().as("count"),
				Aggregation.sort(Sort.Direction.DESC, "count"),
				Aggregation.limit(limit),
				Aggregation.project("count").and("_id").as("month"));
		return mongoTemplate.aggregate(agg, "mymoviedb", MonthCountDto.class).getMappedResults();
	}

	/**
	 * D2 — Số title theo thể loại.
	 *
	 * <p>Flow: split {@code listed_in} → unwind → trim → group genre → top N.</p>
	 */
	public List<GenreCountDto> countByGenre(int limit) {
		// --- StringOperators.Split/Trim thay cho andExpression("$split...") ---
		Aggregation agg = Aggregation.newAggregation(
				Aggregation.match(Criteria.where("listed_in").regex(".+")),
				Aggregation.project()
						.and(StringOperators.Split.valueOf("listed_in").split(","))
						.as("genres"),
				Aggregation.unwind("genres"),
				Aggregation.project()
						.and(StringOperators.Trim.valueOf("genres"))
						.as("genre"),
				Aggregation.match(Criteria.where("genre").regex(".+")),
				Aggregation.group("genre").count().as("count"),
				Aggregation.sort(Sort.Direction.DESC, "count"),
				Aggregation.limit(limit),
				Aggregation.project("count").and("_id").as("genre"));
		return mongoTemplate.aggregate(agg, "mymoviedb", GenreCountDto.class).getMappedResults();
	}

	/**
	 * D3 — Số title theo quốc gia.
	 *
	 * <p>Flow: match country có giá trị → group → sort → limit.</p>
	 */
	public List<CountryCountDto> countByCountry(int limit) {
		Aggregation agg = Aggregation.newAggregation(
				Aggregation.match(Criteria.where("country").regex(".+")),
				Aggregation.group("country").count().as("count"),
				Aggregation.sort(Sort.Direction.DESC, "count"),
				Aggregation.limit(limit),
				Aggregation.project("count").and("_id").as("country"));
		return mongoTemplate.aggregate(agg, "mymoviedb", CountryCountDto.class).getMappedResults();
	}

	/**
	 * D4 — Top Movie dài nhất (theo {@code durationMinutes}).
	 *
	 * <p>Flow: filter Movie + có phút → sort DESC → limit → DTO.</p>
	 */
	public List<MovieRankDto> topLongestMovies(int limit) {
		Query q = Query.query(Criteria.where("type").regex("^Movie$", "i")
				.and("durationMinutes").ne(null))
				.with(Sort.by(Sort.Direction.DESC, "durationMinutes"))
				.limit(limit);
		return toRank(mongoTemplate.find(q, MovieModel.class, "mymoviedb"));
	}

	/**
	 * D5 — Top title thêm gần nhất trên Netflix.
	 *
	 * <p>Flow: filter dateAddedAt hợp lệ → sort DESC → limit → DTO.</p>
	 */
	public List<MovieRankDto> latestAdded(int limit) {
		Query q = Query.query(Criteria.where("dateAddedAt").gt(UNKNOWN_DATE))
				.with(Sort.by(Sort.Direction.DESC, "dateAddedAt"))
				.limit(limit);
		return toRank(mongoTemplate.find(q, MovieModel.class, "mymoviedb"));
	}

	/**
	 * D6 — “Phổ biến” theo quy ước đề bài.
	 *
	 * <p>Flow: Movie + có director → sort {@code release_year} DESC → limit → DTO.</p>
	 */
	public List<MovieRankDto> topPopularByRule(int limit) {
		Query q = Query.query(Criteria.where("type").regex("^Movie$", "i")
				.and("director").regex(".+"))
				.with(Sort.by(Sort.Direction.DESC, "release_year"))
				.limit(limit);
		return toRank(mongoTemplate.find(q, MovieModel.class, "mymoviedb"));
	}

	/** Map entity → hàng bảng xếp hạng trên dashboard. */
	private List<MovieRankDto> toRank(List<MovieModel> movies) {
		List<MovieRankDto> out = new ArrayList<>();
		for (MovieModel m : movies) {
			out.add(MovieRankDto.builder()
					.id(m.getId())
					.title(m.getTitle())
					.type(m.getType())
					.releaseYear(m.getReleaseYear())
					.durationMinutes(m.getDurationMinutes())
					.dateAdded(m.getDateAdded())
					.director(m.getDirector())
					.build());
		}
		return out;
	}

}
