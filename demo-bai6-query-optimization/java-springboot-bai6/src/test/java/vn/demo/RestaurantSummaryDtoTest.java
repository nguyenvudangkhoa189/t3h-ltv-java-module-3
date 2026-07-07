package vn.demo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

import vn.demo.dto.RestaurantSummaryDto;

/** Unit test nhẹ — DTO projection không chứa field thừa. */
class RestaurantSummaryDtoTest {

	@Test
	void summaryDtoHoldsOnlyProjectedFields() {
		RestaurantSummaryDto dto = new RestaurantSummaryDto(
				"id1", "30075445", "Morris Park Bake Shop", "Bronx");

		assertEquals("id1", dto.getId());
		assertEquals("30075445", dto.getRestaurantId());
		assertEquals("Morris Park Bake Shop", dto.getName());
		assertEquals("Bronx", dto.getBorough());
	}

	@Test
	void summaryDtoAllowsNullCuisineStyleFields() {
		RestaurantSummaryDto dto = new RestaurantSummaryDto();
		assertNull(dto.getName());
	}

}
