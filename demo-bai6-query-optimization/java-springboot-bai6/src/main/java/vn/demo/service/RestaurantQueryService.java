package vn.demo.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.BulkOperations;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

import com.mongodb.bulk.BulkWriteResult;

import lombok.RequiredArgsConstructor;
import vn.demo.dto.BulkUpdateResultDto;
import vn.demo.dto.CursorPageDto;
import vn.demo.dto.RestaurantSummaryDto;
import vn.demo.dto.RestaurantWithItemsDto;
import vn.demo.model.ItemModel;
import vn.demo.model.RestaurantModel;
import vn.demo.repository.ItemRepository;
import vn.demo.repository.RestaurantRepository;

/**
 * Nghiệp vụ tối ưu truy vấn restaurants (syllabus §4–§8).
 */
@Service
@RequiredArgsConstructor
public class RestaurantQueryService {

	private final RestaurantRepository restaurantRepository;
	private final ItemRepository itemRepository;
	private final MongoTemplate mongoTemplate;

	@Value("${app.restaurants.feed-size:5}")
	private int defaultFeedSize;

	/** §4.2 — projection: chỉ name, borough, restaurant_id. */
	public List<RestaurantSummaryDto> findSummaries() {
		return restaurantRepository.findAllNamesAndBoroughs().stream()
				.map(r -> new RestaurantSummaryDto(r.getId(), r.getRestaurantId(), r.getName(), r.getBorough()))
				.toList();
	}

	/** §4.3 — full-text search (cần text index). */
	public List<RestaurantSummaryDto> searchByText(String keyword) {
		if (keyword == null || keyword.isBlank()) {
			throw new IllegalArgumentException("keyword không được để trống");
		}
		return restaurantRepository.searchByKeyword(keyword.trim()).stream()
				.map(RestaurantSummaryDto::fromEntity)
				.toList();
	}

	/** §6 — tìm theo prefix (regex {@code ^prefix}). */
	public List<RestaurantSummaryDto> searchByPrefix(String prefix) {
		if (prefix == null || prefix.isBlank()) {
			throw new IllegalArgumentException("prefix không được để trống");
		}
		return restaurantRepository.findByNameRegex("^" + prefix.trim()).stream()
				.map(RestaurantSummaryDto::fromEntity)
				.toList();
	}

	/**
	 * §7 — cursor pagination bằng {@code _id}; trả {@link CursorPageDto} generic (§7.0 syllabus).
	 *
	 * <p>{@code RestaurantModel} chỉ tồn tại trong method; API nhận {@link RestaurantSummaryDto}.</p>
	 */
	public CursorPageDto<RestaurantSummaryDto> findFeed(String afterId, Integer limit) {
		// (1) pageSize = số bản ghi MỘT TRANG trả về cho client.
		//     - Client không truyền limit  -> dùng mặc định (app.restaurants.feed-size).
		//     - Chặn trên 50 để 1 request không kéo quá nhiều dữ liệu (bảo vệ server).
		int pageSize = (limit == null || limit < 1) ? defaultFeedSize : Math.min(limit, 50);

		// (2) Con trỏ (cursor): nếu có afterId -> chỉ lấy bản ghi có _id > afterId
		//     (tức là các bản ghi NẰM SAU bản ghi cuối của trang trước).
		Query query = new Query();
		if (afterId != null && !afterId.isBlank()) {
			if (!ObjectId.isValid(afterId)) {
				throw new IllegalArgumentException("afterId không hợp lệ (phải là ObjectId hex)");
			}
			query.addCriteria(Criteria.where("_id").gt(new ObjectId(afterId)));
		}

		// (3) Sort theo _id để thứ tự ỔN ĐỊNH giữa các lần gọi (bắt buộc với cursor).
		//     MẸO "+1": lấy dư 1 bản ghi (pageSize + 1) để biết CÒN trang sau hay không
		//     mà KHÔNG phải chạy thêm câu count tổng số document (count tốn thời gian khi data lớn).
		query.with(Sort.by(Sort.Direction.ASC, "_id")).limit(pageSize + 1);
		List<RestaurantModel> fetched = mongoTemplate.find(query, RestaurantModel.class, "restaurants");

		// (4) hasMore: nếu lấy được nhiều hơn pageSize (tức chạm tới bản ghi "dư" thứ pageSize+1)
		//     => vẫn còn dữ liệu phía sau => client có thể "Xem thêm".
		boolean hasMore = fetched.size() > pageSize;

		// (5) Cắt bỏ bản ghi "dư" — chỉ giữ đúng pageSize bản ghi để trả về.
		List<RestaurantModel> page = hasMore ? fetched.subList(0, pageSize) : fetched;

		// (6) lastSeenId = _id bản ghi CUỐI trang này -> client gửi lại làm afterId cho lần sau.
		List<RestaurantSummaryDto> items = page.stream().map(RestaurantSummaryDto::fromEntity).toList();
		String lastSeenId = page.isEmpty() ? null : page.get(page.size() - 1).getId();
		return new CursorPageDto<>(items, lastSeenId, hasMore);
	}

