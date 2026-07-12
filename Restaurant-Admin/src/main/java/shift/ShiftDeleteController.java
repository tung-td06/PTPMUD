package shift;

import java.net.URL;
import java.util.ResourceBundle;
import java.util.List;
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

public class ShiftDeleteController implements Initializable {

    @FXML private TextField txtMaCa;
    @FXML private TextField txtNhanVien;
    @FXML private TextField txtTenCa;
    @FXML private TextField txtGioBatDau;
    @FXML private TextField txtGioKetThuc;
    @FXML private Label lblMessage;
    @FXML private Button deleteBtn;
    @FXML private Button cancelBtn;
    @FXML private Button backBtn;

    private ShiftController.ShiftModel selectedShift;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        selectedShift = ShiftController.selectedShift;
        if (selectedShift != null) {
            txtMaCa.setText(selectedShift.getMaca());
            txtTenCa.setText(selectedShift.getTenca());
            txtGioBatDau.setText(selectedShift.getGiobatdau());
            txtGioKetThuc.setText(selectedShift.getGioketthuc());
            txtNhanVien.setText(selectedShift.getManv() + " - Đang tải tên...");

            new Thread(() -> {
                try {
                    Request request = new Request(Module.NHANVIEN, Action.GET_ALL, null);
                    Response response = SocketClient.getInstance().sendRequest(request);
                    Platform.runLater(() -> {
                        if (response != null && response.isSuccess()) {
                            List<model.NhanVien> employees = (List<model.NhanVien>) response.getData();
                            String empName = selectedShift.getManv();
                            if (employees != null) {
                                for (model.NhanVien emp : employees) {
                                    if (emp.getMaNV().equals(selectedShift.getManv())) {
                                        empName = emp.getMaNV() + " - " + emp.getHoTen();
                                        break;
                                    }
                                }
                            }
                            txtNhanVien.setText(empName);
                        } else {
                            txtNhanVien.setText(selectedShift.getManv());
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                    Platform.runLater(() -> txtNhanVien.setText(selectedShift.getManv()));
                }
            }).start();
        }
    }

    @FXML
    private void handleDelete(ActionEvent event) {
        if (selectedShift == null) {
            showError("Không tìm thấy thông tin ca làm việc.");
            return;
        }

        javafx.scene.control.Alert confirmAlert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Xác nhận xóa");
        confirmAlert.setHeaderText(null);
        confirmAlert.setContentText("Bạn có chắc chắn muốn xóa ca làm việc này không?");
        java.util.Optional<javafx.scene.control.ButtonType> result = confirmAlert.showAndWait();

        if (result.isPresent() && result.get() == javafx.scene.control.ButtonType.OK) {
            setFormDisabled(true);
            showSuccess("Đang thực hiện xóa ca...");

            new Thread(() -> {
                try {
                    Request request = new Request(Module.CALAMVIEC, Action.DELETE, selectedShift.getMaca());
                    Response response = SocketClient.getInstance().sendRequest(request);
                    Platform.runLater(() -> {
                        if (response != null && response.isSuccess()) {
                            showSuccess("Xóa phân ca làm việc thành công!");
                            setFormDisabled(false);
                            if (deleteBtn != null) deleteBtn.setDisable(true);
                        } else {
                            showError(response != null ? response.getMessage() : "Server từ chối xóa ca làm việc!");
                            setFormDisabled(false);
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                    Platform.runLater(() -> {
                        showError("Lỗi kết nối Server: " + e.getMessage());
                        setFormDisabled(false);
                    });
                }
            }).start();
        }
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
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/shift.fxml"));
            Parent view = loader.load();
            AnchorPane parent = (AnchorPane) txtMaCa.getScene().lookup("#contentArea");
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

    private void setFormDisabled(boolean disabled) {
        if (deleteBtn != null) deleteBtn.setDisable(disabled);
        if (cancelBtn != null) cancelBtn.setDisable(disabled);
        if (backBtn != null) backBtn.setDisable(disabled);
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
