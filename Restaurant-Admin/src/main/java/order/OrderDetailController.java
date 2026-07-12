package order;

import java.net.URL;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.List;
import java.util.ResourceBundle;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.animation.Timeline;
import javafx.animation.KeyFrame;
import javafx.util.Duration;

import order.OrderController.OrderDetailModel;
import order.OrderController.OrderModel;

public class OrderDetailController implements Initializable {

    @FXML private Label lblSubtitle;
    @FXML private Label lblMaOrder;
    @FXML private Label lblMaban;
    @FXML private Label lblManv;
    @FXML private Label lblNgaytao;
    @FXML private Label lblTrangthai;
    @FXML private Label lblMahd;

    @FXML private TableView<OrderDetailModel> detailTable;
    @FXML private TableColumn<OrderDetailModel, Object> colStt;
    @FXML private TableColumn<OrderDetailModel, String> colMamon;
    @FXML private TableColumn<OrderDetailModel, String> colTenmon;
    @FXML private TableColumn<OrderDetailModel, Integer> colSoluong;
    @FXML private TableColumn<OrderDetailModel, Double> colDongia;
    @FXML private TableColumn<OrderDetailModel, Double> colThanhTien;
    @FXML private TableColumn<OrderDetailModel, String> colTrangThaiMon;

    @FXML private Label lblTotalItems;
    @FXML private Label lblTotalQuantity;
    @FXML private Label lblTotalAmount;

    @FXML private Button btnClose;

