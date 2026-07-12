package booking;

import restaurant.MockDataStore;
import java.net.URL;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
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
import javafx.util.Duration;
import model.BanAn;
import model.DatBan;
import model.KhachHang;
import customer.CustomerController.CustomerModel;
import table.TableController.TableModel;
import network.Action;
import network.Module;
import network.Request;
import network.Response;
import network.SocketClient;

public class BookingController implements Initializable {

    @FXML
    private TableView<BookingModel> bookingTable;

    @FXML
    private TableColumn<BookingModel, String> idColumn;

    @FXML
    private TableColumn<BookingModel, String> customerColumn;

    @FXML
    private TableColumn<BookingModel, String> phoneColumn;

    @FXML
    private TableColumn<BookingModel, Integer> guestsColumn;

    @FXML
    private TableColumn<BookingModel, String> timeColumn;

    @FXML
    private TableColumn<BookingModel, String> tableColumn;

    @FXML
    private TableColumn<BookingModel, String> statusColumn;

    @FXML
    private TableColumn<BookingModel, String> arrivalTimeColumn;

    @FXML
    private TableColumn<BookingModel, String> noteColumn;

    @FXML
    private TableColumn<BookingModel, String> timeRaColumn;

    @FXML
    private TextField searchField;

    @FXML
    private Label totalBookingsLabel;

    @FXML
    private Label confirmedBookingsLabel;

    @FXML
    private Label pendingBookingsLabel;

    @FXML
    private Label cancelledBookingsLabel;

    @FXML
    private ComboBox<String> filterCombo;

    @FXML
    private DatePicker fromDate;

    @FXML
    private DatePicker toDate;

    @FXML
    private Button viewDetailBtn;

    @FXML
    private Button createBtn;

    @FXML
    private Button editBtn;

    @FXML
    private Button deleteBtn;

