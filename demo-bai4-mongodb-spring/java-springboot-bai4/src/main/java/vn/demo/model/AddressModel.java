package vn.demo.model;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * Object địa chỉ, lồng bên trong {@link RestaurantModel}.
 *
 * <p>Không cần {@code @Document} vì đây không phải collection riêng — nó được
 * nhúng (embedded) trong document restaurant.</p>
 */
@Getter
@Setter
@ToString
@JsonIgnoreProperties(ignoreUnknown = true)
public class AddressModel {

	private String building;
	private List<Double> coord;  // [kinh độ, vĩ độ]
	private String street;
	private String zipcode;

}
