package vn.demo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

import vn.demo.dto.ItemSummaryDto;
import vn.demo.model.ItemModel;

/** Unit test — {@link ItemSummaryDto} projection. */
class ItemSummaryDtoTest {

	@Test
	void fromEntity_mapsProjectedFields() {
		ItemModel item = new ItemModel("id1", "30075445", "Cheeseburger", "Classic beef burger", 8.99, "Main");
		ItemSummaryDto dto = ItemSummaryDto.fromEntity(item);

		assertEquals("id1", dto.getId());
		assertEquals("30075445", dto.getRestaurantId());
		assertEquals("Cheeseburger", dto.getName());
		assertEquals(8.99, dto.getPrice());
		assertEquals("Main", dto.getCategory());
	}

	@Test
	void fromEntity_nullReturnsNull() {
		assertNull(ItemSummaryDto.fromEntity(null));
	}

}
