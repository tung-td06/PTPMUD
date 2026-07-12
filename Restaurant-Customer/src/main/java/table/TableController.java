package table;

import java.net.URL;
import java.util.ResourceBundle;
import java.util.List;
import java.util.ArrayList;
import java.util.Comparator;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.control.DatePicker;
import java.time.LocalDate;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.animation.Timeline;
import javafx.animation.KeyFrame;
import javafx.util.Duration;

import model.BanAn;
import model.DatBan;
import model.HoaDon;
import model.Order;
import model.KhachHang;
import model.NhanVien;
import network.Action;
import network.Module;
import network.Request;
import network.Response;
import network.SocketClient;

public class TableController implements Initializable {

    @FXML
    private FlowPane tableFlowPane;

    @FXML
    private ScrollPane tableScrollPane;

    @FXML
    private TextField searchField;

    @FXML
    private DatePicker dpFilterDate;

    @FXML
    private ComboBox<String> filterCombo;

    @FXML
    private Label totalLabel;

    @FXML
    private Label activeLabel;

    @FXML
    private Label reservedLabel;

    @FXML
    private Label emptyLabel;

    @FXML
    private Button createBtn;

    @FXML
    private Button editBtn;

    @FXML
    private Button deleteBtn;

    @FXML
    private Button detailBtn;

