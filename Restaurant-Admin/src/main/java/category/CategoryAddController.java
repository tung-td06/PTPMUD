package category;

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
import model.LoaiMon;
import network.Action;
import network.Module;
import network.Request;
import network.Response;
import network.SocketClient;

public class CategoryAddController implements Initializable {

    @FXML private TextField txtMaLoai;
    @FXML private TextField txtTenLoai;
    @FXML private Label lblMessage;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Initial setup if needed
    }

    @FXML
    private void handleSave(ActionEvent event) {
        String maloai = txtMaLoai.getText().trim();
        String tenloai = txtTenLoai.getText().trim();

        if (maloai.isEmpty() || tenloai.isEmpty()) {
            showError("Vui lòng nhập đầy đủ các trường bắt buộc (*)");
            return;
        }

        if (maloai.length() > 10) {
            showError("Mã loại món không được vượt quá 10 ký tự!");
            return;
        }

        if (!maloai.matches("^[a-zA-Z0-9_-]+$")) {
            showError("Mã loại món chỉ được chứa chữ cái không dấu, số, gạch ngang hoặc gạch dưới!");
            return;
        }

        if (tenloai.length() > 100) {
            showError("Tên loại món không được vượt quá 100 ký tự!");
            return;
        }

        try {
            // Check if MaLoai already exists
            Request checkRequest = new Request(Module.LOAIMON, "EXISTS", maloai);
            Response checkRes = SocketClient.getInstance().sendRequest(checkRequest);
            if (checkRes != null && checkRes.isSuccess() && checkRes.getData() != null && (Boolean) checkRes.getData()) {
                showError("Mã loại món '" + maloai + "' đã tồn tại trên hệ thống!");
                return;
            }

            LoaiMon lm = new LoaiMon(maloai, tenloai);
            Request request = new Request(Module.LOAIMON, Action.ADD, lm);
            Response response = SocketClient.getInstance().sendRequest(request);

            if (response != null && response.isSuccess()) {
                showSuccess("Thêm loại món thành công!");
                clearForm();
                txtMaLoai.requestFocus();
            } else {
                showError(response != null ? response.getMessage() : "Thêm loại món thất bại. Không nhận được phản hồi từ Server!");
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

    private void clearForm() {
        txtMaLoai.clear();
        txtTenLoai.clear();
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
