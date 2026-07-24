# Bài 9: Thanh toán online với PayPal — Orders API v2, Capture & Lưu giao dịch

## Mục tiêu bài học

Sau bài này, học viên có thể:

- Giải thích **các thành phần** của hệ thống thanh toán online: **Payment Gateway**, **Payment Processor**, **Acquirer**, **Issuer**
- Đọc được **bản đồ cổng thanh toán**: quốc tế (PayPal, Stripe) vs Việt Nam (VNPay, MoMo, ZaloPay) — biết chọn cái nào cho dự án
- Đăng ký **PayPal Developer** + tạo **Sandbox app** + tài khoản test (buyer/seller)
- Hiểu **luồng chuẩn** của PayPal Checkout: **Create Order → Approve → Capture** — vì sao create/capture **phải ở server**
- Tích hợp PayPal vào **app phim Spring Boot (Bài 7)**: **JS SDK Smart Buttons** (FE) + **Orders v2 API** (BE)
- Gọi REST PayPal bằng **`RestClient`** + **OAuth2 `client_credentials`** (có **cache access token**)
- Lưu **trạng thái giao dịch** vào MongoDB (collection `payments`) theo một **state machine** rõ ràng
- Thực hiện **hoàn tiền (refund)** qua Payments API v2
- Biết vai trò **webhook** và vì sao production **bắt buộc** dùng (kèm endpoint demo)
- Áp dụng **bảo mật thanh toán**: amount server-side, secret trong env, verify status, **không lưu thẻ** (ý nghĩa PCI-DSS)
- Viết **unit test `PaymentService`** bằng Mockito — **mock `PayPalClient`** (nối tiếp Bài 8)

> **Không nằm trong phạm vi bài này:** subscription/billing định kỳ; xử lý dispute/chargeback chuyên sâu; tự nhận số thẻ (hosted card fields — PCI-DSS SAQ-D); tích hợp VNPay/MoMo từng bước (chỉ giới thiệu).

## Điều kiện tiên quyết

- **[Bài 3](./java_m3_bai3_MongoDB_Spring_1.md)**: Model / Repository / Service / Controller
- **[Bài 4](./java_m3_bai4_MongoDB_Spring_2.md)**: Thymeleaf, DTO, PRG
- **[Bài 7](./java_m3_bai7_Database_Query_To_FrontEnd.md)**: app phim (trang detail — nơi gắn nút thanh toán), collection `mymoviedb`
- **[Bài 8](./java_m3_bai8_Unit_Testing.md)**: Mockito (cho phần test `PaymentService`)
- MongoDB tại `localhost:27017`
- **Tài khoản PayPal Developer** (sandbox — miễn phí): <https://developer.paypal.com/>
- **Spring Boot 3.2+** (để có `RestClient`); demo dùng 3.5.x + Java 17
- Dependency:

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-web</artifactId>
</dependency>
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-mongodb</artifactId>
</dependency>
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-thymeleaf</artifactId>
</dependency>
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-validation</artifactId>
</dependency>
<dependency>
    <groupId>org.projectlombok</groupId>
    <artifactId>lombok</artifactId>
    <optional>true</optional>
