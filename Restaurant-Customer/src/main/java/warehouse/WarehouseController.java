package warehouse;

import restaurant.MockDataStore;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.List;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import model.NguyenLieu;
import network.Action;
import network.Module;
import network.Request;
import network.Response;
import network.SocketClient;

public class WarehouseController implements Initializable {

    @FXML
    private TableView<WarehouseModel> warehouseTable;

    @FXML
    private TableColumn<WarehouseModel, String> idColumn;

    @FXML
    private TableColumn<WarehouseModel, String> nameColumn;

    @FXML
    private TableColumn<WarehouseModel, String> unitColumn;

    @FXML
    private TableColumn<WarehouseModel, Double> quantityColumn;

    @FXML
    private TableColumn<WarehouseModel, Double> minQuantityColumn;

    @FXML
    private TableColumn<WarehouseModel, String> statusColumn;

    @FXML
    private TextField searchField;

    @FXML
    private Label totalItemsLabel;

    @FXML
    private Label sufficientLabel;

    @FXML
    private Label warningLabel;

    @FXML
    private Button createBtn;

    @FXML
    private Button editBtn;

    @FXML
    private Button deleteBtn;

    private final ObservableList<WarehouseModel> masterList = FXCollections.observableArrayList();
    private FilteredList<WarehouseModel> filteredList;
    private javafx.animation.Timeline autoReloadTimeline;
    private boolean isReloading = false;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        setupTable();

        filteredList = new FilteredList<>(masterList, p -> true);
        SortedList<WarehouseModel> sortedList = new SortedList<>(filteredList);
        sortedList.comparatorProperty().bind(warehouseTable.comparatorProperty());
        warehouseTable.setItems(sortedList);

