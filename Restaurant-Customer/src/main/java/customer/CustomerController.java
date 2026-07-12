package customer;

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
import model.KhachHang;
import network.Action;
import network.Module;
import network.Request;
import network.Response;
import network.SocketClient;

public class CustomerController implements Initializable {

    @FXML
    private TableView<CustomerModel> customerTable;

    @FXML
    private TableColumn<CustomerModel, String> makhColumn;

    @FXML
    private TableColumn<CustomerModel, String> tenkhColumn;

    @FXML
    private TableColumn<CustomerModel, String> sdtColumn;

    @FXML
    private TableColumn<CustomerModel, Integer> diemtichluyColumn;

    @FXML
    private TextField searchField;

    @FXML
    private Label totalLabel;

    @FXML
    private Label vipLabel;

    @FXML
    private Label pointsLabel;

    @FXML
    private Button createBtn;

    @FXML
    private Button editBtn;

    @FXML
    private Button deleteBtn;

    private final ObservableList<CustomerModel> masterList = FXCollections.observableArrayList();
    private FilteredList<CustomerModel> filteredList;
    private javafx.animation.Timeline autoReloadTimeline;
    private boolean isReloading = false;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        setupTable();
        
        filteredList = new FilteredList<>(masterList, p -> true);
        SortedList<CustomerModel> sortedList = new SortedList<>(filteredList);
        sortedList.comparatorProperty().bind(customerTable.comparatorProperty());
        customerTable.setItems(sortedList);
        
