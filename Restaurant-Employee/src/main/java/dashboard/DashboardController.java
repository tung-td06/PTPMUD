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

public class DashboardController implements Initializable {

    // private final DashboardData dashboardData = new DashboardData();
    private Timeline timeline;

    @FXML
    private AnchorPane contentArea;

    @FXML
    private Button logoutBtn;

    private Node dashboardView;

    @FXML
    private Label lblWelcome;

    @FXML
    private Label shiftLabel;

    @FXML
    private Label activeTablesLabel;

    @FXML
    private Label pendingBillsLabel;

    @FXML
    private Label todayBookingsLabel;

    @FXML
    private Button dashboardBtn;

    @FXML
    private Button tableBtn;

    @FXML
    private Button billBtn;

    @FXML
    private Button billDetailBtn;

    @FXML
    private Button bookingBtn;

    @FXML
    private Button customerBtn;

    @FXML
    private Button shiftBtn;

    @FXML
    private Button orderBtn;

    @FXML
    private Button profileBtn;

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
        if (contentArea != null && !contentArea.getChildren().isEmpty()) {
            dashboardView = contentArea.getChildren().get(0);
        }
        setupTableColumns();
        setupFilterControls();
        setupMenuActions();
        loadDashboard();
        setupAutoRefresh();

        if (Login.LoginController.loggedInAccount != null) {
            String hoTen = Login.LoginController.loggedInAccount.getTenDN();
            if (restaurant.MockDataStore.employees != null) {
                for (restaurant.MockDataStore.EmployeeModel emp : restaurant.MockDataStore.employees) {
                    if (emp.getId() != null && emp.getId().equalsIgnoreCase(Login.LoginController.loggedInAccount.getMaNV())) {
                        hoTen = emp.getName();
                        break;
                    }
                }
            }
            if (lblWelcome != null) {
                lblWelcome.setText("Xin chào, " + hoTen + " 👋");
            }
            if (lblProfileName != null) {
                lblProfileName.setText(Login.LoginController.loggedInAccount.getTenDN());
            }
            if (lblProfileRole != null) {
                int q = Login.LoginController.loggedInAccount.getQuyen();
                String roleStr = switch (q) {
                    case 1 -> "Quản trị viên";
                    case 2 -> "Quản lý";
                    case 3 -> "Nhân viên";
                    default -> "Không xác định";
                };
                lblProfileRole.setText(roleStr);
            }
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
        if (dashboardBtn != null) {
            dashboardBtn.setOnAction(event -> {
                setActiveMenu(dashboardBtn);
                if (contentArea != null && dashboardView != null) {
                    contentArea.getChildren().clear();
                    contentArea.getChildren().add(dashboardView);
                }
            });
        }

        if (tableBtn != null) {
            tableBtn.setOnAction(event -> {
                setActiveMenu(tableBtn);
                loadView("/fxml/table.fxml");
            });
        }

        if (billBtn != null) {
            billBtn.setOnAction(event -> {
                setActiveMenu(billBtn);
                loadView("/fxml/bill.fxml");
            });
        }

        if (billDetailBtn != null) {
            billDetailBtn.setOnAction(event -> {
                setActiveMenu(billDetailBtn);
                loadView("/fxml/billdetail.fxml");
            });
        }

        if (bookingBtn != null) {
            bookingBtn.setOnAction(event -> {
                setActiveMenu(bookingBtn);
                loadView("/fxml/booking.fxml");
            });
        }

        if (customerBtn != null) {
            customerBtn.setOnAction(event -> {
                setActiveMenu(customerBtn);
                loadView("/fxml/customer.fxml");
            });
        }

        if (shiftBtn != null) {
            shiftBtn.setOnAction(event -> {
                setActiveMenu(shiftBtn);
                loadView("/fxml/shift.fxml");
            });
        }

        if (orderBtn != null) {
            orderBtn.setOnAction(event -> {
                setActiveMenu(orderBtn);
                loadView("/fxml/order.fxml");
            });
        }

        if (profileBtn != null) {
            profileBtn.setOnAction(event -> {
                setActiveMenu(profileBtn);
                loadView("/fxml/profile.fxml");
            });
        }

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
                dashboardBtn, tableBtn, orderBtn, shiftBtn, bookingBtn, billBtn, billDetailBtn, customerBtn, profileBtn
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
        timeComboBox.getSelectionModel().select("Tất cả");

        fromDatePicker.setDisable(false);
        fromDatePicker.setEditable(false);
        toDatePicker.setDisable(false);
        toDatePicker.setEditable(false);

        filteredBills = new FilteredList<>(billMasterList, bill -> true);
        billTable.setItems(filteredBills);

        timeComboBox.valueProperty().addListener((observable, oldValue, newValue) -> {
            applyFilter();
        });
        fromDatePicker.valueProperty().addListener((observable, oldValue, newValue) -> applyFilter());
        toDatePicker.valueProperty().addListener((observable, oldValue, newValue) -> applyFilter());
        searchField.textProperty().addListener((observable, oldValue, newValue) -> applyFilter());
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
    }

