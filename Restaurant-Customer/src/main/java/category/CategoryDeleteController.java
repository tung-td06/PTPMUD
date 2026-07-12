package category;
 
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

public class CategoryDeleteController implements Initializable {

    @FXML private TextField txtMaLoai;
    @FXML private TextField txtTenLoai;
    @FXML private Label lblMessage;
    @FXML private Button deleteBtn;

    private CategoryController.CategoryModel selectedCategory;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        selectedCategory = CategoryController.selectedCategory;
        if (selectedCategory != null) {
            txtMaLoai.setText(selectedCategory.getMaloai());
            txtTenLoai.setText(selectedCategory.getTenloai());
        }
    }

    @FXML
    private void handleDelete(ActionEvent event) {
        if (selectedCategory == null) {
            showError("Không tìm thấy thông tin danh mục loại món.");
            return;
        }

        try {
            String maloai = selectedCategory.getMaloai();
            
            // Check if any foods belong to this category
            Request countReq = new Request(Module.LOAIMON, "COUNT_MON_AN", maloai);
            Response countRes = SocketClient.getInstance().sendRequest(countReq);
            if (countRes != null && countRes.isSuccess() && countRes.getData() != null) {
                int count = ((Number) countRes.getData()).intValue();
                if (count > 0) {
                    showError("Không thể xóa loại món này vì đang có " + count + " món ăn thuộc loại món này!");
                    return;
                }
            }

            Request request = new Request(Module.LOAIMON, Action.DELETE, maloai);
            Response response = SocketClient.getInstance().sendRequest(request);

            if (response != null && response.isSuccess()) {
                showSuccess("Xóa danh mục loại món thành công!");
                deleteBtn.setDisable(true);
            } else {
                showError(response != null ? response.getMessage() : "Xóa danh mục loại món thất bại. Không nhận được phản hồi từ Server!");
            }
        } catch (Exception e) {
            showError("Lỗi: " + e.getMessage());
            e.printStackTrace();
        }
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
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/category.fxml"));
            Parent view = loader.load();
            AnchorPane parent = (AnchorPane) txtMaLoai.getScene().lookup("#contentArea");
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
