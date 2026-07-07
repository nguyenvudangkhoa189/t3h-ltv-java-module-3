package vn.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Kết quả aggregation: item + tên nhà hàng (syllabus §9 — {@code $match} + {@code $lookup} + {@code $project}).
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ItemWithRestaurantDto {

	private String id;
	private String name;
	private Double price;
	private String category;
	private String restaurantId;
	private String restaurantName;

}
