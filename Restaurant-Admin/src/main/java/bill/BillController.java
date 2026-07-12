package bill;

import restaurant.MockDataStore;
import java.math.BigDecimal;
import java.net.URL;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.layout.StackPane;
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
import model.HoaDon;
import model.KhachHang;
import model.BanAn;
import model.NhanVien;
import network.Action;
import network.Module;
import network.Request;
import network.Response;
import network.SocketClient;

public class BillController implements Initializable {

    @FXML
    private TableView<BillModel> billTable;

    @FXML
    private Button viewDetailBtn;



    @FXML
    private TableColumn<BillModel, String> idColumn;

    @FXML
    private TableColumn<BillModel, String> customerColumn;

    @FXML
    private TableColumn<BillModel, String> tableColumn;

    @FXML
    private TableColumn<BillModel, String> employeeColumn;

    @FXML
    private TableColumn<BillModel, String> timeVaoColumn;

    @FXML
    private TableColumn<BillModel, String> timeRaColumn;

    @FXML
    private TableColumn<BillModel, String> totalColumn;

    @FXML
    private TableColumn<BillModel, String> giamGiaColumn;

    @FXML
    private TableColumn<BillModel, String> paymentColumn;

    @FXML
    private TableColumn<BillModel, String> statusColumn;

    @FXML
    private TextField searchField;

    @FXML
    private Label totalRevenueLabel;

    @FXML
    private Label totalBillsLabel;

    @FXML
    private Label paidBillsLabel;

    @FXML
    private Label pendingBillsLabel;

    @FXML
    private ComboBox<String> filterCombo;

    @FXML
    private DatePicker fromDate;

    @FXML
    private DatePicker toDate;

    @FXML
    private Button createBtn;

    @FXML
    private Button editBtn;

    @FXML
    private Button deleteBtn;

    private static final java.text.SimpleDateFormat dateFormat = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private final ObservableList<BillModel> masterList = FXCollections.observableArrayList();
    private FilteredList<BillModel> filteredList;
    private javafx.animation.Timeline autoReloadTimeline;
    private boolean isReloading = false;

    private final Map<String, String> customerMap = new HashMap<>();
    private final Map<String, String> tableMap = new HashMap<>();
    private final Map<String, String> employeeMap = new HashMap<>();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        setupTable();
        
        filteredList = new FilteredList<>(masterList, p -> true);
        SortedList<BillModel> sortedList = new SortedList<>(filteredList);
        sortedList.comparatorProperty().bind(billTable.comparatorProperty());
        billTable.setItems(sortedList);

        loadData();
        setupSearch();
        setupFilters();
        startAutoReload();

