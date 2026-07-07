package vn.demo.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import vn.demo.model.ItemModel;

/**
 * Nhà hàng kèm danh sách món — dùng so sánh N+1 vs {@code findByRestaurantIdIn} (syllabus §8).
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RestaurantWithItemsDto {

	private String restaurantId;
	private String name;
	private String borough;
	private String cuisine;
	private List<ItemModel> items;

}
