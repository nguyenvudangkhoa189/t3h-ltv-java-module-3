package vn.demo.dto;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.NoArgsConstructor;
import vn.demo.model.MovieModel;

/**
 * DTO (Data Transfer Object) cho form Thymeleaf (PHẦN 2).
 *
 * <p>Nhiệm vụ của tầng DTO: là đối tượng trung gian để truyền/nhận dữ liệu giữa
 * giao diện và tầng nghiệp vụ, KHÔNG dùng entity trực tiếp ngoài view.</p>
 *
 * <p>Khác với entity {@link MovieModel}: ở đây {@code genre} là một chuỗi ngăn cách
 * bởi dấu phẩy ({@code genreText}) cho dễ nhập trên form, sau đó chuyển qua lại với
 * {@code List<String>} của entity.</p>
 */
@Data
@NoArgsConstructor
public class MovieFormDto {

	private String id;

	@NotBlank(message = "Title không được để trống")
	private String title;

	@Min(value = 1888, message = "Year phải từ 1888 trở lên")
	private Integer year;

	/** Các thể loại, nhập dạng "Action, Sci-Fi". */
	private String genreText;

	private String director;

	@Min(value = 0, message = "Rating tối thiểu là 0")
	private Double rating;

	/** Tạo form từ entity (dùng khi mở trang sửa). */
	public static MovieFormDto fromEntity(MovieModel movie) {
		MovieFormDto form = new MovieFormDto();
		form.setId(movie.getId());
		form.setTitle(movie.getTitle());
		form.setYear(movie.getYear());
		form.setDirector(movie.getDirector());
		form.setRating(movie.getRating());
		if (movie.getGenre() != null) {
			// Ghép List<String> thành chuỗi "Action, Sci-Fi" để hiển thị trên input
			form.setGenreText(String.join(", ", movie.getGenre()));
		}
		return form;
	}

	/** Chuyển form thành entity để lưu xuống database. */
	public MovieModel toEntity() {
		MovieModel movie = new MovieModel();
		movie.setId(id);
		movie.setTitle(title);
		movie.setYear(year);
		movie.setDirector(director);
		movie.setRating(rating);
		movie.setGenre(parseGenre());
		return movie;
	}

	/** Tách chuỗi "Action, Sci-Fi" thành List<String>, bỏ khoảng trắng và phần tử rỗng. */
	private List<String> parseGenre() {
		if (genreText == null || genreText.isBlank()) {
			return List.of();
		}
		return Arrays.stream(genreText.split(","))
				.map(String::trim)
				.filter(s -> !s.isEmpty())
				.collect(Collectors.toList());
	}

}
