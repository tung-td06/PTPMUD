package shift;

import java.net.URL;
import java.util.ResourceBundle;
import java.util.List;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import network.Action;
import network.Module;
import network.Request;
import network.Response;
import network.SocketClient;

public class ShiftEditController implements Initializable {

    @FXML private TextField txtMaCa;
    @FXML private ComboBox<String> cbNhanVien;
    @FXML private TextField txtTenCa;
    @FXML private TextField txtGioBatDau;
    @FXML private TextField txtGioKetThuc;
    @FXML private Label lblMessage;
    @FXML private Button saveBtn;
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
        }

        loadEmployees();
    }

    private void loadEmployees() {
        cbNhanVien.setDisable(true);
        new Thread(() -> {
            try {
                Request request = new Request(Module.NHANVIEN, Action.GET_ALL, null);
                Response response = SocketClient.getInstance().sendRequest(request);
                Platform.runLater(() -> {
                    if (response != null && response.isSuccess()) {
                        List<model.NhanVien> employees = (List<model.NhanVien>) response.getData();
                        ObservableList<String> empIds = FXCollections.observableArrayList();
                        String selectedEmpString = null;
                        if (employees != null) {
                            for (model.NhanVien emp : employees) {
                                String empStr = emp.getMaNV() + " - " + emp.getHoTen();
                                empIds.add(empStr);
                                if (selectedShift != null && emp.getMaNV().equals(selectedShift.getManv())) {
                                    selectedEmpString = empStr;
                                }
                            }
                        }
                        cbNhanVien.setItems(empIds);
                        if (selectedEmpString != null) {
                            cbNhanVien.setValue(selectedEmpString);
                        }
                    } else {
                        showError("Không thể tải danh sách nhân viên!");
                    }
                    cbNhanVien.setDisable(false);
                });
            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() -> {
                    showError("Lỗi kết nối khi tải nhân viên: " + e.getMessage());
                    cbNhanVien.setDisable(false);
                });
            }
        }).start();
    }

    @FXML
    private void handleSave(ActionEvent event) {
        String maca = txtMaCa.getText().trim();
        String selectedEmp = cbNhanVien.getValue();
        String tenca = txtTenCa.getText().trim();
        String giobatdau = txtGioBatDau.getText().trim();
        String gioketthuc = txtGioKetThuc.getText().trim();

        if (maca.isEmpty() || selectedEmp == null || tenca.isEmpty() || giobatdau.isEmpty() || gioketthuc.isEmpty()) {
            showError("Vui lòng nhập đầy đủ các trường bắt buộc (*)");
            return;
        }

        // Validate format
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        sdf.setLenient(false);
        java.sql.Timestamp startTs;
        java.sql.Timestamp endTs;
        try {
            startTs = new java.sql.Timestamp(sdf.parse(giobatdau).getTime());
        } catch (Exception e) {
            showError("Thời gian bắt đầu sai định dạng (yyyy-MM-dd HH:mm:ss)!");
            return;
        }

        try {
            endTs = new java.sql.Timestamp(sdf.parse(gioketthuc).getTime());
        } catch (Exception e) {
            showError("Thời gian kết thúc sai định dạng (yyyy-MM-dd HH:mm:ss)!");
            return;
        }

        if (startTs.after(endTs)) {
            showError("Thời gian bắt đầu phải trước hoặc bằng thời gian kết thúc!");
            return;
        }

        String manv = selectedEmp.split(" - ")[0];

        setFormDisabled(true);
        showSuccess("Đang cập nhật lịch phân ca...");

        new Thread(() -> {
            try {
                model.CaLamViec updatedCa = new model.CaLamViec(maca, manv, tenca, startTs, endTs);
                Request request = new Request(Module.CALAMVIEC, Action.UPDATE, updatedCa);
                Response response = SocketClient.getInstance().sendRequest(request);

                Platform.runLater(() -> {
                    if (response != null && response.isSuccess()) {
                        showSuccess("Cập nhật ca làm việc thành công!");
                        // update selected shift so that canceling reflects new values
                        selectedShift = new ShiftController.ShiftModel(maca, manv, tenca, giobatdau, gioketthuc);
                        ShiftController.selectedShift = selectedShift;
                    } else {
                        showError(response != null ? response.getMessage() : "Lỗi: Server từ chối cập nhật ca!");
                    }
                    setFormDisabled(false);
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

    @FXML
    private void handleCancel(ActionEvent event) {
        if (selectedShift != null) {
            txtTenCa.setText(selectedShift.getTenca());
            txtGioBatDau.setText(selectedShift.getGiobatdau());
            txtGioKetThuc.setText(selectedShift.getGioketthuc());
            for (String item : cbNhanVien.getItems()) {
                if (item.startsWith(selectedShift.getManv() + " - ")) {
                    cbNhanVien.setValue(item);
                    break;
                }
            }
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
        txtMaCa.setDisable(disabled);
        cbNhanVien.setDisable(disabled);
        txtTenCa.setDisable(disabled);
        txtGioBatDau.setDisable(disabled);
        txtGioKetThuc.setDisable(disabled);
        if (saveBtn != null) saveBtn.setDisable(disabled);
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
