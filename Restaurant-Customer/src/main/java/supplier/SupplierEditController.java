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

public class SupplierEditController implements Initializable {
 
    @FXML private TextField txtMaNCC;
    @FXML private TextField txtTenNCC;
    @FXML private TextField txtSDT;
    @FXML private Label lblMessage;
    @FXML private Button backBtn;
    @FXML private Button cancelBtn;
    @FXML private Button saveBtn;
 
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
    private void handleSave(ActionEvent event) {
        String mancc = txtMaNCC.getText().trim();
        String tenncc = txtTenNCC.getText().trim();
        String sdt = txtSDT.getText().trim();
 
        if (tenncc.isEmpty()) {
            showError("Tên nhà cung cấp không được để trống!");
            txtTenNCC.requestFocus();
            return;
        }
        if (tenncc.length() > 50) {
            showError("Tên nhà cung cấp không được vượt quá 50 ký tự!");
            txtTenNCC.requestFocus();
            return;
        }
        if (sdt.isEmpty()) {
            showError("Số điện thoại không được để trống!");
            txtSDT.requestFocus();
            return;
        }
        if (!sdt.matches("^[0-9]+$")) {
            showError("Số điện thoại chỉ được chứa ký tự số!");
            txtSDT.requestFocus();
            return;
        }
        if (sdt.length() < 10 || sdt.length() > 11) {
            showError("Số điện thoại phải từ 10 đến 11 số!");
            txtSDT.requestFocus();
            return;
        }
 
        setLoading(true);

        new Thread(() -> {
            try {
                // If phone is changed, check for duplicate phone number
                if (!sdt.equals(selectedSupplier.getSdt())) {
                    Request checkPhoneReq = new Request(Module.NHACUNGCAP, "FIND_BY_PHONE", sdt);
                    Response checkPhoneRes = SocketClient.getInstance().sendRequest(checkPhoneReq);
                    if (checkPhoneRes != null && checkPhoneRes.isSuccess() && checkPhoneRes.getData() != null) {
                        model.NhaCungCap duplicate = (model.NhaCungCap) checkPhoneRes.getData();
                        if (duplicate != null && !duplicate.getMaNCC().equals(mancc)) {
                            Platform.runLater(() -> {
                                showError("Số điện thoại này đã tồn tại ở nhà cung cấp khác!");
                                txtSDT.requestFocus();
                                setLoading(false);
                            });
                            return;
                        }
                    }
                }

                // Update NhaCungCap
                model.NhaCungCap ncc = new model.NhaCungCap(mancc, tenncc, sdt);
                Request req = new Request(Module.NHACUNGCAP, Action.UPDATE, ncc);
                Response response = SocketClient.getInstance().sendRequest(req);

                Platform.runLater(() -> {
                    if (response != null && response.isSuccess()) {
                        showSuccess("Cập nhật nhà cung cấp thành công!");
                        // Update static selectedSupplier to reflect edits
                        SupplierController.selectedSupplier = new SupplierController.SupplierModel(mancc, tenncc, sdt);
                        selectedSupplier = SupplierController.selectedSupplier;
                    } else {
                        showError(response != null ? response.getMessage() : "Không nhận được phản hồi từ Server!");
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
        if (selectedSupplier != null) {
            txtTenNCC.setText(selectedSupplier.getTenncc());
            txtSDT.setText(selectedSupplier.getSdt());
            lblMessage.setText("");
            lblMessage.getStyleClass().removeAll("message-error", "message-success");
        }
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
        if (saveBtn != null) saveBtn.setDisable(loading);
        if (cancelBtn != null) cancelBtn.setDisable(loading);
        if (backBtn != null) backBtn.setDisable(loading);
        txtTenNCC.setDisable(loading);
        txtSDT.setDisable(loading);
    }
}
