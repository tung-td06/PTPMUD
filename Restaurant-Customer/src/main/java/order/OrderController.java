package order;

import java.net.URL;
import java.sql.Date;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.animation.Timeline;
import javafx.animation.KeyFrame;
import javafx.util.Duration;

import restaurant.MockDataStore;
import employee.EmployeeController.EmployeeModel;
import table.TableController.TableModel;
import food.FoodController.FoodModel;
import bill.BillController.BillModel;
import network.Action;
import network.Module;
import network.Request;
import network.Response;
import network.SocketClient;

public class OrderController implements Initializable {

    @FXML private TableView<OrderModel> orderTable;
    @FXML private TableColumn<OrderModel, Object> colStt;
    @FXML private TableColumn<OrderModel, String> colMaOrder;
    @FXML private TableColumn<OrderModel, String> colMaban;
    @FXML private TableColumn<OrderModel, String> colManv;
    @FXML private TableColumn<OrderModel, String> colMahd;
    @FXML private TableColumn<OrderModel, Date> colNgaytao;
    @FXML private TableColumn<OrderModel, String> colTrangthai;
    @FXML private TableColumn<OrderModel, Integer> colTongMon;
    @FXML private TableColumn<OrderModel, Integer> colTongSoLuong;
    @FXML private TableColumn<OrderModel, Double> colTongTien;

    @FXML private ComboBox<String> filterStatusCombo;
    @FXML private ComboBox<String> filterTableCombo;
    @FXML private ComboBox<String> filterEmployeeCombo;
    @FXML private DatePicker dpFrom;
    @FXML private DatePicker dpTo;
    @FXML private TextField searchField;
    @FXML private Button createBtn;
    @FXML private Button viewDetailBtn;
    @FXML private Button editBtn;
    @FXML private Button deleteBtn;

    @FXML private Label totalOrdersLabel;
    @FXML private Label activeOrdersLabel;
    @FXML private Label totalRevenueLabel;

    @FXML private VBox emptyStatePane;
    @FXML private VBox loadingOverlay;

    // ObservableLists for rendering
    private final ObservableList<OrderModel> masterList = FXCollections.observableArrayList();
    private final ObservableList<OrderModel> displayList = FXCollections.observableArrayList();

    public static OrderModel selectedOrder = null;

    private final OrderService orderService = new OrderService();
    private Timeline autoReloadTimeline;
    private boolean isReloading = false;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        setupTableColumns();
        setupFilterControls();
        
        // Dynamic visibility bindings for Empty State
        emptyStatePane.visibleProperty().bind(
            Bindings.isEmpty(displayList).and(loadingOverlay.visibleProperty().not())
        );

        // Fetch lookup reference data from Server, then load orders
        loadServerLookupData();
        handleRefresh();
        startAutoReload();

