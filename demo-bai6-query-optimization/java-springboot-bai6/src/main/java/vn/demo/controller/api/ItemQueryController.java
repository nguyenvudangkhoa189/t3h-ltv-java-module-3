package vn.demo.controller.api;

import java.util.List;
import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import vn.demo.dto.ItemSummaryDto;
import vn.demo.dto.ItemWithRestaurantDto;
import vn.demo.service.ItemQueryService;

/**
 * REST API minh họa tối ưu trên items (syllabus §4.4 tóm tắt — chi tiết Bài 5 §9, §9 aggregation).
 */
@RestController
@RequestMapping("/api/items")
@RequiredArgsConstructor
public class ItemQueryController {

	private final ItemQueryService itemQueryService;

	/**
	 * Tái sử dụng {@code updateMulti} từ Bài 5 §9 — đổi {@code restaurant_id} hàng loạt.
	 *
	 * <p>Ví dụ: {@code PUT /api/items/reassign?oldId=30075445&newId=777888}</p>
	 */
	@PutMapping("/reassign")
	public Map<String, Object> reassign(
			@RequestParam String oldId,
			@RequestParam String newId) {
		long modified = itemQueryService.reassignRestaurantId(oldId, newId);
		return Map.of(
				"oldId", oldId,
				"newId", newId,
				"modifiedCount", modified);
	}

	/**
	 * §9 — items kèm tên nhà hàng (aggregation tối ưu).
	 *
	 * <p>Ví dụ: {@code GET /api/items/with-restaurant?category=Main}</p>
	 */
	@GetMapping("/with-restaurant")
	public List<ItemWithRestaurantDto> withRestaurant(
			@RequestParam(required = false) String category) {
		return itemQueryService.findItemsWithRestaurant(category);
	}

	/** GET /api/items?restaurantId= — danh sách món projection (§10.5, sau reassign). */
	@GetMapping
	public List<ItemSummaryDto> byRestaurantId(@RequestParam String restaurantId) {
		return itemQueryService.findSummariesByRestaurantId(restaurantId);
	}

}
