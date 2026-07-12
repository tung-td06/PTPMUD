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
 
public class CustomerEditController implements Initializable {
 
    @FXML private TextField txtMaKH;
    @FXML private TextField txtHoTen;
    @FXML private TextField txtSDT;
    @FXML private TextField txtDiemTichLuy;
    @FXML private Label lblMessage;
 
    private CustomerController.CustomerModel selectedCustomer;
 
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        selectedCustomer = CustomerController.selectedCustomer;
        if (selectedCustomer != null) {
            txtMaKH.setText(selectedCustomer.getMakh());
            txtMaKH.setEditable(false);
            txtMaKH.setDisable(true);
            txtHoTen.setText(selectedCustomer.getTenkh());
            txtSDT.setText(selectedCustomer.getSdt());
            txtDiemTichLuy.setText(String.valueOf(selectedCustomer.getDiemtichluy()));
        }
    }
 
    @FXML
    private void handleSave(ActionEvent event) {
        String makh = txtMaKH.getText().trim();
        String hoten = txtHoTen.getText().trim();
        String sdt = txtSDT.getText().trim();
        String diemStr = txtDiemTichLuy.getText().trim();
 
        if (makh.isEmpty() || hoten.isEmpty() || diemStr.isEmpty()) {
            showError("Vui lòng điền đầy đủ thông tin bắt buộc (*)");
            return;
        }
 
        if (hoten.length() > 100) {
            showError("Tên khách hàng không được vượt quá 100 ký tự!");
            return;
        }

        if (!sdt.isEmpty() && !sdt.matches("^0[0-9]{9,10}$")) {
            showError("Số điện thoại không đúng định dạng (phải bắt đầu bằng số 0 và có 10-11 số)!");
            return;
        }

        int diem;
        try {
            diem = Integer.parseInt(diemStr);
            if (diem < 0) {
                showError("Điểm tích lũy phải lớn hơn hoặc bằng 0");
                return;
            }
        } catch (NumberFormatException e) {
            showError("Điểm tích lũy phải là số nguyên");
            return;
        }
 
        try {
            model.KhachHang kh = new model.KhachHang(
                makh,
                hoten,
                sdt.isEmpty() ? null : sdt,
                diem
            );
            network.Request request = new network.Request(network.Module.KHACHHANG, network.Action.UPDATE, kh);
            network.Response response = network.SocketClient.getInstance().sendRequest(request);

            if (response != null && response.isSuccess()) {
                showSuccess("Cập nhật thông tin khách hàng thành công!");
                selectedCustomer = new CustomerController.CustomerModel(makh, hoten, sdt, diem);
            } else {
                showError(response != null ? response.getMessage() : "Cập nhật thất bại. Không nhận được phản hồi từ Server!");
            }
        } catch (Exception e) {
            showError("Lỗi: " + e.getMessage());
            e.printStackTrace();
        }
    }
 
    @FXML
    private void handleCancel(ActionEvent event) {
        if (selectedCustomer != null) {
            txtHoTen.setText(selectedCustomer.getTenkh());
            txtSDT.setText(selectedCustomer.getSdt());
            txtDiemTichLuy.setText(String.valueOf(selectedCustomer.getDiemtichluy()));
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
