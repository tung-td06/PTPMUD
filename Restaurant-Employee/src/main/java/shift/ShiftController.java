package shift;

import java.net.URL;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.List;
import java.util.ResourceBundle;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;

import model.CaLamViec;
import network.Action;
import network.Module;
import network.Request;
import network.Response;
import network.SocketClient;

public class ShiftController implements Initializable {

    public static ShiftModel selectedShift;

    @FXML
    private TableView<ShiftDisplayModel> shiftTable;

    @FXML
    private TableColumn<ShiftDisplayModel, String> macaColumn;

    @FXML
    private TableColumn<ShiftDisplayModel, String> tencaColumn;

    @FXML
    private TableColumn<ShiftDisplayModel, String> ngaylamColumn;

    @FXML
    private TableColumn<ShiftDisplayModel, String> giobatdauColumn;

    @FXML
    private TableColumn<ShiftDisplayModel, String> gioketthucColumn;

    @FXML
    private TableColumn<ShiftDisplayModel, String> tongthogianColumn;

    @FXML
    private TableColumn<ShiftDisplayModel, String> trangthaiColumn;

    @FXML
    private ComboBox<String> statusCombo;

    @FXML
    private DatePicker datePicker;

    @FXML
    private TextField searchField;

    @FXML
    private Label todayLabel;

    @FXML
    private Label weekLabel;

    @FXML
    private Label weekHoursLabel;

    @FXML
    private Label nextShiftLabel;

    private final ObservableList<ShiftDisplayModel> masterList = FXCollections.observableArrayList();
    private FilteredList<ShiftDisplayModel> filteredList;
    private javafx.animation.Timeline autoReloadTimeline;
    private boolean isReloading = false;

    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
    private static final SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm");

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        setupTable();
        setupFilters();

        filteredList = new FilteredList<>(masterList, p -> true);
        SortedList<ShiftDisplayModel> sortedList = new SortedList<>(filteredList);
        sortedList.comparatorProperty().bind(shiftTable.comparatorProperty());
        shiftTable.setItems(sortedList);

