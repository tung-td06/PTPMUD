# Hướng dẫn Chạy Dự án & Cấu hình Hệ thống (Restaurant App)

Dự án này là một hệ thống quản lý nhà hàng dựa trên mô hình **Client-Server** sử dụng Socket để giao tiếp giữa Server và các Client JavaFX. Dự án gồm có 4 phân hệ chính:
1. **ServerRestaurant**: Máy chủ xử lý dữ liệu và kết nối cơ sở dữ liệu (MS SQL Server).
2. **Restaurant-Admin**: Ứng dụng dành cho Quản trị viên (quản lý món ăn, nhân viên, kho, ca làm việc...).
3. **Restaurant-Customer**: Ứng dụng dành cho Khách hàng (gọi món, đặt bàn...).
4. **Restaurant-Employee**: Ứng dụng dành cho Nhân viên (xử lý hóa đơn, kiểm tra ca làm, quản lý đặt bàn...).

---

## 🛠️ Hướng dẫn Cấu hình Hệ thống

### 1. Đổi Địa chỉ IP Kết nối (Client Socket)
Mặc định, các ứng dụng Client (`Admin`, `Customer`, `Employee`) đang được cấu hình kết nối tới Server tại `localhost` (chạy trên cùng một máy tính).

Nếu bạn chạy ứng dụng Server trên một máy tính và các ứng dụng Client trên các máy tính khác trong cùng mạng LAN/Wi-Fi:
- Bạn cần tìm địa chỉ IP LAN/Wi-Fi của máy chủ (ví dụ: `192.168.1.x`).
- Thay đổi hằng số `SERVER_IP` trong tệp cấu hình của từng phân hệ Client:

* **Restaurant-Admin**:
  - Tệp: [SocketClient.java](file:///d:/GiaoDienAPP/MonHoc/DUAN%20-%20goc/Restaurant-Admin/src/main/java/network/SocketClient.java#L12)
  - Dòng: `public static final String SERVER_IP = "localhost";`
* **Restaurant-Customer**:
  - Tệp: [SocketClient.java](file:///d:/GiaoDienAPP/MonHoc/DUAN%20-%20goc/Restaurant-Customer/src/main/java/network/SocketClient.java#L12)
  - Dòng: `public static final String SERVER_IP = "localhost";`
* **Restaurant-Employee**:
  - Tệp: [SocketClient.java](file:///d:/GiaoDienAPP/MonHoc/DUAN%20-%20goc/Restaurant-Employee/src/main/java/network/SocketClient.java#L11)
  - Dòng: `private static final String SERVER_IP = "localhost";`

> [!IMPORTANT]
> Hãy chắc chắn rằng tất cả các Client và Server cùng kết nối chung một mạng Wi-Fi/LAN và Windows Firewall trên máy Server đã mở cổng `9999` để cho phép các Client kết nối vào.

---

### 2. Cấu hình Cơ sở dữ liệu (SQL Server)
Ứng dụng Server kết nối tới cơ sở dữ liệu Microsoft SQL Server. Cấu hình nằm tại:
- Tệp: [DBConnection.java](file:///d:/GiaoDienAPP/MonHoc/DUAN%20-%20goc/ServerRestaurant/src/main/java/database/DBConnection.java#L9-L17)

Cấu hình mặc định:
- **Database Name**: `QuanLiNhaHang`
- **Port**: `1433`
- **Username**: `sa`
- **Password**: `123456`

Nếu thông tin đăng nhập SQL Server của bạn khác với cấu hình trên, hãy mở tệp [DBConnection.java](file:///d:/GiaoDienAPP/MonHoc/DUAN%20-%20goc/ServerRestaurant/src/main/java/database/DBConnection.java) và thay đổi giá trị của `USER` và `PASSWORD`.

---

## ⚠️ Các Vấn đề thường gặp (Troubleshooting)

### 1. Lỗi không kết nối được Cơ sở dữ liệu (SQL Server Connection Error)
* **Triệu chứng**: Khi khởi động `ServerRestaurant`, ứng dụng báo lỗi kết nối cơ sở dữ liệu hoặc kiểm tra kết nối trả về `null`.
* **Giải pháp**:
  - Đảm bảo dịch vụ SQL Server đang chạy (chạy `services.msc` và kiểm tra dịch vụ `SQL Server (MSSQLSERVER)`).
  - Đảm bảo đã tạo cơ sở dữ liệu tên là `QuanLiNhaHang`.
  - **Kích hoạt TCP/IP**: Mở **SQL Server Configuration Manager** -> **SQL Server Network Configuration** -> **Protocols for MSSQLSERVER** -> Bật (Enable) **TCP/IP** và kiểm tra xem cổng TCP Port có phải là `1433` hay không. Sau đó restart lại dịch vụ SQL Server.

### 2. Lỗi Java Version (UnsupportedClassVersionError)
* **Triệu chứng**: Ứng dụng báo lỗi không tương thích phiên bản Java.
* **Giải pháp**: Dự án đã được nâng cấp lên **Java 21** và **JavaFX 21**. Vui lòng cài đặt JDK 21 trở lên trên máy tính của bạn và cấu hình IDE (NetBeans, IntelliJ, Eclipse...) sử dụng JDK 21 để biên dịch và chạy.

### 3. Lỗi Firewall chặn cổng 9999 khi chạy trên các thiết bị khác nhau
* **Triệu chứng**: Máy Client báo lỗi `Connection timed out` hoặc `Connection refused` khi thử kết nối tới Server mặc dù đã đổi đúng IP LAN của Server.
* **Giải pháp**: Trên máy Server, hãy mở **Windows Defender Firewall** -> **Advanced Settings** -> **Inbound Rules** -> Tạo Rule mới (**New Rule**) cho **Port TCP 9999** và chọn **Allow the connection**.

---

## 🚀 Hướng dẫn Chạy ứng dụng

1. **Khởi động Server trước**:
   - Chạy lớp `RestaurantServer` nằm trong dự án [ServerRestaurant](file:///d:/GiaoDienAPP/MonHoc/DUAN%20-%20goc/ServerRestaurant/src/main/java/server/RestaurantServer.java).
   - Đảm bảo console hiển thị thông báo Server đã lắng nghe trên cổng `9999`.

2. **Khởi động các ứng dụng Client**:
   - Khởi chạy các ứng dụng Client bằng cách chạy lớp `Main` của từng phân hệ:
     - **Admin**: [Restaurant-Admin/Main.java](file:///d:/GiaoDienAPP/MonHoc/DUAN%20-%20goc/Restaurant-Admin/src/main/java/restaurant/Main.java)
     - **Customer**: [Restaurant-Customer/Main.java](file:///d:/GiaoDienAPP/MonHoc/DUAN%20-%20goc/Restaurant-Customer/src/main/java/restaurant/Main.java)
     - **Employee**: [Restaurant-Employee/Main.java](file:///d:/GiaoDienAPP/MonHoc/DUAN%20-%20goc/Restaurant-Employee/src/main/java/restaurant/Main.java)