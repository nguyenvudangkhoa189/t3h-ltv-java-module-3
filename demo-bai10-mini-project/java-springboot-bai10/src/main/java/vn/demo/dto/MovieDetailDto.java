package vn.demo.dto;

import java.util.ArrayList;
import java.util.List;

import lombok.Builder;
import lombok.Data;
import vn.demo.model.MovieModel;

/**
 * DTO trang chi tiết / watching — map từ Netflix CSV + ảnh template gốc.
 */
@Data
@Builder
public class MovieDetailDto {

	private String id;
	private String title;
	private String subtitle;
	private String description;
	private String type;
	private String director;
	private String cast;
	private String country;
	private String dateAdded;
	private Integer releaseYear;
	private String rating;
	private String duration;
	private String listedIn;
	private String posterUrl;
	private String genreLabel;
	/** Số tập giả lập cho trang watching (Netflix CSV không có episode). */
	private List<String> episodes;

	public static MovieDetailDto fromEntity(MovieModel m) {
		// --- Poster + genre + subtitle hiển thị trên anime-details ---
		int posterIndex = Math.abs((m.getId() != null ? m.getId() : "x").hashCode() % 6) + 1;
		String genre = m.getListedIn();
		if (genre != null && genre.contains(",")) {
			genre = genre.substring(0, genre.indexOf(',')).trim();
		}
		String subtitle = m.getCountry() != null ? m.getCountry() : (m.getType() != null ? m.getType() : "");
		if (m.getReleaseYear() != null) {
			subtitle = subtitle + " · " + m.getReleaseYear();
		}

		return MovieDetailDto.builder()
				.id(m.getId())
				.title(m.getTitle())
				.subtitle(subtitle)
				.description(m.getDescription())
				.type(m.getType())
				.director(m.getDirector() != null ? m.getDirector() : "N/A")
				.cast(m.getCast() != null ? m.getCast() : "N/A")
				.country(m.getCountry() != null ? m.getCountry() : "N/A")
				.dateAdded(m.getDateAdded() != null ? m.getDateAdded() : "N/A")
				.releaseYear(m.getReleaseYear())
				.rating(m.getRating() != null ? m.getRating() : "N/A")
				.duration(m.getDuration() != null ? m.getDuration() : "N/A")
				.listedIn(m.getListedIn() != null ? m.getListedIn() : "N/A")
				.posterUrl("/img/popular/popular-" + posterIndex + ".jpg")
				.genreLabel(genre != null ? genre : "Movie")
				.episodes(buildEpisodes(m))
				.build();
	}

	/** TV Show → 12 tập; Movie → 1 tập. Video dùng file mẫu của template. */
	private static List<String> buildEpisodes(MovieModel m) {
		int count = 1;
		if (m.getType() != null && m.getType().toLowerCase().contains("tv")) {
			count = 12;
		} else if (m.getDuration() != null && m.getDuration().toLowerCase().contains("season")) {
			count = 12;
		}
		List<String> eps = new ArrayList<>(count);
		for (int i = 1; i <= count; i++) {
			eps.add(String.format("Ep %02d", i));
		}
		return eps;
	}

}
