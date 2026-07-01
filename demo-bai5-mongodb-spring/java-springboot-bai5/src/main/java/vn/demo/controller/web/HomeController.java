package vn.demo.controller.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/** Điều hướng trang chủ → danh sách nhà hàng. */
@Controller
public class HomeController {

	@GetMapping("/")
	public String home() {
		return "redirect:/restaurants";
	}

}
