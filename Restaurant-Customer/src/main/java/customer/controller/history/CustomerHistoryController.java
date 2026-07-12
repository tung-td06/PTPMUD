package customer.controller.history;

import customer.controller.component.Dialogs;
import customer.controller.component.EmptyState;
import customer.controller.util.CustomerSession;
import dashboard.DashboardController;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import model.BanAn;
import model.DatBan;
import model.HoaDon;
import model.MonAn;
import model.ChiTietHD;
import javafx.scene.control.ScrollPane;
import network.Action;
import network.Module;
import network.Request;
import network.Response;
import network.SocketClient;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.ResourceBundle;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ChoiceDialog;
import javafx.scene.control.TextField;
import javafx.scene.control.PasswordField;
import javafx.geometry.Insets;
import javafx.scene.layout.GridPane;
import javafx.scene.shape.Rectangle;
import javafx.scene.paint.Color;
import javafx.event.ActionEvent;

public class CustomerHistoryController implements Initializable {

    @FXML
    private Button btnBills;

    @FXML
    private Button btnBookings;

    @FXML
    private DatePicker dpFilterDate;

    @FXML
    private ComboBox<String> cmbFilterMonth;

    @FXML
    private ComboBox<String> cmbFilterStatus;

    @FXML
    private TextField txtSearchCode;

    @FXML
    private VBox historyContainer;

