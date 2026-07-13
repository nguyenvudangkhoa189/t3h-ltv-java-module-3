package vn.demo.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import vn.demo.dto.LanguageCountDto;

/**
 * SERVICE — aggregation MongoDB cho biểu đồ Chart.js ({@code /movies/chart}).
 */
@Service
@RequiredArgsConstructor
public class MovieChartService {

	private final MongoTemplate mongoTemplate;

	@Value("${app.movies.chart-min-count:50}")
	private int chartMinCount;

	@Value("${app.movies.chart-limit:10}")
	private int chartLimit;

	/** Top quốc gia theo số title — aggregation trên field {@code country}. */
	public List<LanguageCountDto> topCountries() {
		// --- Pipeline: match → group → lọc count → sort → limit → project ---
		Aggregation agg = Aggregation.newAggregation(
				Aggregation.match(Criteria.where("country").ne(null)),
				Aggregation.group("country").count().as("count"),
				Aggregation.match(Criteria.where("count").gt(chartMinCount)),
				Aggregation.sort(Sort.Direction.DESC, "count"),
				Aggregation.limit(chartLimit),
				Aggregation.project("count").and("_id").as("language"));

		// --- Chạy trên collection mymoviedb, map sang DTO cho Chart.js ---
		return mongoTemplate.aggregate(agg, "mymoviedb", LanguageCountDto.class)
				.getMappedResults();
	}

}
