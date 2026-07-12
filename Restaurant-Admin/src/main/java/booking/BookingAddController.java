package booking;

import java.net.URL;
import java.time.LocalDate;

import java.util.List;
import java.util.ResourceBundle;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import network.Action;
import network.Module;
import network.Request;
import network.Response;
import network.SocketClient;
import model.KhachHang;
import model.BanAn;

public class BookingAddController implements Initializable {

    @FXML
    private TextField txtMaDat;
    @FXML
    private ComboBox<String> cbKhachHang;
    @FXML
    private ComboBox<String> cbBanAn;
    @FXML
    private TextField txtSoNguoi;
    @FXML
    private DatePicker dpTimeVao;
    @FXML
    private ComboBox<String> cbTimeVaoHour;
    @FXML
    private ComboBox<String> cbTimeVaoMin;
    @FXML
    private DatePicker dpTimeRa;
    @FXML
    private ComboBox<String> cbTimeRaHour;
    @FXML
    private ComboBox<String> cbTimeRaMin;
    @FXML
    private DatePicker dpThoiGianDen;
    @FXML
    private ComboBox<String> cbThoiGianDenHour;
    @FXML
    private ComboBox<String> cbThoiGianDenMin;
    @FXML
    private ComboBox<String> cbTrangThai;
    @FXML
    private TextArea txtNote;
    @FXML
    private Label lblMessage;

    private ObservableList<String> khachHangIds = FXCollections.observableArrayList();
    private ObservableList<String> banAnIds = FXCollections.observableArrayList();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        cbTrangThai.setItems(FXCollections.observableArrayList("Đang chờ", "Đã xác nhận", "Đã nhận bàn", "Đã hủy", "Hoàn thành"));
        cbTrangThai.setValue("Đang chờ");

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
        cbThoiGianDenHour.setItems(hours);
        cbThoiGianDenMin.setItems(minutes);

        // Set default values
        dpTimeVao.setValue(LocalDate.now());
        cbTimeVaoHour.setValue("19");
        cbTimeVaoMin.setValue("00");

        dpTimeRa.setValue(LocalDate.now());
        cbTimeRaHour.setValue("21");
        cbTimeRaMin.setValue("00");

