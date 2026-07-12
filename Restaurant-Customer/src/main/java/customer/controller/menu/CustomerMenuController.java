package customer.controller.menu;

import customer.controller.cart.CartManager;
import customer.controller.component.Dialogs;
import customer.controller.component.FoodCard;
import dashboard.DashboardController;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.FlowPane;
import model.LoaiMon;
import model.MonAn;
import network.Action;
import network.Module;
import network.Request;
import network.Response;
import network.SocketClient;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

public class CustomerMenuController implements Initializable {

    public static String selectedCategory = "Tất cả";
    public static String searchQuery = "";
    public static MonAn selectedFood = null;

    @FXML
    private TextField txtSearch;

    @FXML
    private ComboBox<String> cmbCategory;

    @FXML
    private ComboBox<String> cmbSort;

    @FXML
    private FlowPane menuPane;

    private final List<MonAn> masterFoodList = new ArrayList<>();
    private final Map<String, String> categoryMap = new HashMap<>(); // maLoai -> tenLoai
    private boolean isBookingValid = false;
    private long lastCheckTime = 0;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        setupSortComboBox();
        setupFilters();
        loadMenuData();
    }

    private void setupSortComboBox() {
        cmbSort.setItems(FXCollections.observableArrayList(
                "Mặc định",
                "Giá: Thấp đến Cao",
                "Giá: Cao đến Thấp",
                "Tên: A đến Z",
                "Tên: Z đến A"
        ));
        cmbSort.setValue("Mặc định");
    }

    private void setupFilters() {
        // Set search text if redirected from Home
        if (searchQuery != null && !searchQuery.isEmpty()) {
            txtSearch.setText(searchQuery);
            searchQuery = ""; // reset
        }

        // Add filter listeners with booking checks
        txtSearch.textProperty().addListener((o, ov, nv) -> checkBookingAndApplyFilters());
        cmbCategory.valueProperty().addListener((o, ov, nv) -> checkBookingAndApplyFilters());
        cmbSort.valueProperty().addListener((o, ov, nv) -> checkBookingAndApplyFilters());
    }

    private void checkBookingAndApplyFilters() {
        long now = System.currentTimeMillis();
        if (now - lastCheckTime < 1000) {
            applyFilters();
            return;
        }
        lastCheckTime = now;
        new Thread(() -> {
            boolean valid = customer.controller.util.CustomerSession.checkBookingValidSync();
            Platform.runLater(() -> {
                this.isBookingValid = valid;
                applyFilters();
            });
        }).start();
    }

    private void loadMenuData() {
        new Thread(() -> {
            try {
                // Fetch booking status
                boolean valid = customer.controller.util.CustomerSession.checkBookingValidSync();

                // Fetch categories
                Request catReq = new Request(Module.LOAIMON, Action.GET_ALL, null);
                Response catRes = SocketClient.getInstance().sendRequest(catReq);
                List<String> catNames = new ArrayList<>();
                catNames.add("Tất cả");
                if (catRes != null && catRes.isSuccess() && catRes.getData() != null) {
                    List<LoaiMon> categories = (List<LoaiMon>) catRes.getData();
                    for (LoaiMon lm : categories) {
                        categoryMap.put(lm.getMaLoai(), lm.getTenLoai());
                        catNames.add(lm.getTenLoai());
                    }
                }

                // Fetch dishes
                Request foodReq = new Request(Module.MONAN, Action.GET_ALL, null);
                Response foodRes = SocketClient.getInstance().sendRequest(foodReq);
                if (foodRes != null && foodRes.isSuccess() && foodRes.getData() != null) {
                    masterFoodList.clear();
                    masterFoodList.addAll((List<MonAn>) foodRes.getData());
                }

                Platform.runLater(() -> {
                    this.isBookingValid = valid;
                    if (!valid) {
                        Dialogs.showError("Thực đơn", "Bạn chưa nhận bàn hoặc thời gian sử dụng bàn đã hết.\nKhông thể gọi món.");
                    }
                    cmbCategory.setItems(FXCollections.observableArrayList(catNames));
                    // Restore category selection if redirected from Home
                    if (selectedCategory != null && catNames.contains(selectedCategory)) {
                        cmbCategory.setValue(selectedCategory);
                        selectedCategory = "Tất cả"; // reset
                    } else {
                        cmbCategory.setValue("Tất cả");
                    }
                    applyFilters();
                });

            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void applyFilters() {
        if (menuPane == null) return;
        menuPane.getChildren().clear();

        String query = txtSearch.getText().trim().toLowerCase();
        String selectedCat = cmbCategory.getValue();
        String sortOption = cmbSort.getValue();

        List<MonAn> filteredList = new ArrayList<>();

        for (MonAn ma : masterFoodList) {
            // 1. Availability filter (trangthai = 1 or "Còn món")
            String state = ma.getTrangThai();
            boolean isAvailable = state != null && !state.equals("0") && !state.equalsIgnoreCase("false") && !state.equalsIgnoreCase("Hết món") && !state.equalsIgnoreCase("Hết");
            if (!isAvailable) {
                continue;
            }

            // 2. Category filter
            String catName = categoryMap.getOrDefault(ma.getMaLoai(), "");
            if (selectedCat != null && !selectedCat.equals("Tất cả") && !catName.equalsIgnoreCase(selectedCat)) {
                continue;
            }

            // 3. Search query filter
            if (!query.isEmpty() && !ma.getTenMon().toLowerCase().contains(query)) {
                continue;
            }

            filteredList.add(ma);
        }

        // 4. Sort
        if (sortOption != null) {
            switch (sortOption) {
                case "Giá: Thấp đến Cao":
                    filteredList.sort((m1, m2) -> {
                        double p1 = m1.getDonGia() != null ? m1.getDonGia().doubleValue() : 0;
                        double p2 = m2.getDonGia() != null ? m2.getDonGia().doubleValue() : 0;
                        return Double.compare(p1, p2);
                    });
                    break;
                case "Giá: Cao đến Thấp":
                    filteredList.sort((m1, m2) -> {
                        double p1 = m1.getDonGia() != null ? m1.getDonGia().doubleValue() : 0;
                        double p2 = m2.getDonGia() != null ? m2.getDonGia().doubleValue() : 0;
                        return Double.compare(p2, p1);
                    });
                    break;
                case "Tên: A đến Z":
                    filteredList.sort((m1, m2) -> m1.getTenMon().compareToIgnoreCase(m2.getTenMon()));
                    break;
                case "Tên: Z đến A":
                    filteredList.sort((m1, m2) -> m2.getTenMon().compareToIgnoreCase(m1.getTenMon()));
                    break;
                default:
                    // default order from server
                    break;
            }
        }

        // Populate grid
        if (filteredList.isEmpty()) {
            // Add a clean label or empty view if needed
        } else {
            for (MonAn ma : filteredList) {
                FoodCard card = new FoodCard(ma, () -> openFoodDetail(ma), () -> addToCart(ma), this.isBookingValid);
                card.setCategoryName(categoryMap.getOrDefault(ma.getMaLoai(), "Món ăn"));
                menuPane.getChildren().add(card);
            }
        }
    }

    private void openFoodDetail(MonAn food) {
        new Thread(() -> {
            boolean valid = customer.controller.util.CustomerSession.checkBookingValidSync();
            Platform.runLater(() -> {
                if (!valid) {
                    Dialogs.showError("Gọi món", "Bạn chưa nhận bàn hoặc thời gian sử dụng bàn đã hết. Không thể gọi món.");
                    this.isBookingValid = false;
                    applyFilters();
                } else {
                    selectedFood = food;
                    if (DashboardController.getInstance() != null) {
                        try {
                            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/fxml/customer/food_detail.fxml"));
                            javafx.scene.Parent view = loader.load();
                            AnchorPane contentArea = (AnchorPane) DashboardController.getInstance().getScene().lookup("#contentArea");
                            if (contentArea != null) {
                                contentArea.getChildren().clear();
                                contentArea.getChildren().add(view);
                                AnchorPane.setTopAnchor(view, 0.0);
                                AnchorPane.setBottomAnchor(view, 0.0);
                                AnchorPane.setLeftAnchor(view, 0.0);
                                AnchorPane.setRightAnchor(view, 0.0);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            });
        }).start();
    }

    private void addToCart(MonAn food) {
        new Thread(() -> {
            boolean valid = customer.controller.util.CustomerSession.checkBookingValidSync();
            Platform.runLater(() -> {
                if (!valid) {
                    Dialogs.showError("Gọi món", "Bạn chưa nhận bàn hoặc thời gian sử dụng bàn đã hết. Không thể gọi món.");
                    this.isBookingValid = false;
                    applyFilters();
                } else {
                    CartManager.addToCart(food, 1, "");
                    Dialogs.showSuccess("Thêm vào giỏ", "Đã thêm " + food.getTenMon() + " vào giỏ hàng thành công!");
                }
            });
        }).start();
    }
}
