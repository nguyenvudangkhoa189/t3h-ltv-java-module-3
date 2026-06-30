package vn.demo.model;

import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * MODEL (entity) — ánh xạ tới collection "restaurants".
 *
 * <p>File JSON nguồn có các object lồng nhau (address, grades) nên ta tạo các class
 * tương ứng: {@link AddressModel}, {@link GradeModel}.</p>
 *
 * <p><b>Phân biệt 2 loại id:</b></p>
 * <ul>
 *   <li>{@code id} — khóa "_id" (ObjectId) do MongoDB tự sinh.</li>
 *   <li>{@code restaurantId} — id nghiệp vụ nằm trong field "restaurant_id" của file.</li>
 * </ul>
 */
@Getter
@Setter
@ToString
@JsonIgnoreProperties(ignoreUnknown = true) // bỏ qua field lạ trong file JSON
@Document(collection = "restaurants")
public class RestaurantModel {

	/** Khóa "_id" của MongoDB. */
	@Id
	private String id;

	/**
	 * Id nghiệp vụ. @Field: tên field trong MongoDB; @JsonProperty: tên field trong file JSON;
	 * @Indexed: đánh index để tra cứu nhanh.
	 */
	@Indexed
	@Field("restaurant_id")
	@JsonProperty("restaurant_id")
	private String restaurantId;

	private String name;
	private String borough;
	private String cuisine;
	private AddressModel address;       // object lồng
	private List<GradeModel> grades;    // mảng object lồng

}