        loadData();
        setupSearch();
        startAutoReload();
    }

    private void setupTable() {
        makhColumn.setCellValueFactory(new PropertyValueFactory<>("makh"));
        tenkhColumn.setCellValueFactory(new PropertyValueFactory<>("tenkh"));
        sdtColumn.setCellValueFactory(new PropertyValueFactory<>("sdt"));
        diemtichluyColumn.setCellValueFactory(new PropertyValueFactory<>("diemtichluy"));

        diemtichluyColumn.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(Integer item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                    setText(null);
                } else {
                    Label label = new Label(String.valueOf(item) + " điểm");
                    label.getStyleClass().add("status-tag");
                    if (item >= 100) {
                        label.getStyleClass().add("status-active"); // Green color for high points (VIP)
                    } else {
                        label.getStyleClass().add("status-processing"); // Blue color for member points
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
                Request request = new Request(Module.KHACHHANG, Action.GET_ALL, null);
                Response response = SocketClient.getInstance().sendRequest(request);
                Platform.runLater(() -> {
                    if (response != null && response.isSuccess()) {
                        List<KhachHang> serverList = (List<KhachHang>) response.getData();

                        CustomerModel selected = customerTable.getSelectionModel().getSelectedItem();
                        String selectedMaKH = selected != null ? selected.getMakh() : null;

                        masterList.clear();
                        MockDataStore.customers.clear();
                        MockDataStore.customerIds.clear();
                        MockDataStore.customerNames.clear();

                        if (serverList != null) {
                            for (KhachHang kh : serverList) {
                                CustomerModel model = new CustomerModel(
                                    kh.getMaKH(),
                                    kh.getTenKH(),
                                    kh.getSdt(),
                                    kh.getDiemTichLuy()
                                );
                                masterList.add(model);
                                MockDataStore.customers.add(model);
                                MockDataStore.customerIds.add(kh.getMaKH());
                                MockDataStore.customerNames.add(kh.getTenKH());
                            }
                        }

                        filterData(searchField.getText());

                        if (selectedMaKH != null) {
                            for (CustomerModel item : customerTable.getItems()) {
                                if (item.getMakh().equals(selectedMaKH)) {
                                    customerTable.getSelectionModel().select(item);
                                    break;
                                }
                            }
                        }
                    } else {
                        showAlert(response != null ? response.getMessage() : "Mất kết nối Server. Không thể tải danh sách khách hàng!");
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
                Request request = new Request(Module.KHACHHANG, Action.GET_ALL, null);
                Response response = SocketClient.getInstance().sendRequest(request);
                Platform.runLater(() -> {
                    if (response != null && response.isSuccess()) {
                        List<KhachHang> serverList = (List<KhachHang>) response.getData();

                        CustomerModel selected = customerTable.getSelectionModel().getSelectedItem();
                        String selectedMaKH = selected != null ? selected.getMakh() : null;

                        masterList.clear();
                        MockDataStore.customers.clear();
                        MockDataStore.customerIds.clear();
                        MockDataStore.customerNames.clear();

                        if (serverList != null) {
                            for (KhachHang kh : serverList) {
                                CustomerModel model = new CustomerModel(
                                    kh.getMaKH(),
                                    kh.getTenKH(),
                                    kh.getSdt(),
                                    kh.getDiemTichLuy()
                                );
                                masterList.add(model);
                                MockDataStore.customers.add(model);
                                MockDataStore.customerIds.add(kh.getMaKH());
                                MockDataStore.customerNames.add(kh.getTenKH());
                            }
                        }

                        filterData(searchField.getText());

                        if (selectedMaKH != null) {
                            for (CustomerModel item : customerTable.getItems()) {
                                if (item.getMakh().equals(selectedMaKH)) {
                                    customerTable.getSelectionModel().select(item);
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
                if (customerTable.getScene() == null) {
                    autoReloadTimeline.stop();
                    return;
                }
                loadDataSilently();
            })
        );
        autoReloadTimeline.setCycleCount(javafx.animation.Timeline.INDEFINITE);
        autoReloadTimeline.play();

        customerTable.sceneProperty().addListener((obs, oldScene, newScene) -> {
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
            filteredList.setPredicate(cust -> true);
        } else {
            String lowerKeyword = keyword.toLowerCase().trim();
            filteredList.setPredicate(cust -> {
                return cust.getMakh().toLowerCase().contains(lowerKeyword) ||
                       cust.getTenkh().toLowerCase().contains(lowerKeyword) ||
                       (cust.getSdt() != null && cust.getSdt().toLowerCase().contains(lowerKeyword));
            });
        }
        updateCards();
    }

    private void updateCards() {
        int total = masterList.size();
        int vipCount = 0;
        int totalPoints = 0;
        for (CustomerModel cust : masterList) {
            if (cust.getDiemtichluy() != null && cust.getDiemtichluy() >= 100) {
                vipCount++;
            }
            if (cust.getDiemtichluy() != null) {
                totalPoints += cust.getDiemtichluy();
            }
        }

        totalLabel.setText(String.valueOf(total));
        vipLabel.setText(String.valueOf(vipCount));
        pointsLabel.setText(String.valueOf(totalPoints));
    }

    public static CustomerModel selectedCustomer = null;

    @FXML
    private void handleAddNew() {
        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/fxml/customeradd.fxml"));
            javafx.scene.Parent view = loader.load();
            javafx.scene.layout.AnchorPane parent = (javafx.scene.layout.AnchorPane) customerTable.getScene().lookup("#contentArea");
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
        CustomerModel selected = customerTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Vui lòng chọn một khách hàng từ danh sách để sửa.");
            return;
        }
        selectedCustomer = selected;
        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/fxml/customeredit.fxml"));
            javafx.scene.Parent view = loader.load();
            javafx.scene.layout.AnchorPane parent = (javafx.scene.layout.AnchorPane) customerTable.getScene().lookup("#contentArea");
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
        CustomerModel selected = customerTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Vui lòng chọn một khách hàng từ danh sách để xóa.");
            return;
        }
        selectedCustomer = selected;
        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/fxml/customerdelete.fxml"));
            javafx.scene.Parent view = loader.load();
            javafx.scene.layout.AnchorPane parent = (javafx.scene.layout.AnchorPane) customerTable.getScene().lookup("#contentArea");
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
    public static class CustomerModel {
        private final String makh;
        private final String tenkh;
        private final String sdt;
        private final Integer diemtichluy;

        public CustomerModel(String makh, String tenkh, String sdt, Integer diemtichluy) {
            this.makh = makh;
            this.tenkh = tenkh;
            this.sdt = sdt;
            this.diemtichluy = diemtichluy;
        }

        public String getMakh() { return makh; }
        public String getTenkh() { return tenkh; }
        public String getSdt() { return sdt; }
        public Integer getDiemtichluy() { return diemtichluy; }
    }
}
