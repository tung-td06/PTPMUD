package employee;

import restaurant.MockDataStore;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.Button;
import javafx.scene.layout.AnchorPane;
import network.Action;
import network.Module;
import network.Request;
import network.Response;
import network.SocketClient;
import javafx.application.Platform;

public class EmployeeEditController implements Initializable {

    @FXML
    private TextField txtMaNV;
    @FXML
    private TextField txtHoTen;
    @FXML
    private DatePicker dpNgaySinh;
    @FXML
    private TextField txtQue;
    @FXML
    private TextField txtGmail;
    @FXML
    private TextField txtSDT;
    @FXML
    private ComboBox<String> cbChucVu;
    @FXML
    private ComboBox<String> cbTrangThai;
    @FXML
    private TextArea txtNote;
    @FXML
    private Label lblMessage;
    @FXML
    private Button saveBtn;
    @FXML
    private Button cancelBtn;
    @FXML
    private Button backBtn;

    private EmployeeController.EmployeeModel selectedEmployee;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        selectedEmployee = EmployeeController.selectedEmployee;

        cbChucVu.setItems(FXCollections.observableArrayList(
                "Quản lý", "Thu ngân", "Phục vụ", "Đầu bếp", "Bảo vệ", "Lễ tân"));
        cbTrangThai.setItems(FXCollections.observableArrayList(
                "Đang làm", "Nghỉ phép", "Nghỉ việc"));

