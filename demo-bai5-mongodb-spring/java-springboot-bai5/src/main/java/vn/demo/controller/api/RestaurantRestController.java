package vn.demo.controller.api;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import vn.demo.dto.ItemWithRestaurantDto;
import vn.demo.dto.RestaurantWithItemsDto;
import vn.demo.service.RestaurantService;

/**
 * PHẦN REST API — truy vấn liên kết & transaction (§7, §8 syllabus).
 *
 * <p>{@code @RestController} = {@code @Controller} + {@code @ResponseBody}: trả JSON.</p>
 * <p>{@code @RequestMapping("/api/restaurants")} ở cấp class: gom đường dẫn gốc chung (§7.3).</p>
 */
@RestController
@RequestMapping("/api/restaurants")
@RequiredArgsConstructor
public class RestaurantRestController {

	private final RestaurantService restaurantService;

	/**
	 * GET /api/restaurants/{id}/with-items
	 *
	 * <p>Trả nhà hàng kèm mảng {@code menuItems} — kết quả $lookup §7.4.</p>
	 */
	@GetMapping("/{id}/with-items")
	public ResponseEntity<List<RestaurantWithItemsDto>> getRestaurantWithItems(
			@PathVariable("id") String restaurantId) {
		return ResponseEntity.ok(restaurantService.getRestaurantWithItems(restaurantId));
	}

	/**
	 * GET /api/restaurants/{id}/items-with-restaurant
	 *
	 * <p>Trả danh sách món, mỗi món có {@code restaurantInfo} — $lookup ngược §7.5.</p>
	 */
	@GetMapping("/{id}/items-with-restaurant")
	public ResponseEntity<List<ItemWithRestaurantDto>> getItemsWithRestaurantInfo(
			@PathVariable("id") String restaurantId) {
		return ResponseEntity.ok(restaurantService.getItemsWithRestaurantInfo(restaurantId));
	}

	/**
	 * PUT /api/restaurants/change-id?oldId=&newId=&mode=
	 *
	 * <p>Đổi {@code restaurant_id} trên cả 2 collection trong 1 transaction (§8).</p>
	 * <ul>
	 *   <li>{@code mode=optimized} (mặc định) — dùng updateMulti §9.4</li>
	 *   <li>{@code mode=loop} — lặp save từng item §8.5 (để so sánh hiệu năng)</li>
	 * </ul>
	 */
	@PutMapping("/change-id")
	public ResponseEntity<String> updateRestaurantId(
			@RequestParam String oldId,
			@RequestParam String newId,
			@RequestParam(defaultValue = "optimized") String mode) {
		try {
			if ("loop".equalsIgnoreCase(mode)) {
				restaurantService.updateRestaurantIdWithSave(oldId, newId);
			} else {
				long updated = restaurantService.updateRestaurantIdOptimized(oldId, newId);
				return ResponseEntity.ok("Updated restaurant + " + updated + " items");
			}
			return ResponseEntity.ok("Restaurant ID updated in both collections");
		} catch (Exception e) {
			return ResponseEntity.status(500).body("Error: " + e.getMessage());
		}
	}

}
