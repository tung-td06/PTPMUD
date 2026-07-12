package customer.controller.booking;

import customer.controller.component.Dialogs;
import customer.controller.util.CustomerSession;
import dashboard.DashboardController;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DateCell;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextField;
import model.BanAn;
import model.DatBan;
import model.KhachHang;
import network.Action;
import network.Module;
import network.Request;
import network.Response;
import network.SocketClient;
import java.net.URL;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

public class CustomerBookingController implements Initializable {

    @FXML
    private DatePicker dpDate;

    @FXML
    private ComboBox<String> cmbHourIn;

    @FXML
    private ComboBox<String> cmbMinIn;

    @FXML
    private ComboBox<String> cmbHourOut;

    @FXML
    private ComboBox<String> cmbMinOut;

    @FXML
    private ComboBox<Integer> cmbGuests;

    @FXML
    private ComboBox<String> cmbTable;

    @FXML
    private TextField txtNote;

    @FXML
    private Button btnReset;

    @FXML
    private Button btnBook;

    private final List<BanAn> tableList = new ArrayList<>();
    private final Map<String, String> tableNameToCodeMap = new HashMap<>(); // tenBan -> maBan

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        setupFormFields();
        setupDateTimeListeners();
        loadAvailableTables();
    }

    private void setupFormFields() {
        // Prevent booking historical dates
        dpDate.setDayCellFactory(picker -> new DateCell() {
            @Override
            public void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                setDisable(empty || date.isBefore(LocalDate.now()));
            }
        });
        dpDate.setValue(LocalDate.now());

        // Hours: 08 to 22
        List<String> hours = new ArrayList<>();
        for (int i = 8; i <= 22; i++) {
            hours.add(String.format("%02d", i));
        }
        cmbHourIn.setItems(FXCollections.observableArrayList(hours));
        cmbHourOut.setItems(FXCollections.observableArrayList(hours));

        // Time In rounding logic based on current time
        LocalDateTime now = LocalDateTime.now();
        int hour = now.getHour();
        int minute = now.getMinute();
        int roundedHour = (minute <= 30) ? hour : (hour + 1);
        if (roundedHour < 8) roundedHour = 8;
        if (roundedHour > 22) roundedHour = 22;

        cmbHourIn.setValue(String.format("%02d", roundedHour));

        // Minutes: 00, 15, 30, 45
        List<String> minutes = List.of("00", "15", "30", "45");
        cmbMinIn.setItems(FXCollections.observableArrayList(minutes));
        cmbMinOut.setItems(FXCollections.observableArrayList(minutes));
        cmbMinIn.setValue("00");

        // Time Out: In + 2 hours
        int hourOut = roundedHour + 2;
        if (hourOut > 22) hourOut = 22;
        cmbHourOut.setValue(String.format("%02d", hourOut));
        cmbMinOut.setValue("00");

        // Guests: 1 to 20
        List<Integer> guests = new ArrayList<>();
        for (int i = 1; i <= 20; i++) {
            guests.add(i);
        }
        cmbGuests.setItems(FXCollections.observableArrayList(guests));
        cmbGuests.setValue(2);
    }

    private void setupDateTimeListeners() {
        cmbHourIn.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                updateTimeOut();
                loadAvailableTables();
            }
        });
        cmbMinIn.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                updateTimeOut();
                loadAvailableTables();
            }
        });
        dpDate.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                loadAvailableTables();
            }
        });
        cmbHourOut.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                loadAvailableTables();
            }
        });
        cmbMinOut.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                loadAvailableTables();
            }
        });
    }

    private void updateTimeOut() {
        String hIn = cmbHourIn.getValue();
        String mIn = cmbMinIn.getValue();
        if (hIn != null && mIn != null) {
            try {
                int hourIn = Integer.parseInt(hIn);
                int hourOut = hourIn + 2;
                if (hourOut > 22) {
                    hourOut = 22;
                }
                cmbHourOut.setValue(String.format("%02d", hourOut));
                cmbMinOut.setValue(mIn);
            } catch (NumberFormatException e) {
                // Ignore
            }
        }
    }

    private void loadAvailableTables() {
        LocalDate date = dpDate.getValue();
        String hIn = cmbHourIn.getValue();
        String mIn = cmbMinIn.getValue();
        String hOut = cmbHourOut.getValue();
        String mOut = cmbMinOut.getValue();

        if (date == null || hIn == null || mIn == null || hOut == null || mOut == null) {
            return;
        }

        try {
            LocalTime timeIn = LocalTime.of(Integer.parseInt(hIn), Integer.parseInt(mIn));
            LocalTime timeOut = LocalTime.of(Integer.parseInt(hOut), Integer.parseInt(mOut));

            if (!timeIn.isBefore(timeOut)) {
                cmbTable.setItems(FXCollections.emptyObservableList());
                cmbTable.setValue(null);
                return;
            }

            LocalDateTime ldtIn = LocalDateTime.of(date, timeIn);
            LocalDateTime ldtOut = LocalDateTime.of(date, timeOut);
            Timestamp tsIn = Timestamp.valueOf(ldtIn);
            Timestamp tsOut = Timestamp.valueOf(ldtOut);

            new Thread(() -> {
                try {
                    Request req = new Request(Module.BANAN, "GET_FREE_TABLES_IN_SLOT", List.of(tsIn, tsOut));
                    Response res = SocketClient.getInstance().sendRequest(req);

                    List<String> tablesDisplay = new ArrayList<>();
                    tableNameToCodeMap.clear();
                    tableList.clear();

                    if (res != null && res.isSuccess() && res.getData() != null) {
                        List<BanAn> banAns = (List<BanAn>) res.getData();
                        tableList.addAll(banAns);
                        for (BanAn t : banAns) {
                            String desc = t.getTenBan() + " - " + t.getKhuVuc() + " (Trống)";
                            tablesDisplay.add(desc);
                            tableNameToCodeMap.put(desc, t.getMaBan());
                        }
                    }

                    Platform.runLater(() -> {
                        cmbTable.setItems(FXCollections.observableArrayList(tablesDisplay));
                        if (!tablesDisplay.isEmpty()) {
                            cmbTable.setValue(tablesDisplay.get(0));
                        } else {
                            cmbTable.setValue(null);
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }).start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleReset() {
        dpDate.setValue(LocalDate.now());

        LocalDateTime now = LocalDateTime.now();
        int hour = now.getHour();
        int minute = now.getMinute();
        int roundedHour = (minute <= 30) ? hour : (hour + 1);
        if (roundedHour < 8) roundedHour = 8;
        if (roundedHour > 22) roundedHour = 22;

        cmbHourIn.setValue(String.format("%02d", roundedHour));
        cmbMinIn.setValue("00");

        int hourOut = roundedHour + 2;
        if (hourOut > 22) hourOut = 22;
        cmbHourOut.setValue(String.format("%02d", hourOut));
        cmbMinOut.setValue("00");

        cmbGuests.setValue(2);
        txtNote.clear();
        loadAvailableTables();
        btnBook.setDisable(false);
    }

    @FXML
    private void handleBook() {
        LocalDate date = dpDate.getValue();
        if (date == null) {
            Dialogs.showError("Lỗi đặt bàn", "Vui lòng chọn ngày đặt bàn.");
            return;
        }

        String hIn = cmbHourIn.getValue();
        String mIn = cmbMinIn.getValue();
        String hOut = cmbHourOut.getValue();
        String mOut = cmbMinOut.getValue();

        if (hIn == null || mIn == null || hOut == null || mOut == null) {
            Dialogs.showError("Lỗi đặt bàn", "Vui lòng chọn đầy đủ giờ vào và giờ ra dự kiến.");
            return;
        }

        LocalTime timeIn = LocalTime.of(Integer.parseInt(hIn), Integer.parseInt(mIn));
        LocalTime timeOut = LocalTime.of(Integer.parseInt(hOut), Integer.parseInt(mOut));

        if (!timeIn.isBefore(timeOut)) {
            Dialogs.showError("Lỗi đặt bàn", "Giờ vào phải trước giờ ra dự kiến.");
            return;
        }

        Integer guests = cmbGuests.getValue();
        if (guests == null) {
            Dialogs.showError("Lỗi đặt bàn", "Vui lòng chọn số lượng khách.");
            return;
        }

        // Map bàn được chọn
        String selectedTableDesc = cmbTable.getValue();
        if (selectedTableDesc == null || !tableNameToCodeMap.containsKey(selectedTableDesc)) {
            Dialogs.showError("Lỗi đặt bàn", "Vui lòng chọn một bàn ăn trống trong danh sách.");
            return;
        }
        String maBan = tableNameToCodeMap.get(selectedTableDesc);

        // Check if customer profile is valid
        KhachHang currentCustomer = CustomerSession.getCurrentCustomer();
        if (currentCustomer == null) {
            Dialogs.showError("Lỗi đặt bàn", "Không tìm thấy thông tin phiên khách hàng.");
            return;
        }

        btnBook.setDisable(true);

        LocalDateTime ldtIn = LocalDateTime.of(date, timeIn);
        LocalDateTime ldtOut = LocalDateTime.of(date, timeOut);

        Timestamp tsIn = Timestamp.valueOf(ldtIn);
        Timestamp tsOut = Timestamp.valueOf(ldtOut);

        String note = txtNote.getText().trim();

        new Thread(() -> {
            try {
                // Lay ma dat ban tiep theo tu server
                Request reqId = new Request(Module.DATBAN, "GET_NEXT_ID", null);
                Response resId = SocketClient.getInstance().sendRequest(reqId);

                final String finalMaDatBan;
                if (resId != null && resId.isSuccess() && resId.getData() != null) {
                    finalMaDatBan = (String) resId.getData();
                } else {
                    Platform.runLater(() -> {
                        Dialogs.showError("Lỗi đặt bàn", "Không thể lấy mã đặt bàn mới từ máy chủ. Vui lòng thử lại!");
                        btnBook.setDisable(false);
                    });
                    return;
                }

                DatBan booking = new DatBan(
                        finalMaDatBan,
                        currentCustomer.getMaKH(),
                        maBan,
                        tsIn,
                        tsOut,
                        null, // thoiGianDen: null vì khách chưa đến, nhân viên sẽ cập nhật khi check-in
                        guests,
                        note,
                        "Đang chờ");

                Request req = new Request(Module.DATBAN, Action.ADD, booking);
                Response res = SocketClient.getInstance().sendRequest(req);

                Platform.runLater(() -> {
                    if (res != null && res.isSuccess()) {
                        Dialogs.showSuccess("Đặt bàn thành công",
                                "Mã đặt bàn của quý khách là " + finalMaDatBan + ". Vui lòng chờ nhân viên xác nhận!");
                        handleReset();
                        btnBook.setDisable(false);
                    } else {
                        Dialogs.showError("Lỗi đặt bàn", res != null ? res.getMessage()
                                : "Không thể đặt bàn vào lúc này. Vui lòng kiểm tra lại kết nối!");
                        btnBook.setDisable(false);
                    }
                });

            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() -> {
                    Dialogs.showError("Lỗi đặt bàn", "Đã xảy ra lỗi khi gửi yêu cầu: " + e.getMessage());
                    btnBook.setDisable(false);
                });
            }
        }).start();
    }
}
