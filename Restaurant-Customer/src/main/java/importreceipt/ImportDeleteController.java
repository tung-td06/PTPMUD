package importreceipt;
 
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
import network.Action;
import network.Module;
import network.Request;
import network.Response;
import network.SocketClient;
 
public class ImportDeleteController implements Initializable {
 
    @FXML private TextField txtMaHang;
    @FXML private TextField txtNhaCungCap;
    @FXML private TextField txtNgayNhap;
    @FXML private TextField txtTongTien;
    @FXML private Label lblMessage;
    @FXML private Button deleteBtn;
 
    private ImportController.ImportModel selectedImport;
 
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        selectedImport = ImportController.selectedImport;
        if (selectedImport != null) {
            txtMaHang.setText(selectedImport.getMahang());
            txtNhaCungCap.setText(selectedImport.getMancc());
            txtNgayNhap.setText(selectedImport.getNgaynhap());
            txtTongTien.setText(String.format("%,.0f VNĐ", selectedImport.getTongtien()));
        }
    }
 
    @FXML
    private void handleDelete(ActionEvent event) {
        if (selectedImport == null) {
            showError("Không tìm thấy thông tin phiếu nhập hàng.");
            return;
        }
 
        new Thread(() -> {
            try {
                Request request = new Request(Module.NHAPHANG, Action.DELETE, selectedImport.getMahang());
                Response response = SocketClient.getInstance().sendRequest(request);
 
                if (response != null && response.isSuccess()) {
                    Platform.runLater(() -> {
                        ImportController.selectedImport = null;
                        showSuccess("Xóa phiếu nhập hàng thành công!");
                        deleteBtn.setDisable(true);
                    });
                } else {
                    Platform.runLater(() -> showError("Xóa phiếu nhập hàng thất bại! Hãy chắc chắn bạn đã xóa hết Chi tiết phiếu nhập của phiếu này trước."));
                }
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
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/import.fxml"));
            Parent view = loader.load();
            AnchorPane parent = (AnchorPane) txtMaHang.getScene().lookup("#contentArea");
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
