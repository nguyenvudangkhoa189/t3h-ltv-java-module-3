package vn.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO — một dòng bảng xếp hạng D4/D5/D6 trên dashboard.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MovieRankDto {
	private String id;
	private String title;
	private String type;
	private Integer releaseYear;
	private Integer durationMinutes;
	private String dateAdded;
	private String director;
}
