package table;

import java.net.URL;
import java.util.ResourceBundle;
import java.util.List;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;

import table.TableController.TableModel;
import model.DatBan;
import model.HoaDon;
import model.Order;
import model.OrderDetail;
import model.KhachHang;
import model.NhanVien;
import order.OrderController.OrderDetailModel;
import network.Action;
import network.Module;
import network.Request;
import network.Response;
import network.SocketClient;
import javafx.scene.control.TableView;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.geometry.Pos;
import javafx.scene.layout.HBox;
import java.text.DecimalFormat;
import java.math.BigDecimal;

@SuppressWarnings("unchecked")
public class TableDetailController implements Initializable {

    // Header controls
    @FXML
    private Button btnClose;

    // Section 1: General Info
    @FXML
    private Label lblMaBan;
    @FXML
    private Label lblTenBan;
    @FXML
    private Label lblKhuVuc;
    @FXML
    private Label lblTrangThai;
    @FXML
    private Label lblSoNguoiToiDa;
    @FXML
    private Label lblGhiChu;

    // Conditional VBoxes
    @FXML
    private VBox vboxOccupied;
    @FXML
    private VBox vboxReserved;
    @FXML
    private VBox vboxEmpty;

    // Occupied Controls
    @FXML
    private Label lblMaHoaDon;
    @FXML
    private Label lblMaOrder;
    @FXML
    private Label lblThoiGianMo;
    @FXML
    private Label lblKhachHangDung;
    @FXML
    private Label lblNhanVienPhucVu;
    @FXML
    private Label lblTongSoMon;
    @FXML
    private Label lblTongTienTamTinh;

    @FXML private TableView<OrderDetailModel> detailTable;
    @FXML private TableColumn<OrderDetailModel, Object> colStt;
    @FXML private TableColumn<OrderDetailModel, String> colMamon;
    @FXML private TableColumn<OrderDetailModel, String> colTenmon;
    @FXML private TableColumn<OrderDetailModel, Integer> colSoluong;
    @FXML private TableColumn<OrderDetailModel, Double> colDongia;
    @FXML private TableColumn<OrderDetailModel, Double> colThanhTien;
    @FXML private TableColumn<OrderDetailModel, String> colTrangThaiMon;

    private final ObservableList<OrderDetailModel> detailsList = FXCollections.observableArrayList();
    private String currentMaOrderForCheckout = null;

    // Reserved Controls
    @FXML
    private Label lblKhachDat;
    @FXML
    private Label lblSdtKhachDat;
    @FXML
    private Label lblSoNguoiDat;
    @FXML
    private Label lblThoiGianDat;
    @FXML
    private Label lblThoiGianXacNhan;
    @FXML
    private Label lblThoiGianDenDuKien;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        TableModel table = TableController.selectedTable;
        if (table == null) {
            return;
        }

        // Bind general info
        lblMaBan.setText(table.getMaban());
        lblTenBan.setText(table.getTenban());
        lblKhuVuc.setText(table.getKhuvuc());
        lblTrangThai.setText(table.getTrangthai());
        lblSoNguoiToiDa.setText("4 người");
        lblGhiChu.setText("Chưa có dữ liệu");

        // Color status badge
        String status = table.getTrangthai();
        if (status.equalsIgnoreCase("Đang dùng") || status.equalsIgnoreCase("Có khách")) {
            lblTrangThai.setStyle("-fx-text-fill: #ef4444; -fx-font-weight: bold;");
        } else if (status.equalsIgnoreCase("Đặt trước") || status.equalsIgnoreCase("Đã đặt")) {
            lblTrangThai.setStyle("-fx-text-fill: #ff9800; -fx-font-weight: bold;");
        } else {
            lblTrangThai.setStyle("-fx-text-fill: #20c763; -fx-font-weight: bold;");
        }

        // Hide all details initially
        vboxOccupied.setVisible(false);
        vboxOccupied.setManaged(false);
        vboxReserved.setVisible(false);
        vboxReserved.setManaged(false);
        vboxEmpty.setVisible(false);
        vboxEmpty.setManaged(false);

