package billdetail;

import bill.BillController;
import restaurant.MockDataStore;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import model.ChiTietHD;
import model.MonAn;
import network.Action;
import network.Module;
import network.Request;
import network.Response;
import network.SocketClient;

public class BillDetailController implements Initializable {

    @FXML
    private TableView<BillDetailModel> billDetailTable;
    @FXML
    private TableColumn<BillDetailModel, String> mamonColumn;
    @FXML
    private TableColumn<BillDetailModel, String> tenmonColumn;
    @FXML
    private TableColumn<BillDetailModel, Integer> soluongColumn;
    @FXML
    private TableColumn<BillDetailModel, Double> dongiaColumn;
    @FXML
    private TableColumn<BillDetailModel, Double> thanhtienColumn;

    @FXML
    private TextField searchField;
    @FXML
    private Label totalLabel;
    @FXML
    private Label totalAmountLabel;
    @FXML
    private Label billIdLabel;
    @FXML
    private Label billSubtitleLabel;
    @FXML
    private Label tableSubtitleLabel;

    @FXML
    private Button backBtn;

    // MaHD hiện tại đang xem (lấy từ BillController.selectedBill)
    private String currentMaHD = null;

    private final ObservableList<BillDetailModel> masterList = FXCollections.observableArrayList();
    private FilteredList<BillDetailModel> filteredList;

    private final Map<String, String> foodNameMap = new HashMap<>();

    private javafx.animation.Timeline autoReloadTimeline;
    private boolean isReloading = false;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Lấy mã hóa đơn từ hóa đơn đã được chọn ở màn hình trước
        if (BillController.selectedBill != null) {
            currentMaHD = BillController.selectedBill.getId();
        }

        setupTable();

        filteredList = new FilteredList<>(masterList, p -> true);
        SortedList<BillDetailModel> sortedList = new SortedList<>(filteredList);
        sortedList.comparatorProperty().bind(billDetailTable.comparatorProperty());
        billDetailTable.setItems(sortedList);

        // Cập nhật tiêu đề màn hình theo hóa đơn đang xem
        updateHeaderLabels();

