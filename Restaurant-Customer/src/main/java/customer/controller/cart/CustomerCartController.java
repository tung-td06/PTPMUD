package customer.controller.cart;

import customer.controller.component.Dialogs;
import customer.controller.component.EmptyState;
import dashboard.DashboardController;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Rectangle;
import model.MonAn;
import java.net.URL;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.List;
import java.util.ArrayList;
import java.util.Optional;
import java.text.SimpleDateFormat;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ScrollPane;
import javafx.geometry.Insets;
import javafx.scene.layout.StackPane;
import javafx.scene.Node;
import network.Request;
import network.Response;
import network.SocketClient;

/**
 * Controller giỏ hàng: chỉ hiển thị và quản lý danh sách món đã chọn.
 * Toàn bộ logic thanh toán được xử lý tại CustomerCheckoutController.
 */
public class CustomerCartController implements Initializable {

    @FXML
    private AnchorPane rootPane;

    @FXML
    private VBox cartItemsContainer;

    @FXML
    private Label lblTotalCount;

    @FXML
    private Label lblSubtotal;

    @FXML
    private Label lblGrandTotal;

    @FXML
    private Button btnClearAll;

    @FXML
    private Button btnOrder;

    private final Runnable cartListener = this::refreshCart;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        CartManager.addListener(cartListener);
        refreshCart();

