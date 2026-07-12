package importreceipt;
 
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.ResourceBundle;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.fxml.Initializable;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import model.NhaCungCap;
import model.NhapHang;
import network.Action;
import network.Module;
import network.Request;
import network.Response;
import network.SocketClient;
 
public class ImportAddController implements Initializable {
 
    @FXML private TextField txtMaHang;
    @FXML private ComboBox<String> cbNhaCungCap;
    @FXML private TextField txtNgayNhap;
    @FXML private TextField txtTongTien;
    @FXML private Label lblMessage;
 
    // New Supplier Fields
    @FXML private TextField txtNewMaNCC;
    @FXML private TextField txtNewTenNCC;
    @FXML private Label lblNccMessage;
 
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        loadSuppliersAndSelect(null);
        
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        txtNgayNhap.setText(sdf.format(new Date()));
    }
 
    private void loadSuppliersAndSelect(String selectId) {
        new Thread(() -> {
            try {
                Request req = new Request(Module.NHACUNGCAP, Action.GET_ALL, null);
                Response res = SocketClient.getInstance().sendRequest(req);
                if (res != null && res.isSuccess()) {
                    List<NhaCungCap> list = (List<NhaCungCap>) res.getData();
                    Platform.runLater(() -> {
                        ObservableList<String> items = FXCollections.observableArrayList();
                        String targetItem = null;
                        if (list != null) {
                            for (NhaCungCap ncc : list) {
                                String itemString = ncc.getMaNCC() + " - " + ncc.getTenNCC();
                                items.add(itemString);
                                if (ncc.getMaNCC().equals(selectId)) {
                                    targetItem = itemString;
                                }
                            }
                        }
                        cbNhaCungCap.setItems(items);
                        if (targetItem != null) {
                            cbNhaCungCap.setValue(targetItem);
                        }
                    });
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }
 
    @FXML
    private void handleAddNewNCC() {
        String newMa = txtNewMaNCC.getText().trim();
        String newTen = txtNewTenNCC.getText().trim();
 
        if (newMa.isEmpty() || newTen.isEmpty()) {
            showNccError("Vui lòng nhập đầy đủ mã và tên nhà cung cấp mới");
            return;
        }
 
        if (!newMa.matches("^[a-zA-Z0-9_-]+$")) {
            showNccError("Mã nhà cung cấp chỉ được chứa chữ cái, số, gạch nối hoặc gạch dưới");
            return;
        }
 
        new Thread(() -> {
            try {
                // Check duplicate
                Request checkReq = new Request(Module.NHACUNGCAP, Action.GET_BY_ID, newMa);
                Response checkRes = SocketClient.getInstance().sendRequest(checkReq);
                if (checkRes != null && checkRes.isSuccess() && checkRes.getData() != null) {
                    Platform.runLater(() -> showNccError("Mã nhà cung cấp đã tồn tại!"));
                    return;
                }

                // Generate a valid unique dummy phone number (10 digits) to satisfy the SQL database check constraint
                String dummyPhone = "09" + String.format("%08d", (int)(Math.random() * 100000000L));
                NhaCungCap ncc = new NhaCungCap(newMa, newTen, dummyPhone);
                Request addReq = new Request(Module.NHACUNGCAP, Action.ADD, ncc);
                Response addRes = SocketClient.getInstance().sendRequest(addReq);
                if (addRes != null && addRes.isSuccess()) {
                    Platform.runLater(() -> {
                        showNccSuccess("Thêm nhà cung cấp mới thành công!");
                        txtNewMaNCC.clear();
                        txtNewTenNCC.clear();
                        
                        try {
                            supplier.SupplierController.SupplierModel sm = new supplier.SupplierController.SupplierModel(newMa, newTen, dummyPhone);
                            restaurant.MockDataStore.suppliers.add(sm);
                        } catch (Exception ignored) {}
 
                        loadSuppliersAndSelect(newMa);
                    });
                } else {
                    Platform.runLater(() -> showNccError(addRes != null ? addRes.getMessage() : "Thêm nhà cung cấp thất bại!"));
                }
            } catch (Exception e) {
                Platform.runLater(() -> showNccError("Lỗi: " + e.getMessage()));
                e.printStackTrace();
            }
        }).start();
    }
 
    @FXML
    private void handleSave(ActionEvent event) {
        String mahang = txtMaHang.getText().trim();
        String selectedNcc = cbNhaCungCap.getValue();
        String ngaynhap = txtNgayNhap.getText().trim();
        String tongtienStr = txtTongTien.getText().trim();
 
        if (mahang.isEmpty() || selectedNcc == null || ngaynhap.isEmpty() || tongtienStr.isEmpty()) {
            showError("Vui lòng nhập đầy đủ các trường bắt buộc (*)");
            return;
        }
 
        if (!mahang.matches("^[a-zA-Z0-9_-]+$")) {
            showError("Mã phiếu chỉ được chứa chữ cái, số, gạch nối hoặc gạch dưới");
            return;
        }
 
        if (mahang.length() > 10) {
            showError("Mã phiếu nhập không được vượt quá 10 ký tự");
            return;
        }
 
        java.math.BigDecimal tongTienBd;
        try {
            double val = Double.parseDouble(tongtienStr);
            if (val < 0) {
                showError("Tổng tiền nhập hàng phải lớn hơn hoặc bằng 0");
                return;
            }
            tongTienBd = java.math.BigDecimal.valueOf(val);
        } catch (NumberFormatException e) {
            showError("Tổng tiền phải là số hợp lệ");
            return;
        }
 
        java.sql.Timestamp ngaynhapTimestamp;
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Date parsedDate = sdf.parse(ngaynhap);
            ngaynhapTimestamp = new java.sql.Timestamp(parsedDate.getTime());
        } catch (Exception e) {
            showError("Định dạng ngày nhập không hợp lệ (yyyy-MM-dd HH:mm:ss)");
            return;
        }
 
        String mancc = selectedNcc.split(" - ")[0].trim();
 
        new Thread(() -> {
            try {
                // Check duplicate
                Request checkReq = new Request(Module.NHAPHANG, Action.GET_BY_ID, mahang);
                Response checkRes = SocketClient.getInstance().sendRequest(checkReq);
                if (checkRes != null && checkRes.isSuccess() && checkRes.getData() != null) {
                    Platform.runLater(() -> showError("Mã phiếu nhập đã tồn tại trong hệ thống!"));
                    return;
                }
 
                // Add new import
                NhapHang nh = new NhapHang(mahang, mancc, ngaynhapTimestamp, tongTienBd);
                Request addReq = new Request(Module.NHAPHANG, Action.ADD, nh);
                Response addRes = SocketClient.getInstance().sendRequest(addReq);
                if (addRes != null && addRes.isSuccess()) {
                    Platform.runLater(() -> {
                        showSuccess("Tạo phiếu nhập thành công!");
                        clearForm();
                    });
                } else {
                    Platform.runLater(() -> showError(addRes != null ? addRes.getMessage() : "Thêm mới thất bại. Không nhận được phản hồi từ Server!"));
                }
            } catch (Exception e) {
                Platform.runLater(() -> showError("Lỗi: " + e.getMessage()));
                e.printStackTrace();
            }
        }).start();
    }
 
    @FXML
    private void handleCancel(ActionEvent event) {
        clearForm();
    }
 
    @FXML
    private void handleBack(ActionEvent event) {
        goBack();
    }
 
    private void goBack() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/import.fxml"));
            Parent view = loader.load();
            AnchorPane parent = (AnchorPane) txtMaHang.getScene().lookup("#contentArea");
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
        txtMaHang.clear();
        cbNhaCungCap.setValue(null);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        txtNgayNhap.setText(sdf.format(new Date()));
        txtTongTien.clear();
        txtNewMaNCC.clear();
        txtNewTenNCC.clear();
        lblMessage.setText("");
        lblMessage.getStyleClass().removeAll("message-error", "message-success");
        lblNccMessage.setText("");
        lblNccMessage.getStyleClass().removeAll("message-error", "message-success");
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
 
    private void showNccError(String msg) {
        lblNccMessage.getStyleClass().removeAll("message-error", "message-success");
        lblNccMessage.getStyleClass().add("message-error");
        lblNccMessage.setText(msg);
    }
 
    private void showNccSuccess(String msg) {
        lblNccMessage.getStyleClass().removeAll("message-error", "message-success");
        lblNccMessage.getStyleClass().add("message-success");
        lblNccMessage.setText(msg);
    }
}
