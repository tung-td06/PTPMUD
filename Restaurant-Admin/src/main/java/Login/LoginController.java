package Login;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
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

import model.Account;
import network.Action;
import network.Module;
import network.Request;
import network.Response;
import network.SocketClient;

public class LoginController implements Initializable {

    public static Account loggedInAccount;

    @FXML
    private TextField txtUsername;

    @FXML
    private PasswordField txtPassword;

    @FXML
    private Button btnLogin;

    @FXML
    private Label lblMessage;

    @Override
    public void initialize(URL url, ResourceBundle rb) {

        lblMessage.setText("");

        lblMessage.visibleProperty().bind(lblMessage.textProperty().isNotEmpty());
        lblMessage.managedProperty().bind(lblMessage.visibleProperty());

        txtUsername.textProperty().addListener((o, ov, nv) -> lblMessage.setText(""));
        txtPassword.textProperty().addListener((o, ov, nv) -> lblMessage.setText(""));

        new Thread(() -> SocketClient.getInstance().connect()).start();
    }

    @FXML
    private void handleLogin(ActionEvent event) {

        String username = txtUsername.getText().trim();
        String password = txtPassword.getText().trim();

        if (username.isEmpty() || password.isEmpty()) {
            showError("Vui lòng nhập đầy đủ thông tin!");
            return;
        }

        showSuccess("Đang xác thực thông tin...");

        new Thread(() -> {

            try {

                Map<String, String> loginData = new HashMap<>();

                loginData.put("username", username);
                loginData.put("password", password);

                Request request = new Request(
                        Module.ACCOUNT,
                        Action.LOGIN,
                        loginData);

                Response response = SocketClient.getInstance()
                        .sendRequest(request);

                Platform.runLater(() -> {

                    if (response == null) {
                        showError("Không nhận được phản hồi từ Server!");
                        return;
                    }

                    if (!response.isSuccess()) {
                        showError(response.getMessage());
                        return;
                    }

                    Account account = (Account) response.getData();

                    if (account == null) {
                        showError("Không lấy được thông tin tài khoản!");
                        return;
                    }

                    if (account.getQuyen() != 1) {
                        showError("Bạn không có quyền truy cập hệ thống Admin!");
                        return;
                    }

                    loggedInAccount = account;

                    showSuccess("Đăng nhập thành công!");

                    try {

                        FXMLLoader loader = new FXMLLoader(
                                getClass().getResource("/fxml/dashboard.fxml"));

                        Parent root = loader.load();

                        Stage stage = (Stage) btnLogin.getScene().getWindow();

                        stage.setScene(new Scene(root));
                        stage.setTitle("Gourmet Hub - Restaurant Management");
                        stage.centerOnScreen();
                        stage.show();

                    } catch (IOException e) {

                        showError("Không mở được Dashboard.");

                        e.printStackTrace();

                    }

                });

            } catch (Exception e) {

                Platform.runLater(() -> showError("Lỗi: " + e.getMessage()));

                e.printStackTrace();

            }

        }).start();

    }

    private void showError(String message) {

        lblMessage.getStyleClass().removeAll(
                "message-error",
                "message-success");

        lblMessage.getStyleClass().add("message-error");

        lblMessage.setText(message);

    }

    private void showSuccess(String message) {

        lblMessage.getStyleClass().removeAll(
                "message-error",
                "message-success");

        lblMessage.getStyleClass().add("message-success");

        lblMessage.setText(message);

    }

}