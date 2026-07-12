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
import javafx.fxml.Initializable;
import javafx.scene.Parent;
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
 
public class ImportEditController implements Initializable {
 
    @FXML private TextField txtMaHang;
    @FXML private ComboBox<String> cbNhaCungCap;
    @FXML private TextField txtNgayNhap;
    @FXML private TextField txtTongTien;
    @FXML private Label lblMessage;
 
    private ImportController.ImportModel selectedImport;
 
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        selectedImport = ImportController.selectedImport;
        txtMaHang.setDisable(true);
        loadSuppliersAndPreFill();
    }
 
    private void loadSuppliersAndPreFill() {
        new Thread(() -> {
            try {
                Request req = new Request(Module.NHACUNGCAP, Action.GET_ALL, null);
                Response res = SocketClient.getInstance().sendRequest(req);
                if (res != null && res.isSuccess()) {
                    List<NhaCungCap> list = (List<NhaCungCap>) res.getData();
                    Platform.runLater(() -> {
                        ObservableList<String> items = FXCollections.observableArrayList();
                        if (list != null) {
                            for (NhaCungCap ncc : list) {
                                items.add(ncc.getMaNCC());
                            }
                        }
                        cbNhaCungCap.setItems(items);
                        
                        if (selectedImport != null) {
                            txtMaHang.setText(selectedImport.getMahang());
                            cbNhaCungCap.setValue(selectedImport.getMancc());
                            txtNgayNhap.setText(selectedImport.getNgaynhap());
                            txtTongTien.setText(String.format("%.0f", selectedImport.getTongtien()));
                        }
                    });
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }
 
    @FXML
    private void handleSave(ActionEvent event) {
        String mahang = txtMaHang.getText().trim();
        String mancc = cbNhaCungCap.getValue();
        String ngaynhap = txtNgayNhap.getText().trim();
        String tongtienStr = txtTongTien.getText().trim();
 
        if (mahang.isEmpty() || mancc == null || ngaynhap.isEmpty() || tongtienStr.isEmpty()) {
            showError("Vui lòng điền đầy đủ các thông tin bắt buộc (*)");
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
 
        new Thread(() -> {
            try {
                NhapHang nh = new NhapHang(mahang, mancc, ngaynhapTimestamp, tongTienBd);
                Request req = new Request(Module.NHAPHANG, Action.UPDATE, nh);
                Response res = SocketClient.getInstance().sendRequest(req);
                if (res != null && res.isSuccess()) {
                    Platform.runLater(() -> {
                        showSuccess("Cập nhật phiếu nhập hàng thành công!");
                        ImportController.selectedImport = new ImportController.ImportModel(mahang, mancc, ngaynhap, tongTienBd.doubleValue());
                    });
                } else {
                    Platform.runLater(() -> showError(res != null ? res.getMessage() : "Cập nhật thất bại. Không nhận được phản hồi từ Server!"));
                }
            } catch (Exception e) {
                Platform.runLater(() -> showError("Lỗi: " + e.getMessage()));
                e.printStackTrace();
            }
        }).start();
    }
 
    @FXML
    private void handleCancel(ActionEvent event) {
        if (selectedImport != null) {
            cbNhaCungCap.setValue(selectedImport.getMancc());
            txtNgayNhap.setText(selectedImport.getNgaynhap());
            txtTongTien.setText(String.format("%.0f", selectedImport.getTongtien()));
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
