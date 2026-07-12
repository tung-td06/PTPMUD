package restaurant;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import table.TableController.TableModel;
import bill.BillController.BillModel;
import booking.BookingController.BookingModel;
import customer.CustomerController.CustomerModel;
import shift.ShiftController.ShiftModel;
import billdetail.BillDetailController.BillDetailModel;

public class MockDataStore {

    public static class EmployeeModel {
        private String id;
        private String name;
        private String role;
        private String phone;
        private String shift;
        private String status;

        public EmployeeModel(String id, String name, String role, String phone, String shift, String status) {
            this.id = id;
            this.name = name;
            this.role = role;
            this.phone = phone;
            this.shift = shift;
            this.status = status;
        }

        public String getId() { return id; }
        public String getName() { return name; }
        public String getRole() { return role; }
        public String getPhone() { return phone; }
        public String getShift() { return shift; }
        public String getStatus() { return status; }
    }

    public static final ObservableList<TableModel> tables = FXCollections.observableArrayList();
    public static final ObservableList<EmployeeModel> employees = FXCollections.observableArrayList();
    public static final ObservableList<BillModel> bills = FXCollections.observableArrayList();
    public static final ObservableList<BookingModel> bookings = FXCollections.observableArrayList();
    public static final ObservableList<BillDetailModel> billDetails = FXCollections.observableArrayList();

    public static final ObservableList<CustomerModel> customers = FXCollections.observableArrayList();
    public static final ObservableList<ShiftModel> shifts = FXCollections.observableArrayList();

    public static final ObservableList<String> customerIds = FXCollections.observableArrayList();
    public static final ObservableList<String> customerNames = FXCollections.observableArrayList();