</dependency>
```

> **Không cần SDK PayPal.** Ta gọi thẳng REST bằng `RestClient` (đi kèm `spring-boot-starter-web`) để học viên **thấy rõ OAuth2 + REST**, không phụ thuộc version SDK. SDK cũ `com.paypal.sdk:checkout-sdk` đã **deprecated**; nếu muốn, có thể xem SDK mới `com.paypal.sdk:paypal-server-sdk` như hướng đọc thêm (§12).
>
> **Demo chuẩn:** [`demo-bai9-online-payment`](../../demo-bai9-online-payment) — app phim Bootstrap tối giản + luồng **thuê phim** trả tiền qua **PayPal Sandbox thật**.
> Database `db_java_t3h_module3`, collection **`movies`** + **`payments`**.
> **Bắt buộc** dán Client ID / Secret Sandbox thật vào `application.properties` — nếu còn placeholder, trang chi tiết hiện cảnh báo và **không** render nút PayPal.
>
> **Sandbox = tiền ảo.** Toàn bộ lab chạy trên PayPal Sandbox, **không mất tiền thật**. Rào cản của PayPal tại VN chỉ nằm ở **live/production** (rút tiền, onboarding doanh nghiệp) — để **học & demo** thì sandbox hoạt động bình thường.

### Thời lượng gợi ý

| Phần | Thời gian |
|------|-----------|
| Giới thiệu + thành phần hệ thống §1–2 | ~20 phút |
| Bản đồ gateway (quốc tế vs VN) §3 | ~10 phút |
| Đăng ký PayPal Sandbox §4 | ~15 phút |
| Luồng thanh toán chuẩn (sequence) §5 | ~15 phút |
| **Cấu hình + Model + PayPalClient (OAuth2/create/capture)** §6–8 | ~50 phút |
| Service + Controller + Smart Buttons §9–10 | ~45 phút |
| Refund §11 | ~15 phút |
| Webhook (nâng cao) §12 | ~15 phút |
| Bảo mật thanh toán §13 | ~15 phút |
| Unit test `PaymentService` §14 | ~20 phút |
| Lỗi thường gặp + bài tập | ~15 phút |

## Nội dung (làm theo thứ tự)

| # | Chủ đề | Kết quả kiểm tra |
|---|--------|------------------|
| 1 | Giới thiệu online payment | Nói được mua–bán qua Internet gồm những bên nào |
| 2 | Thành phần hệ thống | Phân biệt Gateway / Processor / Acquirer / Issuer |
| 3 | Bản đồ gateway | Biết PayPal/Stripe (quốc tế) vs VNPay/MoMo (VN) |
| 4 | Đăng ký PayPal Sandbox | Có Client ID + Secret + 1 buyer test |
| 5 | Luồng thanh toán chuẩn | Vẽ được sequence Create → Approve → Capture |
| 6 | Cấu hình + dependency | App đọc được credential từ env |
| 7 | `PaymentModel` + Repository | Collection `payments` + state machine |
| 8 | `PayPalClient` (RestClient) | Lấy token, create, capture bằng REST |
| 9 | `PaymentService` + Controller | 2 endpoint create/capture, amount **server-side** |
| 10 | Frontend Smart Buttons | Nút PayPal trên trang detail phim (Bài 7) |
| 11 | Refund | Hoàn tiền bằng captureId |
| 12 | Webhook (nâng cao) | Hiểu vì sao production cần webhook |
| 13 | Bảo mật thanh toán | Checklist đúng (không phải "HTTPS + CORS" chung chung) |
| 14 | Unit test `PaymentService` | Mock `PayPalClient`, không gọi mạng |
| 15 | Lỗi thường gặp | — |
| Phụ lục | Bài tập · Checklist · Liên kết | — |

---

## Kiến trúc lab (mở rộng app phim Bài 7)

```
src/main/java/vn/demo/
├── model/
│   ├── MovieModel.java              ← từ Bài 7 (collection "mymoviedb")
│   └── PaymentModel.java            ← MỚI: collection "payments"
├── repository/
│   ├── MovieRepository.java         ← từ Bài 7
│   └── PaymentRepository.java       ← MỚI
├── client/
│   └── PayPalClient.java            ← MỚI: gọi REST PayPal (OAuth2 + Orders v2)
├── service/
│   ├── MovieService.java            ← từ Bài 7
│   └── PaymentService.java          ← MỚI: nghiệp vụ thanh toán + lưu trạng thái
├── dto/
│   ├── CreateOrderRequest.java      ← MỚI: { movieId } (KHÔNG có amount)
│   └── PaymentResultDto.java        ← MỚI: trả về FE (status, captureId…)
├── controller/
│   ├── MovieViewController.java     ← từ Bài 7 (trang detail)
│   └── PaymentRestController.java   ← MỚI: /api/payments/*
└── config/
    └── PayPalProperties.java        ← MỚI: bind cấu hình paypal.*

src/main/resources/
├── application.properties           ← thêm block paypal.*
└── templates/
    ├── movies/list.html             ← danh sách phim (Bootstrap)
    ├── movies/detail.html           ← chi tiết + nút thanh toán
    └── payments/list.html           ← lịch sử thanh toán
```

| Tầng | Lớp mới | Nhiệm vụ |
|------|---------|----------|
| **Model** | `PaymentModel` | Ánh xạ document `payments`; suffix **`Model`** |
| **Repository** | `PaymentRepository` | Truy vấn giao dịch (theo `paypalOrderId`) |
| **Client** | `PayPalClient` | Gọi PayPal REST: token, create order, capture, refund |
| **Service** | `PaymentService` | **Quyết định amount**, tạo order, capture, cập nhật trạng thái |
| **Controller** | `PaymentRestController` | 2 endpoint cho Smart Buttons gọi vào |
| **Config** | `PayPalProperties` | Bind `paypal.client-id/secret/base-url` |

> **Quy tắc vàng của bài này:** **Amount do server quyết định** (tra từ `movieId`). FE **không bao giờ** gửi số tiền. Create & capture **đều ở backend**; FE chỉ cầm `orderId`.

```mermaid
flowchart LR
    A[Trình duyệt: Smart Button] -->|1. Tạo đơn hàng createOrder| B["PaymentRestController"]
    B --> C[PaymentService]
    C -->|tra số tiền theo movieId| D[PayPalClient]
    D -->|POST /v2/checkout/orders| P((PayPal))
    C --> E[(payments)]
    A -->|2. người dùng duyệt trong cửa sổ PayPal| P
    A -->|3. onApprove → thu tiền| B
    D -->|POST .../capture| P
    C -->|cập nhật COMPLETED + captureId| E
```

---

## 1. Giới thiệu online payment

**Online payment** (thanh toán trực tuyến) là quá trình chuyển tiền an toàn giữa **người mua** và **người bán** qua Internet, kèm các biện pháp bảo mật để bảo vệ thông tin.

Các phương thức phổ biến: **thẻ tín dụng** (credit card), **thẻ ghi nợ** (debit card), **ví điện tử** (PayPal, MoMo…), **chuyển khoản ngân hàng**.

**Vai trò của developer backend** không phải là "tự xử lý tiền", mà là:

- Gọi đúng API của **cổng thanh toán tin cậy** (PayPal/Stripe/VNPay…)
- **Lưu & đối soát** trạng thái giao dịch trong hệ thống của mình
- **Không** tự lưu số thẻ/CVV/password của khách (để cổng thanh toán lo — xem §13)

---

## 2. Các thành phần của hệ thống thanh toán

| Thành phần | Vai trò | Ví dụ |
|------------|---------|-------|
| **Payment Gateway** (cổng thanh toán) | Nhận thông tin thanh toán từ website và chuyển **an toàn** tới bộ xử lý | PayPal, Stripe, VNPay |
| **Payment Processor** (bộ xử lý) | **Định tuyến** giao dịch tới ngân hàng/tổ chức phát hành thẻ để xác thực & chuyển tiền | (thường nằm sau gateway) |
| **Acquirer** (ngân hàng thu hộ) | Ngân hàng của **người bán**, nhận tiền về | Vietcombank, Wells Fargo… |
| **Issuer** (tổ chức phát hành) | Ngân hàng/đơn vị phát hành **thẻ của người mua**, xác thực & trừ tiền | Visa/Mastercard issuer bank |

```mermaid
flowchart LR
    U[Người mua] --> W[Website người bán]
    W --> G[Payment Gateway]
    G --> PR[Payment Processor]
    PR --> ISS[Issuer - NH người mua]
    PR --> ACQ[Acquirer - NH người bán]
    ISS -->|xác thực + trừ tiền| PR
    ACQ -->|nhận tiền| W
```

> Với PayPal, ta chỉ làm việc với **gateway (PayPal)**; PayPal lo phần processor/acquirer/issuer bên trong. Đây chính là lý do dùng cổng thanh toán: **giảm gánh nặng tích hợp & tuân thủ**.

---

## 3. Bản đồ cổng thanh toán — chọn cái nào?

| Nhóm | Cổng phổ biến | Phù hợp khi |
|------|---------------|-------------|
| **Quốc tế** | **PayPal**, **Stripe** | Bán xuyên biên giới, khách nước ngoài, thu USD/EUR… |
| **Việt Nam** | **VNPay**, **MoMo**, **ZaloPay**, **OnePay**, **PayOS** | Khách nội địa, thu VND, QR/ATM nội địa |

**Chọn cho lab học tập:** dùng **PayPal Sandbox** vì:

- **Miễn phí + không cần merchant thật**, tạo tài khoản trong vài phút
- Sandbox **hoạt động ở VN** cho mục đích học
- API **Orders v2** hiện đại, tài liệu tốt, pattern giống Stripe → dễ chuyển đổi sau này

> **Thực tế dự án VN:** nếu khách hàng chủ yếu trong nước, bạn sẽ tích hợp **VNPay/MoMo**. Luồng tổng thể (tạo giao dịch → redirect/QR → nhận kết quả qua callback/IPN → đối soát) **giống về tư duy** với PayPal. Học chắc PayPal ở đây → tích hợp VNPay/MoMo sau nhanh hơn.

---

## 4. Đăng ký PayPal Sandbox (làm 1 lần)

1. Đăng nhập <https://developer.paypal.com/dashboard/>
2. **Apps & Credentials** → tab **Sandbox** → **Create App**
   - Đặt tên app (mỗi website nên 1 app riêng)
   - Chọn loại **Merchant**
   - Lấy **Client ID** và **Secret** (đây là credential để backend gọi API)
3. **Sandbox → Accounts**: PayPal tạo sẵn 2 tài khoản test:
   - 1 **Business** (người bán — tiền sẽ về đây)
   - 1 **Personal** (người mua — dùng để đăng nhập & trả tiền lúc test; sandbox nạp sẵn tiền ảo)
   - Mở tài khoản Personal → **View/Edit** để xem **email + password** đăng nhập lúc thanh toán
4. *(Tùy chọn)* Bật **Negative Testing** trên Business account để test lỗi (`INSTRUMENT_DECLINED`, `INSUFFICIENT_FUNDS`…)

| Thông tin | Dùng ở đâu |
|-----------|------------|
| **Client ID** | Cả backend (gọi API) và frontend (JS SDK — public, để lộ được) |
| **Secret** | **CHỈ** backend (env var); **không** commit, **không** đưa lên FE |
| Buyer sandbox email/pass | Đăng nhập trong popup PayPal khi test |

---

## 5. Luồng thanh toán chuẩn (Orders v2)

```mermaid
sequenceDiagram
    participant FE as Trình duyệt (Smart Button)
    participant BE as Spring Boot (PaymentService)
    participant PP as PayPal Orders v2

    FE->>BE: 1. POST /api/payments/orders { movieId }
    BE->>BE: tra số tiền theo movieId (server quyết định)
    BE->>PP: 2. POST /v2/checkout/orders (intent=CAPTURE)
    PP-->>BE: { id, status: CREATED }
    BE->>BE: lưu Payment(status=CREATED)
    BE-->>FE: { orderId }
    FE->>PP: 3. người dùng đăng nhập và duyệt trong cửa sổ PayPal
    FE->>BE: 4. POST /api/payments/orders/{id}/capture
    BE->>PP: 5. POST /v2/checkout/orders/{id}/capture
    PP-->>BE: { status: COMPLETED, captures[0].id }
    BE->>BE: cập nhật Payment(status=COMPLETED, captureId)
    BE-->>FE: { status, captureId }
```

**State machine của một giao dịch:**

```mermaid
stateDiagram-v2
    [*] --> CREATED: tạo đơn hàng
    CREATED --> COMPLETED: thu tiền thành công
    CREATED --> FAILED: thu tiền lỗi / bị từ chối
    CREATED --> CANCELLED: người dùng hủy trong cửa sổ PayPal
    COMPLETED --> REFUNDED: hoàn tiền
```

> **Vì sao create/capture ở server?** Nếu để trình duyệt tự quyết số tiền hoặc tự capture, kẻ gian có thể sửa payload (ví dụ trả $0.01 cho phim $9.99). Server là nơi **duy nhất** được tin để quyết amount và xác nhận kết quả.

---

## 6. Cấu hình + dependency

### Bước 6.1 — `application.properties`

Ở bước này, cứ **điền trực tiếp** Client ID + Secret (từ Sandbox app §4) để **chạy được ngay**, dễ hình dung. Đến **§13 Bảo mật** ta sẽ chuyển sang cách an toàn hơn (biến môi trường) và giải thích vì sao.

Dùng **`application.properties`** — thống nhất với các demo Bài 3–8 của module.

```properties
spring.application.name=demo-bai9-online-payment

# Khớp Mongo có auth trên máy lab (giống demo bài 4/6/7/8).
# Nếu Mongo không bật auth, dùng dòng comment bên dưới.
spring.data.mongodb.uri=mongodb://root:DBVWiYdDoMnfWmK@localhost:27017/db_java_t3h_module3?authSource=admin
# spring.data.mongodb.uri=mongodb://localhost:27017/db_java_t3h_module3

spring.thymeleaf.cache=false

# BẮT BUỘC: dán Client ID / Secret THẬT từ PayPal Developer Dashboard (§4)
# Placeholder sẽ khiến trang chi tiết KHÔNG hiện nút PayPal.
paypal.base-url=https://api-m.sandbox.paypal.com
paypal.client-id=Axxxxxxxx...dán-Client-ID-Sandbox-thật
paypal.client-secret=Exxxxxxxx...dán-Secret-Sandbox-thật
paypal.currency=USD
```

> **Lưu ý (sẽ xử lý ở §13):** viết thẳng secret vào `application.properties` giúp học ban đầu dễ, nhưng **không an toàn** khi đẩy lên Git. §13 sẽ hướng dẫn thay bằng **biến môi trường** + `.gitignore`.

### Bước 6.2 — Bind cấu hình bằng `@ConfigurationProperties`

**File:** `config/PayPalProperties.java`

Spring Boot map `paypal.base-url` → `baseUrl`, `paypal.client-id` → `clientId` (relaxed binding).

```java
@Getter
@Setter
@ConfigurationProperties(prefix = "paypal")
public class PayPalProperties {
    private String baseUrl;
    private String clientId;
    private String clientSecret;
    private String currency = "USD";

    /** true khi đã dán Client ID/Secret thật (không còn placeholder). */
    public boolean isConfigured() {
        return clientId != null && clientSecret != null
                && !clientId.contains("REPLACE_WITH")
                && !clientId.contains("...")
                && clientId.length() > 20
                && clientSecret.length() > 20;
    }
}
```

Bật scan trên class Application (hoặc gắn `@EnableConfigurationProperties(PayPalProperties.class)`):

```java
@SpringBootApplication
@ConfigurationPropertiesScan
public class DemoBai9OnlinePaymentApplication { ... }
```

---

## 7. `PaymentModel` + Repository

### Bước 7.1 — `PaymentModel` (collection `payments`)

```java
@Getter
@Setter
@NoArgsConstructor
@Document(collection = "payments")
public class PaymentModel {

