package recipe;

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
import network.Action;
import network.Module;
import network.Request;
import network.Response;
import network.SocketClient;

public class RecipeDeleteController implements Initializable {

    @FXML private TextField txtMamon;
    @FXML private TextField txtTenmon;
    @FXML private TextField txtMaNguyenLieu;
    @FXML private TextField txtTenNguyenLieu;
    @FXML private Label lblMessage;
    @FXML private Button deleteBtn;

    private RecipeController.RecipeModel selectedRecipe;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        selectedRecipe = RecipeController.selectedRecipe;
        if (selectedRecipe != null) {
            txtMamon.setText(selectedRecipe.getMamon());
            txtTenmon.setText(selectedRecipe.getTenmon());
            txtMaNguyenLieu.setText(selectedRecipe.getManguyenlieu());
            txtTenNguyenLieu.setText(selectedRecipe.getTennguyenlieu());
        }
    }

    @FXML
    private void handleDelete(ActionEvent event) {
        if (selectedRecipe == null) {
            showError("Không tìm thấy thông tin.");
            return;
        }

        try {
            Request request = new Request(Module.DINHLUONGMON, Action.DELETE, new String[]{selectedRecipe.getMamon(), selectedRecipe.getManguyenlieu()});
            Response response = SocketClient.getInstance().sendRequest(request);

            if (response != null && response.isSuccess()) {
                showSuccess("Xóa công thức món ăn thành công!");
                deleteBtn.setDisable(true);
            } else {
                showError(response != null ? response.getMessage() : "Xóa thất bại. Không nhận được phản hồi từ Server!");
            }
        } catch (Exception e) { showError("Lỗi: " + e.getMessage()); }
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
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/recipe.fxml"));
            Parent view = loader.load();
            AnchorPane parent = (AnchorPane) txtMamon.getScene().lookup("#contentArea");
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
