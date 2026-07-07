package vn.demo.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationOperation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

import com.mongodb.client.result.UpdateResult;

import lombok.RequiredArgsConstructor;
import vn.demo.dto.ItemSummaryDto;
import vn.demo.dto.ItemWithRestaurantDto;
import vn.demo.repository.ItemRepository;

/**
 * Nghiệp vụ tối ưu trên collection items (syllabus §4.4 tóm tắt, chi tiết Bài 5 §9).
 */
@Service
@RequiredArgsConstructor
public class ItemQueryService {

	private final MongoTemplate mongoTemplate;
	private final ItemRepository itemRepository;

	/**
	 * Tái sử dụng pattern {@code updateMulti} từ Bài 5 §9 — cập nhật một phần, không load entity.
	 *
	 * @return số document đã sửa
	 */
	public long reassignRestaurantId(String oldId, String newId) {
		if (oldId == null || oldId.isBlank() || newId == null || newId.isBlank()) {
			throw new IllegalArgumentException("oldId và newId không được để trống");
		}
		if (oldId.equals(newId)) {
			throw new IllegalArgumentException("oldId và newId phải khác nhau");
		}

		Query query = new Query(Criteria.where("restaurant_id").is(oldId));
		Update update = new Update().set("restaurant_id", newId);
		UpdateResult result = mongoTemplate.updateMulti(query, update, "items");
		return result.getModifiedCount();
	}

	/**
	 * §9 — aggregation: {@code $match} sớm → {@code $lookup} → {@code $unwind} → {@code $project}.
	 */
	public List<ItemWithRestaurantDto> findItemsWithRestaurant(String category) {
		List<AggregationOperation> ops = new ArrayList<>();

		// $match càng sớm càng tốt — lọc trước khi join
		if (category != null && !category.isBlank()) {
			ops.add(Aggregation.match(Criteria.where("category").is(category.trim())));
		}

		ops.add(Aggregation.lookup("restaurants", "restaurant_id", "restaurant_id", "restaurant"));
		ops.add(Aggregation.unwind("restaurant"));
		ops.add(Aggregation.project()
				.and("_id").as("id")
				.and("name").as("name")
				.and("price").as("price")
				.and("category").as("category")
				.and("restaurant_id").as("restaurantId")
				.and("restaurant.name").as("restaurantName"));

		Aggregation aggregation = Aggregation.newAggregation(ops);
		AggregationResults<ItemWithRestaurantDto> results =
				mongoTemplate.aggregate(aggregation, "items", ItemWithRestaurantDto.class);
		return results.getMappedResults();
	}

	/**
	 * Lấy món theo {@code restaurant_id} — trả {@link ItemSummaryDto} (kiểm tra sau reassign, §10.5).
	 */
	public List<ItemSummaryDto> findSummariesByRestaurantId(String restaurantId) {
		if (restaurantId == null || restaurantId.isBlank()) {
			throw new IllegalArgumentException("restaurantId không được để trống");
		}
		return itemRepository.findByRestaurantId(restaurantId.trim()).stream()
				.map(ItemSummaryDto::fromEntity)
				.toList();
	}

}
