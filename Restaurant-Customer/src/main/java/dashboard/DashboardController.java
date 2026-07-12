package dashboard;

import java.net.URL;
import java.time.LocalDate;
import java.util.ResourceBundle;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.AnchorPane;
import javafx.scene.Node;
import javafx.fxml.FXMLLoader;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.util.Duration;
import javafx.application.Platform;

import model.HoaDon;
import model.KhachHang;
import model.BanAn;
import model.NhanVien;
import model.MonAn;
import model.NhapHang;
import network.Action;
import network.Module;
import network.Request;
import network.Response;
import network.SocketClient;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import javafx.scene.layout.VBox;
import javafx.scene.Parent;
import customer.controller.util.CustomerSession;

public class DashboardController implements Initializable {

    private static DashboardController instance;

    public static DashboardController getInstance() {
        return instance;
    }

    private final DashboardData dashboardData = new DashboardData();
    private Timeline timeline;
    private boolean isUpdatingFilters = false;
    private boolean isReloading = false;

    @FXML
    private Label tableTitle;

    @FXML
    private Label tableSubtitle;

    @FXML
    private AnchorPane contentArea;

    @FXML
    private VBox menuContainer;

    @FXML
    private Button logoutBtn;

    private Node dashboardView;
    private final List<Button> customerButtons = new java.util.ArrayList<>();

    // Customer App dynamic navigation buttons
    private Button btnHome;
    private Button btnMenu;
    private Button btnCart;
    private Button btnBooking;
    private Button btnOrder;
    private Button btnPay;
    private Button btnHistory;
    private Button btnNotification;
    private Button btnProfile;

    @FXML
    private Label revenueLabel;

    @FXML
    private Label orderLabel;

    @FXML
    private Label tableLabel;

    @FXML
    private Label employeeLabel;

    @FXML
    private Label customerLabel;

    @FXML
    private Label foodLabel;

    @FXML
    private Label bookedTableLabel;

    @FXML
    private Label importLabel;

    @FXML
    private Button dashboardBtn;

    @FXML
    private Button employeeBtn;

    @FXML
    private Button tableBtn;

    @FXML
    private Button foodBtn;

    @FXML
    private Button billBtn;

    @FXML
    private Button orderBtn;

    @FXML
    private Button warehouseBtn;

    @FXML
    private Button bookingBtn;

    @FXML
    private Button categoryBtn;

    @FXML
    private Button customerBtn;

    @FXML
    private Button importBtn;

    @FXML
    private Button supplierBtn;

    @FXML
    private Button shiftBtn;

    @FXML
    private Button accountBtn;

    @FXML
    private Button recipeBtn;

    @FXML
    private ComboBox<String> timeComboBox;

    @FXML
    private DatePicker fromDatePicker;

    @FXML
    private DatePicker toDatePicker;

    @FXML
    private TextField searchField;

    @FXML
    private TableView<BillModel> billTable;

    @FXML
    private TableColumn<BillModel, String> idColumn;

    @FXML
    private TableColumn<BillModel, String> customerColumn;

    @FXML
    private TableColumn<BillModel, String> tableColumn;

    @FXML
    private TableColumn<BillModel, String> totalColumn;

    @FXML
    private TableColumn<BillModel, String> paymentColumn;

    @FXML
    private TableColumn<BillModel, String> statusColumn;

    @FXML
    private TableView<TopFoodModel> topFoodsTable;

    @FXML
    private TableColumn<TopFoodModel, String> foodIdColumn;

    @FXML
    private TableColumn<TopFoodModel, String> foodNameColumn;

    @FXML
    private TableColumn<TopFoodModel, String> foodPriceColumn;

    @FXML
    private TableColumn<TopFoodModel, String> foodSoldColumn;

    @FXML
    private Label lblProfileName;

    @FXML
    private Label lblProfileRole;

