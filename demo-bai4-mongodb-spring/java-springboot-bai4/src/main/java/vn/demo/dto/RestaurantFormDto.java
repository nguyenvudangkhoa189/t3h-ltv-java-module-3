package vn.demo.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import vn.demo.model.RestaurantModel;

/**
 * DTO (Data Transfer Object) cho trang chi tiết / form cập nhật restaurant.
 *
 * <p>Nhiệm vụ của tầng DTO: là đối tượng trung gian để truyền/nhận dữ liệu giữa
 * giao diện và tầng nghiệp vụ, KHÔNG dùng entity trực tiếp ngoài view.</p>
 */
@Data
@NoArgsConstructor
public class RestaurantFormDto {

	private String restaurantId;
	private String name;
	private String borough;
	private String cuisine;

	/** Tạo form từ entity (dùng khi mở trang chi tiết). */
	public static RestaurantFormDto fromEntity(RestaurantModel restaurant) {
		if (restaurant == null) {
			return null;
		}
		RestaurantFormDto dto = new RestaurantFormDto();
		dto.setRestaurantId(restaurant.getRestaurantId());
		dto.setName(restaurant.getName());
		dto.setBorough(restaurant.getBorough());
		dto.setCuisine(restaurant.getCuisine());
		return dto;
	}

	/** Chuyển form thành entity để Service merge và lưu xuống database. */
	public RestaurantModel toEntity() {
		RestaurantModel restaurant = new RestaurantModel();
		restaurant.setRestaurantId(restaurantId);
		restaurant.setName(name);
		restaurant.setBorough(borough);
		restaurant.setCuisine(cuisine);
		return restaurant;
	}

}
