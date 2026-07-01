package vn.demo.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import vn.demo.model.RestaurantModel;

/**
 * REPOSITORY — collection cha "restaurants".
 *
 * <p>Kế thừa {@link MongoRepository} có sẵn CRUD + phân trang.</p>
 */
@Repository
public interface RestaurantRepository extends MongoRepository<RestaurantModel, String> {

	/** Tìm theo id nghiệp vụ (restaurant_id), KHÔNG phải _id MongoDB. */
	RestaurantModel findFirstByRestaurantId(String restaurantId);

	/** Kiểm tra trùng khi đổi restaurant_id (transaction §8). */
	boolean existsByRestaurantId(String restaurantId);

}
