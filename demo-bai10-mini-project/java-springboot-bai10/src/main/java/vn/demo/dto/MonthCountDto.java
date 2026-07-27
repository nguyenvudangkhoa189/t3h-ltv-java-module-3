package vn.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** DTO — kết quả D1 (tháng phát hành phim → số Movie). */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MonthCountDto {
	private String month;
	private long count;
}
