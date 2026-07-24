# Demo Bài 9 - Online Payment với PayPal Sandbox thật

```text
Danh sách phim → Chi tiết phim → Nút PayPal → Backend tạo order → Capture → Lưu payments
```

## Bắt buộc trước khi chạy

1. MongoDB tại `localhost:27017`
2. Tạo app Sandbox tại <https://developer.paypal.com/dashboard/applications/sandbox>
3. Dán **Client ID** và **Secret** thật vào `src/main/resources/application.properties`:

```properties
paypal.client-id=Axxxxxxxx...   # Client ID Sandbox thật
paypal.client-secret=Exxxxxxxx...  # Secret Sandbox thật
```

> Nếu còn để `REPLACE_WITH_YOUR_...`, trang chi tiết sẽ hiện cảnh báo đỏ và **không có nút PayPal**.

## Chạy

```bash
cd demo-bai9-online-payment/java-springboot-bai9
mvn spring-boot:run
```

Mở <http://localhost:8080>

1. Vào **chi tiết phim** → thấy nút PayPal vàng
2. Bấm → đăng nhập **buyer sandbox** → duyệt
3. Mở **Lịch sử thanh toán** hoặc Mongo:

```javascript
use db_java_t3h_module3
db.payments.find().pretty()
```

## Kiểm tra

```bash
mvn test
```