        // Double-click row handler to open details
        billTable.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2 && billTable.getSelectionModel().getSelectedItem() != null) {
                openBillDetailsDialog(billTable.getSelectionModel().getSelectedItem());
            }
        });
    }

    private void setupFilters() {
        filterCombo.setItems(FXCollections.observableArrayList(
                "Tất cả", "Đã thanh toán", "Đang xử lý", "Chờ thanh toán", "Đã hủy"));
        filterCombo.getSelectionModel().select("Tất cả");
        
        filterCombo.valueProperty().addListener((obs, oldVal, newVal) -> applyFilters());
        fromDate.valueProperty().addListener((obs, oldVal, newVal) -> applyFilters());
        toDate.valueProperty().addListener((obs, oldVal, newVal) -> applyFilters());
    }

    private void setupTable() {
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        customerColumn.setCellValueFactory(new PropertyValueFactory<>("customer"));
        tableColumn.setCellValueFactory(new PropertyValueFactory<>("table"));
        employeeColumn.setCellValueFactory(new PropertyValueFactory<>("employee"));
        timeVaoColumn.setCellValueFactory(new PropertyValueFactory<>("timeVaoStr"));
        timeRaColumn.setCellValueFactory(new PropertyValueFactory<>("timeRaStr"));
        totalColumn.setCellValueFactory(new PropertyValueFactory<>("total"));
        giamGiaColumn.setCellValueFactory(new PropertyValueFactory<>("giamGiaStr"));
        paymentColumn.setCellValueFactory(new PropertyValueFactory<>("payment"));
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));

        totalColumn.setComparator((s1, s2) -> {
            try {
                double d1 = Double.parseDouble(s1.replace(" VNĐ", "").replace(",", "").trim());
                double d2 = Double.parseDouble(s2.replace(" VNĐ", "").replace(",", "").trim());
                return Double.compare(d1, d2);
            } catch (Exception e) {
                return s1.compareTo(s2);
            }
        });

        giamGiaColumn.setComparator((s1, s2) -> {
            try {
                double d1 = Double.parseDouble(s1.replace(" VNĐ", "").replace(",", "").trim());
                double d2 = Double.parseDouble(s2.replace(" VNĐ", "").replace(",", "").trim());
                return Double.compare(d1, d2);
            } catch (Exception e) {
                return s1.compareTo(s2);
            }
        });

        paymentColumn.setComparator((s1, s2) -> {
            try {
                double d1 = Double.parseDouble(s1.replace(" VNĐ", "").replace(",", "").trim());
                double d2 = Double.parseDouble(s2.replace(" VNĐ", "").replace(",", "").trim());
                return Double.compare(d1, d2);
            } catch (Exception e) {
                return s1.compareTo(s2);
            }
        });

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
                    } else if (item.equalsIgnoreCase("Đã hủy")) {
                        label.getStyleClass().add("status-pending");
                    } else {
                        label.getStyleClass().add("status-pending");
                    }
                    HBox container = new HBox(label);
                    container.setAlignment(Pos.CENTER);
                    setGraphic(container);
                }
            }
        });

    }

    private void loadData() {
        setLoadingState(true);
        new Thread(() -> {
            try {
                Request khReq = new Request(Module.KHACHHANG, Action.GET_ALL, null);
                Response khRes = SocketClient.getInstance().sendRequest(khReq);
                if (khRes != null && khRes.isSuccess()) {
                    List<KhachHang> khList = (List<KhachHang>) khRes.getData();
                    customerMap.clear();
                    if (khList != null) {
                        for (KhachHang kh : khList) {
                            customerMap.put(kh.getMaKH(), kh.getTenKH());
                        }
                    }
                }

                Request banReq = new Request(Module.BANAN, Action.GET_ALL, null);
                Response banRes = SocketClient.getInstance().sendRequest(banReq);
                if (banRes != null && banRes.isSuccess()) {
                    List<BanAn> banList = (List<BanAn>) banRes.getData();
                    tableMap.clear();
                    if (banList != null) {
                        for (BanAn ban : banList) {
                            tableMap.put(ban.getMaBan(), ban.getTenBan());
                        }
                    }
                }

                Request nvReq = new Request(Module.NHANVIEN, Action.GET_ALL, null);
                Response nvRes = SocketClient.getInstance().sendRequest(nvReq);
                if (nvRes != null && nvRes.isSuccess()) {
                    List<NhanVien> nvList = (List<NhanVien>) nvRes.getData();
                    employeeMap.clear();
                    if (nvList != null) {
                        for (NhanVien nv : nvList) {
                            employeeMap.put(nv.getMaNV(), nv.getHoTen());
                        }
                    }
                }

                Request billReq = new Request(Module.HOADON, Action.GET_ALL, null);
                Response billRes = SocketClient.getInstance().sendRequest(billReq);

                Platform.runLater(() -> {
                    if (billRes != null && billRes.isSuccess()) {
                        List<HoaDon> serverList = (List<HoaDon>) billRes.getData();

                        BillModel selected = billTable.getSelectionModel().getSelectedItem();
                        String selectedId = selected != null ? selected.getId() : null;

                        masterList.clear();
                        MockDataStore.bills.clear();

                        if (serverList != null) {
                            for (HoaDon hd : serverList) {
                                String customerName = customerMap.getOrDefault(hd.getMaKH(), hd.getMaKH());
                                String tableName = tableMap.getOrDefault(hd.getMaBan(), hd.getMaBan());
                                String employeeName = employeeMap.getOrDefault(hd.getMaNV(), hd.getMaNV());

                                String timeVaoStr = hd.getTimeVao() != null ? dateFormat.format(hd.getTimeVao()) : "";
                                String timeRaStr = hd.getTimeRa() != null ? dateFormat.format(hd.getTimeRa()) : "";

                                BigDecimal tongTien = hd.getTongTien() != null ? hd.getTongTien() : BigDecimal.ZERO;
                                BigDecimal giamGia = hd.getGiamGia() != null ? hd.getGiamGia() : BigDecimal.ZERO;
                                BigDecimal thanhToan = hd.getThanhToan() != null ? hd.getThanhToan() : tongTien.subtract(giamGia);

                                BillModel model = new BillModel(
                                    hd.getMaHD(),
                                    customerName,
                                    tableName,
                                    employeeName,
                                    timeVaoStr,
                                    timeRaStr,
                                    String.format("%,.0f VNĐ", tongTien.doubleValue()),
                                    String.format("%,.0f VNĐ", giamGia.doubleValue()),
                                    String.format("%,.0f VNĐ", thanhToan.doubleValue()),
                                    hd.getTrangThai(),
                                    hd.getMaKH(),
                                    hd.getMaBan(),
                                    hd.getMaNV(),
                                    hd.getTimeVao(),
                                    hd.getTimeRa(),
                                    tongTien,
                                    giamGia,
                                    thanhToan
                                );
                                masterList.add(model);
                                MockDataStore.bills.add(model);
                            }
                        }

                        applyFilters();

                        if (selectedId != null) {
                            for (BillModel item : billTable.getItems()) {
                                if (item.getId().equals(selectedId)) {
                                    billTable.getSelectionModel().select(item);
                                    break;
                                }
                            }
                        }
                    } else {
                        showAlert(billRes != null ? billRes.getMessage() : "Mất kết nối Server. Không thể tải danh sách hóa đơn!");
                    }
                    setLoadingState(false);
                });
            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() -> {
                    showAlert("Lỗi kết nối hoặc xử lý dữ liệu: " + e.getMessage());
                    setLoadingState(false);
                });
            }
        }).start();
    }

    private void loadDataSilently() {
        if (isReloading) return;
        isReloading = true;
        new Thread(() -> {
            try {
                Request khReq = new Request(Module.KHACHHANG, Action.GET_ALL, null);
                Response khRes = SocketClient.getInstance().sendRequest(khReq);
                if (khRes != null && khRes.isSuccess()) {
                    List<KhachHang> khList = (List<KhachHang>) khRes.getData();
                    customerMap.clear();
                    if (khList != null) {
                        for (KhachHang kh : khList) {
                            customerMap.put(kh.getMaKH(), kh.getTenKH());
                        }
                    }
                }

                Request banReq = new Request(Module.BANAN, Action.GET_ALL, null);
                Response banRes = SocketClient.getInstance().sendRequest(banReq);
                if (banRes != null && banRes.isSuccess()) {
                    List<BanAn> banList = (List<BanAn>) banRes.getData();
                    tableMap.clear();
                    if (banList != null) {
                        for (BanAn ban : banList) {
                            tableMap.put(ban.getMaBan(), ban.getTenBan());
                        }
                    }
                }

                Request nvReq = new Request(Module.NHANVIEN, Action.GET_ALL, null);
                Response nvRes = SocketClient.getInstance().sendRequest(nvReq);
                if (nvRes != null && nvRes.isSuccess()) {
                    List<NhanVien> nvList = (List<NhanVien>) nvRes.getData();
                    employeeMap.clear();
                    if (nvList != null) {
                        for (NhanVien nv : nvList) {
                            employeeMap.put(nv.getMaNV(), nv.getHoTen());
                        }
                    }
                }

                Request billReq = new Request(Module.HOADON, Action.GET_ALL, null);
                Response billRes = SocketClient.getInstance().sendRequest(billReq);

                Platform.runLater(() -> {
                    if (billRes != null && billRes.isSuccess()) {
                        List<HoaDon> serverList = (List<HoaDon>) billRes.getData();

                        BillModel selected = billTable.getSelectionModel().getSelectedItem();
                        String selectedId = selected != null ? selected.getId() : null;

                        masterList.clear();
                        MockDataStore.bills.clear();

                        if (serverList != null) {
                            for (HoaDon hd : serverList) {
                                String customerName = customerMap.getOrDefault(hd.getMaKH(), hd.getMaKH());
                                String tableName = tableMap.getOrDefault(hd.getMaBan(), hd.getMaBan());
                                String employeeName = employeeMap.getOrDefault(hd.getMaNV(), hd.getMaNV());

                                String timeVaoStr = hd.getTimeVao() != null ? dateFormat.format(hd.getTimeVao()) : "";
                                String timeRaStr = hd.getTimeRa() != null ? dateFormat.format(hd.getTimeRa()) : "";

                                BigDecimal tongTien = hd.getTongTien() != null ? hd.getTongTien() : BigDecimal.ZERO;
                                BigDecimal giamGia = hd.getGiamGia() != null ? hd.getGiamGia() : BigDecimal.ZERO;
                                BigDecimal thanhToan = hd.getThanhToan() != null ? hd.getThanhToan() : tongTien.subtract(giamGia);

                                BillModel model = new BillModel(
                                    hd.getMaHD(),
                                    customerName,
                                    tableName,
                                    employeeName,
                                    timeVaoStr,
                                    timeRaStr,
                                    String.format("%,.0f VNĐ", tongTien.doubleValue()),
                                    String.format("%,.0f VNĐ", giamGia.doubleValue()),
                                    String.format("%,.0f VNĐ", thanhToan.doubleValue()),
                                    hd.getTrangThai(),
                                    hd.getMaKH(),
                                    hd.getMaBan(),
                                    hd.getMaNV(),
                                    hd.getTimeVao(),
                                    hd.getTimeRa(),
                                    tongTien,
                                    giamGia,
                                    thanhToan
                                );
                                masterList.add(model);
                                MockDataStore.bills.add(model);
                            }
                        }

                        applyFilters();

                        if (selectedId != null) {
                            for (BillModel item : billTable.getItems()) {
                                if (item.getId().equals(selectedId)) {
                                    billTable.getSelectionModel().select(item);
                                    break;
                                }
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
        autoReloadTimeline = new javafx.animation.Timeline(
            new javafx.animation.KeyFrame(javafx.util.Duration.seconds(10), event -> {
                if (billTable.getScene() == null) {
                    autoReloadTimeline.stop();
                    return;
                }
                loadDataSilently();
            })
        );
        autoReloadTimeline.setCycleCount(javafx.animation.Timeline.INDEFINITE);
        autoReloadTimeline.play();

        billTable.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene == null && autoReloadTimeline != null) {
                autoReloadTimeline.stop();
            }
        });
    }

    private void setLoadingState(boolean loading) {
        if (createBtn != null) createBtn.setDisable(loading);
        if (editBtn != null) editBtn.setDisable(loading);
        if (deleteBtn != null) deleteBtn.setDisable(loading);
        if (searchField != null) searchField.setDisable(loading);
        if (filterCombo != null) filterCombo.setDisable(loading);
        if (fromDate != null) fromDate.setDisable(loading);
        if (toDate != null) toDate.setDisable(loading);
    }

    private void setupSearch() {
        searchField.textProperty().addListener((observable, oldValue, newValue) -> applyFilters());
    }

    private void applyFilters() {
        String keyword = searchField.getText();
        String lowerKeyword = keyword == null ? "" : keyword.toLowerCase().trim();

        String statusFilter = filterCombo.getValue();
        LocalDate start = fromDate.getValue();
        LocalDate end = toDate.getValue();

        filteredList.setPredicate(bill -> {
            if (!lowerKeyword.isEmpty()) {
                boolean matches = bill.getId().toLowerCase().contains(lowerKeyword) ||
                                  bill.getCustomer().toLowerCase().contains(lowerKeyword) ||
                                  bill.getTable().toLowerCase().contains(lowerKeyword) ||
                                  (bill.getEmployee() != null && bill.getEmployee().toLowerCase().contains(lowerKeyword)) ||
                                  (bill.getPayment() != null && bill.getPayment().toLowerCase().contains(lowerKeyword)) ||
                                  (bill.getStatus() != null && bill.getStatus().toLowerCase().contains(lowerKeyword)) ||
                                  (bill.getMaKH() != null && bill.getMaKH().toLowerCase().contains(lowerKeyword)) ||
                                  (bill.getMaBan() != null && bill.getMaBan().toLowerCase().contains(lowerKeyword)) ||
                                  (bill.getMaNV() != null && bill.getMaNV().toLowerCase().contains(lowerKeyword));
                if (!matches) return false;
            }

            if (statusFilter != null && !statusFilter.equals("Tất cả")) {
                String billStatus = bill.getStatus();
                if (billStatus == null) return false;
                if (statusFilter.equalsIgnoreCase("Chờ thanh toán") || statusFilter.equalsIgnoreCase("Đang xử lý")) {
                    if (!billStatus.equalsIgnoreCase("Chờ thanh toán") &&
                        !billStatus.equalsIgnoreCase("Đang xử lý") &&
                        !billStatus.equalsIgnoreCase("Chưa thanh toán")) {
                        return false;
                    }
                } else {
                    if (!billStatus.equalsIgnoreCase(statusFilter)) {
                        return false;
                    }
                }
            }

            if (start != null) {
                if (bill.getTimeVao() == null) return false;
                LocalDate vaoDate = bill.getTimeVao().toLocalDateTime().toLocalDate();
                if (vaoDate.isBefore(start)) return false;
            }
            if (end != null) {
                if (bill.getTimeVao() == null) return false;
                LocalDate vaoDate = bill.getTimeVao().toLocalDateTime().toLocalDate();
                if (vaoDate.isAfter(end)) return false;
            }

            return true;
        });

        updateStats();
    }

    private void updateStats() {
        int totalBills = filteredList.size();
        int paidBills = 0;
        int pendingBills = 0;
        long totalRevenue = 0;

        for (BillModel bill : filteredList) {
            long value = bill.getTongTien() != null ? bill.getTongTien().longValue() : 0;

            if (bill.getStatus() != null && bill.getStatus().equalsIgnoreCase("Đã thanh toán")) {
                paidBills++;
                totalRevenue += value;
            } else {
                pendingBills++;
            }
        }

        totalRevenueLabel.setText(String.format("%,d VNĐ", totalRevenue));
        totalBillsLabel.setText(String.valueOf(totalBills));
        paidBillsLabel.setText(String.valueOf(paidBills));
        pendingBillsLabel.setText(String.valueOf(pendingBills));
    }

    public static class BillModel {
        private final String id;
        private final String customer;
        private final String table;
        private final String employee;
        private final String timeVaoStr;
        private final String timeRaStr;
        private final String total;
        private final String giamGiaStr;
        private final String payment;
        private final String status;
        
        private final String maKH;
        private final String maBan;
        private final String maNV;
        private final Timestamp timeVao;
        private final Timestamp timeRa;
        private final BigDecimal tongTien;
        private final BigDecimal giamGia;
        private final BigDecimal thanhToan;

        public BillModel(String id, String customer, String table, String employee, String timeVaoStr, String timeRaStr,
                         String total, String giamGiaStr, String payment, String status,
                         String maKH, String maBan, String maNV, Timestamp timeVao, Timestamp timeRa,
                         BigDecimal tongTien, BigDecimal giamGia, BigDecimal thanhToan) {
            this.id = id;
            this.customer = customer;
            this.table = table;
            this.employee = employee;
            this.timeVaoStr = timeVaoStr;
            this.timeRaStr = timeRaStr;
            this.total = total;
            this.giamGiaStr = giamGiaStr;
            this.payment = payment;
            this.status = status;
            this.maKH = maKH;
            this.maBan = maBan;
            this.maNV = maNV;
            this.timeVao = timeVao;
            this.timeRa = timeRa;
            this.tongTien = tongTien;
            this.giamGia = giamGia;
            this.thanhToan = thanhToan;
        }

        // Backwards compatibility constructor
        public BillModel(String id, String customer, String table, String total, String payment, String status,
                         String maKH, String maBan, String maNV, Timestamp timeVao, Timestamp timeRa,
                         BigDecimal tongTien, BigDecimal giamGia, BigDecimal thanhToan) {
            this.id = id;
            this.customer = customer;
            this.table = table;
            this.employee = maNV;
            java.text.SimpleDateFormat df = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            this.timeVaoStr = timeVao != null ? df.format(timeVao) : "";
            this.timeRaStr = timeRa != null ? df.format(timeRa) : "";
            this.total = total;
            this.giamGiaStr = giamGia != null ? String.format("%,.0f VNĐ", giamGia.doubleValue()) : "0 VNĐ";
            this.payment = payment;
            this.status = status;
            this.maKH = maKH;
            this.maBan = maBan;
            this.maNV = maNV;
            this.timeVao = timeVao;
            this.timeRa = timeRa;
            this.tongTien = tongTien;
            this.giamGia = giamGia;
            this.thanhToan = thanhToan;
        }

        public String getId() { return id; }
        public String getCustomer() { return customer; }
        public String getTable() { return table; }
        public String getEmployee() { return employee; }
        public String getTimeVaoStr() { return timeVaoStr; }
        public String getTimeRaStr() { return timeRaStr; }
        public String getTotal() { return total; }
        public String getGiamGiaStr() { return giamGiaStr; }
        public String getPayment() { return payment; }
        public String getStatus() { return status; }
        public String getMaKH() { return maKH; }
        public String getMaBan() { return maBan; }
        public String getMaNV() { return maNV; }
        public Timestamp getTimeVao() { return timeVao; }
        public Timestamp getTimeRa() { return timeRa; }
        public BigDecimal getTongTien() { return tongTien; }
        public BigDecimal getGiamGia() { return giamGia; }
        public BigDecimal getThanhToan() { return thanhToan; }
    }

    public static BillModel selectedBill = null;

    @FXML
    private void handleAddNew() {
        navigate("/fxml/billadd.fxml");
    }

    @FXML
    private void handleEdit() {
        BillModel selected = billTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Vui lòng chọn một hóa đơn từ danh sách để sửa.");
            return;
        }
        selectedBill = selected;
        navigate("/fxml/billedit.fxml");
    }

    @FXML
    private void handleDelete() {
        BillModel selected = billTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Vui lòng chọn một hóa đơn từ danh sách để xóa.");
            return;
        }
        selectedBill = selected;
        navigate("/fxml/billdelete.fxml");
    }

    private void navigate(String fxmlPath) {
        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource(fxmlPath));
            javafx.scene.Parent view = loader.load();
            javafx.scene.layout.AnchorPane parent = (javafx.scene.layout.AnchorPane) billTable.getScene().lookup("#contentArea");
            if (parent != null) {
                parent.getChildren().clear();
                parent.getChildren().add(view);
                javafx.scene.layout.AnchorPane.setTopAnchor(view, 0.0);
                javafx.scene.layout.AnchorPane.setBottomAnchor(view, 0.0);
                javafx.scene.layout.AnchorPane.setLeftAnchor(view, 0.0);
                javafx.scene.layout.AnchorPane.setRightAnchor(view, 0.0);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void openBillDetailsDialog(BillModel bill) {
        if (bill == null) return;
        selectedBill = bill;
        navigate("/fxml/billdetail.fxml");
    }

    @FXML
    private void handleViewDetail() {
        BillModel selected = billTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Vui lòng chọn một hóa đơn từ danh sách để xem chi tiết.");
            return;
        }
        openBillDetailsDialog(selected);
    }

    private void showAlert(String message) {
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.WARNING);
        alert.setTitle("Thông báo");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
