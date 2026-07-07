package vn.demo.service;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.LookupOperation;
import org.springframework.data.mongodb.core.aggregation.MatchOperation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import vn.demo.dto.ItemWithRestaurantDto;
import vn.demo.dto.PagedListView;
import vn.demo.dto.RestaurantDto;
import vn.demo.dto.RestaurantWithItemsDto;
import vn.demo.exception.ResourceNotFoundException;
import vn.demo.model.RestaurantModel;
import vn.demo.repository.ItemRepository;
import vn.demo.repository.RestaurantRepository;

/**
 * SERVICE — nghiệp vụ nhà hàng (collection cha trong quan hệ 1-n).
 *
 * <p>Nhiệm vụ chính của bài 5:</p>
 * <ul>
 *   <li>Truy vấn liên kết 2 collection bằng {@code $lookup} (Aggregation)</li>
 *   <li>Cập nhật đồng bộ {@code restaurant_id} trên cả restaurants + items (Transaction)</li>
 * </ul>
 *
 * <p>Cả REST API và Thymeleaf đều gọi tới đây — minh họa tách tầng như Bài 3.</p>
 */
@Service
@RequiredArgsConstructor
public class RestaurantService {

	private static final int MAX_PAGES_TO_SHOW = 5;

	private final RestaurantRepository restaurantRepository;
	private final ItemRepository itemRepository;
	/** Dùng để chạy aggregation pipeline ($lookup) — Repository thường không đủ cho join. */
	private final MongoTemplate mongoTemplate;

	/**
	 * Lấy 1 trang danh sách nhà hàng — {@link PagedListView} (tái dùng pattern Bài 4 §6.0).
	 *
	 * <p>{@code Page&lt;RestaurantModel&gt;} chỉ tồn tại trong Service; Controller nhận DTO.</p>
	 */
	public PagedListView<RestaurantDto> findPage(Pageable pageable, String sortBy, String dir) {
		Page<RestaurantModel> page = restaurantRepository.findAll(pageable);
		return PagedListView.from(page, RestaurantDto::fromEntity, null, sortBy, dir, MAX_PAGES_TO_SHOW);
	}

	/**
	 * Tìm nhà hàng theo id nghiệp vụ ({@code restaurant_id}), KHÔNG phải {@code _id}.
	 *
	 * @throws ResourceNotFoundException nếu không tìm thấy
	 */
	public RestaurantModel findByRestaurantId(String restaurantId) {
		RestaurantModel restaurant = restaurantRepository.findFirstByRestaurantId(restaurantId);
		if (restaurant == null) {
			throw new ResourceNotFoundException("Không tìm thấy nhà hàng với restaurant_id: " + restaurantId);
		}
		return restaurant;
	}

	/**
	 * §7.4 — $lookup: lấy 1 nhà hàng kèm toàn bộ món ăn của nó.
	 *
	 * <p>Tương đương mongosh:</p>
	 * <pre>
	 * db.restaurants.aggregate([
	 *   { $match: { restaurant_id: "..." } },
	 *   { $lookup: { from: "items", localField: "restaurant_id",
	 *                foreignField: "restaurant_id", as: "menuItems" } }
	 * ])
	 * </pre>
	 */
	public List<RestaurantWithItemsDto> getRestaurantWithItems(String restaurantId) {
		// B1: lọc đúng nhà hàng cần tìm (giống $match trong mongosh)
		MatchOperation matchStage = Aggregation.match(Criteria.where("restaurant_id").is(restaurantId));
		// B2: nối collection items vào field menuItems (giống $lookup)
		LookupOperation lookupStage = LookupOperation.newLookup()
				.from("items")                 // collection phụ
				.localField("restaurant_id")   // field ở collection chính (restaurants)
				.foreignField("restaurant_id") // field ở collection phụ (items)
				.as("menuItems");              // tên field mảng chứa kết quả join
		Aggregation aggregation = Aggregation.newAggregation(matchStage, lookupStage);
		// Map kết quả thẳng vào DTO — type-safe, không dùng HashMap
		return mongoTemplate
				.aggregate(aggregation, "restaurants", RestaurantWithItemsDto.class)
				.getMappedResults();
	}