        populateForm();
    }

    private void populateForm() {
        if (selectedEmployee != null) {
            txtMaNV.setText(selectedEmployee.getId());
            txtHoTen.setText(selectedEmployee.getName());
            txtSDT.setText(selectedEmployee.getPhone());
            cbChucVu.setValue(selectedEmployee.getRole());

            if (selectedEmployee.getNgaySinh() != null) {
                dpNgaySinh.setValue(selectedEmployee.getNgaySinh().toLocalDate());
            } else {
                dpNgaySinh.setValue(null);
            }

            txtQue.setText(selectedEmployee.getQue() != null ? selectedEmployee.getQue() : "");
            txtGmail.setText(selectedEmployee.getGmail() != null ? selectedEmployee.getGmail() : "");
            txtNote.setText(selectedEmployee.getNote() != null ? selectedEmployee.getNote() : "");

            String status = selectedEmployee.getStatus();
            if (status.equalsIgnoreCase("Đang làm việc") || status.equalsIgnoreCase("Đang làm")) {
                cbTrangThai.setValue("Đang làm");
            } else if (status.equalsIgnoreCase("Nghỉ phép")) {
                cbTrangThai.setValue("Nghỉ phép");
            } else {
                cbTrangThai.setValue("Nghỉ việc");
            }
        }
    }

    private void setFormDisabled(boolean disabled) {
        txtHoTen.setDisable(disabled);
        dpNgaySinh.setDisable(disabled);
        txtQue.setDisable(disabled);
        txtGmail.setDisable(disabled);
        txtSDT.setDisable(disabled);
        cbChucVu.setDisable(disabled);
        cbTrangThai.setDisable(disabled);
        txtNote.setDisable(disabled);
        if (saveBtn != null) saveBtn.setDisable(disabled);
        if (cancelBtn != null) cancelBtn.setDisable(disabled);
        if (backBtn != null) backBtn.setDisable(disabled);
    }

    @FXML
    private void handleSave(ActionEvent event) {
        String manv = txtMaNV.getText().trim();
        String hoten = txtHoTen.getText().trim();
        String sdt = txtSDT.getText().trim();
        String chucvu = cbChucVu.getValue();
        String trangthai = cbTrangThai.getValue();
        java.time.LocalDate ngaySinhLocalDate = dpNgaySinh.getValue();
        String que = txtQue.getText().trim();
        String gmail = txtGmail.getText().trim();
        String note = txtNote.getText().trim();

        // 1. Basic empty validations
        if (manv.isEmpty()) {
            showError("Mã nhân viên không được để trống.");
            return;
        }
        if (manv.length() > 15) {
            showError("Mã nhân viên không được vượt quá 15 ký tự.");
            return;
        }
        if (hoten.isEmpty()) {
            showError("Họ tên không được để trống.");
            return;
        }
        if (hoten.length() > 50) {
            showError("Họ tên không được vượt quá 50 ký tự.");
            return;
        }
        if (sdt.isEmpty()) {
            showError("Số điện thoại không được để trống.");
            return;
        }
        if (sdt.length() > 15) {
            showError("Số điện thoại không được vượt quá 15 ký tự.");
            return;
        }
        if (chucvu == null || chucvu.isEmpty()) {
            showError("Vui lòng chọn chức vụ.");
            return;
        }
        if (chucvu.length() > 30) {
            showError("Chức vụ không được vượt quá 30 ký tự.");
            return;
        }
        if (trangthai == null || trangthai.isEmpty()) {
            showError("Vui lòng chọn trạng thái.");
            return;
        }
        if (trangthai.length() > 30) {
            showError("Trạng thái không được vượt quá 30 ký tự.");
            return;
        }
        if (que.length() > 50) {
            showError("Quê quán không được vượt quá 50 ký tự.");
            return;
        }
        if (gmail.length() > 100) {
            showError("Gmail không được vượt quá 100 ký tự.");
            return;
        }
        if (note.length() > 200) {
            showError("Ghi chú không được vượt quá 200 ký tự.");
            return;
        }

        // 2. Format validations
        if (!sdt.matches("\\d+")) {
            showError("Số điện thoại chỉ được chứa số.");
            return;
        }
        if (sdt.length() < 10 || sdt.length() > 11) {
            showError("Số điện thoại phải dài từ 10 đến 11 số.");
            return;
        }
        if (!gmail.isEmpty() && !gmail.matches(".+@.+\\..+")) {
            showError("Gmail không đúng định dạng (VD: name@domain.com).");
            return;
        }

        setFormDisabled(true);
        showSuccess("Đang cập nhật thông tin...");

        new Thread(() -> {
            try {
                java.sql.Date sqlDate = (ngaySinhLocalDate != null) ? java.sql.Date.valueOf(ngaySinhLocalDate) : null;
                model.NhanVien nv = new model.NhanVien(
                    manv,
                    hoten,
                    sqlDate,
                    que.isEmpty() ? null : que,
                    gmail.isEmpty() ? null : gmail,
                    sdt,
                    chucvu,
                    trangthai,
                    note.isEmpty() ? null : note
                );

                Request updateReq = new Request(Module.NHANVIEN, Action.UPDATE, nv);
                Response updateRes = SocketClient.getInstance().sendRequest(updateReq);

                Platform.runLater(() -> {
                    if (updateRes != null && updateRes.isSuccess()) {
                        EmployeeController.EmployeeModel updatedEmp = new EmployeeController.EmployeeModel(
                            manv,
                            hoten,
                            chucvu,
                            sdt,
                            selectedEmployee != null ? selectedEmployee.getShift() : "Chưa phân ca",
                            trangthai,
                            sqlDate,
                            que.isEmpty() ? null : que,
                            gmail.isEmpty() ? null : gmail,
                            note.isEmpty() ? null : note
                        );

                        int index = -1;
                        for (int i = 0; i < MockDataStore.employees.size(); i++) {
                            if (MockDataStore.employees.get(i).getId().equals(manv)) {
                                index = i;
                                break;
                            }
                        }
                        if (index != -1) {
                            MockDataStore.employees.set(index, updatedEmp);
                        }

                        showSuccess("Cập nhật nhân viên thành công!");
                    } else {
                        showError(updateRes != null ? updateRes.getMessage() : "Server từ chối cập nhật.");
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
        populateForm();
        lblMessage.setText("");
        lblMessage.getStyleClass().removeAll("message-error", "message-success");
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
