package vn.demo.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import lombok.Data;

/**
 * MODEL — ánh xạ document collection {@code mymoviedb} (CSV Netflix sau mongoimport).
 *
 * <p>Header CSV: show_id, type, title, director, cast, country, date_added,
 * release_year, rating, duration, listed_in, description</p>
 *
 * <p>Quy ước đặt tên: class trong package {@code model} kết thúc bằng suffix {@code Model}.</p>
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

	private String rating;
	private String duration;

	@Field("listed_in")
	private String listedIn;

	private String description;

}