        loadData();
        setupSearch();
        startAutoReload();
    }

    private void updateHeaderLabels() {
        if (currentMaHD != null) {
            billIdLabel.setText(currentMaHD);
            billSubtitleLabel.setText("Các món ăn trong hóa đơn " + currentMaHD);
            tableSubtitleLabel.setText("Chi tiết đơn hàng của hóa đơn " + currentMaHD);
        } else {
            billIdLabel.setText("Chưa chọn");
            billSubtitleLabel.setText("Vui lòng quay lại và chọn một hóa đơn");
            tableSubtitleLabel.setText("Không có dữ liệu hóa đơn");
        }
    }

    private void setupTable() {
        mamonColumn.setCellValueFactory(new PropertyValueFactory<>("mamon"));
        tenmonColumn.setCellValueFactory(new PropertyValueFactory<>("tenmon"));
        soluongColumn.setCellValueFactory(new PropertyValueFactory<>("soluong"));
        dongiaColumn.setCellValueFactory(new PropertyValueFactory<>("dongia"));
        thanhtienColumn.setCellValueFactory(new PropertyValueFactory<>("thanhtien"));

        dongiaColumn.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : String.format("%,.0f VNĐ", item));
            }
        });
        thanhtienColumn.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : String.format("%,.0f VNĐ", item));
            }
        });

        billDetailTable.setPlaceholder(new Label("Không có món ăn nào trong hóa đơn này"));
    }

    private void setLoadingState(boolean loading) {
        if (backBtn != null)
            backBtn.setDisable(loading);
        if (searchField != null)
            searchField.setDisable(loading);
        if (loading) {
            billDetailTable.setPlaceholder(new Label("Đang tải dữ liệu từ Server..."));
        } else {
            if (currentMaHD == null) {
                billDetailTable.setPlaceholder(new Label("Không có hóa đơn nào được chọn. Vui lòng quay lại."));
            } else {
                billDetailTable.setPlaceholder(new Label("Hóa đơn " + currentMaHD + " không có món ăn nào."));
            }
        }
    }

    private void loadData() {
        if (currentMaHD == null) {
            billDetailTable.setPlaceholder(new Label("Không có hóa đơn nào được chọn. Vui lòng quay lại."));
            return;
        }

        setLoadingState(true);
        new Thread(() -> {
            try {
                // 1. Fetch MonAn (Dishes) để lấy tên món
                Request foodReq = new Request(Module.MONAN, Action.GET_ALL, null);
                Response foodRes = SocketClient.getInstance().sendRequest(foodReq);
                if (foodRes != null && foodRes.isSuccess()) {
                    List<MonAn> foods = (List<MonAn>) foodRes.getData();
                    foodNameMap.clear();
                    if (foods != null) {
                        for (MonAn ma : foods) {
                            foodNameMap.put(ma.getMaMon(), ma.getTenMon());
                        }
                    }
                }

                // 2. Fetch ChiTietHD (Invoice Details) - lọc theo mã hóa đơn hiện tại
                Request detailReq = new Request(Module.CHITIETHD, Action.GET_ALL, null);
                Response detailRes = SocketClient.getInstance().sendRequest(detailReq);

                Platform.runLater(() -> {
                    if (detailRes != null && detailRes.isSuccess()) {
                        List<ChiTietHD> details = (List<ChiTietHD>) detailRes.getData();

                        masterList.clear();
                        MockDataStore.billDetails.clear();

                        if (details != null) {
                            for (ChiTietHD ct : details) {
                                // CHỈ thêm các dòng thuộc hóa đơn hiện tại
                                if (!ct.getMaHD().equals(currentMaHD)) {
                                    continue;
                                }

                                String tenMon = foodNameMap.getOrDefault(ct.getMaMon(), ct.getMaMon());
                                double dg = ct.getDonGia() != null ? ct.getDonGia().doubleValue() : 0.0;
                                double tt = ct.getThanhTien() != null ? ct.getThanhTien().doubleValue()
                                        : (dg * ct.getSoLuong());

                                BillDetailModel model = new BillDetailModel(
                                        ct.getMaHD(),
                                        ct.getMaMon(),
                                        tenMon,
                                        ct.getSoLuong(),
                                        dg,
                                        tt);
                                masterList.add(model);
                                MockDataStore.billDetails.add(model);
                            }
                        }

                        applySearch(searchField.getText());
                        updateCards();
                    } else {
                        showAlert(detailRes != null ? detailRes.getMessage()
                                : "Mất kết nối Server. Không thể tải chi tiết hóa đơn!");
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
        if (isReloading || currentMaHD == null)
            return;
        isReloading = true;
        new Thread(() -> {
            try {
                // Fetch MonAn
                Request foodReq = new Request(Module.MONAN, Action.GET_ALL, null);
                Response foodRes = SocketClient.getInstance().sendRequest(foodReq);
                if (foodRes != null && foodRes.isSuccess()) {
                    List<MonAn> foods = (List<MonAn>) foodRes.getData();
                    foodNameMap.clear();
                    if (foods != null) {
                        for (MonAn ma : foods) {
                            foodNameMap.put(ma.getMaMon(), ma.getTenMon());
                        }
                    }
                }

                // Fetch ChiTietHD
                Request detailReq = new Request(Module.CHITIETHD, Action.GET_ALL, null);
                Response detailRes = SocketClient.getInstance().sendRequest(detailReq);

                Platform.runLater(() -> {
                    if (detailRes != null && detailRes.isSuccess()) {
                        List<ChiTietHD> details = (List<ChiTietHD>) detailRes.getData();

                        masterList.clear();
                        MockDataStore.billDetails.clear();

                        if (details != null) {
                            for (ChiTietHD ct : details) {
                                if (!ct.getMaHD().equals(currentMaHD))
                                    continue;

                                String tenMon = foodNameMap.getOrDefault(ct.getMaMon(), ct.getMaMon());
                                double dg = ct.getDonGia() != null ? ct.getDonGia().doubleValue() : 0.0;
                                double tt = ct.getThanhTien() != null ? ct.getThanhTien().doubleValue()
                                        : (dg * ct.getSoLuong());

                                BillDetailModel model = new BillDetailModel(
                                        ct.getMaHD(),
                                        ct.getMaMon(),
                                        tenMon,
                                        ct.getSoLuong(),
                                        dg,
                                        tt);
                                masterList.add(model);
                                MockDataStore.billDetails.add(model);
                            }
                        }

                        applySearch(searchField.getText());
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
        autoReloadTimeline = new javafx.animation.Timeline(
                new javafx.animation.KeyFrame(javafx.util.Duration.seconds(10), event -> {
                    if (billDetailTable.getScene() == null) {
                        autoReloadTimeline.stop();
                        return;
                    }
                    loadDataSilently();
                }));
        autoReloadTimeline.setCycleCount(javafx.animation.Timeline.INDEFINITE);
        autoReloadTimeline.play();

        billDetailTable.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene == null && autoReloadTimeline != null) {
                autoReloadTimeline.stop();
            }
        });
    }

    private void setupSearch() {
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            applySearch(newValue);
        });
    }

    private void applySearch(String keyword) {
        String lowerKeyword = keyword == null ? "" : keyword.toLowerCase().trim();

        filteredList.setPredicate(bd -> {
            if (lowerKeyword.isEmpty())
                return true;

            return bd.getMamon().toLowerCase().contains(lowerKeyword) ||
                    (bd.getTenmon() != null && bd.getTenmon().toLowerCase().contains(lowerKeyword));
        });

        updateCards();
    }

    private void updateCards() {
        double total = 0;
        for (BillDetailModel bd : filteredList) {
            if (bd.getThanhtien() != null)
                total += bd.getThanhtien();
        }
        totalLabel.setText(String.valueOf(filteredList.size()));
        totalAmountLabel.setText(String.format("%,.0f VNĐ", total));
    }

    /* ================= MODEL ================= */
    public static class BillDetailModel {
        private final String maHD;
        private final String mamon;
        private final String tenmon;
        private final Integer soluong;
        private final Double dongia;
        private final Double thanhtien;

        public BillDetailModel(String maHD, String mamon, String tenmon, Integer soluong, Double dongia,
                Double thanhtien) {
            this.maHD = maHD;
            this.mamon = mamon;
            this.tenmon = tenmon;
            this.soluong = soluong;
            this.dongia = dongia;
            this.thanhtien = thanhtien;
        }

        public String getMaHD()     { return maHD; }
        public String getMamon()    { return mamon; }
        public String getTenmon()   { return tenmon; }
        public Integer getSoluong() { return soluong; }
        public Double getDongia()   { return dongia; }
        public Double getThanhtien(){ return thanhtien; }
    }

    public static BillDetailModel selectedBillDetail = null;

    @FXML
    private void handleBack() {
        navigate("/fxml/bill.fxml");
    }

    private void navigate(String fxmlPath) {
        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource(fxmlPath));
            javafx.scene.Parent view = loader.load();
            javafx.scene.layout.AnchorPane parent = (javafx.scene.layout.AnchorPane) billDetailTable.getScene()
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

    private void showAlert(String message) {
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.WARNING);
        alert.setTitle("Thông báo");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