    @Id
    private String id;

    private String movieId;          // phim được thuê
    private String paypalOrderId;    // id PayPal trả về khi CREATE
    private String captureId;        // id để REFUND về sau
    private BigDecimal amount;       // tiền — dùng BigDecimal, KHÔNG dùng double
    private String currency;         // "USD"
    private String status;           // CREATED, COMPLETED, FAILED, CANCELLED, REFUNDED
    private String payerEmail;       // email người mua (từ PayPal, tuỳ chọn)

    private Instant createdAt;
    private Instant updatedAt;
}
```

> **Vì sao `BigDecimal` cho tiền?** `double`/`float` có sai số nhị phân (0.1 + 0.2 ≠ 0.3). Tiền **luôn** dùng `BigDecimal` + `toPlainString()` khi gửi cho PayPal.

### Bước 7.2 — `PaymentRepository`

```java
public interface PaymentRepository extends MongoRepository<PaymentModel, String> {
    Optional<PaymentModel> findByPaypalOrderId(String paypalOrderId);
    Optional<PaymentModel> findByCaptureId(String captureId);
    List<PaymentModel> findByStatusOrderByCreatedAtDesc(String status);
}
```

---

## 8. `PayPalClient` — gọi PayPal REST bằng `RestClient`

Lớp này là **cầu nối duy nhất** tới PayPal. Nó lo 3 việc: **lấy access token** (OAuth2), **create order**, **capture order** (và **refund** ở §11).

### Bước 8.1 — Khởi tạo + lấy access token (có cache)

```java
@Component
public class PayPalClient {