    static {
        // Initialize Tables
        tables.addAll(
            new TableModel("T01", "Bàn 01", "Khu A", "Trống"),
            new TableModel("T02", "Bàn 02", "Khu A", "Có khách"),
            new TableModel("T03", "Bàn 03", "Khu A", "Trống"),
            new TableModel("T04", "Bàn 04", "Khu A", "Đã đặt"),
            new TableModel("T05", "Bàn 05", "Khu B", "Có khách"),
            new TableModel("T06", "Bàn 06", "Khu B", "Trống"),
            new TableModel("T07", "Bàn VIP 01", "Khu VIP", "Có khách"),
            new TableModel("T08", "Bàn VIP 02", "Khu VIP", "Đã đặt"),
            new TableModel("T09", "Bàn 09", "Khu B", "Trống"),
            new TableModel("T10", "Bàn 10", "Khu B", "Có khách"),
            new TableModel("T11", "Bàn 11", "Khu C", "Trống"),
            new TableModel("T12", "Bàn 12", "Khu C", "Trống")
        );

        // Initialize Employees
        employees.addAll(
            new EmployeeModel("NV001", "Nguyễn Văn An", "Quản lý", "0981000001", "Hành chính", "Đang làm"),
            new EmployeeModel("NV002", "Trần Thị Bình", "Thu ngân", "0981000002", "Ca sáng", "Đang làm"),
            new EmployeeModel("NV003", "Lê Văn Cường", "Phục vụ", "0981000003", "Ca sáng", "Đang làm"),
            new EmployeeModel("NV004", "Phạm Thị Dung", "Phục vụ", "0981000004", "Ca chiều", "Đang làm"),
            new EmployeeModel("NV005", "Hoàng Văn Đức", "Đầu bếp", "0981000005", "Ca chiều", "Đang làm"),
            new EmployeeModel("NV006", "Ngô Thị Hạnh", "Thu ngân", "0981000006", "Ca tối", "Đang làm"),
            new EmployeeModel("NV007", "Đỗ Văn Hải", "Bảo vệ", "0981000007", "Ca tối", "Đang làm"),
            new EmployeeModel("NV008", "Bùi Thị Lan", "Phục vụ", "0981000008", "Ca tối", "Đang làm"),
            new EmployeeModel("NV009", "Vũ Văn Long", "Đầu bếp", "0981000009", "Ca sáng", "Đang làm"),
            new EmployeeModel("NV010", "Phan Thị Mai", "Lễ tân", "0981000010", "Ca sáng", "Đang làm"),
            new EmployeeModel("NV016", "Cao Thị Quỳnh", "Lễ tân", "0981000016", "Ca chiều", "Nghỉ phép")
        );

        // Initialize Bookings
        bookings.addAll(
            new BookingModel("BK001", "Trần Thu Trang", "0901234567", 4, "2026-06-01 18:30", "Bàn 04", "Đã xác nhận"),
            new BookingModel("BK002", "Phùng Thế Vinh", "0912345678", 2, "2026-06-01 19:00", "Bàn 03", "Chờ xác nhận"),
            new BookingModel("BK003", "Nguyễn Minh Hằng", "0923456789", 8, "2026-06-01 19:30", "Bàn VIP 02", "Đã xác nhận"),
            new BookingModel("BK004", "Lê Văn Tám", "0934567890", 5, "2026-06-01 20:00", "Bàn 06", "Đã hủy"),
            new BookingModel("BK005", "Đỗ Tuấn Kiệt", "0945678901", 6, "2026-06-02 11:30", "Bàn VIP 01", "Đã xác nhận"),
            new BookingModel("BK006", "Vũ Mai Phương", "0956789012", 2, "2026-06-02 12:00", "Bàn 11", "Chờ xác nhận"),
            new BookingModel("BK007", "Hoàng Quốc Việt", "0967890123", 4, "2026-06-02 18:00", "Bàn 09", "Đã xác nhận")
        );

        // Initialize Customers
        customers.addAll(
            new CustomerModel("KH001", "Nguyễn Văn A", "0901234567", 120),
            new CustomerModel("KH002", "Trần Thị Bích", "0912345678", 45),
            new CustomerModel("KH003", "Lê Minh C", "0987654321", 250),
            new CustomerModel("KH004", "Phạm Văn D", "0933445566", 80),
            new CustomerModel("KH005", "Hoàng Anh Tuấn", "0944556677", 15),
            new CustomerModel("KH006", "Đặng Thị Thắm", "0981223344", 320),
            new CustomerModel("KH007", "Phan Thanh Hùng", "0988776655", 60)
        );

        for (CustomerModel customer : customers) {
            customerIds.add(customer.getMakh());
            customerNames.add(customer.getTenkh());
        }

        // Initialize Shifts
        shifts.addAll(
            new ShiftModel("CA001", "NV002", "Ca sáng", "2026-06-14 06:00:00", "2026-06-14 12:00:00"),
            new ShiftModel("CA002", "NV003", "Ca sáng", "2026-06-14 06:00:00", "2026-06-14 12:00:00"),
            new ShiftModel("CA003", "NV004", "Ca chiều", "2026-06-14 12:00:00", "2026-06-14 18:00:00"),
            new ShiftModel("CA004", "NV005", "Ca chiều", "2026-06-14 12:00:00", "2026-06-14 18:00:00"),
            new ShiftModel("CA005", "NV006", "Ca tối", "2026-06-14 18:00:00", "2026-06-14 23:30:00")
        );

        // Initialize Bill Details
        billDetails.addAll(
            new BillDetailModel("HD001", "F001", "Phở bò Kobe", 2, 250000.0, 500000.0),
            new BillDetailModel("HD001", "F004", "Nước ép cam tươi", 2, 45000.0, 90000.0),
            new BillDetailModel("HD002", "F003", "Cá hồi áp chảo", 1, 320000.0, 320000.0),
            new BillDetailModel("HD002", "F007", "Cơm chiên hải sản", 2, 150000.0, 300000.0),
            new BillDetailModel("HD003", "F006", "Súp bào ngư vi cá", 2, 450000.0, 900000.0),
            new BillDetailModel("HD003", "F010", "Sườn cừu nướng BBQ", 2, 380000.0, 760000.0),
            new BillDetailModel("HD004", "F002", "Gỏi cuốn tôm thịt", 3, 65000.0, 195000.0),
            new BillDetailModel("HD005", "F009", "Kem bơ Đà Lạt", 2, 40000.0, 80000.0)
        );
    }
}
