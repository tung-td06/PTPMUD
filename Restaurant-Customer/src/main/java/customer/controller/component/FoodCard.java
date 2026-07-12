package customer.controller.component;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Rectangle;
import model.MonAn;
import java.util.Locale;

public class FoodCard extends VBox {

    public FoodCard(MonAn food, Runnable onViewDetail, Runnable onAddToCart, boolean isBookingValid) {
        // Style class
        this.getStyleClass().add("food-card");
        this.setSpacing(10);
        this.setPrefWidth(220);
        this.setMaxWidth(250);

        // Image Container
        ImageView imageView = new ImageView();
        imageView.setFitWidth(200);
        imageView.setFitHeight(125);
        imageView.setPreserveRatio(false);

        // Image rounding clip
        Rectangle clip = new Rectangle(200, 125);
        clip.setArcWidth(16);
        clip.setArcHeight(16);
        imageView.setClip(clip);

        // Load image
        try {
            String path = "/images/default_food.png";
            if (food.getAnh() != null && !food.getAnh().trim().isEmpty()) {
                String potentialPath = "/images/" + food.getAnh();
                if (getClass().getResource(potentialPath) != null) {
                    path = potentialPath;
                }
            }
            Image image = customer.controller.util.ImageCache.getImage(path);
            if (image != null) {
                imageView.setImage(image);
            }
        } catch (Exception e) {
            // Ignore if missing during early init
        }

        VBox imageWrapper = new VBox(imageView);
        imageWrapper.setAlignment(Pos.CENTER);

        // Labels
        Label lblName = new Label(food.getTenMon());
        lblName.getStyleClass().add("food-card-title");
        lblName.setWrapText(true);
        lblName.setMinHeight(40);
        lblName.setMaxHeight(40);

        Label lblCategory = new Label("Loại món");
        lblCategory.getStyleClass().add("food-card-category");

        // Status badge
        String trangThai = food.getTrangThai();
        boolean isAvailable = trangThai != null && !trangThai.equals("0") && !trangThai.equalsIgnoreCase("false") && !trangThai.equalsIgnoreCase("Hết món") && !trangThai.equalsIgnoreCase("Hết");

        Label lblStatus = new Label(isAvailable ? "Còn món" : "Hết món");
        lblStatus.getStyleClass().addAll("status-badge", isAvailable ? "badge-available" : "badge-out");

        // Price
        long priceVal = food.getDonGia() != null ? food.getDonGia().longValue() : 0L;
        Label lblPrice = new Label(String.format(Locale.US, "%,dđ", priceVal));
        lblPrice.getStyleClass().add("food-card-price");

        HBox infoRow = new HBox(lblPrice, new Region(), lblStatus);
        HBox.setHgrow(infoRow.getChildren().get(1), Priority.ALWAYS);
        infoRow.setAlignment(Pos.CENTER_LEFT);

        // Buttons
        Button btnView = new Button("Xem");
        btnView.getStyleClass().add("btn-secondary");
        btnView.setPrefWidth(80);
        btnView.setOnAction(e -> onViewDetail.run());

        Button btnAdd = new Button("Thêm");
        btnAdd.getStyleClass().add("btn-primary");
        btnAdd.setPrefWidth(100);
        btnAdd.setDisable(!isAvailable || !isBookingValid);
        btnAdd.setOnAction(e -> onAddToCart.run());

        HBox btnRow = new HBox(btnView, new Region(), btnAdd);
        HBox.setHgrow(btnRow.getChildren().get(1), Priority.ALWAYS);
        btnRow.setAlignment(Pos.CENTER);
        btnRow.setPadding(new Insets(5, 0, 0, 0));

        this.getChildren().addAll(imageWrapper, lblCategory, lblName, infoRow, btnRow);
        
        // Dynamic hover animation (scale effect)
        this.setOnMouseEntered(e -> {
            this.setScaleX(1.03);
            this.setScaleY(1.03);
        });
        this.setOnMouseExited(e -> {
            this.setScaleX(1.0);
            this.setScaleY(1.0);
        });
    }

    public void setCategoryName(String catName) {
        for (int i = 0; i < getChildren().size(); i++) {
            if (getChildren().get(i) instanceof Label lbl) {
                if (lbl.getStyleClass().contains("food-card-category")) {
                    lbl.setText(catName);
                    break;
                }
            }
        }
    }
}
