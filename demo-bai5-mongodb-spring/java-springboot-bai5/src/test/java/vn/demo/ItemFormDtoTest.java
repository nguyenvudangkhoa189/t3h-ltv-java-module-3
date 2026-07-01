package vn.demo;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import vn.demo.dto.ItemFormDto;
import vn.demo.model.ItemModel;

class ItemFormDtoTest {

	@Test
	void toEntity_clearsIdWhenCreating() {
		ItemFormDto form = new ItemFormDto();
		form.setName("Pizza");
		form.setPrice(10.0);
		form.setCategory("Main");

		ItemModel entity = form.toEntity();
		assertEquals("Pizza", entity.getName());
		assertEquals(10.0, entity.getPrice());
		assertEquals("Main", entity.getCategory());
	}

	@Test
	void fromEntity_roundTrip() {
		ItemModel item = new ItemModel("abc", "30075445", "Fries", "Crispy", 3.49, "Side");
		ItemFormDto form = ItemFormDto.fromEntity(item);
		assertEquals("abc", form.getId());
		assertEquals("Fries", form.getName());
		assertEquals(3.49, form.getPrice());
	}

}
