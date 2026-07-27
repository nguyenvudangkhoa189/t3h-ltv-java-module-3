package vn.demo.model;

import java.time.LocalDate;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import lombok.Data;

/**
 * MODEL — document collection {@code mymoviedb} (CSV Netflix).
 *
 * <p>Field gốc từ import CSV + field chuẩn hóa Bài 10 ({@code dateAddedAt},
 * {@code durationMinutes}) phục vụ Dashboard D1/D4/D5 và CRUD admin.</p>
 */
@Data
@Document(collection = "mymoviedb")
public class MovieModel {

	@Id
	private String id;

	@Field("show_id")
	private String showId;

	private String type;
	private String title;
	private String director;
	private String cast;
	private String country;

	@Field("date_added")
	private String dateAdded;

	@Field("release_year")
	private Integer releaseYear;

	/** Phân loại độ tuổi (TV-MA…) — không phải điểm vote. */
	private String rating;
	private String duration;

	@Field("listed_in")
	private String listedIn;

	private String description;

	/** Ngày Netflix thêm title (parse từ {@code date_added}). */
	private LocalDate dateAddedAt;

	/** Số phút Movie (parse từ {@code duration}); TV Show thường null. */
	private Integer durationMinutes;

}