    private final ObservableList<OrderDetailModel> detailsList = FXCollections.observableArrayList();
    private final OrderService orderService = new OrderService();
    private Timeline autoReloadTimeline;
    private boolean isReloading = false;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        setupTableColumns();
        loadOrderDetail();
        startAutoReload();
    }

    private void setupTableColumns() {
        // STT Auto Increment Column
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

        // Format Đơn giá
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

        // Format Thành tiền
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

        // Format Trạng thái món ăn Status Badges
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
                        label.getStyleClass().add("status-dish-waiting");
                    } else if (item.equalsIgnoreCase("Đang chế biến")) {
                        label.getStyleClass().add("status-dish-cooking");
                    } else if (item.equalsIgnoreCase("Đã xong")) {
                        label.getStyleClass().add("status-dish-done");
                    } else if (item.equalsIgnoreCase("Đã phục vụ")) {
                        label.getStyleClass().add("status-dish-served");
                    } else if (item.equalsIgnoreCase("Đã hủy")) {
                        label.getStyleClass().add("status-dish-cancelled");
                    }
                    HBox box = new HBox(label);
                    box.setAlignment(Pos.CENTER);
                    setGraphic(box);
                }
            }
        });

        detailTable.setItems(detailsList);
    }

    private void loadOrderDetail() {
        OrderModel order = OrderController.selectedOrder;
        if (order == null) {
            return;
        }

        // Set general information labels
        lblSubtitle.setText("Mã Order: " + order.getMaorder() + " | Bàn: " + order.getMaban() + " | Nhân viên: " + order.getManv());
        lblMaOrder.setText(order.getMaorder());
        lblMaban.setText(order.getMaban());
        lblManv.setText(order.getManv() != null ? order.getManv() : "--");
        lblNgaytao.setText(order.getNgayTaoStr());
        lblTrangthai.setText(order.getTrangthai());
        lblMahd.setText(order.getMahd() != null ? order.getMahd() : "--");

        // Apply style to main status label in dialog info
        lblTrangthai.getStyleClass().remove("status-tag");
        lblTrangthai.getStyleClass().remove("status-order-pending");
        lblTrangthai.getStyleClass().remove("status-order-serving");
        lblTrangthai.getStyleClass().remove("status-order-completed");
        lblTrangthai.getStyleClass().remove("status-order-cancelled");

        lblTrangthai.getStyleClass().add("status-tag");
        String stat = order.getTrangthai();
        if (stat.equalsIgnoreCase("Đang order")) {
            lblTrangthai.getStyleClass().add("status-order-pending");
        } else if (stat.equalsIgnoreCase("Đang phục vụ")) {
            lblTrangthai.getStyleClass().add("status-order-serving");
        } else if (stat.equalsIgnoreCase("Hoàn thành")) {
            lblTrangthai.getStyleClass().add("status-order-completed");
        } else if (stat.equalsIgnoreCase("Đã hủy")) {
            lblTrangthai.getStyleClass().add("status-order-cancelled");
        }

        new Thread(() -> {
            try {
                List<OrderDetailModel> details = orderService.getDetailsForOrder(order.getMaorder());
                Platform.runLater(() -> {
                    // Save selection
                    OrderDetailModel selected = detailTable.getSelectionModel().getSelectedItem();

                    detailsList.clear();
                    detailsList.addAll(details);

                    // Restore selection
                    if (selected != null) {
                        for (OrderDetailModel item : detailsList) {
                            if (item.getMamon().equalsIgnoreCase(selected.getMamon())) {
                                detailTable.getSelectionModel().select(item);
                                break;
                            }
                        }
                    }

                    // Compute summary values
                    int totalItems = detailsList.size();
                    int totalQty = 0;
                    double totalAmt = 0.0;

                    for (OrderDetailModel d : detailsList) {
                        totalQty += d.getSoluong();
                        totalAmt += d.getThanhtien();
                    }

                    lblTotalItems.setText(String.valueOf(totalItems));
                    lblTotalQuantity.setText(String.valueOf(totalQty));
                    DecimalFormat df = new DecimalFormat("#,###");
                    lblTotalAmount.setText(df.format(totalAmt) + " VNĐ");
                });
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void loadDataSilently() {
        OrderModel order = OrderController.selectedOrder;
        if (order == null) return;
        isReloading = true;
        new Thread(() -> {
            try {
                // Refresh parent order state in case status has changed
                List<OrderModel> orders = orderService.getAllOrders();
                OrderModel updatedParent = null;
                for (OrderModel o : orders) {
                    if (o.getMaorder().equalsIgnoreCase(order.getMaorder())) {
                        updatedParent = o;
                        break;
                    }
                }
                
                final OrderModel finalParent = updatedParent;
                List<OrderDetailModel> details = orderService.getDetailsForOrder(order.getMaorder());
                
                Platform.runLater(() -> {
                    if (finalParent != null) {
                        OrderController.selectedOrder = finalParent;
                        lblTrangthai.setText(finalParent.getTrangthai());
                        lblTrangthai.getStyleClass().remove("status-tag");
                        lblTrangthai.getStyleClass().remove("status-order-pending");
                        lblTrangthai.getStyleClass().remove("status-order-serving");
                        lblTrangthai.getStyleClass().remove("status-order-completed");
                        lblTrangthai.getStyleClass().remove("status-order-cancelled");

                        lblTrangthai.getStyleClass().add("status-tag");
                        String stat = finalParent.getTrangthai();
                        if (stat.equalsIgnoreCase("Đang order")) {
                            lblTrangthai.getStyleClass().add("status-order-pending");
                        } else if (stat.equalsIgnoreCase("Đang phục vụ")) {
                            lblTrangthai.getStyleClass().add("status-order-serving");
                        } else if (stat.equalsIgnoreCase("Hoàn thành")) {
                            lblTrangthai.getStyleClass().add("status-order-completed");
                        } else if (stat.equalsIgnoreCase("Đã hủy")) {
                            lblTrangthai.getStyleClass().add("status-order-cancelled");
                        }
                    }

                    OrderDetailModel selected = detailTable.getSelectionModel().getSelectedItem();

                    detailsList.clear();
                    detailsList.addAll(details);

                    if (selected != null) {
                        for (OrderDetailModel item : detailsList) {
                            if (item.getMamon().equalsIgnoreCase(selected.getMamon())) {
                                detailTable.getSelectionModel().select(item);
                                break;
                            }
                        }
                    }

                    int totalItems = detailsList.size();
                    int totalQty = 0;
                    double totalAmt = 0.0;

                    for (OrderDetailModel d : detailsList) {
                        totalQty += d.getSoluong();
                        totalAmt += d.getThanhtien();
                    }

                    lblTotalItems.setText(String.valueOf(totalItems));
                    lblTotalQuantity.setText(String.valueOf(totalQty));
                    DecimalFormat df = new DecimalFormat("#,###");
                    lblTotalAmount.setText(df.format(totalAmt) + " VNĐ");

                    isReloading = false;
                });
            } catch (SQLException e) {
                e.printStackTrace();
                Platform.runLater(() -> isReloading = false);
            }
        }).start();
    }

    private void startAutoReload() {
        if (autoReloadTimeline != null) {
            autoReloadTimeline.stop();
        }
        autoReloadTimeline = new Timeline(
            new KeyFrame(Duration.seconds(10), event -> {
                if (detailTable.getScene() == null) {
                    autoReloadTimeline.stop();
                    return;
                }
                if (!isReloading) {
                    loadDataSilently();
                }
            })
        );
        autoReloadTimeline.setCycleCount(Timeline.INDEFINITE);
        autoReloadTimeline.play();

        detailTable.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene == null && autoReloadTimeline != null) {
                autoReloadTimeline.stop();
            }
        });
    }

    @FXML
    private void handleClose() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/order.fxml"));
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
}
