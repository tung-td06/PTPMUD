package warehouse;
 
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
 
public class WarehouseDeleteController implements Initializable {
 
    @FXML private TextField txtMaNL;
    @FXML private TextField txtTenNL;
    @FXML private TextField txtDonVi;
    @FXML private TextField txtSoLuong;
    @FXML private Label lblMessage;
    @FXML private Button deleteBtn;
 
    private WarehouseController.WarehouseModel selectedItem;
 
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        selectedItem = WarehouseController.selectedItem;
        if (selectedItem != null) {
            txtMaNL.setText(selectedItem.getId());
            txtTenNL.setText(selectedItem.getName());
            txtDonVi.setText(selectedItem.getUnit());
            txtSoLuong.setText(String.valueOf((int) selectedItem.getQuantity()));
        }
    }
 
    @FXML
    private void handleDelete(ActionEvent event) {
        if (selectedItem == null) {
            showError("Không tìm thấy thông tin nguyên liệu.");
            return;
        }
 
        try {
            Request request = new Request(Module.NGUYENLIEU, Action.DELETE, selectedItem.getId());
            Response response = SocketClient.getInstance().sendRequest(request);
 
            if (response != null && response.isSuccess()) {
                WarehouseController.selectedItem = null;
                showSuccess("Xóa nguyên liệu thành công!");
                deleteBtn.setDisable(true);
            } else {
                showError("Xóa nguyên liệu thất bại! Nguyên liệu này có thể đang được sử dụng ở Định lượng món hoặc Phiếu nhập hàng.");
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
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/warehouse.fxml"));
            Parent view = loader.load();
            AnchorPane parent = (AnchorPane) txtMaNL.getScene().lookup("#contentArea");
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