    private final RestClient rest;
    private final PayPalProperties props;

    // PayPal access token sống ~9 giờ → cache lại, tránh xin token mỗi request
    private String cachedToken;
    private Instant tokenExpiry = Instant.EPOCH;

    public PayPalClient(PayPalProperties props) {
        this.props = props;
        this.rest = RestClient.builder().baseUrl(props.getBaseUrl()).build();
    }

    /** OAuth2 client_credentials: đổi clientId:secret → access token. */
    private synchronized String accessToken() {
        if (Instant.now().isBefore(tokenExpiry)) {
            return cachedToken;              // còn hạn → dùng lại
        }
        String basic = Base64.getEncoder().encodeToString(
                (props.getClientId() + ":" + props.getClientSecret())
                        .getBytes(StandardCharsets.UTF_8));

        Map<?, ?> res = rest.post()
                .uri("/v1/oauth2/token")
                .header(HttpHeaders.AUTHORIZATION, "Basic " + basic)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body("grant_type=client_credentials")
                .retrieve()
                .body(Map.class);

        this.cachedToken = (String) res.get("access_token");
        long expiresIn = ((Number) res.get("expires_in")).longValue();
        this.tokenExpiry = Instant.now().plusSeconds(expiresIn - 60); // trừ hao 60s
        return cachedToken;
    }
}
```

| Điểm dạy | Giải thích |
|----------|------------|
| `Basic base64(id:secret)` | Chuẩn OAuth2 client_credentials — auth bằng credential app |
| `application/x-www-form-urlencoded` | Body `grant_type=client_credentials` là **form**, không phải JSON |
| Cache token | Token sống ~9h; xin mới mỗi request là lãng phí + dễ bị rate-limit |
| `synchronized` | Tránh nhiều thread cùng xin token một lúc (đơn giản, đủ cho lab) |

**Vì sao cache được access token?**

PayPal trả `access_token` kèm trường `expires_in` (số giây token còn hiệu lực). Token này không gắn với **một giao dịch cụ thể**; nó chỉ chứng minh backend của mình đang gọi API bằng đúng `clientId/clientSecret` của app. Vì vậy trong thời gian token còn hạn, các request như **create order**, **capture**, **refund** đều có thể dùng lại cùng token.

```text
Lần 1: chưa có token → gọi /v1/oauth2/token → lưu cachedToken + tokenExpiry
Lần 2: token còn hạn → dùng lại cachedToken, không gọi /token nữa
Khi gần hết hạn → xin token mới
```

Ta trừ hao `60s` khi tính `tokenExpiry` để tránh trường hợp token vừa hết hạn trong lúc request đang chạy. Đây là cache **trong RAM của ứng dụng**: restart app thì mất cache và lần gọi đầu sẽ xin token lại — hoàn toàn bình thường.

### Bước 8.2 — Create order + Capture order

```java
    /** Tạo order CAPTURE. Trả về raw response của PayPal (Map). */
    public Map<String, Object> createOrder(BigDecimal amount, String currency, String referenceId) {
        Map<String, Object> body = Map.of(
            "intent", "CAPTURE",
            "purchase_units", List.of(Map.of(
                "reference_id", referenceId,                  // để đối soát với hệ thống mình
                "amount", Map.of(
                    "currency_code", currency,
                    "value", amount.toPlainString()          // "9.99"
                )
            ))
        );

        return rest.post()
                .uri("/v2/checkout/orders")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken())
                .contentType(MediaType.APPLICATION_JSON)
                .body(body)
                .retrieve()
                .body(Map.class);
    }

    /** Capture tiền cho order đã được người mua duyệt. */
    public Map<String, Object> captureOrder(String orderId) {
        return rest.post()
                .uri("/v2/checkout/orders/{id}/capture", orderId)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken())
                .contentType(MediaType.APPLICATION_JSON)
                .body("{}")                                   // body rỗng
                .retrieve()
                .body(Map.class);
    }
