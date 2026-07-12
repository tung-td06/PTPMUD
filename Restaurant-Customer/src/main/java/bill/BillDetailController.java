package bill;

import java.net.URL;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;

import bill.BillController.BillModel;
import network.Action;
import network.Module;
import network.Request;
import network.Response;
import network.SocketClient;

public class BillDetailController implements Initializable {

    @FXML private Label lblSubtitle;
    @FXML private Label lblMaHD;
    @FXML private Label lblMaOrder;
    @FXML private Label lblMaBan;
    @FXML private Label lblKhachHang;
    @FXML private Label lblNhanVien;
    @FXML private Label lblNgaytao;
    @FXML private Label lblPayment;
    @FXML private Label lblTrangthai;

    @FXML private TableView<BillDetailRowModel> detailTable;
    @FXML private TableColumn<BillDetailRowModel, Object> colStt;
    @FXML private TableColumn<BillDetailRowModel, String> colMamon;
    @FXML private TableColumn<BillDetailRowModel, String> colTenmon;
    @FXML private TableColumn<BillDetailRowModel, Integer> colSoluong;
    @FXML private TableColumn<BillDetailRowModel, Double> colDongia;
    @FXML private TableColumn<BillDetailRowModel, Double> colThanhTien;

    @FXML private Label lblTotalItems;
    @FXML private Label lblTotalQuantity;
    @FXML private Label lblTotalAmount;

    @FXML private Button btnClose;

    private final ObservableList<BillDetailRowModel> detailsList = FXCollections.observableArrayList();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        setupTableColumns();
        loadBillDetail();
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

        colDongia.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    DecimalFormat df = new DecimalFormat("#,###");
                    setText(df.format(item) + " VNĐ");
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
                    DecimalFormat df = new DecimalFormat("#,###");
                    setText(df.format(item) + " VNĐ");
                }
            }
        });

        detailTable.setItems(detailsList);
    }

    private void loadBillDetail() {
        BillModel bill = BillController.selectedBill;
        if (bill == null) {
            return;
        }

        lblSubtitle.setText("Mã Hóa đơn: " + bill.getId() + " | Bàn: " + bill.getTable() + " | Nhân viên: " + bill.getEmployee());
        lblMaHD.setText(bill.getId());
        lblMaBan.setText(bill.getTable());
        lblNhanVien.setText(bill.getEmployee() != null ? bill.getEmployee() : "--");
        lblKhachHang.setText(bill.getCustomer() != null ? bill.getCustomer() : "--");
        lblNgaytao.setText(bill.getTimeVaoStr());
        lblPayment.setText(bill.getPayment());
        lblTrangthai.setText(bill.getStatus());

        lblTrangthai.getStyleClass().remove("status-tag");
        lblTrangthai.getStyleClass().remove("status-paid");
        lblTrangthai.getStyleClass().remove("status-processing");
        lblTrangthai.getStyleClass().remove("status-pending");

        lblTrangthai.getStyleClass().add("status-tag");
        String stat = bill.getStatus();
        if (stat.equalsIgnoreCase("Đã thanh toán")) {
            lblTrangthai.getStyleClass().add("status-paid");
        } else if (stat.equalsIgnoreCase("Đang xử lý")) {
            lblTrangthai.getStyleClass().add("status-processing");
        } else {
            lblTrangthai.getStyleClass().add("status-pending");
        }

        lblMaOrder.setText("Đang tải...");

        new Thread(() -> {
            try {
                // 1. Fetch Order matching this bill to display MaOrder
                Request orderReq = new Request(Module.ORDER, Action.GET_ALL, null);
                Response orderRes = SocketClient.getInstance().sendRequest(orderReq);
                String foundMaOrder = "--";
                if (orderRes != null && orderRes.isSuccess()) {
                    List<model.Order> orderList = (List<model.Order>) orderRes.getData();
                    if (orderList != null) {
                        for (model.Order o : orderList) {
                            if (o.getMaHD() != null && o.getMaHD().equalsIgnoreCase(bill.getId())) {
                                foundMaOrder = o.getMaOrder();
                                break;
                            }
                        }
                    }
                }

                // 2. Fetch food list to map food ID to food name
                Map<String, String> localFoodMap = new HashMap<>();
                Request foodReq = new Request(Module.MONAN, Action.GET_ALL, null);
                Response foodRes = SocketClient.getInstance().sendRequest(foodReq);
                if (foodRes != null && foodRes.isSuccess()) {
                    List<model.MonAn> foods = (List<model.MonAn>) foodRes.getData();
                    if (foods != null) {
                        for (model.MonAn ma : foods) {
                            localFoodMap.put(ma.getMaMon(), ma.getTenMon());
                        }
                    }
                }

                // 3. Fetch bill details from server
                Request req = new Request(Module.CHITIETHD, "GET_BY_HOADON", bill.getId());
                Response res = SocketClient.getInstance().sendRequest(req);

                final String orderId = foundMaOrder;

                Platform.runLater(() -> {
                    lblMaOrder.setText(orderId);

                    if (res != null && res.isSuccess()) {
                        List<model.ChiTietHD> details = (List<model.ChiTietHD>) res.getData();
                        detailsList.clear();

                        int totalQty = 0;
                        double totalAmt = 0.0;

                        if (details != null) {
                            for (model.ChiTietHD ct : details) {
                                String tenMon = localFoodMap.getOrDefault(ct.getMaMon(), ct.getMaMon());
                                double dg = ct.getDonGia() != null ? ct.getDonGia().doubleValue() : 0.0;
                                double tt = ct.getThanhTien() != null ? ct.getThanhTien().doubleValue() : (dg * ct.getSoLuong());
                                
                                detailsList.add(new BillDetailRowModel(
                                    ct.getMaHD(),
                                    ct.getMaMon(),
                                    tenMon,
                                    ct.getSoLuong(),
                                    dg,
                                    tt
                                ));

                                totalQty += ct.getSoLuong();
                                totalAmt += tt;
                            }
                        }

                        lblTotalItems.setText(String.valueOf(detailsList.size()));
                        lblTotalQuantity.setText(String.valueOf(totalQty));
                        DecimalFormat df = new DecimalFormat("#,###");
                        lblTotalAmount.setText(df.format(totalAmt) + " VNĐ");
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    @FXML
    private void handleClose() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/bill.fxml"));
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

    public static class BillDetailRowModel {
        private final String mahd;
        private final String mamon;
        private final String tenmon;
        private final int soluong;
        private final double dongia;
        private final double thanhtien;

        public BillDetailRowModel(String mahd, String mamon, String tenmon, int soluong, double dongia, double thanhtien) {
            this.mahd = mahd;
            this.mamon = mamon;
            this.tenmon = tenmon;
            this.soluong = soluong;
            this.dongia = dongia;
            this.thanhtien = thanhtien;
        }

        public String getMahd() { return mahd; }
        public String getMamon() { return mamon; }
        public String getTenmon() { return tenmon; }
        public int getSoluong() { return soluong; }
        public double getDongia() { return dongia; }
        public double getThanhtien() { return thanhtien; }
    }
}
