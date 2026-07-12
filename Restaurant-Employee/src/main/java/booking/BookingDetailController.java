package booking;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;

import booking.BookingController.BookingModel;
import model.BanAn;
import model.DatBan;
import model.KhachHang;
import network.Action;
import network.Module;
import network.Request;
import network.Response;
import network.SocketClient;

@SuppressWarnings("unchecked")
public class BookingDetailController implements Initializable {

    @FXML
    private Label lblSubtitle;
    @FXML
    private Label lblMaDat;
    @FXML
    private Label lblTrangThai;
    @FXML
    private Label lblThoiGianDen;
    @FXML
    private Label lblSoNguoi;
    @FXML
    private Label lblTimeVao;
    @FXML
    private Label lblTimeRa;
    @FXML
    private Label lblGhiChu;

    @FXML
    private Label lblMaKH;
    @FXML
    private Label lblTenKH;
    @FXML
    private Label lblSdtKH;
    @FXML
    private Label lblDiemTichLuyKH;

    @FXML
    private Label lblMaBan;
    @FXML
    private Label lblTenBan;
    @FXML
    private Label lblKhuVuc;
    @FXML
    private Label lblSucChua;
    @FXML
    private Label lblTrangThaiBan;

    @FXML
    private Button btnClose;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        BookingModel selected = BookingController.selectedBooking;
        if (selected == null) {
            return;
        }

        // Bind temporary known data first
        lblSubtitle.setText("Mã đặt: " + selected.getId() + " | Bàn: " + selected.getTable() + " | Khách: " + selected.getCustomer());
        lblMaDat.setText(selected.getId());
        lblTrangThai.setText(selected.getStatus());
        lblThoiGianDen.setText(selected.getArrivalTime());
        lblSoNguoi.setText(selected.getGuests() + " người");
        lblTimeVao.setText(selected.getTime());
        lblTimeRa.setText(selected.getTimeRa().isEmpty() ? "--" : selected.getTimeRa());
        lblGhiChu.setText(selected.getNote().isEmpty() ? "Không có" : selected.getNote());
        
        lblTenKH.setText(selected.getCustomer());
        lblSdtKH.setText(selected.getPhone().isEmpty() ? "--" : selected.getPhone());
        lblTenBan.setText(selected.getTable());

        // Setup status tag colors
        updateStatusStyle(selected.getStatus());

        // Load complete info in background
        loadCompleteDetails(selected);
    }

    private void updateStatusStyle(String status) {
        lblTrangThai.getStyleClass().remove("status-tag");
        lblTrangThai.getStyleClass().remove("status-confirmed");
        lblTrangThai.getStyleClass().remove("status-pending");
        lblTrangThai.getStyleClass().remove("status-cancelled");

        lblTrangThai.getStyleClass().add("status-tag");
        if (status.equalsIgnoreCase("Đã xác nhận") || status.equalsIgnoreCase("Đã nhận bàn") || 
            status.equalsIgnoreCase("Hoàn thành") || status.equalsIgnoreCase("Đang sử dụng") || 
            status.equalsIgnoreCase("Đã hoàn thành")) {
            lblTrangThai.getStyleClass().add("status-confirmed");
        } else if (status.equalsIgnoreCase("Chờ xác nhận") || status.equalsIgnoreCase("Đang chờ")) {
            lblTrangThai.getStyleClass().add("status-pending");
        } else {
            lblTrangThai.getStyleClass().add("status-cancelled");
        }
    }

    private void loadCompleteDetails(BookingModel selected) {
        new Thread(() -> {
            try {
                Request requestKH = new Request(Module.KHACHHANG, Action.GET_ALL, null);
                Response responseKH = SocketClient.getInstance().sendRequest(requestKH);

                Request requestBan = new Request(Module.BANAN, Action.GET_ALL, null);
                Response responseBan = SocketClient.getInstance().sendRequest(requestBan);

                Request requestDB = new Request(Module.DATBAN, Action.GET_ALL, null);
                Response responseDB = SocketClient.getInstance().sendRequest(requestDB);

                Platform.runLater(() -> {
                    if (responseKH != null && responseKH.isSuccess() &&
                        responseBan != null && responseBan.isSuccess() &&
                        responseDB != null && responseDB.isSuccess()) {

                        List<KhachHang> khList = (List<KhachHang>) responseKH.getData();
                        List<BanAn> banList = (List<BanAn>) responseBan.getData();
                        List<DatBan> dbList = (List<DatBan>) responseDB.getData();

                        DatBan targetDB = null;
                        for (DatBan db : dbList) {
                            if (db.getMaDatBan().equalsIgnoreCase(selected.getId())) {
                                targetDB = db;
                                break;
                            }
                        }

                        if (targetDB != null) {
                            final DatBan finalDB = targetDB;
                            KhachHang targetKH = null;
                            for (KhachHang kh : khList) {
                                if (kh.getMaKH().equalsIgnoreCase(finalDB.getMaKH())) {
                                    targetKH = kh;
                                    break;
                                }
                            }

                            BanAn targetBan = null;
                            for (BanAn ba : banList) {
                                if (ba.getMaBan().equalsIgnoreCase(finalDB.getMaBan())) {
                                    targetBan = ba;
                                    break;
                                }
                            }

                            if (targetKH != null) {
                                lblMaKH.setText(targetKH.getMaKH());
                                lblTenKH.setText(targetKH.getTenKH());
                                lblSdtKH.setText(targetKH.getSdt());
                                lblDiemTichLuyKH.setText(String.valueOf(targetKH.getDiemTichLuy()));
                            }

                            if (targetBan != null) {
                                lblMaBan.setText(targetBan.getMaBan());
                                lblTenBan.setText(targetBan.getTenBan());
                                lblKhuVuc.setText(targetBan.getKhuVuc());
                                lblSucChua.setText("4 người");
                                lblTrangThaiBan.setText(targetBan.getTrangThai());
                            }
                        } else {
                            showAlert("Không tìm thấy thông tin đặt bàn khớp trên server!");
                        }
                    } else {
                        showAlert("Không thể tải thông tin chi tiết đầy đủ từ server!");
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() -> showAlert("Lỗi kết nối hoặc xử lý dữ liệu: " + e.getMessage()));
            }
        }).start();
    }

    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Thông báo");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @FXML
    private void handleClose() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/booking.fxml"));
            Parent view = loader.load();
            AnchorPane parent = (AnchorPane) btnClose.getScene().lookup("#contentArea");
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
            showAlert("Không thể quay lại màn hình danh sách: " + e.getMessage());
        }
    }
}
