package food;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.application.Platform;

import network.Action;
import network.Module;
import network.Request;
import network.Response;
import network.SocketClient;

public class FoodDeleteController implements Initializable {

    @FXML private TextField txtMaMon;
    @FXML private TextField txtTenMon;
    @FXML private TextField txtLoaiMon;
    @FXML private TextField txtDonGia;
    @FXML private TextField txtTrangThai;
    @FXML private Label lblMessage;
    @FXML private Button deleteBtn;
    @FXML private Button cancelBtn;
    @FXML private Button backBtn;

    private FoodController.FoodModel selectedFood;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        selectedFood = FoodController.selectedFood;
        if (selectedFood != null) {
            txtMaMon.setText(selectedFood.getId());
            txtTenMon.setText(selectedFood.getName());
            txtLoaiMon.setText(selectedFood.getCategory());
            txtDonGia.setText(selectedFood.getPrice());
            txtTrangThai.setText(selectedFood.getStatus());
        }
    }

    private void setLoadingState(boolean loading) {
        if (deleteBtn != null) deleteBtn.setDisable(loading);
        if (cancelBtn != null) cancelBtn.setDisable(loading);
        if (backBtn != null) backBtn.setDisable(loading);
    }

    @FXML
    private void handleDelete(ActionEvent event) {
        if (selectedFood == null) {
            showError("Không tìm thấy thông tin món ăn.");
            return;
        }

        setLoadingState(true);
        lblMessage.setText("Đang xóa món ăn trên Server...");
        lblMessage.getStyleClass().removeAll("message-error", "message-success");

        new Thread(() -> {
            try {
                Request request = new Request(Module.MONAN, Action.DELETE, selectedFood.getId());
                Response response = SocketClient.getInstance().sendRequest(request);
                Platform.runLater(() -> {
                    if (response != null && response.isSuccess()) {
                        showSuccess("Xóa món ăn thành công!");
                        if (deleteBtn != null) deleteBtn.setDisable(true);
                        if (cancelBtn != null) cancelBtn.setDisable(true);
                        if (backBtn != null) backBtn.setDisable(false);
                    } else {
                        showError(response != null ? response.getMessage() : "Xóa món ăn thất bại!");
                        setLoadingState(false);
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() -> {
                    showError("Lỗi kết nối Server: " + e.getMessage());
                    setLoadingState(false);
                });
            }
        }).start();
    }

    @FXML
    private void handleCancel(ActionEvent event) {
        goBack();
    }

    @FXML
    private void handleBack(ActionEvent event) {
        goBack();
    }

    private void goBack() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/food.fxml"));
            Parent view = loader.load();
            AnchorPane parent = (AnchorPane) txtMaMon.getScene().lookup("#contentArea");
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

    private void showError(String msg) {
        lblMessage.getStyleClass().removeAll("message-error", "message-success");
        lblMessage.getStyleClass().add("message-error");
        lblMessage.setText(msg);
    }

    private void showSuccess(String msg) {
        lblMessage.getStyleClass().removeAll("message-error", "message-success");
        lblMessage.getStyleClass().add("message-success");
        lblMessage.setText(msg);
    }
}
