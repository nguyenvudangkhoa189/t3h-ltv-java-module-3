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
 * MODEL — collection "items" (con trong quan hệ 1-n với restaurants).
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

	@Indexed
	@Field("restaurant_id")
	private String restaurantId;

	private String name;
	private String description;
	private Double price;
	private String category;

}
