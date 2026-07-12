package bill;

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

public class BillDeleteController implements Initializable {

    @FXML private TextField txtMaHD;
    @FXML private TextField txtKhachHang;
    @FXML private TextField txtBanAn;
    @FXML private TextField txtNhanVien;
    @FXML private TextField txtTimeVao;
    @FXML private TextField txtTimeRa;
    @FXML private TextField txtTongTien;
    @FXML private TextField txtGiamGia;
    @FXML private TextField txtThanhToan;
    @FXML private TextField txtTrangThai;
    @FXML private Label lblMessage;
    
    @FXML private Button deleteBtn;
    @FXML private Button cancelBtn;
    @FXML private Button backBtn;

    private BillController.BillModel selectedBill;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        selectedBill = BillController.selectedBill;
        if (selectedBill != null) {
            txtMaHD.setText(selectedBill.getId());
            txtKhachHang.setText(selectedBill.getCustomer());
            txtBanAn.setText(selectedBill.getTable());
            txtNhanVien.setText(selectedBill.getEmployee());
            txtTimeVao.setText(selectedBill.getTimeVaoStr());
            txtTimeRa.setText(selectedBill.getTimeRaStr());
            txtTongTien.setText(selectedBill.getTotal());
            txtGiamGia.setText(selectedBill.getGiamGiaStr());
            txtThanhToan.setText(selectedBill.getPayment());
            txtTrangThai.setText(selectedBill.getStatus());
        }
    }

    private void setLoadingState(boolean loading) {
        if (deleteBtn != null) deleteBtn.setDisable(loading);
        if (cancelBtn != null) cancelBtn.setDisable(loading);
        if (backBtn != null) backBtn.setDisable(loading);
    }

    @FXML
    private void handleDelete(ActionEvent event) {
        if (selectedBill == null) {
            showError("Không tìm thấy thông tin hóa đơn.");
            return;
        }

        setLoadingState(true);
        new Thread(() -> {
            try {
                Request request = new Request(Module.HOADON, Action.DELETE, selectedBill.getId());
                Response response = SocketClient.getInstance().sendRequest(request);

                Platform.runLater(() -> {
                    if (response != null && response.isSuccess()) {
                        showSuccess("Xóa hóa đơn thành công!");
                        setLoadingState(false);
                        if (deleteBtn != null) deleteBtn.setDisable(true);
                    } else {
                        showError(response != null ? response.getMessage() : "Không thể xóa hóa đơn trên Server!");
                        setLoadingState(false);
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() -> {
                    showError("Lỗi kết nối Server: " + e.getMessage());
                    setLoadingState(false);
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
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/bill.fxml"));
            Parent view = loader.load();
            AnchorPane parent = (AnchorPane) txtMaHD.getScene().lookup("#contentArea");
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