    private boolean isBillsSegment = true;
    private final List<HoaDon> masterBills = new ArrayList<>();
    private final List<DatBan> masterBookings = new ArrayList<>();
    private final Map<String, String> tableMap = new HashMap<>(); // maBan -> tenBan
    private final SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        setupFilterOptions();
        loadHistoryData();
    }

    private void setupFilterOptions() {
        // Month filter
        List<String> months = new ArrayList<>();
        months.add("Tất cả tháng");
        for (int i = 1; i <= 12; i++) {
            months.add("Tháng " + i);
        }
        cmbFilterMonth.setItems(FXCollections.observableArrayList(months));
        cmbFilterMonth.setValue("Tất cả tháng");

        // Status filter
        updateStatusFilterItems();

        // Listeners
        txtSearchCode.textProperty().addListener((o, ov, nv) -> applyFilters());
        dpFilterDate.valueProperty().addListener((o, ov, nv) -> applyFilters());
        cmbFilterMonth.valueProperty().addListener((o, ov, nv) -> applyFilters());
        cmbFilterStatus.valueProperty().addListener((o, ov, nv) -> applyFilters());
    }

    private void updateStatusFilterItems() {
        List<String> statuses = new ArrayList<>();
        statuses.add("Tất cả trạng thái");
        if (isBillsSegment) {
            statuses.addAll(List.of("Đã thanh toán", "Chưa thanh toán"));
        } else {
            statuses.addAll(List.of("Chờ xác nhận", "Đã xác nhận", "Đã hủy"));
        }
        cmbFilterStatus.setItems(FXCollections.observableArrayList(statuses));
        cmbFilterStatus.setValue("Tất cả trạng thái");
    }

    private void loadHistoryData() {
        new Thread(() -> {
            try {
                String maKH = CustomerSession.getCurrentCustomer().getMaKH();

                // 1. Lấy bảng lookup tenBan
                Request tableReq = new Request(Module.BANAN, Action.GET_ALL, null);
                Response tableRes = SocketClient.getInstance().sendRequest(tableReq);
                if (tableRes != null && tableRes.isSuccess() && tableRes.getData() != null) {
                    List<BanAn> banAns = (List<BanAn>) tableRes.getData();
                    for (BanAn b : banAns) {
                        tableMap.put(b.getMaBan(), b.getTenBan());
                    }
                }

                // 2. Lấy hóa đơn của KH từ server
                Request billReq = new Request(Module.HOADON, "GET_BY_KHACHHANG", maKH);
                Response billRes = SocketClient.getInstance().sendRequest(billReq);
                masterBills.clear();
                if (billRes != null && billRes.isSuccess() && billRes.getData() != null) {
                    List<HoaDon> bills = (List<HoaDon>) billRes.getData();
                    masterBills.addAll(bills);
                }

                // 3. Lấy đặt bàn của KH từ server
                Request bookingReq = new Request(Module.DATBAN, "GET_BY_KHACHHANG", maKH);
                Response bookingRes = SocketClient.getInstance().sendRequest(bookingReq);
                masterBookings.clear();
                if (bookingRes != null && bookingRes.isSuccess() && bookingRes.getData() != null) {
                    List<DatBan> bookings = (List<DatBan>) bookingRes.getData();
                    masterBookings.addAll(bookings);
                }

                Platform.runLater(this::applyFilters);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void applyFilters() {
        if (historyContainer == null) return;
        historyContainer.getChildren().clear();

        LocalDate dayFilter = dpFilterDate.getValue();
        String monthFilter = cmbFilterMonth.getValue();
        String statusFilter = cmbFilterStatus.getValue();
        String searchVal = txtSearchCode != null ? txtSearchCode.getText().trim().toLowerCase() : "";

        int monthVal = 0;
        if (monthFilter != null && monthFilter.startsWith("Tháng ")) {
            monthVal = Integer.parseInt(monthFilter.replace("Tháng ", "").trim());
        }

        if (isBillsSegment) {
            List<HoaDon> filtered = new ArrayList<>();
            for (HoaDon hd : masterBills) {
                if (hd.getTimeVao() == null) continue;
                LocalDate date = hd.getTimeVao().toLocalDateTime().toLocalDate();

                // Search Code
                if (!searchVal.isEmpty() && hd.getMaHD() != null && !hd.getMaHD().toLowerCase().contains(searchVal)) {
                    continue;
                }

                // Day
                if (dayFilter != null && !date.isEqual(dayFilter)) continue;

                // Month
                if (monthVal > 0 && hd.getTimeVao().toLocalDateTime().getMonthValue() != monthVal) continue;

                // Status
                if (statusFilter != null && !statusFilter.equals("Tất cả trạng thái") && !hd.getTrangThai().equalsIgnoreCase(statusFilter)) {
                    continue;
                }

                filtered.add(hd);
            }
            renderBills(filtered);
        } else {
            List<DatBan> filtered = new ArrayList<>();
            for (DatBan db : masterBookings) {
                // Search Code
                if (!searchVal.isEmpty() && db.getMaDatBan() != null && !db.getMaDatBan().toLowerCase().contains(searchVal)) {
                    continue;
                }

                // Dùng thoiGianDen (thời điểm đặt dự kiến) để lọc,
                // timeVao chỉ được set khi check-in thực tế nên có thể null
                java.sql.Timestamp filterTime = db.getThoiGianDen() != null ? db.getThoiGianDen() : db.getTimeVao();
                if (filterTime == null) {
                    // Booking không có cả hai mốc thời gian: vẫn hiển thị (không lọc theo thời gian)
                    filterTime = null;
                }

                if (filterTime != null) {
                    LocalDate date = filterTime.toLocalDateTime().toLocalDate();

                    // Day
                    if (dayFilter != null && !date.isEqual(dayFilter)) continue;

                    // Month
                    if (monthVal > 0 && filterTime.toLocalDateTime().getMonthValue() != monthVal) continue;
                }

                // Status: map trạng thái thô sang trạng thái hiển thị trước khi so sánh
                String rawStatus = db.getTrangThai();
                String mappedStatus = "Chờ xác nhận";
                if (rawStatus != null) {
                    if (rawStatus.equalsIgnoreCase("Đang chờ") || rawStatus.equalsIgnoreCase("Chờ xác nhận")) {
                        mappedStatus = "Chờ xác nhận";
                    } else if (rawStatus.equalsIgnoreCase("Đã nhận bàn") || rawStatus.equalsIgnoreCase("Đã xác nhận")) {
                        mappedStatus = "Đã xác nhận";
                    } else if (rawStatus.equalsIgnoreCase("Đã hủy")) {
                        mappedStatus = "Đã hủy";
                    } else {
                        mappedStatus = rawStatus;
                    }
                }

                if (statusFilter != null && !statusFilter.equals("Tất cả trạng thái")
                        && !mappedStatus.equalsIgnoreCase(statusFilter)) {
                    continue;
                }

                filtered.add(db);
            }
            renderBookings(filtered);
        }
    }

    private void renderBills(List<HoaDon> bills) {
        if (bills.isEmpty()) {
            historyContainer.getChildren().add(new EmptyState("📄", "Không có lịch sử hóa đơn", "Không tìm thấy hóa đơn thanh toán nào khớp với bộ lọc!"));
            return;
        }

        // Sort descending by time
        bills.sort((h1, h2) -> h2.getTimeVao().compareTo(h1.getTimeVao()));

        for (HoaDon hd : bills) {
            HBox card = new HBox();
            card.getStyleClass().add("history-card");

            VBox info = new VBox(5);
            Label lblID = new Label("Mã hóa đơn: " + hd.getMaHD());
            lblID.getStyleClass().add("order-code-label");

            String tableName = tableMap.getOrDefault(hd.getMaBan(), "Bàn ăn");
            String timeStr = hd.getTimeVao() != null ? sdf.format(hd.getTimeVao()) : "";
            Label lblDetails = new Label("Bàn: " + tableName + "  |  Giờ vào: " + timeStr);
            lblDetails.getStyleClass().add("order-date-label");
            info.getChildren().addAll(lblID, lblDetails);

            // Status
            String status = hd.getTrangThai() != null ? hd.getTrangThai() : "Đã thanh toán";
            Label lblStatus = new Label(status);
            lblStatus.getStyleClass().addAll("status-badge", status.equalsIgnoreCase("Đã thanh toán") ? "badge-available" : "badge-pending");

            // Total
            long totalVal = hd.getThanhToan() != null ? hd.getThanhToan().longValue() : 0L;
            Label lblPrice = new Label(String.format(Locale.US, "%,dđ", totalVal));
            lblPrice.getStyleClass().add("order-price-label");
            lblPrice.setPrefWidth(130);
            lblPrice.setAlignment(Pos.CENTER_RIGHT);

            Button btnDetail = new Button("Xem");
            btnDetail.getStyleClass().add("btn-secondary");
            btnDetail.setOnAction(e -> showBillDetailDialog(hd, tableName));

            Region reg1 = new Region();
            HBox.setHgrow(reg1, Priority.ALWAYS);
            Region reg2 = new Region();
            reg2.setPrefWidth(15);
            Region reg3 = new Region();
            reg3.setPrefWidth(15);

            if (status.equalsIgnoreCase("Chưa thanh toán")) {
                Button btnPay = new Button("Thanh toán");
                btnPay.getStyleClass().add("btn-primary");
                btnPay.setOnAction(e -> {
                    boolean paid = handlePayment(hd);
                    if (paid) {
                        processPayment(hd);
                    }
                });
                HBox actions = new HBox(10);
                actions.getChildren().addAll(btnPay, btnDetail);
                card.getChildren().addAll(info, reg1, lblStatus, reg2, lblPrice, reg3, actions);
            } else {
                card.getChildren().addAll(info, reg1, lblStatus, reg2, lblPrice, reg3, btnDetail);
            }
            historyContainer.getChildren().add(card);
        }
    }

    private boolean handlePayment(HoaDon hd) {
        List<String> methods = List.of("Ví điện tử MoMo", "Ví ZaloPay", "Thẻ tín dụng / ATM");
        ChoiceDialog<String> dialog = new ChoiceDialog<>("Ví điện tử MoMo", methods);
        dialog.setTitle("Chọn phương thức thanh toán");
        dialog.setHeaderText("Hóa đơn: " + hd.getMaHD());
        dialog.setContentText("Phương thức:");
        
        Optional<String> result = dialog.showAndWait();
        if (result.isPresent()) {
            String method = result.get();
            double amount = hd.getThanhToan() != null ? hd.getThanhToan().doubleValue() : 0.0;
            if (method.equals("Ví điện tử MoMo")) {
                return showQRPaymentDialog("Thanh toán qua MoMo", "momo", "MoMo", "0987654321", "GOURMET HUB", amount, hd.getMaHD());
            } else if (method.equals("Ví ZaloPay")) {
                return showQRPaymentDialog("Thanh toán qua ZaloPay", "zalopay", "ZaloPay", "0987654321", "GOURMET HUB", amount, hd.getMaHD());
            } else {
                return showCardPaymentDialog(amount);
            }
        }
        return false;
    }

    private void processPayment(HoaDon hd) {
        hd.setTrangThai("Đã thanh toán");
        
        new Thread(() -> {
            try {
                Request payReq = new Request(Module.HOADON, Action.UPDATE, hd);
                Response payRes = SocketClient.getInstance().sendRequest(payReq);
                Platform.runLater(() -> {
                    if (payRes != null && payRes.isSuccess()) {
                        Dialogs.showSuccess("Thanh toán thành công", "Hóa đơn " + hd.getMaHD() + " đã được thanh toán!");
                        loadHistoryData();
                    } else {
                        Dialogs.showError("Thanh toán thất bại", "Có lỗi xảy ra khi thanh toán hóa đơn.");
                    }
                });
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }).start();
    }

    private boolean showQRPaymentDialog(String title, String method, String bankInfo, String accountNo, String accountName, double amount, String orderId) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle(title);
        dialog.setHeaderText(null);

        DialogPane dialogPane = dialog.getDialogPane();
        try {
            String commonCss = getClass().getResource("/css/customer/common.css").toExternalForm();
            String componentCss = getClass().getResource("/css/customer/component.css").toExternalForm();
            dialogPane.getStylesheets().addAll(commonCss, componentCss);
        } catch (Exception e) {}
        dialogPane.getStyleClass().add("dialog-card");

        VBox vbox = new VBox(15);
        vbox.setAlignment(Pos.CENTER);
        vbox.setPadding(new Insets(20));
        vbox.setPrefWidth(400);

        Label lblHeader = new Label("QUÉT MÃ QR THANH TOÁN");
        lblHeader.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #1e293b;");

        GridPane qrCode = new GridPane();
        qrCode.setAlignment(Pos.CENTER);
        qrCode.setStyle("-fx-background-color: white; -fx-padding: 15; -fx-border-color: #e2e8f0; -fx-border-radius: 10; -fx-background-radius: 10;");
        qrCode.setPrefSize(180, 180);
        qrCode.setMaxSize(180, 180);

        for (int row = 0; row < 15; row++) {
            for (int col = 0; col < 15; col++) {
                Rectangle rect = new Rectangle(10, 10);
                boolean isBlack = false;
                if (row < 7 && col < 7) {
                    isBlack = (row == 0 || row == 6 || col == 0 || col == 6) || (row >= 2 && row <= 4 && col >= 2 && col <= 4);
                } else if (row < 7 && col >= 8) {
                    int c = col - 8;
                    isBlack = (row == 0 || row == 6 || c == 0 || c == 6) || (row >= 2 && row <= 4 && c >= 2 && c <= 4);
                } else if (row >= 8 && col < 7) {
                    int r = row - 8;
                    isBlack = (r == 0 || r == 6 || col == 0 || col == 6) || (r >= 2 && r <= 4 && col >= 2 && col <= 4);
                } else {
                    isBlack = Math.random() > 0.5;
                }
                rect.setFill(isBlack ? Color.web("#1e293b") : Color.WHITE);
                qrCode.add(rect, col, row);
            }
        }

        VBox infoCard = new VBox(8);
        infoCard.setStyle("-fx-background-color: #f8fafc; -fx-padding: 15; -fx-background-radius: 10;");
        
        Label lblBank = new Label(method.equals("momo") ? "Ví điện tử: MoMo" : method.equals("zalopay") ? "Ví điện tử: ZaloPay" : "Ngân hàng: " + bankInfo);
        lblBank.setStyle("-fx-font-weight: bold; -fx-text-fill: #334155;");
        Label lblAccNo = new Label(method.equals("momo") || method.equals("zalopay") ? "SĐT nhận: " + accountNo : "Số tài khoản: " + accountNo);
        lblAccNo.setStyle("-fx-text-fill: #475569;");
        Label lblAccName = new Label("Tên người nhận: " + accountName);
        lblAccName.setStyle("-fx-text-fill: #475569;");
        Label lblAmount = new Label("Số tiền: " + String.format(Locale.US, "%,.0f VNĐ", amount));
        lblAmount.setStyle("-fx-font-weight: bold; -fx-text-fill: #059669;");
        Label lblMsg = new Label("Nội dung: " + orderId);
        lblMsg.setStyle("-fx-font-weight: bold; -fx-text-fill: #2563eb;");

        infoCard.getChildren().addAll(lblBank, lblAccNo, lblAccName, lblAmount, lblMsg);
        vbox.getChildren().addAll(lblHeader, qrCode, infoCard);
        dialogPane.setContent(vbox);

        dialogPane.getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        Button btnOk = (Button) dialogPane.lookupButton(ButtonType.OK);
        btnOk.setText("Tôi đã thanh toán");
        btnOk.getStyleClass().add("btn-primary");
        Button btnCancel = (Button) dialogPane.lookupButton(ButtonType.CANCEL);
        btnCancel.setText("Hủy giao dịch");
        btnCancel.getStyleClass().add("btn-secondary");

        Optional<ButtonType> result = dialog.showAndWait();
        return result.isPresent() && result.get() == ButtonType.OK;
    }

    private boolean showCardPaymentDialog(double amount) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Thanh toán Thẻ tín dụng / ATM");
        dialog.setHeaderText(null);

        DialogPane dialogPane = dialog.getDialogPane();
        try {
            String commonCss = getClass().getResource("/css/customer/common.css").toExternalForm();
            String componentCss = getClass().getResource("/css/customer/component.css").toExternalForm();
            dialogPane.getStylesheets().addAll(commonCss, componentCss);
        } catch (Exception e) {}
        dialogPane.getStyleClass().add("dialog-card");

        VBox vbox = new VBox(15);
        vbox.setPadding(new Insets(20));
        vbox.setPrefWidth(420);

        Label lblHeader = new Label("NHẬP THÔNG TIN THẺ");
        lblHeader.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #1e293b;");

        VBox cardMock = new VBox(15);
        cardMock.setStyle("-fx-background-color: linear-gradient(to right, #4f46e5, #06b6d4); -fx-padding: 20; -fx-background-radius: 15; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.15), 10, 0, 0, 4);");
        
        Label cardTitle = new Label("Gourmet Hub Credit");
        cardTitle.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 14px;");

        TextField txtCardNo = new TextField();
        txtCardNo.setPromptText("4111 2222 3333 4444");
        txtCardNo.setStyle("-fx-background-color: rgba(255,255,255,0.15); -fx-text-fill: white; -fx-prompt-text-fill: #cbd5e1; -fx-background-radius: 5; -fx-pref-height: 36;");
        
        HBox row = new HBox(15);
        TextField txtExp = new TextField();
        txtExp.setPromptText("MM/YY");
        txtExp.setStyle("-fx-background-color: rgba(255,255,255,0.15); -fx-text-fill: white; -fx-prompt-text-fill: #cbd5e1; -fx-background-radius: 5; -fx-pref-height: 36;");
        HBox.setHgrow(txtExp, Priority.ALWAYS);

        PasswordField txtCvv = new PasswordField();
        txtCvv.setPromptText("CVV");
        txtCvv.setStyle("-fx-background-color: rgba(255,255,255,0.15); -fx-text-fill: white; -fx-prompt-text-fill: #cbd5e1; -fx-background-radius: 5; -fx-pref-height: 36;");
        HBox.setHgrow(txtCvv, Priority.ALWAYS);
        row.getChildren().addAll(txtExp, txtCvv);

        TextField txtHolder = new TextField();
        txtHolder.setPromptText("TEN CHU THE");
        txtHolder.setStyle("-fx-background-color: rgba(255,255,255,0.15); -fx-text-fill: white; -fx-prompt-text-fill: #cbd5e1; -fx-background-radius: 5; -fx-pref-height: 36;");

        cardMock.getChildren().addAll(cardTitle, txtCardNo, row, txtHolder);

        Label lblAmount = new Label("Tổng tiền thanh toán: " + String.format(Locale.US, "%,.0f VNĐ", amount));
        lblAmount.setStyle("-fx-font-weight: bold; -fx-text-fill: #059669; -fx-font-size: 14px;");

        vbox.getChildren().addAll(lblHeader, cardMock, lblAmount);
        dialogPane.setContent(vbox);

        dialogPane.getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        Button btnOk = (Button) dialogPane.lookupButton(ButtonType.OK);
        btnOk.setText("Thanh toán ngay");
        btnOk.getStyleClass().add("btn-primary");
        Button btnCancel = (Button) dialogPane.lookupButton(ButtonType.CANCEL);
        btnCancel.setText("Hủy giao dịch");
        btnCancel.getStyleClass().add("btn-secondary");

        btnOk.addEventFilter(ActionEvent.ACTION, event -> {
            String card = txtCardNo.getText().trim().replace(" ", "");
            String exp = txtExp.getText().trim();
            String cvv = txtCvv.getText().trim();
            String holder = txtHolder.getText().trim();
            
            if (card.isEmpty() || exp.isEmpty() || cvv.isEmpty() || holder.isEmpty()) {
                Dialogs.showError("Lỗi nhập liệu", "Vui lòng điền đầy đủ thông tin thẻ!");
                event.consume();
            } else if (card.length() < 12) {
                Dialogs.showError("Lỗi số thẻ", "Số thẻ không hợp lệ!");
                event.consume();
            }
        });

        Optional<ButtonType> result = dialog.showAndWait();
        return result.isPresent() && result.get() == ButtonType.OK;
    }

    private void renderBookings(List<DatBan> bookings) {
        if (bookings.isEmpty()) {
            historyContainer.getChildren().add(new EmptyState("📅", "Không có lịch sử đặt bàn", "Không tìm thấy yêu cầu đặt bàn nào khớp với bộ lọc!"));
            return;
        }

        // Sắp xếp giảm dần theo timeVao — null-safe
        bookings.sort((b1, b2) -> {
            java.sql.Timestamp t1 = b1.getTimeVao() != null ? b1.getTimeVao() : b1.getThoiGianDen();
            java.sql.Timestamp t2 = b2.getTimeVao() != null ? b2.getTimeVao() : b2.getThoiGianDen();
            if (t1 == null && t2 == null) return 0;
            if (t1 == null) return 1;
            if (t2 == null) return -1;
            return t2.compareTo(t1);
        });

        for (DatBan db : bookings) {
            HBox card = new HBox();
            card.getStyleClass().add("history-card");

            VBox info = new VBox(5);
            Label lblID = new Label("Mã đặt bàn: " + db.getMaDatBan());
            lblID.getStyleClass().add("order-code-label");

            String tableName = tableMap.getOrDefault(db.getMaBan(), "Bàn ăn");
            String timeStr = db.getTimeVao() != null ? sdf.format(db.getTimeVao()) : "";
            Label lblDetails = new Label("Bàn: " + tableName + "  |  Thời gian: " + timeStr + "  |  Khách: " + db.getSoNguoi());
            lblDetails.getStyleClass().add("order-date-label");
            
            if (db.getNote() != null && !db.getNote().isEmpty()) {
                Label lblNote = new Label("Yêu cầu: " + db.getNote());
                lblNote.getStyleClass().add("item-note-label");
                info.getChildren().addAll(lblID, lblDetails, lblNote);
            } else {
                info.getChildren().addAll(lblID, lblDetails);
            }

            // Status Badge
            String status = "Chờ xác nhận";
            if (db.getTrangThai() != null) {
                if (db.getTrangThai().equalsIgnoreCase("Đang chờ") || db.getTrangThai().equalsIgnoreCase("Chờ xác nhận")) {
                    status = "Chờ xác nhận";
                } else if (db.getTrangThai().equalsIgnoreCase("Đã nhận bàn") || db.getTrangThai().equalsIgnoreCase("Đã xác nhận")) {
                    status = "Đã xác nhận";
                } else if (db.getTrangThai().equalsIgnoreCase("Đã hủy")) {
                    status = "Đã hủy";
                } else {
                    status = db.getTrangThai();
                }
            }
            Label lblStatus = new Label(status);
            lblStatus.getStyleClass().add("status-badge");
            if (status.equalsIgnoreCase("Đã xác nhận")) {
                lblStatus.getStyleClass().add("badge-available"); // green
            } else if (status.equalsIgnoreCase("Chờ xác nhận")) {
                lblStatus.getStyleClass().add("badge-pending"); // yellow
            } else {
                lblStatus.getStyleClass().add("badge-out"); // red
            }

            Region reg = new Region();
            HBox.setHgrow(reg, Priority.ALWAYS);

            card.getChildren().addAll(info, reg, lblStatus);

            // Add Cancel button if reservation is still pending
            if (status.equalsIgnoreCase("Chờ xác nhận")) {
                Button btnCancel = new Button("Hủy đặt");
                btnCancel.getStyleClass().add("btn-danger");
                btnCancel.setOnAction(e -> {
                    if (Dialogs.showConfirm("Hủy đặt bàn", "Quý khách chắc chắn muốn hủy đặt bàn " + db.getMaDatBan() + " chứ?")) {
                        db.setTrangThai("Đã hủy");
                        new Thread(() -> {
                            try {
                                Request cancelReq = new Request(Module.DATBAN, Action.UPDATE, db);
                                Response cancelRes = SocketClient.getInstance().sendRequest(cancelReq);
                                Platform.runLater(() -> {
                                    if (cancelRes != null && cancelRes.isSuccess()) {
                                        Dialogs.showSuccess("Hủy thành công", "Yêu cầu đặt bàn đã được hủy thành công!");
                                        loadHistoryData(); // Reload
                                    } else {
                                        Dialogs.showError("Hủy thất bại", "Có lỗi xảy ra khi hủy yêu cầu đặt bàn.");
                                    }
                                });
                            } catch (Exception ex) {
                                ex.printStackTrace();
                            }
                        }).start();
                    }
                });
                Region spacer = new Region();
                spacer.setPrefWidth(15);
                card.getChildren().addAll(spacer, btnCancel);
            }

            historyContainer.getChildren().add(card);
        }
    }

    @FXML
    private void handleBillsSegment() {
        if (isBillsSegment) return;
        isBillsSegment = true;
        btnBills.getStyleClass().addAll("segment-button-active");
        btnBookings.getStyleClass().removeAll("segment-button-active");
        if (txtSearchCode != null) {
            txtSearchCode.setPromptText("Tìm mã hóa đơn...");
            txtSearchCode.clear();
        }
        updateStatusFilterItems();
        applyFilters();
    }

    @FXML
    private void handleBookingsSegment() {
        if (!isBillsSegment) return;
        isBillsSegment = false;
        btnBookings.getStyleClass().addAll("segment-button-active");
        btnBills.getStyleClass().removeAll("segment-button-active");
        if (txtSearchCode != null) {
            txtSearchCode.setPromptText("Tìm mã đặt bàn...");
            txtSearchCode.clear();
        }
        updateStatusFilterItems();
        applyFilters();
    }



    private void showBillDetailDialog(HoaDon hd, String tableName) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Chi tiết hóa đơn " + hd.getMaHD());
        dialog.setHeaderText(null);

        DialogPane dialogPane = dialog.getDialogPane();
        try {
            String commonCss = getClass().getResource("/css/customer/common.css").toExternalForm();
            String componentCss = getClass().getResource("/css/customer/component.css").toExternalForm();
            dialogPane.getStylesheets().addAll(commonCss, componentCss);
        } catch (Exception e) {}
        dialogPane.getStyleClass().add("dialog-card");

        VBox mainLayout = new VBox(15);
        mainLayout.setPadding(new Insets(20));
        mainLayout.setPrefWidth(450);

        Label lblHeader = new Label("CHI TIẾT HÓA ĐƠN");
        lblHeader.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #1e293b;");
        lblHeader.setAlignment(Pos.CENTER);

        VBox metaBox = new VBox(6);
        metaBox.setStyle("-fx-background-color: #f8fafc; -fx-padding: 15; -fx-background-radius: 10; -fx-border-color: #e2e8f0; -fx-border-radius: 10;");
        
        Label lblId = new Label("Mã hóa đơn: " + hd.getMaHD());
        lblId.setStyle("-fx-font-weight: bold; -fx-text-fill: #334155;");
        
        Label lblTable = new Label("Bàn: " + tableName);
        lblTable.setStyle("-fx-text-fill: #475569;");
        
        Label lblTimeVao = new Label("Giờ vào: " + (hd.getTimeVao() != null ? sdf.format(hd.getTimeVao()) : "N/A"));
        lblTimeVao.setStyle("-fx-text-fill: #475569;");
        
        Label lblTimeRa = new Label("Giờ ra: " + (hd.getTimeRa() != null ? sdf.format(hd.getTimeRa()) : "N/A"));
        lblTimeRa.setStyle("-fx-text-fill: #475569;");

        String status = hd.getTrangThai() != null ? hd.getTrangThai() : "Đã thanh toán";
        Label lblStatus = new Label("Trạng thái: " + status);
        lblStatus.setStyle("-fx-font-weight: bold; -fx-text-fill: " + (status.equalsIgnoreCase("Đã thanh toán") ? "#059669;" : "#d97706;"));

        metaBox.getChildren().addAll(lblId, lblTable, lblTimeVao, lblTimeRa, lblStatus);

        Label lblItemsHeader = new Label("Danh sách món ăn");
        lblItemsHeader.setStyle("-fx-font-weight: bold; -fx-text-fill: #1e293b;");
        
        VBox itemsBox = new VBox(8);
        ScrollPane scrollPane = new ScrollPane(itemsBox);
        scrollPane.setFitToWidth(true);
        scrollPane.setPrefHeight(200);
        scrollPane.setStyle("-fx-background-color: transparent; -fx-background: transparent; -fx-border-color: #e2e8f0; -fx-border-radius: 8; -fx-padding: 5;");

        Label lblLoading = new Label("Đang tải chi tiết món ăn...");
        lblLoading.setStyle("-fx-text-fill: #64748b; -fx-font-style: italic;");
        itemsBox.getChildren().add(lblLoading);

        VBox summaryBox = new VBox(6);
        summaryBox.setStyle("-fx-background-color: #f8fafc; -fx-padding: 12; -fx-background-radius: 10;");

        double rawTongTien = hd.getTongTien() != null ? hd.getTongTien().doubleValue() : 0.0;
        double rawGiamGia = hd.getGiamGia() != null ? hd.getGiamGia().doubleValue() : 0.0;
        double rawThanhToan = hd.getThanhToan() != null ? hd.getThanhToan().doubleValue() : 0.0;

        Label lblTongTien = new Label("Tổng cộng: " + String.format(Locale.US, "%,.0fđ", rawTongTien));
        lblTongTien.setStyle("-fx-text-fill: #475569;");
        
        Label lblGiamGia = new Label("Giảm giá: " + String.format(Locale.US, "%,.0fđ", rawGiamGia));
        lblGiamGia.setStyle("-fx-text-fill: #475569;");
        
        Label lblThanhToan = new Label("Thực thu: " + String.format(Locale.US, "%,.0fđ", rawThanhToan));
        lblThanhToan.setStyle("-fx-font-weight: bold; -fx-text-fill: #059669; -fx-font-size: 14px;");

        summaryBox.getChildren().addAll(lblTongTien, lblGiamGia, lblThanhToan);

        mainLayout.getChildren().addAll(lblHeader, metaBox, lblItemsHeader, scrollPane, summaryBox);
        dialogPane.setContent(mainLayout);
        dialogPane.getButtonTypes().add(ButtonType.CLOSE);
        Button btnClose = (Button) dialogPane.lookupButton(ButtonType.CLOSE);
        btnClose.setText("Đóng");
        btnClose.getStyleClass().add("btn-secondary");

        new Thread(() -> {
            try {
                // 1. Fetch food names
                Map<String, String> foodMap = new HashMap<>();
                Request foodReq = new Request(Module.MONAN, Action.GET_ALL, null);
                Response foodRes = SocketClient.getInstance().sendRequest(foodReq);
                if (foodRes != null && foodRes.isSuccess() && foodRes.getData() != null) {
                    List<MonAn> foods = (List<MonAn>) foodRes.getData();
                    for (MonAn m : foods) {
                        foodMap.put(m.getMaMon(), m.getTenMon());
                    }
                }

                // 2. Fetch details for this bill
                Request detailReq = new Request(Module.CHITIETHD, "GET_BY_HOADON", hd.getMaHD());
                Response detailRes = SocketClient.getInstance().sendRequest(detailReq);

                Platform.runLater(() -> {
                    itemsBox.getChildren().clear();
                    if (detailRes != null && detailRes.isSuccess() && detailRes.getData() != null) {
                        List<ChiTietHD> details = (List<ChiTietHD>) detailRes.getData();
                        if (details.isEmpty()) {
                            Label lblEmpty = new Label("Không có chi tiết món ăn.");
                            lblEmpty.setStyle("-fx-text-fill: #64748b;");
                            itemsBox.getChildren().add(lblEmpty);
                        } else {
                            for (ChiTietHD ct : details) {
                                HBox row = new HBox();
                                row.setStyle("-fx-padding: 5 0; -fx-border-color: #f1f5f9; -fx-border-width: 0 0 1 0;");
                                row.setAlignment(Pos.CENTER_LEFT);

                                VBox dishInfo = new VBox(2);
                                String name = foodMap.getOrDefault(ct.getMaMon(), ct.getMaMon());
                                Label lblName = new Label(name);
                                lblName.setStyle("-fx-font-weight: bold; -fx-text-fill: #334155;");
                                
                                double price = ct.getDonGia() != null ? ct.getDonGia().doubleValue() : 0.0;
                                Label lblDesc = new Label(ct.getSoLuong() + " x " + String.format(Locale.US, "%,.0fđ", price));
                                lblDesc.setStyle("-fx-text-fill: #64748b; -fx-font-size: 11px;");
                                dishInfo.getChildren().addAll(lblName, lblDesc);

                                Region spacer = new Region();
                                HBox.setHgrow(spacer, Priority.ALWAYS);

                                double total = ct.getThanhTien() != null ? ct.getThanhTien().doubleValue() : (price * ct.getSoLuong());
                                Label lblSubtotal = new Label(String.format(Locale.US, "%,.0fđ", total));
                                lblSubtotal.setStyle("-fx-font-weight: bold; -fx-text-fill: #334155;");

                                row.getChildren().addAll(dishInfo, spacer, lblSubtotal);
                                itemsBox.getChildren().add(row);
                            }
                        }
                    } else {
                        Label lblError = new Label("Lỗi khi tải chi tiết món ăn.");
                        lblError.setStyle("-fx-text-fill: #ef4444;");
                        itemsBox.getChildren().add(lblError);
                    }
                });
            } catch (Exception ex) {
                ex.printStackTrace();
                Platform.runLater(() -> {
                    itemsBox.getChildren().clear();
                    Label lblError = new Label("Lỗi kết nối server.");
                    lblError.setStyle("-fx-text-fill: #ef4444;");
                    itemsBox.getChildren().add(lblError);
                });
            }
        }).start();

        dialog.showAndWait();
    }
}