    private ObservableList<TableModel> masterList = FXCollections.observableArrayList();
    private String currentFilterStatus = "Tất cả";
    private Timeline autoReloadTimeline;
    private boolean isReloading = false;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        setupScrollPane();
        setupHeaderFilters();
        setupSearch();
        loadData();
        startAutoReload();
    }

    private void setupScrollPane() {
        tableScrollPane.setFitToWidth(true);
    }

    private void loadData() {
        setLoadingState(true);
        new Thread(() -> {
            try {
                Request request = new Request(Module.BANAN, Action.GET_ALL, null);
                Response response = SocketClient.getInstance().sendRequest(request);

                Request requestDB = new Request(Module.DATBAN, Action.GET_ALL, null);
                Response responseDB = SocketClient.getInstance().sendRequest(requestDB);

                Platform.runLater(() -> {
                    if (response != null && response.isSuccess()) {
                        List<BanAn> serverList = (List<BanAn>) response.getData();
                        List<DatBan> dbList = null;
                        if (responseDB != null && responseDB.isSuccess()) {
                            dbList = (List<DatBan>) responseDB.getData();
                        }

                        LocalDate selectedDate = dpFilterDate.getValue();
                        if (selectedDate == null) {
                            selectedDate = LocalDate.now();
                        }

                        masterList.clear();
                        if (serverList != null) {
                            for (BanAn ba : serverList) {
                                String computedStatus = ba.getTrangThai();

                                // Look for matching active reservation for this table on selectedDate
                                DatBan matchedBooking = null;
                                List<DatBan> activeBookingsOnDate = new ArrayList<>();
                                if (dbList != null) {
                                    for (DatBan db : dbList) {
                                        if (db.getMaBan() != null && db.getMaBan().equalsIgnoreCase(ba.getMaBan())) {
                                            if (db.getTimeVao() != null) {
                                                LocalDate bookingDate = db.getTimeVao().toLocalDateTime().toLocalDate();
                                                if (bookingDate.equals(selectedDate)
                                                        && !"Đã hủy".equalsIgnoreCase(db.getTrangThai())) {
                                                    boolean hasEnded = false;
                                                    if (db.getTimeRa() != null
                                                            && System.currentTimeMillis() > db.getTimeRa().getTime()) {
                                                        hasEnded = true;
                                                    }
                                                    if (!hasEnded) {
                                                        activeBookingsOnDate.add(db);
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                                activeBookingsOnDate.sort(Comparator.comparing(DatBan::getTimeVao,
                                        Comparator.nullsLast(Comparator.naturalOrder())));
                                if (!activeBookingsOnDate.isEmpty()) {
                                    matchedBooking = activeBookingsOnDate.get(0);
                                }

                                if (matchedBooking != null) {
                                    if ("Đã nhận bàn".equalsIgnoreCase(matchedBooking.getTrangThai())) {
                                        computedStatus = "Đang dùng";
                                    } else if ("Đang chờ".equalsIgnoreCase(matchedBooking.getTrangThai())) {
                                        computedStatus = "Đặt trước";
                                    }
                                } else {
                                    // If selectedDate is not today, any table with no reservation is considered
                                    // "Trống"
                                    if (!selectedDate.equals(LocalDate.now())) {
                                        computedStatus = "Trống";
                                    }
                                }

                                masterList.add(new TableModel(
                                        ba.getMaBan(),
                                        ba.getTenBan(),
                                        ba.getKhuVuc(),
                                        computedStatus));
                            }
                        }
                        renderTables();
                        updateCards();
                    } else {
                        showAlert(response != null ? response.getMessage()
                                : "Mất kết nối Server. Không thể tải dữ liệu bàn ăn!");
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
        if (isReloading)
            return;
        isReloading = true;
        new Thread(() -> {
            try {
                Request request = new Request(Module.BANAN, Action.GET_ALL, null);
                Response response = SocketClient.getInstance().sendRequest(request);

                Request requestDB = new Request(Module.DATBAN, Action.GET_ALL, null);
                Response responseDB = SocketClient.getInstance().sendRequest(requestDB);

                Platform.runLater(() -> {
                    if (response != null && response.isSuccess()) {
                        List<BanAn> serverList = (List<BanAn>) response.getData();
                        List<DatBan> dbList = null;
                        if (responseDB != null && responseDB.isSuccess()) {
                            dbList = (List<DatBan>) responseDB.getData();
                        }

                        LocalDate selectedDate = dpFilterDate.getValue();
                        if (selectedDate == null) {
                            selectedDate = LocalDate.now();
                        }

                        masterList.clear();
                        if (serverList != null) {
                            for (BanAn ba : serverList) {
                                String computedStatus = ba.getTrangThai();

                                // Look for matching active reservation for this table on selectedDate
                                DatBan matchedBooking = null;
                                List<DatBan> activeBookingsOnDate = new ArrayList<>();
                                if (dbList != null) {
                                    for (DatBan db : dbList) {
                                        if (db.getMaBan() != null && db.getMaBan().equalsIgnoreCase(ba.getMaBan())) {
                                            if (db.getTimeVao() != null) {
                                                LocalDate bookingDate = db.getTimeVao().toLocalDateTime().toLocalDate();
                                                if (bookingDate.equals(selectedDate)
                                                        && !"Đã hủy".equalsIgnoreCase(db.getTrangThai())) {
                                                    boolean hasEnded = false;
                                                    if (db.getTimeRa() != null
                                                            && System.currentTimeMillis() > db.getTimeRa().getTime()) {
                                                        hasEnded = true;
                                                    }
                                                    if (!hasEnded) {
                                                        activeBookingsOnDate.add(db);
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                                activeBookingsOnDate.sort(Comparator.comparing(DatBan::getTimeVao,
                                        Comparator.nullsLast(Comparator.naturalOrder())));
                                if (!activeBookingsOnDate.isEmpty()) {
                                    matchedBooking = activeBookingsOnDate.get(0);
                                }

                                if (matchedBooking != null) {
                                    if ("Đã nhận bàn".equalsIgnoreCase(matchedBooking.getTrangThai())) {
                                        computedStatus = "Đang dùng";
                                    } else if ("Đang chờ".equalsIgnoreCase(matchedBooking.getTrangThai())) {
                                        computedStatus = "Đặt trước";
                                    }
                                } else {
                                    // If selectedDate is not today, any table with no reservation is considered
                                    // "Trống"
                                    if (!selectedDate.equals(LocalDate.now())) {
                                        computedStatus = "Trống";
                                    }
                                }

                                masterList.add(new TableModel(
                                        ba.getMaBan(),
                                        ba.getTenBan(),
                                        ba.getKhuVuc(),
                                        computedStatus));
                            }
                        }
                        renderTables();
                        updateCards();
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
                    if (tableScrollPane.getScene() == null) {
                        autoReloadTimeline.stop();
                        return;
                    }
                    loadDataSilently();
                }));
        autoReloadTimeline.setCycleCount(Timeline.INDEFINITE);
        autoReloadTimeline.play();

        tableScrollPane.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene == null && autoReloadTimeline != null) {
                autoReloadTimeline.stop();
            }
        });
    }

    private void setLoadingState(boolean loading) {
        if (createBtn != null)
            createBtn.setDisable(loading);
        if (editBtn != null)
            editBtn.setDisable(loading);
        if (deleteBtn != null)
            deleteBtn.setDisable(loading);
        if (detailBtn != null)
            detailBtn.setDisable(loading);
        if (searchField != null)
            searchField.setDisable(loading);
        if (filterCombo != null)
            filterCombo.setDisable(loading);
        if (dpFilterDate != null)
            dpFilterDate.setDisable(loading);
    }

    private void updateTableStatusInDB(String maban, String status) {
        setLoadingState(true);
        new Thread(() -> {
            try {
                // Construct a List to match Dispatcher's value index logic
                List<String> payload = List.of(maban, status);
                Request request = new Request(Module.BANAN, "UPDATE_TRANG_THAI", payload);
                Response response = SocketClient.getInstance().sendRequest(request);
                Platform.runLater(() -> {
                    if (response == null || !response.isSuccess()) {
                        showAlert(response != null ? response.getMessage() : "Lỗi cập nhật trạng thái lên Server!");
                    }
                    loadData();
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

    private void setupHeaderFilters() {
        dpFilterDate.setValue(LocalDate.now());

        // Disable past dates in the calendar picker
        dpFilterDate.setDayCellFactory(picker -> new javafx.scene.control.DateCell() {
            @Override
            public void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                if (date != null && date.isBefore(LocalDate.now())) {
                    setDisable(true);
                    setStyle("-fx-background-color: #f1f5f9; -fx-text-fill: #94a3b8;");
                }
            }
        });

        dpFilterDate.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null && newVal.isBefore(LocalDate.now())) {
                Platform.runLater(() -> dpFilterDate.setValue(LocalDate.now()));
                return;
            }
            loadData();
        });

        filterCombo.setItems(FXCollections.observableArrayList("Tất cả", "Trống", "Đang dùng", "Đặt trước"));
        filterCombo.setValue("Tất cả");

        filterCombo.setOnAction(e -> {
            if (filterCombo.getValue() != null) {
                applyFilter(filterCombo.getValue());
            }
        });
    }

    private void applyFilter(String status) {
        currentFilterStatus = status;
        renderTables();
    }

    private void setupSearch() {
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            renderTables();
        });
    }

    private void renderTables() {
        tableFlowPane.getChildren().clear();
        String searchKeyword = searchField.getText() != null ? searchField.getText().toLowerCase().trim() : "";

        // Client-side sorting alphabetically by maban
        List<TableModel> sortedList = new ArrayList<>(masterList);
        sortedList.sort(Comparator.comparing(TableModel::getMaban, Comparator.nullsLast(String::compareTo)));

        VBox toSelectCard = null;
        TableModel toSelectTable = null;

        for (TableModel table : sortedList) {
            if (!currentFilterStatus.equals("Tất cả") && !table.getTrangthai().equalsIgnoreCase(currentFilterStatus)) {
                continue;
            }

            if (!searchKeyword.isEmpty() &&
                    !table.getTenban().toLowerCase().contains(searchKeyword) &&
                    !table.getKhuvuc().toLowerCase().contains(searchKeyword) &&
                    !table.getMaban().toLowerCase().contains(searchKeyword)) {
                continue;
            }

            VBox card = createTableCard(table);
            tableFlowPane.getChildren().add(card);

            // Match selection
            if (selectedTable != null && table.getMaban().equals(selectedTable.getMaban())) {
                toSelectCard = card;
                toSelectTable = table;
            }
        }

        if (toSelectCard != null) {
            selectedCardNode = toSelectCard;
            selectedTable = toSelectTable;
            selectedCardNode.setStyle(
                    "-fx-border-color: #3b82f6; -fx-border-width: 2.5px; -fx-border-radius: 14; -fx-background-radius: 14;");
        } else {
            selectedCardNode = null;
            selectedTable = null;
        }
    }

    private VBox createTableCard(TableModel table) {
        VBox card = new VBox(12);
        card.setPrefSize(180, 160);
        card.getStyleClass().add("table-card");

        Label statusTag = new Label(table.getTrangthai());
        statusTag.getStyleClass().add("table-card-status");

        if (table.getTrangthai().equalsIgnoreCase("Trống")) {
            card.getStyleClass().add("card-empty");
            statusTag.getStyleClass().add("tag-empty");
        } else if (table.getTrangthai().equalsIgnoreCase("Đang dùng")
                || table.getTrangthai().equalsIgnoreCase("Có khách")) {
            card.getStyleClass().add("card-occupied");
            statusTag.getStyleClass().add("tag-occupied");
        } else {
            card.getStyleClass().add("card-reserved");
            statusTag.getStyleClass().add("tag-reserved");
        }

        Label nameLabel = new Label(table.getTenban());
        nameLabel.getStyleClass().add("table-card-name");

        Label locationLabel = new Label("📍 " + table.getKhuvuc());
        locationLabel.getStyleClass().add("table-card-capacity");

        Button actionBtn = new Button();
        actionBtn.getStyleClass().add("table-card-action");
        if (table.getTrangthai().equalsIgnoreCase("Trống")) {
            actionBtn.setText("Mở bàn");
            actionBtn.setOnAction(e -> {
                table.setTrangthai("Đang dùng");
                updateTableStatusInDB(table.getMaban(), "Đang dùng");
                renderTables();
                updateCards();
            });
        } else if (table.getTrangthai().equalsIgnoreCase("Đang dùng")
                || table.getTrangthai().equalsIgnoreCase("Có khách")) {
            actionBtn.setText("Thanh toán");
            actionBtn.setOnAction(e -> {
                table.setTrangthai("Trống");
                updateTableStatusInDB(table.getMaban(), "Trống");
                renderTables();
                updateCards();
            });
        } else {
            actionBtn.setText("Nhận bàn");
            actionBtn.setOnAction(e -> {
                table.setTrangthai("Đang dùng");
                updateTableStatusInDB(table.getMaban(), "Đang dùng");
                renderTables();
                updateCards();
            });
        }

        card.getChildren().addAll(statusTag, nameLabel, locationLabel, actionBtn);
        card.setAlignment(Pos.TOP_CENTER);
        card.setPadding(new Insets(12));

        card.setOnMouseClicked(event -> {
            if (selectedCardNode != null) {
                selectedCardNode.setStyle("");
            }
            selectedTable = table;
            selectedCardNode = card;
            card.setStyle(
                    "-fx-border-color: #3b82f6; -fx-border-width: 2.5px; -fx-border-radius: 14; -fx-background-radius: 14;");
        });

        return card;
    }

    private void updateCards() {
        int total = masterList.size();
        int occupied = 0;
        int reserved = 0;
        int empty = 0;

        for (TableModel table : masterList) {
            if (table.getTrangthai().equalsIgnoreCase("Trống")) {
                empty++;
            } else if (table.getTrangthai().equalsIgnoreCase("Đang dùng")
                    || table.getTrangthai().equalsIgnoreCase("Có khách")) {
                occupied++;
            } else if (table.getTrangthai().equalsIgnoreCase("Đặt trước")
                    || table.getTrangthai().equalsIgnoreCase("Đã đặt")) {
                reserved++;
            }
        }

        totalLabel.setText(String.valueOf(total));
        activeLabel.setText(String.valueOf(occupied));
        reservedLabel.setText(String.valueOf(reserved));
        emptyLabel.setText(String.valueOf(empty));
    }

    @FXML
    private void handleRefresh() {
        loadData();
    }

    public static class TableModel {
        private final String maban;
        private final String tenban;
        private final String khuvuc;
        private String trangthai;

        public TableModel(String maban, String tenban, String khuvuc, String trangthai) {
            this.maban = maban;
            this.tenban = tenban;
            this.khuvuc = khuvuc;
            this.trangthai = trangthai;
        }

        public String getMaban() {
            return maban;
        }

        public String getTenban() {
            return tenban;
        }

        public String getKhuvuc() {
            return khuvuc;
        }

        public String getTrangthai() {
            return trangthai;
        }

        public void setTrangthai(String trangthai) {
            this.trangthai = trangthai;
        }
    }

    public static TableModel selectedTable = null;
    private VBox selectedCardNode = null;

    @FXML
    private void handleAddNew() {
        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/fxml/tableadd.fxml"));
            javafx.scene.Parent view = loader.load();
            javafx.scene.layout.AnchorPane parent = (javafx.scene.layout.AnchorPane) tableScrollPane.getScene()
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
        if (selectedTable == null) {
            showAlert("Vui lòng chọn một bàn.");
            return;
        }
        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/fxml/tableedit.fxml"));
            javafx.scene.Parent view = loader.load();
            javafx.scene.layout.AnchorPane parent = (javafx.scene.layout.AnchorPane) tableScrollPane.getScene()
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
        if (selectedTable == null) {
            showAlert("Vui lòng chọn một bàn.");
            return;
        }
        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(
                    getClass().getResource("/fxml/tabledelete.fxml"));
            javafx.scene.Parent view = loader.load();
            javafx.scene.layout.AnchorPane parent = (javafx.scene.layout.AnchorPane) tableScrollPane.getScene()
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
    private void handleDetail() {
        if (selectedTable == null) {
            showAlert("Vui lòng chọn một bàn.");
            return;
        }
        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(
                    getClass().getResource("/fxml/tabledetail.fxml"));
            javafx.scene.Parent view = loader.load();
            javafx.scene.layout.AnchorPane parent = (javafx.scene.layout.AnchorPane) tableScrollPane.getScene()
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
            showAlert("Không thể mở chi tiết bàn ăn: " + e.getMessage());
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
