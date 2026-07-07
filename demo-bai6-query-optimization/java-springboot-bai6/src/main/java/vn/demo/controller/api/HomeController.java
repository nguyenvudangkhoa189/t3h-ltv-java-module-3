package vn.demo.controller.api;

import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/** Trang gốc — liệt kê các API demo Bài 6. */
@RestController
public class HomeController {

	@GetMapping("/")
	public Map<String, Object> home() {
		Map<String, Object> body = new LinkedHashMap<>();
		body.put("demo", "Bài 6 — Tối ưu truy vấn MongoDB");
		body.put("apis", Map.of(
				"summary", "GET /api/restaurants/summary",
				"feed", "GET /api/restaurants/feed?limit=5",
				"searchPrefix", "GET /api/restaurants/search?prefix=Mor",
				"searchText", "GET /api/restaurants/search/text?q=chinese",
				"withItems", "GET /api/restaurants/with-items?mode=optimized",
				"bulkDemo", "POST /api/restaurants/bulk-demo",
				"reassign", "PUT /api/items/reassign?oldId=30075445&newId=777888",
				"itemsByRestaurant", "GET /api/items?restaurantId=30075445",
				"itemsWithRestaurant", "GET /api/items/with-restaurant?category=Main"));
		return body;
	}

}
