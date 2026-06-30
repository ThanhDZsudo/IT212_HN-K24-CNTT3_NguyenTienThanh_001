# TỔNG HỢP BÁO CÁO PHÂN TÍCH & THIẾT KẾ HỆ THỐNG - RIKKEI LOGISTICS PLATFORM
Tài liệu này tổng hợp toàn bộ các nhiệm vụ phân tích thiết kế hệ thống và giải pháp lập trình đã thực hiện trong quá trình phát triển nền tảng Rikkei Logistics cùng bộ xử lý thanh toán OCP và giải pháp bắt lỗi bảo mật tập trung.

---

## PHẦN 1: TÁI CẤU TRÚC HỆ THỐNG ĐỂ DỄ MỞ RỘNG (OCP)

### 1.1. Yêu cầu gốc từ người dùng (Prompt Copy)
> **Vấn đề:** Đoạn mã nguồn xử lý thanh toán dưới đây đang vi phạm nghiêm trọng các nguyên tắc thiết kế phần mềm (đặc biệt là Open/Closed Principle). Hiện tại, mỗi lần marketing tung ra một loại mã giảm giá mới, hoặc công ty tích hợp thêm cổng thanh toán mới (như ZaloPay, ApplePay), team Dev đều phải sửa trực tiếp vào hàm checkout, dẫn đến rủi ro hỏng logic cũ.
>
> **Mục tiêu bắt buộc:** Mã nguồn mới phải đảm bảo rằng việc thêm một loại Voucher mới, thêm một Payment Method mới, hoặc thay đổi hình thức thông báo (từ Email sang SMS) sẽ không cần chạm vào hay sửa đổi hàm tính toán cốt lõi.

