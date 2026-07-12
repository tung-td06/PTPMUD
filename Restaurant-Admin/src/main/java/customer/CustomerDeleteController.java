package customer;
 
import java.net.URL;
import java.util.ResourceBundle;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
 
public class CustomerDeleteController implements Initializable {
 
    @FXML private TextField txtMaKH;
    @FXML private TextField txtHoTen;
    @FXML private TextField txtSDT;
    @FXML private TextField txtDiemTichLuy;
    @FXML private Label lblMessage;
    @FXML private Button deleteBtn;
 
    private CustomerController.CustomerModel selectedCustomer;
 
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        selectedCustomer = CustomerController.selectedCustomer;
        if (selectedCustomer != null) {
            txtMaKH.setText(selectedCustomer.getMakh());
            txtMaKH.setEditable(false);
            txtMaKH.setDisable(true);
            txtHoTen.setText(selectedCustomer.getTenkh());
            txtHoTen.setEditable(false);
            txtHoTen.setDisable(true);
            txtSDT.setText(selectedCustomer.getSdt());
            txtSDT.setEditable(false);
            txtSDT.setDisable(true);
            txtDiemTichLuy.setText(String.valueOf(selectedCustomer.getDiemtichluy()));
            txtDiemTichLuy.setEditable(false);
            txtDiemTichLuy.setDisable(true);
        }
    }
 
    @FXML
    private void handleDelete(ActionEvent event) {
        if (selectedCustomer == null) {
            showError("Không tìm thấy thông tin khách hàng.");
            return;
        }
 
        new Thread(() -> {
            try {
                network.Request request = new network.Request(
                    network.Module.KHACHHANG,
                    network.Action.DELETE,
                    selectedCustomer.getMakh()
                );
                network.Response response = network.SocketClient.getInstance().sendRequest(request);
                Platform.runLater(() -> {
                    if (response != null && response.isSuccess()) {
                        showSuccess("Xóa khách hàng thành công!");
                        deleteBtn.setDisable(true);
                    } else {
                        showError(response != null ? response.getMessage() : "Xóa thất bại. Không nhận được phản hồi từ Server!");
                    }
                });
            } catch (Exception e) {
                Platform.runLater(() -> showError("Lỗi: " + e.getMessage()));
                e.printStackTrace();
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
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/customer.fxml"));
            Parent view = loader.load();
            AnchorPane parent = (AnchorPane) txtMaKH.getScene().lookup("#contentArea");
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