```

| Endpoint | Method | Ý nghĩa |
|----------|--------|---------|
| `/v1/oauth2/token` | POST | Lấy access token |
| `/v2/checkout/orders` | POST | Tạo order (trả `id`, `status=CREATED`) |
| `/v2/checkout/orders/{id}/capture` | POST | Thu tiền (trả `status=COMPLETED` + `captures[].id`) |

> **Thử nhanh bằng cURL** để hiểu API trước khi code (dán token vào):
> ```bash
> curl -X POST https://api-m.sandbox.paypal.com/v1/oauth2/token \
>   -u "CLIENT_ID:SECRET" \
>   -d "grant_type=client_credentials"
> ```

---

## 9. `PaymentService` + Controller

### Bước 9.1 — DTO vào/ra (FE không gửi amount)

```java
// Request từ FE: CHỈ có movieId
public record CreateOrderRequest(String movieId) {}

// Response trả FE
public record PaymentResultDto(String orderId, String status, String captureId) {}
```

### Bước 9.2 — `PaymentService` (amount quyết định ở server)

```java
@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PayPalClient paypal;
    private final PaymentRepository paymentRepository;
    private final MovieRepository movieRepository;
    private final PayPalProperties props;

    /** Giá thuê 1 phim — quyết định ở SERVER, không nhận từ FE. */
    private BigDecimal priceOf(MovieModel movie) {
        // Lab: lấy từ field rentalPrice của phim (demo seed sẵn giá).
        return movie.getRentalPrice();
    }

    /** Bước 1: tạo order + lưu Payment(status=CREATED). */
    public String createOrder(String movieId) {
        MovieModel movie = movieRepository.findById(movieId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy phim: " + movieId));

        BigDecimal amount = priceOf(movie);
        Map<String, Object> order = paypal.createOrder(amount, props.getCurrency(), movieId);
        String orderId = (String) order.get("id");

        PaymentModel p = new PaymentModel();
        p.setMovieId(movieId);
        p.setPaypalOrderId(orderId);
        p.setAmount(amount);
        p.setCurrency(props.getCurrency());
        p.setStatus("CREATED");
        p.setCreatedAt(Instant.now());
        p.setUpdatedAt(Instant.now());
        paymentRepository.save(p);

        return orderId;
    }

    /** Bước 2: capture + cập nhật trạng thái. */
    public PaymentResultDto capture(String orderId) {
        PaymentModel p = paymentRepository.findByPaypalOrderId(orderId)
                .orElseThrow(() -> new IllegalStateException("Không tìm thấy payment cho order: " + orderId));

        Map<String, Object> res = paypal.captureOrder(orderId);
        String status = (String) res.get("status");   // kỳ vọng "COMPLETED"

        if ("COMPLETED".equals(status)) {
            p.setStatus("COMPLETED");
            p.setCaptureId(extractCaptureId(res));
        } else {
            p.setStatus("FAILED");
        }
        p.setUpdatedAt(Instant.now());
        paymentRepository.save(p);

        return new PaymentResultDto(orderId, p.getStatus(), p.getCaptureId());
    }

    /** Bóc captureId từ response: purchase_units[0].payments.captures[0].id */
    @SuppressWarnings("unchecked")
    private String extractCaptureId(Map<String, Object> res) {
        var units    = (List<Map<String, Object>>) res.get("purchase_units");
        var payments = (Map<String, Object>) units.get(0).get("payments");
        var captures = (List<Map<String, Object>>) payments.get("captures");
        return (String) captures.get(0).get("id");
    }
}
```

### Bước 9.3 — `PaymentRestController`

```java
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/payments")
public class PaymentRestController {

    private final PaymentService paymentService;

    @PostMapping("/orders")
    public Map<String, String> create(@RequestBody CreateOrderRequest req) {
        String orderId = paymentService.createOrder(req.movieId());
        return Map.of("id", orderId);       // FE cần { id }
    }

    @PostMapping("/orders/{orderId}/capture")
    public PaymentResultDto capture(@PathVariable String orderId) {
        return paymentService.capture(orderId);
    }
}
```

| Endpoint | Vai trò | Ai gọi |
|----------|---------|--------|
| `POST /api/payments/orders` | Tạo order, trả `{ id }` | `createOrder()` của Smart Button |
| `POST /api/payments/orders/{id}/capture` | Capture + lưu kết quả | `onApprove()` của Smart Button |

---

## 10. Frontend — Smart Buttons trên trang chi tiết phim

Trong demo: `templates/movies/detail.html`.

**Lưu ý quan trọng:**

- Client ID lấy từ server (`paypal.client-id`) — **public**, được phép để trên FE.
- URL SDK phải dùng `&amp;` (không viết `&` trần) kẻo HTML cắt query và SDK không load → **không thấy nút**.
- Nếu chưa dán Client ID/Secret thật, trang hiện **cảnh báo đỏ** thay vì nút trống.

```html
<!-- Chỉ load khi đã cấu hình credential thật -->
<script th:if="${paypalConfigured}"
        th:src="|https://www.paypal.com/sdk/js?client-id=${paypalClientId}&amp;currency=${paypalCurrency}|"></script>
