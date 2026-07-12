package account;

import java.net.URL;
import java.util.ResourceBundle;
import java.util.List;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import network.Action;
import network.Module;
import network.Request;
import network.Response;
import network.SocketClient;
import javafx.application.Platform;

public class AccountAddController implements Initializable {

    @FXML private ComboBox<String> cbMaNV;
    @FXML private TextField txtUsername;
    @FXML private ComboBox<String> cbQuyen;
    @FXML private PasswordField txtPassword;
    @FXML private PasswordField txtConfirmPassword;
    @FXML private Label lblMessage;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        cbQuyen.setItems(FXCollections.observableArrayList("Admin", "Quản lý", "Nhân viên"));
        loadEmployeeAndAccountData();
    }

    private void loadEmployeeAndAccountData() {
        cbMaNV.setDisable(true);
        new Thread(() -> {
            try {
                // Request 1: Get all employees
                Request reqEmp = new Request(Module.NHANVIEN, Action.GET_ALL, null);
                Response resEmp = SocketClient.getInstance().sendRequest(reqEmp);

                // Request 2: Get all existing accounts
                Request reqAcc = new Request(Module.ACCOUNT, Action.GET_ALL, null);
                Response resAcc = SocketClient.getInstance().sendRequest(reqAcc);

                Platform.runLater(() -> {
                    try {
                        if (resEmp == null || !resEmp.isSuccess()) {
                            showError("Không thể tải danh sách nhân viên: " + 
                                (resEmp != null ? resEmp.getMessage() : "Không phản hồi"));
                            cbMaNV.setDisable(false);
                            return;
                        }

                        // Direct cast like working modules (ShiftAddController pattern)
                        @SuppressWarnings("unchecked")
                        List<model.NhanVien> employees = (List<model.NhanVien>) resEmp.getData();

                        // Build set of employee IDs that already have accounts
                        java.util.Set<String> existingAccEmpIds = new java.util.HashSet<>();
                        if (resAcc != null && resAcc.isSuccess() && resAcc.getData() != null) {
                            @SuppressWarnings("unchecked")
                            List<model.Account> accounts = (List<model.Account>) resAcc.getData();
                            for (model.Account acc : accounts) {
                                existingAccEmpIds.add(acc.getMaNV());
                            }
                        }

                        // Populate dropdown with employees who don't yet have an account
                        javafx.collections.ObservableList<String> empList = FXCollections.observableArrayList();
                        if (employees != null) {
                            for (model.NhanVien emp : employees) {
                                if (!existingAccEmpIds.contains(emp.getMaNV())) {
                                    empList.add(emp.getMaNV() + " - " + emp.getHoTen());
                                }
                            }
                        }
                        cbMaNV.setItems(empList);
                        
                        if (empList.isEmpty()) {
                            showError("Tất cả nhân viên đều đã có tài khoản.");
                        }
                    } catch (Exception ex) {
                        ex.printStackTrace();
                        showError("Lỗi xử lý dữ liệu: " + ex.getMessage());
                    }
                    cbMaNV.setDisable(false);
                });
            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() -> {
                    showError("Lỗi kết nối Server: " + e.getMessage());
                    cbMaNV.setDisable(false);
                });
            }
        }).start();
    }

    @FXML
    private void handleSave(ActionEvent event) {
        String selectedEmp = cbMaNV.getValue();
        String username = txtUsername.getText().trim();
        String quyen = cbQuyen.getValue();
        String password = txtPassword.getText().trim();
        String confirmPassword = txtConfirmPassword.getText().trim();

        if (selectedEmp == null || username.isEmpty() || quyen == null || password.isEmpty()) {
            showError("Vui lòng nhập đầy đủ các trường bắt buộc (*)");
            return;
        }
        if (!password.equals(confirmPassword)) {
            showError("Mật khẩu xác nhận không khớp.");
            return;
        }

        String maNV = selectedEmp.split(" - ")[0];

        int quyenInt = switch (quyen) {
            case "Admin" -> 1;
            case "Quản lý" -> 2;
            case "Nhân viên" -> 3;
            default -> 3;
        };

        model.Account newAccount = new model.Account();
        newAccount.setMaNV(maNV);
        newAccount.setTenDN(username);
        newAccount.setPassword(password);
        newAccount.setQuyen(quyenInt);

        new Thread(() -> {
            try {
                Request checkReq = new Request(Module.ACCOUNT, Action.GET_ALL, null);
                Response checkRes = SocketClient.getInstance().sendRequest(checkReq);
                if (checkRes != null && checkRes.isSuccess() && checkRes.getData() != null) {
                    @SuppressWarnings("unchecked")
                    List<model.Account> allAccs = (List<model.Account>) checkRes.getData();
                    for (model.Account a : allAccs) {
                        if (a.getTenDN().equalsIgnoreCase(username)) {
                            Platform.runLater(() -> showError("Tên đăng nhập đã tồn tại trong hệ thống."));
                            return;
                        }
                        if (a.getMaNV().equalsIgnoreCase(maNV)) {
                            Platform.runLater(() -> showError("Nhân viên này đã có tài khoản."));
                            return;
                        }
                    }
                }

                Request req = new Request(Module.ACCOUNT, Action.ADD, newAccount);
                Response res = SocketClient.getInstance().sendRequest(req);

                Platform.runLater(() -> {
                    if (res != null && res.isSuccess()) {
                        showSuccess("Thêm tài khoản thành công!");
                        clearForm();
                        loadEmployeeAndAccountData();
                    } else {
                        showError("Lỗi từ Server: " + (res != null ? res.getMessage() : "Không phản hồi"));
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() -> showError("Lỗi: " + e.getMessage()));
            }
        }).start();
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
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/account.fxml"));
            Parent view = loader.load();
            AnchorPane parent = (AnchorPane) cbMaNV.getScene().lookup("#contentArea");
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
        cbMaNV.setValue(null);
        txtUsername.clear();
        cbQuyen.setValue(null);
        txtPassword.clear();
        txtConfirmPassword.clear();
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
