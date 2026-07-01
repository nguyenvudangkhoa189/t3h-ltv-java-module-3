package vn.demo.dto;

import java.util.List;

import org.springframework.data.mongodb.core.mapping.Field;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import vn.demo.model.ItemModel;

/**
 * DTO kết quả $lookup — 1 nhà hàng kèm danh sách món (§7.2 syllabus).
 *
 * <p>Dùng DTO thay {@code HashMap} để type-safe khi map kết quả aggregation.</p>
 * <p>Field {@code menuItems} tương ứng {@code as: "menuItems"} trong $lookup.</p>
 */
@Getter
@Setter
@NoArgsConstructor
public class RestaurantWithItemsDto {

	@Field("restaurant_id")
	private String restaurantId;

	private String name;
	private String borough;
	private String cuisine;

	/** Mảng item join được từ collection items — không cần query riêng. */
	private List<ItemModel> menuItems;

}
