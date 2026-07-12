package table;

import java.net.URL;
import java.util.ResourceBundle;
import java.util.List;

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
import javafx.application.Platform;

import model.BanAn;
import network.Action;
import network.Module;
import network.Request;
import network.Response;
import network.SocketClient;

public class TableAddController implements Initializable {

    @FXML
    private TextField txtMaBan;
    @FXML
    private TextField txtTenBan;
    @FXML
    private ComboBox<String> cbKhuVuc;
    @FXML
    private ComboBox<String> cbTrangThai;
    @FXML
    private Label lblMessage;
    @FXML
    private Button saveBtn;
    @FXML
    private Button cancelBtn;
    @FXML
    private Button backBtn;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        cbTrangThai.setItems(FXCollections.observableArrayList(
                "Trống", "Đang dùng", "Đặt trước"));
        loadKhuVuc();
    }

    private void loadKhuVuc() {
        new Thread(() -> {
            try {
                Request request = new Request(Module.BANAN, Action.GET_ALL, null);
                Response response = SocketClient.getInstance().sendRequest(request);
                Platform.runLater(() -> {
                    ObservableList<String> areas = FXCollections.observableArrayList();
                    if (response != null && response.isSuccess()) {
                        List<BanAn> serverList = (List<BanAn>) response.getData();
                        if (serverList != null) {
                            for (BanAn ba : serverList) {
                                if (ba.getKhuVuc() != null && !ba.getKhuVuc().trim().isEmpty() && !areas.contains(ba.getKhuVuc())) {
                                    areas.add(ba.getKhuVuc());
                                }
                            }
                        }
                    }
                    List<String> defaults = List.of("Tầng 1", "Tầng 2", "Sân vườn", "VIP", "Ngoài trời");
                    for (String d : defaults) {
                        if (!areas.contains(d)) {
                            areas.add(d);
                        }
                    }
                    cbKhuVuc.setItems(areas);
                });
            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() -> {
                    cbKhuVuc.setItems(FXCollections.observableArrayList("Tầng 1", "Tầng 2", "Sân vườn", "VIP", "Ngoài trời"));
                });
            }
        }).start();
    }

    private void setFormDisable(boolean disable) {
        txtMaBan.setDisable(disable);
        txtTenBan.setDisable(disable);
        cbKhuVuc.setDisable(disable);
        cbTrangThai.setDisable(disable);
        if (saveBtn != null) saveBtn.setDisable(disable);
        if (cancelBtn != null) cancelBtn.setDisable(disable);
        if (backBtn != null) backBtn.setDisable(disable);
    }

    @FXML
    private void handleSave(ActionEvent event) {
        String maban = txtMaBan.getText().trim();
        String tenban = txtTenBan.getText().trim();
        String khuvuc = cbKhuVuc.getValue() != null ? cbKhuVuc.getValue() : "";
        String trangthai = cbTrangThai.getValue();

        if (maban.isEmpty() || tenban.isEmpty() || trangthai == null) {
            showError("Vui lòng nhập đầy đủ các trường bắt buộc (*)");
            return;
        }

        setFormDisable(true);
        new Thread(() -> {
            try {
                // Validate duplicate code or name
                Request listRequest = new Request(Module.BANAN, Action.GET_ALL, null);
                Response listResponse = SocketClient.getInstance().sendRequest(listRequest);
                if (listResponse != null && listResponse.isSuccess()) {
                    List<BanAn> existingTables = (List<BanAn>) listResponse.getData();
                    if (existingTables != null) {
                        for (BanAn existing : existingTables) {
                            if (existing.getMaBan().equalsIgnoreCase(maban)) {
                                Platform.runLater(() -> {
                                    showError("Mã bàn đã tồn tại.");
                                    setFormDisable(false);
                                });
                                return;
                            }
                            if (existing.getTenBan().equalsIgnoreCase(tenban)) {
                                Platform.runLater(() -> {
                                    showError("Tên bàn đã tồn tại.");
                                    setFormDisable(false);
                                });
                                return;
                            }
                        }
                    }
                }

                BanAn ba = new BanAn(maban, tenban, khuvuc, trangthai);
                Request request = new Request(Module.BANAN, Action.ADD, ba);
                Response response = SocketClient.getInstance().sendRequest(request);
                Platform.runLater(() -> {
                    if (response != null && response.isSuccess()) {
                        showSuccess("Thêm bàn ăn thành công!");
                        clearForm();
                        txtMaBan.requestFocus();
                    } else {
                        showError(response != null ? response.getMessage() : "Mã bàn đã tồn tại hoặc lỗi Server!");
                    }
                    setFormDisable(false);
                });
            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() -> {
                    showError("Lỗi kết nối Server: " + e.getMessage());
                    setFormDisable(false);
                });
            }
        }).start();
    }

    @FXML
    private void handleCancel(ActionEvent event) {
        clearForm();
    }

    @FXML
    private void handleBack(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/table.fxml"));
            Parent view = loader.load();
            AnchorPane parent = (AnchorPane) txtMaBan.getScene().lookup("#contentArea");
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
        txtMaBan.clear();
        txtTenBan.clear();
        cbKhuVuc.setValue(null);
        cbTrangThai.setValue(null);
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
