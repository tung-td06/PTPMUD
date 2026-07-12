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
 
public class WarehouseEditController implements Initializable {
 
    @FXML private TextField txtMaNL;
    @FXML private TextField txtTenNL;
    @FXML private TextField txtDonVi;
    @FXML private TextField txtSoLuong;
    @FXML private Label lblMessage;
 
    private WarehouseController.WarehouseModel selectedItem;
 
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        selectedItem = WarehouseController.selectedItem;
        if (selectedItem != null) {
            txtMaNL.setText(selectedItem.getId());
            txtTenNL.setText(selectedItem.getName());
            txtDonVi.setText(selectedItem.getUnit());
            txtSoLuong.setText(String.valueOf((int) selectedItem.getQuantity()));
        }
    }
 
    @FXML
    private void handleSave(ActionEvent event) {
        String manl = txtMaNL.getText().trim();
        String tennl = txtTenNL.getText().trim();
        String donvi = txtDonVi.getText().trim();
        String soluongStr = txtSoLuong.getText().trim();
 
        if (manl.isEmpty() || tennl.isEmpty() || donvi.isEmpty() || soluongStr.isEmpty()) {
            showError("Vui lòng điền đầy đủ các trường bắt buộc (*)");
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
            NguyenLieu nl = new NguyenLieu(manl, tennl, donvi, qtyVal);
            Request request = new Request(Module.NGUYENLIEU, Action.UPDATE, nl);
            Response response = SocketClient.getInstance().sendRequest(request);
 
            if (response != null && response.isSuccess()) {
                showSuccess("Cập nhật thông tin kho thành công!");
                // Sync the selected item state
                WarehouseController.selectedItem = new WarehouseController.WarehouseModel(
                    manl,
                    tennl,
                    donvi,
                    qtyVal,
                    selectedItem != null ? selectedItem.getMinQuantity() : 10.0,
                    qtyVal >= (selectedItem != null ? selectedItem.getMinQuantity() : 10.0) ? "Đủ hàng" : (qtyVal > 0 ? "Sắp hết" : "Hết hàng")
                );
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
        if (selectedItem != null) {
            txtTenNL.setText(selectedItem.getName());
            txtDonVi.setText(selectedItem.getUnit());
            txtSoLuong.setText(String.valueOf((int) selectedItem.getQuantity()));
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
