package vn.demo.repository;

import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;

import com.mongodb.client.result.UpdateResult;

import lombok.RequiredArgsConstructor;
import vn.demo.model.ItemModel;

/**
 * Hiện thực {@link ItemRepositoryCustom} — cập nhật hàng loạt bằng updateMulti (§9.3).
 *
 * <p><b>Quy tắc đặt tên bắt buộc:</b> class phải tên {@code ItemRepositoryImpl}
 * để Spring Data tự nhận và merge vào {@link ItemRepository}.</p>
 */
@Repository
@RequiredArgsConstructor
public class ItemRepositoryImpl implements ItemRepositoryCustom {

	private final MongoTemplate mongoTemplate;

	@Override
	public long updateRestaurantId(String oldId, String newId) {
		// Điều kiện: tất cả item có restaurant_id = oldId
		Query query = new Query(Criteria.where("restaurant_id").is(oldId));
		// Giá trị mới ghi đè
		Update update = new Update().set("restaurant_id", newId);
		// updateMulti: 1 lệnh cập nhật nhiều document (thay vì lặp save)
		UpdateResult result = mongoTemplate.updateMulti(query, update, ItemModel.class);
		return result.getModifiedCount();
	}

}