    private ObservableList<BookingModel> masterList = FXCollections.observableArrayList();
    private Timeline autoReloadTimeline;
    private boolean isReloading = false;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        setupTable();
        loadData();
        setupSearch();
        setupFilters();
        startAutoReload();
    }

    private void setupFilters() {
        filterCombo.setItems(FXCollections.observableArrayList(
                "Tất cả", "Hôm nay", "Đang chờ", "Đã xác nhận", "Đã nhận bàn", "Đã hủy", "Hoàn thành"));
        filterCombo.getSelectionModel().select("Hôm nay");
        filterCombo.setDisable(false);
        fromDate.setDisable(false);
        fromDate.setEditable(false);
        toDate.setDisable(false);
        toDate.setEditable(false);

        // Add action listeners
        filterCombo.valueProperty().addListener((obs, oldVal, newVal) -> applyFilters());
        fromDate.valueProperty().addListener((obs, oldVal, newVal) -> applyFilters());
        toDate.valueProperty().addListener((obs, oldVal, newVal) -> applyFilters());
    }

    private void setupTable() {
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        customerColumn.setCellValueFactory(new PropertyValueFactory<>("customer"));
        guestsColumn.setCellValueFactory(new PropertyValueFactory<>("guests"));
        timeColumn.setCellValueFactory(new PropertyValueFactory<>("time"));
        timeRaColumn.setCellValueFactory(new PropertyValueFactory<>("timeRa"));
        tableColumn.setCellValueFactory(new PropertyValueFactory<>("table"));
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
        arrivalTimeColumn.setCellValueFactory(new PropertyValueFactory<>("arrivalTime"));
        noteColumn.setCellValueFactory(new PropertyValueFactory<>("note"));

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
                    if (item.equalsIgnoreCase("Đã xác nhận") || item.equalsIgnoreCase("Đã nhận bàn") || item.equalsIgnoreCase("Hoàn thành") || item.equalsIgnoreCase("Đang sử dụng") || item.equalsIgnoreCase("Đã hoàn thành")) {
                        label.getStyleClass().add("status-confirmed");
                    } else if (item.equalsIgnoreCase("Chờ xác nhận") || item.equalsIgnoreCase("Đang chờ")) {
                        label.getStyleClass().add("status-pending");
                    } else {
                        label.getStyleClass().add("status-cancelled");
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
                // Fetch Customers
                Request requestKH = new Request(Module.KHACHHANG, Action.GET_ALL, null);
                Response responseKH = SocketClient.getInstance().sendRequest(requestKH);

                // Fetch Tables
                Request requestBan = new Request(Module.BANAN, Action.GET_ALL, null);
                Response responseBan = SocketClient.getInstance().sendRequest(requestBan);

                // Fetch Bookings
                Request requestDB = new Request(Module.DATBAN, Action.GET_ALL, null);
                Response responseDB = SocketClient.getInstance().sendRequest(requestDB);

                Platform.runLater(() -> {
                    if (responseKH != null && responseKH.isSuccess() &&
                            responseBan != null && responseBan.isSuccess() &&
                            responseDB != null && responseDB.isSuccess()) {

                        // Parse Customers
                        List<KhachHang> serverKHList = (List<KhachHang>) responseKH.getData();
                        MockDataStore.customers.clear();
                        MockDataStore.customerIds.clear();
                        MockDataStore.customerNames.clear();
                        Map<String, KhachHang> customerMap = new HashMap<>();
                        if (serverKHList != null) {
                            for (KhachHang kh : serverKHList) {
                                MockDataStore.customers.add(new CustomerModel(kh.getMaKH(), kh.getTenKH(), kh.getSdt(),
                                        kh.getDiemTichLuy()));
                                MockDataStore.customerIds.add(kh.getMaKH());
                                MockDataStore.customerNames.add(kh.getTenKH());
                                customerMap.put(kh.getMaKH(), kh);
                            }
                        }

                        // Parse Tables
                        List<BanAn> serverBanList = (List<BanAn>) responseBan.getData();
                        MockDataStore.tables.clear();
                        Map<String, BanAn> tableMap = new HashMap<>();
                        if (serverBanList != null) {
                            for (BanAn ba : serverBanList) {
                                MockDataStore.tables.add(new TableModel(ba.getMaBan(), ba.getTenBan(), ba.getKhuVuc(),
                                        ba.getTrangThai()));
                                tableMap.put(ba.getMaBan(), ba);
                            }
                        }

                        // Parse Bookings
                        List<DatBan> serverDBList = (List<DatBan>) responseDB.getData();
                        BookingModel selected = bookingTable.getSelectionModel().getSelectedItem();
                        String selectedId = selected != null ? selected.getId() : null;

                        masterList.clear();
                        MockDataStore.bookings.clear();
                        if (serverDBList != null) {
                            for (DatBan db : serverDBList) {
                                KhachHang kh = customerMap.get(db.getMaKH());
                                String custName = kh != null ? kh.getTenKH() : "Khách vãng lai";
                                String custPhone = kh != null ? kh.getSdt() : "";

                                BanAn ba = tableMap.get(db.getMaBan());
                                String tableName = ba != null ? ba.getTenBan() : "Mang về";

                                String status = db.getTrangThai() != null ? db.getTrangThai() : "Đang chờ";

                                String arrivalTime = db.getThoiGianDen() != null
                                        ? formatTimestamp(db.getThoiGianDen())
                                        : "Chưa đến";

                                BookingModel model = new BookingModel(
                                        db.getMaDatBan(),
                                        custName,
                                        custPhone,
                                        db.getSoNguoi(),
                                        formatTimestamp(db.getTimeVao()),
                                        formatTimestamp(db.getTimeRa()),
                                        tableName,
                                        status,
                                        arrivalTime,
                                        db.getNote());
                                masterList.add(model);
                                MockDataStore.bookings.add(model);
                            }
                        }

                        applyFilters();

                        if (selectedId != null) {
                            for (BookingModel item : bookingTable.getItems()) {
                                if (item.getId().equals(selectedId)) {
                                    bookingTable.getSelectionModel().select(item);
                                    break;
                                }
                            }
                        }
                    } else {
                        showAlert("Không thể tải đầy đủ dữ liệu từ Server!");
                    }
                    setLoadingState(false);
                });
            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() -> {
                    showAlert("Lỗi: " + e.getMessage());
                    setLoadingState(false);
                });
            }
        }).start();
    }

    private void loadDataSilently() {
        if (isReloading)
            return;
        isReloading = true;
        new Thread(() -> {
            try {
                // Fetch Customers
                Request requestKH = new Request(Module.KHACHHANG, Action.GET_ALL, null);
                Response responseKH = SocketClient.getInstance().sendRequest(requestKH);

                // Fetch Tables
                Request requestBan = new Request(Module.BANAN, Action.GET_ALL, null);
                Response responseBan = SocketClient.getInstance().sendRequest(requestBan);

                // Fetch Bookings
                Request requestDB = new Request(Module.DATBAN, Action.GET_ALL, null);
                Response responseDB = SocketClient.getInstance().sendRequest(requestDB);

                Platform.runLater(() -> {
                    if (responseKH != null && responseKH.isSuccess() &&
                            responseBan != null && responseBan.isSuccess() &&
                            responseDB != null && responseDB.isSuccess()) {

                        // Parse Customers
                        List<KhachHang> serverKHList = (List<KhachHang>) responseKH.getData();
                        MockDataStore.customers.clear();
                        MockDataStore.customerIds.clear();
                        MockDataStore.customerNames.clear();
                        Map<String, KhachHang> customerMap = new HashMap<>();
                        if (serverKHList != null) {
                            for (KhachHang kh : serverKHList) {
                                MockDataStore.customers.add(new CustomerModel(kh.getMaKH(), kh.getTenKH(), kh.getSdt(),
                                        kh.getDiemTichLuy()));
                                MockDataStore.customerIds.add(kh.getMaKH());
                                MockDataStore.customerNames.add(kh.getTenKH());
                                customerMap.put(kh.getMaKH(), kh);
                            }
                        }

                        // Parse Tables
                        List<BanAn> serverBanList = (List<BanAn>) responseBan.getData();
                        MockDataStore.tables.clear();
                        Map<String, BanAn> tableMap = new HashMap<>();
                        if (serverBanList != null) {
                            for (BanAn ba : serverBanList) {
                                MockDataStore.tables.add(new TableModel(ba.getMaBan(), ba.getTenBan(), ba.getKhuVuc(),
                                        ba.getTrangThai()));
                                tableMap.put(ba.getMaBan(), ba);
                            }
                        }

                        // Parse Bookings
                        List<DatBan> serverDBList = (List<DatBan>) responseDB.getData();
                        BookingModel selected = bookingTable.getSelectionModel().getSelectedItem();
                        String selectedId = selected != null ? selected.getId() : null;

                        masterList.clear();
                        MockDataStore.bookings.clear();
                        if (serverDBList != null) {
                            for (DatBan db : serverDBList) {
                                KhachHang kh = customerMap.get(db.getMaKH());
                                String custName = kh != null ? kh.getTenKH() : "Khách vãng lai";
                                String custPhone = kh != null ? kh.getSdt() : "";

                                BanAn ba = tableMap.get(db.getMaBan());
                                String tableName = ba != null ? ba.getTenBan() : "Mang về";

                                String status = db.getTrangThai() != null ? db.getTrangThai() : "Đang chờ";

                                String arrivalTime = db.getThoiGianDen() != null
                                        ? formatTimestamp(db.getThoiGianDen())
                                        : "Chưa đến";

                                BookingModel model = new BookingModel(
                                        db.getMaDatBan(),
                                        custName,
                                        custPhone,
                                        db.getSoNguoi(),
                                        formatTimestamp(db.getTimeVao()),
                                        formatTimestamp(db.getTimeRa()),
                                        tableName,
                                        status,
                                        arrivalTime,
                                        db.getNote());
                                masterList.add(model);
                                MockDataStore.bookings.add(model);
                            }
                        }

                        applyFilters();

                        if (selectedId != null) {
                            for (BookingModel item : bookingTable.getItems()) {
                                if (item.getId().equals(selectedId)) {
                                    bookingTable.getSelectionModel().select(item);
                                    break;
                                }
                            }
                        }
                    }
                    isReloading = false;
                });
            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() -> isReloading = false);
            }
        }).start();
    }

    private void startAutoReload() {
        if (autoReloadTimeline != null) {
            autoReloadTimeline.stop();
        }
        autoReloadTimeline = new Timeline(
                new KeyFrame(Duration.seconds(10), event -> {
                    if (bookingTable.getScene() == null) {
                        autoReloadTimeline.stop();
                        return;
                    }
                    loadDataSilently();
                }));
        autoReloadTimeline.setCycleCount(Timeline.INDEFINITE);
        autoReloadTimeline.play();

        bookingTable.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene == null && autoReloadTimeline != null) {
                autoReloadTimeline.stop();
            }
        });
    }

    private void setLoadingState(boolean loading) {
        if (viewDetailBtn != null)
            viewDetailBtn.setDisable(loading);
        if (createBtn != null)
            createBtn.setDisable(loading);
        if (editBtn != null)
            editBtn.setDisable(loading);
        if (deleteBtn != null)
            deleteBtn.setDisable(loading);
        if (searchField != null)
            searchField.setDisable(loading);
        if (filterCombo != null)
            filterCombo.setDisable(loading);
        if (fromDate != null)
            fromDate.setDisable(loading);
        if (toDate != null)
            toDate.setDisable(loading);
    }

    private void setupSearch() {
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            applyFilters();
        });
    }

    private void applyFilters() {
        String keyword = searchField.getText() != null ? searchField.getText().toLowerCase().trim() : "";
        String statusFilter = filterCombo.getValue() != null ? filterCombo.getValue() : "Tất cả";
        LocalDate start = fromDate.getValue();
        LocalDate end = toDate.getValue();

        ObservableList<BookingModel> filtered = FXCollections.observableArrayList();
        for (BookingModel bk : masterList) {
            // 1. Search keyword
            if (!keyword.isEmpty()) {
                if (!bk.getId().toLowerCase().contains(keyword) &&
                        !bk.getCustomer().toLowerCase().contains(keyword) &&
                        !bk.getPhone().toLowerCase().contains(keyword) &&
                        !bk.getTable().toLowerCase().contains(keyword) &&
                        !bk.getStatus().toLowerCase().contains(keyword)) {
                    continue;
                }
            }

            // 2. Status filter
            if (!statusFilter.equals("Tất cả")) {
                if (statusFilter.equalsIgnoreCase("Hôm nay")) {
                    if (bk.getTime() != null && bk.getTime().length() >= 10) {
                        try {
                            LocalDate bookingDate = LocalDate.parse(bk.getTime().substring(0, 10));
                            if (!bookingDate.equals(LocalDate.now())) {
                                continue;
                            }
                        } catch (Exception e) {
                            continue;
                        }
                    } else {
                        continue;
                    }
                } else {
                    if (!bk.getStatus().equalsIgnoreCase(statusFilter)) {
                        continue;
                    }
                }
            }

            // 3. Date filter
            if (bk.getTime() != null && bk.getTime().length() >= 10) {
                try {
                    LocalDate bookingDate = LocalDate.parse(bk.getTime().substring(0, 10));
                    if (start != null && bookingDate.isBefore(start)) {
                        continue;
                    }
                    if (end != null && bookingDate.isAfter(end)) {
                        continue;
                    }
                } catch (Exception e) {
                    // Ignore date parse issues
                }
            } else {
                if (start != null || end != null) {
                    continue;
                }
            }

            filtered.add(bk);
        }
        bookingTable.setItems(filtered);
        updateStatsFiltered(filtered);
    }

    private void updateStats() {
        updateStatsFiltered(masterList);
    }

    private void updateStatsFiltered(List<BookingModel> list) {
        int total = list.size();
        int confirmed = 0;
        int pending = 0;
        int cancelled = 0;

        for (BookingModel bk : list) {
            String status = bk.getStatus();
            if (status.equalsIgnoreCase("Đã nhận bàn") || status.equalsIgnoreCase("Đang sử dụng") || status.equalsIgnoreCase("Đã hoàn thành") || status.equalsIgnoreCase("Đã xác nhận") || status.equalsIgnoreCase("Hoàn thành")) {
                confirmed++;
            } else if (status.equalsIgnoreCase("Đang chờ") || status.equalsIgnoreCase("Chờ xác nhận")) {
                pending++;
            } else if (status.equalsIgnoreCase("Đã hủy")) {
                cancelled++;
            }
        }

        totalBookingsLabel.setText(String.valueOf(total));
        confirmedBookingsLabel.setText(String.valueOf(confirmed));
        pendingBookingsLabel.setText(String.valueOf(pending));
        cancelledBookingsLabel.setText(String.valueOf(cancelled));
    }

    private String formatTimestamp(Timestamp ts) {
        if (ts == null)
            return "";
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        return sdf.format(ts);
    }

    /* ================= MODEL ================= */
    public static class BookingModel {
        private final String id;
        private final String customer;
        private final String phone;
        private final int guests;
        private final String time;
        private final String timeRa;
        private final String table;
        private final String status;
        private final String arrivalTime;
        private final String note;

        public BookingModel(String id, String customer, String phone, int guests, String time, String table,
                String status) {
            this(id, customer, phone, guests, time, "", table, status, "Chưa đến", "");
        }

        public BookingModel(String id, String customer, String phone, int guests, String time, String table,
                String status, String note) {
            this(id, customer, phone, guests, time, "", table, status, "Chưa đến", note);
        }

        public BookingModel(String id, String customer, String phone, int guests, String time, String timeRa, String table,
                String status, String arrivalTime, String note) {
            this.id = id;
            this.customer = customer;
            this.phone = phone;
            this.guests = guests;
            this.time = time;
            this.timeRa = timeRa;
            this.table = table;
            this.status = status;
            this.arrivalTime = arrivalTime != null ? arrivalTime : "Chưa đến";
            this.note = note != null ? note : "";
        }

        public String getId() {
            return id;
        }

        public String getCustomer() {
            return customer;
        }

        public String getPhone() {
            return phone;
        }

        public int getGuests() {
            return guests;
        }

        public String getTime() {
            return time;
        }

        public String getTimeRa() {
            return timeRa;
        }

        public String getTable() {
            return table;
        }

        public String getStatus() {
            return status;
        }

        public String getArrivalTime() {
            return arrivalTime;
        }

        public String getNote() {
            return note;
        }
    }

    public static BookingModel selectedBooking = null;

    @FXML
    private void handleAddNew() {
        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/fxml/bookingadd.fxml"));
            javafx.scene.Parent view = loader.load();
            javafx.scene.layout.AnchorPane parent = (javafx.scene.layout.AnchorPane) bookingTable.getScene()
                    .lookup("#contentArea");
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

    @FXML
    private void handleEdit() {
        BookingModel selected = bookingTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Vui lòng chọn một lượt đặt bàn từ danh sách để sửa.");
            return;
        }
        selectedBooking = selected;
        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(
                    getClass().getResource("/fxml/bookingedit.fxml"));
            javafx.scene.Parent view = loader.load();
            javafx.scene.layout.AnchorPane parent = (javafx.scene.layout.AnchorPane) bookingTable.getScene()
                    .lookup("#contentArea");
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

    @FXML
    private void handleDelete() {
        BookingModel selected = bookingTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Vui lòng chọn một lượt đặt bàn từ danh sách để xóa.");
            return;
        }
        selectedBooking = selected;
        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(
                    getClass().getResource("/fxml/bookingdelete.fxml"));
            javafx.scene.Parent view = loader.load();
            javafx.scene.layout.AnchorPane parent = (javafx.scene.layout.AnchorPane) bookingTable.getScene()
                    .lookup("#contentArea");
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

    @FXML
    private void handleViewDetail() {
        BookingModel selected = bookingTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Vui lòng chọn một lượt đặt bàn từ danh sách để xem chi tiết.");
            return;
        }
        selectedBooking = selected;
        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(
                    getClass().getResource("/fxml/bookingdetail.fxml"));
            javafx.scene.Parent view = loader.load();
            javafx.scene.layout.AnchorPane parent = (javafx.scene.layout.AnchorPane) bookingTable.getScene()
                    .lookup("#contentArea");
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
            showAlert("Không thể mở màn hình chi tiết đặt bàn: " + e.getMessage());
        }
    }

    private void showAlert(String message) {
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.WARNING);
        alert.setTitle("Thông báo");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
