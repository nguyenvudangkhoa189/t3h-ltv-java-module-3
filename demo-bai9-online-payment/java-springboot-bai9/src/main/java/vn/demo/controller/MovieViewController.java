package vn.demo.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import lombok.RequiredArgsConstructor;
import vn.demo.config.PayPalProperties;
import vn.demo.repository.MovieRepository;
import vn.demo.service.PaymentService;

@Controller
@RequiredArgsConstructor
public class MovieViewController {

	private final MovieRepository movieRepository;
	private final PaymentService paymentService;
	private final PayPalProperties payPalProperties;

	@GetMapping("/")
	public String home() {
		return "redirect:/movies";
	}

	@GetMapping("/movies")
	public String list(Model model) {
		model.addAttribute("movies", movieRepository.findAll());
		return "movies/list";
	}

	@GetMapping("/movies/{id}")
	public String detail(@PathVariable String id, Model model) {
		var movie = movieRepository.findById(id)
				.orElseThrow(() -> new IllegalArgumentException("Không tìm thấy phim: " + id));
		model.addAttribute("movie", movie);
		model.addAttribute("paypalConfigured", payPalProperties.isConfigured());
		model.addAttribute("paypalClientId", payPalProperties.getClientId());
		model.addAttribute("paypalCurrency", payPalProperties.getCurrency());
		return "movies/detail";
	}

	@GetMapping("/payments")
	public String payments(Model model) {
		model.addAttribute("payments", paymentService.findRecentPayments());
		return "payments/list";
	}
}