<script th:if="${paypalConfigured}" th:inline="javascript">
    const movieId = /*[[${movie.id}]]*/ '';

    async function createOrderOnServer() {
        const response = await fetch('/api/payments/orders', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ movieId: movieId })   // chỉ gửi movieId
        });
        const data = await response.json();
        if (!response.ok) throw new Error(data.error || 'Tạo order thất bại');
        return data.id;
    }

    async function captureOrderOnServer(orderId) {
        const response = await fetch('/api/payments/orders/' + orderId + '/capture', { method: 'POST' });
        const data = await response.json();
        if (!response.ok) throw new Error(data.error || 'Capture thất bại');
        return data;
    }

    document.addEventListener('DOMContentLoaded', function () {
        paypal.Buttons({
            createOrder: () => createOrderOnServer(),
            onApprove: async (data) => {
                const payment = await captureOrderOnServer(data.orderID);
                // Hiện kết quả; bản ghi đã lưu trong Mongo collection payments
                alert('Thanh toán ' + payment.status + ' — ' + payment.captureId);
            },
            onCancel: async (data) => {
                if (data.orderID) {
                    await fetch('/api/payments/orders/' + data.orderID + '/cancel', { method: 'POST' });
                }
            },
            onError: (err) => console.error(err)
        }).render('#paypal-button-container');
    });
</script>
```

Controller truyền thêm cờ cấu hình:

```java
model.addAttribute("paypalConfigured", payPalProperties.isConfigured());
model.addAttribute("paypalClientId", payPalProperties.getClientId());
model.addAttribute("paypalCurrency", payPalProperties.getCurrency());
```

**Kiểm tra (PayPal Sandbox thật):**

1. Dán Client ID / Secret thật vào `application.properties`, **restart** app.
2. Mở `/movies/{id}` → thấy nút PayPal vàng (không còn cảnh báo đỏ).
3. Bấm nút → popup PayPal → đăng nhập **buyer sandbox** → duyệt.
4. Trang hiện `COMPLETED` + `captureId`.
5. Mongo: `use db_java_t3h_module3` rồi `db.payments.find()` — có document.
6. Hoặc mở `/payments` trên web.

> **Cách legacy (chỉ nhắc, không dùng):** PayPal còn kiểu **"Buy Now" HTML button**. Cách đó **không** lưu trạng thái ở backend → **không dùng**.

---

## 11. Refund — hoàn tiền

Thêm vào `PayPalClient`:

```java
    /** Hoàn toàn bộ tiền cho 1 capture. */
    public Map<String, Object> refund(String captureId) {
        return rest.post()
                .uri("/v2/payments/captures/{id}/refund", captureId)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken())
                .contentType(MediaType.APPLICATION_JSON)
                .body("{}")     // body rỗng = hoàn toàn bộ; muốn hoàn 1 phần thì truyền amount
                .retrieve()
                .body(Map.class);
    }
```

Service:

```java
    public void refund(String paymentId) {
        PaymentModel p = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy payment"));
        if (!"COMPLETED".equals(p.getStatus())) {
            throw new IllegalStateException("Chỉ hoàn tiền giao dịch đã COMPLETED");
        }
        paypal.refund(p.getCaptureId());     // dùng captureId đã lưu
        p.setStatus("REFUNDED");
        p.setUpdatedAt(Instant.now());
        paymentRepository.save(p);
    }
```

> Đây là lý do **phải lưu `captureId`** ở §7: không có nó thì không refund được.

---

## 12. Webhook (nâng cao — vì sao production cần)

**Vấn đề:** người mua có thể **đóng tab** ngay sau khi trả tiền, trước khi `onApprove` kịp gọi capture. Nếu chỉ dựa vào FE, bạn sẽ **mất dấu** giao dịch.

**Giải pháp:** PayPal gửi **webhook** (HTTP POST) tới server bạn mỗi khi có sự kiện (`PAYMENT.CAPTURE.COMPLETED`, `PAYMENT.CAPTURE.REFUNDED`…). Server cập nhật trạng thái **độc lập với trình duyệt**.

```java
@RestController
@RequestMapping("/api/webhooks/paypal")
public class PayPalWebhookController {

    private final PaymentService paymentService;

