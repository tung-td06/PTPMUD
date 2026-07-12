package customer;

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

public class CustomerAddController implements Initializable {

    @FXML private TextField txtMaKH;
    @FXML private TextField txtHoTen;
    @FXML private TextField txtSDT;
    @FXML private TextField txtDiemTichLuy;
    @FXML private Label lblMessage;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Initial setup if needed
        txtDiemTichLuy.setText("0"); // Default points
    }

    @FXML
    private void handleSave(ActionEvent event) {
        String makh = txtMaKH.getText().trim();
        String tenkh = txtHoTen.getText().trim();
        String sdt = txtSDT.getText().trim();
        String diemtichluyStr = txtDiemTichLuy.getText().trim();

        if (makh.isEmpty() || tenkh.isEmpty()) {
            showError("Vui lòng nhập đầy đủ các trường bắt buộc (*)");
            return;
        }

        if (makh.length() > 10) {
            showError("Mã khách hàng không được vượt quá 10 ký tự!");
            return;
        }

        if (!makh.matches("^[a-zA-Z0-9_-]+$")) {
            showError("Mã khách hàng chỉ được chứa chữ cái không dấu, số, gạch ngang hoặc gạch dưới!");
            return;
        }

        if (tenkh.length() > 100) {
            showError("Tên khách hàng không được vượt quá 100 ký tự!");
            return;
        }

        if (!sdt.isEmpty() && !sdt.matches("^0[0-9]{9,10}$")) {
            showError("Số điện thoại không đúng định dạng (phải bắt đầu bằng số 0 và có 10-11 số)!");
            return;
        }

        int diemtichluy = 0;
        try {
            diemtichluy = Integer.parseInt(diemtichluyStr);
            if (diemtichluy < 0) {
                showError("Điểm tích lũy phải lớn hơn hoặc bằng 0");
                return;
            }
        } catch (NumberFormatException e) {
            showError("Điểm tích lũy phải là một số nguyên hợp lệ");
            return;
        }

        try {
            // Check if MaKH already exists
            network.Request checkRequest = new network.Request(network.Module.KHACHHANG, network.Action.GET_BY_ID, makh);
            network.Response checkRes = network.SocketClient.getInstance().sendRequest(checkRequest);
            if (checkRes != null && checkRes.isSuccess() && checkRes.getData() != null) {
                showError("Mã khách hàng '" + makh + "' đã tồn tại trên hệ thống!");
                return;
            }

            model.KhachHang kh = new model.KhachHang(
                makh,
                tenkh,
                sdt.isEmpty() ? null : sdt,
                diemtichluy
            );
            network.Request request = new network.Request(network.Module.KHACHHANG, network.Action.ADD, kh);
            network.Response response = network.SocketClient.getInstance().sendRequest(request);

            if (response != null && response.isSuccess()) {
                showSuccess("Thêm khách hàng thành công!");
                clearForm();
                txtMaKH.requestFocus();
            } else {
                showError(response != null ? response.getMessage() : "Thêm khách hàng thất bại. Không nhận được phản hồi từ Server!");
            }
        } catch (Exception e) {
            showError("Lỗi: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleCancel(ActionEvent event) {
        clearForm();
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

    private void clearForm() {
        txtMaKH.clear();
        txtHoTen.clear();
        txtSDT.clear();
        txtDiemTichLuy.setText("0");
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
