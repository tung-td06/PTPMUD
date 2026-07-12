package supplier;

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
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.animation.Timeline;
import javafx.animation.KeyFrame;
import javafx.util.Duration;

import model.NhaCungCap;
import network.Action;
import network.Module;
import network.Request;
import network.Response;
import network.SocketClient;

public class SupplierController implements Initializable {

    @FXML
    private TableView<SupplierModel> supplierTable;

    @FXML
    private TableColumn<SupplierModel, String> manccColumn;

    @FXML
    private TableColumn<SupplierModel, String> tennccColumn;

    @FXML
    private TableColumn<SupplierModel, String> sdtColumn;

    @FXML
    private TextField searchField;

    @FXML
    private Label totalLabel;

    @FXML
    private Button createBtn;

    @FXML
    private Button editBtn;

    @FXML
    private Button deleteBtn;

    private final ObservableList<SupplierModel> masterList = FXCollections.observableArrayList();
    private FilteredList<SupplierModel> filteredList;
    private Timeline autoReloadTimeline;
    private boolean isReloading = false;
    private boolean isLoading = false;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        setupTable();

        filteredList = new FilteredList<>(masterList, p -> true);
        SortedList<SupplierModel> sortedList = new SortedList<>(filteredList);
        sortedList.comparatorProperty().bind(supplierTable.comparatorProperty());
        supplierTable.setItems(sortedList);

        loadData();
        setupSearch();
        startAutoReload();
    }

    private void setupTable() {
        manccColumn.setCellValueFactory(new PropertyValueFactory<>("mancc"));
        tennccColumn.setCellValueFactory(new PropertyValueFactory<>("tenncc"));
        sdtColumn.setCellValueFactory(new PropertyValueFactory<>("sdt"));
    }

    private void loadData() {
        if (isLoading) return;
        isLoading = true;
        setLoadingState(true);
        new Thread(() -> {
            try {
                Request request = new Request(Module.NHACUNGCAP, Action.GET_ALL, null);
                Response response = SocketClient.getInstance().sendRequest(request);
                Platform.runLater(() -> {
                    if (response != null && response.isSuccess()) {
                        List<NhaCungCap> serverList = (List<NhaCungCap>) response.getData();

                        SupplierModel selected = supplierTable.getSelectionModel().getSelectedItem();
                        String selectedId = selected != null ? selected.getMancc() : null;

                        masterList.clear();
                        MockDataStore.suppliers.clear();

                        if (serverList != null) {
                            for (NhaCungCap ncc : serverList) {
                                SupplierModel model = new SupplierModel(
                                    ncc.getMaNCC(),
                                    ncc.getTenNCC(),
                                    ncc.getSdt()
                                );
                                masterList.add(model);
                                MockDataStore.suppliers.add(model);
                            }
                        }

                        filterData(searchField.getText());

                        if (selectedId != null) {
                            for (SupplierModel item : supplierTable.getItems()) {
                                if (item.getMancc().equals(selectedId)) {
                                    supplierTable.getSelectionModel().select(item);
                                    break;
                                }
                            }
                        }
                    } else {
                        showAlert(response != null ? response.getMessage() : "Mất kết nối Server. Không thể tải danh sách nhà cung cấp!");
                    }
                    isLoading = false;
                    setLoadingState(false);
                });
            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() -> {
                    showAlert("Lỗi kết nối hoặc xử lý dữ liệu: " + e.getMessage());
                    isLoading = false;
                    setLoadingState(false);
                });
            }
        }).start();
    }

    private void loadDataSilently() {
        if (isReloading || isLoading) return;
        isReloading = true;
        new Thread(() -> {
            try {
                Request request = new Request(Module.NHACUNGCAP, Action.GET_ALL, null);
                Response response = SocketClient.getInstance().sendRequest(request);
                Platform.runLater(() -> {
                    if (response != null && response.isSuccess()) {
                        List<NhaCungCap> serverList = (List<NhaCungCap>) response.getData();

                        SupplierModel selected = supplierTable.getSelectionModel().getSelectedItem();
                        String selectedId = selected != null ? selected.getMancc() : null;

                        masterList.clear();
                        MockDataStore.suppliers.clear();

                        if (serverList != null) {
                            for (NhaCungCap ncc : serverList) {
                                SupplierModel model = new SupplierModel(
                                    ncc.getMaNCC(),
                                    ncc.getTenNCC(),
                                    ncc.getSdt()
                                );
                                masterList.add(model);
                                MockDataStore.suppliers.add(model);
                            }
                        }

                        filterData(searchField.getText());

                        if (selectedId != null) {
                            for (SupplierModel item : supplierTable.getItems()) {
                                if (item.getMancc().equals(selectedId)) {
                                    supplierTable.getSelectionModel().select(item);
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
        autoReloadTimeline = new Timeline(
            new KeyFrame(Duration.seconds(10), event -> {
                if (supplierTable.getScene() == null) {
                    autoReloadTimeline.stop();
                    return;
                }
                loadDataSilently();
            })
        );
        autoReloadTimeline.setCycleCount(Timeline.INDEFINITE);
        autoReloadTimeline.play();

        supplierTable.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene == null && autoReloadTimeline != null) {
                autoReloadTimeline.stop();
            }
        });
        supplierTable.parentProperty().addListener((obs, oldParent, newParent) -> {
            if (newParent == null && autoReloadTimeline != null) {
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
                return item.getMancc().toLowerCase().contains(lowerKeyword) ||
                       item.getTenncc().toLowerCase().contains(lowerKeyword) ||
                       item.getSdt().contains(lowerKeyword);
            });
        }
        updateCards();
    }

    private void updateCards() {
        totalLabel.setText(String.valueOf(masterList.size()));
    }

    public static SupplierModel selectedSupplier = null;

    @FXML
    private void handleAddNew() {
        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/fxml/supplieradd.fxml"));
            javafx.scene.Parent view = loader.load();
            javafx.scene.layout.AnchorPane parent = (javafx.scene.layout.AnchorPane) supplierTable.getScene().lookup("#contentArea");
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
        SupplierModel selected = supplierTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Vui lòng chọn một nhà cung cấp từ danh sách để sửa.");
            return;
        }
        selectedSupplier = selected;
        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/fxml/supplieredit.fxml"));
            javafx.scene.Parent view = loader.load();
            javafx.scene.layout.AnchorPane parent = (javafx.scene.layout.AnchorPane) supplierTable.getScene().lookup("#contentArea");
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
        SupplierModel selected = supplierTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Vui lòng chọn một nhà cung cấp từ danh sách để xóa.");
            return;
        }
        selectedSupplier = selected;
        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/fxml/supplierdelete.fxml"));
            javafx.scene.Parent view = loader.load();
            javafx.scene.layout.AnchorPane parent = (javafx.scene.layout.AnchorPane) supplierTable.getScene().lookup("#contentArea");
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
    public static class SupplierModel {
        private final String mancc;
        private final String tenncc;
        private final String sdt;

        public SupplierModel(String mancc, String tenncc, String sdt) {
            this.mancc = mancc;
            this.tenncc = tenncc;
            this.sdt = sdt;
        }

        public String getMancc() { return mancc; }
        public String getTenncc() { return tenncc; }
        public String getSdt() { return sdt; }
    }
}
