package customer.controller.customerhome;

import customer.controller.cart.CartManager;
import customer.controller.component.Dialogs;
import customer.controller.component.FoodCard;
import customer.controller.menu.CustomerMenuController;
import dashboard.DashboardController;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.layout.AnchorPane;
import model.LoaiMon;
import model.MonAn;
import network.Action;
import network.Module;
import network.Request;
import network.Response;
import network.SocketClient;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

public class CustomerHomeController implements Initializable {

    @FXML
    private TextField txtSearch;

    @FXML
    private HBox categoryBox;

    @FXML
    private FlowPane featuredPane;

    @FXML
    private FlowPane bestSellerPane;

    @FXML
    private FlowPane newDishesPane;

    @FXML
    private VBox featuredSection;

    @FXML
    private VBox bestSellerSection;

    @FXML
    private VBox newDishesSection;

    private final Map<String, String> categoryMap = new HashMap<>(); // maLoai -> tenLoai

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        setupSearchHandler();
        loadHomeData();
    }

    private void setupSearchHandler() {
        txtSearch.setOnAction(e -> {
            String query = txtSearch.getText().trim();
            if (!query.isEmpty()) {
                CustomerMenuController.searchQuery = query;
                if (DashboardController.getInstance() != null) {
                    DashboardController.getInstance().navigateToTab(1); // Navigate to Menu
                }
            }
        });
    }

    private void loadHomeData() {
        new Thread(() -> {
            try {
                // Fetch booking status
                boolean isBookingValid = customer.controller.util.CustomerSession.checkBookingValidSync();

                // 1. Fetch Categories
                Request catReq = new Request(Module.LOAIMON, Action.GET_ALL, null);
                Response catRes = SocketClient.getInstance().sendRequest(catReq);
                List<LoaiMon> categories = new ArrayList<>();
                if (catRes != null && catRes.isSuccess() && catRes.getData() != null) {
                    categories = (List<LoaiMon>) catRes.getData();
                    for (LoaiMon lm : categories) {
                        categoryMap.put(lm.getMaLoai(), lm.getTenLoai());
                    }
                }

                // 2. Fetch Mon An
                Request foodReq = new Request(Module.MONAN, Action.GET_ALL, null);
                Response foodRes = SocketClient.getInstance().sendRequest(foodReq);
                List<MonAn> foods = new ArrayList<>();
                if (foodRes != null && foodRes.isSuccess() && foodRes.getData() != null) {
                    foods = (List<MonAn>) foodRes.getData();
                }

                final List<LoaiMon> finalCats = categories;
                final List<MonAn> finalFoods = foods;
                final boolean finalBookingValid = isBookingValid;

                Platform.runLater(() -> {
                    populateCategories(finalCats);
                    populateFoodSections(finalFoods, finalBookingValid);
                });

            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void populateCategories(List<LoaiMon> categories) {
        if (categoryBox == null) return;
        categoryBox.getChildren().clear();

        // Add "Tất cả" chip
        Button btnAll = new Button("Tất cả");
        btnAll.getStyleClass().addAll("category-chip", "category-chip-active");
        btnAll.setOnAction(e -> {
            CustomerMenuController.selectedCategory = "Tất cả";
            if (DashboardController.getInstance() != null) {
                DashboardController.getInstance().navigateToTab(1);
            }
        });
        categoryBox.getChildren().add(btnAll);

        for (LoaiMon lm : categories) {
            Button btnCat = new Button(lm.getTenLoai());
            btnCat.getStyleClass().add("category-chip");
            btnCat.setOnAction(e -> {
                CustomerMenuController.selectedCategory = lm.getTenLoai();
                if (DashboardController.getInstance() != null) {
                    DashboardController.getInstance().navigateToTab(1);
                }
            });
            categoryBox.getChildren().add(btnCat);
        }
    }

    private void populateFoodSections(List<MonAn> foods, boolean isBookingValid) {
        if (featuredPane == null || bestSellerPane == null || newDishesPane == null) return;

        featuredPane.getChildren().clear();
        bestSellerPane.getChildren().clear();
        newDishesPane.getChildren().clear();

        // Filter active dishes
        List<MonAn> activeFoods = new ArrayList<>();
        for (MonAn ma : foods) {
            String state = ma.getTrangThai();
            boolean isAvailable = state != null && !state.equals("0") && !state.equalsIgnoreCase("false") && !state.equalsIgnoreCase("Hết món") && !state.equalsIgnoreCase("Hết");
            if (isAvailable) {
                activeFoods.add(ma);
            }
        }

        int count = activeFoods.size();
        if (count == 0) {
            featuredSection.setVisible(false);
            featuredSection.setManaged(false);
            bestSellerSection.setVisible(false);
            bestSellerSection.setManaged(false);
            newDishesSection.setVisible(false);
            newDishesSection.setManaged(false);
            return;
        }

        // Section 1: Featured (first 4)
        for (int i = 0; i < Math.min(4, count); i++) {
            MonAn ma = activeFoods.get(i);
            FoodCard card = new FoodCard(ma, () -> openFoodDetail(ma), () -> addToCart(ma), isBookingValid);
            card.setCategoryName(categoryMap.getOrDefault(ma.getMaLoai(), "Món ăn"));
            featuredPane.getChildren().add(card);
        }

        // Section 2: Best Sellers (middle 4 or random offset)
        int bestStart = count > 4 ? 4 : 0;
        for (int i = bestStart; i < Math.min(bestStart + 4, count); i++) {
            MonAn ma = activeFoods.get(i);
            FoodCard card = new FoodCard(ma, () -> openFoodDetail(ma), () -> addToCart(ma), isBookingValid);
            card.setCategoryName(categoryMap.getOrDefault(ma.getMaLoai(), "Món ăn"));
            bestSellerPane.getChildren().add(card);
        }

        // Section 3: New (last 4)
        int newStart = count > 8 ? count - 4 : 0;
        for (int i = newStart; i < Math.min(newStart + 4, count); i++) {
            MonAn ma = activeFoods.get(i);
            FoodCard card = new FoodCard(ma, () -> openFoodDetail(ma), () -> addToCart(ma), isBookingValid);
            card.setCategoryName(categoryMap.getOrDefault(ma.getMaLoai(), "Món ăn"));
            newDishesPane.getChildren().add(card);
        }
    }

    private void openFoodDetail(MonAn food) {
        new Thread(() -> {
            boolean valid = customer.controller.util.CustomerSession.checkBookingValidSync();
            Platform.runLater(() -> {
                if (!valid) {
                    Dialogs.showError("Gọi món", "Bạn chưa nhận bàn hoặc thời gian sử dụng bàn đã hết. Không thể gọi món.");
                } else {
                    CustomerMenuController.selectedFood = food;
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
                } else {
                    CartManager.addToCart(food, 1, "");
                    Dialogs.showSuccess("Thêm vào giỏ", "Đã thêm " + food.getTenMon() + " vào giỏ hàng thành công!");
                }
            });
        }).start();
    }
}
