package vn.demo.controller.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Trang chủ — chuyển hướng sang danh sách phim (giao diện Thymeleaf).
 */
@Controller
public class HomeController {

	/** Khi truy cập "/", tự chuyển hướng sang "/movies". */
	@GetMapping("/")
	public String home() {
		return "redirect:/movies";
	}

}
