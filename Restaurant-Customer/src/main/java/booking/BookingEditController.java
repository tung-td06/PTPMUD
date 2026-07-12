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
    private DatePicker dpNgay;
    @FXML
    private ComboBox<String> cbTrangThai;
    @FXML
    private TextArea txtNote;
    @FXML
    private Label lblMessage;
    @FXML
    private Label lblThoiGianDen;

    private BookingController.BookingModel selectedBooking;
    private ObservableList<String> khachHangIds = FXCollections.observableArrayList();
    private ObservableList<String> banAnIds = FXCollections.observableArrayList();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        selectedBooking = BookingController.selectedBooking;

        cbTrangThai.setItems(FXCollections.observableArrayList("Chờ xác nhận", "Đã xác nhận"));
        loadComboData();

        if (selectedBooking != null) {
            txtMaDat.setText(selectedBooking.getId());
            txtSoNguoi.setText(String.valueOf(selectedBooking.getGuests()));
            cbTrangThai.setValue(selectedBooking.getStatus());

            // Resolve date
            try {
                String dateTime = selectedBooking.getTime();
                if (dateTime != null && dateTime.length() >= 10) {
                    dpNgay.setValue(LocalDate.parse(dateTime.substring(0, 10)));
                }
            } catch (Exception e) {
                dpNgay.setValue(LocalDate.now());
            }
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
                        // Hiển thị thời gian đến (readonly)
                        if (lblThoiGianDen != null) {
                            if (db.getThoiGianDen() != null) {
                                java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm");
                                lblThoiGianDen.setText(sdf.format(db.getThoiGianDen()));
                            } else {
                                lblThoiGianDen.setText("Chưa đến");
                            }
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
        LocalDate ngay = dpNgay.getValue();

        if (madat.isEmpty() || songuoiStr.isEmpty() || ngay == null) {
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

        String trangThaiStr = cbTrangThai.getValue() != null ? cbTrangThai.getValue() : "Chờ xác nhận";
        String trangthaiVal = "Đang chờ";
        if ("Đã xác nhận".equals(trangThaiStr)) {
            trangthaiVal = "Đã nhận bàn";
        } else if ("Đã hủy".equals(trangThaiStr)) {
            trangthaiVal = "Đã hủy";
        }

        java.sql.Timestamp timeVao = java.sql.Timestamp.valueOf(ngay.atTime(19, 0));
        java.sql.Timestamp timeRa = java.sql.Timestamp.valueOf(ngay.atTime(21, 0));

        String noteVal = txtNote.getText() != null ? txtNote.getText().trim() : "";

        model.DatBan db = new model.DatBan(
                madat,
                makh,
                maban,
                timeVao,
                timeRa,
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
            try {
                String dateTime = selectedBooking.getTime();
                if (dateTime != null && dateTime.length() >= 10) {
                    dpNgay.setValue(LocalDate.parse(dateTime.substring(0, 10)));
                }
            } catch (Exception e) {
                dpNgay.setValue(LocalDate.now());
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
