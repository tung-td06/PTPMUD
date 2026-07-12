package restaurant;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import food.FoodController.FoodModel;
import table.TableController.TableModel;
import employee.EmployeeController.EmployeeModel;
import bill.BillController.BillModel;
import warehouse.WarehouseController.WarehouseModel;
import booking.BookingController.BookingModel;
import customer.CustomerController.CustomerModel;
import supplier.SupplierController.SupplierModel;
import shift.ShiftController.ShiftModel;
import category.CategoryController.CategoryModel;
import importreceipt.ImportController.ImportModel;
import account.AccountController.AccountModel;
import recipe.RecipeController.RecipeModel;

public class MockDataStore {

        public static final ObservableList<FoodModel> foods = FXCollections.observableArrayList();
        public static final ObservableList<TableModel> tables = FXCollections.observableArrayList();
        public static final ObservableList<EmployeeModel> employees = FXCollections.observableArrayList();
        public static final ObservableList<BillModel> bills = FXCollections.observableArrayList();
        public static final ObservableList<WarehouseModel> warehouseItems = FXCollections.observableArrayList();
        public static final ObservableList<BookingModel> bookings = FXCollections.observableArrayList();

        public static final ObservableList<CustomerModel> customers = FXCollections.observableArrayList();
        public static final ObservableList<SupplierModel> suppliers = FXCollections.observableArrayList();
        public static final ObservableList<ShiftModel> shifts = FXCollections.observableArrayList();
        public static final ObservableList<CategoryModel> categories = FXCollections.observableArrayList();
        public static final ObservableList<ImportModel> imports = FXCollections.observableArrayList();
        public static final ObservableList<AccountModel> accounts = FXCollections.observableArrayList();
        public static final ObservableList<RecipeModel> recipes = FXCollections.observableArrayList();

        public static final ObservableList<String> customerIds = FXCollections.observableArrayList();
        public static final ObservableList<String> customerNames = FXCollections.observableArrayList();

        static {
                // Initialize Foods
                foods.addAll(
                                new FoodModel("F001", "Phở bò Kobe", "Món chính", "250,000 VNĐ", "Còn món"),
                                new FoodModel("F002", "Gỏi cuốn tôm thịt", "Khai vị", "65,000 VNĐ", "Còn món"),
                                new FoodModel("F003", "Cá hồi áp chảo sốt chanh leo", "Món chính", "320,000 VNĐ",
                                                "Còn món"),
                                new FoodModel("F004", "Nước ép cam tươi", "Đồ uống", "45,000 VNĐ", "Còn món"),
                                new FoodModel("F005", "Bánh tiramisu", "Tráng miệng", "55,000 VNĐ", "Hết món"),
                                new FoodModel("F006", "Súp bào ngư vi cá", "Khai vị", "450,000 VNĐ", "Còn món"),
                                new FoodModel("F007", "Cơm chiên hải sản", "Món chính", "150,000 VNĐ", "Còn món"),
                                new FoodModel("F008", "Trà đào cam sả", "Đồ uống", "50,000 VNĐ", "Còn món"),
                                new FoodModel("F009", "Kem bơ Đà Lạt", "Tráng miệng", "40,000 VNĐ", "Còn món"),
                                new FoodModel("F010", "Sườn cừu nướng BBQ", "Món chính", "380,000 VNĐ", "Hết món"));

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
                                new TableModel("T12", "Bàn 12", "Khu C", "Trống"));

                // Initialize Employees
                employees.addAll(
                                new EmployeeModel("NV001", "Nguyễn Văn An", "Quản lý", "0981000001", "Hành chính",
                                                "Đang làm"),
                                new EmployeeModel("NV002", "Trần Thị Bình", "Thu ngân", "0981000002", "Ca sáng",
                                                "Đang làm"),
                                new EmployeeModel("NV003", "Lê Văn Cường", "Phục vụ", "0981000003", "Ca sáng",
                                                "Đang làm"),
                                new EmployeeModel("NV004", "Phạm Thị Dung", "Phục vụ", "0981000004", "Ca chiều",
                                                "Đang làm"),
                                new EmployeeModel("NV005", "Hoàng Văn Đức", "Đầu bếp", "0981000005", "Ca chiều",
                                                "Đang làm"),
                                new EmployeeModel("NV006", "Ngô Thị Hạnh", "Thu ngân", "0981000006", "Ca tối",
                                                "Đang làm"),
                                new EmployeeModel("NV007", "Đỗ Văn Hải", "Bảo vệ", "0981000007", "Ca tối", "Đang làm"),
                                new EmployeeModel("NV008", "Bùi Thị Lan", "Phục vụ", "0981000008", "Ca tối",
                                                "Đang làm"),
                                new EmployeeModel("NV009", "Vũ Văn Long", "Đầu bếp", "0981000009", "Ca sáng",
                                                "Đang làm"),
                                new EmployeeModel("NV010", "Phan Thị Mai", "Lễ tân", "0981000010", "Ca sáng",
                                                "Đang làm"),
                                new EmployeeModel("NV016", "Cao Thị Quỳnh", "Lễ tân", "0981000016", "Ca chiều",
                                                "Nghỉ phép"));

