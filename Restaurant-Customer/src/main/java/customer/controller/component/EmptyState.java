package customer.controller.component;

import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

public class EmptyState extends VBox {

    public EmptyState(String emoji, String title, String description) {
        this.getStyleClass().add("empty-pane");
        this.setSpacing(10);
        this.setAlignment(Pos.CENTER);

        Label lblIcon = new Label(emoji);
        lblIcon.getStyleClass().add("empty-icon");

        Label lblTitle = new Label(title);
        lblTitle.getStyleClass().add("empty-title");

        Label lblDesc = new Label(description);
        lblDesc.getStyleClass().add("empty-desc");
        lblDesc.setWrapText(true);

        this.getChildren().addAll(lblIcon, lblTitle, lblDesc);
    }
}
