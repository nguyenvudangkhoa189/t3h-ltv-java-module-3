package vn.demo.service;

import java.util.List;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import vn.demo.exception.ResourceNotFoundException;
import vn.demo.model.ItemModel;
import vn.demo.repository.ItemRepository;

/**
 * SERVICE — nghiệp vụ món ăn (collection con / phụ trong quan hệ 1-n).
 *
 * <p>Mỗi {@link ItemModel} có {@code restaurantId} trỏ về nhà hàng cha.</p>
 */
@Service
@RequiredArgsConstructor
public class ItemService {

	private final ItemRepository itemRepository;

	/** Lấy 1 món theo {@code _id} của MongoDB (dùng cho form sửa/xóa). */
	public ItemModel getById(String id) {
		return itemRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy món với id: " + id));
	}

	/** Lấy tất cả món của 1 nhà hàng (truy vấn đơn giản, không cần $lookup). */
	public List<ItemModel> findByRestaurantId(String restaurantId) {
		return itemRepository.findByRestaurantId(restaurantId);
	}

	/**
	 * Bài tập §7 — lọc món theo loại (category) của 1 nhà hàng.
	 * Spring tự sinh query từ tên hàm derived query.
	 */
	public List<ItemModel> findByRestaurantIdAndCategory(String restaurantId, String category) {
		return itemRepository.findByRestaurantIdAndCategory(restaurantId, category);
	}

	/**
	 * Tạo món mới. Đặt {@code id = null} để chắc chắn MongoDB sinh {@code _id} mới (insert).
	 *
	 * <p>Caller phải gán {@code restaurantId} trước khi gọi (gắn khóa liên kết về nhà hàng cha).</p>
	 */
	public ItemModel create(ItemModel item) {
		item.setId(null);
		return itemRepository.save(item);
	}

	/** Cập nhật partial — chỉ ghi đè field có giá trị (giống Bài 3/4). */
	public ItemModel update(String id, ItemModel updated) {
		ItemModel existing = getById(id);
		if (updated.getName() != null) {
			existing.setName(updated.getName());
		}
		if (updated.getDescription() != null) {
			existing.setDescription(updated.getDescription());
		}
		if (updated.getPrice() != null) {
			existing.setPrice(updated.getPrice());
		}
		if (updated.getCategory() != null) {
			existing.setCategory(updated.getCategory());
		}
		return itemRepository.save(existing);
	}

	public void delete(String id) {
		if (!itemRepository.existsById(id)) {
			throw new ResourceNotFoundException("Không tìm thấy món với id: " + id);
		}
		itemRepository.deleteById(id);
	}

}
