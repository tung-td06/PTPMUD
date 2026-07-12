package food;

import restaurant.MockDataStore;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
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
import javafx.scene.control.ComboBox;
import javafx.application.Platform;
import javafx.animation.Timeline;
import javafx.animation.KeyFrame;
import javafx.util.Duration;

import model.MonAn;
import model.LoaiMon;
import network.Action;
import network.Module;
import network.Request;
import network.Response;
import network.SocketClient;

import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Locale;

public class FoodController implements Initializable {

    @FXML
    private TableView<FoodModel> foodTable;

    @FXML
    private TableColumn<FoodModel, String> idColumn;

    @FXML
    private TableColumn<FoodModel, String> nameColumn;

    @FXML
    private TableColumn<FoodModel, String> categoryColumn;

    @FXML
    private TableColumn<FoodModel, String> priceColumn;

    @FXML
    private TableColumn<FoodModel, String> statusColumn;

    @FXML
    private TextField searchField;

    @FXML
    private ComboBox<String> filterCombo;

    @FXML
    private Label totalLabel;

    @FXML
    private Label activeLabel;

    @FXML
    private Label outLabel;

    @FXML
    private Button createBtn;

    @FXML
    private Button editBtn;

    @FXML
    private Button deleteBtn;

    private ObservableList<FoodModel> masterList = FXCollections.observableArrayList();
    private String currentCategory = "Tất cả";
    
