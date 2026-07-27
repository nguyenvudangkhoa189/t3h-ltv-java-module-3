package vn.demo.dto;

import java.util.List;

import lombok.Builder;
import lombok.Data;

/**
 * Dữ liệu trang chủ {@code index.html} — các section map từ MongoDB.
 */
@Data
@Builder
public class HomePageView {

	/** Hero slider (3 slide). */
	private List<MovieCardDto> hero;
	/** Trending Now — 6 phim. */
	private List<MovieCardDto> trending;
	/** Popular Shows — 6 TV Show. */
	private List<MovieCardDto> popular;
	/** Recently Added — 6 title mới nhất theo release_year. */
	private List<MovieCardDto> recent;
	/** Live Action — 6 title có genre Action. */
	private List<MovieCardDto> liveAction;
	/** Sidebar Top Views — 5 title. */
	private List<MovieCardDto> topViews;

}