        loadData();
        setupSearch();
        startAutoReload();
    }

    private void setupTable() {
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        unitColumn.setCellValueFactory(new PropertyValueFactory<>("unit"));
        quantityColumn.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        minQuantityColumn.setCellValueFactory(new PropertyValueFactory<>("minQuantity"));
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));

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
                    if (item.equalsIgnoreCase("Đủ hàng")) {
                        label.getStyleClass().add("status-sufficient");
                    } else if (item.equalsIgnoreCase("Sắp hết")) {
                        label.getStyleClass().add("status-warning");
                    } else {
                        label.getStyleClass().add("status-critical");
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
                Request request = new Request(Module.NGUYENLIEU, Action.GET_ALL, null);
                Response response = SocketClient.getInstance().sendRequest(request);
                Platform.runLater(() -> {
                    if (response != null && response.isSuccess()) {
                        List<NguyenLieu> serverList = (List<NguyenLieu>) response.getData();

                        WarehouseModel selected = warehouseTable.getSelectionModel().getSelectedItem();
                        String selectedId = selected != null ? selected.getId() : null;

                        masterList.clear();
                        MockDataStore.warehouseItems.clear();

                        if (serverList != null) {
                            for (NguyenLieu nl : serverList) {
                                double qty = nl.getSoLuongKho();
                                double minQty = 10.0;
                                String status = qty >= minQty ? "Đủ hàng" : (qty > 0 ? "Sắp hết" : "Hết hàng");
                                WarehouseModel model = new WarehouseModel(
                                    nl.getMaNL(),
                                    nl.getTenNL(),
                                    nl.getDonViTinh(),
                                    qty,
                                    minQty,
                                    status
                                );
                                masterList.add(model);
                                MockDataStore.warehouseItems.add(model);
                            }
                        }

                        filterData(searchField.getText());

                        if (selectedId != null) {
                            for (WarehouseModel item : warehouseTable.getItems()) {
                                if (item.getId().equals(selectedId)) {
                                    warehouseTable.getSelectionModel().select(item);
                                    break;
                                }
                            }
                        }
                    } else {
                        showAlert(response != null ? response.getMessage() : "Mất kết nối Server. Không thể tải dữ liệu kho!");
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
                Request request = new Request(Module.NGUYENLIEU, Action.GET_ALL, null);
                Response response = SocketClient.getInstance().sendRequest(request);
                Platform.runLater(() -> {
                    if (response != null && response.isSuccess()) {
                        List<NguyenLieu> serverList = (List<NguyenLieu>) response.getData();

                        WarehouseModel selected = warehouseTable.getSelectionModel().getSelectedItem();
                        String selectedId = selected != null ? selected.getId() : null;

                        masterList.clear();
                        MockDataStore.warehouseItems.clear();

                        if (serverList != null) {
                            for (NguyenLieu nl : serverList) {
                                double qty = nl.getSoLuongKho();
                                double minQty = 10.0;
                                String status = qty >= minQty ? "Đủ hàng" : (qty > 0 ? "Sắp hết" : "Hết hàng");
                                WarehouseModel model = new WarehouseModel(
                                    nl.getMaNL(),
                                    nl.getTenNL(),
                                    nl.getDonViTinh(),
                                    qty,
                                    minQty,
                                    status
                                );
                                masterList.add(model);
                                MockDataStore.warehouseItems.add(model);
                            }
                        }

                        filterData(searchField.getText());

                        if (selectedId != null) {
                            for (WarehouseModel item : warehouseTable.getItems()) {
                                if (item.getId().equals(selectedId)) {
                                    warehouseTable.getSelectionModel().select(item);
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
                if (warehouseTable.getScene() == null) {
                    autoReloadTimeline.stop();
                    return;
                }
                loadDataSilently();
            })
        );
        autoReloadTimeline.setCycleCount(javafx.animation.Timeline.INDEFINITE);
        autoReloadTimeline.play();

        warehouseTable.sceneProperty().addListener((obs, oldScene, newScene) -> {
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
    }

    private void setupSearch() {
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            filterData(newValue);
        });
    }

    private void filterData(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            filteredList.setPredicate(item -> true);
        } else {
            String lowerKeyword = keyword.toLowerCase().trim();
            filteredList.setPredicate(item -> {
                return item.getId().toLowerCase().contains(lowerKeyword) ||
                       item.getName().toLowerCase().contains(lowerKeyword) ||
                       item.getUnit().toLowerCase().contains(lowerKeyword) ||
                       item.getStatus().toLowerCase().contains(lowerKeyword);
            });
        }
        updateStats();
    }

    private void updateStats() {
        int total = masterList.size();
        int sufficient = 0;
        int warning = 0;

        for (WarehouseModel item : masterList) {
            if (item.getStatus().equalsIgnoreCase("Đủ hàng")) {
                sufficient++;
            } else {
                warning++;
            }
        }

        totalItemsLabel.setText(String.valueOf(total));
        sufficientLabel.setText(String.valueOf(sufficient));
        warningLabel.setText(String.valueOf(warning));
    }

    /* ================= MODEL ================= */
    public static class WarehouseModel {
        private final String id;
        private final String name;
        private final String unit;
        private final double quantity;
        private final double minQuantity;
        private final String status;

        public WarehouseModel(String id, String name, String unit, double quantity, double minQuantity, String status) {
            this.id = id;
            this.name = name;
            this.unit = unit;
            this.quantity = quantity;
            this.minQuantity = minQuantity;
            this.status = status;
        }

        public String getId() { return id; }
        public String getName() { return name; }
        public String getUnit() { return unit; }
        public double getQuantity() { return quantity; }
        public double getMinQuantity() { return minQuantity; }
        public String getStatus() { return status; }
    }

    public static WarehouseModel selectedItem = null;

    @FXML
    private void handleAddNew() {
        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/fxml/warehouseadd.fxml"));
            javafx.scene.Parent view = loader.load();
            javafx.scene.layout.AnchorPane parent = (javafx.scene.layout.AnchorPane) warehouseTable.getScene().lookup("#contentArea");
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
        WarehouseModel selected = warehouseTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Vui lòng chọn một nguyên liệu từ danh sách để sửa.");
            return;
        }
        selectedItem = selected;
        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/fxml/warehouseedit.fxml"));
            javafx.scene.Parent view = loader.load();
            javafx.scene.layout.AnchorPane parent = (javafx.scene.layout.AnchorPane) warehouseTable.getScene().lookup("#contentArea");
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
        WarehouseModel selected = warehouseTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Vui lòng chọn một nguyên liệu từ danh sách để xóa.");
            return;
        }
        selectedItem = selected;
        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/fxml/warehousedelete.fxml"));
            javafx.scene.Parent view = loader.load();
            javafx.scene.layout.AnchorPane parent = (javafx.scene.layout.AnchorPane) warehouseTable.getScene().lookup("#contentArea");
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
