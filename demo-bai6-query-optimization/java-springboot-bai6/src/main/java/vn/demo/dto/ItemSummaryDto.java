package vn.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import vn.demo.model.ItemModel;

/**
 * DTO projection — danh sách món theo {@code restaurant_id} (syllabus §10.5).
 *
 * <p>Chỉ các field cần cho kiểm tra sau {@code PUT /api/items/reassign} — không lộ entity ra Controller.</p>
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ItemSummaryDto {

	private String id;
	private String restaurantId;
	private String name;
	private Double price;
	private String category;

	/** Chuyển entity sang DTO (Service gọi trước khi trả Controller). */
	public static ItemSummaryDto fromEntity(ItemModel item) {
		if (item == null) {
			return null;
		}
		return new ItemSummaryDto(
				item.getId(), item.getRestaurantId(), item.getName(), item.getPrice(), item.getCategory());
	}

}
