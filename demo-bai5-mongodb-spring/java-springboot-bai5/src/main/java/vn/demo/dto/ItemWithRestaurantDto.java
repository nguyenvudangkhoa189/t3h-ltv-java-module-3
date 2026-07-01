package vn.demo.dto;

import java.util.List;

import org.springframework.data.mongodb.core.mapping.Field;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import vn.demo.model.RestaurantModel;

/**
 * DTO kết quả $lookup ngược — món kèm thông tin nhà hàng (§7.5 syllabus).
 *
 * <p>{@code restaurantInfo} luôn là mảng (kể cả 1-1) — có thể dùng $unwind trong mongosh để phẳng.</p>
 */
@Getter
@Setter
@NoArgsConstructor
public class ItemWithRestaurantDto {

	@Field("restaurant_id")
	private String restaurantId;

	private String name;
	private Double price;
	private String category;

	/** Thông tin nhà hàng join từ collection restaurants. */
	private List<RestaurantModel> restaurantInfo;

}
