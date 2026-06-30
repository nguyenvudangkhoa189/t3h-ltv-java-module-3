package vn.demo.controller.web;

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
import lombok.extern.slf4j.Slf4j;
import vn.demo.dto.MovieFormDto;
import vn.demo.exception.ResourceNotFoundException;
import vn.demo.model.MovieModel;
import vn.demo.service.MovieService;

/**
 * PHẦN 2 — BÀI TẬP: làm lại các chức năng của REST API bằng giao diện Thymeleaf.
 *
 * <p>Đây là tầng Controller dành cho trang web. {@code @Controller}: mỗi handler
 * trả về <b>tên template</b> (vd {@code "movies/list"} -&gt; {@code templates/movies/list.html})
 * hoặc chỉ thị {@code redirect:}, KHÔNG trả JSON.</p>
 *
 * <p>Lưu ý: class này gọi cùng {@link MovieService} với {@code MovieRestController} —
 * chỉ khác tầng trình bày (View thay vì JSON), minh họa lợi ích tách tầng.</p>
 */
@Slf4j
@Controller
@RequestMapping("/movies")
@RequiredArgsConstructor
public class MovieViewController {

	private final MovieService movieService;

	// Số phim mỗi trang, đọc từ application.properties (mặc định 5)
	@Value("${app.movies.page-size:5}")
	private int pageSize;

	/**
	 * GET /movies — danh sách + tìm kiếm + phân trang.
	 *
	 * @param keyword từ khóa tìm theo title (tùy chọn)
	 * @param page    chỉ số trang, bắt đầu từ 0 (chuẩn Spring Data)
	 */
	@GetMapping
	public String list(
			@RequestParam(value = "keyword", required = false) String keyword,
			@RequestParam(value = "page", defaultValue = "0") int page,
			Model model) {
		// PageRequest gói thông tin trang + sắp xếp; trang đánh số từ 0
		PageRequest pageRequest = PageRequest.of(Math.max(page, 0), pageSize, Sort.by("title").ascending());
		Page<MovieModel> moviePage = movieService.findPage(keyword, pageRequest);
		// Đẩy dữ liệu sang view qua Model
		model.addAttribute("moviePage", moviePage);
		model.addAttribute("movies", moviePage.getContent());
		model.addAttribute("keyword", keyword == null ? "" : keyword);
		return "movies/list";
	}

	/** GET /movies/new — mở form tạo mới (form rỗng). */
	@GetMapping("/new")
	public String createForm(Model model) {
		model.addAttribute("movieForm", new MovieFormDto());
		model.addAttribute("isEdit", false);
		return "movies/form";
	}

	/**
	 * POST /movies — xử lý tạo mới.
	 *
	 * <p>Validate form; nếu lỗi thì hiển thị lại form. Nếu hợp lệ thì lưu rồi
	 * {@code redirect} theo mẫu Post-Redirect-Get (tránh tạo trùng khi F5).</p>
	 */
	@PostMapping
	public String create(
			@Valid @ModelAttribute("movieForm") MovieFormDto movieForm,
			BindingResult bindingResult,
			Model model,
			RedirectAttributes redirectAttributes) {
		if (bindingResult.hasErrors()) {
			model.addAttribute("isEdit", false);
			return "movies/form";
		}
		MovieModel created = movieService.create(movieForm.toEntity());
		redirectAttributes.addFlashAttribute("message", "Tạo phim thành công!");
		return "redirect:/movies/" + created.getId();
	}

	/** GET /movies/{id} — xem chi tiết 1 phim. */
	@GetMapping("/{id}")
	public String detail(@PathVariable String id, Model model) {
		try {
			model.addAttribute("movie", movieService.getById(id));
			return "movies/detail";
		} catch (ResourceNotFoundException e) {
			return "movies/not-found";
		}
	}

	/** GET /movies/{id}/edit — mở form sửa (đổ sẵn dữ liệu hiện tại). */
	@GetMapping("/{id}/edit")
	public String editForm(@PathVariable String id, Model model) {
		try {
			model.addAttribute("movieForm", MovieFormDto.fromEntity(movieService.getById(id)));
			model.addAttribute("isEdit", true);
			return "movies/form";
		} catch (ResourceNotFoundException e) {
			return "movies/not-found";
		}
	}

	/** POST /movies/{id} — xử lý cập nhật. */
	@PostMapping("/{id}")
	public String update(
			@PathVariable String id,
			@Valid @ModelAttribute("movieForm") MovieFormDto movieForm,
			BindingResult bindingResult,
			Model model,
			RedirectAttributes redirectAttributes) {
		if (bindingResult.hasErrors()) {
			model.addAttribute("isEdit", true);
			return "movies/form";
		}
		try {
			movieService.update(id, movieForm.toEntity());
			redirectAttributes.addFlashAttribute("message", "Cập nhật thành công!");
			return "redirect:/movies/" + id;
		} catch (ResourceNotFoundException e) {
			return "movies/not-found";
		}
	}

	/** POST /movies/{id}/delete — xóa rồi quay lại danh sách (PRG). */
	@PostMapping("/{id}/delete")
	public String delete(@PathVariable String id, RedirectAttributes redirectAttributes) {
		try {
			movieService.delete(id);
			redirectAttributes.addFlashAttribute("message", "Đã xóa phim.");
		} catch (ResourceNotFoundException e) {
			redirectAttributes.addFlashAttribute("error", "Không tìm thấy phim.");
		}
		return "redirect:/movies";
	}

}
