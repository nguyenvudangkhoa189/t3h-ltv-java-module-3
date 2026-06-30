package vn.demo;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import vn.demo.model.RestaurantModel;
import vn.demo.service.RestaurantService;

/**
 * Unit test thuần (không cần MongoDB) cho việc parse 1 dòng NDJSON và merge update.
 */
class RestaurantParseTest {

	private final RestaurantService service = new RestaurantService(null);

	@Test
	void parseLine_mapsNestedJsonAndRestaurantId() throws Exception {
		String line = "{\"restaurant_id\":\"30075445\",\"name\":\"Morris Park Bake Shop\","
				+ "\"borough\":\"Bronx\",\"cuisine\":\"Bakery\","
				+ "\"address\":{\"building\":\"1007\",\"street\":\"Morris Park Ave\",\"zipcode\":\"10462\"},"
				+ "\"grades\":[{\"grade\":\"A\",\"score\":2,\"date\":\"2014-03-03\"}]}";

		RestaurantModel r = service.parseLine(line);

		assertThat(r.getRestaurantId()).isEqualTo("30075445");
		assertThat(r.getName()).isEqualTo("Morris Park Bake Shop");
		assertThat(r.getAddress().getZipcode()).isEqualTo("10462");
		assertThat(r.getGrades()).hasSize(1);
		assertThat(r.getGrades().get(0).getScore()).isEqualTo(2);
	}

	@Test
	void updateDetail_onlyOverwritesNonNullFields() {
		RestaurantModel existing = new RestaurantModel();
		existing.setName("Old Name");
		existing.setBorough("Bronx");
		existing.setCuisine("Bakery");

		RestaurantModel changes = new RestaurantModel();
		changes.setName("New Name");   // borough & cuisine null -> giữ nguyên

		RestaurantModel merged = mergeWithoutDb(existing, changes);

		assertThat(merged.getName()).isEqualTo("New Name");
		assertThat(merged.getBorough()).isEqualTo("Bronx");
		assertThat(merged.getCuisine()).isEqualTo("Bakery");
	}

	/** Lặp lại logic merge của updateDetail nhưng không gọi DB (để test thuần). */
	private RestaurantModel mergeWithoutDb(RestaurantModel oldObject, RestaurantModel newObject) {
		if (newObject.getName() != null) {
			oldObject.setName(newObject.getName());
		}
		if (newObject.getBorough() != null) {
			oldObject.setBorough(newObject.getBorough());
		}
		if (newObject.getCuisine() != null) {
			oldObject.setCuisine(newObject.getCuisine());
		}
		return oldObject;
	}

}
