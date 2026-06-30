package vn.demo.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * Object đánh giá, lồng bên trong {@link RestaurantModel}.
 *
 * <p>{@code @JsonIgnoreProperties(ignoreUnknown=true)} để bỏ qua field thừa trong
 * file (ví dụ "date") mà model không khai báo, tránh lỗi khi parse.</p>
 */
@Getter
@Setter
@ToString
@JsonIgnoreProperties(ignoreUnknown = true)
public class GradeModel {

	private String grade;
	private Integer score;

}
