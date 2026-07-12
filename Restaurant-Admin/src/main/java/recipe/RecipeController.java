package recipe;

import java.net.URL;
import java.util.ResourceBundle;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Set;
import java.util.HashSet;
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
import network.Action;
import network.Module;
import network.Request;
import network.Response;
import network.SocketClient;

public class RecipeController implements Initializable {

    @FXML private TableView<RecipeModel> recipeTable;
    @FXML private TableColumn<RecipeModel, String> mamonColumn;
    @FXML private TableColumn<RecipeModel, String> tenmonColumn;
    @FXML private TableColumn<RecipeModel, Object> manguyenlieuColumn;
    @FXML private TextField searchField;
    @FXML private Label totalRecipesLabel;
    @FXML private Label totalIngredientsLabel;

    @FXML private Button createBtn;
    @FXML private Button viewDetailBtn;
    @FXML private Button editBtn;
    @FXML private Button deleteBtn;

    private ObservableList<RecipeModel> masterList = FXCollections.observableArrayList();
    private List<model.DinhLuongMon> rawServerRecipes = new ArrayList<>();
    private javafx.animation.Timeline autoReloadTimeline;
    private boolean isReloading = false;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        setupTable();
        loadData();
        setupSearch();
        startAutoReload();

        // Double-click row handler to open details
        recipeTable.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2 && recipeTable.getSelectionModel().getSelectedItem() != null) {
                selectedRecipe = recipeTable.getSelectionModel().getSelectedItem();
                navigate("/fxml/recipedetail.fxml");
            }
        });
    }

    private void setupTable() {
        mamonColumn.setCellValueFactory(new PropertyValueFactory<>("mamon"));
        tenmonColumn.setCellValueFactory(new PropertyValueFactory<>("tenmon"));
        manguyenlieuColumn.setCellValueFactory(new PropertyValueFactory<>("soNguyenLieu"));
    }

    private void loadData() {
        setLoadingState(true);
        new Thread(() -> {
            try {
                // Fetch Recipes
                Request recipeReq = new Request(Module.DINHLUONGMON, Action.GET_ALL, null);
                Response recipeRes = SocketClient.getInstance().sendRequest(recipeReq);

                // Fetch Dishes
                Request foodReq = new Request(Module.MONAN, Action.GET_ALL, null);
                Response foodRes = SocketClient.getInstance().sendRequest(foodReq);

                // Fetch Ingredients
                Request materialReq = new Request(Module.NGUYENLIEU, Action.GET_ALL, null);
                Response materialRes = SocketClient.getInstance().sendRequest(materialReq);

                Platform.runLater(() -> {
                    if (recipeRes != null && recipeRes.isSuccess() &&
                        foodRes != null && foodRes.isSuccess() &&
                        materialRes != null && materialRes.isSuccess()) {

                        List<model.DinhLuongMon> serverRecipes = (List<model.DinhLuongMon>) recipeRes.getData();
                        List<model.MonAn> serverFoods = (List<model.MonAn>) foodRes.getData();
                        List<model.NguyenLieu> serverMaterials = (List<model.NguyenLieu>) materialRes.getData();

                        // Map foods for quick lookup
                        java.util.Map<String, String> foodMap = new java.util.HashMap<>();
                        if (serverFoods != null) {
                            for (model.MonAn fa : serverFoods) {
                                foodMap.put(fa.getMaMon(), fa.getTenMon());
                            }
                        }

                        // Map ingredients for quick lookup
                        java.util.Map<String, model.NguyenLieu> materialMap = new java.util.HashMap<>();
                        if (serverMaterials != null) {
                            for (model.NguyenLieu nl : serverMaterials) {
                                materialMap.put(nl.getMaNL(), nl);
                            }
                        }

                        rawServerRecipes.clear();
                        if (serverRecipes != null) {
                            rawServerRecipes.addAll(serverRecipes);
                        }

                        // Group by maMon
                        java.util.Map<String, java.util.List<model.DinhLuongMon>> grouped = new java.util.LinkedHashMap<>();
                        if (serverRecipes != null) {
                            for (model.DinhLuongMon dl : serverRecipes) {
                                grouped.computeIfAbsent(dl.getMaMon(), k -> new java.util.ArrayList<>()).add(dl);
                            }
                        }

                        RecipeModel selected = recipeTable.getSelectionModel().getSelectedItem();
                        String selectedMaMon = selected != null ? selected.getMamon() : null;

                        masterList.clear();
                        for (java.util.Map.Entry<String, java.util.List<model.DinhLuongMon>> entry : grouped.entrySet()) {
                            String maMon = entry.getKey();
                            java.util.List<model.DinhLuongMon> list = entry.getValue();
                            int count = list.size();
                            String tenMon = foodMap.getOrDefault(maMon, "Không rõ");

                            StringBuilder searchSb = new StringBuilder();
                            String firstMaNL = "";
                            String firstTenNL = "";
                            String firstDinhLuongText = "";
                            int firstRawDinh = 0;
                            String firstUnit = "";

                            for (int i = 0; i < list.size(); i++) {
                                model.DinhLuongMon dl = list.get(i);
                                model.NguyenLieu nl = materialMap.get(dl.getMaNL());
                                String tenNL = nl != null ? nl.getTenNL() : "Không rõ";
                                String unit = dl.getDonViTinh() != null ? dl.getDonViTinh() : "";
                                searchSb.append(" ").append(dl.getMaNL()).append(" ").append(tenNL);

                                if (i == 0) {
                                    firstMaNL = dl.getMaNL();
                                    firstTenNL = tenNL;
                                    firstRawDinh = dl.getDinhLuong();
                                    firstUnit = unit;
                                    firstDinhLuongText = dl.getDinhLuong() + (unit.isEmpty() ? "" : " " + unit);
                                }
                            }

                            masterList.add(new RecipeModel(
                                maMon,
                                tenMon,
                                firstMaNL,
                                firstTenNL,
                                firstDinhLuongText,
                                firstRawDinh,
                                firstUnit,
                                count,
                                searchSb.toString()
                            ));
                        }

                        filterData(searchField.getText());

                        // Reselect item
                        if (selectedMaMon != null) {
                            for (RecipeModel item : recipeTable.getItems()) {
                                if (item.getMamon().equals(selectedMaMon)) {
                                    recipeTable.getSelectionModel().select(item);
                                    break;
                                }
                            }
                        }

                    } else {
                        String msg = "Không thể tải dữ liệu từ Server:";
                        if (recipeRes == null || !recipeRes.isSuccess()) msg += "\n- Lỗi tải định lượng: " + (recipeRes != null ? recipeRes.getMessage() : "Mất kết nối");
                        if (foodRes == null || !foodRes.isSuccess()) msg += "\n- Lỗi tải món ăn: " + (foodRes != null ? foodRes.getMessage() : "Mất kết nối");
                        if (materialRes == null || !materialRes.isSuccess()) msg += "\n- Lỗi tải nguyên liệu: " + (materialRes != null ? materialRes.getMessage() : "Mất kết nối");
                        showAlert(msg);
                    }
                    setLoadingState(false);
                });
            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() -> {
                    showAlert("Lỗi khi kết nối tới Server: " + e.getMessage());
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
                Request recipeReq = new Request(Module.DINHLUONGMON, Action.GET_ALL, null);
                Response recipeRes = SocketClient.getInstance().sendRequest(recipeReq);

                Request foodReq = new Request(Module.MONAN, Action.GET_ALL, null);
                Response foodRes = SocketClient.getInstance().sendRequest(foodReq);

                Request materialReq = new Request(Module.NGUYENLIEU, Action.GET_ALL, null);
                Response materialRes = SocketClient.getInstance().sendRequest(materialReq);

                Platform.runLater(() -> {
                    if (recipeRes != null && recipeRes.isSuccess() &&
                        foodRes != null && foodRes.isSuccess() &&
                        materialRes != null && materialRes.isSuccess()) {

                        List<model.DinhLuongMon> serverRecipes = (List<model.DinhLuongMon>) recipeRes.getData();
                        List<model.MonAn> serverFoods = (List<model.MonAn>) foodRes.getData();
                        List<model.NguyenLieu> serverMaterials = (List<model.NguyenLieu>) materialRes.getData();

                        java.util.Map<String, String> foodMap = new java.util.HashMap<>();
                        if (serverFoods != null) {
                            for (model.MonAn fa : serverFoods) {
                                foodMap.put(fa.getMaMon(), fa.getTenMon());
                            }
                        }

                        java.util.Map<String, model.NguyenLieu> materialMap = new java.util.HashMap<>();
                        if (serverMaterials != null) {
                            for (model.NguyenLieu nl : serverMaterials) {
                                materialMap.put(nl.getMaNL(), nl);
                            }
                        }

                        rawServerRecipes.clear();
                        if (serverRecipes != null) {
                            rawServerRecipes.addAll(serverRecipes);
                        }

                        // Group by maMon
                        java.util.Map<String, java.util.List<model.DinhLuongMon>> grouped = new java.util.LinkedHashMap<>();
                        if (serverRecipes != null) {
                            for (model.DinhLuongMon dl : serverRecipes) {
                                grouped.computeIfAbsent(dl.getMaMon(), k -> new java.util.ArrayList<>()).add(dl);
                            }
                        }

                        RecipeModel selected = recipeTable.getSelectionModel().getSelectedItem();
                        String selectedMaMon = selected != null ? selected.getMamon() : null;

                        masterList.clear();
                        for (java.util.Map.Entry<String, java.util.List<model.DinhLuongMon>> entry : grouped.entrySet()) {
                            String maMon = entry.getKey();
                            java.util.List<model.DinhLuongMon> list = entry.getValue();
                            int count = list.size();
                            String tenMon = foodMap.getOrDefault(maMon, "Không rõ");

                            StringBuilder searchSb = new StringBuilder();
                            String firstMaNL = "";
                            String firstTenNL = "";
                            String firstDinhLuongText = "";
                            int firstRawDinh = 0;
                            String firstUnit = "";

                            for (int i = 0; i < list.size(); i++) {
                                model.DinhLuongMon dl = list.get(i);
                                model.NguyenLieu nl = materialMap.get(dl.getMaNL());
                                String tenNL = nl != null ? nl.getTenNL() : "Không rõ";
                                String unit = nl != null ? nl.getDonViTinh() : "";
                                searchSb.append(" ").append(dl.getMaNL()).append(" ").append(tenNL);

                                if (i == 0) {
                                    firstMaNL = dl.getMaNL();
                                    firstTenNL = tenNL;
                                    firstRawDinh = dl.getDinhLuong();
                                    firstUnit = unit;
                                    firstDinhLuongText = dl.getDinhLuong() + (unit.isEmpty() ? "" : " " + unit);
                                }
                            }

                            masterList.add(new RecipeModel(
                                maMon,
                                tenMon,
                                firstMaNL,
                                firstTenNL,
                                firstDinhLuongText,
                                firstRawDinh,
                                firstUnit,
                                count,
                                searchSb.toString()
                            ));
                        }

                        filterData(searchField.getText());

                        if (selectedMaMon != null) {
                            for (RecipeModel item : recipeTable.getItems()) {
                                if (item.getMamon().equals(selectedMaMon)) {
                                    recipeTable.getSelectionModel().select(item);
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
        autoReloadTimeline = new javafx.animation.Timeline(
            new javafx.animation.KeyFrame(javafx.util.Duration.seconds(10), event -> {
                if (recipeTable.getScene() == null) {
                    autoReloadTimeline.stop();
                    return;
                }
                loadDataSilently();
            })
        );
        autoReloadTimeline.setCycleCount(javafx.animation.Timeline.INDEFINITE);
        autoReloadTimeline.play();

        recipeTable.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene == null && autoReloadTimeline != null) {
                autoReloadTimeline.stop();
            }
        });
    }

    private void setLoadingState(boolean loading) {
        if (recipeTable != null) recipeTable.setDisable(loading);
        if (searchField != null) searchField.setDisable(loading);
        if (createBtn != null) createBtn.setDisable(loading);
        if (viewDetailBtn != null) viewDetailBtn.setDisable(loading);
        if (editBtn != null) editBtn.setDisable(loading);
        if (deleteBtn != null) deleteBtn.setDisable(loading);
    }

    private void setupSearch() {
        searchField.textProperty().addListener((observable, oldValue, newValue) -> filterData(newValue));
    }

    private void filterData(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            recipeTable.setItems(masterList);
            updateCards();
            return;
        }
        String lw = keyword.toLowerCase().trim();
        ObservableList<RecipeModel> filtered = FXCollections.observableArrayList();
        for (RecipeModel rc : masterList) {
            if (rc.getMamon().toLowerCase().contains(lw) ||
                rc.getTenmon().toLowerCase().contains(lw) ||
                rc.getIngredientsSearchStr().toLowerCase().contains(lw)) {
                filtered.add(rc);
            }
        }
        recipeTable.setItems(filtered);
        updateCards();
    }

    private void updateCards() {
        java.util.Set<String> uniqueFoods = new java.util.HashSet<>();
        java.util.Set<String> uniqueMaterials = new java.util.HashSet<>();
        if (rawServerRecipes != null) {
            for (model.DinhLuongMon dl : rawServerRecipes) {
                uniqueFoods.add(dl.getMaMon());
                uniqueMaterials.add(dl.getMaNL());
            }
        }
        totalRecipesLabel.setText(String.valueOf(uniqueFoods.size()));
        totalIngredientsLabel.setText(String.valueOf(uniqueMaterials.size()));
    }

    /* ================= MODEL ================= */
    public static class RecipeModel {
        private final String mamon;
        private final String tenmon;
        private final String manguyenlieu;
        private final String tennguyenlieu;
        private final String dinhluong;
        private final int rawDinhLuong;
        private final String donViTinh;
        private final int soNguyenLieu;
        private final String ingredientsSearchStr;

        public RecipeModel(String mamon, String tenmon, String manguyenlieu, String tennguyenlieu, String dinhluong) {
            this(mamon, tenmon, manguyenlieu, tennguyenlieu, dinhluong, 0, "");
        }

        public RecipeModel(String mamon, String tenmon, String manguyenlieu, String tennguyenlieu, String dinhluong, int rawDinhLuong, String donViTinh) {
            this(mamon, tenmon, manguyenlieu, tennguyenlieu, dinhluong, rawDinhLuong, donViTinh, 1, "");
        }

        public RecipeModel(String mamon, String tenmon, String manguyenlieu, String tennguyenlieu, String dinhluong, int rawDinhLuong, String donViTinh, int soNguyenLieu, String ingredientsSearchStr) {
            this.mamon = mamon; this.tenmon = tenmon;
            this.manguyenlieu = manguyenlieu; this.tennguyenlieu = tennguyenlieu;
            this.dinhluong = dinhluong;
            this.rawDinhLuong = rawDinhLuong;
            this.donViTinh = donViTinh;
            this.soNguyenLieu = soNguyenLieu;
            this.ingredientsSearchStr = ingredientsSearchStr;
        }

        public String getMamon() { return mamon; }
        public String getTenmon() { return tenmon; }
        public String getManguyenlieu() { return manguyenlieu; }
        public String getTennguyenlieu() { return tennguyenlieu; }
        public String getDinhluong() { return dinhluong; }
        public int getRawDinhLuong() { return rawDinhLuong; }
        public String getDonViTinh() { return donViTinh; }
        public int getSoNguyenLieu() { return soNguyenLieu; }
        public String getIngredientsSearchStr() { return ingredientsSearchStr; }
    }

    public static RecipeModel selectedRecipe = null;

    @FXML
    private void handleAddNew() {
        navigate("/fxml/recipeadd.fxml");
    }

    @FXML
    private void handleViewDetail() {
        RecipeModel selected = recipeTable.getSelectionModel().getSelectedItem();
        if (selected == null) { showAlert("Vui lòng chọn một món ăn."); return; }
        selectedRecipe = selected;
        navigate("/fxml/recipedetail.fxml");
    }

    @FXML
    private void handleEdit() {
        RecipeModel selected = recipeTable.getSelectionModel().getSelectedItem();
        if (selected == null) { showAlert("Vui lòng chọn một món ăn."); return; }
        selectedRecipe = selected;
        navigate("/fxml/recipeedit.fxml");
    }

    @FXML
    private void handleDelete() {
        RecipeModel selected = recipeTable.getSelectionModel().getSelectedItem();
        if (selected == null) { showAlert("Vui lòng chọn một món ăn."); return; }
        selectedRecipe = selected;
        navigate("/fxml/recipedelete.fxml");
    }

    @FXML
    private void handleRefresh() {
        searchField.clear();
        loadData();
    }

    private void navigate(String fxmlPath) {
        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource(fxmlPath));
            javafx.scene.Parent view = loader.load();
            javafx.scene.layout.AnchorPane parent = (javafx.scene.layout.AnchorPane) recipeTable.getScene().lookup("#contentArea");
            if (parent != null) {
                parent.getChildren().clear();
                parent.getChildren().add(view);
                javafx.scene.layout.AnchorPane.setTopAnchor(view, 0.0);
                javafx.scene.layout.AnchorPane.setBottomAnchor(view, 0.0);
                javafx.scene.layout.AnchorPane.setLeftAnchor(view, 0.0);
                javafx.scene.layout.AnchorPane.setRightAnchor(view, 0.0);
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void showAlert(String message) {
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.WARNING);
        alert.setTitle("Thông báo"); alert.setHeaderText(null); alert.setContentText(message);
        alert.showAndWait();
    }
}
