package customer.controller.orderdetail;

import customer.controller.order.CustomerOrderController;
import dashboard.DashboardController;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import model.Order;
import order.OrderController.OrderDetailModel;
import order.OrderService;
import java.net.URL;
import java.sql.SQLException;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

public class CustomerOrderDetailController implements Initializable {

    @FXML
    private Label lblOrderCode;

    @FXML
    private Label lblTotalPrice;

    @FXML
    private VBox itemsContainer;

    @FXML
    private HBox circlePending;
    @FXML
    private HBox circleProcessing;
    @FXML
    private HBox circleReady;
    @FXML
    private HBox circleServed;
    @FXML
    private HBox circleCompleted;

    @FXML
    private Label lblPending;
    @FXML
    private Label lblProcessing;
    @FXML
    private Label lblReady;
    @FXML
    private Label lblServed;
    @FXML
    private Label lblCompleted;

    @FXML
    private Button btnBack;

    @FXML
    private Button btnPay;

    private Order currentOrder;
    private final OrderService orderService = new OrderService();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        currentOrder = CustomerOrderController.selectedOrder;
        if (currentOrder == null) {
            handleBack();
            return;
        }

        // Bind basic details
        lblOrderCode.setText("Chi tiết đơn gọi món: " + currentOrder.getMaOrder());
        lblTotalPrice.setText(String.format(Locale.US, "%,.0fđ", currentOrder.getTongTien()));

        // Bind Timeline Status
        updateTimeline(currentOrder.getTrangThai());

        // Load Items list
        loadOrderDetails();
    }

    private void updateTimeline(String status) {
        int activeStep = 1;
        if (status == null) status = "Đang chờ";

        if (status.equalsIgnoreCase("Đang chế biến")) {
            activeStep = 2;
        } else if (status.equalsIgnoreCase("Đã xong")) {
            activeStep = 3;
        } else if (status.equalsIgnoreCase("Đã phục vụ") || status.equalsIgnoreCase("Đang phục vụ")) {
            activeStep = 4;
        } else if (status.equalsIgnoreCase("Hoàn thành")) {
            activeStep = 5;
        }

        HBox[] circles = { circlePending, circleProcessing, circleReady, circleServed, circleCompleted };
        Label[] labels = { lblPending, lblProcessing, lblReady, lblServed, lblCompleted };

        for (int i = 0; i < 5; i++) {
            int stepNum = i + 1;
            circles[i].getStyleClass().removeAll("step-circle-active", "step-circle-completed");
            labels[i].getStyleClass().removeAll("step-label-active", "step-label-completed");

            if (stepNum < activeStep) {
                // Completed step
                circles[i].getStyleClass().add("step-circle-completed");
                labels[i].getStyleClass().add("step-label-completed");
                
                // Swap circle inner icon to Checkmark
                if (circles[i].getChildren().get(0) instanceof Label lbl) {
                    lbl.setText("✓");
                }
            } else if (stepNum == activeStep) {
                // Active step
                circles[i].getStyleClass().add("step-circle-active");
                labels[i].getStyleClass().add("step-label-active");
                if (circles[i].getChildren().get(0) instanceof Label lbl) {
                    lbl.setText(String.valueOf(stepNum));
                }
            } else {
                // Unreached step
                if (circles[i].getChildren().get(0) instanceof Label lbl) {
                    lbl.setText(String.valueOf(stepNum));
                }
            }
        }
    }

    private void loadOrderDetails() {
        if (itemsContainer == null) return;
        itemsContainer.getChildren().clear();

        new Thread(() -> {
            try {
                String maKH = customer.controller.util.CustomerSession.getCurrentCustomer().getMaKH();
                List<order.OrderController.OrderModel> myOrders = orderService.getOrdersByKhachHang(maKH);
                String latestStatus = currentOrder.getTrangThai();
                for(order.OrderController.OrderModel o : myOrders) {
                    if(o.getMaorder().equals(currentOrder.getMaOrder())) {
                        latestStatus = o.getTrangthai();
                        currentOrder.setTrangThai(latestStatus);
                        break;
                    }
                }
                final String finalStatus = latestStatus;

                List<OrderDetailModel> details = orderService.getDetailsForOrder(currentOrder.getMaOrder());
                Platform.runLater(() -> {
                    updateTimeline(finalStatus);
                    
                    for (OrderDetailModel d : details) {
                        HBox row = new HBox();
                        row.getStyleClass().add("detail-item-row");

                        VBox nameBox = new VBox(2);
                        Label lblName = new Label(d.getTenmon());
                        lblName.getStyleClass().add("detail-item-name");

                        String desc = d.getSoluong() + " x " + String.format(Locale.US, "%,dđ", (long) d.getDongia());
                        Label lblDesc = new Label(desc);
                        lblDesc.getStyleClass().add("detail-item-details");
                        nameBox.getChildren().addAll(lblName, lblDesc);

                        long subtotalVal = (long) (d.getSoluong() * d.getDongia());
                        Label lblSub = new Label(String.format(Locale.US, "%,dđ", subtotalVal));
                        lblSub.getStyleClass().add("detail-item-price");
                        
                        Label lblStatus = new Label(d.getTrangthai() != null ? d.getTrangthai() : "Đang chờ");
                        lblStatus.setStyle("-fx-text-fill: #059669; -fx-font-size: 12px; -fx-font-weight: bold;");
                        
                        VBox rightBox = new VBox(2);
                        rightBox.setAlignment(javafx.geometry.Pos.CENTER_RIGHT);
                        rightBox.getChildren().addAll(lblSub, lblStatus);

                        Region reg = new Region();
                        HBox.setHgrow(reg, Priority.ALWAYS);

                        row.getChildren().addAll(nameBox, reg, rightBox);
                        itemsContainer.getChildren().add(row);
                    }
                    
                    boolean hasActiveOrder = "Đang order".equalsIgnoreCase(finalStatus) || "Đang phục vụ".equalsIgnoreCase(finalStatus) || "Đang chờ".equalsIgnoreCase(finalStatus) || "Đang chế biến".equalsIgnoreCase(finalStatus) || "Đã xong".equalsIgnoreCase(finalStatus);
                    if (hasActiveOrder) {
                        if (btnPay != null) {
                            btnPay.setVisible(true);
                            btnPay.setManaged(true);
                            btnPay.setText("💳 Thanh toán");
                        }
                    } else {
                        if (btnPay != null) {
                            btnPay.setVisible(false);
                            btnPay.setManaged(false);
                        }
                    }
                });
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }).start();
    }

    @FXML
    private void handleBack() {
        if (DashboardController.getInstance() != null) {
            DashboardController.getInstance().navigateToTab(4); // Back to Active Orders tab
        }
    }

    @FXML
    private void handlePay() {
        customer.controller.order.CustomerOrderController.performCheckout(currentOrder, () -> {
            handleBack();
        });
    }
}
