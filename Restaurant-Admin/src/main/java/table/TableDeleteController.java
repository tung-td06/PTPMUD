package table;
 
import java.net.URL;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.application.Platform;

import network.Action;
import network.Module;
import network.Request;
import network.Response;
import network.SocketClient;
 
public class TableDeleteController implements Initializable {
 
    @FXML private TextField txtMaBan;
    @FXML private TextField txtTenBan;
    @FXML private TextField txtKhuVuc;
    @FXML private TextField txtTrangThai;
    @FXML private Label lblMessage;
    @FXML private Button deleteBtn;
    @FXML private Button cancelBtn;
    @FXML private Button backBtn;
 
    private TableController.TableModel selectedTable;
 
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        selectedTable = TableController.selectedTable;
        if (selectedTable != null) {
            txtMaBan.setText(selectedTable.getMaban());
            txtTenBan.setText(selectedTable.getTenban());
            txtKhuVuc.setText(selectedTable.getKhuvuc());
            txtTrangThai.setText(selectedTable.getTrangthai());
        }
    }
 
    @FXML
    private void handleDelete(ActionEvent event) {
        if (selectedTable == null) {
            showError("Không tìm thấy thông tin bàn ăn.");
            return;
        }
 
        setFormDisable(true);
        new Thread(() -> {
            try {
                Request request = new Request(Module.BANAN, Action.DELETE, selectedTable.getMaban());
                Response response = SocketClient.getInstance().sendRequest(request);
                Platform.runLater(() -> {
                    if (response != null && response.isSuccess()) {
                        TableController.selectedTable = null;
                        showSuccess("Xóa bàn ăn thành công!");
                        deleteBtn.setDisable(true);
                    } else {
                        showError(response != null ? response.getMessage() : "Lỗi xóa bàn ăn hoặc lỗi Server!");
                        setFormDisable(false);
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() -> {
                    showError("Lỗi kết nối Server: " + e.getMessage());
                    setFormDisable(false);
                });
            }
        }).start();
    }

    private void setFormDisable(boolean disable) {
        if (deleteBtn != null) deleteBtn.setDisable(disable);
        if (cancelBtn != null) cancelBtn.setDisable(disable);
        if (backBtn != null) backBtn.setDisable(disable);
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
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/table.fxml"));
            Parent view = loader.load();
            AnchorPane parent = (AnchorPane) txtMaBan.getScene().lookup("#contentArea");
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
