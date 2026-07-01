package vn.demo.controller.web;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import vn.demo.dto.ItemFormDto;
import vn.demo.model.ItemModel;
import vn.demo.service.ItemService;

/**
 * CONTROLLER (web) — thao tác CRUD trên món ăn (collection con).
 *
 * <p>Tách riêng khỏi {@link RestaurantViewController} vì URL gốc là {@code /items}
 * (khác {@code /restaurants}) — đúng nghiệp vụ RESTful.</p>
 */
@Controller
@RequestMapping("/items")
@RequiredArgsConstructor
public class ItemViewController {

	private final ItemService itemService;

	/** GET /items/{itemId}/edit — mở form sửa, đổ sẵn dữ liệu hiện tại. */
	@GetMapping("/{itemId}/edit")
	public String editForm(@PathVariable String itemId, Model model) {
		ItemModel item = itemService.getById(itemId);
		model.addAttribute("itemForm", ItemFormDto.fromEntity(item));
		model.addAttribute("isEdit", true);
		model.addAttribute("restaurantId", item.getRestaurantId());
		model.addAttribute("actionUrl", "/items/" + itemId);
		return "items/form";
	}

	/** POST /items/{itemId} — cập nhật món, redirect về trang nhà hàng cha (PRG). */
	@PostMapping("/{itemId}")
	public String update(
			@PathVariable String itemId,
			@Valid @ModelAttribute("itemForm") ItemFormDto form,
			BindingResult bindingResult,
			Model model,
			RedirectAttributes ra) {
		ItemModel existing = itemService.getById(itemId);
		if (bindingResult.hasErrors()) {
			model.addAttribute("isEdit", true);
			model.addAttribute("restaurantId", existing.getRestaurantId());
			model.addAttribute("actionUrl", "/items/" + itemId);
			return "items/form";
		}
		itemService.update(itemId, form.toEntity());
		ra.addFlashAttribute("message", "Cập nhật món thành công!");
		return "redirect:/restaurants/" + existing.getRestaurantId();
	}

	/** POST /items/{itemId}/delete — xóa món, quay về trang chi tiết nhà hàng. */
	@PostMapping("/{itemId}/delete")
	public String delete(@PathVariable String itemId, RedirectAttributes ra) {
		ItemModel item = itemService.getById(itemId);
		String restaurantId = item.getRestaurantId();
		itemService.delete(itemId);
		ra.addFlashAttribute("message", "Đã xóa món.");
		return "redirect:/restaurants/" + restaurantId;
	}

}
