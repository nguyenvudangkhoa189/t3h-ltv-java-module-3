package vn.demo.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import vn.demo.dto.ImportResultDto;
import vn.demo.dto.PagedListView;
import vn.demo.dto.RestaurantDto;
import vn.demo.dto.RestaurantFormDto;
import vn.demo.model.RestaurantModel;
import vn.demo.repository.RestaurantRepository;

/**
 * SERVICE — tầng nghiệp vụ cho Restaurant.
 *
 * <p>Nhiệm vụ: import file, phân trang, xem & chỉnh sửa. Controller chỉ gọi xuống
 * đây, không tự xử lý logic.</p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RestaurantService {

	/** Số document mỗi lần ghi xuống DB (ghi theo batch để tối ưu hiệu năng). */
	private static final int BATCH_SIZE = 500;

	/** Số chỉ số trang tối đa hiển thị trên thanh phân trang. */
	private static final int MAX_PAGES_TO_SHOW = 5;

	private final RestaurantRepository restaurantRepository;

	/** Tái sử dụng 1 ObjectMapper (thread-safe) thay vì tạo mới mỗi dòng. */
	private final ObjectMapper objectMapper = new ObjectMapper();

	/**
	 * Đọc file định dạng <b>NDJSON</b> (mỗi dòng là 1 object JSON) và lưu vào DB.
	 *
	 * @param file file người dùng upload
	 * @return kết quả import (số lượng + thông báo)
	 */
	public ImportResultDto handleImport(MultipartFile file) {
		if (file == null || file.isEmpty()) {
			return ImportResultDto.fail("Please select a JSON file to upload.");
		}
		// try-with-resources: tự đóng reader sau khi xong
		try (BufferedReader reader = new BufferedReader(
				new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
			int count = importFromReader(reader);
			log.info("Đã import {} restaurant", count);
			return ImportResultDto.ok(count);
		} catch (Exception e) {
			log.warn("Import thất bại: {}", e.getMessage());
			return ImportResultDto.fail("Failed to import file: " + e.getMessage());
		}
	}

	/**
	 * Duyệt từng dòng, gom thành batch và lưu dần.
	 *
	 * @return tổng số document đã lưu
	 */
	private int importFromReader(BufferedReader reader) throws IOException {
		int count = 0;
		List<RestaurantModel> batch = new ArrayList<>();
		String line;
		while ((line = reader.readLine()) != null) { // đọc từng dòng tới hết file
			if (line.trim().isEmpty()) {
				continue; // bỏ qua dòng rỗng
			}
			batch.add(parseLine(line));
			count++;
			if (batch.size() >= BATCH_SIZE) {
				restaurantRepository.saveAll(batch); // ghi 1 lô rồi dọn để gom lô tiếp
				batch.clear();
			}
		}
		if (!batch.isEmpty()) {
			restaurantRepository.saveAll(batch); // ghi nốt phần còn lại (< BATCH_SIZE)
		}
		return count;
	}

	/** Chuyển 1 dòng JSON thành đối tượng RestaurantModel (tách riêng để dễ unit test). */
	public RestaurantModel parseLine(String line) throws IOException {
		return objectMapper.readValue(line, RestaurantModel.class);
	}

	/**
	 * Lấy 1 trang dữ liệu — trả {@link PagedListView} (§6.0 syllabus).
	 *
	 * <p>Repository trả {@code Page&lt;RestaurantModel&gt;} nội bộ; Service chuyển sang
	 * {@code PagedResponse&lt;RestaurantDto&gt;} qua {@link vn.demo.dto.PageMapper} — Controller không thấy {@code Page}.</p>
	 */
	public PagedListView<RestaurantDto> findPage(Pageable pageable, String sortBy, String dir) {
		Page<RestaurantModel> page = restaurantRepository.findAll(pageable);
		return PagedListView.from(page, RestaurantDto::fromEntity, null, sortBy, dir, MAX_PAGES_TO_SHOW);
	}

	/** Tìm restaurant theo id nghiệp vụ (restaurant_id). */
	public RestaurantFormDto getFormByRestaurantId(String restaurantId) {
		RestaurantModel restaurant = restaurantRepository.findFirstByRestaurantId(restaurantId);
		return RestaurantFormDto.fromEntity(restaurant);
	}

	/**
	 * Cập nhật thông tin cơ bản; chỉ ghi đè field có giá trị mới (partial update).
	 *
	 * @param restaurantId id nghiệp vụ cần sửa
	 * @param newObject    dữ liệu mới từ form
	 * @return form sau khi lưu; {@code null} nếu không tìm thấy
	 */
	public RestaurantFormDto updateDetail(String restaurantId, RestaurantFormDto newObject) {
		RestaurantModel oldObject = restaurantRepository.findFirstByRestaurantId(restaurantId);
		if (oldObject == null) {
			return null;
		}
		RestaurantModel changes = newObject.toEntity();
		if (changes.getName() != null) {
			oldObject.setName(changes.getName());
		}
		if (changes.getBorough() != null) {
			oldObject.setBorough(changes.getBorough());
		}
		if (changes.getCuisine() != null) {
			oldObject.setCuisine(changes.getCuisine());
		}
		return RestaurantFormDto.fromEntity(restaurantRepository.save(oldObject));
	}

}
