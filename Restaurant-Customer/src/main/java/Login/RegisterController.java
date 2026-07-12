package Login;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import model.AccountKH;
import model.KhachHang;
import network.Action;
import network.Module;
import network.Request;
import network.Response;
import network.SocketClient;

public class RegisterController implements Initializable {

    @FXML
    private TextField txtFullName;

    @FXML
    private TextField txtPhone;

    @FXML
    private TextField txtUsername;

    @FXML
    private PasswordField txtPassword;

    @FXML
    private PasswordField txtRetypePassword;

    @FXML
    private Button btnRegister;

    @FXML
    private Label lblMessage;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        lblMessage.setText("");
        lblMessage.visibleProperty().bind(lblMessage.textProperty().isNotEmpty());
        lblMessage.managedProperty().bind(lblMessage.visibleProperty());

        txtFullName.textProperty().addListener((o, ov, nv) -> lblMessage.setText(""));
        txtPhone.textProperty().addListener((o, ov, nv) -> lblMessage.setText(""));
        txtUsername.textProperty().addListener((o, ov, nv) -> lblMessage.setText(""));
        txtPassword.textProperty().addListener((o, ov, nv) -> lblMessage.setText(""));
        txtRetypePassword.textProperty().addListener((o, ov, nv) -> lblMessage.setText(""));
    }

    @FXML
    private void handleRegister(ActionEvent event) {
        String fullName = txtFullName.getText().trim();
        String phone = txtPhone.getText().trim();
        String username = txtUsername.getText().trim();
        String password = txtPassword.getText().trim();
        String retypePassword = txtRetypePassword.getText().trim();

        // 1. Validation
        if (fullName.isEmpty() || phone.isEmpty() || username.isEmpty() || password.isEmpty() || retypePassword.isEmpty()) {
            showError("Vui lòng điền đầy đủ các thông tin bắt buộc (*)");
            return;
        }

        if (!phone.matches("^0[0-9]{9,10}$")) {
            showError("Số điện thoại không hợp lệ (phải bắt đầu bằng số 0, gồm 10-11 chữ số)!");
            return;
        }

        if (username.contains(" ")) {
            showError("Tên đăng nhập không được chứa khoảng trắng!");
            return;
        }

        if (username.length() > 30) {
            showError("Tên đăng nhập không được vượt quá 30 ký tự!");
            return;
        }

        if (!password.equals(retypePassword)) {
            showError("Mật khẩu nhập lại không trùng khớp!");
            return;
        }

        showSuccess("Đang thực hiện đăng ký...");
        btnRegister.setDisable(true);

        new Thread(() -> {
            try {
                // 2. Check if username already exists
                Request checkUserReq = new Request(Module.ACCOUNTKH, "GET_BY_USERNAME", username);
                Response checkUserRes = SocketClient.getInstance().sendRequest(checkUserReq);
                if (checkUserRes != null && checkUserRes.isSuccess() && checkUserRes.getData() != null) {
                    Platform.runLater(() -> {
                        showError("Tên đăng nhập '" + username + "' đã được sử dụng!");
                        btnRegister.setDisable(false);
                    });
                    return;
                }

                // 3. Check if phone already exists
                Request checkPhoneReq = new Request(Module.KHACHHANG, "GET_BY_PHONE", phone);
                Response checkPhoneRes = SocketClient.getInstance().sendRequest(checkPhoneReq);
                if (checkPhoneRes != null && checkPhoneRes.isSuccess() && checkPhoneRes.getData() != null) {
                    Platform.runLater(() -> {
                        showError("Số điện thoại '" + phone + "' đã được sử dụng!");
                        btnRegister.setDisable(false);
                    });
                    return;
                }

                // 4. Generate next MaKH
                Request getAllReq = new Request(Module.KHACHHANG, Action.GET_ALL, null);
                Response getAllRes = SocketClient.getInstance().sendRequest(getAllReq);
                if (getAllRes == null || !getAllRes.isSuccess()) {
                    Platform.runLater(() -> {
                        showError("Không thể kết nối tới máy chủ để lấy mã khách hàng.");
                        btnRegister.setDisable(false);
                    });
                    return;
                }

                List<KhachHang> customers = (List<KhachHang>) getAllRes.getData();
                int maxNum = 0;
                if (customers != null) {
                    for (KhachHang kh : customers) {
                        String id = kh.getMaKH();
                        if (id != null && id.startsWith("KH")) {
                            try {
                                int num = Integer.parseInt(id.substring(2).trim());
                                if (num > maxNum) {
                                    maxNum = num;
                                }
                            } catch (NumberFormatException e) {
                                // Skip non-numeric customer IDs
                            }
                        }
                    }
                }
                String nextMaKH = "KH" + String.format("%02d", maxNum + 1);

                // 5. Create KhachHang profile
                KhachHang newKh = new KhachHang(nextMaKH, fullName, phone, 0);
                Request addKhReq = new Request(Module.KHACHHANG, Action.ADD, newKh);
                Response addKhRes = SocketClient.getInstance().sendRequest(addKhReq);

                if (addKhRes == null || !addKhRes.isSuccess()) {
                    Platform.runLater(() -> {
                        showError("Lỗi khi đăng ký thông tin khách hàng: " + (addKhRes != null ? addKhRes.getMessage() : "Không phản hồi"));
                        btnRegister.setDisable(false);
                    });
                    return;
                }

                // 6. Create AccountKH account
                AccountKH newAcc = new AccountKH(nextMaKH, username, password);
                Request addAccReq = new Request(Module.ACCOUNTKH, Action.ADD, newAcc);
                Response addAccRes = SocketClient.getInstance().sendRequest(addAccReq);

                if (addAccRes == null || !addAccRes.isSuccess()) {
                    // Rollback KhachHang insertion if AccountKH creation fails
                    Request deleteKhReq = new Request(Module.KHACHHANG, Action.DELETE, nextMaKH);
                    SocketClient.getInstance().sendRequest(deleteKhReq);

                    Platform.runLater(() -> {
                        showError("Lỗi khi đăng ký tài khoản đăng nhập: " + (addAccRes != null ? addAccRes.getMessage() : "Không phản hồi"));
                        btnRegister.setDisable(false);
                    });
                    return;
                }

                // 7. Registration Success
                Platform.runLater(() -> {
                    showSuccess("Đăng ký tài khoản thành công! Đang chuyển hướng...");
                    
                    // Delay and navigate back to login screen
                    new Thread(() -> {
                        try {
                            Thread.sleep(2000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        Platform.runLater(this::navigateToLogin);
                    }).start();
                });

            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() -> {
                    showError("Lỗi hệ thống: " + e.getMessage());
                    btnRegister.setDisable(false);
                });
            }
        }).start();
    }

    @FXML
    private void handleGoLogin(ActionEvent event) {
        navigateToLogin();
    }

    private void navigateToLogin() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/login.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) btnRegister.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Gourmet Hub - Đăng nhập");
            stage.centerOnScreen();
            stage.show();
        } catch (IOException e) {
            showError("Không mở được giao diện Đăng nhập.");
            e.printStackTrace();
        }
    }

    private void showError(String message) {
        lblMessage.getStyleClass().removeAll("message-error", "message-success");
        lblMessage.getStyleClass().add("message-error");
        lblMessage.setText(message);
    }

    private void showSuccess(String message) {
        lblMessage.getStyleClass().removeAll("message-error", "message-success");
        lblMessage.getStyleClass().add("message-success");
        lblMessage.setText(message);
    }
}
