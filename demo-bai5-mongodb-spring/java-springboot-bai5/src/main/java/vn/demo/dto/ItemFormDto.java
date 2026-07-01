package vn.demo.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.NoArgsConstructor;
import vn.demo.model.ItemModel;

/**
 * DTO form Thymeleaf — thêm/sửa món ăn (§10.5 syllabus).
 *
 * <p>Tách DTO khỏi entity để validate form và chuyển đổi qua lại {@link ItemModel}.</p>
 */
@Data
@NoArgsConstructor
public class ItemFormDto {

	private String id;

	@NotBlank(message = "Tên món không được để trống")
	private String name;

	private String description;

	@Min(value = 0, message = "Giá phải >= 0")
	private Double price;

	private String category;

	/** Tạo form từ entity (khi mở trang sửa). */
	public static ItemFormDto fromEntity(ItemModel item) {
		ItemFormDto form = new ItemFormDto();
		form.setId(item.getId());
		form.setName(item.getName());
		form.setDescription(item.getDescription());
		form.setPrice(item.getPrice());
		form.setCategory(item.getCategory());
		return form;
	}

	/** Chuyển form thành entity để lưu xuống MongoDB. */
	public ItemModel toEntity() {
		ItemModel item = new ItemModel();
		item.setId(id);
		item.setName(name);
		item.setDescription(description);
		item.setPrice(price);
		item.setCategory(category);
		return item;
	}

}
