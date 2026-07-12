package food;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.Button;
import javafx.scene.layout.AnchorPane;
import javafx.application.Platform;

import model.LoaiMon;
import model.MonAn;
import network.Action;
import network.Module;
import network.Request;
import network.Response;
import network.SocketClient;

import java.util.List;
import java.util.ArrayList;

public class FoodAddController implements Initializable {

    @FXML
    private TextField txtMaMon;
    @FXML
    private TextField txtTenMon;
    @FXML
    private ComboBox<String> cbLoaiMon;
    @FXML
    private TextField txtDonGia;
    @FXML
    private TextField txtAnh;
    @FXML
    private ComboBox<String> cbTrangThai;
    @FXML
    private Label lblMessage;

    @FXML
    private Button backBtn;
    @FXML
    private Button cancelBtn;
    @FXML
    private Button saveBtn;

    private List<LoaiMon> categoryList = new ArrayList<>();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        cbTrangThai.setItems(FXCollections.observableArrayList("Còn món", "Hết món"));
        loadLoaiMon();
    }

    private void loadLoaiMon() {
        setLoadingState(true);
        new Thread(() -> {
            try {
                Request request = new Request(Module.LOAIMON, Action.GET_ALL, null);
                Response response = SocketClient.getInstance().sendRequest(request);
                Platform.runLater(() -> {
                    if (response != null && response.isSuccess() && response.getData() != null) {
                        List<LoaiMon> serverList = (List<LoaiMon>) response.getData();
                        categoryList.clear();
                        categoryList.addAll(serverList);
                        ObservableList<String> names = FXCollections.observableArrayList();
                        for (LoaiMon lm : categoryList) {
                            names.add(lm.getTenLoai());
                        }
                        cbLoaiMon.setItems(names);
                    } else {
                        showError("Không thể tải danh sách loại món từ Server!");
                    }
                    setLoadingState(false);
                });
            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() -> {
                    showError("Lỗi kết nối Server: " + e.getMessage());
                    setLoadingState(false);
                });
            }
        }).start();
    }

    private void setLoadingState(boolean loading) {
        if (saveBtn != null) saveBtn.setDisable(loading);
        if (cancelBtn != null) cancelBtn.setDisable(loading);
        if (backBtn != null) backBtn.setDisable(loading);
        if (txtMaMon != null) txtMaMon.setDisable(loading);
        if (txtTenMon != null) txtTenMon.setDisable(loading);
        if (txtDonGia != null) txtDonGia.setDisable(loading);
        if (txtAnh != null) txtAnh.setDisable(loading);
        if (cbLoaiMon != null) cbLoaiMon.setDisable(loading);
        if (cbTrangThai != null) cbTrangThai.setDisable(loading);
    }

    @FXML
    private void handleSave(ActionEvent event) {
        String mamon = txtMaMon.getText().trim();
        String tenmon = txtTenMon.getText().trim();
        String dongia = txtDonGia.getText().trim();
        String trangthai = cbTrangThai.getValue();

        if (mamon.isEmpty() || tenmon.isEmpty() || dongia.isEmpty() || trangthai == null || cbLoaiMon.getValue() == null) {
            showError("Vui lòng nhập đầy đủ các trường bắt buộc (*)");
            return;
        }

        if (mamon.length() > 15) {
            showError("Mã món ăn không được vượt quá 15 ký tự!");
            return;
        }

        if (!mamon.matches("^[a-zA-Z0-9_-]+$")) {
            showError("Mã món chỉ được chứa chữ cái không dấu, số, gạch ngang hoặc gạch dưới!");
            return;
        }

        if (tenmon.length() > 50) {
            showError("Tên món ăn không được vượt quá 50 ký tự!");
            return;
        }

        java.math.BigDecimal price;
        try {
            price = new java.math.BigDecimal(dongia);
        } catch (NumberFormatException e) {
            showError("Đơn giá phải là số");
            return;
        }

        if (price.compareTo(java.math.BigDecimal.ZERO) <= 0) {
            showError("Đơn giá phải lớn hơn 0");
            return;
        }

        String imagePath = txtAnh.getText().trim();
        if (imagePath.length() > 255) {
            showError("Đường dẫn ảnh không được vượt quá 255 ký tự!");
            return;
        }

        String selectedCategoryName = cbLoaiMon.getValue();
        String maloai = null;
        for (LoaiMon lm : categoryList) {
            if (lm.getTenLoai().equals(selectedCategoryName)) {
                maloai = lm.getMaLoai();
                break;
            }
        }

        if (maloai == null) {
            showError("Loại món đã chọn không hợp lệ!");
            return;
        }

        String serverTrangThai = "1";
        if ("Hết món".equals(trangthai)) {
            serverTrangThai = "0";
        }

        setLoadingState(true);
        lblMessage.setText("Đang kiểm tra thông tin trên Server...");
        lblMessage.getStyleClass().removeAll("message-error", "message-success");

        final String finalMaloai = maloai;
        final String finalServerTrangThai = serverTrangThai;
        final java.math.BigDecimal finalPrice = price;

        new Thread(() -> {
            try {
                // Check Mamom exists
                Request existReq = new Request(Module.MONAN, "EXISTS", mamon);
                Response existRes = SocketClient.getInstance().sendRequest(existReq);
                if (existRes != null && existRes.isSuccess() && existRes.getData() != null && (Boolean) existRes.getData()) {
                    Platform.runLater(() -> {
                        showError("Mã món ăn đã tồn tại trên hệ thống!");
                        setLoadingState(false);
                    });
                    return;
                }

                // Check Tenmon exists
                Request nameReq = new Request(Module.MONAN, "FIND_BY_NAME", tenmon);
                Response nameRes = SocketClient.getInstance().sendRequest(nameReq);
                if (nameRes != null && nameRes.isSuccess() && nameRes.getData() != null) {
                    Platform.runLater(() -> {
                        showError("Tên món ăn đã tồn tại trên hệ thống!");
                        setLoadingState(false);
                    });
                    return;
                }

                // Save
                MonAn ma = new MonAn(mamon, finalMaloai, tenmon, finalPrice, imagePath, finalServerTrangThai);
                Request addReq = new Request(Module.MONAN, Action.ADD, ma);
                Response addRes = SocketClient.getInstance().sendRequest(addReq);

                Platform.runLater(() -> {
                    if (addRes != null && addRes.isSuccess()) {
                        showSuccess("Thêm món ăn thành công!");
                        clearForm();
                        txtMaMon.requestFocus();
                    } else {
                        showError(addRes != null ? addRes.getMessage() : "Thêm món ăn thất bại!");
                    }
                    setLoadingState(false);
                });

            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() -> {
                    showError("Lỗi kết nối Server: " + e.getMessage());
                    setLoadingState(false);
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
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/food.fxml"));
            Parent view = loader.load();
            AnchorPane parent = (AnchorPane) txtMaMon.getScene().lookup("#contentArea");
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
        txtMaMon.clear();
        txtTenMon.clear();
        txtDonGia.clear();
        txtAnh.clear();
        cbLoaiMon.setValue(null);
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

