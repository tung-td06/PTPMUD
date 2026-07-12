package table;

import java.net.URL;
import java.util.ResourceBundle;
import java.util.List;
import java.util.Comparator;
import java.time.LocalDate;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
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
import javafx.util.Duration;

import table.TableController.TableModel;
import model.DatBan;
import model.BanAn;
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
    // Khi "Dang dung", cac label nay duoc map sang thong tin DatBan:
    // lblMaHoaDon      -> Ma dat ban
    // lblMaOrder       -> Ma khach hang
    // lblThoiGianMo    -> Thoi gian den thuc te (thoigianden)
    // lblKhachHangDung -> Ten KH (SDT)
    // lblNhanVienPhucVu-> So nguoi
    // lblTongSoMon     -> Trang thai dat ban
    // lblTongTienTamTinh -> Thoi gian ket thuc (timera)
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

    @FXML
    private TableView<OrderDetailModel> detailTable;
    @FXML
    private TableColumn<OrderDetailModel, Object> colStt;
    @FXML
    private TableColumn<OrderDetailModel, String> colMamon;
    @FXML
    private TableColumn<OrderDetailModel, String> colTenmon;
    @FXML
    private TableColumn<OrderDetailModel, Integer> colSoluong;
    @FXML
    private TableColumn<OrderDetailModel, Double> colDongia;
    @FXML
    private TableColumn<OrderDetailModel, Double> colThanhTien;
    @FXML
    private TableColumn<OrderDetailModel, String> colTrangThaiMon;

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

    // Auto-refresh
    private Timeline refreshTimeline;
    private String currentMaBan;
    private volatile boolean isRefreshing = false;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        TableModel table = TableController.selectedTable;
        if (table == null) {
            return;
        }

        currentMaBan = table.getMaban();

        // Hien thi thong tin co ban tu cache truoc (de UI khong trong trong khi load)
        lblMaBan.setText(table.getMaban());
        lblTenBan.setText(table.getTenban());
        lblKhuVuc.setText(table.getKhuvuc());
        lblSoNguoiToiDa.setText("4 nguoi");
        lblGhiChu.setText("Dang tai...");

        // An tat ca section trang thai ban dau
        setAllSectionsHidden();

        // Tai du lieu moi nhat tu server ngay lap tuc
        loadDataFromServer();

        // Bat dau auto-refresh moi 4 giay
        startAutoRefresh();
    }

    /**
     * Tai toan bo du lieu moi nhat tu server.
     * Duoc goi khi mo cua so va moi 4 giay.
     */
    private void loadDataFromServer() {
        if (isRefreshing) return;
        isRefreshing = true;

        new Thread(() -> {
            try {
                // 1. Lay thong tin ban moi nhat tu server (khong dung cache FE)
                Request reqBan = new Request(Module.BANAN, Action.GET_BY_ID, currentMaBan);
                Response resBan = SocketClient.getInstance().sendRequest(reqBan);

                BanAn banAn = null;
                if (resBan != null && resBan.isSuccess() && resBan.getData() instanceof BanAn) {
                    banAn = (BanAn) resBan.getData();
                }

                final String trangThaiBan = (banAn != null) ? banAn.getTrangThai() : "Trong";
                final BanAn finalBanAn = banAn;

                // 2. Dua theo trang thai, load chi tiet tuong ung
                if ("Dang dung".equalsIgnoreCase(trangThaiBan) || "Co khach".equalsIgnoreCase(trangThaiBan)
                        || "\u0110ang d\u00f9ng".equalsIgnoreCase(trangThaiBan)
                        || "C\u00f3 kh\u00e1ch".equalsIgnoreCase(trangThaiBan)) {
                    loadOccupiedData(finalBanAn);
                } else if ("Dat truoc".equalsIgnoreCase(trangThaiBan) || "Da dat".equalsIgnoreCase(trangThaiBan)
                        || "\u0110\u1eb7t tr\u01b0\u1edbc".equalsIgnoreCase(trangThaiBan)
                        || "\u0110\u00e3 \u0111\u1eb7t".equalsIgnoreCase(trangThaiBan)) {
                    loadReservedData(finalBanAn);
                } else {
                    // Trong hoac trang thai khac
                    Platform.runLater(() -> {
                        if (finalBanAn != null) {
                            updateGeneralInfo(finalBanAn);
                        }
                        setAllSectionsHidden();
                        vboxEmpty.setVisible(true);
                        vboxEmpty.setManaged(true);
                        lblGhiChu.setText("Chua co du lieu");
                    });
                    isRefreshing = false;
                }

            } catch (Exception e) {
                e.printStackTrace();
                isRefreshing = false;
            }
        }).start();
    }

    /**
     * Load du lieu khi ban "Dang dung":
     * Lay ban ghi datban co trangthai = 'Da nhan ban' + join KhachHang
     */
    private void loadOccupiedData(BanAn banAn) {
        new Thread(() -> {
            try {
                // Lay ban ghi dat ban hien tai (Da nhan ban)
                Request reqDB = new Request(Module.DATBAN, "GET_ACTIVE_BY_BAN", currentMaBan);
                Response resDB = SocketClient.getInstance().sendRequest(reqDB);

                DatBan activeDatBan = null;
                if (resDB != null && resDB.isSuccess() && resDB.getData() instanceof DatBan) {
                    activeDatBan = (DatBan) resDB.getData();
                }

                // Lay danh sach khach hang de join ten
                List<KhachHang> khList = null;
                if (activeDatBan != null && activeDatBan.getMaKH() != null) {
                    Request reqKH = new Request(Module.KHACHHANG, Action.GET_ALL, null);
                    Response resKH = SocketClient.getInstance().sendRequest(reqKH);
                    if (resKH != null && resKH.isSuccess()) {
                        khList = (List<KhachHang>) resKH.getData();
                    }
                }

                // Neu khong co ban ghi Da nhan ban -> hien thi trong
                if (activeDatBan == null) {
                    final BanAn fb = banAn;
                    Platform.runLater(() -> {
                        if (fb != null) updateGeneralInfo(fb);
                        setAllSectionsHidden();
                        vboxEmpty.setVisible(true);
                        vboxEmpty.setManaged(true);
                        lblGhiChu.setText("Chua co du lieu");
                    });
                    isRefreshing = false;
                    return;
                }

                final DatBan finalDB = activeDatBan;
                final List<KhachHang> finalKhList = khList;

                Platform.runLater(() -> {
                    // Cap nhat thong tin chung
                    if (banAn != null) updateGeneralInfo(banAn);

                    // Hien thi section Dang dung
                    setAllSectionsHidden();
                    vboxOccupied.setVisible(true);
                    vboxOccupied.setManaged(true);

                    // An table mon an (khong lien quan den datban)
                    if (detailTable != null) {
                        detailTable.setVisible(false);
                        detailTable.setManaged(false);
                    }

                    // Map thong tin dat ban sang cac label hien co
                    // lblMaHoaDon -> Ma dat ban
                    lblMaHoaDon.setText(finalDB.getMaDatBan() != null ? finalDB.getMaDatBan() : "--");

                    // lblMaOrder -> Ma khach hang
                    lblMaOrder.setText(finalDB.getMaKH() != null ? finalDB.getMaKH() : "--");

                    // lblThoiGianMo -> Thoi gian den thuc te (thoigianden)
                    lblThoiGianMo.setText(finalDB.getThoiGianDen() != null
                            ? formatTimestamp(finalDB.getThoiGianDen())
                            : "Chua ghi nhan");

                    // lblKhachHangDung -> Ten KH (SDT)
                    String customerDisplay = "--";
                    if (finalDB.getMaKH() != null && !finalDB.getMaKH().trim().isEmpty()) {
                        KhachHang kh = findKhachHang(finalDB.getMaKH(), finalKhList);
                        if (kh != null) {
                            String sdt = (kh.getSdt() != null && !kh.getSdt().trim().isEmpty())
                                    ? kh.getSdt() : "";
                            customerDisplay = kh.getTenKH() + (sdt.isEmpty() ? "" : " (" + sdt + ")");
                        } else {
                            customerDisplay = finalDB.getMaKH();
                        }
                    }
                    lblKhachHangDung.setText(customerDisplay);

                    // lblNhanVienPhucVu -> So nguoi
                    lblNhanVienPhucVu.setText(finalDB.getSoNguoi() + " ng\u01b0\u1eddi");

                    // lblTongSoMon -> Trang thai dat ban
                    lblTongSoMon.setText(finalDB.getTrangThai() != null ? finalDB.getTrangThai() : "--");

                    // lblTongTienTamTinh -> Thoi gian ket thuc (timera)
                    lblTongTienTamTinh.setText(finalDB.getTimeRa() != null
                            ? formatTimestamp(finalDB.getTimeRa())
                            : "Chua co du lieu");

                    // Ghi chu (dung lblGhiChu trong section chung)
                    lblGhiChu.setText((finalDB.getNote() != null && !finalDB.getNote().trim().isEmpty())
                            ? finalDB.getNote()
                            : "Khong co ghi chu");

                    currentMaOrderForCheckout = null;
                });

            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                isRefreshing = false;
            }
        }).start();
    }

    /**
     * Load du lieu khi ban "Dat truoc":
     * Tim ban ghi datban co trangthai = 'Da xac nhan' hom nay
     */
    private void loadReservedData(BanAn banAn) {
        new Thread(() -> {
            try {
                Request reqKH = new Request(Module.KHACHHANG, Action.GET_ALL, null);
                Response resKH = SocketClient.getInstance().sendRequest(reqKH);
                List<KhachHang> khList = null;
                if (resKH != null && resKH.isSuccess()) {
                    khList = (List<KhachHang>) resKH.getData();
                }

                Request reqDB = new Request(Module.DATBAN, "GET_BY_BAN", currentMaBan);
                Response resDB = SocketClient.getInstance().sendRequest(reqDB);

                final List<KhachHang> customers = khList;

                Platform.runLater(() -> {
                    if (banAn != null) updateGeneralInfo(banAn);

                    DatBan activeDB = null;
                    if (resDB != null && resDB.isSuccess()) {
                        List<DatBan> dbList = (List<DatBan>) resDB.getData();
                        if (dbList != null) {
                            long now = System.currentTimeMillis();
                            LocalDate today = LocalDate.now();
                            dbList.sort(Comparator.comparing(DatBan::getTimeVao,
                                    Comparator.nullsLast(Comparator.naturalOrder())));
                            for (DatBan db : dbList) {
                                if (db.getTimeVao() != null && db.getTimeRa() != null) {
                                    LocalDate bookingDate = db.getTimeVao().toLocalDateTime().toLocalDate();
                                    boolean isCancelled = "Da huy".equalsIgnoreCase(db.getTrangThai())
                                            || "\u0110\u00e3 h\u1ee7y".equalsIgnoreCase(db.getTrangThai());
                                    boolean isCompleted = "Hoan thanh".equalsIgnoreCase(db.getTrangThai())
                                            || "Ho\u00e0n th\u00e0nh".equalsIgnoreCase(db.getTrangThai());
                                    boolean isExpired = db.getTimeRa().getTime() < now;
                                    if (bookingDate.equals(today) && !isCancelled && !isCompleted && !isExpired) {
                                        if ("Da xac nhan".equalsIgnoreCase(db.getTrangThai())
                                                || "\u0110\u00e3 x\u00e1c nh\u1eadn".equalsIgnoreCase(db.getTrangThai())) {
                                            activeDB = db;
                                            break;
                                        }
                                    }
                                }
                            }
                        }
                    }

                    String customerName = "Chua co du lieu";
                    String sdt = "Chua co du lieu";
                    String timeDatStr = "Chua co du lieu";
                    String timeXacNhanStr = "Chua den";
                    String timeVaoStr = "Chua co du lieu";
                    String songuoiStr = "Chua co du lieu";

                    if (activeDB != null) {
                        if (activeDB.getMaKH() != null && !activeDB.getMaKH().trim().isEmpty()) {
                            KhachHang matchedKH = findKhachHang(activeDB.getMaKH(), customers);
                            if (matchedKH != null) {
                                customerName = matchedKH.getTenKH();
                                sdt = matchedKH.getSdt() != null && !matchedKH.getSdt().trim().isEmpty()
                                        ? matchedKH.getSdt() : "Chua co du lieu";
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

                        if (activeDB.getThoiGianDen() != null) {
                            timeXacNhanStr = formatTimestamp(activeDB.getThoiGianDen());
                        }

                        songuoiStr = String.valueOf(activeDB.getSoNguoi());

                        lblGhiChu.setText((activeDB.getNote() != null && !activeDB.getNote().trim().isEmpty())
                                ? activeDB.getNote() : "Khong co ghi chu");
                    }

                    setAllSectionsHidden();
                    vboxReserved.setVisible(true);
                    vboxReserved.setManaged(true);

                    lblKhachDat.setText(customerName);
                    lblSdtKhachDat.setText(sdt);
                    lblSoNguoiDat.setText(songuoiStr);
                    lblThoiGianDat.setText(timeDatStr);
                    lblThoiGianXacNhan.setText(timeXacNhanStr);
                    lblThoiGianDenDuKien.setText(timeVaoStr);
                });
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                isRefreshing = false;
            }
        }).start();
    }

    /**
     * Cap nhat thong tin chung (header) cua ban tu doi tuong BanAn moi nhat.
     */
    private void updateGeneralInfo(BanAn banAn) {
        lblMaBan.setText(banAn.getMaBan());
        lblTenBan.setText(banAn.getTenBan());
        lblKhuVuc.setText(banAn.getKhuVuc());
        lblTrangThai.setText(banAn.getTrangThai());
        lblSoNguoiToiDa.setText("4 ng\u01b0\u1eddi");

        String status = banAn.getTrangThai();
        if (status.equalsIgnoreCase("\u0110ang d\u00f9ng") || status.equalsIgnoreCase("C\u00f3 kh\u00e1ch")) {
            lblTrangThai.setStyle("-fx-text-fill: #ef4444; -fx-font-weight: bold;");
        } else if (status.equalsIgnoreCase("\u0110\u1eb7t tr\u01b0\u1edbc") || status.equalsIgnoreCase("\u0110\u00e3 \u0111\u1eb7t")) {
            lblTrangThai.setStyle("-fx-text-fill: #ff9800; -fx-font-weight: bold;");
        } else {
            lblTrangThai.setStyle("-fx-text-fill: #20c763; -fx-font-weight: bold;");
        }
    }

    /**
     * An tat ca cac section trang thai chi tiet.
     */
    private void setAllSectionsHidden() {
        vboxOccupied.setVisible(false);
        vboxOccupied.setManaged(false);
        vboxReserved.setVisible(false);
        vboxReserved.setManaged(false);
        vboxEmpty.setVisible(false);
        vboxEmpty.setManaged(false);
    }

    /**
     * Bat dau Timeline auto-refresh moi 4 giay.
     */
    private void startAutoRefresh() {
        refreshTimeline = new Timeline(new KeyFrame(Duration.seconds(4), e -> loadDataFromServer()));
        refreshTimeline.setCycleCount(Timeline.INDEFINITE);
        refreshTimeline.play();
    }

    /**
     * Dung Timeline khi dong cua so.
     */
    private void stopAutoRefresh() {
        if (refreshTimeline != null) {
            refreshTimeline.stop();
            refreshTimeline = null;
        }
    }

    @FXML
    private void handleClose(ActionEvent event) {
        stopAutoRefresh();
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
            return "Ch\u01b0a c\u00f3 d\u1eef li\u1ec7u";
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
            return df.format(amount) + " VN\u0110";
        } catch (Exception e) {
            return String.valueOf(amount) + " VN\u0110";
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
                    if (item.equalsIgnoreCase("\u0110ang ch\u1edd")) {
                        label.setStyle(
                                "-fx-background-color: #cbd5e1; -fx-text-fill: #475569; -fx-font-weight: bold; -fx-padding: 3 8 3 8; -fx-background-radius: 4;");
                    } else if (item.equalsIgnoreCase("\u0110ang ch\u1ebf bi\u1ebfn")) {
                        label.setStyle(
                                "-fx-background-color: #ffe4e6; -fx-text-fill: #e11d48; -fx-font-weight: bold; -fx-padding: 3 8 3 8; -fx-background-radius: 4;");
                    } else if (item.equalsIgnoreCase("\u0110\u00e3 xong")) {
                        label.setStyle(
                                "-fx-background-color: #dbeafe; -fx-text-fill: #2563eb; -fx-font-weight: bold; -fx-padding: 3 8 3 8; -fx-background-radius: 4;");
                    } else if (item.equalsIgnoreCase("\u0110\u00e3 ph\u1ee5c v\u1ee5")) {
                        label.setStyle(
                                "-fx-background-color: #dcfce7; -fx-text-fill: #15803d; -fx-font-weight: bold; -fx-padding: 3 8 3 8; -fx-background-radius: 4;");
                    } else if (item.equalsIgnoreCase("\u0110\u00e3 h\u1ee7y")) {
                        label.setStyle(
                                "-fx-background-color: #fee2e2; -fx-text-fill: #991b1b; -fx-font-weight: bold; -fx-padding: 3 8 3 8; -fx-background-radius: 4;");
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
            showAlert(Alert.AlertType.ERROR, "L\u1ed7i", "Kh\u00f4ng c\u00f3 \u0111\u01a1n h\u00e0ng ho\u1ea1t \u0111\u1ed9ng \u0111\u1ec3 thanh to\u00e1n!");
            return;
        }

        boolean hasIncomplete = false;
        for (OrderDetailModel item : detailsList) {
            if ("\u0110ang ch\u1edd".equalsIgnoreCase(item.getTrangthai())
                    || "\u0110ang ch\u1ebf bi\u1ebfn".equalsIgnoreCase(item.getTrangthai())) {
                hasIncomplete = true;
                break;
            }
        }

        if (hasIncomplete) {
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
            confirm.setTitle("X\u00e1c nh\u1eadn thanh to\u00e1n");
            confirm.setHeaderText("M\u00f3n \u0103n ch\u01b0a ho\u00e0n th\u00e0nh");
            confirm.setContentText(
                    "\u0110\u01a1n h\u00e0ng n\u00e0y v\u1eabn c\u00f2n m\u00f3n \u0103n \u0111ang ch\u1edd ho\u1eb7c \u0111ang ch\u1ebf bi\u1ebfn. B\u1ea1n c\u00f3 ch\u1eafc ch\u1eafn mu\u1ed1n thanh to\u00e1n kh\u00f4ng?");

            ButtonType result = confirm.showAndWait().orElse(ButtonType.CANCEL);
            if (result != ButtonType.OK) {
                return;
            }
        } else {
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
            confirm.setTitle("X\u00e1c nh\u1eadn thanh to\u00e1n");
            confirm.setHeaderText("Th\u1ef1c hi\u1ec7n thanh to\u00e1n");
            confirm.setContentText("B\u1ea1n c\u00f3 ch\u1eafc ch\u1eafn mu\u1ed1n th\u1ef1c hi\u1ec7n thanh to\u00e1n h\u00f3a \u0111\u01a1n cho b\u00e0n \u0103n n\u00e0y?");

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
                        showAlert(Alert.AlertType.INFORMATION, "Th\u00e0nh c\u00f4ng", "Thanh to\u00e1n h\u00f3a \u0111\u01a1n th\u00e0nh c\u00f4ng!");
                        handleClose(null);
                    } else {
                        showAlert(Alert.AlertType.ERROR, "Th\u1ea5t b\u1ea1i",
                                res != null ? res.getMessage() : "Thanh to\u00e1n th\u1ea5t b\u1ea1i tr\u00ean Server!");
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() -> {
                    showAlert(Alert.AlertType.ERROR, "L\u1ed7i k\u1ebft n\u1ed1i", "L\u1ed7i k\u1ebft n\u1ed1i Server: " + e.getMessage());
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