        loadComboData();
    }

    private void loadComboData() {
        lblMessage.setText("Đang tải dữ liệu từ Server...");
        new Thread(() -> {
            try {
                // Fetch Customers
                Request requestKH = new Request(Module.KHACHHANG, Action.GET_ALL, null);
                Response responseKH = SocketClient.getInstance().sendRequest(requestKH);

                // Fetch Tables
                Request requestBan = new Request(Module.BANAN, Action.GET_ALL, null);
                Response responseBan = SocketClient.getInstance().sendRequest(requestBan);

                Platform.runLater(() -> {
                    if (responseKH != null && responseKH.isSuccess()) {
                        List<KhachHang> serverKHList = (List<KhachHang>) responseKH.getData();
                        khachHangIds.clear();
                        ObservableList<String> khNames = FXCollections.observableArrayList();
                        if (serverKHList != null) {
                            for (KhachHang kh : serverKHList) {
                                khachHangIds.add(kh.getMaKH());
                                khNames.add(kh.getMaKH() + " - " + kh.getTenKH());
                            }
                        }
                        cbKhachHang.setItems(khNames);
                    }

                    if (responseBan != null && responseBan.isSuccess()) {
                        List<BanAn> serverBanList = (List<BanAn>) responseBan.getData();
                        banAnIds.clear();
                        ObservableList<String> banNames = FXCollections.observableArrayList();
                        if (serverBanList != null) {
                            for (BanAn ba : serverBanList) {
                                banAnIds.add(ba.getMaBan());
                                banNames.add(ba.getMaBan() + " - " + ba.getTenBan() + " (" + ba.getTrangThai() + ")");
                            }
                        }
                        cbBanAn.setItems(banNames);
                    }
                    lblMessage.setText("");
                });
            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() -> showError("Lỗi tải danh mục: " + e.getMessage()));
            }
        }).start();
    }

    @FXML
    private void handleSave(ActionEvent event) {
        String madat = txtMaDat.getText().trim();
        String songuoiStr = txtSoNguoi.getText().trim();

        LocalDate dateVao = dpTimeVao.getValue();
        String hourVao = cbTimeVaoHour.getValue();
        String minVao = cbTimeVaoMin.getValue();

        LocalDate dateRa = dpTimeRa.getValue();
        String hourRa = cbTimeRaHour.getValue();
        String minRa = cbTimeRaMin.getValue();

        if (madat.isEmpty() || songuoiStr.isEmpty() || dateVao == null || hourVao == null || minVao == null || dateRa == null || hourRa == null || minRa == null) {
            showError("Vui lòng nhập đầy đủ các trường bắt buộc (*)");
            return;
        }

        int songuoi;
        try {
            songuoi = Integer.parseInt(songuoiStr);
            if (songuoi <= 0) {
                showError("Số lượng người phải > 0");
                return;
            }
        } catch (NumberFormatException e) {
            showError("Số lượng người phải là số nguyên");
            return;
        }

        int khIdx = cbKhachHang.getSelectionModel().getSelectedIndex();
        if (khIdx < 0) {
            showError("Vui lòng chọn khách hàng");
            return;
        }
        String makh = khachHangIds.get(khIdx);

        int banIdx = cbBanAn.getSelectionModel().getSelectedIndex();
        if (banIdx < 0) {
            showError("Vui lòng chọn bàn ăn");
            return;
        }
        String maban = banAnIds.get(banIdx);

        String trangthaiVal = cbTrangThai.getValue() != null ? cbTrangThai.getValue() : "Đang chờ";

        java.sql.Timestamp timeVao = java.sql.Timestamp.valueOf(dateVao.atTime(Integer.parseInt(hourVao), Integer.parseInt(minVao)));
        java.sql.Timestamp timeRa = java.sql.Timestamp.valueOf(dateRa.atTime(Integer.parseInt(hourRa), Integer.parseInt(minRa)));

        // Handle arrival time
        java.sql.Timestamp thoiGianDen = null;
        LocalDate dateDen = dpThoiGianDen.getValue();
        String hourDen = cbThoiGianDenHour.getValue();
        String minDen = cbThoiGianDenMin.getValue();
        if (dateDen != null && hourDen != null && minDen != null) {
            thoiGianDen = java.sql.Timestamp.valueOf(dateDen.atTime(Integer.parseInt(hourDen), Integer.parseInt(minDen)));
        }

        String noteVal = txtNote.getText() != null ? txtNote.getText().trim() : "";

        model.DatBan db = new model.DatBan(
                madat,
                makh,
                maban,
                timeVao,
                timeRa,
                thoiGianDen,
                songuoi,
                noteVal,
                trangthaiVal);

        lblMessage.setText("Đang gửi yêu cầu...");
        new Thread(() -> {
            try {
                Request request = new Request(Module.DATBAN, Action.ADD, db);
                Response response = SocketClient.getInstance().sendRequest(request);
                Platform.runLater(() -> {
                    if (response != null && response.isSuccess()) {
                        showSuccess("Đặt bàn thành công!");
                        clearForm();
                    } else {
                        showError(response != null ? response.getMessage()
                                : "Đặt bàn thất bại hoặc bàn đã bị đặt trùng khung giờ!");
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() -> showError("Lỗi kết nối: " + e.getMessage()));
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
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/booking.fxml"));
            Parent view = loader.load();
            AnchorPane parent = (AnchorPane) txtMaDat.getScene().lookup("#contentArea");
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
        txtMaDat.clear();
        txtSoNguoi.clear();
        txtNote.clear();
        cbKhachHang.setValue(null);
        cbBanAn.setValue(null);
        dpTimeVao.setValue(LocalDate.now());
        cbTimeVaoHour.setValue("19");
        cbTimeVaoMin.setValue("00");
        dpTimeRa.setValue(LocalDate.now());
        cbTimeRaHour.setValue("21");
        cbTimeRaMin.setValue("00");
        dpThoiGianDen.setValue(null);
        cbThoiGianDenHour.setValue(null);
        cbThoiGianDenMin.setValue(null);
        cbTrangThai.setValue("Đang chờ");
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
