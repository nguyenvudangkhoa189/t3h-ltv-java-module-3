package vn.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** DTO — kết quả D3 (quốc gia → số title). */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CountryCountDto {
	private String country;
	private long count;
}
