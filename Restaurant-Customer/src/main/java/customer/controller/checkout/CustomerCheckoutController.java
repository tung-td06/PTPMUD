package customer.controller.checkout;

import customer.controller.cart.CartManager;
import customer.controller.component.Dialogs;
import customer.controller.util.CustomerSession;
import dashboard.DashboardController;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import model.KhachHang;
import network.Action;
import network.Module;
import network.Request;
import network.SocketClient;
import order.OrderController.OrderDetailModel;
import order.OrderController.OrderModel;
import order.OrderService;
import java.net.URL;
import java.sql.Date;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.ResourceBundle;

public class CustomerCheckoutController implements Initializable {

    @FXML
    private Label lblCustomerName;

    @FXML
    private Label lblCustomerPhone;

    @FXML
    private Label lblCustomerUsername;

    @FXML
    private Label lblCustomerPoints;

    @FXML
    private TextField txtCheckoutNote;

    @FXML
    private VBox checkoutItemsContainer;

    @FXML
    private Label lblSubtotal;

    @FXML
    private Label lblGrandTotal;

    @FXML
    private Button btnPlaceOrder;

    @FXML
    private Button btnBackToCart;

    private KhachHang currentCustomer;
    private long subtotal = 0;
    private long grandTotal = 0;
    private final OrderService orderService = new OrderService();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        currentCustomer = CustomerSession.getCurrentCustomer();
        if (currentCustomer == null) {
            handleBackToCart();
            return;
        }

        // Bind Customer Info
        lblCustomerName.setText(currentCustomer.getTenKH());
        lblCustomerPhone.setText(currentCustomer.getSdt());
        lblCustomerUsername.setText(CustomerSession.getLoggedInAccount().getTenDN());
        lblCustomerPoints.setText(currentCustomer.getDiemTichLuy() + " điểm");

        // Populate Order Items
        populateItems();

