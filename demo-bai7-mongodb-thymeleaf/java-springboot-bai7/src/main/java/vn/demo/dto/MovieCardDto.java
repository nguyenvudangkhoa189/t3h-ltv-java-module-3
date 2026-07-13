package vn.demo.dto;

import lombok.Builder;
import lombok.Data;
import vn.demo.model.MovieModel;

@Data
@Builder
public class MovieCardDto {

	private String id;
	private String title;
	private String posterUrl;
	private Integer releaseYear;
	private String rating;
	private String duration;
	private String type;
	private String genreLabel;
	/** Mô tả ngắn — dùng cho Hero slider trên index. */
	private String description;

	public static MovieCardDto fromEntity(MovieModel m) {
		return fromEntity(m, "popular", "popular");
	}

	/**
	 * Map entity → DTO; ảnh lấy từ thư mục template gốc (trending/popular/recent/live/sidebar/hero).
	 *
	 * @param folder thư mục trong {@code /img/} (vd {@code trending})
	 * @param prefix tiền tố file (vd {@code trend} → trend-1.jpg … trend-6.jpg)
	 */
	public static MovieCardDto fromEntity(MovieModel m, String folder, String prefix) {
		// --- Chọn ảnh poster theo hash id (CSV Netflix không có URL poster) ---
		int index = posterIndex(m, maxIndexFor(folder, prefix));
		String genre = firstGenre(m.getListedIn());
		String desc = m.getDescription();
		if (desc != null && desc.length() > 120) {
			desc = desc.substring(0, 117) + "...";
		}
		return MovieCardDto.builder()
				.id(m.getId())
				.title(m.getTitle())
				.posterUrl("/img/" + folder + "/" + prefix + "-" + index + ".jpg")
				.releaseYear(m.getReleaseYear())
				.rating(m.getRating())
				.duration(m.getDuration())
				.type(m.getType())
				.genreLabel(genre)
				.description(desc)
				.build();
	}

	private static String firstGenre(String listedIn) {
		if (listedIn == null || listedIn.isBlank()) {
			return "Movie";
		}
		if (listedIn.contains(",")) {
			return listedIn.substring(0, listedIn.indexOf(',')).trim();
		}
		return listedIn.trim();
	}

	/** Số ảnh có sẵn trong template gốc theo thư mục/prefix. */
	private static int maxIndexFor(String folder, String prefix) {
		if ("comment".equals(prefix)) {
			return 4;
		}
		if ("tv".equals(prefix) || "sidebar".equals(folder)) {
			return 5;
		}
		if ("hero".equals(folder)) {
			return 1;
		}
		return 6;
	}

	private static int posterIndex(MovieModel m, int max) {
		String key = m.getId() != null ? m.getId() : m.getShowId();
		if (key == null) {
			return 1;
		}
		return Math.abs(key.hashCode() % max) + 1;
	}

}
