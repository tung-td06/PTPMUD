package bill;

import java.math.BigDecimal;
import java.net.URL;
import java.sql.Timestamp;
import java.time.LocalDate;
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
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import model.HoaDon;
import model.KhachHang;
import model.BanAn;
import model.NhanVien;
import network.Action;
import network.Module;
import network.Request;
import network.Response;
import network.SocketClient;

public class BillEditController implements Initializable {

    @FXML private TextField txtMaHD;
    @FXML private ComboBox<String> cbKhachHang;
    @FXML private ComboBox<String> cbBanAn;
    @FXML private ComboBox<String> cbNhanVien;
    @FXML private DatePicker dpTimeVao;
    @FXML private ComboBox<String> cbTimeVaoHour;
    @FXML private ComboBox<String> cbTimeVaoMin;
    @FXML private DatePicker dpTimeRa;
    @FXML private ComboBox<String> cbTimeRaHour;
    @FXML private ComboBox<String> cbTimeRaMin;
    @FXML private TextField txtTongTien;
    @FXML private TextField txtGiamGia;
    @FXML private ComboBox<String> cbTrangThai;
    @FXML private Label lblMessage;

    @FXML private Button saveBtn;
    @FXML private Button cancelBtn;
    @FXML private Button backBtn;

    private BillController.BillModel selectedBill;
    private final ObservableList<String> khachHangIds = FXCollections.observableArrayList();
    private final ObservableList<String> banAnIds = FXCollections.observableArrayList();
    private final ObservableList<String> nhanVienIds = FXCollections.observableArrayList();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        selectedBill = BillController.selectedBill;

        cbTrangThai.setItems(FXCollections.observableArrayList(
            "Đã thanh toán", "Đang xử lý", "Chờ thanh toán", "Đã hủy"
        ));

        ObservableList<String> hours = FXCollections.observableArrayList();
        for (int i = 0; i < 24; i++) {
            hours.add(String.format("%02d", i));
        }
        ObservableList<String> minutes = FXCollections.observableArrayList();
        for (int i = 0; i < 60; i++) {
            minutes.add(String.format("%02d", i));
        }
        cbTimeVaoHour.setItems(hours);
        cbTimeVaoMin.setItems(minutes);
        cbTimeRaHour.setItems(hours);
        cbTimeRaMin.setItems(minutes);