    private boolean matchesTimeFilter(BillModel bill, String timeFilter, LocalDate fromDate, LocalDate toDate) {
        if (timeFilter == null || "Tất cả".equals(timeFilter)) {
            return true;
        }

        LocalDate billDate = bill.getBillDate();
        if (billDate == null) {
            return false;
        }

        LocalDate today = LocalDate.now();
        switch (timeFilter) {
            case "Hôm nay":
                return billDate.isEqual(today);
            case "Tuần này":
                LocalDate startOfWeek = today.minusDays(today.getDayOfWeek().getValue() - 1L);
                LocalDate endOfWeek = startOfWeek.plusDays(6);
                return isBetweenInclusive(billDate, startOfWeek, endOfWeek);
            case "Tháng này":
                return billDate.getMonth() == today.getMonth() && billDate.getYear() == today.getYear();
            case "Tùy chọn":
                return isBetweenInclusive(billDate, fromDate, toDate);
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
                || contains(String.valueOf(bill.getBillDate()), keyword);
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

        new Thread(() -> {
            try {
                // Shift Info
                String shiftText = "Chưa vào ca";
                if (restaurant.MockDataStore.shifts != null && Login.LoginController.loggedInAccount != null) {
                    for (shift.ShiftController.ShiftModel sh : restaurant.MockDataStore.shifts) {
                        if (sh.getManv() != null && sh.getManv().equalsIgnoreCase(Login.LoginController.loggedInAccount.getMaNV())) {
                            shiftText = sh.getTenca();
                            break;
                        }
                    }
                }

                // Active Tables (busy tables count)
                int activeTables = 0;
                if (restaurant.MockDataStore.tables != null) {
                    for (table.TableController.TableModel t : restaurant.MockDataStore.tables) {
                        if ("Có khách".equalsIgnoreCase(t.getTrangthai()) || "Đang dùng".equalsIgnoreCase(t.getTrangthai())) {
                            activeTables++;
                        }
                    }
                }

                // Pending Bills
                int pendingBills = 0;
                if (restaurant.MockDataStore.bills != null) {
                    for (bill.BillController.BillModel b : restaurant.MockDataStore.bills) {
                        if ("Chờ thanh toán".equalsIgnoreCase(b.getStatus()) || "Đang xử lý".equalsIgnoreCase(b.getStatus()) || "Chưa thanh toán".equalsIgnoreCase(b.getStatus())) {
                            pendingBills++;
                        }
                    }
                }

                // Today Bookings
                int todayBookings = 0;
                String todayStr = LocalDate.now().toString();
                if (restaurant.MockDataStore.bookings != null) {
                    for (booking.BookingController.BookingModel bk : restaurant.MockDataStore.bookings) {
                        if (bk.getTime() != null && bk.getTime().startsWith(todayStr)) {
                            todayBookings++;
                        }
                    }
                }

                final String finalShift = shiftText;
                final int finalActive = activeTables;
                final int finalPending = pendingBills;
                final int finalTodayBookings = todayBookings;

                Platform.runLater(() -> {
                    if (shiftLabel != null) shiftLabel.setText(finalShift);
                    if (activeTablesLabel != null) activeTablesLabel.setText(String.valueOf(finalActive));
                    if (pendingBillsLabel != null) pendingBillsLabel.setText(String.valueOf(finalPending));
                    if (todayBookingsLabel != null) todayBookingsLabel.setText(String.valueOf(finalTodayBookings));

                    // Load recent bills table
                    ObservableList<BillModel> recentList = FXCollections.observableArrayList();
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
                                today.minusDays(index++)));
                    }
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

        public BillModel(String id, String customer, String table, String total, String payment, String status,
                LocalDate billDate) {
            this.id = id;
            this.customer = customer;
            this.table = table;
            this.total = total;
            this.payment = payment;
            this.status = status;
            this.billDate = billDate;
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
}
