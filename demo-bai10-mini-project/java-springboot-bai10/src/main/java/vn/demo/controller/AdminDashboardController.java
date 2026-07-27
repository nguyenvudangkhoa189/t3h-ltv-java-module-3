package vn.demo.controller;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import lombok.RequiredArgsConstructor;
import vn.demo.dto.CountryCountDto;
import vn.demo.dto.GenreCountDto;
import vn.demo.dto.MonthCountDto;
import vn.demo.dto.MovieRankDto;
import vn.demo.service.DashboardStatsService;

/**
 * CONTROLLER — Admin Dashboard ({@code /admin/dashboard}).
 *
 * <p><b>Flow:</b> GET (đã auth ADMIN) → gọi 6 method stats → đẩy list + labels Chart.js
 * → view {@code admin/dashboard}.</p>
 */
@Controller
@RequiredArgsConstructor
@RequestMapping("/admin")
public class AdminDashboardController {

	private final DashboardStatsService dashboardStatsService;

	/** {@code /admin} → redirect dashboard. */
	@GetMapping
	public String adminRoot() {
		return "redirect:/admin/dashboard";
	}

	@GetMapping("/dashboard")
	public String dashboard(Model model) {
		// --- Lấy 6 khối thống kê từ MongoDB ---
		List<MonthCountDto> months = dashboardStatsService.topMovieReleaseMonths(7);
		List<GenreCountDto> genres = dashboardStatsService.countByGenre(10);
		List<CountryCountDto> countries = dashboardStatsService.countByCountry(10);
		List<MovieRankDto> longest = dashboardStatsService.topLongestMovies(5);
		List<MovieRankDto> latest = dashboardStatsService.latestAdded(5);
		List<MovieRankDto> popular = dashboardStatsService.topPopularByRule(10);

		model.addAttribute("months", months);
		model.addAttribute("genres", genres);
		model.addAttribute("countries", countries);
		model.addAttribute("longest", longest);
		model.addAttribute("latest", latest);
		model.addAttribute("popular", popular);

		// --- Tách labels/counts cho Chart.js (th:inline) ---
		model.addAttribute("monthLabels", months.stream().map(MonthCountDto::getMonth).collect(Collectors.toList()));
		model.addAttribute("monthCounts", months.stream().map(MonthCountDto::getCount).collect(Collectors.toList()));
		model.addAttribute("genreLabels", genres.stream().map(GenreCountDto::getGenre).collect(Collectors.toList()));
		model.addAttribute("genreCounts", genres.stream().map(GenreCountDto::getCount).collect(Collectors.toList()));
		model.addAttribute("countryLabels", countries.stream().map(CountryCountDto::getCountry).collect(Collectors.toList()));
		model.addAttribute("countryCounts", countries.stream().map(CountryCountDto::getCount).collect(Collectors.toList()));

		return "admin/dashboard";
	}

}
