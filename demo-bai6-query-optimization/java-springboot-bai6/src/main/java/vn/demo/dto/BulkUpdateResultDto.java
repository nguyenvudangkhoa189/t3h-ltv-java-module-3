package vn.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Kết quả {@code BulkOperations.execute()} (syllabus §5.2). */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BulkUpdateResultDto {

	private long modifiedCount;
	private long matchedCount;
	private String message;

}
