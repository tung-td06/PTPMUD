package shift;

import java.net.URL;
import java.util.ResourceBundle;
import java.util.List;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import model.CaLamViec;
import network.Action;
import network.Module;
import network.Request;
import network.Response;
import network.SocketClient;

public class ShiftController implements Initializable {

    @FXML
    private TableView<ShiftModel> shiftTable;

    @FXML
    private TableColumn<ShiftModel, String> macaColumn;

    @FXML
    private TableColumn<ShiftModel, String> manvColumn;

    @FXML
    private TableColumn<ShiftModel, String> tencaColumn;

    @FXML
    private TableColumn<ShiftModel, String> giobatdauColumn;

    @FXML
    private TableColumn<ShiftModel, String> gioketthucColumn;

    @FXML
    private TextField searchField;

    @FXML
    private Label totalLabel;

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

    private ObservableList<ShiftModel> masterList = FXCollections.observableArrayList();
    private javafx.animation.Timeline autoReloadTimeline;
    private boolean isReloading = false;
    private boolean isRevertingDate = false;
    private boolean isUpdatingFilters = false;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        setupTable();
        setupFilters();
        setupSearch();
        loadData();
        startAutoReload();
    }

    private void setupFilters() {
        filterCombo.setItems(FXCollections.observableArrayList(
                "Tất cả", "Ca sáng", "Ca chiều", "Ca tối"));
        filterCombo.getSelectionModel().select("Tất cả");
        filterCombo.setDisable(false);
        fromDate.setDisable(false);
        fromDate.setEditable(false);
        toDate.setDisable(false);
        toDate.setEditable(false);

        filterCombo.setOnAction(e -> {
            if (isUpdatingFilters) return;
            String newValue = filterCombo.getValue();
            if ("Tất cả".equals(newValue)) {
                isUpdatingFilters = true;
                fromDate.setValue(null);
                toDate.setValue(null);
                isUpdatingFilters = false;
            }
            applyFiltersAndSearch();
        });

        filterCombo.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (isUpdatingFilters) return;
            if ("Tất cả".equals(newValue)) {
                isUpdatingFilters = true;
                fromDate.setValue(null);
                toDate.setValue(null);
                isUpdatingFilters = false;
            }
            applyFiltersAndSearch();
        });

        fromDate.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (isRevertingDate || isUpdatingFilters) return;
            if (newValue != null && toDate.getValue() != null && newValue.isAfter(toDate.getValue())) {
                isRevertingDate = true;
                showAlert("Từ ngày không thể lớn hơn Đến ngày!");
                fromDate.setValue(oldValue);
                isRevertingDate = false;
            } else {
                isUpdatingFilters = true;
                if (newValue != null) {
                    if ("Tất cả".equals(filterCombo.getValue())) {
                        filterCombo.getSelectionModel().clearSelection();
                    }
                } else {
                    if (toDate.getValue() == null && filterCombo.getValue() == null) {
                        filterCombo.getSelectionModel().select("Tất cả");
                    }
                }
                isUpdatingFilters = false;
                applyFiltersAndSearch();
            }
        });

        toDate.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (isRevertingDate || isUpdatingFilters) return;
            if (newValue != null && fromDate.getValue() != null && fromDate.getValue().isAfter(newValue)) {
                isRevertingDate = true;
                showAlert("Đến ngày không thể nhỏ hơn Từ ngày!");
                toDate.setValue(oldValue);
                isRevertingDate = false;
            } else {
                isUpdatingFilters = true;
                if (newValue != null) {
                    if ("Tất cả".equals(filterCombo.getValue())) {
                        filterCombo.getSelectionModel().clearSelection();
                    }
                } else {
                    if (fromDate.getValue() == null && filterCombo.getValue() == null) {
                        filterCombo.getSelectionModel().select("Tất cả");
                    }
                }
                isUpdatingFilters = false;
                applyFiltersAndSearch();
            }
        });
    }

    private void setupTable() {
        macaColumn.setCellValueFactory(new PropertyValueFactory<>("maca"));
        manvColumn.setCellValueFactory(new PropertyValueFactory<>("manv"));
        tencaColumn.setCellValueFactory(new PropertyValueFactory<>("tenca"));
        giobatdauColumn.setCellValueFactory(new PropertyValueFactory<>("giobatdau"));
        gioketthucColumn.setCellValueFactory(new PropertyValueFactory<>("gioketthuc"));
    }

    private void loadData() {
        setLoadingState(true);
        new Thread(() -> {
            try {
                Request request = new Request(Module.CALAMVIEC, Action.GET_ALL, null);
                Response response = SocketClient.getInstance().sendRequest(request);
                Platform.runLater(() -> {
                    if (response != null && response.isSuccess()) {
                        List<CaLamViec> serverList = (List<CaLamViec>) response.getData();

                        ShiftModel selected = shiftTable.getSelectionModel().getSelectedItem();
                        String selectedMaCa = selected != null ? selected.getMaca() : null;

                        masterList.clear();
                        if (serverList != null) {
                            for (CaLamViec ca : serverList) {
                                masterList.add(new ShiftModel(
                                    ca.getMaCa(),
                                    ca.getMaNV(),
                                    ca.getTenCa(),
                                    formatTimestamp(ca.getGioBatDau()),
                                    formatTimestamp(ca.getGioKetThuc())
                                ));
                            }
                        }

                        applyFiltersAndSearch();

                        if (selectedMaCa != null) {
                            for (ShiftModel item : shiftTable.getItems()) {
                                if (item.getMaca().equals(selectedMaCa)) {
                                    shiftTable.getSelectionModel().select(item);
                                    break;
                                }
                            }
                        }
                    } else {
                        showAlert(response != null ? response.getMessage() : "Mất kết nối Server. Không thể tải dữ liệu ca làm việc!");
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
                Request request = new Request(Module.CALAMVIEC, Action.GET_ALL, null);
                Response response = SocketClient.getInstance().sendRequest(request);
                Platform.runLater(() -> {
                    if (response != null && response.isSuccess()) {
                        List<CaLamViec> serverList = (List<CaLamViec>) response.getData();

                        ShiftModel selected = shiftTable.getSelectionModel().getSelectedItem();
                        String selectedMaCa = selected != null ? selected.getMaca() : null;

                        masterList.clear();
                        if (serverList != null) {
                            for (CaLamViec ca : serverList) {
                                masterList.add(new ShiftModel(
                                    ca.getMaCa(),
                                    ca.getMaNV(),
                                    ca.getTenCa(),
                                    formatTimestamp(ca.getGioBatDau()),
                                    formatTimestamp(ca.getGioKetThuc())
                                ));
                            }
                        }

                        applyFiltersAndSearch();

                        if (selectedMaCa != null) {
                            for (ShiftModel item : shiftTable.getItems()) {
                                if (item.getMaca().equals(selectedMaCa)) {
                                    shiftTable.getSelectionModel().select(item);
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
                if (shiftTable.getScene() == null) {
                    autoReloadTimeline.stop();
                    return;
                }
                loadDataSilently();
            })
        );
        autoReloadTimeline.setCycleCount(javafx.animation.Timeline.INDEFINITE);
        autoReloadTimeline.play();

        shiftTable.sceneProperty().addListener((obs, oldScene, newScene) -> {
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
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            applyFiltersAndSearch();
        });
    }

    private void applyFiltersAndSearch() {
        String keyword = searchField.getText() != null ? searchField.getText().trim().toLowerCase() : "";
        String filterType = filterCombo.getValue() != null ? filterCombo.getValue() : "Tất cả";
        LocalDate from = fromDate.getValue();
        LocalDate to = toDate.getValue();

        java.time.LocalDateTime filterStart = null;
        java.time.LocalDateTime filterEnd = null;
        if (from != null) {
            filterStart = from.atStartOfDay();
        }
        if (to != null) {
            filterEnd = to.atTime(23, 59, 59);
        }

        ObservableList<ShiftModel> filtered = FXCollections.observableArrayList();
        for (ShiftModel shift : masterList) {
            if (!keyword.isEmpty()) {
                boolean matchesKeyword = (shift.getMaca() != null && shift.getMaca().toLowerCase().contains(keyword)) ||
                                         (shift.getManv() != null && shift.getManv().toLowerCase().contains(keyword)) ||
                                         (shift.getTenca() != null && shift.getTenca().toLowerCase().contains(keyword)) ||
                                         (shift.getGiobatdau() != null && shift.getGiobatdau().toLowerCase().contains(keyword)) ||
                                         (shift.getGioketthuc() != null && shift.getGioketthuc().toLowerCase().contains(keyword));
                if (!matchesKeyword) {
                    continue;
                }
            }

            if (!filterType.equals("Tất cả")) {
                int startHour = -1;
                int endHour = -1;
                try {
                    if (shift.getGiobatdau() != null && shift.getGiobatdau().length() >= 13) {
                        startHour = Integer.parseInt(shift.getGiobatdau().substring(11, 13));
                    }
                } catch (Exception e) {
                    // Ignore
                }
                try {
                    if (shift.getGioketthuc() != null && shift.getGioketthuc().length() >= 13) {
                        endHour = Integer.parseInt(shift.getGioketthuc().substring(11, 13));
                    }
                } catch (Exception e) {
                    // Ignore
                }

                boolean matchesFilter = false;
                if (startHour != -1 && endHour != -1) {
                    boolean belongsToMorning = false;
                    boolean belongsToAfternoon = false;
                    boolean belongsToEvening = false;

                    for (int h = 0; h < 24; h++) {
                        boolean isActive = false;
                        if (startHour <= endHour) {
                            isActive = (h >= startHour && h < endHour);
                        } else {
                            isActive = (h >= startHour || h < endHour);
                        }

                        if (isActive) {
                            if (h >= 6 && h < 12) {
                                belongsToMorning = true;
                            } else if (h >= 12 && h < 17) {
                                belongsToAfternoon = true;
                            } else if (h >= 17 || h < 6) {
                                belongsToEvening = true;
                            }
                        }
                    }

                    if (filterType.equals("Ca sáng") && belongsToMorning) {
                        matchesFilter = true;
                    } else if (filterType.equals("Ca chiều") && belongsToAfternoon) {
                        matchesFilter = true;
                    } else if (filterType.equals("Ca tối") && belongsToEvening) {
                        matchesFilter = true;
                    }
                } else {
                    // Fallback to name check if hours are not parseable
                    String tenCaLower = shift.getTenca() != null ? shift.getTenca().toLowerCase() : "";
                    if (filterType.equals("Ca sáng") && (tenCaLower.contains("sáng") || tenCaLower.contains("sang") || 
                        tenCaLower.contains("hành chính") || tenCaLower.contains("hanh chinh") || 
                        tenCaLower.contains("gãy") || tenCaLower.contains("gay"))) {
                        matchesFilter = true;
                    } else if (filterType.equals("Ca chiều") && (tenCaLower.contains("chiều") || tenCaLower.contains("chieu"))) {
                        matchesFilter = true;
                    } else if (filterType.equals("Ca tối") && (tenCaLower.contains("tối") || tenCaLower.contains("toi") || 
                               tenCaLower.contains("đêm") || tenCaLower.contains("dem"))) {
                        matchesFilter = true;
                    }
                }

                if (!matchesFilter) {
                    continue;
                }
            }

            java.time.LocalDateTime shiftStart = null;
            java.time.LocalDateTime shiftEnd = null;
            java.time.format.DateTimeFormatter formatter = java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            try {
                if (shift.getGiobatdau() != null) {
                    shiftStart = java.time.LocalDateTime.parse(shift.getGiobatdau().trim(), formatter);
                }
            } catch (Exception e) {
                // Ignore
            }
            try {
                if (shift.getGioketthuc() != null) {
                    shiftEnd = java.time.LocalDateTime.parse(shift.getGioketthuc().trim(), formatter);
                }
            } catch (Exception e) {
                // Ignore
            }

            if (filterStart != null || filterEnd != null) {
                if (shiftStart == null || shiftEnd == null) {
                    continue;
                }
                if (filterStart != null && filterEnd != null) {
                    if (shiftStart.isAfter(filterEnd) || shiftEnd.isBefore(filterStart)) {
                        continue;
                    }
                } else if (filterStart != null) {
                    if (shiftEnd.isBefore(filterStart)) {
                        continue;
                    }
                } else { // filterEnd != null
                    if (shiftStart.isAfter(filterEnd)) {
                        continue;
                    }
                }
            }

            filtered.add(shift);
        }

        shiftTable.setItems(filtered);
        updateCards(filtered.size());
    }

    private void updateCards(int count) {
        totalLabel.setText(String.valueOf(count));
    }

    private String formatTimestamp(Timestamp ts) {
        if (ts == null) return "";
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(ts);
    }

    public static ShiftModel selectedShift = null;

    @FXML
    private void handleAddNew() {
        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/fxml/shiftadd.fxml"));
            javafx.scene.Parent view = loader.load();
            javafx.scene.layout.AnchorPane parent = (javafx.scene.layout.AnchorPane) shiftTable.getScene().lookup("#contentArea");
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
        ShiftModel selected = shiftTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Vui lòng chọn một ca làm việc từ danh sách để sửa.");
            return;
        }
        selectedShift = selected;
        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/fxml/shiftedit.fxml"));
            javafx.scene.Parent view = loader.load();
            javafx.scene.layout.AnchorPane parent = (javafx.scene.layout.AnchorPane) shiftTable.getScene().lookup("#contentArea");
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
        ShiftModel selected = shiftTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Vui lòng chọn một ca làm việc từ danh sách để xóa.");
            return;
        }
        selectedShift = selected;
        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/fxml/shiftdelete.fxml"));
            javafx.scene.Parent view = loader.load();
            javafx.scene.layout.AnchorPane parent = (javafx.scene.layout.AnchorPane) shiftTable.getScene().lookup("#contentArea");
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

    /* ================= MODEL ================= */
    public static class ShiftModel {
        private final String maca;
        private final String manv;
        private final String tenca;
        private final String giobatdau;
        private final String gioketthuc;

        public ShiftModel(String maca, String manv, String tenca, String giobatdau, String gioketthuc) {
            this.maca = maca;
            this.manv = manv;
            this.tenca = tenca;
            this.giobatdau = giobatdau;
            this.gioketthuc = gioketthuc;
        }

        public String getMaca() { return maca; }
        public String getManv() { return manv; }
        public String getTenca() { return tenca; }
        public String getGiobatdau() { return giobatdau; }
        public String getGioketthuc() { return gioketthuc; }
    }
}
