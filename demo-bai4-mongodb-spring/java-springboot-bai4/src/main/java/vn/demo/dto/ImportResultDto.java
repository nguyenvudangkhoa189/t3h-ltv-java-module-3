package vn.demo.dto;

/**
 * DTO — kết quả của một lần import file.
 *
 * <p>Là {@code record} (lớp dữ liệu bất biến, Java 16+). Dùng để Service trả kết quả
 * gọn gàng cho Controller thay vì trả "chuỗi thông báo lẫn lộn".</p>
 *
 * @param success     import thành công hay không
 * @param message     thông báo hiển thị cho người dùng
 * @param importCount số document đã lưu thành công
 */
public record ImportResultDto(boolean success, String message, int importCount) {

	/** Tạo kết quả thành công kèm số lượng đã import. */
	public static ImportResultDto ok(int count) {
		return new ImportResultDto(true, "File is uploaded successfully with " + count + " documents", count);
	}

	/** Tạo kết quả thất bại kèm thông báo lỗi. */
	public static ImportResultDto fail(String message) {
		return new ImportResultDto(false, message, 0);
	}

}
