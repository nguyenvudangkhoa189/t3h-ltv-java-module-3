package vn.demo.model;

import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * MODEL (entity) — ánh xạ collection {@code movies}.
 *
 * <p><b>Chiến lược test Bài 8:</b> class này là <i>anemic model</i> (chủ yếu field +
 * getter/setter), <b>không viết unit test Model</b>. Logic nghiệp vụ nằm ở
 * {@link vn.demo.service.MovieService} — nơi học viên viết Mockito test.</p>
 */
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "movies")
public class MovieModel {

	@Id
	private String id;

	@NotBlank(message = "Title không được để trống")
	@Indexed
	private String title;

	@Min(value = 1888, message = "Year phải từ 1888 trở lên")
	private Integer year;

	private List<String> genre;

	@Size(max = 120, message = "Director tối đa 120 ký tự")
	private String director;

	@Min(value = 0, message = "Rating tối thiểu là 0")
	private Double rating;

}
