package recipe;
 
import java.net.URL;
import java.util.List;
import java.util.ArrayList;
import java.util.ResourceBundle;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.AnchorPane;
import javafx.scene.Parent;
import javafx.fxml.FXMLLoader;
import javafx.animation.Timeline;
import javafx.animation.KeyFrame;
import javafx.animation.Animation;
import javafx.util.Duration;
 
import network.Action;
import network.Module;
import network.Request;
import network.Response;
import network.SocketClient;
 
public class RecipeDetailController implements Initializable {
 
    @FXML private Label lblSubtitle;
    @FXML private Label lblMamon;
    @FXML private Label lblTenmon;
    @FXML private Label lblLoaimon;
    @FXML private Label lblGiaban;
    @FXML private Label lblTotalIngredients;
 
    @FXML private TableView<RecipeDetailItemModel> detailTable;
    @FXML private TableColumn<RecipeDetailItemModel, Object> colStt;
    @FXML private TableColumn<RecipeDetailItemModel, String> colMaNL;
    @FXML private TableColumn<RecipeDetailItemModel, String> colTenNL;
    @FXML private TableColumn<RecipeDetailItemModel, Integer> colDinhLuong;
    @FXML private TableColumn<RecipeDetailItemModel, String> colDonVi;
 
    @FXML private Button btnClose;
 
