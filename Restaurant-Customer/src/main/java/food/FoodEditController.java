package food;

import restaurant.MockDataStore;
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

public class FoodEditController implements Initializable {

    @FXML private TextField txtMaMon;
    @FXML private TextField txtTenMon;
    @FXML private ComboBox<String> cbLoaiMon;
    @FXML private TextField txtDonGia;
    @FXML private TextField txtAnh;
    @FXML private ComboBox<String> cbTrangThai;
    @FXML private Label lblMessage;

    @FXML private Button backBtn;
    @FXML private Button cancelBtn;
    @FXML private Button saveBtn;

    private FoodController.FoodModel selectedFood;
    private List<LoaiMon> categoryList = new ArrayList<>();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        selectedFood = FoodController.selectedFood;
        cbTrangThai.setItems(FXCollections.observableArrayList("Còn món", "Hết món"));

        if (selectedFood != null) {
            txtMaMon.setText(selectedFood.getId());
            txtTenMon.setText(selectedFood.getName());
            cbTrangThai.setValue(selectedFood.getStatus());
            String priceVal = selectedFood.getPrice().replace(" VNĐ", "").replace(",", "").trim();
            txtDonGia.setText(priceVal);

            setLoadingState(true);
            new Thread(() -> {
                try {
                    // Fetch categories from Server
                    Request catReq = new Request(Module.LOAIMON, Action.GET_ALL, null);
                    Response catRes = SocketClient.getInstance().sendRequest(catReq);

                    // Fetch full dish details
                    Request dishReq = new Request(Module.MONAN, Action.GET_BY_ID, selectedFood.getId());
                    Response dishRes = SocketClient.getInstance().sendRequest(dishReq);

                    Platform.runLater(() -> {
                        if (catRes != null && catRes.isSuccess() && catRes.getData() != null) {
                            List<LoaiMon> serverList = (List<LoaiMon>) catRes.getData();
                            categoryList.clear();
                            categoryList.addAll(serverList);
                            ObservableList<String> names = FXCollections.observableArrayList();
                            for (LoaiMon lm : categoryList) {
                                names.add(lm.getTenLoai());
                            }
                            cbLoaiMon.setItems(names);
                            cbLoaiMon.setValue(selectedFood.getCategory());
                        } else {
                            showError("Không thể tải danh sách loại món từ Server!");
                        }

                        if (dishRes != null && dishRes.isSuccess() && dishRes.getData() != null) {
                            MonAn fullDish = (MonAn) dishRes.getData();
                            txtAnh.setText(fullDish.getAnh() != null ? fullDish.getAnh() : "");
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
    }

    private void setLoadingState(boolean loading) {
        if (saveBtn != null) saveBtn.setDisable(loading);
        if (cancelBtn != null) cancelBtn.setDisable(loading);
        if (backBtn != null) backBtn.setDisable(loading);
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
        String dongiaStr = txtDonGia.getText().trim();
        String trangthai = cbTrangThai.getValue();

        if (mamon.isEmpty() || tenmon.isEmpty() || cbLoaiMon.getValue() == null || dongiaStr.isEmpty() || trangthai == null) {
            showError("Vui lòng nhập đầy đủ các thông tin bắt buộc (*)");
            return;
        }

        if (tenmon.length() > 50) {
            showError("Tên món ăn không được vượt quá 50 ký tự!");
            return;
        }

        java.math.BigDecimal price;
        try {
            price = new java.math.BigDecimal(dongiaStr);
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
        lblMessage.setText("Đang lưu thay đổi lên Server...");
        lblMessage.getStyleClass().removeAll("message-error", "message-success");

        final String finalMaloai = maloai;
        final String finalServerTrangThai = serverTrangThai;
        final java.math.BigDecimal finalPrice = price;

        new Thread(() -> {
            try {
                // Check unique name (excluding current Mamom)
                Request checkNameReq = new Request(Module.MONAN, "FIND_BY_NAME", tenmon);
                Response checkNameRes = SocketClient.getInstance().sendRequest(checkNameReq);
                if (checkNameRes != null && checkNameRes.isSuccess() && checkNameRes.getData() != null) {
                    MonAn existingMon = (MonAn) checkNameRes.getData();
                    if (!existingMon.getMaMon().equalsIgnoreCase(mamon)) {
                        Platform.runLater(() -> {
                            showError("Tên món ăn đã tồn tại trên hệ thống!");
                            setLoadingState(false);
                        });
                        return;
                    }
                }

                // Update
                MonAn ma = new MonAn(mamon, finalMaloai, tenmon, finalPrice, imagePath, finalServerTrangThai);
                Request updateReq = new Request(Module.MONAN, Action.UPDATE, ma);
                Response updateRes = SocketClient.getInstance().sendRequest(updateReq);

                Platform.runLater(() -> {
                    if (updateRes != null && updateRes.isSuccess()) {
                        showSuccess("Cập nhật món ăn thành công!");
                        clearForm();
                    } else {
                        showError(updateRes != null ? updateRes.getMessage() : "Cập nhật món ăn thất bại!");
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

    private void clearForm() {
        txtTenMon.clear();
        txtDonGia.clear();
        txtAnh.clear();
        cbLoaiMon.setValue(null);
        cbTrangThai.setValue(null);
        lblMessage.setText("");
        lblMessage.getStyleClass().removeAll("message-error", "message-success");
        txtTenMon.requestFocus();
    }

    @FXML
    private void handleCancel(ActionEvent event) {
        if (selectedFood != null) {
            setLoadingState(true);
            new Thread(() -> {
                try {
                    Request dishReq = new Request(Module.MONAN, Action.GET_BY_ID, selectedFood.getId());
                    Response dishRes = SocketClient.getInstance().sendRequest(dishReq);
                    
                    Platform.runLater(() -> {
                        txtTenMon.setText(selectedFood.getName());
                        cbLoaiMon.setValue(selectedFood.getCategory());
                        String priceVal = selectedFood.getPrice().replace(" VNĐ", "").replace(",", "").trim();
                        txtDonGia.setText(priceVal);
                        cbTrangThai.setValue(selectedFood.getStatus());
                        
                        if (dishRes != null && dishRes.isSuccess() && dishRes.getData() != null) {
                            MonAn fullDish = (MonAn) dishRes.getData();
                            txtAnh.setText(fullDish.getAnh() != null ? fullDish.getAnh() : "");
                        } else {
                            txtAnh.setText("");
                        }
                        
                        lblMessage.setText("");
                        lblMessage.getStyleClass().removeAll("message-error", "message-success");
                        setLoadingState(false);
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                    Platform.runLater(() -> setLoadingState(false));
                }
            }).start();
        }
    }

    @FXML
    private void handleBack(ActionEvent event) {
        goBack();
    }

    private void goBack() {
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
