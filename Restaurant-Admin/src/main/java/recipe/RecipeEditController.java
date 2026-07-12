package recipe;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import network.Action;
import network.Module;
import network.Request;
import network.Response;
import network.SocketClient;

public class RecipeEditController implements Initializable {

    @FXML private TextField txtMamon;
    @FXML private TextField txtTenmon;
    @FXML private TextField txtMaNguyenLieu;
    @FXML private TextField txtTenNguyenLieu;
    @FXML private TextField txtDinhLuong;
    @FXML private TextField txtDonVi;
    @FXML private Label lblMessage;

    private RecipeController.RecipeModel selectedRecipe;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        selectedRecipe = RecipeController.selectedRecipe;
        if (selectedRecipe != null) {
            txtMamon.setText(selectedRecipe.getMamon());
            txtTenmon.setText(selectedRecipe.getTenmon());
            txtMaNguyenLieu.setText(selectedRecipe.getManguyenlieu());
            txtTenNguyenLieu.setText(selectedRecipe.getTennguyenlieu());
            txtDinhLuong.setText(String.valueOf(selectedRecipe.getRawDinhLuong()));
            txtDonVi.setText(selectedRecipe.getDonViTinh());
        }
    }

    @FXML
    private void handleSave(ActionEvent event) {
        String newMamon = txtMamon.getText().trim();
        String newManguyenlieu = txtMaNguyenLieu.getText().trim();
        String dinhluongStr = txtDinhLuong.getText().trim();
        String donViStr = txtDonVi.getText().trim();

        if (newMamon.isEmpty() || newManguyenlieu.isEmpty() || dinhluongStr.isEmpty()) {
            showError("Vui lòng nhập đầy đủ thông tin.");
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
            boolean keyChanged = !newMamon.equals(selectedRecipe.getMamon()) || !newManguyenlieu.equals(selectedRecipe.getManguyenlieu());

            if (keyChanged) {
                // Verify that the new combination doesn't already exist on server
                Request checkReq = new Request(Module.DINHLUONGMON, Action.GET_BY_ID, new String[]{newMamon, newManguyenlieu});
                Response checkRes = SocketClient.getInstance().sendRequest(checkReq);
                if (checkRes != null && checkRes.isSuccess() && checkRes.getData() != null) {
                    showError("Cặp món ăn và nguyên liệu mới này đã tồn tại.");
                    return;
                }

                // Delete the old record
                Request deleteReq = new Request(Module.DINHLUONGMON, Action.DELETE, new String[]{selectedRecipe.getMamon(), selectedRecipe.getManguyenlieu()});
                Response deleteRes = SocketClient.getInstance().sendRequest(deleteReq);
                if (deleteRes == null || !deleteRes.isSuccess()) {
                    showError("Không thể xóa bản ghi cũ: " + (deleteRes != null ? deleteRes.getMessage() : "Mất kết nối"));
                    return;
                }

                // Add the new record
                model.DinhLuongMon newRecipe = new model.DinhLuongMon(newMamon, newManguyenlieu, dinhLuongVal, donViStr);
                Request addReq = new Request(Module.DINHLUONGMON, Action.ADD, newRecipe);
                Response addRes = SocketClient.getInstance().sendRequest(addReq);
                if (addRes != null && addRes.isSuccess()) {
                    showSuccess("Cập nhật định lượng thành công!");
                    selectedRecipe = new RecipeController.RecipeModel(
                        newMamon, txtTenmon.getText().trim(),
                        newManguyenlieu, txtTenNguyenLieu.getText().trim(),
                        dinhluongStr + (donViStr.isEmpty() ? "" : " " + donViStr),
                        dinhLuongVal, donViStr
                    );
                    RecipeController.selectedRecipe = selectedRecipe;
                } else {
                    showError("Lỗi thêm bản ghi mới: " + (addRes != null ? addRes.getMessage() : "Mất kết nối"));
                }
            } else {
                model.DinhLuongMon updated = new model.DinhLuongMon(newMamon, newManguyenlieu, dinhLuongVal, donViStr);
                Request request = new Request(Module.DINHLUONGMON, Action.UPDATE, updated);
                Response response = SocketClient.getInstance().sendRequest(request);

                if (response != null && response.isSuccess()) {
                    showSuccess("Cập nhật định lượng thành công!");
                    selectedRecipe = new RecipeController.RecipeModel(
                        newMamon, txtTenmon.getText().trim(),
                        newManguyenlieu, txtTenNguyenLieu.getText().trim(),
                        dinhluongStr + (donViStr.isEmpty() ? "" : " " + donViStr),
                        dinhLuongVal, donViStr
                    );
                    RecipeController.selectedRecipe = selectedRecipe;
                } else {
                    showError(response != null ? response.getMessage() : "Cập nhật định lượng thất bại!");
                }
            }
        } catch (Exception e) { showError("Lỗi: " + e.getMessage()); }
    }

    @FXML private void handleCancel(ActionEvent event) {
        if (selectedRecipe != null) {
            txtMamon.setText(selectedRecipe.getMamon());
            txtTenmon.setText(selectedRecipe.getTenmon());
            txtMaNguyenLieu.setText(selectedRecipe.getManguyenlieu());
            txtTenNguyenLieu.setText(selectedRecipe.getTennguyenlieu());
            txtDinhLuong.setText(String.valueOf(selectedRecipe.getRawDinhLuong()));
            txtDonVi.setText(selectedRecipe.getDonViTinh());
            lblMessage.setText(""); lblMessage.getStyleClass().removeAll("message-error", "message-success");
        }
    }

    @FXML private void handleBack(ActionEvent event) { goBack(); }

    private void goBack() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/recipe.fxml"));
            Parent view = loader.load();
            AnchorPane parent = (AnchorPane) txtMamon.getScene().lookup("#contentArea");
            if (parent != null) {
                parent.getChildren().clear(); parent.getChildren().add(view);
                AnchorPane.setTopAnchor(view, 0.0); AnchorPane.setBottomAnchor(view, 0.0);
                AnchorPane.setLeftAnchor(view, 0.0); AnchorPane.setRightAnchor(view, 0.0);
            }
        } catch (Exception e) { e.printStackTrace(); }
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
