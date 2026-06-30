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
 * MODEL (entity) — ánh xạ tới collection "movies" trong MongoDB.
 *
 * <p>Nhiệm vụ của tầng Model: mô tả cấu trúc 1 document. Mỗi thuộc tính tương ứng
 * một field trong collection.</p>
 *
 * <p>Dùng {@code @Getter/@Setter/@ToString} thay cho {@code @Data} trên entity để
 * tránh {@code equals()/hashCode()} mặc định gây lỗi tinh vi với dữ liệu lồng nhau.</p>
 */
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "movies") // ánh xạ class này tới collection "movies"
public class MovieModel {

	/** Khóa chính "_id" của MongoDB (kiểu String, ánh xạ từ ObjectId). */
	@Id
	private String id;

	/** Tên phim. @Indexed: tạo index để tìm kiếm theo title nhanh hơn. */
	@NotBlank(message = "Title không được để trống")
	@Indexed
	private String title;

	@Min(value = 1888, message = "Year phải từ 1888 trở lên")
	private Integer year;

	/** Danh sách thể loại — ví dụ về field kiểu mảng trong document. */
	private List<String> genre;

	@Size(max = 120, message = "Director tối đa 120 ký tự")
	private String director;

	@Min(value = 0, message = "Rating tối thiểu là 0")
	private Double rating;

}
