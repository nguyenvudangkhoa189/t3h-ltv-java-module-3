package vn.demo.util;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * UTIL — parse field chuỗi Netflix CSV sang kiểu dùng cho aggregation / CRUD.
 *
 * <p>Dùng bởi {@code MovieNormalizeRunner} và {@code AdminMovieService} khi lưu.</p>
 */
public final class MovieDataNormalizer {

	private static final DateTimeFormatter DATE_ADDED =
			DateTimeFormatter.ofPattern("MMMM d, yyyy", Locale.ENGLISH);

	private static final Pattern MINUTES = Pattern.compile("(\\d+)\\s*min", Pattern.CASE_INSENSITIVE);

	private MovieDataNormalizer() {
	}

	/**
	 * Parse {@code "August 14, 2020"} → {@link LocalDate}.
	 *
	 * @return null nếu rỗng / sai format
	 */
	public static LocalDate parseDateAdded(String dateAdded) {
		if (dateAdded == null || dateAdded.isBlank()) {
			return null;
		}
		try {
			return LocalDate.parse(dateAdded.trim(), DATE_ADDED);
		} catch (DateTimeParseException ex) {
			return null;
		}
	}

	/**
	 * Parse {@code "93 min"} → số phút; {@code "4 Seasons"} → null.
	 */
	public static Integer parseDurationMinutes(String duration) {
		if (duration == null || duration.isBlank()) {
			return null;
		}
		Matcher m = MINUTES.matcher(duration.trim());
		if (m.find()) {
			return Integer.parseInt(m.group(1));
		}
		return null;
	}

}
