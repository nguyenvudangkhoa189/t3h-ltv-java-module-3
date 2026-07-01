package vn.demo.controller.api;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import vn.demo.model.ItemModel;
import vn.demo.service.ItemService;

/**
 * REST API phụ — thao tác trên collection items (§7 bài tập mở rộng).
 */
@RestController
@RequestMapping("/api/items")
@RequiredArgsConstructor
public class ItemRestController {

	private final ItemService itemService;

	/**
	 * GET /api/items/by-category?restaurantId=30075445&category=Main
	 *
	 * <p>Derived query — không cần $lookup vì chỉ truy vấn 1 collection items.</p>
	 */
	@GetMapping("/by-category")
	public ResponseEntity<List<ItemModel>> byCategory(
			@RequestParam String restaurantId,
			@RequestParam String category) {
		return ResponseEntity.ok(itemService.findByRestaurantIdAndCategory(restaurantId, category));
	}

}