### 1.2. Tóm tắt giải pháp kỹ thuật
Xây dựng giải pháp OCP bằng Strategy Pattern và Dependency Injection:
* **Interface hoá các thành phần hay thay đổi:** Tạo các interface `VoucherStrategy`, `PaymentProcessor`, và `NotificationService` để trừu tượng hóa các nghiệp vụ giảm giá, thanh toán và thông báo.
* **Gộp cấu trúc mã nguồn phẳng gọn gàng:** Triển khai toàn bộ interface và lớp hiện thực cụ thể (`VipVoucherStrategy`, `FreeshipVoucherStrategy`, `MomoPaymentProcessor`, `VnPayPaymentProcessor`, `EmailNotificationService`, `SmsNotificationService`) chung một file duy nhất tại [OrderService.java](file:///e:/BaiTap/IT212_UngDungAI/HN-K24-CNTT3_NguyenTienThanh_001/src/refactoring/OrderService.java).
* **Cơ chế hoạt động:** Lớp `OrderService` lõi nhận danh sách các strategy thông qua Constructor Injection của Spring. Hàm `checkout` chỉ điều phối việc duyệt qua danh sách các strategy này để tự áp dụng logic mà không cần sửa code cũ khi có loại voucher, cổng thanh toán hoặc kênh thông báo mới được thêm vào.

---

## PHẦN 2: DEBUGGING BẢO MẬT VÀ XỬ LÝ LỖI HỆ THỐNG (JWT EXPIRED)

### 2.1. Yêu cầu gốc từ người dùng (Prompt Copy)
> **Sự cố:** Hệ thống backend của dự án đang gặp sự cố. Thay vì trả về mã lỗi HTTP 401 Unauthorized cùng thông báo JSON chuẩn mực khi người dùng sử dụng token đã hết hạn, server lại bị crash và quăng ra lỗi HTTP 500 Internal Server Error.
>
> **Mục tiêu bắt buộc:** Tìm ra giải pháp để bắt lỗi này một cách tập trung, ở tầng cao nhất của ứng dụng, đảm bảo mọi lỗi liên quan đến xác thực đều trả về một format JSON đồng nhất (VD: `{"error": "AUTH_FAILED", "message": "..."}`).
>
> **Chứng minh:** Đưa ra các đoạn code giải pháp và giải thích tại sao không nên chỉ dùng try-catch đơn thuần bên trong hàm Filter.

### 2.2. Tóm tắt giải pháp kỹ thuật
Xây dựng bộ xử lý ngoại lệ tập trung ở tầng Filter:
* **ExceptionHandlerFilter:** Tạo lớp [ExceptionHandlerFilter.java](file:///e:/BaiTap/IT212_UngDungAI/HN-K24-CNTT3_NguyenTienThanh_001/src/security/ExceptionHandlerFilter.java) đứng trước [JwtAuthenticationFilter.java](file:///e:/BaiTap/IT212_UngDungAI/HN-K24-CNTT3_NguyenTienThanh_001/src/security/JwtAuthenticationFilter.java) trong chuỗi Filter Chain. Filter này bắt toàn bộ ngoại lệ như `ExpiredJwtException`, `JwtException` và tự động ghi phản hồi JSON đồng nhất với mã trạng thái HTTP 401.
* **Security Config:** Cấu hình liên kết các bộ lọc tập trung trong [SecurityConfig.java](file:///e:/BaiTap/IT212_UngDungAI/HN-K24-CNTT3_NguyenTienThanh_001/src/security/SecurityConfig.java).
* **Tại sao không nên dùng try-catch đơn thuần trong JwtAuthenticationFilter:**
  1. *Vi phạm nguyên lý Single Responsibility (SRP):* Filter xác thực chỉ nên làm nhiệm vụ kiểm tra token, không nên ôm đồm cả việc phân tích lỗi và serialize JSON để phản hồi.
  2. *Thiếu tính tập trung:* Nếu hệ thống mở rộng thêm các bộ lọc xác thực khác (API Key, OAuth2, v.v.), ta phải viết lặp lại logic try-catch và ghi phản hồi JSON ở rất nhiều nơi.

---

## PHẦN 3: THIẾT KẾ HỆ THỐNG RIKKEI LOGISTICS

### 3.1. Bối cảnh dự án (Prompt Copy)
> **Bối cảnh dự án:** Một startup về giao hàng siêu tốc vừa tìm đến công ty bạn để đặt hàng xây dựng một nền tảng công nghệ toàn diện có tên "Rikkei Logistics". Nền tảng này cần có một ứng dụng Mobile dành cho khách hàng/tài xế và một hệ thống Web Admin dành cho nhân viên điều phối. Khách hàng đưa ra các nghiệp vụ cốt lõi sau:
> 1. Quản lý người dùng: Hệ thống có 3 role chính: Khách hàng (Thường & VIP), Tài xế, và Quản trị viên.
> 2. Nghiệp vụ tính phí giao hàng phức tạp: Phí quãng đường (5km đầu là 40.000 VNĐ, từ km thứ 6 cộng thêm 5.000 VNĐ/km). Phụ phí trọng lượng (Dưới 10kg miễn phí, từ 10kg - 30kg phụ phí 20%, trên 30kg phụ phí cố định 100.000 VNĐ). Đặc quyền VIP (Khách VIP miễn 100% phí quãng đường nhưng chịu phụ phí trọng lượng). Mã giảm giá (Giảm tối đa 50.000 VNĐ, không áp dụng cho hàng > 30kg).
> 3. Theo dõi đơn hàng (Tracking): Trạng thái đơn hàng phải được cập nhật theo thời gian thực (Real-time) trên app của khách hàng.

### 3.2. Nhiệm vụ 1: Đề xuất giải pháp công nghệ (Tech Stack)
* **A. Yêu cầu nhiệm vụ (Prompt Copy):**
  > *Viết prompt yêu cầu AI đề xuất một bộ Tech Stack phù hợp cho các yêu cầu của khách hàng nêu trên và đưa ra lý do thuyết phục cho khách hàng.*
* **B. Tóm tắt giải pháp công nghệ:**
  - **Backend:** Java Spring Boot (xử lý nghiệp vụ chính, tính phí, phân quyền) kết hợp Node.js/NestJS (xử lý cổng kết nối realtime WebSocket/Socket.io nhận vị trí GPS tài xế).
  - **Database:** PostgreSQL tích hợp extension **PostGIS** để tối ưu hóa lưu trữ và truy vấn không gian địa lý (khoảng cách tọa độ giữa tài xế và khách hàng) + Caching Redis.
  - **Frontend:** **Flutter** (Mobile đa nền tảng cho Khách hàng & Tài xế) và **React.js** (Web Admin quản lý điều phối).
* **C. Nhận xét phản biện từ Chuyên viên phân tích hệ thống (SA):**
  - *Ưu điểm:* Việc kết hợp Spring Boot và Node.js giúp cô lập dịch vụ realtime tracking có tần suất I/O cực cao ra khỏi luồng thanh toán và tính phí tài chính quan trọng của Spring Boot, giúp tối ưu hiệu năng và tài nguyên.
  - *Rủi ro & Giảm thiểu:* Khách hàng có thể e ngại việc vận hành 2 công nghệ backend. Giải pháp là đóng gói chúng dưới dạng Docker containers để dễ dàng quản lý đồng bộ.

### 3.3. Nhiệm vụ 2: Phân tích thực thể (Entity Analysis)
* **A. Yêu cầu nhiệm vụ (Prompt Copy):**
  > *Viết prompt yêu cầu AI bóc tách nghiệp vụ để xác định các thực thể (Entities) cốt lõi của Database và các thuộc tính quan trọng.*
* **B. Danh sách thực thể cốt lõi (Entities) đã xác định:**
  1. `User`: Tài khoản người dùng (id, username, password_hash, email, phone, role, customer_type, status).
  2. `DriverDetail`: Chi tiết phương tiện tài xế (id, user_id, license_number, vehicle_type, plate_number, is_active).
  3. `DeliveryOrder`: Đơn giao hàng (id, customer_id, driver_id, pickup_address, delivery_address, distance, weight, base_distance_fee, weight_surcharge, discount_amount, total_fee, status, created_at).
  4. `Voucher`: Mã khuyến mãi (code, discount_value, max_discount, min_order_weight_limit, expiry_date, usage_limit).
  5. `TrackingLog`: Tọa độ di chuyển thời gian thực (id, order_id, latitude, longitude, timestamp).
  6. `Payment`: Giao dịch thanh toán (id, order_id, payment_method, amount, status, transaction_id, created_at).
* **C. Giải pháp thiết kế cho các nghiệp vụ đặc thù:**
  - Mối quan hệ 1-N giữa `DeliveryOrder` và `TrackingLog` cho phép lưu trữ lịch sử lộ trình của tài xế mà không gây chậm DB chính nhờ việc ghi nhận liên tục qua luồng xử lý bất đồng bộ.

### 3.4. Nhiệm vụ 3: Thiết kế sơ đồ quan hệ thực thể ERD
* **A. Yêu cầu nhiệm vụ (Prompt Copy):**
  > *Viết prompt yêu cầu AI tạo ra mã vẽ sơ đồ ERD (định dạng Mermaid hoặc PlantUML) dựa trên các thực thể đã chốt.*
* **B. Sơ đồ ERD trực quan hóa:**
  Sơ đồ ERD đã được xuất ra thành file hình ảnh nằm trong thư mục [docs/erd_diagram.png](file:///e:/BaiTap/IT212_UngDungAI/HN-K24-CNTT3_NguyenTienThanh_001/docs/erd_diagram.png).
* **C. Mã nguồn Mermaid:**
  ```mermaid
  erDiagram
      USER ||--o| DRIVER_DETAIL : "has details if driver"
      USER ||--o{ DELIVERY_ORDER : "creates as customer"
      USER ||--o{ DELIVERY_ORDER : "delivers as driver"
      DELIVERY_ORDER ||--o{ TRACKING_LOG : "has movement logs"
      DELIVERY_ORDER ||--|| PAYMENT : "has payment transaction"
      DELIVERY_ORDER }o--o| VOUCHER : "applies"

      USER {
          uuid id PK
          string username
          string password_hash
          string email
          string phone
          string role
          string customer_type
          int status
          timestamp created_at
      }

      DRIVER_DETAIL {
          uuid id PK
          uuid user_id FK
          string license_number
          string vehicle_type
          string plate_number
          boolean is_active
      }

      DELIVERY_ORDER {
          uuid id PK
          uuid customer_id FK
          uuid driver_id FK
          string pickup_address
          string delivery_address
          double distance
          double weight
          double base_distance_fee
          double weight_surcharge
          double discount_amount
          double total_fee
          string voucher_code FK
          string status
          timestamp created_at
      }

      VOUCHER {
          string code PK
          double discount_value
          double max_discount
          double min_order_weight_limit
          timestamp expiry_date
          int usage_limit
      }

      TRACKING_LOG {
          uuid id PK
          uuid order_id FK
          double latitude
          double longitude
          timestamp timestamp
      }

      PAYMENT {
          uuid id PK
          uuid order_id FK
          string payment_method
          double amount
          string status
          string transaction_id
          timestamp created_at
      }
  ```