	/**
	 * §7.5 — $lookup ngược: lấy các món của nhà hàng, mỗi món kèm thông tin nhà hàng.
	 *
	 * <p>Truy vấn bắt đầu từ collection {@code items} thay vì {@code restaurants}.</p>
	 */
	public List<ItemWithRestaurantDto> getItemsWithRestaurantInfo(String restaurantId) {
		MatchOperation matchOperation = Aggregation.match(Criteria.where("restaurant_id").is(restaurantId));
		LookupOperation lookupOperation = LookupOperation.newLookup()
				.from("restaurants")
				.localField("restaurant_id")
				.foreignField("restaurant_id")
				.as("restaurantInfo");         // luôn là mảng, kể cả quan hệ 1-1
		Aggregation aggregation = Aggregation.newAggregation(matchOperation, lookupOperation);
		return mongoTemplate
				.aggregate(aggregation, "items", ItemWithRestaurantDto.class)
				.getMappedResults();
	}

	/**
	 * §8.5 — Đổi {@code restaurant_id} bằng cách lặp save từng item.
	 *
	 * <p>Nhược điểm: N+1 write khi có nhiều món. Dùng để so sánh với {@link #updateRestaurantIdOptimized}.</p>
	 *
	 * <p>{@code @Transactional}: cả 2 bước cùng commit hoặc rollback (cần Replica Set + MongoTransactionManager).</p>
	 */
	@Transactional
	public void updateRestaurantIdWithSave(String oldId, String newId) {
		validateNewRestaurantId(oldId, newId);
		RestaurantModel restaurant = restaurantRepository.findFirstByRestaurantId(oldId);
		if (restaurant == null) {
			throw new ResourceNotFoundException("Restaurant not found with restaurant_id: " + oldId);
		}
		// Bước 1: cập nhật id trên collection restaurants
		restaurant.setRestaurantId(newId);
		restaurantRepository.save(restaurant);
		// Bước 2: cập nhật restaurant_id trên từng item (chậm nếu nhiều món)
		itemRepository.findByRestaurantId(oldId).forEach(item -> {
			item.setRestaurantId(newId);
			itemRepository.save(item);
		});
	}

	/**
	 * §9.4 — Đổi {@code restaurant_id} bằng {@code updateMulti} (1 câu lệnh cho tất cả items).
	 *
	 * @return số item đã được cập nhật
	 */
	@Transactional
	public long updateRestaurantIdOptimized(String oldId, String newId) {
		validateNewRestaurantId(oldId, newId);
		RestaurantModel restaurant = restaurantRepository.findFirstByRestaurantId(oldId);
		if (restaurant == null) {
			throw new ResourceNotFoundException("Restaurant not found: " + oldId);
		}
		restaurant.setRestaurantId(newId);
		restaurantRepository.save(restaurant);              // Bước 1
		return itemRepository.updateRestaurantId(oldId, newId); // Bước 2: 1 lệnh updateMulti
	}

	/**
	 * Bài tập mở rộng — xóa nhà hàng kèm toàn bộ món (cascade delete thủ công).
	 *
	 * <p>MongoDB không có FK constraint → app phải tự xóa items trước/sau.</p>
	 */
	@Transactional
	public void deleteRestaurantWithItems(String restaurantId) {
		findByRestaurantId(restaurantId); // kiểm tra tồn tại
		itemRepository.deleteByRestaurantId(restaurantId);
		RestaurantModel restaurant = restaurantRepository.findFirstByRestaurantId(restaurantId);
		restaurantRepository.delete(restaurant);
	}

	/** Kiểm tra id mới không trùng và khác id cũ. */
	private void validateNewRestaurantId(String oldId, String newId) {
		if (oldId.equals(newId)) {
			throw new IllegalArgumentException("ID mới phải khác ID cũ");
		}
		if (restaurantRepository.existsByRestaurantId(newId)) {
			throw new IllegalArgumentException("restaurant_id mới đã tồn tại: " + newId);
		}
	}

}
