package customer.controller.payment;

import customer.controller.component.Dialogs;
import customer.controller.util.CustomerSession;
import dashboard.DashboardController;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.CheckBox;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import network.Request;
import network.Response;
import network.SocketClient;
import order.OrderService;
import model.Order;
import customer.controller.cart.CartManager;

import java.math.BigDecimal;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.LinkedHashMap;
import java.util.Optional;
import java.util.ResourceBundle;

public class CustomerPaymentController implements Initializable {

    @FXML
    private AnchorPane rootPane;

    @FXML
    private VBox ordersContainer;

    @FXML
    private Label lblTableName;

    @FXML
    private Label lblBookingCode;

    @FXML
    private Label lblCheckInTime;

    @FXML
    private Label lblSubtotal;

    @FXML
    private Label lblGrandTotal;

    @FXML
    private Button btnPayConfirm;

    @FXML
    private RadioButton radTransfer;

    @FXML
    private RadioButton radCash;

    @FXML
    private ToggleGroup paymentMethodGroup;

    @FXML
    private CheckBox chkUsePoints;

    @FXML
    private Label lblPointsBalance;

    @FXML
    private HBox rowDiscount;

    @FXML
    private Label lblDiscount;

    @FXML
    private HBox rowPointsEarned;

    @FXML
    private Label lblPointsEarned;

    private double originalSubtotal = 0.0;

    private final OrderService orderService = new OrderService();
    private final SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");
    private StackPane loadingOverlay;
    private volatile boolean pollingActive = false;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        btnPayConfirm.setDisable(true);
        lblTableName.setText("Đang tải...");
        lblBookingCode.setText("Đang tải...");
        lblCheckInTime.setText("Đang tải...");

