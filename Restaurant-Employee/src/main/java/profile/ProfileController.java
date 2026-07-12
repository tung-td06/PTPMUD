package profile;

import Login.LoginController;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import model.Account;
import model.NhanVien;
import network.Action;
import network.Module;
import network.Request;
import network.Response;
import network.SocketClient;

public class ProfileController implements Initializable {

    @FXML private Label lblAvatarInitials;
    @FXML private Label lblEmpNameLarge;
    @FXML private Label lblEmpRoleLarge;
    @FXML private Label lblStatusTag;
    @FXML private Label lblMaNV;
    @FXML private Label lblHoTen;
    @FXML private Label lblNgaySinh;
    @FXML private Label lblSdt;
    @FXML private Label lblEmail;
    @FXML private Label lblQueQuan;
    @FXML private Label lblChucVu;
    @FXML private Label lblUsername;

    @FXML private PasswordField txtOldPassword;
    @FXML private PasswordField txtNewPassword;
    @FXML private PasswordField txtConfirmPassword;
    @FXML private Label lblMessage;

    private Account currentAccount;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        currentAccount = LoginController.loggedInAccount;
        if (currentAccount != null) {
            lblUsername.setText(currentAccount.getTenDN());
            lblMaNV.setText(currentAccount.getMaNV());
            lblEmpNameLarge.setText(currentAccount.getTenDN());
            setInitials(currentAccount.getTenDN());
            
            String roleStr = switch (currentAccount.getQuyen()) {
                case 1 -> "Quản trị viên";
                case 2 -> "Quản lý";
                case 3 -> "Nhân viên";
                default -> "Không xác định";
            };
            lblEmpRoleLarge.setText(roleStr);
            lblChucVu.setText(roleStr);
            lblStatusTag.getStyleClass().add("status-active");

            // Fetch employee details from Server
            loadEmployeeDetails();
        }
    }

    private void setInitials(String name) {
        if (name == null || name.trim().isEmpty()) {
            lblAvatarInitials.setText("-");
            return;
        }
        String[] words = name.trim().split("\\s+");
        String initial = words[words.length - 1].substring(0, 1).toUpperCase();
        lblAvatarInitials.setText(initial);
    }

    private void loadEmployeeDetails() {
        new Thread(() -> {
            try {
                Request req = new Request(Module.NHANVIEN, Action.GET_ALL, null);
                Response res = SocketClient.getInstance().sendRequest(req);
                if (res != null && res.isSuccess()) {
                    List<NhanVien> employees = (List<NhanVien>) res.getData();
                    if (employees != null) {
                        for (NhanVien nv : employees) {
                            if (nv.getMaNV() != null && nv.getMaNV().equalsIgnoreCase(currentAccount.getMaNV())) {
                                Platform.runLater(() -> {
                                    lblHoTen.setText(nv.getHoTen());
                                    lblEmpNameLarge.setText(nv.getHoTen());
                                    setInitials(nv.getHoTen());
                                    lblNgaySinh.setText(nv.getNgaySinh() != null ? nv.getNgaySinh().toString() : "-");
                                    lblSdt.setText(nv.getSdt() != null ? nv.getSdt() : "-");
                                    lblEmail.setText(nv.getGmail() != null ? nv.getGmail() : "-");
                                    lblQueQuan.setText(nv.getQue() != null ? nv.getQue() : "-");
                                    
                                    String status = nv.getTrangThai() != null ? nv.getTrangThai() : "Đang làm việc";
                                    lblStatusTag.setText(status);
                                    lblStatusTag.getStyleClass().removeAll("status-active", "status-pending");
                                    if ("Nghỉ việc".equalsIgnoreCase(status) || "Nghỉ".equalsIgnoreCase(status)) {
                                        lblStatusTag.getStyleClass().add("status-pending");
                                    } else {
                                        lblStatusTag.getStyleClass().add("status-active");
                                    }
                                });
                                break;
                            }
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    @FXML
    private void handleChangePassword(ActionEvent event) {
        String oldPwd = txtOldPassword.getText().trim();
        String newPwd = txtNewPassword.getText().trim();
        String confirmPwd = txtConfirmPassword.getText().trim();

        if (oldPwd.isEmpty() || newPwd.isEmpty() || confirmPwd.isEmpty()) {
            showError("Vui lòng nhập đầy đủ các trường!");
            return;
        }

        if (!oldPwd.equals(currentAccount.getPassword())) {
            showError("Mật khẩu cũ không chính xác!");
            return;
        }

        if (newPwd.length() < 4) {
            showError("Mật khẩu mới phải từ 4 ký tự trở lên!");
            return;
        }

        if (!newPwd.equals(confirmPwd)) {
            showError("Mật khẩu xác nhận không trùng khớp!");
            return;
        }

        Account updatedAccount = new Account();
        updatedAccount.setMaNV(currentAccount.getMaNV());
        updatedAccount.setTenDN(currentAccount.getTenDN());
        updatedAccount.setPassword(newPwd);
        updatedAccount.setQuyen(currentAccount.getQuyen());

        new Thread(() -> {
            try {
                Request req = new Request(Module.ACCOUNT, Action.UPDATE, updatedAccount);
                Response res = SocketClient.getInstance().sendRequest(req);
                Platform.runLater(() -> {
                    if (res != null && res.isSuccess()) {
                        currentAccount.setPassword(newPwd);
                        showSuccess("Đổi mật khẩu thành công!");
                        txtOldPassword.clear();
                        txtNewPassword.clear();
                        txtConfirmPassword.clear();
                    } else {
                        showError(res != null ? res.getMessage() : "Lỗi không xác định từ Server!");
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() -> showError("Lỗi: " + e.getMessage()));
            }
        }).start();
    }

    @FXML
    private void handleLogout(ActionEvent event) {
        try {
            javafx.stage.Stage stage = (javafx.stage.Stage) lblEmpNameLarge.getScene().getWindow();

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/login.fxml"));
            javafx.scene.Parent root = loader.load();

            stage.getScene().setRoot(root);
            stage.setResizable(true);
            stage.setMinWidth(1000);
            stage.setMinHeight(600);
            stage.setWidth(1400);
            stage.setHeight(800);
            stage.centerOnScreen();
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
