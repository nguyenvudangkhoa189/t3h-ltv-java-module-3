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
 * MODEL — collection "restaurants".
 *
 * <p>{@code @Indexed} trên {@code restaurantId} và {@code borough} minh họa index cho field
 * hay dùng trong find/sort (syllabus §2).</p>
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

	@Indexed
	@Field("restaurant_id")
	private String restaurantId;

	private String name;

	@Indexed
	private String borough;

	private String cuisine;

}