        CustomerSession.checkActiveBooking(() -> {
            var activeBooking = CustomerSession.getActiveBooking();
            if (activeBooking != null) {
                lblTableName.setText(activeBooking.getMaBan());
                lblBookingCode.setText(activeBooking.getMaDatBan());
                if (activeBooking.getThoiGianDen() != null) {
                    lblCheckInTime.setText(sdf.format(activeBooking.getThoiGianDen()));
                } else {
                    lblCheckInTime.setText("Vừa xong");
                }
            } else {
                lblTableName.setText("Chưa nhận bàn");
                lblBookingCode.setText("N/A");
                lblCheckInTime.setText("N/A");
            }
            fetchCustomerPointsAndLoadOrders();
        });
    }

    private void fetchCustomerPointsAndLoadOrders() {
        var customer = CustomerSession.getCurrentCustomer();
        if (customer == null) {
            loadUnpaidOrders();
            return;
        }

        new Thread(() -> {
            try {
                Request req = new Request(network.Module.KHACHHANG, network.Action.GET_BY_ID, customer.getMaKH());
                Response res = SocketClient.getInstance().sendRequest(req);
                if (res != null && res.isSuccess() && res.getData() != null) {
                    model.KhachHang kh = (model.KhachHang) res.getData();
                    Platform.runLater(() -> {
                        customer.setDiemTichLuy(kh.getDiemTichLuy());
                        lblPointsBalance.setText("Điểm hiện có: " + kh.getDiemTichLuy() + " điểm");
                    });
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                Platform.runLater(() -> {
                    lblPointsBalance.setText("Điểm hiện có: " + customer.getDiemTichLuy() + " điểm");
                    loadUnpaidOrders();
                });
            }
        }).start();
    }

    private void updateCalculationUI() {
        lblSubtotal.setText(String.format(Locale.US, "%,dđ", (long) originalSubtotal));

        double discount = 0.0;
        var customer = CustomerSession.getCurrentCustomer();

        if (chkUsePoints.isSelected() && customer != null) {
            int currentPoints = customer.getDiemTichLuy();
            double possibleDiscount = currentPoints * 10.0;
            if (possibleDiscount > originalSubtotal) {
                discount = originalSubtotal;
            } else {
                discount = possibleDiscount;
            }

            rowDiscount.setVisible(true);
            rowDiscount.setManaged(true);
            lblDiscount.setText(String.format(Locale.US, "-%,dđ", (long) discount));
        } else {
            rowDiscount.setVisible(false);
            rowDiscount.setManaged(false);
            lblDiscount.setText("0đ");
        }

        double grandTotal = originalSubtotal - discount;
        if (grandTotal < 0.0)
            grandTotal = 0.0;

        lblGrandTotal.setText(String.format(Locale.US, "%,dđ", (long) grandTotal));

        int pointsEarned = (int) (grandTotal / 1000.0);
        lblPointsEarned.setText(String.format(Locale.US, "+%d điểm", pointsEarned));
    }

    @FXML
    private void handleUsePointsToggle() {
        updateCalculationUI();
    }

    private void loadUnpaidOrders() {
        if (ordersContainer == null)
            return;
        ordersContainer.getChildren().clear();

        var activeBooking = CustomerSession.getActiveBooking();
        if (activeBooking == null) {
            originalSubtotal = 0.0;
            updateCalculationUI();
            btnPayConfirm.setDisable(true);
            return;
        }

        final String maKH = CustomerSession.getCurrentCustomer().getMaKH();

        new Thread(() -> {
            try {
                List<Order> unpaid = orderService.getValidOrdersForCheckout(maKH);

                Platform.runLater(() -> {
                    if (unpaid.isEmpty()) {
                        Label emptyLabel = new Label("Chưa có món hoàn thành để thanh toán.");
                        emptyLabel.setStyle("-fx-text-fill: #64748b; -fx-font-size: 14px; -fx-font-style: italic;");
                        ordersContainer.getChildren().add(emptyLabel);

                        originalSubtotal = 0.0;
                        updateCalculationUI();
                        btnPayConfirm.setDisable(true);
                        return;
                    }

                    btnPayConfirm.setDisable(false);
                    double totalAll = 0.0;

                    for (Order o : unpaid) {
                        totalAll += o.getTongTien();

                        VBox orderCard = new VBox(10);
                        orderCard.setStyle(
                                "-fx-background-color: #ffffff; -fx-padding: 15; -fx-background-radius: 10; -fx-border-color: #e2e8f0; -fx-border-width: 1;");

                        Label lblHeader = new Label("Mã đơn: " + o.getMaOrder() + " | Ngày: "
                                + (o.getNgayTao() != null ? sdf.format(o.getNgayTao()) : "Chưa rõ"));
                        lblHeader.setStyle("-fx-font-weight: bold; -fx-text-fill: #0f1f44; -fx-font-size: 14px;");
                        orderCard.getChildren().add(lblHeader);

                        try {
                            List<order.OrderController.OrderDetailModel> details = orderService
                                    .getDetailsForOrder(o.getMaOrder());
                            for (order.OrderController.OrderDetailModel d : details) {
                                HBox row = new HBox(12);
                                row.setAlignment(Pos.CENTER_LEFT);

                                Label lblName = new Label(d.getTenmon());
                                lblName.setStyle("-fx-text-fill: #334155; -fx-font-size: 13px; -fx-font-weight: bold;");
                                Label lblQty = new Label("x" + d.getSoluong());
                                lblQty.setStyle("-fx-text-fill: #64748b; -fx-font-size: 13px;");
                                Label lblPrice = new Label(String.format(Locale.US, "%,.0fđ", d.getThanhtien()));
                                lblPrice.setStyle(
                                        "-fx-font-weight: bold; -fx-text-fill: #475569; -fx-font-size: 13px;");

                                Region r = new Region();
                                HBox.setHgrow(r, Priority.ALWAYS);

                                row.getChildren().addAll(lblName, lblQty, r, lblPrice);
                                orderCard.getChildren().add(row);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                        Label lblOrderTotal = new Label(
                                String.format(Locale.US, "Cộng đơn: %,dđ", (long) o.getTongTien()));
                        lblOrderTotal.setStyle("-fx-font-weight: bold; -fx-text-fill: #b45309; -fx-font-size: 13px;");
                        HBox rightBox = new HBox(lblOrderTotal);
                        rightBox.setAlignment(Pos.CENTER_RIGHT);
                        orderCard.getChildren().add(rightBox);

                        ordersContainer.getChildren().add(orderCard);
                    }

                    originalSubtotal = totalAll;
                    updateCalculationUI();
                });
            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() -> {
                    Dialogs.showError("Thanh toán", "Lỗi tải thông tin đơn hàng: " + e.getMessage());
                });
            }
        }).start();
    }

    @FXML
    private void handleRequestPayment() {
        var activeBooking = CustomerSession.getActiveBooking();
        if (activeBooking == null) {
            Dialogs.showError("Thanh toán", "Bạn chưa nhận bàn ăn!");
            return;
        }

        final String maKH = CustomerSession.getCurrentCustomer().getMaKH();
        final String maBan = activeBooking.getMaBan();
        final String bookingId = activeBooking.getMaDatBan();

        showLoadingOverlay();
        new Thread(() -> {
            try {
                Map<String, Object> checkData = orderService.customerGetCheckoutInfo(maKH);
                BigDecimal subtotal = (BigDecimal) checkData.get("subtotal");
                final double total = subtotal != null ? subtotal.doubleValue() : 0.0;

                Platform.runLater(() -> {
                    hideLoadingOverlay();
                    showPaymentDialog(maKH, maBan, total, bookingId);
                });
            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() -> {
                    hideLoadingOverlay();
                    Dialogs.showError("Thanh toán", "Không thể lấy thông tin thanh toán từ Server: " + e.getMessage());
                });
            }
        }).start();
    }

    private void showPaymentDialog(String maKH, String maBan, double total, String bookingId) {
        String paymentMethod = radTransfer.isSelected() ? "Chuyển khoản" : "Tiền mặt";

        double discount = 0.0;
        int pointsToUse = 0;
        var customer = CustomerSession.getCurrentCustomer();
        if (chkUsePoints.isSelected() && customer != null) {
            int currentPoints = customer.getDiemTichLuy();
            double possibleDiscount = currentPoints * 10.0;
            if (possibleDiscount > total) {
                discount = total;
                pointsToUse = (int) Math.ceil(total / 10.0);
            } else {
                discount = possibleDiscount;
                pointsToUse = currentPoints;
            }
        }
        double amountToPay = total - discount;
        if (amountToPay < 0.0)
            amountToPay = 0.0;
        final int finalPointsToUse = pointsToUse;
        final double finalDiscount = discount;
        final double finalAmountToPay = amountToPay;

        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Thanh toán " + paymentMethod);
        dialog.setHeaderText(null);
        dialog.setResizable(true); // Cho phép co giãn, phóng to cửa sổ

        DialogPane dialogPane = dialog.getDialogPane();

        // Thêm tính năng kéo thả cho ô cửa sổ
        final double[] xOffset = { 0 };
        final double[] yOffset = { 0 };
        dialogPane.setOnMousePressed(event -> {
            xOffset[0] = event.getSceneX();
            yOffset[0] = event.getSceneY();
        });
        dialogPane.setOnMouseDragged(event -> {
            javafx.stage.Window window = dialogPane.getScene().getWindow();
            if (window != null) {
                window.setX(event.getScreenX() - xOffset[0]);
                window.setY(event.getScreenY() - yOffset[0]);
            }
        });
        try {
            String commonCss = getClass().getResource("/css/customer/common.css").toExternalForm();
            String componentCss = getClass().getResource("/css/customer/component.css").toExternalForm();
            dialogPane.getStylesheets().addAll(commonCss, componentCss);
        } catch (Exception e) {
            // ignore
        }
        dialogPane.getStyleClass().add("dialog-card");

        VBox root = new VBox(15);
        root.setPadding(new Insets(20));
        root.setAlignment(Pos.CENTER);
        root.setPrefWidth(420);

        Label lblTitle = new Label("THANH TOÁN " + paymentMethod.toUpperCase());
        lblTitle.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #0f172a;");

        // Format clean table name
        String cleanMaBan = maBan.trim().toUpperCase().replace(" ", "").replace("BÀN", "BAN");
        String addInfo = maKH + "-" + cleanMaBan;

        StackPane qrContainer = new StackPane();
        if ("Chuyển khoản".equals(paymentMethod)) {
            String qrUrl = "https://img.vietqr.io/image/MB-0198581740-compact2.png?amount=" + (long) amountToPay
                    + "&addInfo=" + addInfo + "&accountName=Tran%20Duc%20Tung";

            qrContainer.setStyle(
                    "-fx-background-color: #f8fafc; -fx-border-color: #e2e8f0; -fx-border-width: 1; -fx-border-radius: 8; -fx-background-radius: 8;");

            ImageView qrImageView = new ImageView();
            qrImageView.setPreserveRatio(true);

            // Tự động điều chỉnh kích thước của vùng chứa và ảnh QR theo kích thước của cửa
            // sổ (dialogPane)
            qrContainer.prefWidthProperty().bind(dialogPane.widthProperty().multiply(0.55));
            qrContainer.prefHeightProperty().bind(dialogPane.widthProperty().multiply(0.55));
            qrContainer.setMinWidth(200);
            qrContainer.setMinHeight(200);
            qrContainer.setMaxWidth(500);
            qrContainer.setMaxHeight(500);

            qrImageView.fitWidthProperty().bind(qrContainer.widthProperty().subtract(10));
            qrImageView.fitHeightProperty().bind(qrContainer.heightProperty().subtract(10));

            ProgressIndicator progress = new ProgressIndicator();
            progress.setMaxSize(40, 40);

            qrContainer.getChildren().addAll(qrImageView, progress);

            // Load image in background
            Image qrImage = new Image(qrUrl, true);
            qrImage.progressProperty().addListener((obs, oldVal, newVal) -> {
                if (newVal.doubleValue() == 1.0) {
                    progress.setVisible(false);
                }
            });
            qrImageView.setImage(qrImage);
        } else {
            qrContainer.setVisible(false);
            qrContainer.setManaged(false);
        }

        // Info VBox
        VBox infoBox = new VBox(8);
        infoBox.setStyle("-fx-background-color: #f1f5f9; -fx-padding: 12; -fx-background-radius: 8;");
        infoBox.setAlignment(Pos.CENTER_LEFT);

        if ("Chuyển khoản".equals(paymentMethod)) {
            infoBox.getChildren().addAll(
                    createDetailRow("Ngân hàng:", "MB Bank"),
                    createDetailRow("Số tài khoản:", "0198581740"),
                    createDetailRow("Chủ tài khoản:", "Tran Duc Tung"),
                    createDetailRow("Nội dung:", addInfo),
                    createDetailRow("Tạm tính:", String.format(Locale.US, "%,.0fđ", total)),
                    createDetailRow("Giảm giá:", String.format(Locale.US, "-%,.0fđ", discount)),
                    createDetailRow("Số tiền:", String.format(Locale.US, "%,.0fđ", amountToPay)));
        } else {
            infoBox.getChildren().addAll(
                    createDetailRow("Phương thức:", "Tiền mặt"),
                    createDetailRow("Tạm tính:", String.format(Locale.US, "%,.0fđ", total)),
                    createDetailRow("Giảm giá:", String.format(Locale.US, "-%,.0fđ", discount)),
                    createDetailRow("Số tiền:", String.format(Locale.US, "%,.0fđ", amountToPay)));
        }

        String statusText = "Chuyển khoản".equals(paymentMethod)
                ? "Đang chờ xác nhận chuyển khoản từ nhà hàng..."
                : "Vui lòng thanh toán tại quầy. Đang chờ xác nhận...";
        Label lblStatus = new Label(statusText);
        lblStatus.setStyle("-fx-text-fill: #b45309; -fx-font-size: 13px; -fx-font-style: italic;");

        root.getChildren().addAll(lblTitle, qrContainer, infoBox, lblStatus);
        dialogPane.setContent(root);

        dialogPane.getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        Button btnConfirm = (Button) dialogPane.lookupButton(ButtonType.OK);
        btnConfirm.setText("Xác nhận");
        btnConfirm.setDisable(true);
        btnConfirm.getStyleClass().add("btn-primary");

        Button btnCancel = (Button) dialogPane.lookupButton(ButtonType.CANCEL);
        btnCancel.setText("Hủy");
        btnCancel.getStyleClass().add("btn-secondary");

        // Polling thread
        pollingActive = true;
        Thread pollThread = new Thread(() -> {
            while (pollingActive) {
                try {
                    Thread.sleep(2000);
                    Request req = new Request(network.Module.DATBAN, network.Action.GET_BY_ID, bookingId);
                    Response res = SocketClient.getInstance().sendRequest(req);
                    if (res != null && res.isSuccess() && res.getData() != null) {
                        model.DatBan db = (model.DatBan) res.getData();
                        if ("Hoàn thành".equalsIgnoreCase(db.getTrangThai())) {
                            pollingActive = false;
                            Platform.runLater(() -> {
                                btnConfirm.setDisable(false);
                                lblStatus.setText("Nhà hàng đã xác nhận thanh toán!");
                                lblStatus.setStyle(
                                        "-fx-text-fill: #15803d; -fx-font-size: 13px; -fx-font-weight: bold;");
                            });
                            break;
                        }
                    }
                } catch (Exception e) {
                    // Ignore and keep polling
                }
            }
        });
        pollThread.setDaemon(true);
        pollThread.start();

        dialog.setOnHidden(e -> {
            pollingActive = false;
        });

        // Add event filter to handle OK click with server communication
        btnConfirm.addEventFilter(javafx.event.ActionEvent.ACTION, event -> {
            event.consume(); // prevent default dialog closing

            btnConfirm.setDisable(true);
            btnCancel.setDisable(true);
            lblStatus.setText("Đang xử lý tạo hóa đơn...");
            lblStatus.setStyle("-fx-text-fill: #2563eb; -fx-font-size: 13px; -fx-font-weight: bold;");

            new Thread(() -> {
                try {
                    // Send ConfirmPaymentRequest to Server
                    Request confirmReq = new Request(network.Module.HOADON, "ConfirmPaymentRequest",
                            List.of(maKH, BigDecimal.valueOf(finalDiscount), paymentMethod, finalPointsToUse));
                    Response confirmRes = SocketClient.getInstance().sendRequest(confirmReq);

                    Platform.runLater(() -> {
                        if (confirmRes != null && confirmRes.isSuccess()) {
                            // Update local customer points balance
                            int pointsEarned = (int) (finalAmountToPay / 1000.0);
                            if (customer != null) {
                                int actualDiemTru = (int) Math.ceil(finalDiscount / 10.0);
                                customer.setDiemTichLuy(customer.getDiemTichLuy() - actualDiemTru + pointsEarned);
                            }

                            dialog.close();
                            CartManager.clearCart();

                            CustomerSession.checkActiveBooking(() -> {
                                if (DashboardController.getInstance() != null) {
                                    DashboardController.getInstance().refreshCustomerNavigation();
                                    DashboardController.getInstance()
                                            .loadCustomerView("/fxml/customer/customer_order.fxml");
                                }
                                loadUnpaidOrders();
                            });
                        } else {
                            btnConfirm.setDisable(false);
                            btnCancel.setDisable(false);
                            String errMsg = confirmRes != null ? confirmRes.getMessage()
                                    : "Không thể tạo hóa đơn trên Server.";
                            Dialogs.showError("Thanh toán thất bại", errMsg);
                            lblStatus.setText("Lỗi: " + errMsg);
                            lblStatus.setStyle("-fx-text-fill: #dc2626; -fx-font-size: 13px; -fx-font-weight: bold;");
                        }
                    });
                } catch (Exception ex) {
                    ex.printStackTrace();
                    Platform.runLater(() -> {
                        btnConfirm.setDisable(false);
                        btnCancel.setDisable(false);
                        Dialogs.showError("Thanh toán thất bại", "Có lỗi xảy ra: " + ex.getMessage());
                        lblStatus.setText("Lỗi kết nối Server.");
                        lblStatus.setStyle("-fx-text-fill: #dc2626; -fx-font-size: 13px; -fx-font-weight: bold;");
                    });
                }
            }).start();
        });

        dialog.showAndWait();
    }

    private HBox createDetailRow(String label, String value) {
        HBox row = new HBox(5);
        Label lblL = new Label(label);
        lblL.setStyle("-fx-text-fill: #64748b; -fx-font-size: 13px; -fx-pref-width: 100px;");
        Label lblV = new Label(value);
        lblV.setStyle("-fx-text-fill: #0f172a; -fx-font-size: 13px; -fx-font-weight: bold;");
        row.getChildren().addAll(lblL, lblV);
        return row;
    }

    private void showLoadingOverlay() {
        if (loadingOverlay == null) {
            loadingOverlay = new StackPane();
            loadingOverlay.setStyle("-fx-background-color: rgba(0, 0, 0, 0.6);");

            VBox vbox = new VBox(20);
            vbox.setAlignment(Pos.CENTER);

            javafx.scene.control.ProgressIndicator spinner = new javafx.scene.control.ProgressIndicator();
            spinner.setStyle("-fx-progress-color: #f3c13a;");
            spinner.setPrefSize(80, 80);

            Label label = new Label("Vui lòng chờ xác nhận thanh toán...");
            label.setStyle("-fx-text-fill: white; -fx-font-size: 18px; -fx-font-weight: bold;");

            vbox.getChildren().addAll(spinner, label);
            loadingOverlay.getChildren().add(vbox);
        }

        if (rootPane != null && !rootPane.getChildren().contains(loadingOverlay)) {
            rootPane.getChildren().add(loadingOverlay);
            AnchorPane.setTopAnchor(loadingOverlay, 0.0);
            AnchorPane.setBottomAnchor(loadingOverlay, 0.0);
            AnchorPane.setLeftAnchor(loadingOverlay, 0.0);
            AnchorPane.setRightAnchor(loadingOverlay, 0.0);

            for (Node node : rootPane.getChildren()) {
                if (node != loadingOverlay) {
                    node.setDisable(true);
                }
            }
        }
    }

    private void hideLoadingOverlay() {
        if (rootPane != null && loadingOverlay != null) {
            rootPane.getChildren().remove(loadingOverlay);
            for (Node node : rootPane.getChildren()) {
                node.setDisable(false);
            }
        }
    }
}
