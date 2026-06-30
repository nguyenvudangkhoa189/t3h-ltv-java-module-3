package vn.demo.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import vn.demo.dto.ImportResultDto;
import vn.demo.model.RestaurantModel;
import vn.demo.service.RestaurantService;

/**
 * CONTROLLER (web) — giao diện Thymeleaf cho Restaurant.
 *
 * <p>Nhiệm vụ: nhận HTTP request, gọi {@link RestaurantService}, đẩy dữ liệu vào
 * {@link Model} rồi trả về tên template.</p>
 *
 * <p>Toàn bộ dùng {@code @Controller} (KHÔNG {@code @RestController}): mỗi handler
 * trả về <b>tên template</b> hoặc chỉ thị {@code redirect:}. Bài 4 chỉ có giao diện
 * web nên không tách package api/web như Bài 3.</p>
 */
@Slf4j
@Controller
@RequiredArgsConstructor
public class RestaurantViewController {

	/** Số chỉ số trang tối đa hiển thị trên thanh phân trang. */
	private static final int MAX_PAGES_TO_SHOW = 5;

	private final RestaurantService restaurantService;

	// Số dòng mỗi trang, đọc từ application.properties (mặc định 10)
	@Value("${app.restaurants.page-size:10}")
	private int pageSize;

	/** GET /restaurants/upload — hiển thị trang upload. */
	@GetMapping("/restaurants/upload")
	public String showUploadPage() {
		return "restaurants/upload";
	}

	/**
	 * POST /restaurants/upload — nhận file, import, rồi hiển thị lại trang upload kèm kết quả.
	 *
	 * <p><b>Lưu ý:</b> trả về TÊN TEMPLATE ("restaurants/upload"), KHÔNG trả về chuỗi
	 * thông báo — thông báo được đưa vào Model. Nếu trả chuỗi, Spring sẽ hiểu nhầm đó
	 * là tên view và báo lỗi.</p>
	 */
	@PostMapping("/restaurants/upload")
	public String handleUpload(@RequestParam("file") MultipartFile file, Model model) {
		ImportResultDto result = restaurantService.handleImport(file);
		model.addAttribute("message", result.message());
		if (result.success()) {
			model.addAttribute("importCount", result.importCount());
		}
		return "restaurants/upload";
	}

	/**
	 * GET /restaurants — danh sách có phân trang và sắp xếp.
	 *
	 * @param page   chỉ số trang, bắt đầu từ 0 (chuẩn Spring Data)
	 * @param sortBy field để sắp xếp (mặc định "name")
	 * @param dir    hướng sắp xếp: asc/desc
	 */
	@GetMapping("/restaurants")
	public String listRestaurants(
			@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "name") String sortBy,
			@RequestParam(defaultValue = "asc") String dir,
			Model model) {

		// Tạo điều kiện sắp xếp theo hướng người dùng chọn
		Sort sort = dir.equalsIgnoreCase("desc")
				? Sort.by(sortBy).descending()
				: Sort.by(sortBy).ascending();

		// PageRequest.of(page, size, sort): trang đánh số từ 0
		Page<RestaurantModel> restaurantPage = restaurantService.findAllPagination(
				PageRequest.of(Math.max(page, 0), pageSize, sort));

		int totalPages = restaurantPage.getTotalPages();
		int currentPage = restaurantPage.getNumber();

		// Tính cửa sổ chỉ số trang hiển thị (quanh trang hiện tại) để thanh phân trang gọn
		int startPage = Math.max(0, currentPage - MAX_PAGES_TO_SHOW / 2);
		int endPage = Math.min(totalPages - 1, startPage + MAX_PAGES_TO_SHOW - 1);
		if ((endPage - startPage) < (MAX_PAGES_TO_SHOW - 1)) {
			startPage = Math.max(0, endPage - (MAX_PAGES_TO_SHOW - 1));
		}

		model.addAttribute("list", restaurantPage.getContent());
		model.addAttribute("currentPage", currentPage);
		model.addAttribute("totalPages", totalPages);
		model.addAttribute("startPage", startPage);
		model.addAttribute("endPage", endPage);
		model.addAttribute("sortBy", sortBy);
		model.addAttribute("dir", dir);
		return "restaurants/list";
	}

	/** GET /restaurants/detail/{id} — trang chi tiết (tìm theo restaurant_id). */
	@GetMapping("/restaurants/detail/{id}")
	public String showRestaurantDetail(@PathVariable String id, Model model) {
		RestaurantModel restaurant = restaurantService.findByRestaurantId(id);
		if (restaurant == null) {
			return "restaurants/not-found";
		}
		model.addAttribute("restaurant", restaurant);
		return "restaurants/detail";
	}

	/** POST /restaurants/detail/{id} — nhận dữ liệu cập nhật rồi quay lại trang chi tiết (PRG). */
	@PostMapping("/restaurants/detail/{id}")
	public String updateRestaurant(
			@PathVariable String id,
			@ModelAttribute RestaurantModel updatedRestaurant,
			RedirectAttributes redirectAttributes) {
		RestaurantModel oldRestaurant = restaurantService.findByRestaurantId(id); // tìm bản ghi hiện tại
		if (oldRestaurant == null) {
			redirectAttributes.addFlashAttribute("message", "Restaurant not found");
			return "redirect:/restaurants";
		}
		restaurantService.updateDetail(oldRestaurant, updatedRestaurant);
		redirectAttributes.addFlashAttribute("message", "Update data successfully");
		return "redirect:/restaurants/detail/" + id; // PRG: vẫn ở lại trang chi tiết
	}

}