    private final ObservableList<BillModel> billMasterList = FXCollections.observableArrayList();
    private FilteredList<BillModel> filteredBills;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        instance = this;
        if (contentArea != null && !contentArea.getChildren().isEmpty()) {
            dashboardView = contentArea.getChildren().get(0);
        }
        setupTableColumns();
        setupFilterControls();
        setupMenuActions();
        setupAutoRefresh();

        if (timeline != null) {
            timeline.stop(); // Tắt tự làm mới của Admin dashboard
        }

        if (Login.LoginController.loggedInAccount != null) {
            if (lblProfileName != null) {
                lblProfileName.setText("Đang tải...");
            }
            if (lblProfileRole != null) {
                lblProfileRole.setText("Khách hàng");
            }
            CustomerSession.initialize(Login.LoginController.loggedInAccount, () -> {
                if (lblProfileName != null && CustomerSession.getCurrentCustomer() != null) {
                    lblProfileName.setText(CustomerSession.getCurrentCustomer().getTenKH());
                }
                setupCustomerNavigation();
            });
        } else {
            setupCustomerNavigation();
        }
    }

    private void setupAutoRefresh() {
        timeline = new Timeline(
                new KeyFrame(Duration.seconds(10), e -> loadDashboard()));
        timeline.setCycleCount(Animation.INDEFINITE);
        timeline.play();
    }

    /* ================= MENU ================= */

    private void setupMenuActions() {
        dashboardBtn.setOnAction(event -> {
            setActiveMenu(dashboardBtn);
            if (contentArea != null && dashboardView != null) {
                contentArea.getChildren().clear();
                contentArea.getChildren().add(dashboardView);
                loadDashboard();
            }
        });

        employeeBtn.setOnAction(event -> {
            setActiveMenu(employeeBtn);
            loadView("/fxml/employee.fxml");
        });

        tableBtn.setOnAction(event -> {
            setActiveMenu(tableBtn);
            loadView("/fxml/table.fxml");
        });

        foodBtn.setOnAction(event -> {
            setActiveMenu(foodBtn);
            loadView("/fxml/food.fxml");
        });

        billBtn.setOnAction(event -> {
            setActiveMenu(billBtn);
            loadView("/fxml/bill.fxml");
        });

        orderBtn.setOnAction(event -> {
            setActiveMenu(orderBtn);
            loadView("/fxml/order.fxml");
        });

        warehouseBtn.setOnAction(event -> {
            setActiveMenu(warehouseBtn);
            loadView("/fxml/warehouse.fxml");
        });

        bookingBtn.setOnAction(event -> {
            setActiveMenu(bookingBtn);
            loadView("/fxml/booking.fxml");
        });

        categoryBtn.setOnAction(event -> {
            setActiveMenu(categoryBtn);
            loadView("/fxml/category.fxml");
        });

        customerBtn.setOnAction(event -> {
            setActiveMenu(customerBtn);
            loadView("/fxml/customer.fxml");
        });

        importBtn.setOnAction(event -> {
            setActiveMenu(importBtn);
            loadView("/fxml/import.fxml");
        });

        supplierBtn.setOnAction(event -> {
            setActiveMenu(supplierBtn);
            loadView("/fxml/supplier.fxml");
        });

        shiftBtn.setOnAction(event -> {
            setActiveMenu(shiftBtn);
            loadView("/fxml/shift.fxml");
        });

        accountBtn.setOnAction(event -> {
            setActiveMenu(accountBtn);
            loadView("/fxml/account.fxml");
        });

        recipeBtn.setOnAction(event -> {
            setActiveMenu(recipeBtn);
            loadView("/fxml/recipe.fxml");
        });

        if (logoutBtn != null) {
            logoutBtn.setOnAction(event -> handleLogout());
        }
    }

    private void setActiveMenu(Button button) {
        if (button == null)
            return;
        resetMenu();
        button.getStyleClass().remove("menu-btn");
        if (!button.getStyleClass().contains("menu-btn-active")) {
            button.getStyleClass().add("menu-btn-active");
        }
    }

    private void loadView(String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            javafx.scene.Parent view = loader.load();
            if (contentArea != null) {
                contentArea.getChildren().clear();
                contentArea.getChildren().add(view);
                AnchorPane.setTopAnchor(view, 0.0);
                AnchorPane.setBottomAnchor(view, 0.0);
                AnchorPane.setLeftAnchor(view, 0.0);
                AnchorPane.setRightAnchor(view, 0.0);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void handleLogout() {
        try {
            if (timeline != null) {
                timeline.stop();
            }
            javafx.stage.Stage stage = (javafx.stage.Stage) logoutBtn.getScene().getWindow();

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/login.fxml"));
            javafx.scene.Parent root = loader.load();

            stage.getScene().setRoot(root);
            stage.setResizable(true);
            stage.setMinWidth(1000);
            stage.setMinHeight(600);
            stage.setWidth(1400);
            stage.setHeight(800);
            stage.centerOnScreen();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void resetMenu() {
        Button[] buttons = {
                dashboardBtn, tableBtn, shiftBtn, categoryBtn, bookingBtn, billBtn, orderBtn, customerBtn,
                warehouseBtn, foodBtn, supplierBtn, employeeBtn, accountBtn, importBtn, recipeBtn
        };

        for (Button btn : buttons) {
            if (btn != null) {
                btn.getStyleClass().remove("menu-btn-active");
                if (!btn.getStyleClass().contains("menu-btn")) {
                    btn.getStyleClass().add("menu-btn");
                }
            }
        }
    }

    /* ================= CARDS & TABLE DATA ================= */

    private void setupFilterControls() {
        timeComboBox.setItems(FXCollections.observableArrayList(
                "Tất cả", "Hôm nay", "7 ngày gần đây", "30 ngày gần đây", "Năm nay"));

        fromDatePicker.setDisable(false);
        fromDatePicker.setEditable(false);
        toDatePicker.setDisable(false);
        toDatePicker.setEditable(false);

        filteredBills = new FilteredList<>(billMasterList, bill -> true);
        billTable.setItems(filteredBills);

        timeComboBox.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (isUpdatingFilters)
                return;
            if (newValue != null) {
                isUpdatingFilters = true;
                fromDatePicker.setValue(null);
                toDatePicker.setValue(null);
                isUpdatingFilters = false;
            }
            applyFilter();
        });

        fromDatePicker.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (isUpdatingFilters)
                return;
            if (newValue != null) {
                isUpdatingFilters = true;
                timeComboBox.getSelectionModel().clearSelection();
                isUpdatingFilters = false;
            }
            validateAndApplyFilter();
        });

        toDatePicker.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (isUpdatingFilters)
                return;
            if (newValue != null) {
                isUpdatingFilters = true;
                timeComboBox.getSelectionModel().clearSelection();
                isUpdatingFilters = false;
            }
            validateAndApplyFilter();
        });

        searchField.textProperty().addListener((observable, oldValue, newValue) -> applyFilter());

        // Default selection to "Hôm nay"
        timeComboBox.getSelectionModel().select("Hôm nay");
    }

    private void validateAndApplyFilter() {
        LocalDate fromDate = fromDatePicker.getValue();
        LocalDate toDate = toDatePicker.getValue();
        if (fromDate != null && toDate != null && fromDate.isAfter(toDate)) {
            Platform.runLater(() -> {
                showAlert("Lỗi chọn ngày", "Từ ngày không được lớn hơn Đến ngày!");
                isUpdatingFilters = true;
                toDatePicker.setValue(null);
                isUpdatingFilters = false;
                applyFilter();
            });
            return;
        }
        applyFilter();
    }

    private void showAlert(String title, String message) {
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void updateTableTitle(String timeFilter, LocalDate fromDate, LocalDate toDate) {
        if (tableTitle == null)
            return;

        java.time.format.DateTimeFormatter formatter = java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy");

        if (timeFilter != null) {
            tableTitle.setText("Hóa đơn (" + timeFilter + ")");
        } else if (fromDate != null || toDate != null) {
            String fromStr = fromDate != null ? fromDate.format(formatter) : "Đầu";
            String toStr = toDate != null ? toDate.format(formatter) : "Hiện tại";
            tableTitle.setText("Hóa đơn (" + fromStr + " - " + toStr + ")");
        } else {
            tableTitle.setText("Hóa đơn (Tất cả)");
        }

        if (tableSubtitle != null) {
            tableSubtitle.setText(""); // clear the redundant subtitle
        }
    }

    private void applyFilter() {
        if (filteredBills == null) {
            return;
        }

        String timeFilter = timeComboBox.getValue();
        LocalDate fromDate = fromDatePicker.getValue();
        LocalDate toDate = toDatePicker.getValue();
        String keyword = normalize(searchField.getText());

        filteredBills.setPredicate(bill -> {
            if (bill == null) {
                return false;
            }

            return matchesTimeFilter(bill, timeFilter, fromDate, toDate)
                    && matchesKeyword(bill, keyword);
        });

        // Update Title dynamically
        updateTableTitle(timeFilter, fromDate, toDate);

        // Update statistics cards based on the filtered set of bills
        double filteredRevenue = 0;
        int filteredBillCount = filteredBills.size();

        for (BillModel b : filteredBills) {
            if ("Đã thanh toán".equalsIgnoreCase(b.getStatus())) {
                try {
                    String clean = b.getTotal().replace(" VNĐ", "").replace(",", "").trim();
                    filteredRevenue += Double.parseDouble(clean);
                } catch (Exception e) {
                    // Ignore parsing error
                }
            }
        }

        java.text.DecimalFormat df = new java.text.DecimalFormat("#,###");
        revenueLabel.setText(df.format(filteredRevenue) + " VNĐ");
        orderLabel.setText(String.valueOf(filteredBillCount));
    }

    private boolean matchesTimeFilter(BillModel bill, String timeFilter, LocalDate fromDate, LocalDate toDate) {
        LocalDate billDate = bill.getBillDate();
        if (billDate == null) {
            return false;
        }

        LocalDate today = LocalDate.now();

        if (timeFilter == null) {
            // DatePicker filter logic
            if (fromDate != null && toDate != null) {
                return isBetweenInclusive(billDate, fromDate, toDate);
            } else if (fromDate != null) {
                return !billDate.isBefore(fromDate);
            } else if (toDate != null) {
                return !billDate.isAfter(toDate);
            }
            return true;
        }

        switch (timeFilter) {
            case "Hôm nay":
                return billDate.isEqual(today);
            case "7 ngày gần đây":
                return isBetweenInclusive(billDate, today.minusDays(6), today);
            case "30 ngày gần đây":
                return isBetweenInclusive(billDate, today.minusDays(29), today);
            case "Năm nay":
                return isBetweenInclusive(billDate, LocalDate.of(today.getYear(), 1, 1), today);
            case "Tất cả":
            default:
                return true;
        }
    }

    private boolean isBetweenInclusive(LocalDate value, LocalDate fromDate, LocalDate toDate) {
        if (fromDate != null && value.isBefore(fromDate)) {
            return false;
        }
        return toDate == null || !value.isAfter(toDate);
    }

    private boolean matchesKeyword(BillModel bill, String keyword) {
        if (keyword.isEmpty()) {
            return true;
        }

        return contains(bill.getId(), keyword)
                || contains(bill.getCustomer(), keyword)
                || contains(bill.getTable(), keyword)
                || contains(bill.getTotal(), keyword)
                || contains(bill.getPayment(), keyword)
                || contains(bill.getStatus(), keyword)
                || contains(String.valueOf(bill.getBillDate()), keyword)
                || (bill.getMaBan() != null && contains(bill.getMaBan(), keyword));
    }

    private boolean contains(String value, String keyword) {
        return normalize(value).contains(keyword);
    }

    private String normalize(String value) {
        return value == null ? "" : value.toLowerCase().trim();
    }

    private void loadDashboard() {
        if (contentArea != null && dashboardView != null && !contentArea.getChildren().contains(dashboardView)) {
            return;
        }
        if (isReloading)
            return;
        isReloading = true;

        new Thread(() -> {
            try {
                // Asynchronously fetch latest data from server
                List<KhachHang> khList = null;
                List<BanAn> banList = null;
                List<NhanVien> nvList = null;
                List<MonAn> foodList = null;
                List<NhapHang> importList = null;
                List<HoaDon> serverList = null;

                try {
                    Request khReq = new Request(Module.KHACHHANG, Action.GET_ALL, null);
                    Response khRes = SocketClient.getInstance().sendRequest(khReq);
                    if (khRes != null && khRes.isSuccess()) {
                        khList = (List<KhachHang>) khRes.getData();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

                try {
                    Request banReq = new Request(Module.BANAN, Action.GET_ALL, null);
                    Response banRes = SocketClient.getInstance().sendRequest(banReq);
                    if (banRes != null && banRes.isSuccess()) {
                        banList = (List<BanAn>) banRes.getData();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

                try {
                    Request nvReq = new Request(Module.NHANVIEN, Action.GET_ALL, null);
                    Response nvRes = SocketClient.getInstance().sendRequest(nvReq);
                    if (nvRes != null && nvRes.isSuccess()) {
                        nvList = (List<NhanVien>) nvRes.getData();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

                try {
                    Request foodReq = new Request(Module.MONAN, Action.GET_ALL, null);
                    Response foodRes = SocketClient.getInstance().sendRequest(foodReq);
                    if (foodRes != null && foodRes.isSuccess()) {
                        foodList = (List<MonAn>) foodRes.getData();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

                try {
                    Request importReq = new Request(Module.NHAPHANG, Action.GET_ALL, null);
                    Response importRes = SocketClient.getInstance().sendRequest(importReq);
                    if (importRes != null && importRes.isSuccess()) {
                        importList = (List<NhapHang>) importRes.getData();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

                try {
                    Request billReq = new Request(Module.HOADON, Action.GET_ALL, null);
                    Response billRes = SocketClient.getInstance().sendRequest(billReq);
                    if (billRes != null && billRes.isSuccess()) {
                        serverList = (List<HoaDon>) billRes.getData();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

                // Statistics
                final int totalEmployees = (nvList != null) ? nvList.size() : restaurant.MockDataStore.employees.size();
                final int totalCustomers = (khList != null) ? khList.size() : restaurant.MockDataStore.customers.size();
                final int totalFoods = (foodList != null) ? foodList.size() : restaurant.MockDataStore.foods.size();
                final int totalImports = (importList != null) ? importList.size()
                        : restaurant.MockDataStore.imports.size();

                int busyTables = 0;
                int bookedTables = 0;
                if (banList != null) {
                    for (BanAn t : banList) {
                        if ("Có khách".equalsIgnoreCase(t.getTrangThai())) {
                            busyTables++;
                        } else if ("Đã đặt".equalsIgnoreCase(t.getTrangThai())) {
                            bookedTables++;
                        }
                    }
                } else {
                    for (table.TableController.TableModel t : restaurant.MockDataStore.tables) {
                        if ("Có khách".equalsIgnoreCase(t.getTrangthai())) {
                            busyTables++;
                        } else if ("Đã đặt".equalsIgnoreCase(t.getTrangthai())) {
                            bookedTables++;
                        }
                    }
                }
                final int finalBusy = busyTables;
                final int finalBooked = bookedTables;

                // Build lookup maps
                Map<String, String> customerMap = new HashMap<>();
                if (khList != null) {
                    for (KhachHang kh : khList) {
                        customerMap.put(kh.getMaKH(), kh.getTenKH());
                    }
                } else {
                    for (customer.CustomerController.CustomerModel kh : restaurant.MockDataStore.customers) {
                        customerMap.put(kh.getMakh(), kh.getTenkh());
                    }
                }

                Map<String, String> tableMap = new HashMap<>();
                if (banList != null) {
                    for (BanAn ban : banList) {
                        tableMap.put(ban.getMaBan(), ban.getTenBan());
                    }
                } else {
                    for (table.TableController.TableModel t : restaurant.MockDataStore.tables) {
                        tableMap.put(t.getMaban(), t.getTenban());
                    }
                }

                // Process bills
                ObservableList<BillModel> recentList = FXCollections.observableArrayList();
                if (serverList != null) {
                    for (HoaDon hd : serverList) {
                        String customerName = customerMap.getOrDefault(hd.getMaKH(), hd.getMaKH());
                        String tableName = tableMap.getOrDefault(hd.getMaBan(), hd.getMaBan());

                        BigDecimal tongTien = hd.getTongTien() != null ? hd.getTongTien() : BigDecimal.ZERO;
                        BigDecimal giamGia = hd.getGiamGia() != null ? hd.getGiamGia() : BigDecimal.ZERO;
                        BigDecimal thanhToan = hd.getThanhToan() != null ? hd.getThanhToan()
                                : tongTien.subtract(giamGia);

                        LocalDate billDate = hd.getTimeVao() != null ? hd.getTimeVao().toLocalDateTime().toLocalDate()
                                : LocalDate.now();

                        recentList.add(new BillModel(
                                hd.getMaHD(),
                                customerName,
                                tableName,
                                String.format("%,.0f VNĐ", tongTien.doubleValue()),
                                String.format("%,.0f VNĐ", thanhToan.doubleValue()),
                                hd.getTrangThai(),
                                billDate,
                                hd.getMaBan()));
                    }
                } else {
                    LocalDate today = LocalDate.now();
                    int index = 0;
                    for (bill.BillController.BillModel b : restaurant.MockDataStore.bills) {
                        recentList.add(new BillModel(
                                b.getId(),
                                b.getCustomer(),
                                b.getTable(),
                                b.getTotal(),
                                b.getPayment(),
                                b.getStatus(),
                                b.getTimeVao() != null ? b.getTimeVao().toLocalDateTime().toLocalDate()
                                        : today.minusDays(index++),
                                b.getMaBan()));
                    }
                }

                Platform.runLater(() -> {
                    // Update non-bill statistics
                    employeeLabel.setText(String.valueOf(totalEmployees));
                    customerLabel.setText(String.valueOf(totalCustomers));
                    foodLabel.setText(String.valueOf(totalFoods));
                    tableLabel.setText(String.valueOf(finalBusy));
                    bookedTableLabel.setText(String.valueOf(finalBooked));
                    importLabel.setText(String.valueOf(totalImports));

                    // Load recent bills table & calculate revenue/orderLabel
                    billMasterList.setAll(recentList);
                    applyFilter();

                    // Load top selling foods (Mock metrics)
                    ObservableList<TopFoodModel> topFoods = FXCollections.observableArrayList(
                            new TopFoodModel("F001", "Phở bò Kobe", "250,000 VNĐ", "142"),
                            new TopFoodModel("F003", "Cá hồi áp chảo", "320,000 VNĐ", "98"),
                            new TopFoodModel("F007", "Cơm chiên hải sản", "150,000 VNĐ", "85"),
                            new TopFoodModel("F004", "Nước ép cam tươi", "45,000 VNĐ", "76"),
                            new TopFoodModel("F006", "Súp bào ngư vi cá", "450,000 VNĐ", "63"));
                    topFoodsTable.setItems(topFoods);
                });
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                isReloading = false;
            }
        }).start();
    }

    private void setupTableColumns() {
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        customerColumn.setCellValueFactory(new PropertyValueFactory<>("customer"));
        tableColumn.setCellValueFactory(new PropertyValueFactory<>("table"));
        totalColumn.setCellValueFactory(new PropertyValueFactory<>("total"));
        paymentColumn.setCellValueFactory(new PropertyValueFactory<>("payment"));
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));

        foodIdColumn.setCellValueFactory(new PropertyValueFactory<>("foodId"));
        foodNameColumn.setCellValueFactory(new PropertyValueFactory<>("foodName"));
        foodPriceColumn.setCellValueFactory(new PropertyValueFactory<>("foodPrice"));
        foodSoldColumn.setCellValueFactory(new PropertyValueFactory<>("foodSold"));

        statusColumn.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                    setText(null);
                } else {
                    Label label = new Label(item);
                    label.getStyleClass().add("status-tag");

                    if (item.equalsIgnoreCase("Đã thanh toán")) {
                        label.getStyleClass().add("status-paid");
                    } else if (item.equalsIgnoreCase("Đang xử lý")) {
                        label.getStyleClass().add("status-processing");
                    } else if (item.equalsIgnoreCase("Chờ thanh toán")) {
                        label.getStyleClass().add("status-pending");
                    }

                    HBox container = new HBox(label);
                    container.setAlignment(Pos.CENTER);
                    setGraphic(container);
                }
            }
        });
    }

    /* ================= MODEL ================= */

    public static class BillModel {
        private final String id;
        private final String customer;
        private final String table;
        private final String total;
        private final String payment;
        private final String status;
        private final LocalDate billDate;
        private final String maBan;

        public BillModel(String id, String customer, String table, String total, String payment, String status,
                LocalDate billDate) {
            this(id, customer, table, total, payment, status, billDate, "");
        }

        public BillModel(String id, String customer, String table, String total, String payment, String status,
                LocalDate billDate, String maBan) {
            this.id = id;
            this.customer = customer;
            this.table = table;
            this.total = total;
            this.payment = payment;
            this.status = status;
            this.billDate = billDate;
            this.maBan = maBan;
        }

        public String getId() {
            return id;
        }

        public String getCustomer() {
            return customer;
        }

        public String getTable() {
            return table;
        }

        public String getTotal() {
            return total;
        }

        public String getPayment() {
            return payment;
        }

        public String getStatus() {
            return status;
        }

        public LocalDate getBillDate() {
            return billDate;
        }

        public String getMaBan() {
            return maBan;
        }
    }

    public static class TopFoodModel {
        private final String foodId;
        private final String foodName;
        private final String foodPrice;
        private final String foodSold;

        public TopFoodModel(String foodId, String foodName, String foodPrice, String foodSold) {
            this.foodId = foodId;
            this.foodName = foodName;
            this.foodPrice = foodPrice;
            this.foodSold = foodSold;
        }

        public String getFoodId() {
            return foodId;
        }

        public String getFoodName() {
            return foodName;
        }

        public String getFoodPrice() {
            return foodPrice;
        }

        public String getFoodSold() {
            return foodSold;
        }
    }

    private void setupCustomerNavigation() {
        if (menuContainer == null)
            return;
        menuContainer.getChildren().clear();

        btnHome = createMenuButton("Trang chủ", "🏠");
        btnMenu = createMenuButton("Thực đơn", "🍜");
        btnCart = createMenuButton("Giỏ hàng", "🛒");
        btnBooking = createMenuButton("Đặt bàn", "📅");
        btnOrder = createMenuButton("Đơn hàng", "📋");
        btnPay = createMenuButton("Thanh toán", "💳");
        btnHistory = createMenuButton("Lịch sử", "📜");
        btnNotification = createMenuButton("Thông báo", "🔔");
        btnProfile = createMenuButton("Tài khoản", "👤");

        customerButtons.clear();
        customerButtons.addAll(
                List.of(btnHome, btnMenu, btnCart, btnBooking, btnOrder, btnHistory, btnNotification, btnProfile));

        btnHome.setOnAction(e -> {
            setCustomerActiveMenu(btnHome);
            loadCustomerView("/fxml/customer/customer_home.fxml");
        });

        btnMenu.setOnAction(e -> {
            setCustomerActiveMenu(btnMenu);
            loadCustomerView("/fxml/customer/customer_menu.fxml");
        });

        btnCart.setOnAction(e -> {
            setCustomerActiveMenu(btnCart);
            loadCustomerView("/fxml/customer/customer_cart.fxml");
        });

        btnBooking.setOnAction(e -> {
            setCustomerActiveMenu(btnBooking);
            loadCustomerView("/fxml/customer/customer_booking.fxml");
        });

        btnOrder.setOnAction(e -> {
            setCustomerActiveMenu(btnOrder);
            loadCustomerView("/fxml/customer/customer_order.fxml");
        });

        btnPay.setOnAction(e -> {
            setCustomerActiveMenu(btnPay);
            loadCustomerView("/fxml/customer/customer_payment.fxml");
        });

        btnHistory.setOnAction(e -> {
            setCustomerActiveMenu(btnHistory);
            loadCustomerView("/fxml/customer/customer_history.fxml");
        });

        btnNotification.setOnAction(e -> {
            setCustomerActiveMenu(btnNotification);
            loadCustomerView("/fxml/customer/customer_notification.fxml");
        });

        btnProfile.setOnAction(e -> {
            setCustomerActiveMenu(btnProfile);
            loadCustomerView("/fxml/customer/customer_profile.fxml");
        });

        refreshCustomerNavigation();

        // Default to Home screen
        setCustomerActiveMenu(btnHome);
        loadCustomerView("/fxml/customer/customer_home.fxml");
    }

    private Button createMenuButton(String text, String emoji) {
        Button btn = new Button(text);
        btn.getStyleClass().add("menu-btn");
        btn.setPrefWidth(265.0);
        Label icon = new Label(emoji);
        icon.getStyleClass().add("menu-icon");
        btn.setGraphic(icon);
        return btn;
    }

    private void setCustomerActiveMenu(Button activeBtn) {
        for (Button btn : customerButtons) {
            btn.getStyleClass().remove("menu-btn-active");
            if (!btn.getStyleClass().contains("menu-btn")) {
                btn.getStyleClass().add("menu-btn");
            }
        }
        if (btnPay != null) {
            btnPay.getStyleClass().remove("menu-btn-active");
            if (!btnPay.getStyleClass().contains("menu-btn")) {
                btnPay.getStyleClass().add("menu-btn");
            }
        }
        activeBtn.getStyleClass().remove("menu-btn");
        activeBtn.getStyleClass().add("menu-btn-active");
    }

    public void refreshCustomerNavigation() {
        if (menuContainer == null || CustomerSession.getCurrentCustomer() == null)
            return;

        Platform.runLater(() -> {
            menuContainer.getChildren().clear();
            menuContainer.getChildren().addAll(List.of(
                    btnHome, btnMenu, btnCart, btnBooking, btnPay, btnHistory, btnNotification, btnProfile
            ));
        });
    }

    public void loadCustomerView(String fxmlPath) {
        CustomerSession.checkActiveBooking(null);
        refreshCustomerNavigation();
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent view = loader.load();
            if (contentArea != null) {
                contentArea.getChildren().clear();
                contentArea.getChildren().add(view);
                AnchorPane.setTopAnchor(view, 0.0);
                AnchorPane.setBottomAnchor(view, 0.0);
                AnchorPane.setLeftAnchor(view, 0.0);
                AnchorPane.setRightAnchor(view, 0.0);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void navigateToTab(int index) {
        if (index >= 0 && index < customerButtons.size()) {
            Button btn = customerButtons.get(index);
            Platform.runLater(btn::fire);
        }
    }

    public void navigateToPay() {
        if (btnPay != null) {
            Platform.runLater(() -> {
                setCustomerActiveMenu(btnPay);
                loadCustomerView("/fxml/customer/customer_payment.fxml");
            });
        }
    }

    public javafx.scene.Scene getScene() {
        if (contentArea != null) {
            return contentArea.getScene();
        }
        return null;
    }
}
