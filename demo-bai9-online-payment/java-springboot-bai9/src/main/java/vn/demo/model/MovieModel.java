package vn.demo.model;

import java.math.BigDecimal;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "movies")
public class MovieModel {

	@Id
	private String id;

	private String title;
	private Integer releaseYear;
	private String genre;
	private String description;
	private BigDecimal rentalPrice;
}
