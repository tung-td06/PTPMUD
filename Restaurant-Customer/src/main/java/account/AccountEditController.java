package account;

import java.net.URL;
import java.util.ResourceBundle;
import java.util.List;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
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
import javafx.fxml.Initializable;

public class AccountEditController implements Initializable {

    @FXML private TextField txtMaNV;
    @FXML private TextField txtUsername;
    @FXML private ComboBox<String> cbQuyen;
    @FXML private PasswordField txtPassword;
    @FXML private Label lblMessage;

    private AccountController.AccountModel selectedAccount;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        selectedAccount = AccountController.selectedAccount;

        cbQuyen.setItems(FXCollections.observableArrayList("Admin", "Quản lý", "Nhân viên"));

        if (selectedAccount != null) {
            txtMaNV.setText(selectedAccount.getMaNV());
            txtUsername.setText(selectedAccount.getUsername());
            cbQuyen.setValue(selectedAccount.getQuyen());
        }
    }

    @FXML
    private void handleSave(ActionEvent event) {
        String username = txtUsername.getText().trim();
        String quyen = cbQuyen.getValue();
        String password = txtPassword.getText().trim();

        if (username.isEmpty() || quyen == null) {
            showError("Vui lòng nhập đầy đủ các trường bắt buộc (*)");
            return;
        }

        String finalPassword = password.isEmpty() ? selectedAccount.getPassword() : password;

        int quyenInt = switch (quyen) {
            case "Admin" -> 1;
            case "Quản lý" -> 2;
            case "Nhân viên" -> 3;
            default -> 3;
        };

        model.Account updatedAccount = new model.Account();
        updatedAccount.setMaNV(selectedAccount.getMaNV());
        updatedAccount.setTenDN(username);
        updatedAccount.setPassword(finalPassword);
        updatedAccount.setQuyen(quyenInt);

        new Thread(() -> {
            try {
                // Check if username is taken by any OTHER account
                Request checkReq = new Request(Module.ACCOUNT, Action.GET_ALL, null);
                Response checkRes = SocketClient.getInstance().sendRequest(checkReq);
                if (checkRes != null && checkRes.isSuccess() && checkRes.getData() != null) {
                    @SuppressWarnings("unchecked")
                    List<model.Account> allAccs = (List<model.Account>) checkRes.getData();
                    for (model.Account a : allAccs) {
                        if (a.getTenDN().equalsIgnoreCase(username) && !a.getMaNV().equalsIgnoreCase(selectedAccount.getMaNV())) {
                            Platform.runLater(() -> showError("Tên đăng nhập đã tồn tại trong hệ thống."));
                            return;
                        }
                    }
                }

                Request req = new Request(Module.ACCOUNT, Action.UPDATE, updatedAccount);
                Response res = SocketClient.getInstance().sendRequest(req);

                Platform.runLater(() -> {
                    if (res != null && res.isSuccess()) {
                        showSuccess("Cập nhật tài khoản thành công!");
                        
                        // Sync logged in session if it's the current user's account
                        if (Login.LoginController.loggedInAccount != null && 
                            Login.LoginController.loggedInAccount.getMaNV().equals(selectedAccount.getMaNV())) {
                            
                            Login.LoginController.loggedInAccount.setTenDN(username);
                            Login.LoginController.loggedInAccount.setPassword(finalPassword);
                            Login.LoginController.loggedInAccount.setQuyen(quyenInt);

                            // Dynamically update sidebar UI labels
                            javafx.scene.Scene scene = txtMaNV.getScene();
                            if (scene != null) {
                                Label profileName = (Label) scene.lookup("#lblProfileName");
                                Label profileRole = (Label) scene.lookup("#lblProfileRole");
                                if (profileName != null) {
                                    profileName.setText(username);
                                }
                                if (profileRole != null) {
                                    String roleStr = switch (quyenInt) {
                                        case 1 -> "Quản trị viên";
                                        case 2 -> "Quản lý";
                                        case 3 -> "Nhân viên";
                                        default -> "Không xác định";
                                    };
                                    profileRole.setText(roleStr);
                                }
                            }
                        }
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
        if (selectedAccount != null) {
            txtUsername.setText(selectedAccount.getUsername());
            cbQuyen.setValue(selectedAccount.getQuyen());
            txtPassword.clear();
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
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/account.fxml"));
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
