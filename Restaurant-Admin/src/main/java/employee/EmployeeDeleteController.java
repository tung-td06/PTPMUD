package employee;

import restaurant.MockDataStore;
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
import network.Action;
import network.Module;
import network.Request;
import network.Response;
import network.SocketClient;
import javafx.application.Platform;

public class EmployeeDeleteController implements Initializable {

    @FXML private TextField txtMaNV;
    @FXML private TextField txtHoTen;
    @FXML private TextField txtSDT;
    @FXML private TextField txtChucVu;
    @FXML private Label lblMessage;
    @FXML private Button deleteBtn;

    private EmployeeController.EmployeeModel selectedEmployee;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        selectedEmployee = EmployeeController.selectedEmployee;
        if (selectedEmployee != null) {
            txtMaNV.setText(selectedEmployee.getId());
            txtHoTen.setText(selectedEmployee.getName());
            txtSDT.setText(selectedEmployee.getPhone());
            txtChucVu.setText(selectedEmployee.getRole());
        }
    }

    @FXML
    private void handleDelete(ActionEvent event) {
        if (selectedEmployee == null) {
            showError("Không tìm thấy thông tin nhân viên.");
            return;
        }

        deleteBtn.setDisable(true);
        showSuccess("Đang thực hiện xóa nhân viên...");

        new Thread(() -> {
            try {
                Request deleteReq = new Request(Module.NHANVIEN, Action.DELETE, selectedEmployee.getId());
                Response deleteRes = SocketClient.getInstance().sendRequest(deleteReq);

                Platform.runLater(() -> {
                    if (deleteRes != null && deleteRes.isSuccess()) {
                        int index = -1;
                        for (int i = 0; i < MockDataStore.employees.size(); i++) {
                            if (MockDataStore.employees.get(i).getId().equals(selectedEmployee.getId())) {
                                index = i;
                                break;
                            }
                        }
                        if (index != -1) {
                            MockDataStore.employees.remove(index);
                        }

                        showSuccess("Xóa nhân viên thành công!");
                    } else {
                        showError(deleteRes != null ? deleteRes.getMessage() : "Server từ chối xóa nhân viên.");
                        deleteBtn.setDisable(false);
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() -> {
                    showError("Lỗi kết nối Server: " + e.getMessage());
                    deleteBtn.setDisable(false);
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
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/employee.fxml"));
            Parent view = loader.load();
            AnchorPane parent = (AnchorPane) txtMaNV.getScene().lookup("#contentArea");
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
