package vn.demo.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import vn.demo.model.RestaurantModel;

/**
 * DTO (Data Transfer Object) hiển thị trên danh sách restaurant.
 *
 * <p>Nhiệm vụ của tầng DTO: chỉ chứa các field cần thiết cho bảng list.</p>
 */
@Data
@NoArgsConstructor
public class RestaurantDto {

	private String restaurantId;
	private String name;
	private String borough;
	private String cuisine;

	/** Chuyển entity sang DTO (Service gọi khi trả dữ liệu ra Controller). */
	public static RestaurantDto fromEntity(RestaurantModel restaurant) {
		if (restaurant == null) {
			return null;
		}
		RestaurantDto dto = new RestaurantDto();
		dto.setRestaurantId(restaurant.getRestaurantId());
		dto.setName(restaurant.getName());
		dto.setBorough(restaurant.getBorough());
		dto.setCuisine(restaurant.getCuisine());
		return dto;
	}

}
