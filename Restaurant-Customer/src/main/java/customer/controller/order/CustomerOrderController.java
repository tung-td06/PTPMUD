package customer.controller.order;

import customer.controller.component.Dialogs;
import customer.controller.component.EmptyState;
import customer.controller.util.CustomerSession;
import dashboard.DashboardController;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.layout.AnchorPane;
import javafx.util.Duration;
import model.Order;
import order.OrderController.OrderModel;
import order.OrderService;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import java.math.BigDecimal;
import java.util.Map;
import java.util.HashMap;
import java.util.Optional;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ScrollPane;
import javafx.geometry.Insets;
import customer.controller.cart.CartManager;

public class CustomerOrderController implements Initializable {

    @FXML
    private VBox ordersContainer;

    private Timeline autoReloadTimeline;
    private final OrderService orderService = new OrderService();
    private final SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");
    public static Order selectedOrder = null;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        loadOrders();
        startAutoReload();

        // Dừng timeline khi scene bị đóng
        ordersContainer.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene == null && autoReloadTimeline != null) {
                autoReloadTimeline.stop();
            }
        });
    }

    private void startAutoReload() {
        autoReloadTimeline = new Timeline(new KeyFrame(Duration.seconds(10), e -> loadOrders()));
        autoReloadTimeline.setCycleCount(Animation.INDEFINITE);
        autoReloadTimeline.play();
    }

    private void loadOrders() {
        new Thread(() -> {
            try {
                String maKH = CustomerSession.getCurrentCustomer().getMaKH();

                // Lấy trực tiếp order của KH qua GET_BY_KHACHHANG
                List<OrderModel> orders = orderService.getOrdersByKhachHang(maKH);

                // Bổ sung các order được đặt trong session này (phòng trường hợp server chưa
                // đồng bộ)
                List<OrderModel> filtered = new ArrayList<>(orders);
                List<OrderModel> allOrders = orderService.getAllOrders();
                for (OrderModel o : allOrders) {
                    // Thêm order chưa có trong danh sách nhưng được đặt trong session này
                    boolean alreadyIn = filtered.stream().anyMatch(f -> f.getMaorder().equals(o.getMaorder()));
                    if (!alreadyIn && CustomerSession.hasPlacedOrderCode(o.getMaorder())) {
                        filtered.add(o);
                    }
                }

                // Chuyển đổi sang model.Order để tương thích với updateUI
                List<Order> resultOrders = new ArrayList<>();
                for (OrderModel om : filtered) {
                    Order o = new Order();
                    o.setMaOrder(om.getMaorder());
                    o.setMaBan(om.getMaban());
                    o.setMaNV(om.getManv());
                    o.setMaKH(om.getMakh());
                    o.setMaHD(om.getMahd());
                    o.setNgayTao(om.getNgaytao() != null ? new java.sql.Timestamp(om.getNgaytao().getTime()) : null);
                    o.setTrangThai(om.getTrangthai());
                    o.setTongMon(om.getTongMon());
                    o.setTongSoLuong(om.getTongSoLuong());
                    o.setTongTien(om.getTongTien());
                    resultOrders.add(o);
                }

                Platform.runLater(() -> updateUI(resultOrders));

            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void updateUI(List<Order> orders) {
        if (ordersContainer == null)
            return;
        ordersContainer.getChildren().clear();

        if (orders.isEmpty()) {
            EmptyState empty = new EmptyState("📋", "Chưa có đơn hàng nào",
                    "Các món ăn quý khách gọi sẽ hiển thị ở đây để theo dõi chế biến.");
            ordersContainer.getChildren().add(empty);
            return;
        }

        // Sắp xếp theo ngày tạo mới nhất
        orders.sort((o1, o2) -> {
            if (o1.getNgayTao() == null && o2.getNgayTao() == null)
                return 0;
            if (o1.getNgayTao() == null)
                return 1;
            if (o2.getNgayTao() == null)
                return -1;
            return o2.getNgayTao().compareTo(o1.getNgayTao());
        });

        for (Order o : orders) {
            HBox card = new HBox();
            card.getStyleClass().add("order-card");

            VBox infoBox = new VBox(5);
            Label lblCode = new Label("Mã đơn: " + o.getMaOrder());
            lblCode.getStyleClass().add("order-code-label");

            String timeStr = o.getNgayTao() != null ? sdf.format(o.getNgayTao()) : "Chưa xác định";
            Label lblDate = new Label("Ngày tạo: " + timeStr);
            lblDate.getStyleClass().add("order-date-label");

            infoBox.getChildren().addAll(lblCode, lblDate);

            // Badge trạng thái
            String status = o.getTrangThai() != null ? o.getTrangThai() : "Đang chờ";

            // Map "Đang order" to "Đang xử lý" cho UI
            String displayStatus = status.equalsIgnoreCase("Đang order") ? "Đang xử lý" : status;
            Label lblStatus = new Label(displayStatus);
            lblStatus.getStyleClass().add("status-badge");

            if (status.equalsIgnoreCase("Hoàn thành") || status.equalsIgnoreCase("Đã phục vụ")) {
                lblStatus.getStyleClass().add("badge-available");
            } else if (status.equalsIgnoreCase("Đang order") || status.equalsIgnoreCase("Đang chờ")
                    || status.equalsIgnoreCase("Chờ xác nhận")) {
                lblStatus.getStyleClass().add("badge-pending");
            } else {
                lblStatus.getStyleClass().add("badge-active");
            }

            // Tổng tiền
            long priceVal = (long) o.getTongTien();
            Label lblPrice = new Label(String.format(Locale.US, "%,dđ", priceVal));
            lblPrice.getStyleClass().add("order-price-label");
            lblPrice.setPrefWidth(120);
            lblPrice.setAlignment(Pos.CENTER_RIGHT);

            // Nút chi tiết
            Button btnView = new Button("Chi tiết");
            btnView.getStyleClass().add("btn-primary");
            btnView.setOnAction(e -> handleViewDetail(o));

            Button btnPay = null;
            if ("Đang order".equalsIgnoreCase(status) || "Đang phục vụ".equalsIgnoreCase(status)
                    || "Đang chờ".equalsIgnoreCase(status) || "Đang chế biến".equalsIgnoreCase(status)
                    || "Đã xong".equalsIgnoreCase(status)) {
                btnPay = new Button("Thanh toán");
                btnPay.getStyleClass().add("btn-success");
                btnPay.setStyle("-fx-background-color: #059669; -fx-text-fill: white;");
                btnPay.setOnAction(e -> performCheckout(o, () -> loadOrders()));
            }

            Region reg1 = new Region();
            HBox.setHgrow(reg1, Priority.ALWAYS);
            Region reg2 = new Region();
            reg2.setPrefWidth(15);
            Region reg3 = new Region();
            reg3.setPrefWidth(15);

            HBox actionsBox = new HBox(10);
            actionsBox.setAlignment(Pos.CENTER_RIGHT);
            if (btnPay != null) {
                actionsBox.getChildren().addAll(btnPay, btnView);
            } else {
                actionsBox.getChildren().add(btnView);
            }

            card.getChildren().addAll(infoBox, reg1, lblStatus, reg2, lblPrice, reg3, actionsBox);
            ordersContainer.getChildren().add(card);
        }
    }

    private void handleViewDetail(Order order) {
        selectedOrder = order;
        if (DashboardController.getInstance() != null) {
            try {
                javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(
                        getClass().getResource("/fxml/customer/customer_orderdetail.fxml"));
                javafx.scene.Parent view = loader.load();
                AnchorPane contentArea = (AnchorPane) DashboardController.getInstance().getScene()
                        .lookup("#contentArea");
                if (contentArea != null) {
                    contentArea.getChildren().clear();
                    contentArea.getChildren().add(view);
                    AnchorPane.setTopAnchor(view, 0.0);
                    AnchorPane.setBottomAnchor(view, 0.0);
                    AnchorPane.setLeftAnchor(view, 0.0);
                    AnchorPane.setRightAnchor(view, 0.0);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static void performCheckout(Order order, Runnable onComplete) {
        if (DashboardController.getInstance() != null) {
            DashboardController.getInstance().navigateToPay();
        }
    }

    private static void showCheckoutConfirmDialog(String maKH, Map<String, Object> data, Runnable onComplete) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Xác nhận thanh toán");
        dialog.setHeaderText(null);

        DialogPane dialogPane = dialog.getDialogPane();
        try {
            String commonCss = CustomerOrderController.class.getResource("/css/customer/common.css").toExternalForm();
            String componentCss = CustomerOrderController.class.getResource("/css/customer/component.css")
                    .toExternalForm();
            dialogPane.getStylesheets().addAll(commonCss, componentCss);
        } catch (Exception e) {
            // Ignore
        }
        dialogPane.getStyleClass().add("dialog-card");

        VBox root = new VBox(15);
        root.setPadding(new Insets(20));
        root.setPrefWidth(450);

        Label lblTitle = new Label("HÓA ĐƠN THANH TOÁN");
        lblTitle.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #1e293b;");
        lblTitle.setAlignment(Pos.CENTER);

        Label lblOrder = new Label("Mã đơn: " + data.get("maorder") + "  |  Bàn: " + data.get("maban"));
        lblOrder.setStyle("-fx-font-size: 13px; -fx-text-fill: #64748b;");

        // Items list container
        VBox itemsBox = new VBox(8);
        itemsBox.setStyle("-fx-background-color: #f8fafc; -fx-padding: 12; -fx-background-radius: 8;");

        List<Map<String, Object>> items = (List<Map<String, Object>>) data.get("items");
        for (Map<String, Object> item : items) {
            HBox itemRow = new HBox();
            Label lblName = new Label((String) item.get("tenmon"));
            lblName.setStyle("-fx-font-weight: bold; -fx-text-fill: #334155;");

            int qty = (int) item.get("soluong");
            BigDecimal price = (BigDecimal) item.get("dongia");
            BigDecimal rowTotal = price.multiply(BigDecimal.valueOf(qty));

            Label lblDesc = new Label("  x" + qty);
            lblDesc.setStyle("-fx-text-fill: #64748b;");

            Label lblTotal = new Label(String.format(Locale.US, "%,.0fđ", rowTotal.doubleValue()));
            lblTotal.setStyle("-fx-font-weight: bold; -fx-text-fill: #334155;");

            Region reg = new Region();
            HBox.setHgrow(reg, Priority.ALWAYS);

            itemRow.getChildren().addAll(lblName, lblDesc, reg, lblTotal);
            itemsBox.getChildren().add(itemRow);
        }

        ScrollPane scroll = new ScrollPane(itemsBox);
        scroll.setFitToWidth(true);
        scroll.setPrefHeight(150);
        scroll.setStyle("-fx-background-color: transparent; -fx-background: transparent;");

        // Calculations
        BigDecimal subtotal = (BigDecimal) data.get("subtotal");
        BigDecimal tax = (BigDecimal) data.get("tax");
        BigDecimal discount = (BigDecimal) data.get("discount");
        BigDecimal grandTotal = (BigDecimal) data.get("grandTotal");

        VBox calcBox = new VBox(8);
        calcBox.setStyle("-fx-padding: 10 0 0 0; -fx-border-color: #e2e8f0; -fx-border-width: 1 0 0 0;");

        calcBox.getChildren().add(createCalcRow("Tổng món:", String.valueOf(data.get("totalQty")) + " món"));
        calcBox.getChildren()
                .add(createCalcRow("Tổng tiền:", String.format(Locale.US, "%,.0f VNĐ", subtotal.doubleValue())));
        calcBox.getChildren().add(createCalcRow("Thuế:", String.format(Locale.US, "%,.0f VNĐ", tax.doubleValue())));
        calcBox.getChildren()
                .add(createCalcRow("Giảm giá:", String.format(Locale.US, "%,.0f VNĐ", discount.doubleValue())));

        HBox totalRow = new HBox();
        Label lblTotalLabel = new Label("Thành tiền:");
        lblTotalLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 16px; -fx-text-fill: #1e293b;");
        Label lblTotalVal = new Label(String.format(Locale.US, "%,.0f VNĐ", grandTotal.doubleValue()));
        lblTotalVal.setStyle("-fx-font-weight: bold; -fx-font-size: 18px; -fx-text-fill: #059669;");
        Region regCalc = new Region();
        HBox.setHgrow(regCalc, Priority.ALWAYS);
        totalRow.getChildren().addAll(lblTotalLabel, regCalc, lblTotalVal);
        calcBox.getChildren().add(totalRow);

        root.getChildren().addAll(lblTitle, lblOrder, scroll, calcBox);
        dialogPane.setContent(root);

        dialogPane.getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        Button btnOk = (Button) dialogPane.lookupButton(ButtonType.OK);
        btnOk.setText("Xác nhận thanh toán");
        btnOk.getStyleClass().add("btn-primary");
        Button btnCancel = (Button) dialogPane.lookupButton(ButtonType.CANCEL);
        btnCancel.setText("Hủy");
        btnCancel.getStyleClass().add("btn-secondary");

        Optional<ButtonType> res = dialog.showAndWait();
        if (res.isPresent() && res.get() == ButtonType.OK) {
            new Thread(() -> {
                try {
                    OrderService service = new OrderService();
                    model.HoaDon hd = service.customerConfirmCheckout(maKH, discount);

                    Platform.runLater(() -> {
                        Dialogs.showSuccess("Thanh toán", "Thanh toán thành công.");
                        CartManager.clearCart();

                        showInvoiceDialog(hd, items);

                        CustomerSession.checkActiveBooking(() -> {
                            if (DashboardController.getInstance() != null) {
                                DashboardController.getInstance().refreshCustomerNavigation();
                            }
                            if (onComplete != null) {
                                onComplete.run();
                            }
                        });
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                    Platform.runLater(() -> {
                        Dialogs.showError("Thanh toán thất bại", "Có lỗi xảy ra: " + e.getMessage());
                    });
                }
            }).start();
        }
    }

    private static HBox createCalcRow(String labelText, String valText) {
        HBox row = new HBox();
        Label lbl = new Label(labelText);
        lbl.setStyle("-fx-text-fill: #64748b;");
        Label val = new Label(valText);
        val.setStyle("-fx-font-weight: bold; -fx-text-fill: #334155;");
        Region reg = new Region();
        HBox.setHgrow(reg, Priority.ALWAYS);
        row.getChildren().addAll(lbl, reg, val);
        return row;
    }

    private static void showInvoiceDialog(model.HoaDon hd, List<Map<String, Object>> items) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Hóa đơn đã tạo");
        DialogPane dialogPane = dialog.getDialogPane();
        try {
            String commonCss = CustomerOrderController.class.getResource("/css/customer/common.css").toExternalForm();
            String componentCss = CustomerOrderController.class.getResource("/css/customer/component.css")
                    .toExternalForm();
            dialogPane.getStylesheets().addAll(commonCss, componentCss);
        } catch (Exception e) {
        }
        dialogPane.getStyleClass().add("dialog-card");

        VBox root = new VBox(10);
        root.setPadding(new Insets(20));
        root.setPrefWidth(400);

        Label lblHeader = new Label("HÓA ĐƠN THANH TOÁN");
        lblHeader.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #059669;");
        lblHeader.setAlignment(Pos.CENTER);

        VBox infoBox = new VBox(6);
        infoBox.setStyle("-fx-background-color: #f8fafc; -fx-padding: 10; -fx-background-radius: 8;");
        infoBox.getChildren().addAll(
                new Label("Mã hóa đơn: " + hd.getMaHD()),
                new Label("Bàn: " + hd.getMaBan()),
                new Label("Khách hàng: " + CustomerSession.getCurrentCustomer().getTenKH()),
                new Label("Thời gian ra: "
                        + (hd.getTimeRa() != null ? new SimpleDateFormat("dd/MM/yyyy HH:mm").format(hd.getTimeRa())
                                : "")));

        VBox itemsBox = new VBox(6);
        itemsBox.setStyle("-fx-padding: 10 0;");
        for (Map<String, Object> item : items) {
            HBox itemRow = new HBox();
            Label lblName = new Label((String) item.get("tenmon") + " x" + item.get("soluong"));
            lblName.setStyle("-fx-text-fill: #334155;");

            BigDecimal price = (BigDecimal) item.get("dongia");
            int qty = (int) item.get("soluong");
            Label lblTotal = new Label(
                    String.format(Locale.US, "%,.0fđ", price.multiply(BigDecimal.valueOf(qty)).doubleValue()));
            lblTotal.setStyle("-fx-font-weight: bold; -fx-text-fill: #334155;");

            Region reg = new Region();
            HBox.setHgrow(reg, Priority.ALWAYS);
            itemRow.getChildren().addAll(lblName, reg, lblTotal);
            itemsBox.getChildren().add(itemRow);
        }

        VBox totalsBox = new VBox(6);
        totalsBox.setStyle("-fx-border-color: #e2e8f0; -fx-border-width: 1 0 0 0; -fx-padding: 8 0 0 0;");
        totalsBox.getChildren().addAll(
                createCalcRow("Tổng cộng:", String.format(Locale.US, "%,.0f VNĐ", hd.getTongTien().doubleValue())),
                createCalcRow("Giảm giá:", String.format(Locale.US, "%,.0f VNĐ", hd.getGiamGia().doubleValue())));

        HBox gRow = new HBox();
        Label lblG = new Label("Thực thu:");
        lblG.setStyle("-fx-font-weight: bold; -fx-text-fill: #1e293b;");
        Label lblGV = new Label(String.format(Locale.US, "%,.0f VNĐ", hd.getThanhToan().doubleValue()));
        lblGV.setStyle("-fx-font-weight: bold; -fx-text-fill: #059669; -fx-font-size: 15px;");
        Region r = new Region();
        HBox.setHgrow(r, Priority.ALWAYS);
        gRow.getChildren().addAll(lblG, r, lblGV);
        totalsBox.getChildren().add(gRow);

        root.getChildren().addAll(lblHeader, infoBox, itemsBox, totalsBox);
        dialogPane.setContent(root);
        dialogPane.getButtonTypes().add(ButtonType.OK);

        Button btn = (Button) dialogPane.lookupButton(ButtonType.OK);
        btn.setText("Đóng");
        btn.getStyleClass().add("btn-primary");
        dialog.showAndWait();
    }
}
