package vn.demo.config;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import vn.demo.model.ItemModel;
import vn.demo.model.RestaurantModel;
import vn.demo.repository.ItemRepository;
import vn.demo.repository.RestaurantRepository;

/**
 * Nạp dữ liệu mẫu quan hệ 1-n khi cả 2 collection đều rỗng (§6.1 syllabus).
 *
 * <p>{@link CommandLineRunner} chạy 1 lần sau khi app khởi động. Không xóa dữ liệu cũ.</p>
 * <p>Dữ liệu khớp script {@code 01-create-sample-data.mongodb}.</p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

	private final RestaurantRepository restaurantRepository;
	private final ItemRepository itemRepository;

	@Value("${app.restaurants.seed-on-startup:true}")
	private boolean seedOnStartup;

	@Override
	public void run(String... args) {
		// Chỉ seed khi CẢ HAI collection đều rỗng — tránh ghi đè dữ liệu học viên đã có
		if (!seedOnStartup || restaurantRepository.count() > 0 || itemRepository.count() > 0) {
			return;
		}

		RestaurantModel r1 = new RestaurantModel(null, "30075445", "Morris Park Bake Shop", "Bronx", "Bakery");
		RestaurantModel r2 = new RestaurantModel(null, "40356018", "Wild East", "Brooklyn", "Chinese");
		RestaurantModel r3 = new RestaurantModel(null, "40363171", "Carnegie Diner", "Manhattan", "American");
		restaurantRepository.saveAll(List.of(r1, r2, r3));

		// Mỗi item có restaurant_id trỏ về nhà hàng cha — minh họa quan hệ 1-n
		List<ItemModel> items = List.of(
				new ItemModel(null, "30075445", "Cheeseburger", "Classic beef burger", 8.99, "Main"),
				new ItemModel(null, "30075445", "Fries", "Crispy fries", 3.49, "Side"),
				new ItemModel(null, "30075445", "Chocolate Cake", "House dessert", 5.99, "Dessert"),
				new ItemModel(null, "40356018", "Kung Pao Chicken", "Spicy stir-fry", 12.50, "Main"),
				new ItemModel(null, "40356018", "Spring Rolls", "Vegetable rolls", 4.50, "Appetizer"),
				new ItemModel(null, "40363171", "Club Sandwich", "Triple-decker", 11.00, "Main"),
				new ItemModel(null, "40363171", "Caesar Salad", "Fresh greens", 9.50, "Side"));
		itemRepository.saveAll(items);

		log.info("Đã nạp {} nhà hàng và {} món ăn mẫu", 3, items.size());
	}

}
