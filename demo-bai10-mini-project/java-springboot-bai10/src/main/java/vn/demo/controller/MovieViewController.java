package vn.demo.controller;

import java.util.List;

import org.springframework.http.MediaType;
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
import vn.demo.dto.CommentFormDto;
import vn.demo.dto.HomePageView;
import vn.demo.dto.MovieCardDto;
import vn.demo.dto.MovieDetailDto;
import vn.demo.dto.PagedListView;
import vn.demo.service.CommentService;
import vn.demo.service.MovieService;

/**
 * CONTROLLER (web) — Thymeleaf public (kế thừa Bài 7).
 *
 * <p><b>Flow:</b> GET trang → Service lấy DTO từ MongoDB → template Anime.
 * POST comment → validate → save → {@code redirect:} (PRG). {@code /movies/chart}
 * redirect sang admin dashboard (Bài 10).</p>
 */
@Controller
@RequiredArgsConstructor
@RequestMapping("/movies")
public class MovieViewController {

	private final MovieService movieService;
	private final CommentService commentService;

	/** Trang chủ — {@code anime-main/index} + fragment layout. */
	@GetMapping(produces = MediaType.TEXT_HTML_VALUE)
	public String showIndex(Model model) {
		// --- Gom dữ liệu các section index từ MongoDB ---
		HomePageView home = movieService.buildHomePage();
		model.addAttribute("hero", home.getHero());
		model.addAttribute("trending", home.getTrending());
		model.addAttribute("popular", home.getPopular());
		model.addAttribute("recent", home.getRecent());
		model.addAttribute("liveAction", home.getLiveAction());
		model.addAttribute("topViews", home.getTopViews());

		// --- Sidebar New Comment = comment thật từ DB ---
		model.addAttribute("sidebarComments", commentService.findRecentSidebar(4));
		return "anime-main/index";
	}

	/** Categories — phân trang + sidebar. */
	@GetMapping(value = "/home", produces = MediaType.TEXT_HTML_VALUE)
	public String showMovieList(@RequestParam(defaultValue = "0") int page, Model model) {
		// --- Trang danh sách (9 item / page) ---
		PagedListView<MovieCardDto> listView = movieService.findPage(page, 9);
		model.addAttribute("list", listView.getContent());
		model.addAttribute("currentPage", listView.getPagination().getPage());
		model.addAttribute("totalPages", listView.getPagination().getTotalPages());
		model.addAttribute("startPage", listView.getStartPage());
		model.addAttribute("endPage", listView.getEndPage());

		// --- Sidebar ---
		model.addAttribute("topViews", movieService.findTopViews(5));
		model.addAttribute("sidebarComments", commentService.findRecentSidebar(4));
		return "anime-main/categories";
	}

	/** Chi tiết phim + danh sách bình luận. */
	@GetMapping(value = "/detail/{id}", produces = MediaType.TEXT_HTML_VALUE)
	public String showMovieDetail(@PathVariable String id, Model model) {
		// --- Load phim; không thấy → not-found ---
		MovieDetailDto movie = movieService.findDetailById(id).orElse(null);
		if (movie == null) {
			return "movies/not-found";
		}

		// --- Đưa dữ liệu + form trống lên view ---
		model.addAttribute("movie", movie);
		model.addAttribute("comments", commentService.findByMovieId(id));
		model.addAttribute("related", movieService.findRelated(id, 4));
		model.addAttribute("commentForm", new CommentFormDto());
		return "anime-main/anime-details";
	}

	/** POST bình luận trên trang detail — PRG. */
	@PostMapping("/detail/{id}")
	public String createCommentOnDetail(@PathVariable String id,
			@Valid @ModelAttribute("commentForm") CommentFormDto form,
			BindingResult bindingResult,
			RedirectAttributes redirectAttributes,
			Model model) {

		// --- Gắn movieId từ path (không tin field ẩn từ client) ---
		form.setMovieId(id);

		// --- Validation fail → render lại form kèm lỗi ---
		if (bindingResult.hasErrors()) {
			MovieDetailDto movie = movieService.findDetailById(id).orElseThrow();
			model.addAttribute("movie", movie);
			model.addAttribute("comments", commentService.findByMovieId(id));
			model.addAttribute("related", movieService.findRelated(id, 4));
			return "anime-main/anime-details";
		}

		// --- Lưu DB rồi redirect (F5 không tạo comment trùng) ---
		commentService.save(form);
		redirectAttributes.addFlashAttribute("message", "Đã lưu bình luận");
		return "redirect:/movies/detail/" + id;
	}

	/** Watching — video mẫu template + bình luận. */
	@GetMapping(value = "/watching/{id}", produces = MediaType.TEXT_HTML_VALUE)
	public String showWatching(@PathVariable String id,
			@RequestParam(defaultValue = "1") int ep,
			Model model) {
		MovieDetailDto movie = movieService.findDetailById(id).orElse(null);
		if (movie == null) {
			return "movies/not-found";
		}

		// --- Chọn tập hiện tại trong khoảng hợp lệ ---
		int maxEp = movie.getEpisodes() != null ? movie.getEpisodes().size() : 1;
		int currentEp = Math.min(Math.max(ep, 1), maxEp);
		model.addAttribute("movie", movie);
		model.addAttribute("currentEp", currentEp);
		model.addAttribute("comments", commentService.findByMovieId(id));
		model.addAttribute("commentForm", new CommentFormDto());
		return "anime-main/anime-watching";
	}

	/** POST bình luận trên trang watching — PRG. */
	@PostMapping("/watching/{id}")
	public String createCommentOnWatching(@PathVariable String id,
			@Valid @ModelAttribute("commentForm") CommentFormDto form,
			BindingResult bindingResult,
			RedirectAttributes redirectAttributes,
			Model model) {

		form.setMovieId(id);
		if (bindingResult.hasErrors()) {
			MovieDetailDto movie = movieService.findDetailById(id).orElseThrow();
			model.addAttribute("movie", movie);
			model.addAttribute("currentEp", 1);
			model.addAttribute("comments", commentService.findByMovieId(id));
			return "anime-main/anime-watching";
		}

		commentService.save(form);
		redirectAttributes.addFlashAttribute("message", "Đã lưu bình luận");
		return "redirect:/movies/watching/" + id;
	}

	/**
	 * Chart public cũ (Bài 7) → Admin Dashboard.
	 *
	 * <p>Flow: khách chưa login sẽ bị Security đưa về /login rồi vào dashboard.</p>
	 */
	@GetMapping("/chart")
	public String showChart() {
		return "redirect:/admin/dashboard";
	}

}
