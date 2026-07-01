package vn.demo.repository;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import vn.demo.model.ItemModel;

/**
 * REPOSITORY — collection con "items".
 *
 * <p>Kế thừa thêm {@link ItemRepositoryCustom} để có hàm {@code updateMulti} (§9).</p>
 */
@Repository
public interface ItemRepository extends MongoRepository<ItemModel, String>, ItemRepositoryCustom {

	/** Derived query — Spring tự sinh: db.items.find({ restaurant_id: ? }) */
	List<ItemModel> findByRestaurantId(String restaurantId);

	/** Bài tập: lọc món theo loại của 1 nhà hàng. */
	List<ItemModel> findByRestaurantIdAndCategory(String restaurantId, String category);

	/** Xóa tất cả món của 1 nhà hàng (cascade delete thủ công). */
	void deleteByRestaurantId(String restaurantId);

}
