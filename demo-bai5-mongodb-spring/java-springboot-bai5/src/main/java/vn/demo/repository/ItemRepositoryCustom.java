package vn.demo.repository;

/**
 * Interface custom cho các thao tác phức tạp không có sẵn trong MongoRepository (§9.1).
 *
 * <p>Spring Data tự ghép với {@link ItemRepositoryImpl} (quy tắc đặt tên: *RepositoryImpl).</p>
 */
public interface ItemRepositoryCustom {

	/**
	 * Cập nhật hàng loạt restaurant_id trên tất cả items khớp oldId.
	 *
	 * @return số document đã sửa
	 */
	long updateRestaurantId(String oldId, String newId);

}
