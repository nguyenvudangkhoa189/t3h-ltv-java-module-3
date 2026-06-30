package vn.demo.repository;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import vn.demo.model.RestaurantModel;

/**
 * REPOSITORY — tầng truy cập dữ liệu cho collection "restaurants".
 *
 * <p>Kế thừa {@link MongoRepository} để có sẵn CRUD + phân trang
 * ({@code findAll(Pageable)}). Bên dưới thêm vài derived query.</p>
 */
@Repository
public interface RestaurantRepository extends MongoRepository<RestaurantModel, String> {

	/** Tìm theo quận (borough). */
	List<RestaurantModel> findByBorough(String borough);

	/** Tìm 1 restaurant theo id nghiệp vụ (field restaurant_id), KHÔNG phải _id. */
	RestaurantModel findFirstByRestaurantId(String restaurantId);

}
