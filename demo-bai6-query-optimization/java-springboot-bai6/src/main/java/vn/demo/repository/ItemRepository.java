package vn.demo.repository;

import java.util.Collection;
import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import vn.demo.model.ItemModel;

/**
 * REPOSITORY — collection "items".
 *
 * <p>{@code findByRestaurantIdIn} tránh N+1 khi load món của nhiều nhà hàng (syllabus §8).</p>
 */
@Repository
public interface ItemRepository extends MongoRepository<ItemModel, String> {

	List<ItemModel> findByRestaurantId(String restaurantId);

	/** Một query lấy món của nhiều nhà hàng — thay loop {@code findByRestaurantId}. */
	List<ItemModel> findByRestaurantIdIn(Collection<String> restaurantIds);

	List<ItemModel> findByRestaurantIdAndCategory(String restaurantId, String category);

}
