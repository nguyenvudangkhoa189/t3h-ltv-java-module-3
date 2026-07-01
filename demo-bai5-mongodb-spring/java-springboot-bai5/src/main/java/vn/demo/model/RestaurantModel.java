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
 * MODEL — collection chính "restaurants" (cha trong quan hệ 1-n).
 *
 * <p>Phân biệt 2 loại id (giống Bài 4):</p>
 * <ul>
 *   <li>{@code id} — khóa {@code _id} do MongoDB tự sinh</li>
 *   <li>{@code restaurantId} — id nghiệp vụ, dùng làm khóa liên kết với {@link ItemModel}</li>
 * </ul>
 */
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "restaurants")
public class RestaurantModel {

	@Id
	private String id;

	/** Khóa liên kết — các item trỏ về field này qua restaurant_id. */
	@Indexed
	@Field("restaurant_id")
	private String restaurantId;

	private String name;
	private String borough;
	private String cuisine;

}