        if (status.equalsIgnoreCase("Đang dùng") || status.equalsIgnoreCase("Có khách")) {
            vboxOccupied.setVisible(true);
            vboxOccupied.setManaged(true);
            loadOccupiedDetails(table);
        } else if (status.equalsIgnoreCase("Đặt trước") || status.equalsIgnoreCase("Đã đặt")) {
            vboxReserved.setVisible(true);
            vboxReserved.setManaged(true);
            loadReservedDetails(table);
        } else {
            vboxEmpty.setVisible(true);
            vboxEmpty.setManaged(true);
        }
    }

    private void loadOccupiedDetails(TableModel table) {
        new Thread(() -> {
            try {
                Request reqKH = new Request(Module.KHACHHANG, Action.GET_ALL, null);
                Response resKH = SocketClient.getInstance().sendRequest(reqKH);
                List<KhachHang> khList = null;
                if (resKH != null && resKH.isSuccess()) {
                    khList = (List<KhachHang>) resKH.getData();
                }

                Request reqNV = new Request(Module.NHANVIEN, Action.GET_ALL, null);
                Response resNV = SocketClient.getInstance().sendRequest(reqNV);
                List<NhanVien> nvList = null;
                if (resNV != null && resNV.isSuccess()) {
                    nvList = (List<NhanVien>) resNV.getData();
                }

                Request reqHD = new Request(Module.HOADON, "GET_BY_BAN", table.getMaban());
                Response resHD = SocketClient.getInstance().sendRequest(reqHD);

                Request reqOrd = new Request(Module.ORDER, Action.GET_ALL, null);
                Response resOrd = SocketClient.getInstance().sendRequest(reqOrd);

                final List<KhachHang> customers = khList;
                final List<NhanVien> employees = nvList;

                HoaDon activeHDTemp = null;
                if (resHD != null && resHD.isSuccess()) {
                    List<HoaDon> hdList = (List<HoaDon>) resHD.getData();
                    if (hdList != null) {
                        for (HoaDon hd : hdList) {
                            if ("Chưa thanh toán".equalsIgnoreCase(hd.getTrangThai())) {
                                activeHDTemp = hd;
                                break;
                            }
                        }
                    }
                }

                Order activeOrdTemp = null;
                if (resOrd != null && resOrd.isSuccess()) {
                    List<Order> ordList = (List<Order>) resOrd.getData();
                    if (ordList != null) {
                        for (Order ord : ordList) {
                            if (table.getMaban().equalsIgnoreCase(ord.getMaBan())
                                    && ("Đang order".equalsIgnoreCase(ord.getTrangThai())
                                        || "Đang phục vụ".equalsIgnoreCase(ord.getTrangThai()))) {
                                activeOrdTemp = ord;
                                break;
                            }
                        }
                    }
                }

                Response resDetailTemp = null;
                if (activeOrdTemp != null) {
                    Request reqDetail = new Request(Module.ORDER, Action.GET_BY_ID, activeOrdTemp.getMaOrder());
                    resDetailTemp = SocketClient.getInstance().sendRequest(reqDetail);
                }

                final HoaDon activeHD = activeHDTemp;
                final Order activeOrd = activeOrdTemp;
                final Response resDetail = resDetailTemp;

                Platform.runLater(() -> {
                    setupTableColumns();
                    detailsList.clear();

                    if (resDetail != null && resDetail.isSuccess()) {
                        List<OrderDetail> list = (List<OrderDetail>) resDetail.getData();
                        if (list != null) {
                            for (OrderDetail d : list) {
                                detailsList.add(new OrderDetailModel(
                                    d.getMaOrder(),
                                    d.getMaMon(),
                                    d.getTenMon(),
                                    d.getSoLuong(),
                                    d.getDonGia() != null ? d.getDonGia().doubleValue() : 0.0,
                                    d.getTrangThai()
                                ));
                            }
                        }
                    }
                    detailTable.setItems(detailsList);

                    String customerName = "Chưa có dữ liệu";
                    String employeeName = "Chưa có dữ liệu";

                    if (activeHD != null) {
                        if (activeHD.getMaKH() != null && !activeHD.getMaKH().trim().isEmpty()) {
                            KhachHang matchedKH = findKhachHang(activeHD.getMaKH(), customers);
                            if (matchedKH != null) {
                                customerName = matchedKH.getTenKH() + " (" + matchedKH.getSdt() + ")";
                            } else {
                                customerName = activeHD.getMaKH();
                            }
                        }
                        if (activeHD.getMaNV() != null && !activeHD.getMaNV().trim().isEmpty()) {
                            NhanVien matchedNV = findNhanVien(activeHD.getMaNV(), employees);
                            if (matchedNV != null) {
                                employeeName = matchedNV.getHoTen();
                            } else {
                                employeeName = activeHD.getMaNV();
                            }
                        }
                    }

                    String maHD = activeHD != null ? activeHD.getMaHD() : "Chưa có dữ liệu";
                    String maOrder = activeOrd != null ? activeOrd.getMaOrder() : "Chưa có dữ liệu";
                    currentMaOrderForCheckout = activeOrd != null ? activeOrd.getMaOrder() : null;

                    String timeVao = activeHD != null && activeHD.getTimeVao() != null
                            ? formatTimestamp(activeHD.getTimeVao())
                            : "Chưa có dữ liệu";

                    int totalMon = 0;
                    double totalMoney = 0.0;
                    for (OrderDetailModel item : detailsList) {
                        if (!"Đã hủy".equalsIgnoreCase(item.getTrangthai())) {
                            totalMon++;
                            totalMoney += item.getThanhtien();
                        }
                    }

                    lblMaHoaDon.setText(maHD);
                    lblMaOrder.setText(maOrder);
                    lblThoiGianMo.setText(timeVao);
                    lblKhachHangDung.setText(customerName);
                    lblNhanVienPhucVu.setText(employeeName);
                    lblTongSoMon.setText(String.valueOf(totalMon));
                    lblTongTienTamTinh.setText(formatCurrency(totalMoney));
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void loadReservedDetails(TableModel table) {
        new Thread(() -> {
            try {
                Request reqKH = new Request(Module.KHACHHANG, Action.GET_ALL, null);
                Response resKH = SocketClient.getInstance().sendRequest(reqKH);
                List<KhachHang> khList = null;
                if (resKH != null && resKH.isSuccess()) {
                    khList = (List<KhachHang>) resKH.getData();
                }

                Request reqDB = new Request(Module.DATBAN, "GET_BY_BAN", table.getMaban());
                Response resDB = SocketClient.getInstance().sendRequest(reqDB);

                final List<KhachHang> customers = khList;

                Platform.runLater(() -> {
                    DatBan activeDB = null;
                    if (resDB != null && resDB.isSuccess()) {
                        List<DatBan> dbList = (List<DatBan>) resDB.getData();
                        if (dbList != null) {
                            for (DatBan db : dbList) {
                                if (!"Đã hủy".equalsIgnoreCase(db.getTrangThai())) {
                                    activeDB = db;
                                    break;
                                }
                            }
                        }
                    }

                    String customerName = "Chưa có dữ liệu";
                    String sdt = "Chưa có dữ liệu";
                    String timeDatStr = "Chưa có dữ liệu";
                    String timeXacNhanStr = "Chưa nhận bàn";
                    String timeVaoStr = "Chưa có dữ liệu";
                    String songuoiStr = "Chưa có dữ liệu";

                    if (activeDB != null) {
                        if (activeDB.getMaKH() != null && !activeDB.getMaKH().trim().isEmpty()) {
                            KhachHang matchedKH = findKhachHang(activeDB.getMaKH(), customers);
                            if (matchedKH != null) {
                                customerName = matchedKH.getTenKH();
                                sdt = matchedKH.getSdt() != null && !matchedKH.getSdt().trim().isEmpty()
                                        ? matchedKH.getSdt()
                                        : "Chưa có dữ liệu";
                            } else {
                                customerName = activeDB.getMaKH();
                            }
                        }

                        if (activeDB.getTimeVao() != null) {
                            timeVaoStr = formatTimestamp(activeDB.getTimeVao());
                            java.sql.Timestamp simBookingTime = new java.sql.Timestamp(
                                    activeDB.getTimeVao().getTime() - 24 * 3600 * 1000L);
                            timeDatStr = formatTimestamp(simBookingTime);
                        }

                        // Hiển thị thời gian đến thực tế từ trường thoigianden
                        if (activeDB.getThoiGianDen() != null) {
                            timeXacNhanStr = formatTimestamp(activeDB.getThoiGianDen());
                        } else {
                            timeXacNhanStr = "Chưa đến";
                        }

                        songuoiStr = String.valueOf(activeDB.getSoNguoi());

                        if (activeDB.getNote() != null && !activeDB.getNote().trim().isEmpty()) {
                            lblGhiChu.setText(activeDB.getNote());
                        }
                    }

                    lblKhachDat.setText(customerName);
                    lblSdtKhachDat.setText(sdt);
                    lblSoNguoiDat.setText(songuoiStr);
                    lblThoiGianDat.setText(timeDatStr);
                    lblThoiGianXacNhan.setText(timeXacNhanStr);
                    lblThoiGianDenDuKien.setText(timeVaoStr);
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    @FXML
    private void handleClose(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/table.fxml"));
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
        }
    }

    private KhachHang findKhachHang(String maKH, List<KhachHang> customers) {
        if (maKH == null || customers == null)
            return null;
        for (KhachHang kh : customers) {
            if (kh.getMaKH().equalsIgnoreCase(maKH)) {
                return kh;
            }
        }
        return null;
    }

    private NhanVien findNhanVien(String maNV, List<NhanVien> employees) {
        if (maNV == null || employees == null)
            return null;
        for (NhanVien nv : employees) {
            if (nv.getMaNV().equalsIgnoreCase(maNV)) {
                return nv;
            }
        }
        return null;
    }

    private String formatTimestamp(java.sql.Timestamp ts) {
        if (ts == null)
            return "Chưa có dữ liệu";
        try {
            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd/MM/yyyy HH:mm");
            return sdf.format(ts);
        } catch (Exception e) {
            return ts.toString();
        }
    }

    private String formatCurrency(double amount) {
        try {
            java.text.DecimalFormat df = new java.text.DecimalFormat("#,###");
            return df.format(amount) + " VNĐ";
        } catch (Exception e) {
            return String.valueOf(amount) + " VNĐ";
        }
    }
    private void setupTableColumns() {
        colStt.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(Object item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setText(null);
                } else {
                    setText(String.valueOf(getIndex() + 1));
                }
            }
        });

        colMamon.setCellValueFactory(new PropertyValueFactory<>("mamon"));
        colTenmon.setCellValueFactory(new PropertyValueFactory<>("tenmon"));
        colSoluong.setCellValueFactory(new PropertyValueFactory<>("soluong"));
        colDongia.setCellValueFactory(new PropertyValueFactory<>("dongia"));
        colThanhTien.setCellValueFactory(new PropertyValueFactory<>("thanhtien"));
        colTrangThaiMon.setCellValueFactory(new PropertyValueFactory<>("trangthai"));

        colDongia.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(formatCurrency(item));
                }
            }
        });

        colThanhTien.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(formatCurrency(item));
                }
            }
        });

        colTrangThaiMon.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                } else {
                    Label label = new Label(item);
                    label.getStyleClass().add("status-tag");
                    if (item.equalsIgnoreCase("Đang chờ")) {
                        label.setStyle("-fx-background-color: #cbd5e1; -fx-text-fill: #475569; -fx-font-weight: bold; -fx-padding: 3 8 3 8; -fx-background-radius: 4;");
                    } else if (item.equalsIgnoreCase("Đang chế biến")) {
                        label.setStyle("-fx-background-color: #ffe4e6; -fx-text-fill: #e11d48; -fx-font-weight: bold; -fx-padding: 3 8 3 8; -fx-background-radius: 4;");
                    } else if (item.equalsIgnoreCase("Đã xong")) {
                        label.setStyle("-fx-background-color: #dbeafe; -fx-text-fill: #2563eb; -fx-font-weight: bold; -fx-padding: 3 8 3 8; -fx-background-radius: 4;");
                    } else if (item.equalsIgnoreCase("Đã phục vụ")) {
                        label.setStyle("-fx-background-color: #dcfce7; -fx-text-fill: #15803d; -fx-font-weight: bold; -fx-padding: 3 8 3 8; -fx-background-radius: 4;");
                    } else if (item.equalsIgnoreCase("Đã hủy")) {
                        label.setStyle("-fx-background-color: #fee2e2; -fx-text-fill: #991b1b; -fx-font-weight: bold; -fx-padding: 3 8 3 8; -fx-background-radius: 4;");
                    }
                    HBox box = new HBox(label);
                    box.setAlignment(Pos.CENTER);
                    setGraphic(box);
                }
            }
        });
    }

    @FXML
    private void handleThanhToan(ActionEvent event) {
        if (currentMaOrderForCheckout == null) {
            showAlert(Alert.AlertType.ERROR, "Lỗi", "Không có đơn hàng hoạt động để thanh toán!");
            return;
        }

        boolean hasIncomplete = false;
        for (OrderDetailModel item : detailsList) {
            if ("Đang chờ".equalsIgnoreCase(item.getTrangthai()) || "Đang chế biến".equalsIgnoreCase(item.getTrangthai())) {
                hasIncomplete = true;
                break;
            }
        }

        if (hasIncomplete) {
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
            confirm.setTitle("Xác nhận thanh toán");
            confirm.setHeaderText("Món ăn chưa hoàn thành");
            confirm.setContentText("Đơn hàng này vẫn còn món ăn đang chờ hoặc đang chế biến. Bạn có chắc chắn muốn thanh toán không?");
            
            ButtonType result = confirm.showAndWait().orElse(ButtonType.CANCEL);
            if (result != ButtonType.OK) {
                return;
            }
        } else {
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
            confirm.setTitle("Xác nhận thanh toán");
            confirm.setHeaderText("Thực hiện thanh toán");
            confirm.setContentText("Bạn có chắc chắn muốn thực hiện thanh toán hóa đơn cho bàn ăn này?");
            
            ButtonType result = confirm.showAndWait().orElse(ButtonType.CANCEL);
            if (result != ButtonType.OK) {
                return;
            }
        }

        String maNV = "NV01";
        if (Login.LoginController.loggedInAccount != null && Login.LoginController.loggedInAccount.getMaNV() != null) {
            maNV = Login.LoginController.loggedInAccount.getMaNV();
        }

        final String finalMaNV = maNV;
        new Thread(() -> {
            try {
                List<Object> requestData = List.of(currentMaOrderForCheckout, finalMaNV, BigDecimal.ZERO);
                Request req = new Request(Module.HOADON, "THANH_TOAN", requestData);
                Response res = SocketClient.getInstance().sendRequest(req);

                Platform.runLater(() -> {
                    if (res != null && res.isSuccess()) {
                        showAlert(Alert.AlertType.INFORMATION, "Thành công", "Thanh toán hóa đơn thành công!");
                        handleClose(null);
                    } else {
                        showAlert(Alert.AlertType.ERROR, "Thất bại", res != null ? res.getMessage() : "Thanh toán thất bại trên Server!");
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() -> {
                    showAlert(Alert.AlertType.ERROR, "Lỗi kết nối", "Lỗi kết nối Server: " + e.getMessage());
                });
            }
        }).start();
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
