package vn.demo.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Trang chủ — chuyển hướng sang danh sách restaurants.
 */
@Controller
public class HomeController {

	/** Khi truy cập "/", tự chuyển hướng sang "/restaurants". */
	@GetMapping("/")
	public String home() {
		return "redirect:/restaurants";
	}

}
