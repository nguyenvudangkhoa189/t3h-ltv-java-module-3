package vn.demo.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
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
import vn.demo.dto.MovieFormDto;
import vn.demo.model.MovieModel;
import vn.demo.service.AdminMovieService;

/**
 * CONTROLLER — Admin CRUD phim ({@code /admin/movies/**}).
 *
 * <p><b>Flow:</b> List → New/Edit form → POST validate → Service → {@code redirect:} (PRG)
 * + flash. Delete chỉ nhận POST + confirm ở view.</p>
 */
@Controller
@RequiredArgsConstructor
@RequestMapping("/admin/movies")
public class AdminMovieController {

	private final AdminMovieService adminMovieService;

	@Value("${app.admin.page-size:10}")
	private int pageSize;

	/** Danh sách phân trang. */
	@GetMapping
	public String list(@RequestParam(defaultValue = "0") int page, Model model) {
		Page<MovieModel> result = adminMovieService.findPage(page, pageSize);
		model.addAttribute("movies", result.getContent());
		model.addAttribute("currentPage", result.getNumber());
		model.addAttribute("totalPages", result.getTotalPages());
		return "admin/movies-list";
	}

	/** Form thêm (trống, type mặc định Movie). */
	@GetMapping("/new")
	public String createForm(Model model) {
		MovieFormDto form = new MovieFormDto();
		form.setType("Movie");
		model.addAttribute("movieForm", form);
		model.addAttribute("formMode", "create");
		return "admin/movie-form";
	}

	/**
	 * POST tạo mới.
	 *
	 * <p>Flow: bind + validate → lỗi thì trả lại form; OK → create + redirect list.</p>
	 */
	@PostMapping
	public String create(@Valid @ModelAttribute("movieForm") MovieFormDto form,
			BindingResult bindingResult,
			Model model,
			RedirectAttributes redirectAttributes) {
		if (bindingResult.hasErrors()) {
			model.addAttribute("formMode", "create");
			return "admin/movie-form";
		}
		adminMovieService.create(form);
		redirectAttributes.addFlashAttribute("successMessage", "Đã thêm phim.");
		return "redirect:/admin/movies";
	}

	/** Form sửa — prefill theo id. */
	@GetMapping("/{id}/edit")
	public String editForm(@PathVariable String id, Model model) {
		model.addAttribute("movieForm", adminMovieService.getFormById(id));
		model.addAttribute("formMode", "edit");
		return "admin/movie-form";
	}

	/**
	 * POST cập nhật.
	 *
	 * <p>Flow: validate → lỗi giữ form edit; OK → update + PRG list.</p>
	 */
	@PostMapping("/{id}/edit")
	public String update(@PathVariable String id,
			@Valid @ModelAttribute("movieForm") MovieFormDto form,
			BindingResult bindingResult,
			Model model,
			RedirectAttributes redirectAttributes) {
		if (bindingResult.hasErrors()) {
			form.setId(id);
			model.addAttribute("formMode", "edit");
			return "admin/movie-form";
		}
		adminMovieService.update(id, form);
		redirectAttributes.addFlashAttribute("successMessage", "Đã cập nhật phim.");
		return "redirect:/admin/movies";
	}

	/**
	 * POST xóa (không dùng GET).
	 *
	 * <p>Flow: Service xóa movie + comments → PRG list + flash.</p>
	 */
	@PostMapping("/{id}/delete")
	public String delete(@PathVariable String id, RedirectAttributes redirectAttributes) {
		adminMovieService.deleteById(id);
		redirectAttributes.addFlashAttribute("successMessage", "Đã xóa phim.");
		return "redirect:/admin/movies";
	}

}
