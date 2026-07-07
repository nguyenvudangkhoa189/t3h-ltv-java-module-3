package vn.demo.config;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import vn.demo.model.ItemModel;
import vn.demo.model.RestaurantModel;
import vn.demo.repository.ItemRepository;
import vn.demo.repository.RestaurantRepository;

/**
 * Nạp dữ liệu mẫu khi cả 2 collection đều rỗng.
 *
 * <p>Seed nhiều nhà hàng hơn Bài 5 để demo cursor feed và tìm kiếm rõ hơn.</p>
 * <p>{@code @Order(2)} chạy sau {@link IndexConfig} (text index).</p>
 */
@Slf4j
@Component
@Order(2)
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

	private final RestaurantRepository restaurantRepository;
	private final ItemRepository itemRepository;

	@Value("${app.restaurants.seed-on-startup:true}")
	private boolean seedOnStartup;

	@Override
	public void run(String... args) {
		if (!seedOnStartup || restaurantRepository.count() > 0 || itemRepository.count() > 0) {
			return;
		}

		List<RestaurantModel> restaurants = List.of(
				new RestaurantModel(null, "30075445", "Morris Park Bake Shop", "Bronx", "Bakery"),
				new RestaurantModel(null, "40356018", "Wild East", "Brooklyn", "Chinese"),
				new RestaurantModel(null, "40363171", "Carnegie Diner", "Manhattan", "American"),
				new RestaurantModel(null, "40364220", "Morning Star Cafe", "Queens", "American"),
				new RestaurantModel(null, "40364350", "Shop House Kitchen", "Brooklyn", "Thai"),
				new RestaurantModel(null, "40365001", "Asia Garden", "Manhattan", "Asian"),
				new RestaurantModel(null, "40365002", "Brooklyn Pizza Shop", "Brooklyn", "Pizza"),
				new RestaurantModel(null, "40365003", "Cafe Mocha", "Bronx", "Coffee"),
				new RestaurantModel(null, "40365004", "Dragon Palace", "Queens", "Chinese"),
				new RestaurantModel(null, "40365005", "Empire Burger", "Manhattan", "American"),
				new RestaurantModel(null, "40365006", "Fresh Salad Bar", "Brooklyn", "Healthy"),
				new RestaurantModel(null, "40365007", "Golden Bakery", "Bronx", "Bakery"));
		restaurantRepository.saveAll(restaurants);

		List<ItemModel> items = new ArrayList<>();
		items.add(new ItemModel(null, "30075445", "Cheeseburger", "Classic beef burger", 8.99, "Main"));
		items.add(new ItemModel(null, "30075445", "Fries", "Crispy fries", 3.49, "Side"));
		items.add(new ItemModel(null, "30075445", "Chocolate Cake", "House dessert", 5.99, "Dessert"));
		items.add(new ItemModel(null, "40356018", "Kung Pao Chicken", "Spicy stir-fry", 12.50, "Main"));
		items.add(new ItemModel(null, "40356018", "Spring Rolls", "Vegetable rolls", 4.50, "Appetizer"));
		items.add(new ItemModel(null, "40363171", "Club Sandwich", "Triple-decker", 11.00, "Main"));
		items.add(new ItemModel(null, "40363171", "Caesar Salad", "Fresh greens", 9.50, "Side"));
		items.add(new ItemModel(null, "40364220", "Pancakes", "Maple syrup", 7.50, "Main"));
		items.add(new ItemModel(null, "40364350", "Pad Thai", "Classic noodles", 10.00, "Main"));
		items.add(new ItemModel(null, "40365001", "Dumplings", "Steamed", 6.50, "Appetizer"));
		items.add(new ItemModel(null, "40365002", "Margherita", "Tomato basil", 14.00, "Main"));
		items.add(new ItemModel(null, "40365003", "Latte", "Espresso milk", 4.50, "Drink"));
		items.add(new ItemModel(null, "40365004", "Mapo Tofu", "Spicy tofu", 11.50, "Main"));
		items.add(new ItemModel(null, "40365005", "Double Burger", "Two patties", 13.00, "Main"));
		items.add(new ItemModel(null, "40365006", "Greek Salad", "Feta olives", 8.00, "Side"));
		items.add(new ItemModel(null, "40365007", "Croissant", "Butter pastry", 3.50, "Dessert"));
		itemRepository.saveAll(items);

		log.info("Đã nạp {} nhà hàng và {} món ăn mẫu (Bài 6)", restaurants.size(), items.size());
	}

}
