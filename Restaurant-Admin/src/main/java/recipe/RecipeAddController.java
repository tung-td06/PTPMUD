package recipe;

import java.net.URL;
import java.util.ResourceBundle;
import java.util.List;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import network.Action;
import network.Module;
import network.Request;
import network.Response;
import network.SocketClient;

public class RecipeAddController implements Initializable {

    @FXML private ComboBox<String> cbMonAn;
    @FXML private ComboBox<String> cbNguyenLieu;
    @FXML private TextField txtMamon;
    @FXML private TextField txtTenmon;
    @FXML private TextField txtMaNguyenLieu;
    @FXML private TextField txtTenNguyenLieu;
    @FXML private TextField txtDinhLuong;
    @FXML private TextField txtDonVi;
    @FXML private Label lblMessage;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        setupListeners();
        loadComboBoxData();
    }

    private void setupListeners() {
        cbMonAn.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                String[] parts = newVal.split(" - ");
                txtMamon.setText(parts[0]);
                if (parts.length > 1) {
                    txtTenmon.setText(parts[1]);
                }
            } else {
                txtMamon.clear();
                txtTenmon.clear();
            }
        });

        cbNguyenLieu.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                String[] parts = newVal.split(" - ");
                txtMaNguyenLieu.setText(parts[0]);
                if (parts.length > 1) {
                    String nameWithUnit = parts[1];
                    int unitIdx = nameWithUnit.indexOf(" (");
                    if (unitIdx != -1) {
                        txtTenNguyenLieu.setText(nameWithUnit.substring(0, unitIdx));
                    } else {
                        txtTenNguyenLieu.setText(nameWithUnit);
                    }
                }
            } else {
                txtMaNguyenLieu.clear();
                txtTenNguyenLieu.clear();
            }
        });
    }

    private void loadComboBoxData() {
        new Thread(() -> {
            try {
                // Fetch dishes
                Request foodReq = new Request(Module.MONAN, Action.GET_ALL, null);
                Response foodRes = SocketClient.getInstance().sendRequest(foodReq);

                // Fetch ingredients
                Request materialReq = new Request(Module.NGUYENLIEU, Action.GET_ALL, null);
                Response materialRes = SocketClient.getInstance().sendRequest(materialReq);

                Platform.runLater(() -> {
                    if (foodRes != null && foodRes.isSuccess() && materialRes != null && materialRes.isSuccess()) {
                        List<model.MonAn> foods = (List<model.MonAn>) foodRes.getData();
                        List<model.NguyenLieu> materials = (List<model.NguyenLieu>) materialRes.getData();

                        javafx.collections.ObservableList<String> foodList = FXCollections.observableArrayList();
                        if (foods != null) {
                            for (model.MonAn food : foods) {
                                foodList.add(food.getMaMon() + " - " + food.getTenMon());
                            }
                        }
                        cbMonAn.setItems(foodList);

                        javafx.collections.ObservableList<String> rawList = FXCollections.observableArrayList();
                        if (materials != null) {
                            for (model.NguyenLieu item : materials) {
                                String unitStr = item.getDonViTinh() != null && !item.getDonViTinh().isEmpty() ? " (" + item.getDonViTinh() + ")" : "";
                                rawList.add(item.getMaNL() + " - " + item.getTenNL() + unitStr);
                            }
                        }
                        cbNguyenLieu.setItems(rawList);
                    } else {
                        showError("Không thể tải danh sách món ăn/nguyên liệu từ Server!");
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() -> showError("Lỗi tải dữ liệu ComboBox: " + e.getMessage()));
            }
        }).start();
    }

    @FXML
    private void handleSave(ActionEvent event) {
        String mamon = txtMamon.getText().trim();
        String tenmon = txtTenmon.getText().trim();
        String manguyenlieu = txtMaNguyenLieu.getText().trim();
        String tennguyenlieu = txtTenNguyenLieu.getText().trim();
        String dinhluongStr = txtDinhLuong.getText().trim();

        if (mamon.isEmpty() || manguyenlieu.isEmpty() || dinhluongStr.isEmpty()) {
            showError("Vui lòng nhập đầy đủ các trường bắt buộc (*)");
            return;
        }

        int dinhLuongVal;
        try {
            dinhLuongVal = Integer.parseInt(dinhluongStr);
            if (dinhLuongVal <= 0) {
                showError("Định lượng phải là số nguyên dương lớn hơn 0!");
                return;
            }
        } catch (NumberFormatException e) {
            showError("Định lượng phải là một số nguyên hợp lệ!");
            return;
        }

        try {
            // check unique recipe combination
            Request checkReq = new Request(Module.DINHLUONGMON, Action.GET_BY_ID, new String[]{mamon, manguyenlieu});
            Response checkRes = SocketClient.getInstance().sendRequest(checkReq);
            if (checkRes != null && checkRes.isSuccess() && checkRes.getData() != null) {
                showError("Nguyên liệu này đã tồn tại trong công thức của món ăn này.");
                return;
            }

            String donViVal = txtDonVi.getText().trim();

            model.DinhLuongMon newRecipe = new model.DinhLuongMon(mamon, manguyenlieu, dinhLuongVal, donViVal);
            Request request = new Request(Module.DINHLUONGMON, Action.ADD, newRecipe);
            Response response = SocketClient.getInstance().sendRequest(request);

            if (response != null && response.isSuccess()) {
                showSuccess("Thêm công thức món ăn thành công!");
                clearForm();
            } else {
                showError(response != null ? response.getMessage() : "Không thể thêm định lượng món ăn!");
            }
        } catch (Exception e) { showError("Lỗi: " + e.getMessage()); }
    }

    @FXML private void handleCancel(ActionEvent event) { clearForm(); }
    @FXML private void handleBack(ActionEvent event) { goBack(); }

    private void goBack() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/recipe.fxml"));
            Parent view = loader.load();
            AnchorPane parent = (AnchorPane) cbMonAn.getScene().lookup("#contentArea");
            if (parent != null) {
                parent.getChildren().clear(); parent.getChildren().add(view);
                AnchorPane.setTopAnchor(view, 0.0); AnchorPane.setBottomAnchor(view, 0.0);
                AnchorPane.setLeftAnchor(view, 0.0); AnchorPane.setRightAnchor(view, 0.0);
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void clearForm() {
        cbMonAn.setValue(null); cbNguyenLieu.setValue(null);
        txtMamon.clear(); txtTenmon.clear();
        txtMaNguyenLieu.clear(); txtTenNguyenLieu.clear();
        txtDinhLuong.clear(); txtDonVi.clear();
        lblMessage.setText(""); lblMessage.getStyleClass().removeAll("message-error", "message-success");
        cbMonAn.requestFocus();
    }

    private void showError(String msg) {
        lblMessage.getStyleClass().removeAll("message-error", "message-success");
        lblMessage.getStyleClass().add("message-error"); lblMessage.setText(msg);
    }

    private void showSuccess(String msg) {
        lblMessage.getStyleClass().removeAll("message-error", "message-success");
        lblMessage.getStyleClass().add("message-success"); lblMessage.setText(msg);
    }
}
