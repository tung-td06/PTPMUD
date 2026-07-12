package supplier;
 
import java.net.URL;
import java.util.ResourceBundle;
import javafx.application.Platform;
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

public class SupplierDeleteController implements Initializable {
 
    @FXML private TextField txtMaNCC;
    @FXML private TextField txtTenNCC;
    @FXML private TextField txtSDT;
    @FXML private Label lblMessage;
    @FXML private Button deleteBtn;
    @FXML private Button cancelBtn;
    @FXML private Button backBtn;
 
    private SupplierController.SupplierModel selectedSupplier;
 
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        selectedSupplier = SupplierController.selectedSupplier;
        if (selectedSupplier != null) {
            txtMaNCC.setText(selectedSupplier.getMancc());
            txtTenNCC.setText(selectedSupplier.getTenncc());
            txtSDT.setText(selectedSupplier.getSdt());
        }
    }
 
    @FXML
    private void handleDelete(ActionEvent event) {
        if (selectedSupplier == null) {
            showError("Không tìm thấy thông tin nhà cung cấp.");
            return;
        }
 
        setLoading(true);
        new Thread(() -> {
            try {
                Request request = new Request(Module.NHACUNGCAP, Action.DELETE, selectedSupplier.getMancc());
                Response response = SocketClient.getInstance().sendRequest(request);
 
                Platform.runLater(() -> {
                    if (response != null && response.isSuccess()) {
                        SupplierController.selectedSupplier = null;
                        selectedSupplier = null;
                        showSuccess("Xóa nhà cung cấp thành công!");
                        if (deleteBtn != null) deleteBtn.setDisable(true);
                        if (cancelBtn != null) cancelBtn.setDisable(true);
                    } else {
                        showError(response != null ? response.getMessage() : "Xóa nhà cung cấp thất bại! Nhà cung cấp này có thể đang có phiếu nhập hàng.");
                    }
                    setLoading(false);
                });
            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() -> {
                    showError("Lỗi kết nối hoặc xử lý: " + e.getMessage());
                    setLoading(false);
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
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/supplier.fxml"));
            Parent view = loader.load();
            AnchorPane parent = (AnchorPane) txtMaNCC.getScene().lookup("#contentArea");
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

    private void setLoading(boolean loading) {
        if (deleteBtn != null && selectedSupplier != null) deleteBtn.setDisable(loading);
        if (cancelBtn != null) cancelBtn.setDisable(loading);
        if (backBtn != null) backBtn.setDisable(loading);
    }
}
