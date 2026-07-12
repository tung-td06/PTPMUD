package customer.controller.profile;

import customer.controller.component.Dialogs;
import customer.controller.util.CustomerSession;
import dashboard.DashboardController;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import model.Account;
import model.KhachHang;
import network.Action;
import network.Module;
import network.Request;
import network.Response;
import network.SocketClient;
import java.net.URL;
import java.util.ResourceBundle;

public class CustomerProfileController implements Initializable {

    @FXML
    private Label lblSummaryName;

    @FXML
    private Label lblSummaryRole;

    @FXML
    private Label lblSummaryPoints;

    @FXML
    private TextField txtUsername;

    @FXML
    private TextField txtPhone;

    @FXML
    private TextField txtName;

    @FXML
    private PasswordField txtOldPassword;

    @FXML
    private PasswordField txtNewPassword;

    @FXML
    private PasswordField txtConfirmPassword;

    @FXML
    private Button btnLogout;

    @FXML
    private Button btnSaveProfile;

    @FXML
    private Button btnChangePassword;

    private KhachHang currentCustomer;
    private Account loggedInAccount;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        currentCustomer = CustomerSession.getCurrentCustomer();
        loggedInAccount = CustomerSession.getLoggedInAccount();

        if (currentCustomer == null || loggedInAccount == null) {
            return;
        }

        // Bind profile data
        lblSummaryName.setText(currentCustomer.getTenKH());
        lblSummaryPoints.setText(currentCustomer.getDiemTichLuy() + " điểm");
        txtUsername.setText(loggedInAccount.getTenDN());
        txtPhone.setText(currentCustomer.getSdt());
        txtName.setText(currentCustomer.getTenKH());
    }

    @FXML
    private void handleSaveProfile() {
        String name = txtName.getText().trim();
        String phone = txtPhone.getText().trim();

        if (name.isEmpty() || phone.isEmpty()) {
            Dialogs.showError("Lỗi cập nhật", "Họ tên và số điện thoại không được để trống!");
            return;
        }

        currentCustomer.setTenKH(name);
        currentCustomer.setSdt(phone);

        btnSaveProfile.setDisable(true);

        new Thread(() -> {
            try {
                Request req = new Request(Module.KHACHHANG, Action.UPDATE, currentCustomer);
                Response res = SocketClient.getInstance().sendRequest(req);

                Platform.runLater(() -> {
                    if (res != null && res.isSuccess()) {
                        Dialogs.showSuccess("Cập nhật thành công", "Thông tin cá nhân đã được lưu thành công!");
                        lblSummaryName.setText(name);
                        
                        // Update topbar profile label if exists
                        if (DashboardController.getInstance() != null) {
                            Label lblProfileName = (Label) DashboardController.getInstance().getScene().lookup("#lblProfileName");
                            if (lblProfileName != null) {
                                lblProfileName.setText(name);
                            }
                        }
                    } else {
                        Dialogs.showError("Lỗi cập nhật", res != null ? res.getMessage() : "Không thể lưu thông tin lúc này.");
                    }
                    btnSaveProfile.setDisable(false);
                });

            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() -> {
                    Dialogs.showError("Lỗi cập nhật", "Lỗi gửi yêu cầu: " + e.getMessage());
                    btnSaveProfile.setDisable(false);
                });
            }
        }).start();
    }

    @FXML
    private void handleChangePassword() {
        String oldPwd = txtOldPassword.getText().trim();
        String newPwd = txtNewPassword.getText().trim();
        String confirmPwd = txtConfirmPassword.getText().trim();

        if (oldPwd.isEmpty() || newPwd.isEmpty() || confirmPwd.isEmpty()) {
            Dialogs.showError("Đổi mật khẩu", "Vui lòng điền đầy đủ các thông tin mật khẩu!");
            return;
        }

        if (!newPwd.equals(confirmPwd)) {
            Dialogs.showError("Đổi mật khẩu", "Mật khẩu mới và mật khẩu xác nhận không trùng khớp!");
            return;
        }

        if (newPwd.length() < 6) {
            Dialogs.showError("Đổi mật khẩu", "Mật khẩu mới phải có ít nhất 6 ký tự!");
            return;
        }

        btnChangePassword.setDisable(true);

        // Gửi yêu cầu đổi mật khẩu lên server kèm cả mật khẩu cũ để server tự xác minh.
        // Không so sánh plain text tại client vì server có thể lưu mật khẩu dưới dạng hash.
        java.util.List<String> payload = new java.util.ArrayList<>();
        payload.add(loggedInAccount.getTenDN()); // tên đăng nhập
        payload.add(oldPwd);                      // mật khẩu cũ để server verify
        payload.add(newPwd);                      // mật khẩu mới

        new Thread(() -> {
            try {
                Request req = new Request(Module.ACCOUNT, network.Command.CHANGE_PASSWORD, payload);
                Response res = SocketClient.getInstance().sendRequest(req);

                Platform.runLater(() -> {
                    if (res != null && res.isSuccess()) {
                        // Cập nhật object local để các thao tác tiếp theo nhất quán
                        loggedInAccount.setPassword(newPwd);
                        Dialogs.showSuccess("Đổi mật khẩu thành công", "Mật khẩu tài khoản đã được thay đổi thành công!");
                        txtOldPassword.clear();
                        txtNewPassword.clear();
                        txtConfirmPassword.clear();
                    } else {
                        String msg = res != null ? res.getMessage() : "Không thể thay đổi mật khẩu lúc này.";
                        Dialogs.showError("Lỗi đổi mật khẩu", msg);
                    }
                    btnChangePassword.setDisable(false);
                });

            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() -> {
                    Dialogs.showError("Lỗi đổi mật khẩu", "Lỗi gửi yêu cầu: " + e.getMessage());
                    btnChangePassword.setDisable(false);
                });
            }
        }).start();
    }

    @FXML
    private void handleLogout() {
        if (Dialogs.showConfirm("Đăng xuất", "Quý khách có chắc chắn muốn đăng xuất khỏi hệ thống chứ?")) {
            // Find logout button in dashboard.fxml sidebar and trigger its action
            javafx.scene.Scene scene = null;
            if (btnLogout != null && btnLogout.getScene() != null) {
                scene = btnLogout.getScene();
            } else if (btnSaveProfile != null && btnSaveProfile.getScene() != null) {
                scene = btnSaveProfile.getScene();
            }
            if (scene != null) {
                Button dashboardLogout = (Button) scene.lookup("#logoutBtn");
                if (dashboardLogout != null) {
                    Platform.runLater(dashboardLogout::fire);
                } else {
                    // Fallback to exit
                    Platform.exit();
                }
            }
        }
    }
}
