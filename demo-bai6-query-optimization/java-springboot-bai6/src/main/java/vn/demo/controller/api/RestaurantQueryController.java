package vn.demo.controller.api;

import java.util.List;
import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import vn.demo.dto.BulkUpdateResultDto;
import vn.demo.dto.CursorPageDto;
import vn.demo.dto.RestaurantSummaryDto;
import vn.demo.dto.RestaurantWithItemsDto;
import vn.demo.service.RestaurantQueryService;

/**
 * REST API minh họa tối ưu truy vấn restaurants (syllabus §4–§8, §10).
 */
@RestController
@RequestMapping("/api/restaurants")
@RequiredArgsConstructor
public class RestaurantQueryController {

	private final RestaurantQueryService restaurantQueryService;

	/** §4.2 / §10.1 — danh sách chỉ gồm name, borough, restaurant_id. */
	@GetMapping("/summary")
	public List<RestaurantSummaryDto> summary() {
		return restaurantQueryService.findSummaries();
	}

	/**
	 * §7 / §10.2 — cursor feed.
	 *
	 * <p>Trang đầu: không truyền {@code afterId}. Trang sau: truyền {@code lastSeenId} từ response trước.</p>
	 */
	@GetMapping("/feed")
	public CursorPageDto<RestaurantSummaryDto> feed(
			@RequestParam(required = false) String afterId,
			@RequestParam(required = false) Integer limit) {
		return restaurantQueryService.findFeed(afterId, limit);
	}

	/** §6 / §10.3 — tìm theo prefix (regex {@code ^prefix}). */
	@GetMapping("/search")
	public List<RestaurantSummaryDto> searchByPrefix(@RequestParam String prefix) {
		return restaurantQueryService.searchByPrefix(prefix);
	}

	/** §4.3 — full-text search ({@code $text}). */
	@GetMapping("/search/text")
	public List<RestaurantSummaryDto> searchByText(@RequestParam("q") String keyword) {
		return restaurantQueryService.searchByText(keyword);
	}

	/**
	 * §8 — nhà hàng kèm món.
	 *
	 * <ul>
	 *   <li>{@code mode=optimized} (mặc định) — {@code findByRestaurantIdIn}</li>
	 *   <li>{@code mode=n1} — loop {@code findByRestaurantId} (N+1, chỉ để so sánh)</li>
	 * </ul>
	 */
	@GetMapping("/with-items")
	public List<RestaurantWithItemsDto> withItems(
			@RequestParam(defaultValue = "optimized") String mode) {
		return restaurantQueryService.findAllWithItems(mode);
	}

	/** §5.2 — demo BulkOperations (update có filter rõ ràng). */
	@PostMapping("/bulk-demo")
	public BulkUpdateResultDto bulkDemo() {
		return restaurantQueryService.bulkDemoUpdate();
	}

	/** Endpoint gốc — liệt kê API có sẵn. */
	@GetMapping
	public Map<String, String> index() {
		return Map.of(
				"summary", "GET /api/restaurants/summary",
				"feed", "GET /api/restaurants/feed?limit=5&afterId=",
				"searchPrefix", "GET /api/restaurants/search?prefix=Mor",
				"searchText", "GET /api/restaurants/search/text?q=chinese",
				"withItems", "GET /api/restaurants/with-items?mode=optimized",
				"bulkDemo", "POST /api/restaurants/bulk-demo");
	}

}