    public PayPalWebhookController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping
    public ResponseEntity<Void> handle(@RequestBody Map<String, Object> event,
                                       @RequestHeader Map<String, String> headers) {
        // 1) BẮT BUỘC production: verify chữ ký webhook trước khi tin
        //    POST /v1/notifications/verify-webhook-signature (dùng headers + webhook_id)
        // 2) Xử lý IDEMPOTENT theo event id (đã xử lý rồi thì bỏ qua)
        String type = (String) event.get("event_type");
        if ("PAYMENT.CAPTURE.COMPLETED".equals(type)) {
            // paymentService.markCompletedFromWebhook(event);
        }
        return ResponseEntity.ok().build();   // trả 200 để PayPal ngừng retry
    }
}
```

| Yêu cầu webhook production | Vì sao |
|---------------------------|--------|
| **Verify signature** | Tránh kẻ gian giả webhook để đánh dấu "đã trả tiền" |
| **Idempotent** | PayPal có thể gửi lặp; xử lý 2 lần không được sinh 2 kết quả |
| **Trả 200 nhanh** | Không xử lý nặng trong request; nếu lỗi PayPal sẽ retry |

> Trong lab local, webhook cần URL public → dùng `ngrok` để expose `localhost`. Có thể để phần này là **đọc hiểu/bonus**.

---

## 13. Bảo mật thanh toán (checklist đúng)

| Nguyên tắc | Cách làm cụ thể |
|-----------|-----------------|
| **Secret chỉ ở backend** | `PAYPAL_CLIENT_SECRET` trong env var; **không** lên FE; **không** commit; thêm vào `.gitignore` |
| **Amount do server quyết** | Tra giá theo `movieId` trong `PaymentService`; **bỏ qua** mọi số tiền từ FE |
| **Create & capture ở server** | FE chỉ nhận `orderId`; không tự capture ở client |
| **Verify kết quả** | Kiểm `status == COMPLETED` + amount/currency khớp order nội bộ **trước khi** cho xem phim |
| **Không lưu thẻ/CVV/password** | PayPal xử lý — đây là ý nghĩa **PCI-DSS**: đừng để dữ liệu thẻ chạm server bạn |
| **Luôn lưu & log trạng thái** | Giả định mạng có thể đứt giữa chừng; có bản ghi để đối soát |
| **Webhook có chữ ký + idempotent** | Xem §12 |
| **HTTPS ở production** | Bảo vệ dữ liệu trên đường truyền |

> **Đính chính quan niệm sai:** "dùng **CORS** để bảo mật thanh toán" là **không chính xác**. CORS chỉ là chính sách trình duyệt (kiểm soát origin gọi API), **không** chống gian lận số tiền. Cơ chế bảo vệ tiền là: amount server-side + verify status + webhook signature.

### Bước 13.1 — Chuyển secret ra biến môi trường

Ở §6 ta đã điền thẳng Client ID/Secret vào `application.properties` cho dễ chạy. **Trước khi commit / lên production**, chuyển sang **biến môi trường** để secret không nằm trong mã nguồn:

```properties
# application.properties — thay giá trị thật bằng placeholder ${...}
paypal.base-url=https://api-m.sandbox.paypal.com
paypal.client-id=${PAYPAL_CLIENT_ID}
paypal.client-secret=${PAYPAL_CLIENT_SECRET}
paypal.currency=USD
```

```bash
# Đặt biến môi trường trước khi chạy app
export PAYPAL_CLIENT_ID="AeA1QIZ...client-id-sandbox"
export PAYPAL_CLIENT_SECRET="EJxxxx...secret-sandbox"
```

> IntelliJ/Cursor: đặt trong **Run/Debug Configurations → Environment variables**.
> Thêm `.env` / file chứa secret vào `.gitignore`. **So sánh với §6 để học viên thấy rõ**: cùng một app, chỉ khác chỗ lưu secret — nhưng an toàn hơn hẳn.

---

## 14. Unit test `PaymentService` (nối tiếp Bài 8)

Test logic thanh toán **không gọi PayPal thật** bằng cách **mock `PayPalClient`** — đúng tinh thần Bài 8 (§4: `@Mock`/`@InjectMocks`).

```java
@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock PayPalClient paypal;
    @Mock PaymentRepository paymentRepository;
    @Mock MovieRepository movieRepository;
    @Mock PayPalProperties props;

    @InjectMocks PaymentService paymentService;

    @Test
    void createOrder_luuTrangThai_CREATED() {
        MovieModel movie = new MovieModel();
        movie.setId("m1");
        movie.setTitle("Inception");

        when(props.getCurrency()).thenReturn("USD");
        when(movieRepository.findById("m1")).thenReturn(Optional.of(movie));
        when(paypal.createOrder(any(), eq("USD"), eq("m1")))
                .thenReturn(Map.of("id", "PP-ORDER-123", "status", "CREATED"));

        String orderId = paymentService.createOrder("m1");

        assertThat(orderId).isEqualTo("PP-ORDER-123");
        verify(paymentRepository).save(argThat(p ->
                p.getStatus().equals("CREATED") &&
                p.getPaypalOrderId().equals("PP-ORDER-123") &&
                p.getAmount().compareTo(new BigDecimal("9.99")) == 0));
    }

    @Test
    void createOrder_movieKhongTonTai_nemException() {
        when(movieRepository.findById("x")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> paymentService.createOrder("x"))
                .isInstanceOf(IllegalArgumentException.class);

        verify(paypal, never()).createOrder(any(), any(), any());  // không gọi PayPal
    }

    @Test
    void capture_completed_luuCaptureId() {
        PaymentModel existing = new PaymentModel();
        existing.setPaypalOrderId("PP-ORDER-123");
        existing.setStatus("CREATED");

        when(paymentRepository.findByPaypalOrderId("PP-ORDER-123"))
                .thenReturn(Optional.of(existing));
        when(paypal.captureOrder("PP-ORDER-123")).thenReturn(Map.of(
                "status", "COMPLETED",
                "purchase_units", List.of(Map.of(
                        "payments", Map.of(
                                "captures", List.of(Map.of("id", "CAP-999")))))));

        PaymentResultDto result = paymentService.capture("PP-ORDER-123");

        assertThat(result.status()).isEqualTo("COMPLETED");
        assertThat(result.captureId()).isEqualTo("CAP-999");
    }
}
```

| Điểm dạy | Giải thích |
|----------|------------|
| Mock `PayPalClient` | Test **không** cần mạng/PayPal → nhanh, ổn định |
| `verify(paypal, never())...` | Chứng minh nhánh lỗi **không** gọi PayPal (tránh tạo order rác) |
| `argThat(...)` | Bắt object `save(...)` để assert `status`/`amount` (Bài 8 §9.6) |

---

## 15. Lỗi thường gặp

| Triệu chứng | Nguyên nhân | Cách xử lý |
|-------------|-------------|------------|
| Nút PayPal không hiện | Sai `client-id` ở FE, hoặc lệch `currency` FE/BE | Dùng đúng Sandbox Client ID; đồng bộ `USD` |
| `401 Unauthorized` khi gọi API | Token hết hạn / sai secret / thiếu `Bearer ` | Kiểm secret env; cache token có trừ hao thời gian |
| `CURRENCY_NOT_SUPPORTED` | Đặt `VND` | Sandbox dùng `USD` |
| Capture xong tiền không về | Dùng buyer account để capture thay vì để user duyệt qua popup | Tách vai trò: user duyệt → server capture |
| `payments` không có bản ghi | Lỗi trước khi `save`, hoặc sai URI Mongo | Kiểm log; `db.payments.find()` |
| `captureId` null | Bóc sai đường dẫn trong response | Đúng path `purchase_units[0].payments.captures[0].id` |
| Số tiền sai lệch | Dùng `double` cho tiền | Dùng `BigDecimal` + `toPlainString()` |
| Secret lộ trên Git | Hardcode trong `application.properties` | Chuyển sang env var + `.gitignore` |
| Trả tiền 2 lần khi F5 | Không idempotent | Kiểm trạng thái trước khi capture; dùng webhook |
| Webhook bị giả mạo | Không verify signature | `verify-webhook-signature` trước khi tin |

---

## Tóm tắt

| Khái niệm | Ý chính |
|-----------|---------|
| Gateway vs Processor | Gateway truyền an toàn; Processor định tuyến tới NH |
| PayPal vs VNPay/MoMo | Quốc tế vs nội địa; tư duy luồng giống nhau |
| Sandbox | Miễn phí, tiền ảo, dùng được ở VN để học |
| Orders v2 | Create → (user duyệt) → Capture, **đều ở server** |
| `RestClient` + OAuth2 | `client_credentials` → Bearer token (cache ~9h) |
| Amount server-side | FE chỉ gửi `movieId`; **không** gửi tiền |
| `payments` collection | Lưu `paypalOrderId`, `captureId`, `status`, `amount` (BigDecimal) |
| Refund | Cần `captureId` đã lưu |
| Webhook | Production **bắt buộc**; verify signature + idempotent |
| Bảo mật | Secret env, verify status, không lưu thẻ (PCI-DSS) |
| Test | Mock `PayPalClient` (Bài 8) |

---

## Phụ lục

### Bài tập

1. **Cơ bản (bắt buộc):** hoàn thiện luồng create → capture cho 1 phim; sau khi `COMPLETED`, cho phép mở trang watching. Kiểm tra document trong `payments`.
2. **Giá theo loại phim:** đổi `priceOf()` — Movie $9.99, TV Show $14.99 (đọc field `type`/`listed_in` của Bài 7). Viết test `@ParameterizedTest` cho `priceOf`.
3. **Trang lịch sử thanh toán:** `GET /payments` (Thymeleaf) liệt kê giao dịch từ `payments`, mới nhất trước.
4. **Refund từ UI:** thêm nút "Hoàn tiền" gọi service §11; sau refund trạng thái phải là `REFUNDED`.
5. **Test (nối Bài 8):** viết `PaymentServiceTest` — ít nhất 3 case: create OK, movie không tồn tại (`verify never`), capture `COMPLETED` lưu `captureId`.
6. **(Nâng cao — đọc hiểu):** viết 8–12 dòng mô tả luồng tích hợp **VNPay** hoặc **MoMo** sandbox và so sánh với PayPal (điểm giống/khác).
7. **(Nâng cao — bonus):** dựng endpoint webhook + `ngrok`, cấu hình webhook trên PayPal dashboard, nhận `PAYMENT.CAPTURE.COMPLETED`.

### Checklist nộp bài

- [ ] Đăng ký được Sandbox app, có Client ID + Secret + buyer test
- [ ] Secret đặt bằng **env var**, không commit (có trong `.gitignore`)
- [ ] `PaymentModel` (suffix `Model`) + collection `payments`; tiền dùng `BigDecimal`
- [ ] `PayPalClient` lấy token (cache) + create + capture bằng `RestClient`
- [ ] Amount **quyết định ở server** theo `movieId`; FE **không** gửi amount
- [ ] Nút Smart Button trên trang detail; trả tiền bằng buyer sandbox → `COMPLETED`
- [ ] Document `payments` có `paypalOrderId`, `captureId`, `status`
- [ ] Có refund dùng `captureId` (→ `REFUNDED`)
- [ ] Có `PaymentServiceTest` mock `PayPalClient` (không gọi mạng)
- [ ] Giải thích được vì sao production cần webhook + không lưu thẻ

### Liên kết tham khảo

- [PayPal Developer Dashboard](https://developer.paypal.com/dashboard/)
- [Orders API v2 — Integration](https://developer.paypal.com/api/rest/integration/orders-api/)
- [PayPal Checkout — Quick start](https://developer.paypal.com/checkout/integrate)
- [Orders v2 — API reference](https://developer.paypal.com/docs/api/orders/v2/)
- [Payments v2 — Refund capture](https://developer.paypal.com/docs/api/payments/v2/#captures_refund)
- [PayPal JavaScript SDK — Buttons](https://developer.paypal.com/sdk/js/reference/)
- [PayPal Webhooks](https://developer.paypal.com/api/rest/webhooks/)
- [Spring Framework — RestClient](https://docs.spring.io/spring-framework/reference/integration/rest-clients.html#rest-restclient)
- [VNPay — Cổng thanh toán](https://sandbox.vnpayment.vn/apis/) · [MoMo Developers](https://developers.momo.vn/)
- [Bài 7 — Database Query To FrontEnd](./java_m3_bai7_Database_Query_To_FrontEnd.md) · [Bài 8 — Unit Testing](./java_m3_bai8_Unit_Testing.md)
- **Demo chuẩn (khi có):** [`demo-bai9-online-payment`](../../demo-bai9-online-payment)
- **Tiếp theo:** [Bài 10 — Mini Project](./java_m3_bai10_Mini_Project.md)
