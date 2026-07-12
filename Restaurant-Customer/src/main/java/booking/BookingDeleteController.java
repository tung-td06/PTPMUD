package booking;
 
import java.net.URL;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.application.Platform;
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
 
public class BookingDeleteController implements Initializable {
 
    @FXML private TextField txtMaDat;
    @FXML private TextField txtKhachHang;
    @FXML private TextField txtBanAn;
    @FXML private TextField txtSoNguoi;
    @FXML private TextField txtThoiGian;
    @FXML private TextField txtTrangThai;
    @FXML private TextField txtThoiGianDen;
    @FXML private Label lblMessage;
    @FXML private Button deleteBtn;
 
    private BookingController.BookingModel selectedBooking;
 
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        selectedBooking = BookingController.selectedBooking;
        if (selectedBooking != null) {
            txtMaDat.setText(selectedBooking.getId());
            txtKhachHang.setText(selectedBooking.getCustomer());
            txtBanAn.setText(selectedBooking.getTable());
            txtSoNguoi.setText(String.valueOf(selectedBooking.getGuests()));
            txtThoiGian.setText(selectedBooking.getTime());
            txtTrangThai.setText(selectedBooking.getStatus());
            if (txtThoiGianDen != null) {
                txtThoiGianDen.setText(selectedBooking.getArrivalTime());
            }
        }
    }
 
    @FXML
    private void handleDelete(ActionEvent event) {
        if (selectedBooking == null) {
            showError("Không tìm thấy thông tin đặt bàn.");
            return;
        }

        lblMessage.setText("Đang xóa đặt bàn...");
        new Thread(() -> {
            try {
                Request request = new Request(Module.DATBAN, Action.DELETE, selectedBooking.getId());
                Response response = SocketClient.getInstance().sendRequest(request);
                Platform.runLater(() -> {
                    if (response != null && response.isSuccess()) {
                        showSuccess("Hủy đặt bàn thành công!");
                        deleteBtn.setDisable(true);
                    } else {
                        showError(response != null ? response.getMessage() : "Hủy đặt bàn thất bại!");
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() -> showError("Lỗi kết nối: " + e.getMessage()));
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
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/booking.fxml"));
            Parent view = loader.load();
            AnchorPane parent = (AnchorPane) txtMaDat.getScene().lookup("#contentArea");
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