        // Calculate Initial Billing
        subtotal = CartManager.getTotal().longValue();
        updateTotals();
    }

    private void populateItems() {
        checkoutItemsContainer.getChildren().clear();
        var items = CartManager.getCartItems();
        for (var item : items) {
            HBox row = new HBox();
            row.getStyleClass().add("checkout-item-row");

            VBox nameBox = new VBox(2);
            Label lblName = new Label(item.getFood().getTenMon());
            lblName.getStyleClass().add("checkout-item-name");

            String details = item.getQuantity() + " x " + String.format(Locale.US, "%,dđ", item.getFood().getDonGia().longValue());
            if (item.getNote() != null && !item.getNote().isEmpty()) {
                details += " (" + item.getNote() + ")";
            }
            Label lblDetails = new Label(details);
            lblDetails.getStyleClass().add("checkout-item-details");
            nameBox.getChildren().addAll(lblName, lblDetails);

            long rowTotal = item.getSubtotal().longValue();
            Label lblPrice = new Label(String.format(Locale.US, "%,dđ", rowTotal));
            lblPrice.getStyleClass().add("checkout-item-price");

            Region reg = new Region();
            HBox.setHgrow(reg, Priority.ALWAYS);

            row.getChildren().addAll(nameBox, reg, lblPrice);
            checkoutItemsContainer.getChildren().add(row);
        }
    }

    private void updateTotals() {
        grandTotal = subtotal;

        lblSubtotal.setText(String.format(Locale.US, "%,d VNĐ", subtotal));
        lblGrandTotal.setText(String.format(Locale.US, "%,d VNĐ", grandTotal));
    }

    @FXML
    private void handleBackToCart() {
        if (DashboardController.getInstance() != null) {
            // Load Cart view
            try {
                javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/fxml/customer/customer_cart.fxml"));
                javafx.scene.Parent view = loader.load();
                AnchorPane contentArea = (AnchorPane) DashboardController.getInstance().getScene().lookup("#contentArea");
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

    @FXML
    private void handlePlaceOrder() {
        var cartItems = CartManager.getCartItems();
        if (cartItems.isEmpty()) {
            Dialogs.showError("Đặt món", "Giỏ hàng rỗng! Không thể tiến hành thanh toán.");
            return;
        }

        if (!Dialogs.showConfirm("Đặt món", "Quý khách có chắc chắn muốn gửi đơn hàng này đi không?")) {
            return;
        }

        btnPlaceOrder.setDisable(true);
        final String maKH = currentCustomer.getMaKH();

        new Thread(() -> {
            try {
                // Tạo danh sách chi tiết đơn với mã đơn trống (Server sẽ tự sinh mã và gán lại)
                List<model.OrderDetail> details = new ArrayList<>();
                for (var item : cartItems) {
                    details.add(new model.OrderDetail(
                            "",
                            item.getFood().getMaMon(),
                            item.getFood().getTenMon(),
                            item.getQuantity(),
                            item.getFood().getDonGia(),
                            "Đang chờ",
                            item.getNote() != null ? item.getNote() : ""
                    ));
                }

                // Gửi đơn hàng lên server và nhận mã order sinh ra từ server
                String maBan = customer.controller.util.CustomerSession.getActiveBooking() != null 
                        ? customer.controller.util.CustomerSession.getActiveBooking().getMaBan() : "";
                List<String> resData = orderService.customerAddOrder(maKH, maBan, details);
                String finalOrderCode = resData.get(0);
                customer.controller.util.CustomerSession.addPlacedOrderCode(finalOrderCode);

                Platform.runLater(() -> {
                    Dialogs.showSuccess("Đặt món", "Đặt món thành công.");
                    CartManager.clearCart();
                    if (DashboardController.getInstance() != null) {
                        DashboardController.getInstance().navigateToTab(4); // Chuyển sang tab Đơn hàng
                    }
                });

            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() -> {
                    String msg = e.getMessage();
                    if (msg != null && msg.contains("BOOKING_NOT_VALID")) {
                        Dialogs.showError("Lỗi đặt món", "Bạn chưa nhận bàn hoặc thời gian sử dụng bàn đã hết.");
                    } else {
                        Dialogs.showError("Lỗi đặt món", msg != null ? msg : "Có lỗi xảy ra, vui lòng thử lại sau.");
                    }
                    btnPlaceOrder.setDisable(false);
                });
            }
        }).start();
    }

    private boolean showQRPaymentDialog(String title, String method, String bankInfo, String accountNo, String accountName, double amount, String orderId) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle(title);
        dialog.setHeaderText(null);

        // Styling dialog pane
        DialogPane dialogPane = dialog.getDialogPane();
        try {
            String commonCss = getClass().getResource("/css/customer/common.css").toExternalForm();
            String componentCss = getClass().getResource("/css/customer/component.css").toExternalForm();
            dialogPane.getStylesheets().addAll(commonCss, componentCss);
        } catch (Exception e) {
            // Ignore stylesheet loading issues if resources are resolving differently
        }
        dialogPane.getStyleClass().add("dialog-card");

        // Custom content layout
        VBox vbox = new VBox(15);
        vbox.setAlignment(Pos.CENTER);
        vbox.setPadding(new Insets(20));
        vbox.setPrefWidth(400);

        Label lblHeader = new Label("QUÉT MÃ QR THANH TOÁN");
        lblHeader.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #1e293b;");

        // Draw a simulated QR Code using GridPane
        javafx.scene.layout.GridPane qrCode = new javafx.scene.layout.GridPane();
        qrCode.setAlignment(Pos.CENTER);
        qrCode.setStyle("-fx-background-color: white; -fx-padding: 15; -fx-border-color: #e2e8f0; -fx-border-radius: 10; -fx-background-radius: 10;");
        qrCode.setPrefSize(180, 180);
        qrCode.setMaxSize(180, 180);

        // Draw the positioning squares of a QR Code
        for (int row = 0; row < 15; row++) {
            for (int col = 0; col < 15; col++) {
                javafx.scene.shape.Rectangle rect = new javafx.scene.shape.Rectangle(10, 10);
                boolean isBlack = false;
                
                // Top-left corner square (7x7)
                if (row < 7 && col < 7) {
                    isBlack = (row == 0 || row == 6 || col == 0 || col == 6) || (row >= 2 && row <= 4 && col >= 2 && col <= 4);
                }
                // Top-right corner square (7x7)
                else if (row < 7 && col >= 8) {
                    int c = col - 8;
                    isBlack = (row == 0 || row == 6 || c == 0 || c == 6) || (row >= 2 && row <= 4 && c >= 2 && c <= 4);
                }
                // Bottom-left corner square (7x7)
                else if (row >= 8 && col < 7) {
                    int r = row - 8;
                    isBlack = (r == 0 || r == 6 || col == 0 || col == 6) || (r >= 2 && r <= 4 && col >= 2 && col <= 4);
                }
                // Random noise for the rest of the QR Code
                else {
                    isBlack = Math.random() > 0.5;
                }
                
                rect.setFill(isBlack ? javafx.scene.paint.Color.web("#1e293b") : javafx.scene.paint.Color.WHITE);
                qrCode.add(rect, col, row);
            }
        }

        // Info details card
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
        } catch (Exception e) {
            // Ignore stylesheet loading issues if resources are resolving differently
        }
        dialogPane.getStyleClass().add("dialog-card");

        VBox vbox = new VBox(15);
        vbox.setPadding(new Insets(20));
        vbox.setPrefWidth(420);

        Label lblHeader = new Label("NHẬP THÔNG TIN THẺ");
        lblHeader.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #1e293b;");

        // Card Mockup layout
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

        javafx.scene.control.PasswordField txtCvv = new javafx.scene.control.PasswordField();
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

        // Validation on OK click
        btnOk.addEventFilter(javafx.event.ActionEvent.ACTION, event -> {
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
}
