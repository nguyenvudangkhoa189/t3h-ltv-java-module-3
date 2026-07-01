package vn.demo.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * MODEL — collection phụ "items" (con trong quan hệ 1-n).
 *
 * <p>{@code @Field("restaurant_id")} BẮT BUỘC: nếu thiếu, Spring lưu field tên {@code restaurantId}
 * (camelCase) trong MongoDB → $lookup theo {@code restaurant_id} sẽ không khớp (§7.1 syllabus).</p>
 */
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "items")
public class ItemModel {

	@Id
	private String id;

	/** Khóa liên kết trỏ về {@link RestaurantModel#getRestaurantId()}. */
	@Indexed
	@Field("restaurant_id")
	private String restaurantId;

	private String name;
	private String description;
	private Double price;
	private String category;

}