    private final ObservableList<RecipeDetailItemModel> detailsList = FXCollections.observableArrayList();
    private Timeline autoReloadTimeline;
    private boolean isReloading = false;
 
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        setupTableColumns();
        loadDetail();
        startAutoReload();
    }
 
    private void setupTableColumns() {
        colStt.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(Object item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setText(null);
                } else {
                    setText(String.valueOf(getIndex() + 1));
                }
            }
        });
 
        colMaNL.setCellValueFactory(new PropertyValueFactory<>("maNL"));
        colTenNL.setCellValueFactory(new PropertyValueFactory<>("tenNL"));
        colDinhLuong.setCellValueFactory(new PropertyValueFactory<>("dinhLuong"));
        colDonVi.setCellValueFactory(new PropertyValueFactory<>("donVi"));
 
        detailTable.setItems(detailsList);
    }
 
    private void loadDetail() {
        RecipeController.RecipeModel selected = RecipeController.selectedRecipe;
        if (selected == null) {
            return;
        }
 
        lblMamon.setText(selected.getMamon());
        lblTenmon.setText(selected.getTenmon());
        lblSubtitle.setText("Mã món: " + selected.getMamon() + " | Tên món: " + selected.getTenmon());
 
        new Thread(() -> {
            try {
                // 1. Fetch Dishes to resolve Category Code and Price
                Request foodReq = new Request(Module.MONAN, Action.GET_ALL, null);
                Response foodRes = SocketClient.getInstance().sendRequest(foodReq);
                String maLoai = "";
                String priceStr = "--";
                if (foodRes != null && foodRes.isSuccess()) {
                    List<model.MonAn> foods = (List<model.MonAn>) foodRes.getData();
                    if (foods != null) {
                        for (model.MonAn ma : foods) {
                            if (ma.getMaMon().equalsIgnoreCase(selected.getMamon())) {
                                maLoai = ma.getMaLoai();
                                priceStr = ma.getDonGia() != null ? String.format("%,.0f VNĐ", ma.getDonGia().doubleValue()) : "0 VNĐ";
                                break;
                            }
                        }
                    }
                }
 
                // 2. Fetch Categories to resolve Category Name
                String categoryName = maLoai;
                if (!maLoai.isEmpty()) {
                    Request catReq = new Request(Module.LOAIMON, Action.GET_ALL, null);
                    Response catRes = SocketClient.getInstance().sendRequest(catReq);
                    if (catRes != null && catRes.isSuccess()) {
                        List<model.LoaiMon> categories = (List<model.LoaiMon>) catRes.getData();
                        if (categories != null) {
                            for (model.LoaiMon lm : categories) {
                                if (lm.getMaLoai().equalsIgnoreCase(maLoai)) {
                                    categoryName = lm.getTenLoai();
                                    break;
                                }
                            }
                        }
                    }
                }
 
                // 3. Fetch Recipes matching mamon
                Request recipeReq = new Request(Module.DINHLUONGMON, Action.GET_BY_MONAN, selected.getMamon());
                Response recipeRes = SocketClient.getInstance().sendRequest(recipeReq);
                List<RecipeDetailItemModel> list = new ArrayList<>();
                if (recipeRes != null && recipeRes.isSuccess()) {
                    List<model.DinhLuongMon> recipes = (List<model.DinhLuongMon>) recipeRes.getData();
                    if (recipes != null) {
                        for (model.DinhLuongMon dl : recipes) {
                            // Check actual real-time data with server for each ingredient to fix unit issues
                            Request matReq = new Request(Module.NGUYENLIEU, Action.GET_BY_ID, dl.getMaNL());
                            Response matRes = SocketClient.getInstance().sendRequest(matReq);
                            String tenNL = "Không rõ";
                            String donVi = "";
                            if (matRes != null && matRes.isSuccess() && matRes.getData() != null) {
                                model.NguyenLieu nl = (model.NguyenLieu) matRes.getData();
                                tenNL = nl.getTenNL();
                            }
                            donVi = dl.getDonViTinh() != null ? dl.getDonViTinh() : "";
                            list.add(new RecipeDetailItemModel(dl.getMaNL(), tenNL, dl.getDinhLuong(), donVi));
                        }
                    }
                }
 
                final String finalCategory = categoryName.isEmpty() ? "--" : categoryName;
                final String finalPrice = priceStr;
                Platform.runLater(() -> {
                    lblLoaimon.setText(finalCategory);
                    lblGiaban.setText(finalPrice);
                    detailsList.setAll(list);
                    lblTotalIngredients.setText(String.valueOf(list.size()));
                });
 
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }
 
    private void loadDataSilently() {
        RecipeController.RecipeModel selected = RecipeController.selectedRecipe;
        if (selected == null) {
            return;
        }
        isReloading = true;
        new Thread(() -> {
            try {
                // Fetch Recipes
                Request recipeReq = new Request(Module.DINHLUONGMON, Action.GET_BY_MONAN, selected.getMamon());
                Response recipeRes = SocketClient.getInstance().sendRequest(recipeReq);
                List<RecipeDetailItemModel> list = new ArrayList<>();
                if (recipeRes != null && recipeRes.isSuccess()) {
                    List<model.DinhLuongMon> recipes = (List<model.DinhLuongMon>) recipeRes.getData();
                    if (recipes != null) {
                        for (model.DinhLuongMon dl : recipes) {
                            // Check actual real-time data with server for each ingredient to fix unit issues
                            Request matReq = new Request(Module.NGUYENLIEU, Action.GET_BY_ID, dl.getMaNL());
                            Response matRes = SocketClient.getInstance().sendRequest(matReq);
                            String tenNL = "Không rõ";
                            String donVi = "";
                            if (matRes != null && matRes.isSuccess() && matRes.getData() != null) {
                                model.NguyenLieu nl = (model.NguyenLieu) matRes.getData();
                                tenNL = nl.getTenNL();
                            }
                            donVi = dl.getDonViTinh() != null ? dl.getDonViTinh() : "";
                            list.add(new RecipeDetailItemModel(dl.getMaNL(), tenNL, dl.getDinhLuong(), donVi));
                        }
                    }
                }
 
                Platform.runLater(() -> {
                    detailsList.setAll(list);
                    lblTotalIngredients.setText(String.valueOf(list.size()));
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
        autoReloadTimeline = new Timeline(
            new KeyFrame(Duration.seconds(10), event -> {
                if (detailTable.getScene() == null) {
                    autoReloadTimeline.stop();
                    return;
                }
                if (!isReloading) {
                    loadDataSilently();
                }
            })
        );
        autoReloadTimeline.setCycleCount(Timeline.INDEFINITE);
        autoReloadTimeline.play();
 
        detailTable.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene == null && autoReloadTimeline != null) {
                autoReloadTimeline.stop();
            }
        });
    }
 
    @FXML
    private void handleClose() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/recipe.fxml"));
            Parent view = loader.load();
            AnchorPane parent = (AnchorPane) btnClose.getScene().lookup("#contentArea");
            if (parent != null) {
                parent.getChildren().clear();
                parent.getChildren().add(view);
                AnchorPane.setTopAnchor(view, 0.0);
                AnchorPane.setBottomAnchor(view, 0.0);
                AnchorPane.setLeftAnchor(view, 0.0);
                AnchorPane.setRightAnchor(view, 0.0);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
 
    /* ================= MODEL ================= */
    public static class RecipeDetailItemModel {
        private final String maNL;
        private final String tenNL;
        private final int dinhLuong;
        private final String donVi;
 
        public RecipeDetailItemModel(String maNL, String tenNL, int dinhLuong, String donVi) {
            this.maNL = maNL;
            this.tenNL = tenNL;
            this.dinhLuong = dinhLuong;
            this.donVi = donVi;
        }
 
        public String getMaNL() { return maNL; }
        public String getTenNL() { return tenNL; }
        public int getDinhLuong() { return dinhLuong; }
        public String getDonVi() { return donVi; }
    }
}
