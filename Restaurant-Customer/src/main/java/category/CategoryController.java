package category;

import java.net.URL;
import java.util.ResourceBundle;
import java.util.List;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import model.LoaiMon;
import network.Action;
import network.Module;
import network.Request;
import network.Response;
import network.SocketClient;

public class CategoryController implements Initializable {

    @FXML
    private TableView<CategoryModel> categoryTable;

    @FXML
    private TableColumn<CategoryModel, String> maloaiColumn;

    @FXML
    private TableColumn<CategoryModel, String> tenloaiColumn;

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

    private ObservableList<CategoryModel> masterList = FXCollections.observableArrayList();
    private javafx.animation.Timeline autoReloadTimeline;
    private boolean isReloading = false;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        setupTable();
        loadData();
        setupSearch();
        startAutoReload();
    }

    private void setupTable() {
        maloaiColumn.setCellValueFactory(new PropertyValueFactory<>("maloai"));
        tenloaiColumn.setCellValueFactory(new PropertyValueFactory<>("tenloai"));
    }

    private void loadData() {
        setLoadingState(true);
        new Thread(() -> {
            try {
                Request request = new Request(Module.LOAIMON, Action.GET_ALL, null);
                Response response = SocketClient.getInstance().sendRequest(request);
                Platform.runLater(() -> {
                    if (response != null && response.isSuccess()) {
                        List<LoaiMon> serverList = (List<LoaiMon>) response.getData();

                        CategoryModel selected = categoryTable.getSelectionModel().getSelectedItem();
                        String selectedMaLoai = selected != null ? selected.getMaloai() : null;

                        masterList.clear();
                        if (serverList != null) {
                            for (LoaiMon lm : serverList) {
                                masterList.add(new CategoryModel(lm.getMaLoai(), lm.getTenLoai()));
                            }
                        }

                        filterData(searchField.getText());

                        if (selectedMaLoai != null) {
                            for (CategoryModel item : categoryTable.getItems()) {
                                if (item.getMaloai().equals(selectedMaLoai)) {
                                    categoryTable.getSelectionModel().select(item);
                                    break;
                                }
                            }
                        }
                    } else {
                        showAlert(response != null ? response.getMessage() : "Mất kết nối Server. Không thể tải danh sách loại món!");
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
                Request request = new Request(Module.LOAIMON, Action.GET_ALL, null);
                Response response = SocketClient.getInstance().sendRequest(request);
                Platform.runLater(() -> {
                    if (response != null && response.isSuccess()) {
                        List<LoaiMon> serverList = (List<LoaiMon>) response.getData();

                        CategoryModel selected = categoryTable.getSelectionModel().getSelectedItem();
                        String selectedMaLoai = selected != null ? selected.getMaloai() : null;

                        masterList.clear();
                        if (serverList != null) {
                            for (LoaiMon lm : serverList) {
                                masterList.add(new CategoryModel(lm.getMaLoai(), lm.getTenLoai()));
                            }
                        }

                        filterData(searchField.getText());

                        if (selectedMaLoai != null) {
                            for (CategoryModel item : categoryTable.getItems()) {
                                if (item.getMaloai().equals(selectedMaLoai)) {
                                    categoryTable.getSelectionModel().select(item);
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
                if (categoryTable.getScene() == null) {
                    autoReloadTimeline.stop();
                    return;
                }
                loadDataSilently();
            })
        );
        autoReloadTimeline.setCycleCount(javafx.animation.Timeline.INDEFINITE);
        autoReloadTimeline.play();

        categoryTable.sceneProperty().addListener((obs, oldScene, newScene) -> {
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
            categoryTable.setItems(masterList);
            updateCards();
            return;
        }

        String lowerKeyword = keyword.toLowerCase().trim();
        ObservableList<CategoryModel> filtered = FXCollections.observableArrayList();
        for (CategoryModel cat : masterList) {
            if (cat.getMaloai().toLowerCase().contains(lowerKeyword) ||
                cat.getTenloai().toLowerCase().contains(lowerKeyword)) {
                filtered.add(cat);
            }
        }
        categoryTable.setItems(filtered);
        updateCards();
    }

    private void updateCards() {
        totalLabel.setText(String.valueOf(masterList.size()));
    }

    public static CategoryModel selectedCategory = null;

    @FXML
    private void handleAddNew() {
        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/fxml/categoryadd.fxml"));
            javafx.scene.Parent view = loader.load();
            javafx.scene.layout.AnchorPane parent = (javafx.scene.layout.AnchorPane) categoryTable.getScene().lookup("#contentArea");
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
        CategoryModel selected = categoryTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Vui lòng chọn một loại món từ danh sách để sửa.");
            return;
        }
        selectedCategory = selected;
        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/fxml/categoryedit.fxml"));
            javafx.scene.Parent view = loader.load();
            javafx.scene.layout.AnchorPane parent = (javafx.scene.layout.AnchorPane) categoryTable.getScene().lookup("#contentArea");
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
        CategoryModel selected = categoryTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Vui lòng chọn một loại món từ danh sách để xóa.");
            return;
        }
        selectedCategory = selected;
        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/fxml/categorydelete.fxml"));
            javafx.scene.Parent view = loader.load();
            javafx.scene.layout.AnchorPane parent = (javafx.scene.layout.AnchorPane) categoryTable.getScene().lookup("#contentArea");
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
    public static class CategoryModel {
        private final String maloai;
        private final String tenloai;

        public CategoryModel(String maloai, String tenloai) {
            this.maloai = maloai;
            this.tenloai = tenloai;
        }

        public String getMaloai() { return maloai; }
        public String getTenloai() { return tenloai; }
    }
}
