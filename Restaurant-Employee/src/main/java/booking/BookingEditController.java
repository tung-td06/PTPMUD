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
import model.DatBan;

public class BookingEditController implements Initializable {

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

    private BookingController.BookingModel selectedBooking;
    private ObservableList<String> khachHangIds = FXCollections.observableArrayList();
    private ObservableList<String> banAnIds = FXCollections.observableArrayList();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        selectedBooking = BookingController.selectedBooking;

        cbTrangThai.setItems(
                FXCollections.observableArrayList("Đang chờ", "Đã xác nhận", "Đã nhận bàn", "Đã hủy", "Hoàn thành"));

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

        loadComboData();

        if (selectedBooking != null) {
            txtMaDat.setText(selectedBooking.getId());
            txtSoNguoi.setText(String.valueOf(selectedBooking.getGuests()));
            cbTrangThai.setValue(selectedBooking.getStatus());

            setDateTimeFields(selectedBooking.getTime(), dpTimeVao, cbTimeVaoHour, cbTimeVaoMin);
            setDateTimeFields(selectedBooking.getTimeRa(), dpTimeRa, cbTimeRaHour, cbTimeRaMin);
            setDateTimeFields(selectedBooking.getArrivalTime(), dpThoiGianDen, cbThoiGianDenHour, cbThoiGianDenMin);
        }

        cbTrangThai.valueProperty().addListener((obs, oldVal, newVal) -> {
            if ("Đã nhận bàn".equals(newVal)) {
                java.time.LocalDateTime now = java.time.LocalDateTime.now();
                dpThoiGianDen.setValue(now.toLocalDate());
                cbThoiGianDenHour.setValue(String.format("%02d", now.getHour()));
                cbThoiGianDenMin.setValue(String.format("%02d", now.getMinute()));
            }
        });
    }

    private void setDateTimeFields(String dateTimeStr, DatePicker dp, ComboBox<String> cbHour, ComboBox<String> cbMin) {
        if (dateTimeStr == null || dateTimeStr.trim().isEmpty() || dateTimeStr.equals("Chưa đến")
                || dateTimeStr.equals("Chưa xác nhận")) {
            dp.setValue(null);
            cbHour.setValue(null);
            cbMin.setValue(null);
            return;
        }
        try {
            String[] parts = dateTimeStr.split(" ");
            if (parts.length >= 2) {
                dp.setValue(LocalDate.parse(parts[0]));
                String[] timeParts = parts[1].split(":");
                if (timeParts.length >= 2) {
                    cbHour.setValue(timeParts[0]);
                    cbMin.setValue(timeParts[1]);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadComboData() {
        lblMessage.setText("Đang tải dữ liệu...");
        new Thread(() -> {
            try {
                // Fetch Customers
                Request requestKH = new Request(Module.KHACHHANG, Action.GET_ALL, null);
                Response responseKH = SocketClient.getInstance().sendRequest(requestKH);

                // Fetch Tables
                Request requestBan = new Request(Module.BANAN, Action.GET_ALL, null);
                Response responseBan = SocketClient.getInstance().sendRequest(requestBan);

                // Fetch Booking details for Note field
                Response responseDB = null;
                if (selectedBooking != null) {
                    Request requestDB = new Request(Module.DATBAN, Action.GET_BY_ID, selectedBooking.getId());
                    responseDB = SocketClient.getInstance().sendRequest(requestDB);
                }

                final Response finalResponseDB = responseDB;

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

                        // Select customer
                        if (selectedBooking != null) {
                            for (String item : khNames) {
                                if (item.contains(selectedBooking.getCustomer())) {
                                    cbKhachHang.setValue(item);
                                    break;
                                }
                            }
                        }
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

                        // Select table
                        if (selectedBooking != null) {
                            for (String item : banNames) {
                                if (item.contains(selectedBooking.getTable())) {
                                    cbBanAn.setValue(item);
                                    break;
                                }
                            }
                        }
                    }

                    if (finalResponseDB != null && finalResponseDB.isSuccess() && finalResponseDB.getData() != null) {
                        DatBan db = (DatBan) finalResponseDB.getData();
                        if (db.getNote() != null) {
                            txtNote.setText(db.getNote());
                        }
                        if (db.getThoiGianDen() != null) {
                            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm");
                            setDateTimeFields(sdf.format(db.getThoiGianDen()), dpThoiGianDen, cbThoiGianDenHour,
                                    cbThoiGianDenMin);
                        }
                    }

                    lblMessage.setText("");
                });
            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() -> showError("Lỗi tải thông tin: " + e.getMessage()));
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

        if (madat.isEmpty() || songuoiStr.isEmpty() || dateVao == null || hourVao == null || minVao == null
                || dateRa == null || hourRa == null || minRa == null) {
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

        java.sql.Timestamp timeVao = java.sql.Timestamp
                .valueOf(dateVao.atTime(Integer.parseInt(hourVao), Integer.parseInt(minVao)));
        java.sql.Timestamp timeRa = java.sql.Timestamp
                .valueOf(dateRa.atTime(Integer.parseInt(hourRa), Integer.parseInt(minRa)));

        // Handle arrival time
        java.sql.Timestamp thoiGianDen = null;
        LocalDate dateDen = dpThoiGianDen.getValue();
        String hourDen = cbThoiGianDenHour.getValue();
        String minDen = cbThoiGianDenMin.getValue();
        if (dateDen != null && hourDen != null && minDen != null) {
            thoiGianDen = java.sql.Timestamp
                    .valueOf(dateDen.atTime(Integer.parseInt(hourDen), Integer.parseInt(minDen)));
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

        lblMessage.setText("Đang cập nhật...");
        new Thread(() -> {
            try {
                Request request = new Request(Module.DATBAN, Action.UPDATE, db);
                Response response = SocketClient.getInstance().sendRequest(request);
                Platform.runLater(() -> {
                    if (response != null && response.isSuccess()) {
                        showSuccess("Cập nhật đặt bàn thành công!");
                    } else {
                        showError(response != null ? response.getMessage() : "Cập nhật đặt bàn thất bại!");
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
        if (selectedBooking != null) {
            txtSoNguoi.setText(String.valueOf(selectedBooking.getGuests()));
            cbTrangThai.setValue(selectedBooking.getStatus());
            for (String item : cbKhachHang.getItems()) {
                if (item.contains(selectedBooking.getCustomer())) {
                    cbKhachHang.setValue(item);
                    break;
                }
            }
            for (String item : cbBanAn.getItems()) {
                if (item.contains(selectedBooking.getTable())) {
                    cbBanAn.setValue(item);
                    break;
                }
            }
            setDateTimeFields(selectedBooking.getTime(), dpTimeVao, cbTimeVaoHour, cbTimeVaoMin);
            setDateTimeFields(selectedBooking.getTimeRa(), dpTimeRa, cbTimeRaHour, cbTimeRaMin);
            setDateTimeFields(selectedBooking.getArrivalTime(), dpThoiGianDen, cbThoiGianDenHour, cbThoiGianDenMin);
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
