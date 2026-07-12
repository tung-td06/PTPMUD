package customer.controller.fooddetail;

import customer.controller.cart.CartManager;
import customer.controller.component.Dialogs;
import customer.controller.menu.CustomerMenuController;
import dashboard.DashboardController;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.shape.Rectangle;
import model.MonAn;
import java.net.URL;
import java.util.Locale;
import java.util.ResourceBundle;

public class FoodDetailController implements Initializable {

    @FXML
    private ImageView imgFood;

    @FXML
    private Label lblCategory;

    @FXML
    private Label lblTitle;

    @FXML
    private Label lblPrice;

    @FXML
    private Label lblDescription;

    @FXML
    private Label lblQuantity;

    @FXML
    private TextField txtNote;

    @FXML
    private Button btnMinus;

    @FXML
    private Button btnPlus;

    @FXML
    private Button btnBack;

    @FXML
    private Button btnAddToCart;

    private MonAn currentFood;
    private int quantity = 1;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        currentFood = CustomerMenuController.selectedFood;
        if (currentFood == null) {
            handleBack();
            return;
        }

        // Bind data
        lblTitle.setText(currentFood.getTenMon());
        long priceVal = currentFood.getDonGia() != null ? currentFood.getDonGia().longValue() : 0L;
        lblPrice.setText(String.format(Locale.US, "%,d VNĐ", priceVal));
        lblQuantity.setText(String.valueOf(quantity));

        // Image rounding clip
        Rectangle clip = new Rectangle(420, 340);
        clip.setArcWidth(24);
        clip.setArcHeight(24);
        imgFood.setClip(clip);

        // Load Image
        try {
            String path = "/images/default_food.png";
            if (currentFood.getAnh() != null && !currentFood.getAnh().trim().isEmpty()) {
                String potentialPath = "/images/" + currentFood.getAnh();
                if (getClass().getResource(potentialPath) != null) {
                    path = potentialPath;
                }
            }
            Image image = customer.controller.util.ImageCache.getImage(path);
            if (image != null) {
                imgFood.setImage(image);
            }
        } catch (Exception e) {
            // ignore
        }
    }

    @FXML
    private void handleMinus() {
        if (quantity > 1) {
            quantity--;
            lblQuantity.setText(String.valueOf(quantity));
        }
    }

    @FXML
    private void handlePlus() {
        quantity++;
        lblQuantity.setText(String.valueOf(quantity));
    }

    @FXML
    private void handleBack() {
        if (DashboardController.getInstance() != null) {
            DashboardController.getInstance().navigateToTab(1); // Back to Menu
        }
    }

    @FXML
    private void handleAddToCart() {
        if (currentFood == null) return;
        btnAddToCart.setDisable(true);
        new Thread(() -> {
            boolean valid = customer.controller.util.CustomerSession.checkBookingValidSync();
            Platform.runLater(() -> {
                if (!valid) {
                    Dialogs.showError("Gọi món", "Bạn chưa nhận bàn hoặc thời gian sử dụng bàn đã hết. Không thể gọi món.");
                    btnAddToCart.setDisable(false);
                } else {
                    String note = txtNote.getText().trim();
                    CartManager.addToCart(currentFood, quantity, note);
                    Dialogs.showSuccess("Giỏ hàng", "Đã thêm " + quantity + " x " + currentFood.getTenMon() + " vào giỏ hàng thành công!");
                    handleBack();
                }
            });
        }).start();
    }
}
