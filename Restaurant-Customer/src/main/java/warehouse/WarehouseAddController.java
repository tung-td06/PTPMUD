package warehouse;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import model.NguyenLieu;
import network.Action;
import network.Module;
import network.Request;
import network.Response;
import network.SocketClient;

public class WarehouseAddController implements Initializable {

    @FXML private TextField txtMaNL;
    @FXML private TextField txtTenNL;
    @FXML private TextField txtDonVi;
    @FXML private TextField txtSoLuong;
    @FXML private Label lblMessage;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        PlatformRunFocus();
    }

    private void PlatformRunFocus() {
        javafx.application.Platform.runLater(() -> txtMaNL.requestFocus());
    }

    @FXML
    private void handleSave(ActionEvent event) {
        String manl = txtMaNL.getText().trim();
        String tennl = txtTenNL.getText().trim();
        String donvi = txtDonVi.getText().trim();
        String soluongStr = txtSoLuong.getText().trim();

        if (manl.isEmpty() || tennl.isEmpty() || donvi.isEmpty() || soluongStr.isEmpty()) {
            showError("Vui lòng nhập đầy đủ các trường bắt buộc (*)");
            return;
        }

        if (manl.length() > 10) {
            showError("Mã nguyên liệu không được vượt quá 10 ký tự!");
            return;
        }

        if (!manl.matches("^[a-zA-Z0-9_-]+$")) {
            showError("Mã nguyên liệu chỉ được chứa chữ cái không dấu, số, gạch ngang hoặc gạch dưới!");
            return;
        }

        int qtyVal;
        try {
            qtyVal = Integer.parseInt(soluongStr);
            if (qtyVal < 0) {
                showError("Số lượng kho phải lớn hơn hoặc bằng 0");
                return;
            }
        } catch (NumberFormatException e) {
            showError("Số lượng kho phải là số nguyên hợp lệ");
            return;
        }

        try {
            // Check if MaNL already exists
            Request checkRequest = new Request(Module.NGUYENLIEU, Action.GET_BY_ID, manl);
            Response checkRes = SocketClient.getInstance().sendRequest(checkRequest);
            if (checkRes != null && checkRes.isSuccess() && checkRes.getData() != null) {
                showError("Mã nguyên liệu '" + manl + "' đã tồn tại trên hệ thống!");
                return;
            }

            NguyenLieu nl = new NguyenLieu(manl, tennl, donvi, qtyVal);
            Request request = new Request(Module.NGUYENLIEU, Action.ADD, nl);
            Response response = SocketClient.getInstance().sendRequest(request);

            if (response != null && response.isSuccess()) {
                showSuccess("Thêm nguyên liệu thành công!");
                clearForm();
                txtMaNL.requestFocus();
            } else {
                showError(response != null ? response.getMessage() : "Thêm nguyên liệu thất bại. Không nhận được phản hồi từ Server!");
            }
        } catch (Exception e) {
            showError("Lỗi: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleCancel(ActionEvent event) { clearForm(); }

    @FXML
    private void handleBack(ActionEvent event) {
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
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void clearForm() {
        txtMaNL.clear(); txtTenNL.clear(); txtDonVi.clear(); txtSoLuong.clear();
        lblMessage.setText("");
        lblMessage.getStyleClass().removeAll("message-error", "message-success");
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