                // Initialize Bills
                /*
                 * bills.addAll(
                 * new BillModel("HD001", "Nguyễn Văn A", "Bàn 01", "1,200,000 VNĐ", "Tiền mặt",
                 * "Đã thanh toán"),
                 * new BillModel("HD002", "Trần Văn B", "Bàn 05", "850,000 VNĐ", "Chuyển khoản",
                 * "Đang xử lý"),
                 * new BillModel("HD003", "Lê Minh C", "Bàn VIP", "2,500,000 VNĐ", "Visa",
                 * "Đã thanh toán"),
                 * new BillModel("HD004", "Phạm Văn D", "Bàn 08", "620,000 VNĐ", "Momo",
                 * "Chờ thanh toán"),
                 * new BillModel("HD005", "Hoàng Anh Tuấn", "Bàn 03", "350,000 VNĐ", "Momo",
                 * "Đã thanh toán"),
                 * new BillModel("HD006", "Đặng Thị Thắm", "Bàn 11", "980,000 VNĐ", "Tiền mặt",
                 * "Đã thanh toán"),
                 * new BillModel("HD007", "Phan Thanh Hùng", "Bàn VIP 02", "1,850,000 VNĐ",
                 * "Thẻ tín dụng",
                 * "Chờ thanh toán"));
                 */
                // Initialize Warehouse Items
                warehouseItems.addAll(
                                new WarehouseModel("NL001", "Thịt bò thăn", "kg", 45.0, 10.0, "Đủ hàng"),
                                new WarehouseModel("NL002", "Gạo Tám thơm", "kg", 120.0, 20.0, "Đủ hàng"),
                                new WarehouseModel("NL003", "Rau xà lách", "kg", 4.5, 5.0, "Sắp hết"),
                                new WarehouseModel("NL004", "Trà Lipton", "hộp", 2.0, 5.0, "Sắp hết"),
                                new WarehouseModel("NL005", "Đường cát trắng", "kg", 25.0, 5.0, "Đủ hàng"),
                                new WarehouseModel("NL006", "Cà phê hạt", "kg", 0.0, 5.0, "Hết hàng"),
                                new WarehouseModel("NL007", "Dầu ăn Neptune", "lít", 30.0, 8.0, "Đủ hàng"),
                                new WarehouseModel("NL008", "Cá hồi phi lê", "kg", 15.0, 5.0, "Đủ hàng"),
                                new WarehouseModel("NL009", "Bột mì đa dụng", "kg", 1.2, 5.0, "Sắp hết"),
                                new WarehouseModel("NL010", "Sữa đặc Ngôi sao", "lon", 0.0, 10.0, "Hết hàng"));

                // Initialize Bookings
                bookings.addAll(
                                new BookingModel("BK001", "Trần Thu Trang", "0901234567", 4, "2026-06-01 18:30",
                                                "Bàn 04",
                                                "Đã xác nhận"),
                                new BookingModel("BK002", "Phùng Thế Vinh", "0912345678", 2, "2026-06-01 19:00",
                                                "Bàn 03",
                                                "Chờ xác nhận"),
                                new BookingModel("BK003", "Nguyễn Minh Hằng", "0923456789", 8, "2026-06-01 19:30",
                                                "Bàn VIP 02",
                                                "Đã xác nhận"),
                                new BookingModel("BK004", "Lê Văn Tám", "0934567890", 5, "2026-06-01 20:00", "Bàn 06",
                                                "Đã hủy"),
                                new BookingModel("BK005", "Đỗ Tuấn Kiệt", "0945678901", 6, "2026-06-02 11:30",
                                                "Bàn VIP 01",
                                                "Đã xác nhận"),
                                new BookingModel("BK006", "Vũ Mai Phương", "0956789012", 2, "2026-06-02 12:00",
                                                "Bàn 11",
                                                "Chờ xác nhận"),
                                new BookingModel("BK007", "Hoàng Quốc Việt", "0967890123", 4, "2026-06-02 18:00",
                                                "Bàn 09",
                                                "Đã xác nhận"));