    private Map<String, String> categoryMap = new HashMap<>(); // maLoai -> tenLoai
    private Timeline autoReloadTimeline;
    private boolean isReloading = false;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        setupTable();
        loadData();
        setupHeaderFilters();
        setupSearch();
    }

    private void setupTable() {
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        categoryColumn.setCellValueFactory(new PropertyValueFactory<>("category"));
        priceColumn.setCellValueFactory(new PropertyValueFactory<>("price"));
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));

        // Numerical price sorting
        priceColumn.setComparator((p1, p2) -> {
            try {
                long val1 = Long.parseLong(p1.replace(" VNĐ", "").replace(",", "").trim());
                long val2 = Long.parseLong(p2.replace(" VNĐ", "").replace(",", "").trim());
                return Long.compare(val1, val2);
            } catch (Exception e) {
                return p1.compareTo(p2);
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
                    if (item.equalsIgnoreCase("Còn món")) {
                        label.getStyleClass().add("status-active");
                    } else {
                        label.getStyleClass().add("status-out");
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
                // 1. Fetch Categories from Server
                Request catReq = new Request(Module.LOAIMON, Action.GET_ALL, null);
                Response catRes = SocketClient.getInstance().sendRequest(catReq);
                
                // 2. Fetch Dishes from Server
                Request foodReq = new Request(Module.MONAN, Action.GET_ALL, null);
                Response foodRes = SocketClient.getInstance().sendRequest(foodReq);
                
                Platform.runLater(() -> {
                    if (catRes != null && catRes.isSuccess() && catRes.getData() != null) {
                        List<LoaiMon> categories = (List<LoaiMon>) catRes.getData();
                        categoryMap.clear();
                        ObservableList<String> comboItems = FXCollections.observableArrayList();
                        comboItems.add("Tất cả");
                        for (LoaiMon lm : categories) {
                            categoryMap.put(lm.getMaLoai(), lm.getTenLoai());
                            comboItems.add(lm.getTenLoai());
                        }
                        
                        String currentVal = filterCombo.getValue();
                        filterCombo.setItems(comboItems);
                        if (currentVal != null && comboItems.contains(currentVal)) {
                            filterCombo.setValue(currentVal);
                        } else {
                            filterCombo.setValue("Tất cả");
                            currentCategory = "Tất cả";
                        }
                    }
                    
                    if (foodRes != null && foodRes.isSuccess() && foodRes.getData() != null) {
                        List<MonAn> serverFoods = (List<MonAn>) foodRes.getData();
                        
                        FoodModel selected = foodTable.getSelectionModel().getSelectedItem();
                        String selectedId = selected != null ? selected.getId() : null;
                        
                        masterList.clear();
                        for (MonAn ma : serverFoods) {
                            String catName = categoryMap.getOrDefault(ma.getMaLoai(), ma.getMaLoai());
                            String priceStr = ma.getDonGia() != null ? String.format(Locale.US, "%,d VNĐ", ma.getDonGia().longValue()) : "0 VNĐ";
                            
                            String statusStr = "Còn món";
                            if (ma.isTrangThai() != null && (ma.isTrangThai().equals("0") || ma.isTrangThai().equalsIgnoreCase("false"))) {
                                statusStr = "Hết món";
                            }
                            
                            masterList.add(new FoodModel(ma.getMaMon(), ma.getTenMon(), catName, priceStr, statusStr));
                        }
                        
                        // Sync with MockDataStore so other modules see updated dishes
                        MockDataStore.foods.clear();
                        MockDataStore.foods.addAll(masterList);
                        
                        filterData();
                        
                        if (selectedId != null) {
                            for (FoodModel item : foodTable.getItems()) {
                                if (item.getId().equals(selectedId)) {
                                    foodTable.getSelectionModel().select(item);
                                    break;
                                }
                            }
                        }
                        
                        updateCards();
                    }
                    setLoadingState(false);
                    startAutoReload();
                });
            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() -> setLoadingState(false));
            }
        }).start();
    }

    private void loadDataSilently() {
        if (isReloading) return;
        isReloading = true;
        
        new Thread(() -> {
            try {
                Request catReq = new Request(Module.LOAIMON, Action.GET_ALL, null);
                Response catRes = SocketClient.getInstance().sendRequest(catReq);
                
                Request foodReq = new Request(Module.MONAN, Action.GET_ALL, null);
                Response foodRes = SocketClient.getInstance().sendRequest(foodReq);
                
                Platform.runLater(() -> {
                    if (catRes != null && catRes.isSuccess() && catRes.getData() != null) {
                        List<LoaiMon> categories = (List<LoaiMon>) catRes.getData();
                        categoryMap.clear();
                        ObservableList<String> comboItems = FXCollections.observableArrayList();
                        comboItems.add("Tất cả");
                        for (LoaiMon lm : categories) {
                            categoryMap.put(lm.getMaLoai(), lm.getTenLoai());
                            comboItems.add(lm.getTenLoai());
                        }
                        
                        String currentVal = filterCombo.getValue();
                        filterCombo.setItems(comboItems);
                        if (currentVal != null && comboItems.contains(currentVal)) {
                            filterCombo.setValue(currentVal);
                        } else {
                            filterCombo.setValue("Tất cả");
                            currentCategory = "Tất cả";
                        }
                    }
                    
                    if (foodRes != null && foodRes.isSuccess() && foodRes.getData() != null) {
                        List<MonAn> serverFoods = (List<MonAn>) foodRes.getData();
                        
                        FoodModel selected = foodTable.getSelectionModel().getSelectedItem();
                        String selectedId = selected != null ? selected.getId() : null;
                        
                        masterList.clear();
                        for (MonAn ma : serverFoods) {
                            String catName = categoryMap.getOrDefault(ma.getMaLoai(), ma.getMaLoai());
                            String priceStr = ma.getDonGia() != null ? String.format(Locale.US, "%,d VNĐ", ma.getDonGia().longValue()) : "0 VNĐ";
                            
                            String statusStr = "Còn món";
                            if (ma.isTrangThai() != null && (ma.isTrangThai().equals("0") || ma.isTrangThai().equalsIgnoreCase("false"))) {
                                statusStr = "Hết món";
                            }
                            
                            masterList.add(new FoodModel(ma.getMaMon(), ma.getTenMon(), catName, priceStr, statusStr));
                        }
                        
                        MockDataStore.foods.clear();
                        MockDataStore.foods.addAll(masterList);
                        
                        filterData();
                        
                        if (selectedId != null) {
                            for (FoodModel item : foodTable.getItems()) {
                                if (item.getId().equals(selectedId)) {
                                    foodTable.getSelectionModel().select(item);
                                    break;
                                }
                            }
                        }
                        
                        updateCards();
                    }
                    isReloading = false;
                });
            } catch (Exception e) {
                e.printStackTrace();
                isReloading = false;
            }
        }).start();
    }

    private void startAutoReload() {
        if (autoReloadTimeline != null) {
            autoReloadTimeline.stop();
        }
        autoReloadTimeline = new Timeline(
            new KeyFrame(Duration.seconds(10), event -> {
                if (foodTable.getScene() == null) {
                    if (autoReloadTimeline != null) {
                        autoReloadTimeline.stop();
                    }
                    return;
                }
                loadDataSilently();
            })
        );
        autoReloadTimeline.setCycleCount(Timeline.INDEFINITE);
        autoReloadTimeline.play();

        foodTable.sceneProperty().addListener((obs, oldScene, newScene) -> {
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
    }

    private void setupHeaderFilters() {
        filterCombo.setValue("Tất cả");
        filterCombo.setOnAction(e -> {
            if (filterCombo.getValue() != null) {
                applyFilter(filterCombo.getValue());
            }
        });
    }

    private void applyFilter(String category) {
        currentCategory = category;
        filterData();
    }

    private void setupSearch() {
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            filterData();
        });
    }

    private void filterData() {
        String keyword = searchField.getText().toLowerCase().trim();
        ObservableList<FoodModel> filtered = FXCollections.observableArrayList();

        for (FoodModel food : masterList) {
            if (!currentCategory.equals("Tất cả") && !food.getCategory().equalsIgnoreCase(currentCategory)) {
                continue;
            }

            if (!keyword.isEmpty() &&
                    !food.getId().toLowerCase().contains(keyword) &&
                    !food.getName().toLowerCase().contains(keyword)) {
                continue;
            }

            filtered.add(food);
        }

        foodTable.setItems(filtered);
    }

    private void updateCards() {
        int total = masterList.size();
        int active = 0;
        for (FoodModel food : masterList) {
            if (food.getStatus().equalsIgnoreCase("Còn món")) {
                active++;
            }
        }
        int out = total - active;

        totalLabel.setText(String.valueOf(total));
        activeLabel.setText(String.valueOf(active));
        outLabel.setText(String.valueOf(out));
    }

    public static class FoodModel {
        private final String id;
        private final String name;
        private final String category;
        private final String price;
        private final String status;

        public FoodModel(String id, String name, String category, String price, String status) {
            this.id = id;
            this.name = name;
            this.category = category;
            this.price = price;
            this.status = status;
        }

        public String getId() {
            return id;
        }

        public String getName() {
            return name;
        }

        public String getCategory() {
            return category;
        }

        public String getPrice() {
            return price;
        }

        public String getStatus() {
            return status;
        }
    }

    public static FoodModel selectedFood = null;

    @FXML
    private void handleAddNew() {
        if (autoReloadTimeline != null) {
            autoReloadTimeline.stop();
        }
        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/fxml/foodadd.fxml"));
            javafx.scene.Parent view = loader.load();
            javafx.scene.layout.AnchorPane parent = (javafx.scene.layout.AnchorPane) foodTable.getScene()
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
        FoodModel selected = foodTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Vui lòng chọn một món ăn từ danh sách để sửa.");
            return;
        }
        selectedFood = selected;
        if (autoReloadTimeline != null) {
            autoReloadTimeline.stop();
        }
        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/fxml/foodedit.fxml"));
            javafx.scene.Parent view = loader.load();
            javafx.scene.layout.AnchorPane parent = (javafx.scene.layout.AnchorPane) foodTable.getScene()
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
        FoodModel selected = foodTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Vui lòng chọn một món ăn từ danh sách để xóa.");
            return;
        }
        selectedFood = selected;
        if (autoReloadTimeline != null) {
            autoReloadTimeline.stop();
        }
        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/fxml/fooddelete.fxml"));
            javafx.scene.Parent view = loader.load();
            javafx.scene.layout.AnchorPane parent = (javafx.scene.layout.AnchorPane) foodTable.getScene()
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
