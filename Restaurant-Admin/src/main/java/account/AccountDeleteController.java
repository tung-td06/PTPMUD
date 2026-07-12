package account;

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

public class AccountDeleteController implements Initializable {

    @FXML private TextField txtMaNV;
    @FXML private TextField txtUsername;
    @FXML private TextField txtQuyen;
    @FXML private Label lblMessage;
    @FXML private Button deleteBtn;

    private AccountController.AccountModel selectedAccount;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        selectedAccount = AccountController.selectedAccount;
        if (selectedAccount != null) {
            txtMaNV.setText(selectedAccount.getMaNV());
            txtUsername.setText(selectedAccount.getUsername());
            txtQuyen.setText(selectedAccount.getQuyen());
        }
    }

    @FXML
    private void handleDelete(ActionEvent event) {
        if (selectedAccount == null) {
            showError("Không tìm thấy thông tin tài khoản.");
            return;
        }
        deleteBtn.setDisable(true);
        new Thread(() -> {
            try {
                Request req = new Request(Module.ACCOUNT, Action.DELETE, selectedAccount.getMaNV());
                Response res = SocketClient.getInstance().sendRequest(req);

                Platform.runLater(() -> {
                    if (res != null && res.isSuccess()) {
                        showSuccess("Xóa tài khoản thành công!");
                    } else {
                        showError("Xóa tài khoản thất bại: " + (res != null ? res.getMessage() : "Không phản hồi"));
                        deleteBtn.setDisable(false);
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() -> {
                    showError("Lỗi: " + e.getMessage());
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