        // Double-click row handler to open details
        orderTable.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2 && orderTable.getSelectionModel().getSelectedItem() != null) {
                openDetailDialog(orderTable.getSelectionModel().getSelectedItem());
            }
        });
    }

    private void setupTableColumns() {
        // STT Auto Increment Column
        colStt.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(Object item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setText(null);
                } else {
                    setText(String.valueOf(getIndex() + 1));
                }
            }
        });

        colMaOrder.setCellValueFactory(new PropertyValueFactory<>("maorder"));
        colMaban.setCellValueFactory(new PropertyValueFactory<>("maban"));
        colManv.setCellValueFactory(new PropertyValueFactory<>("manv"));
        colMahd.setCellValueFactory(new PropertyValueFactory<>("mahd"));
        
        // Correct chronological Date Sorting with Formatted Output
        colNgaytao.setCellValueFactory(new PropertyValueFactory<>("ngaytao"));
        colNgaytao.setCellFactory(column -> new TableCell<>() {
            private final SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");
            @Override
            protected void updateItem(Date item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(sdf.format(item));
                }
            }
        });

        colTrangthai.setCellValueFactory(new PropertyValueFactory<>("trangthai"));
        colTongMon.setCellValueFactory(new PropertyValueFactory<>("tongMon"));
        colTongSoLuong.setCellValueFactory(new PropertyValueFactory<>("tongSoLuong"));
        colTongTien.setCellValueFactory(new PropertyValueFactory<>("tongTien"));

        // Format Hóa đơn col to render "--" if empty
        colMahd.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setText(null);
                } else if (item == null || item.trim().isEmpty()) {
                    setText("--");
                } else {
                    setText(item);
                }
            }
        });

        // Format Trạng thái Status Badges
        colTrangthai.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                } else {
                    Label label = new Label(item);
                    label.getStyleClass().add("status-tag");
                    if (item.equalsIgnoreCase("Đang order")) {
                        label.getStyleClass().add("status-order-pending");
                    } else if (item.equalsIgnoreCase("Đang phục vụ")) {
                        label.getStyleClass().add("status-order-serving");
                    } else if (item.equalsIgnoreCase("Hoàn thành")) {
                        label.getStyleClass().add("status-order-completed");
                    } else if (item.equalsIgnoreCase("Đã hủy")) {
                        label.getStyleClass().add("status-order-cancelled");
                    }
                    HBox box = new HBox(label);
                    box.setAlignment(Pos.CENTER);
                    setGraphic(box);
                }
            }
        });

        // Format Tổng tiền Currency Format
        colTongTien.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    DecimalFormat df = new DecimalFormat("#,###");
                    setText(df.format(item) + " VNĐ");
                }
            }
        });

        orderTable.setItems(displayList);
    }

    private void setupFilterControls() {
        // Status filter
        filterStatusCombo.setItems(FXCollections.observableArrayList(
            "Tất cả", "Đang order", "Đang phục vụ", "Hoàn thành", "Đã hủy"
        ));
        filterStatusCombo.getSelectionModel().select("Tất cả");

        // Initial setup for combo items, will be populated on lookup load
        updateTableCombos();
        updateEmployeeCombos();

        // Listeners for realtime search and filtering
        searchField.textProperty().addListener((obs, oldVal, newVal) -> applyFilters());
        filterStatusCombo.valueProperty().addListener((obs, oldVal, newVal) -> applyFilters());
        filterTableCombo.valueProperty().addListener((obs, oldVal, newVal) -> applyFilters());
        filterEmployeeCombo.valueProperty().addListener((obs, oldVal, newVal) -> applyFilters());
        dpFrom.valueProperty().addListener((obs, oldVal, newVal) -> applyFilters());
        dpTo.valueProperty().addListener((obs, oldVal, newVal) -> applyFilters());
    }

    private void updateTableCombos() {
        String selectedTable = filterTableCombo.getValue();
        ObservableList<String> tableOptions = FXCollections.observableArrayList("Tất cả");
        if (MockDataStore.tables != null) {
            for (TableModel t : MockDataStore.tables) {
                tableOptions.add(t.getMaban());
            }
        }
        filterTableCombo.setItems(tableOptions);
        if (tableOptions.contains(selectedTable)) {
            filterTableCombo.setValue(selectedTable);
        } else {
            filterTableCombo.setValue("Tất cả");
        }
    }

    private void updateEmployeeCombos() {
        String selectedEmployee = filterEmployeeCombo.getValue();
        ObservableList<String> employeeOptions = FXCollections.observableArrayList("Tất cả");
        if (MockDataStore.employees != null) {
            for (EmployeeModel e : MockDataStore.employees) {
                employeeOptions.add(e.getId() + " - " + e.getName());
            }
        }
        filterEmployeeCombo.setItems(employeeOptions);
        if (employeeOptions.contains(selectedEmployee)) {
            filterEmployeeCombo.setValue(selectedEmployee);
        } else {
            filterEmployeeCombo.setValue("Tất cả");
        }
    }

    private void loadServerLookupData() {
        new Thread(() -> {
            try {
                // 1. Load Tables
                Request tableReq = new Request(Module.BANAN, Action.GET_ALL, null);
                Response tableRes = SocketClient.getInstance().sendRequest(tableReq);
                if (tableRes != null && tableRes.isSuccess()) {
                    List<model.BanAn> serverList = (List<model.BanAn>) tableRes.getData();
                    Platform.runLater(() -> {
                        MockDataStore.tables.clear();
                        if (serverList != null) {
                            for (model.BanAn ba : serverList) {
                                MockDataStore.tables.add(new TableModel(
                                    ba.getMaBan(),
                                    ba.getTenBan(),
                                    ba.getKhuVuc(),
                                    ba.getTrangThai()
                                ));
                            }
                        }
                        updateTableCombos();
                    });
                }

                // 2. Load Employees
                Request empReq = new Request(Module.NHANVIEN, Action.GET_ALL, null);
                Response empRes = SocketClient.getInstance().sendRequest(empReq);
                if (empRes != null && empRes.isSuccess()) {
                    List<model.NhanVien> serverList = (List<model.NhanVien>) empRes.getData();
                    Platform.runLater(() -> {
                        MockDataStore.employees.clear();
                        if (serverList != null) {
                            for (model.NhanVien nv : serverList) {
                                MockDataStore.employees.add(new EmployeeModel(
                                    nv.getMaNV(),
                                    nv.getHoTen(),
                                    nv.getChucVu(),
                                    nv.getSdt(),
                                    nv.getNote(),
                                    nv.getTrangThai()
                                ));
                            }
                        }
                        updateEmployeeCombos();
                    });
                }

                // 3. Load Foods
                Request foodReq = new Request(Module.MONAN, Action.GET_ALL, null);
                Response foodRes = SocketClient.getInstance().sendRequest(foodReq);
                if (foodRes != null && foodRes.isSuccess()) {
                    List<model.MonAn> serverList = (List<model.MonAn>) foodRes.getData();
                    Platform.runLater(() -> {
                        MockDataStore.foods.clear();
                        if (serverList != null) {
                            for (model.MonAn ma : serverList) {
                                MockDataStore.foods.add(new FoodModel(
                                    ma.getMaMon(),
                                    ma.getTenMon(),
                                    ma.getMaLoai(),
                                    String.format("%,.0f VNĐ", ma.getDonGia() != null ? ma.getDonGia().doubleValue() : 0.0),
                                    ma.getTrangThai()
                                ));
                            }
                        }
                    });
                }

                // 4. Load Bills
                Request billReq = new Request(Module.HOADON, Action.GET_ALL, null);
                Response billRes = SocketClient.getInstance().sendRequest(billReq);
                if (billRes != null && billRes.isSuccess()) {
                    List<model.HoaDon> serverList = (List<model.HoaDon>) billRes.getData();
                    Platform.runLater(() -> {
                        MockDataStore.bills.clear();
                        if (serverList != null) {
                            for (model.HoaDon hd : serverList) {
                                MockDataStore.bills.add(new BillModel(
                                    hd.getMaHD(),
                                    "",
                                    "",
                                    "",
                                    "",
                                    "",
                                    String.format("%,.0f VNĐ", hd.getTongTien() != null ? hd.getTongTien().doubleValue() : 0.0),
                                    "",
                                    "",
                                    hd.getTrangThai(),
                                    hd.getMaKH(),
                                    hd.getMaBan(),
                                    hd.getMaNV(),
                                    hd.getTimeVao(),
                                    hd.getTimeRa(),
                                    hd.getTongTien(),
                                    hd.getGiamGia(),
                                    hd.getThanhToan()
                                ));
                            }
                        }
                    });
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void showLoading(boolean show) {
        loadingOverlay.setVisible(show);
    }

    @FXML
    private void handleRefresh() {
        showLoading(true);
        loadServerLookupData();
        new Thread(() -> {
            try {
                List<OrderModel> orders = orderService.getAllOrders();
                Platform.runLater(() -> {
                    // Save selection
                    OrderModel selected = orderTable.getSelectionModel().getSelectedItem();

                    masterList.clear();
                    masterList.addAll(orders);
                    applyFilters();

                    // Restore selection
                    if (selected != null) {
                        for (OrderModel o : displayList) {
                            if (o.getMaorder().equalsIgnoreCase(selected.getMaorder())) {
                                orderTable.getSelectionModel().select(o);
                                break;
                            }
                        }
                    }
                    showLoading(false);
                });
            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() -> {
                    showAlert("Lỗi tải dữ liệu từ Server: " + e.getMessage());
                    showLoading(false);
                });
            }
        }).start();
    }

    private void loadDataSilently() {
        isReloading = true;
        new Thread(() -> {
            try {
                List<OrderModel> orders = orderService.getAllOrders();
                Platform.runLater(() -> {
                    OrderModel selected = orderTable.getSelectionModel().getSelectedItem();

                    masterList.clear();
                    masterList.addAll(orders);
                    applyFilters();

                    if (selected != null) {
                        for (OrderModel o : displayList) {
                            if (o.getMaorder().equalsIgnoreCase(selected.getMaorder())) {
                                orderTable.getSelectionModel().select(o);
                                break;
                            }
                        }
                    }
                    isReloading = false;
                });
            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() -> {
                    isReloading = false;
                });
            }
        }).start();
    }

    private void startAutoReload() {
        if (autoReloadTimeline != null) {
            autoReloadTimeline.stop();
        }
        autoReloadTimeline = new Timeline(
            new KeyFrame(Duration.seconds(10), event -> {
                if (orderTable.getScene() == null) {
                    autoReloadTimeline.stop();
                    return;
                }
                if (!isReloading) {
                    loadDataSilently();
                }
            })
        );
        autoReloadTimeline.setCycleCount(Timeline.INDEFINITE);
        autoReloadTimeline.play();

        orderTable.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene == null && autoReloadTimeline != null) {
                autoReloadTimeline.stop();
            }
        });
    }

    private void applyFilters() {
        String keyword = searchField.getText() == null ? "" : searchField.getText().trim().toLowerCase();
        String selectedStatus = filterStatusCombo.getValue();
        String selectedTable = filterTableCombo.getValue();
        String selectedEmployee = filterEmployeeCombo.getValue();
        LocalDate fromDate = dpFrom.getValue();
        LocalDate toDate = dpTo.getValue();

        List<OrderModel> filtered = new ArrayList<>();
        LocalDate today = LocalDate.now();

        for (OrderModel order : masterList) {
            LocalDate orderLocalDate = null;
            if (order.getNgaytao() != null) {
                orderLocalDate = order.getNgaytao().toLocalDate();
            }

            // Time filters preset keywords in Search box
            boolean matchesDatePreset = false;
            if (orderLocalDate != null) {
                if (keyword.equalsIgnoreCase("hôm nay") || keyword.equalsIgnoreCase("hom nay")) {
                    matchesDatePreset = orderLocalDate.isEqual(today);
                } else if (keyword.equalsIgnoreCase("tuần này") || keyword.equalsIgnoreCase("tuan nay")) {
                    LocalDate startOfWeek = today.minusDays(today.getDayOfWeek().getValue() - 1);
                    LocalDate endOfWeek = startOfWeek.plusDays(6);
                    matchesDatePreset = !orderLocalDate.isBefore(startOfWeek) && !orderLocalDate.isAfter(endOfWeek);
                } else if (keyword.equalsIgnoreCase("tháng này") || keyword.equalsIgnoreCase("thang nay")) {
                    matchesDatePreset = (orderLocalDate.getYear() == today.getYear() && orderLocalDate.getMonth() == today.getMonth());
                }
            }

            // 1. Search text filter
            boolean matchesSearch = keyword.isEmpty() 
                || order.getMaorder().toLowerCase().contains(keyword)
                || order.getMaban().toLowerCase().contains(keyword)
                || (order.getManv() != null && order.getManv().toLowerCase().contains(keyword))
                || (order.getMahd() != null && order.getMahd().toLowerCase().contains(keyword))
                || matchesDatePreset;

            // 2. Status filter
            boolean matchesStatus = selectedStatus == null || selectedStatus.equals("Tất cả") 
                || order.getTrangthai().equalsIgnoreCase(selectedStatus);

            // 3. Table filter
            boolean matchesTable = selectedTable == null || selectedTable.equals("Tất cả")
                || order.getMaban().equalsIgnoreCase(selectedTable);

            // 4. Employee filter
            boolean matchesEmployee = selectedEmployee == null || selectedEmployee.equals("Tất cả");
            if (!matchesEmployee && selectedEmployee != null) {
                String empId = selectedEmployee.split(" - ")[0];
                matchesEmployee = order.getManv() != null && order.getManv().equalsIgnoreCase(empId);
            }

            // 5. Date picker filter
            boolean matchesDate = true;
            if (orderLocalDate != null) {
                if (fromDate != null && orderLocalDate.isBefore(fromDate)) {
                    matchesDate = false;
                }
                if (toDate != null && orderLocalDate.isAfter(toDate)) {
                    matchesDate = false;
                }
            } else if (fromDate != null || toDate != null) {
                matchesDate = false;
            }

            if (matchesSearch && matchesStatus && matchesTable && matchesEmployee && matchesDate) {
                filtered.add(order);
            }
        }

        displayList.setAll(filtered);
        updateStatsCards();
    }

    private void updateStatsCards() {
        int total = masterList.size();
        int active = 0;
        double revenue = 0.0;

        for (OrderModel order : masterList) {
            if (order.getTrangthai().equalsIgnoreCase("Đang order") 
                    || order.getTrangthai().equalsIgnoreCase("Đang phục vụ")) {
                active++;
            }
            if (order.getTrangthai().equalsIgnoreCase("Hoàn thành")) {
                revenue += order.getTongTien();
            }
        }

        totalOrdersLabel.setText(String.valueOf(total));
        activeOrdersLabel.setText(String.valueOf(active));
        DecimalFormat df = new DecimalFormat("#,###");
        totalRevenueLabel.setText(df.format(revenue) + " VNĐ");
    }

    private void openDetailDialog(OrderModel order) {
        selectedOrder = order;
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/orderdetail.fxml"));
            Parent view = loader.load();
            AnchorPane parent = (AnchorPane) orderTable.getScene().lookup("#contentArea");
            if (parent != null) {
                parent.getChildren().clear();
                parent.getChildren().add(view);
                AnchorPane.setTopAnchor(view, 0.0);
                AnchorPane.setBottomAnchor(view, 0.0);
                AnchorPane.setLeftAnchor(view, 0.0);
                AnchorPane.setRightAnchor(view, 0.0);
            }
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Không thể mở màn hình chi tiết order: " + e.getMessage());
        }
    }

    private void showAlert(String msg) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Thông báo");
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }

    @FXML
    private void handleViewDetail() {
        OrderModel selected = orderTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Vui lòng chọn một đơn hàng từ danh sách để xem chi tiết.");
            return;
        }
        openDetailDialog(selected);
    }

    @FXML
    private void handleAddNew() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/orderadd.fxml"));
            Parent view = loader.load();
            AnchorPane parent = (AnchorPane) orderTable.getScene().lookup("#contentArea");
            if (parent != null) {
                parent.getChildren().clear();
                parent.getChildren().add(view);
                AnchorPane.setTopAnchor(view, 0.0);
                AnchorPane.setBottomAnchor(view, 0.0);
                AnchorPane.setLeftAnchor(view, 0.0);
                AnchorPane.setRightAnchor(view, 0.0);
            }
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Không thể mở màn hình thêm order: " + e.getMessage());
        }
    }

    @FXML
    private void handleEdit() {
        OrderModel selected = orderTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Vui lòng chọn một đơn hàng từ danh sách để sửa.");
            return;
        }
        selectedOrder = selected;
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/orderedit.fxml"));
            Parent view = loader.load();
            AnchorPane parent = (AnchorPane) orderTable.getScene().lookup("#contentArea");
            if (parent != null) {
                parent.getChildren().clear();
                parent.getChildren().add(view);
                AnchorPane.setTopAnchor(view, 0.0);
                AnchorPane.setBottomAnchor(view, 0.0);
                AnchorPane.setLeftAnchor(view, 0.0);
                AnchorPane.setRightAnchor(view, 0.0);
            }
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Không thể mở màn hình sửa order: " + e.getMessage());
        }
    }

    @FXML
    private void handleDelete() {
        OrderModel selected = orderTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Vui lòng chọn một đơn hàng từ danh sách để xóa.");
            return;
        }
        selectedOrder = selected;
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/orderdelete.fxml"));
            Parent view = loader.load();
            AnchorPane parent = (AnchorPane) orderTable.getScene().lookup("#contentArea");
            if (parent != null) {
                parent.getChildren().clear();
                parent.getChildren().add(view);
                AnchorPane.setTopAnchor(view, 0.0);
                AnchorPane.setBottomAnchor(view, 0.0);
                AnchorPane.setLeftAnchor(view, 0.0);
                AnchorPane.setRightAnchor(view, 0.0);
            }
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Không thể mở màn hình xóa order: " + e.getMessage());
        }
    }

    /* ================================= MODEL CLASSES ================================= */

    public static class OrderModel {
        private String maorder;
        private String maban;
        private String manv;
        private String makh;  // Khách hàng tự đặt (nullable)
        private String mahd;
        private Date ngaytao;
        private String trangthai;
        private int tongMon;
        private int tongSoLuong;
        private double tongTien;

        // Constructor gốc (cho admin/nhân viên tạo order không cần maKH)
        public OrderModel(String maorder, String maban, String manv, String mahd, Date ngaytao, String trangthai) {
            this.maorder = maorder;
            this.maban = maban;
            this.manv = manv;
            this.makh = null;
            this.mahd = mahd;
            this.ngaytao = ngaytao;
            this.trangthai = trangthai;
        }

        // Constructor đầy đủ có maKH (cho khách hàng tự đặt qua app)
        public OrderModel(String maorder, String maban, String manv, String makh, String mahd, Date ngaytao, String trangthai) {
            this.maorder = maorder;
            this.maban = maban;
            this.manv = manv;
            this.makh = makh;
            this.mahd = mahd;
            this.ngaytao = ngaytao;
            this.trangthai = trangthai;
        }

        public String getMaorder() { return maorder; }
        public void setMaorder(String maorder) { this.maorder = maorder; }

        public String getMaban() { return maban; }
        public void setMaban(String maban) { this.maban = maban; }

        public String getManv() { return manv; }
        public void setManv(String manv) { this.manv = manv; }

        public String getMakh() { return makh; }
        public void setMakh(String makh) { this.makh = makh; }

        public String getMahd() { return mahd; }
        public void setMahd(String mahd) { this.mahd = mahd; }

        public Date getNgaytao() { return ngaytao; }
        public void setNgaytao(Date ngaytao) { this.ngaytao = ngaytao; }

        public String getNgayTaoStr() {
            if (ngaytao == null) return "";
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");
            try {
                return sdf.format(ngaytao);
            } catch (Exception e) {
                SimpleDateFormat sdf2 = new SimpleDateFormat("dd/MM/yyyy");
                return sdf2.format(ngaytao);
            }
        }

        public String getTrangthai() { return trangthai; }
        public void setTrangthai(String trangthai) { this.trangthai = trangthai; }

        public int getTongMon() { return tongMon; }
        public void setTongMon(int tongMon) { this.tongMon = tongMon; }

        public int getTongSoLuong() { return tongSoLuong; }
        public void setTongSoLuong(int tongSoLuong) { this.tongSoLuong = tongSoLuong; }

        public double getTongTien() { return tongTien; }
        public void setTongTien(double tongTien) { this.tongTien = tongTien; }
    }

    public static class OrderDetailModel {
        private String maorder;
        private String mamon;
        private String tenmon;
        private int soluong;
        private double dongia;
        private String trangthai;

        public OrderDetailModel(String maorder, String mamon, String tenmon, int soluong, double dongia, String trangthai) {
            this.maorder = maorder;
            this.mamon = mamon;
            this.tenmon = tenmon;
            this.soluong = soluong;
            this.dongia = dongia;
            this.trangthai = trangthai;
        }

        public String getMaorder() { return maorder; }
        public String getMamon() { return mamon; }
        public String getTenmon() { return tenmon; }
        public int getSoluong() { return soluong; }
        public double getDongia() { return dongia; }
        public double getThanhtien() { return soluong * dongia; }
        public String getTrangthai() { return trangthai; }
    }
}
