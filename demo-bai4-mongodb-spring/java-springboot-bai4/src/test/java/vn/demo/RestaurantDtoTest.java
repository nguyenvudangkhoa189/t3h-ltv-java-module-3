package vn.demo;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import vn.demo.dto.RestaurantDto;
import vn.demo.dto.RestaurantFormDto;
import vn.demo.model.RestaurantModel;
import vn.demo.service.RestaurantService;

class RestaurantDtoTest {

	private final RestaurantService service = new RestaurantService(null);

	@Test
	void parseLine_mapsNestedJsonAndRestaurantId() throws Exception {
		String line = "{\"restaurant_id\":\"30075445\",\"name\":\"Morris Park Bake Shop\","
				+ "\"borough\":\"Bronx\",\"cuisine\":\"Bakery\","
				+ "\"address\":{\"building\":\"1007\",\"street\":\"Morris Park Ave\",\"zipcode\":\"10462\"},"
				+ "\"grades\":[{\"grade\":\"A\",\"score\":2,\"date\":\"2014-03-03\"}]}";

		RestaurantModel restaurant = service.parseLine(line);

		RestaurantDto dto = RestaurantDto.fromEntity(restaurant);

		assertThat(dto.getRestaurantId()).isEqualTo("30075445");
		assertThat(dto.getName()).isEqualTo("Morris Park Bake Shop");
		assertThat(dto.getBorough()).isEqualTo("Bronx");
	}

	@Test
	void formDto_toEntity_onlyMapsEditableFields() {
		RestaurantFormDto form = new RestaurantFormDto();
		form.setRestaurantId("30075445");
		form.setName("New Name");
		form.setBorough("Bronx");
		form.setCuisine("Bakery");

		RestaurantModel restaurant = form.toEntity();

		assertThat(restaurant.getRestaurantId()).isEqualTo("30075445");
		assertThat(restaurant.getName()).isEqualTo("New Name");
		assertThat(restaurant.getBorough()).isEqualTo("Bronx");
	}

}