        // Dọn listener khi view bị gỡ khỏi scene để tránh memory leak
        cartItemsContainer.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene == null) {
                CartManager.removeListener(cartListener);
            }
        });
    }

    private void refreshCart() {
        if (cartItemsContainer == null) return;
        cartItemsContainer.getChildren().clear();

        boolean hasBooking = (customer.controller.util.CustomerSession.getActiveBooking() != null);

        var items = CartManager.getCartItems();
        if (items.isEmpty()) {
            EmptyState empty = new EmptyState("🛒", "Giỏ hàng trống", "Bạn chưa thêm món ăn nào. Hãy quay lại Thực đơn để chọn món nhé!");
            cartItemsContainer.getChildren().add(empty);
            if (btnOrder != null) btnOrder.setDisable(true);
            btnClearAll.setDisable(true);
            lblTotalCount.setText("0 món");
            lblSubtotal.setText("0đ");
            lblGrandTotal.setText("0đ");
            return;
        }

        if (btnOrder != null) btnOrder.setDisable(!hasBooking);
        btnClearAll.setDisable(false);

        long grandTotal = CartManager.getTotal().longValue();
        lblTotalCount.setText(CartManager.getTotalCount() + " món");
        lblSubtotal.setText(String.format(Locale.US, "%,dđ", grandTotal));
        lblGrandTotal.setText(String.format(Locale.US, "%,dđ", grandTotal));

        for (var item : items) {
            MonAn food = item.getFood();

            HBox card = new HBox();
            card.getStyleClass().add("cart-item-card");

            // Ảnh món
            ImageView iv = new ImageView();
            iv.setFitWidth(80);
            iv.setFitHeight(60);
            iv.setPreserveRatio(false);
            Rectangle clip = new Rectangle(80, 60);
            clip.setArcWidth(12);
            clip.setArcHeight(12);
            iv.setClip(clip);

            try {
                String path = "/images/default_food.png";
                if (food.getAnh() != null && !food.getAnh().trim().isEmpty()) {
                    String p = "/images/" + food.getAnh();
                    if (getClass().getResource(p) != null) {
                        path = p;
                    }
                }
                Image image = customer.controller.util.ImageCache.getImage(path);
                if (image != null) {
                    iv.setImage(image);
                }
            } catch (Exception e) {
                // Ignore
            }

            // Tên và giá
            VBox nameBox = new VBox(4);
            Label lblName = new Label(food.getTenMon());
            lblName.getStyleClass().add("cart-item-title");
            long unitPrice = food.getDonGia() != null ? food.getDonGia().longValue() : 0L;
            Label lblPrice = new Label(String.format(Locale.US, "đơn giá: %,dđ", unitPrice));
            lblPrice.getStyleClass().add("cart-item-price");

            if (item.getNote() != null && !item.getNote().isEmpty()) {
                Label lblNote = new Label("Ghi chú: " + item.getNote());
                lblNote.getStyleClass().add("item-note-label");
                nameBox.getChildren().addAll(lblName, lblPrice, lblNote);
            } else {
                nameBox.getChildren().addAll(lblName, lblPrice);
            }

            // Điều chỉnh số lượng
            HBox qBox = new HBox(8);
            qBox.setAlignment(Pos.CENTER);
            Button btnDec = new Button("-");
            btnDec.getStyleClass().add("cart-quantity-btn");
            btnDec.setOnAction(e -> {
                new Thread(() -> {
                    boolean valid = customer.controller.util.CustomerSession.checkBookingValidSync();
                    Platform.runLater(() -> {
                        if (!valid) {
                            Dialogs.showError("Giỏ hàng", "Bạn chưa nhận bàn hoặc thời gian sử dụng bàn đã hết. Không thể gọi món.");
                            refreshCart();
                        } else {
                            CartManager.updateQuantity(food.getMaMon(), item.getQuantity() - 1);
                        }
                    });
                }).start();
            });

            Label lblQ = new Label(String.valueOf(item.getQuantity()));
            lblQ.getStyleClass().add("cart-quantity-label");

            Button btnInc = new Button("+");
            btnInc.getStyleClass().add("cart-quantity-btn");
            btnInc.setOnAction(e -> {
                new Thread(() -> {
                    boolean valid = customer.controller.util.CustomerSession.checkBookingValidSync();
                    Platform.runLater(() -> {
                        if (!valid) {
                            Dialogs.showError("Giỏ hàng", "Bạn chưa nhận bàn hoặc thời gian sử dụng bàn đã hết. Không thể gọi món.");
                            refreshCart();
                        } else {
                            CartManager.updateQuantity(food.getMaMon(), item.getQuantity() + 1);
                        }
                    });
                }).start();
            });

            qBox.getChildren().addAll(btnDec, lblQ, btnInc);

            // Thành tiền
            long subtotal = item.getSubtotal().longValue();
            Label lblSub = new Label(String.format(Locale.US, "%,dđ", subtotal));
            lblSub.getStyleClass().add("cart-item-subtotal");
            lblSub.setPrefWidth(100);
            lblSub.setAlignment(Pos.CENTER_RIGHT);

            // Nút xóa
            Button btnDel = new Button("❌");
            btnDel.getStyleClass().add("cart-delete-btn");
            btnDel.setOnAction(e -> {
                new Thread(() -> {
                    boolean valid = customer.controller.util.CustomerSession.checkBookingValidSync();
                    Platform.runLater(() -> {
                        if (!valid) {
                            Dialogs.showError("Giỏ hàng", "Bạn chưa nhận bàn hoặc thời gian sử dụng bàn đã hết. Không thể gọi món.");
                            refreshCart();
                        } else {
                            if (Dialogs.showConfirm("Xóa món ăn", "Bạn chắc chắn muốn xóa " + food.getTenMon() + " khỏi giỏ hàng chứ?")) {
                                CartManager.removeFromCart(food.getMaMon());
                            }
                        }
                    });
                }).start();
            });

            Region reg1 = new Region();
            HBox.setHgrow(reg1, Priority.ALWAYS);
            Region reg2 = new Region();
            reg2.setPrefWidth(15);

            card.getChildren().addAll(iv, nameBox, reg1, qBox, reg2, lblSub, btnDel);
            cartItemsContainer.getChildren().add(card);
        }
    }

    @FXML
    private void handleClearAll() {
        new Thread(() -> {
            boolean valid = customer.controller.util.CustomerSession.checkBookingValidSync();
            Platform.runLater(() -> {
                if (!valid) {
                    Dialogs.showError("Giỏ hàng", "Bạn chưa nhận bàn hoặc thời gian sử dụng bàn đã hết. Không thể gọi món.");
                    refreshCart();
                } else {
                    if (Dialogs.showConfirm("Xóa giỏ hàng", "Bạn chắc chắn muốn xóa sạch toàn bộ món ăn trong giỏ hàng chứ?")) {
                        CartManager.clearCart();
                    }
                }
            });
        }).start();
    }

    /**
     * Thực hiện gửi gọi món ăn lên server.
     */
    @FXML
    private void handleOrder() {
        var cartItems = CartManager.getCartItems();
        if (cartItems.isEmpty()) {
            Dialogs.showError("Gọi món", "Giỏ hàng rỗng! Không thể tiến hành gọi món.");
            return;
        }

        var activeBooking = customer.controller.util.CustomerSession.getActiveBooking();
        if (activeBooking == null) {
            Dialogs.showError("Gọi món", "Bạn chưa nhận bàn hoặc thời gian sử dụng bàn đã hết. Không thể gọi món.");
            return;
        }

        if (!Dialogs.showConfirm("Gọi món", "Quý khách có chắc chắn muốn gửi gọi các món ăn này không?")) {
            return;
        }

        if (btnOrder != null) btnOrder.setDisable(true);
        final String maKH = customer.controller.util.CustomerSession.getCurrentCustomer().getMaKH();
        final String maBan = activeBooking.getMaBan();

        new Thread(() -> {
            try {
                // Tạo danh sách chi tiết đơn từ giỏ hàng
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

                // Gửi gọi món lên server
                order.OrderService orderService = new order.OrderService();
                List<String> resData = orderService.customerAddOrder(maKH, maBan, details);
                String finalOrderCode = resData.get(0);
                String empName = resData.get(1);
                String empId = resData.get(2);
                customer.controller.util.CustomerSession.addPlacedOrderCode(finalOrderCode);

                Platform.runLater(() -> {
                    Dialogs.showSuccess("Gọi món", "Gọi món thành công.\nNhân viên phục vụ: " + empName + " (Mã " + empId + ")");
                    CartManager.clearCart();
                    refreshCart();
                    if (DashboardController.getInstance() != null) {
                        DashboardController.getInstance().refreshCustomerNavigation();
                    }
                });

            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() -> {
                    String msg = e.getMessage();
                    if (msg != null && msg.contains("BOOKING_NOT_VALID")) {
                        Dialogs.showError("Lỗi gọi món", "Bạn chưa nhận bàn hoặc thời gian sử dụng bàn đã hết.");
                    } else if (msg != null && msg.contains("TIME_NOT_VALID")) {
                        Dialogs.showError("Lỗi gọi món", "Thời gian đặt bàn hiện tại của bạn không hợp lệ.");
                    } else if (msg != null && msg.contains("NO_STAFF_AVAILABLE")) {
                        Dialogs.showError("Lỗi gọi món", "Hiện chưa có nhân viên phục vụ khả dụng.\nVui lòng thử lại sau vài phút.");
                    } else {
                        Dialogs.showError("Lỗi gọi món", msg != null ? msg : "Có lỗi xảy ra, vui lòng thử lại sau.");
                    }
                    if (btnOrder != null) btnOrder.setDisable(false);
                });
            }
        }).start();
    }

}