                // Initialize Customers
                customers.addAll(
                                new CustomerModel("KH001", "Nguyễn Văn A", "0901234567", 120),
                                new CustomerModel("KH002", "Trần Thị Bích", "0912345678", 45),
                                new CustomerModel("KH003", "Lê Minh C", "0987654321", 250),
                                new CustomerModel("KH004", "Phạm Văn D", "0933445566", 80),
                                new CustomerModel("KH005", "Hoàng Anh Tuấn", "0944556677", 15),
                                new CustomerModel("KH006", "Đặng Thị Thắm", "0981223344", 320),
                                new CustomerModel("KH007", "Phan Thanh Hùng", "0988776655", 60));

                for (CustomerModel customer : customers) {
                        customerIds.add(customer.getMakh());
                        customerNames.add(customer.getTenkh());
                }

                // Initialize Suppliers
                suppliers.addAll(
                                new SupplierModel("NCC001", "Công ty Thực phẩm Hữu cơ Việt", "0243999888"),
                                new SupplierModel("NCC002", "Tổng kho Hải sản Miền Bắc", "0283777666"),
                                new SupplierModel("NCC003", "Đại lý Rau sạch Vân Nội", "0977665544"),
                                new SupplierModel("NCC004", "Nhà phân phối Nước ngọt Sài Gòn", "0911223344"));

                // Initialize Shifts
                shifts.addAll(
                                new ShiftModel("CA001", "NV002", "Ca sáng", "2026-06-14 06:00:00",
                                                "2026-06-14 12:00:00"),
                                new ShiftModel("CA002", "NV003", "Ca sáng", "2026-06-14 06:00:00",
                                                "2026-06-14 12:00:00"),
                                new ShiftModel("CA003", "NV004", "Ca chiều", "2026-06-14 12:00:00",
                                                "2026-06-14 18:00:00"),
                                new ShiftModel("CA004", "NV005", "Ca chiều", "2026-06-14 12:00:00",
                                                "2026-06-14 18:00:00"),
                                new ShiftModel("CA005", "NV006", "Ca tối", "2026-06-14 18:00:00",
                                                "2026-06-14 23:30:00"));

                // Initialize Categories
                categories.addAll(
                                new CategoryModel("L001", "Khai vị"),
                                new CategoryModel("L002", "Món chính"),
                                new CategoryModel("L003", "Đồ uống"),
                                new CategoryModel("L004", "Tráng miệng"));

                // Initialize Imports
                imports.addAll(
                                new ImportModel("PN001", "NCC001", "2026-06-10 08:30:00", 12500000.0),
                                new ImportModel("PN002", "NCC002", "2026-06-12 09:15:00", 8400000.0),
                                new ImportModel("PN003", "NCC003", "2026-06-13 07:00:00", 3200000.0),
                                new ImportModel("PN004", "NCC001", "2026-06-14 10:00:00", 4500000.0));

                // Initialize Accounts
                accounts.addAll(
                                new AccountModel("NV001", "nvan.an", "Admin", "••••••"),
                                new AccountModel("NV002", "tthi.binh", "Nhân viên", "••••••"),
                                new AccountModel("NV003", "lvan.cuong", "Nhân viên", "••••••"),
                                new AccountModel("NV005", "hvan.duc", "Nhân viên", "••••••"),
                                new AccountModel("NV006", "ngthi.hanh", "Nhân viên", "••••••"));

                // Initialize Recipes
                recipes.addAll(
                                new RecipeModel("F001", "Phở bò Kobe", "NL001", "Thịt bò thăn", "0.2 kg"),
                                new RecipeModel("F001", "Phở bò Kobe", "NL002", "Gạo Tám thơm", "0.1 kg"),
                                new RecipeModel("F003", "Cá hồi áp chảo", "NL008", "Cá hồi phi lê", "0.25 kg"),
                                new RecipeModel("F003", "Cá hồi áp chảo", "NL007", "Dầu ăn Neptune", "0.05 lít"),
                                new RecipeModel("F007", "Cơm chiên hải sản", "NL002", "Gạo Tám thơm", "0.15 kg"),
                                new RecipeModel("F007", "Cơm chiên hải sản", "NL007", "Dầu ăn Neptune", "0.03 lít"),
                                new RecipeModel("F002", "Gỏi cuốn tôm thịt", "NL003", "Rau xà lách", "0.05 kg"),
                                new RecipeModel("F005", "Bánh tiramisu", "NL005", "Đường cát trắng", "0.1 kg"),
                                new RecipeModel("F005", "Bánh tiramisu", "NL009", "Bột mì đa dụng", "0.15 kg"));
        }
}
