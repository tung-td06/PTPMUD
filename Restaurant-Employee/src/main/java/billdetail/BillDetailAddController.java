package billdetail;

import restaurant.MockDataStore;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.application.Platform;
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
import javafx.scene.layout.AnchorPane;
import java.math.BigDecimal;
import java.util.List;
import model.ChiTietHD;
import model.MonAn;
import model.HoaDon;
import network.Action;
import network.Module;
import network.Request;
import network.Response;
import network.SocketClient;

public class BillDetailAddController implements Initializable {

    @FXML private ComboBox<String> cbMaHD;
    @FXML private ComboBox<String> cbMonAn;
    @FXML private TextField txtSoluong;
    @FXML private TextField txtDongia;
    @FXML private TextField txtThanhtien;
    @FXML private Label lblMessage;

    private final ObservableList<MonAn> foodList = FXCollections.observableArrayList();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        loadDropdownData();

        cbMonAn.valueProperty().addListener((obs, ov, nv) -> {
            if (nv != null) {
                String foodId = nv.split(" - ")[0];
                for (MonAn food : foodList) {
                    if (food.getMaMon().equals(foodId)) {
                        txtDongia.setText(String.format("%.0f", food.getDonGia() != null ? food.getDonGia().doubleValue() : 0.0));
                        calcTotal();
                        break;
                    }
                }
            }
        });
        txtSoluong.textProperty().addListener((obs, ov, nv) -> calcTotal());
        txtDongia.textProperty().addListener((obs, ov, nv) -> calcTotal());
    }

    private void loadDropdownData() {
        cbMaHD.setDisable(true);
        cbMonAn.setDisable(true);
        new Thread(() -> {
            try {
                // 1. Load bills
                Request billReq = new Request(Module.HOADON, Action.GET_ALL, null);
                Response billRes = SocketClient.getInstance().sendRequest(billReq);
                ObservableList<String> billIds = FXCollections.observableArrayList();
                if (billRes != null && billRes.isSuccess()) {
                    List<HoaDon> bills = (List<HoaDon>) billRes.getData();
                    if (bills != null) {
                        for (HoaDon hd : bills) {
                            billIds.add(hd.getMaHD());
                        }
                    }
                }

                // 2. Load food list
                Request foodReq = new Request(Module.MONAN, Action.GET_ALL, null);
                Response foodRes = SocketClient.getInstance().sendRequest(foodReq);
                ObservableList<String> foodStrings = FXCollections.observableArrayList();
                foodList.clear();
                if (foodRes != null && foodRes.isSuccess()) {
                    List<MonAn> foods = (List<MonAn>) foodRes.getData();
                    if (foods != null) {
                        foodList.addAll(foods);
                        for (MonAn food : foods) {
                            foodStrings.add(food.getMaMon() + " - " + food.getTenMon());
                        }
                    }
                }

                Platform.runLater(() -> {
                    cbMaHD.setItems(billIds);
                    cbMonAn.setItems(foodStrings);
                    cbMaHD.setDisable(false);
                    cbMonAn.setDisable(false);
                });
            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() -> showError("Lỗi tải danh mục từ server: " + e.getMessage()));
            }
        }).start();
    }

    private void calcTotal() {
        try {
            double dongia = Double.parseDouble(txtDongia.getText().trim());
            int soluong = Integer.parseInt(txtSoluong.getText().trim());
            txtThanhtien.setText(String.format("%.0f", dongia * soluong));
        } catch (NumberFormatException ignored) {}
    }

    @FXML
    private void handleSave(ActionEvent event) {
        String maHD = cbMaHD.getValue();
        String selectedFood = cbMonAn.getValue();
        String slStr = txtSoluong.getText().trim();
        String dgStr = txtDongia.getText().trim();

        if (maHD == null || selectedFood == null || slStr.isEmpty() || dgStr.isEmpty()) {
            showError("Vui lòng nhập đầy đủ các trường bắt buộc (*)");
            return;
        }

        int soluong;
        double dongia;
        try {
            soluong = Integer.parseInt(slStr);
            if (soluong <= 0) {
                showError("Số lượng phải lớn hơn 0");
                return;
            }
        } catch (NumberFormatException e) {
            showError("Số lượng phải là một số nguyên hợp lệ");
            return;
        }

        try {
            dongia = Double.parseDouble(dgStr);
            if (dongia < 0) {
                showError("Đơn giá không được âm");
                return;
            }
        } catch (NumberFormatException e) {
            showError("Đơn giá phải là một số hợp lệ");
            return;
        }

        String mamon = selectedFood.split(" - ")[0];

        // Check for local duplicate primary key
        for (BillDetailController.BillDetailModel localDetail : MockDataStore.billDetails) {
            if (localDetail.getMaHD().equals(maHD) && localDetail.getMamon().equals(mamon)) {
                showError("Chi tiết hóa đơn (mã hóa đơn + món ăn) này đã tồn tại!");
                return;
            }
        }

        setLoading(true);
        new Thread(() -> {
            try {
                ChiTietHD ct = new ChiTietHD();
                ct.setMaHD(maHD);
                ct.setMaMon(mamon);
                ct.setSoLuong(soluong);
                ct.setDonGia(BigDecimal.valueOf(dongia));
                ct.setThanhTien(BigDecimal.valueOf(dongia * soluong));

                // Save to server
                Request addReq = new Request(Module.CHITIETHD, Action.ADD, ct);
                Response addRes = SocketClient.getInstance().sendRequest(addReq);

                if (addRes != null && addRes.isSuccess()) {
                    // Update Invoice total
                    syncInvoiceTotal(maHD);
                    
                    Platform.runLater(() -> {
                        showSuccess("Thêm chi tiết hóa đơn thành công!");
                        clearForm();
                        setLoading(false);
                        cbMaHD.requestFocus();
                    });
                } else {
                    Platform.runLater(() -> {
                        showError(addRes != null ? addRes.getMessage() : "Thêm chi tiết hóa đơn thất bại.");
                        setLoading(false);
                    });
                }
            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() -> {
                    showError("Lỗi: " + e.getMessage());
                    setLoading(false);
                });
            }
        }).start();
    }

    private void syncInvoiceTotal(String maHD) {
        try {
            // Get all details for this invoice from Server
            Request detailReq = new Request(Module.CHITIETHD, "GET_BY_HOADON", maHD);
            Response detailRes = SocketClient.getInstance().sendRequest(detailReq);
            
            BigDecimal newTotal = BigDecimal.ZERO;
            if (detailRes != null && detailRes.isSuccess()) {
                List<ChiTietHD> details = (List<ChiTietHD>) detailRes.getData();
                if (details != null) {
                    for (ChiTietHD detail : details) {
                        BigDecimal tt = detail.getThanhTien();
                        if (tt == null) {
                            tt = detail.getDonGia().multiply(BigDecimal.valueOf(detail.getSoLuong()));
                        }
                        newTotal = newTotal.add(tt);
                    }
                }
            }

            // Fetch the invoice
            Request hdReq = new Request(Module.HOADON, Action.GET_BY_ID, maHD);
            Response hdRes = SocketClient.getInstance().sendRequest(hdReq);
            if (hdRes != null && hdRes.isSuccess()) {
                HoaDon hd = (HoaDon) hdRes.getData();
                if (hd != null) {
                    hd.setTongTien(newTotal);
                    BigDecimal giamGia = hd.getGiamGia() != null ? hd.getGiamGia() : BigDecimal.ZERO;
                    BigDecimal thanhToan = newTotal.subtract(giamGia);
                    if (thanhToan.compareTo(BigDecimal.ZERO) < 0) thanhToan = BigDecimal.ZERO;
                    hd.setThanhToan(thanhToan);
                    
                    // Update Invoice
                    Request updateReq = new Request(Module.HOADON, Action.UPDATE, hd);
                    SocketClient.getInstance().sendRequest(updateReq);
                }
            }
        } catch (Exception e) {
            System.err.println("Lỗi đồng bộ hóa đơn: " + e.getMessage());
        }
    }

    private void setLoading(boolean loading) {
        cbMaHD.setDisable(loading);
        cbMonAn.setDisable(loading);
        txtSoluong.setDisable(loading);
        txtDongia.setDisable(loading);
    }

    @FXML private void handleCancel(ActionEvent event) { clearForm(); }
    @FXML private void handleBack(ActionEvent event) { goBack(); }

    private void goBack() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/billdetail.fxml"));
            Parent view = loader.load();
            AnchorPane parent = (AnchorPane) cbMaHD.getScene().lookup("#contentArea");
            if (parent != null) {
                parent.getChildren().clear(); parent.getChildren().add(view);
                AnchorPane.setTopAnchor(view, 0.0); AnchorPane.setBottomAnchor(view, 0.0);
                AnchorPane.setLeftAnchor(view, 0.0); AnchorPane.setRightAnchor(view, 0.0);
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void clearForm() {
        cbMaHD.setValue(null); cbMonAn.setValue(null);
        txtSoluong.clear(); txtDongia.clear(); txtThanhtien.clear();
        lblMessage.setText(""); lblMessage.getStyleClass().removeAll("message-error", "message-success");
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