        loadComboData();
    }

    private void loadComboData() {
        setLoadingState(true);
        new Thread(() -> {
            try {
                // Fetch Customers
                Request khReq = new Request(Module.KHACHHANG, Action.GET_ALL, null);
                Response khRes = SocketClient.getInstance().sendRequest(khReq);
                ObservableList<String> khNames = FXCollections.observableArrayList();
                ObservableList<String> khIdsTemp = FXCollections.observableArrayList();
                if (khRes != null && khRes.isSuccess()) {
                    List<KhachHang> khList = (List<KhachHang>) khRes.getData();
                    if (khList != null) {
                        for (KhachHang kh : khList) {
                            khIdsTemp.add(kh.getMaKH());
                            khNames.add(kh.getMaKH() + " - " + kh.getTenKH());
                        }
                    }
                }

                // Fetch Tables
                Request banReq = new Request(Module.BANAN, Action.GET_ALL, null);
                Response banRes = SocketClient.getInstance().sendRequest(banReq);
                ObservableList<String> banNames = FXCollections.observableArrayList();
                ObservableList<String> banIdsTemp = FXCollections.observableArrayList();
                if (banRes != null && banRes.isSuccess()) {
                    List<BanAn> banList = (List<BanAn>) banRes.getData();
                    if (banList != null) {
                        for (BanAn ban : banList) {
                            banIdsTemp.add(ban.getMaBan());
                            banNames.add(ban.getMaBan() + " - " + ban.getTenBan());
                        }
                    }
                }

                // Fetch Employees
                Request nvReq = new Request(Module.NHANVIEN, Action.GET_ALL, null);
                Response nvRes = SocketClient.getInstance().sendRequest(nvReq);
                ObservableList<String> nvNames = FXCollections.observableArrayList();
                ObservableList<String> nvIdsTemp = FXCollections.observableArrayList();
                if (nvRes != null && nvRes.isSuccess()) {
                    List<NhanVien> nvList = (List<NhanVien>) nvRes.getData();
                    if (nvList != null) {
                        for (NhanVien nv : nvList) {
                            nvIdsTemp.add(nv.getMaNV());
                            nvNames.add(nv.getMaNV() + " - " + nv.getHoTen());
                        }
                    }
                }

                Platform.runLater(() -> {
                    khachHangIds.setAll(khIdsTemp);
                    cbKhachHang.setItems(khNames);

                    banAnIds.setAll(banIdsTemp);
                    cbBanAn.setItems(banNames);

                    nhanVienIds.setAll(nvIdsTemp);
                    cbNhanVien.setItems(nvNames);

                    // Populate fields now that Combo list data is loaded
                    if (selectedBill != null) {
                        txtMaHD.setText(selectedBill.getId());
                        txtTongTien.setText(selectedBill.getTongTien() != null ? selectedBill.getTongTien().toPlainString() : "0");
                        txtGiamGia.setText(selectedBill.getGiamGia() != null ? selectedBill.getGiamGia().toPlainString() : "0");
                        cbTrangThai.setValue(selectedBill.getStatus());

                        if (selectedBill.getTimeVao() != null) {
                            dpTimeVao.setValue(selectedBill.getTimeVao().toLocalDateTime().toLocalDate());
                            cbTimeVaoHour.setValue(String.format("%02d", selectedBill.getTimeVao().toLocalDateTime().getHour()));
                            cbTimeVaoMin.setValue(String.format("%02d", selectedBill.getTimeVao().toLocalDateTime().getMinute()));
                        }
                        if (selectedBill.getTimeRa() != null) {
                            dpTimeRa.setValue(selectedBill.getTimeRa().toLocalDateTime().toLocalDate());
                            cbTimeRaHour.setValue(String.format("%02d", selectedBill.getTimeRa().toLocalDateTime().getHour()));
                            cbTimeRaMin.setValue(String.format("%02d", selectedBill.getTimeRa().toLocalDateTime().getMinute()));
                        }

                        // Select Customer
                        for (int i = 0; i < khachHangIds.size(); i++) {
                            if (khachHangIds.get(i).equals(selectedBill.getMaKH())) {
                                cbKhachHang.getSelectionModel().select(i);
                                break;
                            }
                        }

                        // Select Table
                        for (int i = 0; i < banAnIds.size(); i++) {
                            if (banAnIds.get(i).equals(selectedBill.getMaBan())) {
                                cbBanAn.getSelectionModel().select(i);
                                break;
                            }
                        }

                        // Select Employee
                        for (int i = 0; i < nhanVienIds.size(); i++) {
                            if (nhanVienIds.get(i).equals(selectedBill.getMaNV())) {
                                cbNhanVien.getSelectionModel().select(i);
                                break;
                            }
                        }
                    }

                    setLoadingState(false);
                });
            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() -> {
                    showError("Lỗi tải thông tin ComboBox: " + e.getMessage());
                    setLoadingState(false);
                });
            }
        }).start();
    }

    private void setLoadingState(boolean loading) {
        if (saveBtn != null) saveBtn.setDisable(loading);
        if (cancelBtn != null) cancelBtn.setDisable(loading);
        if (backBtn != null) backBtn.setDisable(loading);
        cbKhachHang.setDisable(loading);
        cbBanAn.setDisable(loading);
        cbNhanVien.setDisable(loading);
        dpTimeVao.setDisable(loading);
        cbTimeVaoHour.setDisable(loading);
        cbTimeVaoMin.setDisable(loading);
        dpTimeRa.setDisable(loading);
        cbTimeRaHour.setDisable(loading);
        cbTimeRaMin.setDisable(loading);
        txtTongTien.setDisable(loading);
        txtGiamGia.setDisable(loading);
        cbTrangThai.setDisable(loading);
    }

    @FXML
    private void handleSave(ActionEvent event) {
        String mahd = txtMaHD.getText().trim();
        if (mahd.isEmpty()) {
            showError("Mã hóa đơn bắt buộc!");
            return;
        }

        int khIdx = cbKhachHang.getSelectionModel().getSelectedIndex();
        if (khIdx < 0) {
            showError("Vui lòng chọn khách hàng!");
            return;
        }
        String makh = khachHangIds.get(khIdx);

        int banIdx = cbBanAn.getSelectionModel().getSelectedIndex();
        if (banIdx < 0) {
            showError("Vui lòng chọn bàn ăn!");
            return;
        }
        String maban = banAnIds.get(banIdx);

        int nvIdx = cbNhanVien.getSelectionModel().getSelectedIndex();
        if (nvIdx < 0) {
            showError("Vui lòng chọn nhân viên phục vụ!");
            return;
        }
        String manv = nhanVienIds.get(nvIdx);

        LocalDate dateVao = dpTimeVao.getValue();
        LocalDate dateRa = dpTimeRa.getValue();

        Timestamp timeVao;
        if (dateVao != null) {
            String hourStr = cbTimeVaoHour.getValue() != null ? cbTimeVaoHour.getValue() : "00";
            String minStr = cbTimeVaoMin.getValue() != null ? cbTimeVaoMin.getValue() : "00";
            int hour = Integer.parseInt(hourStr);
            int min = Integer.parseInt(minStr);
            timeVao = Timestamp.valueOf(dateVao.atTime(hour, min, 0));
        } else {
            timeVao = new Timestamp(System.currentTimeMillis());
        }

        Timestamp timeRa = null;
        if (dateRa != null) {
            String hourStr = cbTimeRaHour.getValue() != null ? cbTimeRaHour.getValue() : "23";
            String minStr = cbTimeRaMin.getValue() != null ? cbTimeRaMin.getValue() : "59";
            int hour = Integer.parseInt(hourStr);
            int min = Integer.parseInt(minStr);
            timeRa = Timestamp.valueOf(dateRa.atTime(hour, min, 0));
            if (timeVao != null && timeRa.before(timeVao)) {
                showError("Thời gian ra phải sau thời gian vào!");
                return;
            }
        }

        BigDecimal tongTien = BigDecimal.ZERO;
        String txtTong = txtTongTien.getText().trim();
        if (!txtTong.isEmpty()) {
            try {
                tongTien = new BigDecimal(txtTong);
                if (tongTien.compareTo(BigDecimal.ZERO) < 0) {
                    showError("Tổng tiền phải lớn hơn hoặc bằng 0!");
                    return;
                }
            } catch (NumberFormatException e) {
                showError("Tổng tiền phải là một số hợp lệ!");
                return;
            }
        }

        BigDecimal giamGia = BigDecimal.ZERO;
        String txtGiam = txtGiamGia.getText().trim();
        if (!txtGiam.isEmpty()) {
            try {
                giamGia = new BigDecimal(txtGiam);
                if (giamGia.compareTo(BigDecimal.ZERO) < 0) {
                    showError("Giảm giá phải lớn hơn hoặc bằng 0!");
                    return;
                }
            } catch (NumberFormatException e) {
                showError("Giảm giá phải là một số hợp lệ!");
                return;
            }
        }

        if (tongTien.compareTo(giamGia) < 0) {
            showError("Giảm giá không được vượt quá tổng tiền!");
            return;
        }

        String trangThai = cbTrangThai.getValue();
        if (trangThai == null || trangThai.trim().isEmpty()) {
            showError("Vui lòng chọn trạng thái hóa đơn!");
            return;
        }

        setLoadingState(true);
        final Timestamp finalTimeVao = timeVao;
        final Timestamp finalTimeRa = timeRa;
        final BigDecimal finalTongTien = tongTien;
        final BigDecimal finalGiamGia = giamGia;

        new Thread(() -> {
            try {
                HoaDon hd = new HoaDon();
                hd.setMaHD(mahd);
                hd.setMaKH(makh);
                hd.setMaBan(maban);
                hd.setMaNV(manv);
                hd.setTimeVao(finalTimeVao);
                hd.setTimeRa(finalTimeRa);
                hd.setTongTien(finalTongTien);
                hd.setGiamGia(finalGiamGia);
                hd.setThanhToan(finalTongTien.subtract(finalGiamGia));
                hd.setTrangThai(trangThai);

                Request updateReq = new Request(Module.HOADON, Action.UPDATE, hd);
                Response updateRes = SocketClient.getInstance().sendRequest(updateReq);

                Platform.runLater(() -> {
                    if (updateRes != null && updateRes.isSuccess()) {
                        showSuccess("Cập nhật hóa đơn thành công!");
                    } else {
                        showError(updateRes != null ? updateRes.getMessage() : "Không thể cập nhật hóa đơn trên Server!");
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
        if (selectedBill != null) {
            txtTongTien.setText(selectedBill.getTongTien() != null ? selectedBill.getTongTien().toPlainString() : "0");
            txtGiamGia.setText(selectedBill.getGiamGia() != null ? selectedBill.getGiamGia().toPlainString() : "0");
            cbTrangThai.setValue(selectedBill.getStatus());

            if (selectedBill.getTimeVao() != null) {
                dpTimeVao.setValue(selectedBill.getTimeVao().toLocalDateTime().toLocalDate());
                cbTimeVaoHour.setValue(String.format("%02d", selectedBill.getTimeVao().toLocalDateTime().getHour()));
                cbTimeVaoMin.setValue(String.format("%02d", selectedBill.getTimeVao().toLocalDateTime().getMinute()));
            } else {
                dpTimeVao.setValue(null);
                cbTimeVaoHour.setValue(null);
                cbTimeVaoMin.setValue(null);
            }
            if (selectedBill.getTimeRa() != null) {
                dpTimeRa.setValue(selectedBill.getTimeRa().toLocalDateTime().toLocalDate());
                cbTimeRaHour.setValue(String.format("%02d", selectedBill.getTimeRa().toLocalDateTime().getHour()));
                cbTimeRaMin.setValue(String.format("%02d", selectedBill.getTimeRa().toLocalDateTime().getMinute()));
            } else {
                dpTimeRa.setValue(null);
                cbTimeRaHour.setValue(null);
                cbTimeRaMin.setValue(null);
            }

            for (int i = 0; i < khachHangIds.size(); i++) {
                if (khachHangIds.get(i).equals(selectedBill.getMaKH())) {
                    cbKhachHang.getSelectionModel().select(i);
                    break;
                }
            }

            for (int i = 0; i < banAnIds.size(); i++) {
                if (banAnIds.get(i).equals(selectedBill.getMaBan())) {
                    cbBanAn.getSelectionModel().select(i);
                    break;
                }
            }

            for (int i = 0; i < nhanVienIds.size(); i++) {
                if (nhanVienIds.get(i).equals(selectedBill.getMaNV())) {
                    cbNhanVien.getSelectionModel().select(i);
                    break;
                }
            }

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
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/bill.fxml"));
            Parent view = loader.load();
            AnchorPane parent = (AnchorPane) txtMaHD.getScene().lookup("#contentArea");
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

