package vn.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** DTO — kết quả D2 (thể loại → số title). */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GenreCountDto {
	private String genre;
	private long count;
}
