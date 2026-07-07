package vn.demo.repository;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import vn.demo.model.RestaurantModel;

/**
 * REPOSITORY — collection "restaurants".
 *
 * <p>Custom {@code @Query}: projection (§4.2), {@code $text} (§4.3), regex prefix (§6).</p>
 */
@Repository
public interface RestaurantRepository extends MongoRepository<RestaurantModel, String> {

	RestaurantModel findFirstByRestaurantId(String restaurantId);

	boolean existsByRestaurantId(String restaurantId);

	/** Projection — chỉ lấy field cần hiển thị danh sách. */
	@Query(value = "{}", fields = "{ 'name': 1, 'borough': 1, 'restaurant_id': 1 }")
	List<RestaurantModel> findAllNamesAndBoroughs();

	/**
	 * Full-text search — cần text index trên {@code name} + {@code cuisine} (xem {@code IndexConfig}).
	 *
	 * <p>{@code ?0} = tham số đầu tiên ({@code keyword}).</p>
	 */
	@Query("{ $text: { $search: ?0 } }")
	List<RestaurantModel> searchByKeyword(String keyword);

	/**
	 * Regex tìm theo pattern — service truyền {@code "^" + prefix} để tìm đầu chuỗi.
	 */
	@Query("{ 'name': { $regex: ?0 } }")
	List<RestaurantModel> findByNameRegex(String regex);

}
