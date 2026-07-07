package vn.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import vn.demo.model.RestaurantModel;

/**
 * DTO projection — chỉ các field cần hiển thị danh sách (syllabus §4.2).
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RestaurantSummaryDto {

	private String id;
	private String restaurantId;
	private String name;
	private String borough;

	/** Chuyển entity sang DTO (Service gọi trước khi trả Controller). */
	public static RestaurantSummaryDto fromEntity(RestaurantModel restaurant) {
		if (restaurant == null) {
			return null;
		}
		return new RestaurantSummaryDto(
				restaurant.getId(), restaurant.getRestaurantId(), restaurant.getName(), restaurant.getBorough());
	}

}
