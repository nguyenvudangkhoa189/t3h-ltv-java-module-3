package vn.demo.controller.web;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import vn.demo.dto.ItemFormDto;
import vn.demo.dto.RestaurantWithItemsDto;
import vn.demo.model.RestaurantModel;
import vn.demo.service.ItemService;
import vn.demo.service.RestaurantService;

/**
 * PHẦN Thymeleaf — quản lý quan hệ restaurant ↔ items (§10 syllabus).
 *
 * <p>{@code @Controller}: mỗi handler trả <b>tên template</b> hoặc {@code redirect:}, không trả JSON.</p>
 * <p>{@code @RequestMapping("/restaurants")}: đường dẫn gốc chung; handler chỉ khai báo phần còn lại.</p>
 */
@Controller
@RequestMapping("/restaurants")
@RequiredArgsConstructor
public class RestaurantViewController {

	private static final int MAX_PAGES_TO_SHOW = 5;

	private final RestaurantService restaurantService;
	private final ItemService itemService;

	@Value("${app.restaurants.page-size:10}")
	private int pageSize;

	/** GET /restaurants — danh sách nhà hàng có phân trang + sắp xếp (tái dùng pattern Bài 4). */
	@GetMapping
	public String list(
			@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "name") String sortBy,
			@RequestParam(defaultValue = "asc") String dir,
			Model model) {
		Sort sort = dir.equalsIgnoreCase("desc")
				? Sort.by(sortBy).descending()
				: Sort.by(sortBy).ascending();
		// PageRequest.of: trang đánh số từ 0 (chuẩn Spring Data)
		Page<RestaurantModel> restaurantPage = restaurantService.findAll(
				PageRequest.of(Math.max(page, 0), pageSize, sort));

		int totalPages = restaurantPage.getTotalPages();
		int currentPage = restaurantPage.getNumber();
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

	/**
	 * GET /restaurants/{id} — chi tiết nhà hàng + bảng món ăn.
	 *
	 * <p>Dùng {@code getRestaurantWithItems} ($lookup) thay vì gọi 2 query riêng → minh họa join trong app.</p>
	 */
	@GetMapping("/{id}")
	public String detail(@PathVariable("id") String restaurantId, Model model) {
		List<RestaurantWithItemsDto> result = restaurantService.getRestaurantWithItems(restaurantId);
		if (result.isEmpty()) {
			return "restaurants/not-found";
		}
		RestaurantWithItemsDto restaurant = result.get(0);
		model.addAttribute("restaurant", restaurant);
		model.addAttribute("items", restaurant.getMenuItems());
		return "restaurants/detail";
	}

	/** GET /restaurants/{id}/items/new — form thêm món (itemForm rỗng). */
	@GetMapping("/{id}/items/new")
	public String newItemForm(@PathVariable("id") String restaurantId, Model model) {
		restaurantService.findByRestaurantId(restaurantId); // 404 nếu nhà hàng không tồn tại
		model.addAttribute("itemForm", new ItemFormDto());
		model.addAttribute("isEdit", false);
		model.addAttribute("restaurantId", restaurantId);
		model.addAttribute("actionUrl", "/restaurants/" + restaurantId + "/items");
		return "items/form";
	}

	/**
	 * POST /restaurants/{id}/items — lưu món mới.
	 *
	 * <p>Gắn {@code restaurantId} vào item → tạo liên kết 1-n về nhà hàng cha.</p>
	 * <p>Sau khi lưu: redirect (PRG) về trang chi tiết.</p>
	 */
	@PostMapping("/{id}/items")
	public String addItem(
			@PathVariable("id") String restaurantId,
			@Valid @ModelAttribute("itemForm") ItemFormDto form,
			BindingResult bindingResult,
			Model model,
			RedirectAttributes ra) {
		if (bindingResult.hasErrors()) {
			model.addAttribute("isEdit", false);
			model.addAttribute("restaurantId", restaurantId);
			model.addAttribute("actionUrl", "/restaurants/" + restaurantId + "/items");
			return "items/form";
		}
		var item = form.toEntity();
		item.setRestaurantId(restaurantId); // khóa liên kết — field quan trọng nhất của bài 5
		itemService.create(item);
		ra.addFlashAttribute("message", "Đã thêm món thành công!");
		return "redirect:/restaurants/" + restaurantId;
	}

	/**
	 * POST /restaurants/{id}/change-id — đổi restaurant_id (transaction §8 + updateMulti §9).
	 *
	 * <p>Cập nhật cả collection restaurants và items. Cần MongoDB Replica Set.</p>
	 */
	@PostMapping("/{id}/change-id")
	public String changeId(
			@PathVariable("id") String oldId,
			@RequestParam String newId,
			RedirectAttributes ra) {
		try {
			long count = restaurantService.updateRestaurantIdOptimized(oldId, newId);
			ra.addFlashAttribute("message", "Đổi ID thành công (cập nhật " + count + " món).");
			return "redirect:/restaurants/" + newId;
		} catch (Exception e) {
			ra.addFlashAttribute("error", "Lỗi: " + e.getMessage());
			return "redirect:/restaurants/" + oldId;
		}
	}

}