        loadData();
        startAutoReload();
    }

    private void setupTable() {
        macaColumn.setCellValueFactory(new PropertyValueFactory<>("maca"));
        tencaColumn.setCellValueFactory(new PropertyValueFactory<>("tenca"));
        ngaylamColumn.setCellValueFactory(new PropertyValueFactory<>("ngaylam"));
        giobatdauColumn.setCellValueFactory(new PropertyValueFactory<>("giobatdau"));
        gioketthucColumn.setCellValueFactory(new PropertyValueFactory<>("gioketthuc"));
        tongthogianColumn.setCellValueFactory(new PropertyValueFactory<>("tongthoigian"));
        trangthaiColumn.setCellValueFactory(new PropertyValueFactory<>("trangthai"));

        trangthaiColumn.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                    setText(null);
                } else {
                    Label label = new Label(item);
                    label.getStyleClass().add("status-tag");

                    if (item.equalsIgnoreCase("Đang diễn ra")) {
                        label.getStyleClass().add("status-processing");
                    } else if (item.equalsIgnoreCase("Đã hoàn thành")) {
                        label.getStyleClass().add("status-paid");
                    } else if (item.equalsIgnoreCase("Chưa bắt đầu")) {
                        label.getStyleClass().add("status-pending");
                    }

                    HBox container = new HBox(label);
                    container.setAlignment(Pos.CENTER);
                    setGraphic(container);
                }
            }
        });

        shiftTable.setPlaceholder(new Label("Không có dữ liệu ca làm việc"));
    }

    private void setupFilters() {
        statusCombo.setItems(FXCollections.observableArrayList(
                "Tất cả", "Chưa bắt đầu", "Đang diễn ra", "Đã hoàn thành"));
        statusCombo.getSelectionModel().select("Tất cả");

        statusCombo.valueProperty().addListener((obs, oldVal, newVal) -> applyFilters());
        datePicker.valueProperty().addListener((obs, oldVal, newVal) -> applyFilters());
        searchField.textProperty().addListener((obs, oldVal, newVal) -> applyFilters());
    }

    private void loadData() {
        setLoadingState(true);
        new Thread(() -> {
            try {
                Request request = new Request(Module.CALAMVIEC, Action.GET_ALL, null);
                Response response = SocketClient.getInstance().sendRequest(request);
                if (response == null || !response.isSuccess()) {
                    loadMockDataFallback();
                    return;
                }
                Platform.runLater(() -> {
                    List<CaLamViec> serverList = (List<CaLamViec>) response.getData();
                    updateMasterList(serverList);
                    setLoadingState(false);
                });
            } catch (Exception e) {
                e.printStackTrace();
                loadMockDataFallback();
            }
        }).start();
    }

    private void loadMockDataFallback() {
        System.out.println("No server connection found. Loading mock shifts...");
        long now = System.currentTimeMillis();
        
        masterList.clear();

        // Generate some realistic shift dates relative to today
        LocalDate today = LocalDate.now();

        // 1. Ca sáng hôm qua (Đã hoàn thành)
        Timestamp startYesterday = Timestamp.valueOf(today.minusDays(1).toString() + " 06:00:00");
        Timestamp endYesterday = Timestamp.valueOf(today.minusDays(1).toString() + " 12:00:00");
        addMockShift("CA001", "Ca sáng", startYesterday, endYesterday, now);

        // 2. Ca chiều hôm qua (Đã hoàn thành)
        Timestamp startYesterdayPm = Timestamp.valueOf(today.minusDays(1).toString() + " 12:00:00");
        Timestamp endYesterdayPm = Timestamp.valueOf(today.minusDays(1).toString() + " 18:00:00");
        addMockShift("CA002", "Ca chiều", startYesterdayPm, endYesterdayPm, now);

        // 3. Ca sáng hôm nay (Đã hoàn thành hoặc Đang diễn ra tùy thuộc vào giờ hiện tại)
        Timestamp startToday = Timestamp.valueOf(today.toString() + " 06:00:00");
        Timestamp endToday = Timestamp.valueOf(today.toString() + " 12:00:00");
        addMockShift("CA003", "Ca sáng", startToday, endToday, now);

        // 4. Ca chiều hôm nay (Đang diễn ra hoặc Chưa bắt đầu)
        Timestamp startTodayPm = Timestamp.valueOf(today.toString() + " 12:00:00");
        Timestamp endTodayPm = Timestamp.valueOf(today.toString() + " 18:00:00");
        addMockShift("CA004", "Ca chiều", startTodayPm, endTodayPm, now);

        // 5. Ca tối ngày mai (Chưa bắt đầu)
        Timestamp startTomorrow = Timestamp.valueOf(today.plusDays(1).toString() + " 18:00:00");
        Timestamp endTomorrow = Timestamp.valueOf(today.plusDays(1).toString() + " 23:30:00");
        addMockShift("CA005", "Ca tối", startTomorrow, endTomorrow, now);

        Platform.runLater(() -> {
            applyFilters();
            updateCards(now);
            setLoadingState(false);
        });
    }

    private void addMockShift(String maca, String tenca, Timestamp startTs, Timestamp endTs, long now) {
        String ngaylam = dateFormat.format(startTs);
        String giobatdau = timeFormat.format(startTs);
        String gioketthuc = timeFormat.format(endTs);

        double diffHours = (endTs.getTime() - startTs.getTime()) / (1000.0 * 60.0 * 60.0);
        String tongthoigian = (diffHours == (int) diffHours) 
                ? String.format("%d giờ", (int) diffHours) 
                : String.format("%.1f giờ", diffHours);

        String trangthai;
        if (now < startTs.getTime()) {
            trangthai = "Chưa bắt đầu";
        } else if (now > endTs.getTime()) {
            trangthai = "Đã hoàn thành";
        } else {
            trangthai = "Đang diễn ra";
        }

        LocalDate localDate = startTs.toLocalDateTime().toLocalDate();
        masterList.add(new ShiftDisplayModel(maca, tenca, ngaylam, giobatdau, gioketthuc, tongthoigian, trangthai, localDate, startTs, endTs));
    }

    private void loadDataSilently() {
        if (isReloading) return;
        isReloading = true;
        new Thread(() -> {
            try {
                Request request = new Request(Module.CALAMVIEC, Action.GET_ALL, null);
                Response response = SocketClient.getInstance().sendRequest(request);
                Platform.runLater(() -> {
                    if (response != null && response.isSuccess()) {
                        List<CaLamViec> serverList = (List<CaLamViec>) response.getData();
                        updateMasterList(serverList);
                    }
                    isReloading = false;
                });
            } catch (Exception e) {
                isReloading = false;
            }
        }).start();
    }

    private void updateMasterList(List<CaLamViec> serverList) {
        if (serverList == null) return;

        String loggedInMaNV = Login.LoginController.loggedInAccount != null 
                ? Login.LoginController.loggedInAccount.getMaNV() : null;

        masterList.clear();
        long now = System.currentTimeMillis();

        for (CaLamViec ca : serverList) {
            // Filter by logged in user
            if (loggedInMaNV != null && !ca.getMaNV().equalsIgnoreCase(loggedInMaNV)) {
                continue;
            }

            Timestamp startTs = ca.getGioBatDau();
            Timestamp endTs = ca.getGioKetThuc();

            if (startTs == null || endTs == null) continue;

            String maca = ca.getMaCa();
            String tenca = ca.getTenCa();
            String ngaylam = dateFormat.format(startTs);
            String giobatdau = timeFormat.format(startTs);
            String gioketthuc = timeFormat.format(endTs);

            double diffHours = (endTs.getTime() - startTs.getTime()) / (1000.0 * 60.0 * 60.0);
            String tongthoigian = (diffHours == (int) diffHours) 
                    ? String.format("%d giờ", (int) diffHours) 
                    : String.format("%.1f giờ", diffHours);

            String trangthai;
            if (now < startTs.getTime()) {
                trangthai = "Chưa bắt đầu";
            } else if (now > endTs.getTime()) {
                trangthai = "Đã hoàn thành";
            } else {
                trangthai = "Đang diễn ra";
            }

            LocalDate localDate = startTs.toLocalDateTime().toLocalDate();

            masterList.add(new ShiftDisplayModel(maca, tenca, ngaylam, giobatdau, gioketthuc, tongthoigian, trangthai, localDate, startTs, endTs));
        }

        applyFilters();
        updateCards(now);
    }

    private void applyFilters() {
        String statusFilter = statusCombo.getValue();
        LocalDate selectedDate = datePicker.getValue();
        String keyword = searchField.getText() == null ? "" : searchField.getText().trim().toLowerCase();

        filteredList.setPredicate(shift -> {
            // Keyword filter
            if (!keyword.isEmpty()) {
                boolean matches = shift.getMaca().toLowerCase().contains(keyword) ||
                        shift.getTenca().toLowerCase().contains(keyword);
                if (!matches) return false;
            }

            // Status filter
            if (statusFilter != null && !statusFilter.equals("Tất cả")) {
                if (!shift.getTrangthai().equalsIgnoreCase(statusFilter)) {
                    return false;
                }
            }

            // Date filter
            if (selectedDate != null) {
                if (!shift.getLocalDate().equals(selectedDate)) {
                    return false;
                }
            }

            return true;
        });
    }

    private void updateCards(long now) {
        LocalDate today = LocalDate.now();
        LocalDate startOfWeek = today.minusDays(today.getDayOfWeek().getValue() - 1);
        LocalDate endOfWeek = startOfWeek.plusDays(6);

        int todayCount = 0;
        int weekCount = 0;
        double totalHoursThisWeek = 0.0;
        ShiftDisplayModel nextShift = null;

        for (ShiftDisplayModel shift : masterList) {
            LocalDate date = shift.getLocalDate();
            
            // Today shifts
            if (date.equals(today)) {
                todayCount++;
            }

            // Week shifts
            if (!date.isBefore(startOfWeek) && !date.isAfter(endOfWeek)) {
                weekCount++;
                double hours = (shift.getEndTs().getTime() - shift.getStartTs().getTime()) / (1000.0 * 60.0 * 60.0);
                totalHoursThisWeek += hours;
            }

            // Next upcoming shift
            if (shift.getStartTs().getTime() > now) {
                if (nextShift == null || shift.getStartTs().getTime() < nextShift.getStartTs().getTime()) {
                    nextShift = shift;
                }
            }
        }

        todayLabel.setText(String.valueOf(todayCount));
        weekLabel.setText(String.valueOf(weekCount));

        if (totalHoursThisWeek == (int) totalHoursThisWeek) {
            weekHoursLabel.setText(String.format("%d giờ", (int) totalHoursThisWeek));
        } else {
            weekHoursLabel.setText(String.format("%.1f giờ", totalHoursThisWeek));
        }

        if (nextShift != null) {
            String dateStr = new SimpleDateFormat("dd/MM").format(nextShift.getStartTs());
            nextShiftLabel.setText(nextShift.getTenca() + " (" + dateStr + ")");
        } else {
            nextShiftLabel.setText("Không có");
        }
    }

    private void setLoadingState(boolean loading) {
        searchField.setDisable(loading);
        statusCombo.setDisable(loading);
        datePicker.setDisable(loading);
        if (loading) {
            shiftTable.setPlaceholder(new Label("Đang tải danh sách ca làm việc..."));
        } else {
            shiftTable.setPlaceholder(new Label("Không có dữ liệu ca làm việc"));
        }
    }

    private void startAutoReload() {
        if (autoReloadTimeline != null) {
            autoReloadTimeline.stop();
        }
        autoReloadTimeline = new javafx.animation.Timeline(
                new javafx.animation.KeyFrame(javafx.util.Duration.seconds(10), event -> {
                    if (shiftTable.getScene() == null) {
                        autoReloadTimeline.stop();
                        return;
                    }
                    loadDataSilently();
                }));
        autoReloadTimeline.setCycleCount(javafx.animation.Timeline.INDEFINITE);
        autoReloadTimeline.play();

        shiftTable.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene == null && autoReloadTimeline != null) {
                autoReloadTimeline.stop();
            }
        });
    }

    @FXML
    private void handleRefresh() {
        searchField.clear();
        statusCombo.getSelectionModel().select("Tất cả");
        datePicker.setValue(null);
        loadData();
    }

    /* ================= DISPLAY MODEL ================= */
    public static class ShiftDisplayModel {
        private final String maca;
        private final String tenca;
        private final String ngaylam;
        private final String giobatdau;
        private final String gioketthuc;
        private final String tongthoigian;
        private final String trangthai;
        private final LocalDate localDate;
        private final Timestamp startTs;
        private final Timestamp endTs;

        public ShiftDisplayModel(String maca, String tenca, String ngaylam, String giobatdau, String gioketthuc, 
                                 String tongthoigian, String trangthai, LocalDate localDate, Timestamp startTs, Timestamp endTs) {
            this.maca = maca;
            this.tenca = tenca;
            this.ngaylam = ngaylam;
            this.giobatdau = giobatdau;
            this.gioketthuc = gioketthuc;
            this.tongthoigian = tongthoigian;
            this.trangthai = trangthai;
            this.localDate = localDate;
            this.startTs = startTs;
            this.endTs = endTs;
        }

        public String getMaca() { return maca; }
        public String getTenca() { return tenca; }
        public String getNgaylam() { return ngaylam; }
        public String getGiobatdau() { return giobatdau; }
        public String getGioketthuc() { return gioketthuc; }
        public String getTongthoigian() { return tongthoigian; }
        public String getTrangthai() { return trangthai; }
        public LocalDate getLocalDate() { return localDate; }
        public Timestamp getStartTs() { return startTs; }
        public Timestamp getEndTs() { return endTs; }
    }

    /* ================= BACKWARD COMPATIBILITY SHIFT MODEL ================= */
    public static class ShiftModel {
        private String maca;
        private String manv;
        private String tenca;
        private String giobatdau;
        private String gioketthuc;

        public ShiftModel(String maca, String manv, String tenca, String giobatdau, String gioketthuc) {
            this.maca = maca;
            this.manv = manv;
            this.tenca = tenca;
            this.giobatdau = giobatdau;
            this.gioketthuc = gioketthuc;
        }

        public String getMaca() { return maca; }
        public void setMaca(String maca) { this.maca = maca; }

        public String getManv() { return manv; }
        public void setManv(String manv) { this.manv = manv; }

        public String getTenca() { return tenca; }
        public void setTenca(String tenca) { this.tenca = tenca; }

        public String getGiobatdau() { return giobatdau; }
        public void setGiobatdau(String giobatdau) { this.giobatdau = giobatdau; }

        public String getGioketthuc() { return gioketthuc; }
        public void setGioketthuc(String gioketthuc) { this.gioketthuc = gioketthuc; }
    }
}