	/**
	 * §8 — so sánh N+1 vs tối ưu {@code findByRestaurantIdIn}.
	 *
	 * @param mode {@code n1} (chậm) hoặc {@code optimized} (mặc định)
	 */
	public List<RestaurantWithItemsDto> findAllWithItems(String mode) {
		List<RestaurantModel> restaurants = restaurantRepository.findAll();
		if ("n1".equalsIgnoreCase(mode)) {
			return loadItemsNPlusOne(restaurants);
		}
		return loadItemsOptimized(restaurants);
	}

	/** ❌ N+1: 1 query restaurants + N query items. */
	private List<RestaurantWithItemsDto> loadItemsNPlusOne(List<RestaurantModel> restaurants) {
		List<RestaurantWithItemsDto> result = new ArrayList<>();
		for (RestaurantModel r : restaurants) {
			List<ItemModel> items = itemRepository.findByRestaurantId(r.getRestaurantId());
			result.add(toDto(r, items));
		}
		return result;
	}

	/** ✅ 1 query items cho tất cả restaurant_id. */
	private List<RestaurantWithItemsDto> loadItemsOptimized(List<RestaurantModel> restaurants) {
		List<String> ids = restaurants.stream().map(RestaurantModel::getRestaurantId).toList();
		List<ItemModel> allItems = itemRepository.findByRestaurantIdIn(ids);
		Map<String, List<ItemModel>> byRestaurant = allItems.stream()
				.collect(Collectors.groupingBy(ItemModel::getRestaurantId));

		return restaurants.stream()
				.map(r -> toDto(r, byRestaurant.getOrDefault(r.getRestaurantId(), List.of())))
				.toList();
	}

	private RestaurantWithItemsDto toDto(RestaurantModel r, List<ItemModel> items) {
		return new RestaurantWithItemsDto(
				r.getRestaurantId(), r.getName(), r.getBorough(), r.getCuisine(), items);
	}

	/**
	 * §5.2 — BulkOperations: gom nhiều update trong một lần {@code execute()}.
	 *
	 * <p>Demo an toàn: chỉ update document khớp điều kiện rõ ràng (không dùng Query rỗng).</p>
	 */
	public BulkUpdateResultDto bulkDemoUpdate() {
		BulkOperations bulkOps =
				mongoTemplate.bulkOps(BulkOperations.BulkMode.UNORDERED, "restaurants");

		// Tác vụ 1: đổi borough của "Wild East"
		Query q1 = new Query(Criteria.where("name").is("Wild East"));
		Update u1 = new Update().set("borough", "Brooklyn Center");
		bulkOps.updateOne(q1, u1);

		// Tác vụ 2: đổi cuisine "Bakery" → "Pastry Shop"
		Query q2 = new Query(Criteria.where("cuisine").is("Bakery"));
		Update u2 = new Update().set("cuisine", "Pastry Shop");
		bulkOps.updateMulti(q2, u2);

		BulkWriteResult result = bulkOps.execute();
		return new BulkUpdateResultDto(
				result.getModifiedCount(),
				result.getMatchedCount(),
				"Bulk update: Wild East → Brooklyn Center; cuisine Bakery → Pastry Shop");
	}

}
