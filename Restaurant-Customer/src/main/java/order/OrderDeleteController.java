package order;

import java.net.URL;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.List;
import java.util.ResourceBundle;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
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
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;

import order.OrderController.OrderModel;
import order.OrderController.OrderDetailModel;
import network.Action;
import network.Module;
import network.Request;
import network.Response;
import network.SocketClient;

public class OrderDeleteController implements Initializable {

    @FXML private TextField txtMaOrder;
    @FXML private TextField txtBanAn;
    @FXML private TextField txtNhanVien;
    @FXML private TextField txtMaHD;
    @FXML private TextField txtNgayTao;
    @FXML private TextField txtTrangThai;

    // Table View Details
    @FXML private TableView<OrderDetailModel> detailTable;
    @FXML private TableColumn<OrderDetailModel, Object> colStt;
    @FXML private TableColumn<OrderDetailModel, String> colMamon;
    @FXML private TableColumn<OrderDetailModel, String> colTenmon;
    @FXML private TableColumn<OrderDetailModel, Integer> colSoluong;
    @FXML private TableColumn<OrderDetailModel, Double> colDongia;
    @FXML private TableColumn<OrderDetailModel, Double> colThanhTien;
    @FXML private TableColumn<OrderDetailModel, String> colTrangThaiMon;

    @FXML private Label lblMessage;

    @FXML private Button backBtn;
    @FXML private Button cancelBtn;
    @FXML private Button deleteBtn;

    private OrderModel selectedOrder;
    private final ObservableList<OrderDetailModel> detailsList = FXCollections.observableArrayList();
    private final OrderService orderService = new OrderService();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        selectedOrder = OrderController.selectedOrder;

        // Setup columns
        setupTableColumns();

        // Populate fields
        if (selectedOrder != null) {
            txtMaOrder.setText(selectedOrder.getMaorder());
            txtBanAn.setText(selectedOrder.getMaban());
            txtNhanVien.setText(selectedOrder.getManv() != null ? selectedOrder.getManv() : "--");
            txtMaHD.setText(selectedOrder.getMahd() != null ? selectedOrder.getMahd() : "--");
            txtNgayTao.setText(selectedOrder.getNgayTaoStr());
            txtTrangThai.setText(selectedOrder.getTrangthai());

            // Load details list from DB
            loadDetails();
        }
    }

    private void loadDetails() {
        new Thread(() -> {
            try {
                List<OrderDetailModel> existingDetails = orderService.getDetailsForOrder(selectedOrder.getMaorder());
                Platform.runLater(() -> {
                    detailsList.clear();
                    detailsList.addAll(existingDetails);
                });
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }).start();
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

        // Format Don gia
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

        // Format Thanh tien
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

        // Format Trang thai mon
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

    @FXML
    private void handleDelete(ActionEvent event) {
        if (selectedOrder == null) {
            showError("Không tìm thấy thông tin đơn hàng để xóa.");
            return;
        }

        deleteBtn.setDisable(true);
        cancelBtn.setDisable(true);
        backBtn.setDisable(true);

        new Thread(() -> {
            try {
                // Delete from Database
                orderService.deleteOrder(selectedOrder.getMaorder());

                // Update Table Status to "Trống" on Server via socket
                List<String> payload = List.of(selectedOrder.getMaban(), "Trống");
                Request tableRequest = new Request(Module.BANAN, "UPDATE_TRANG_THAI", payload);
                SocketClient.getInstance().sendRequest(tableRequest);

                Platform.runLater(() -> {
                    showSuccess("Xóa đơn hàng thành công!");
                    new Thread(() -> {
                        try { Thread.sleep(800); } catch (Exception ignored) {}
                        Platform.runLater(this::goBack);
                    }).start();
                });
            } catch (SQLException e) {
                e.printStackTrace();
                Platform.runLater(() -> {
                    showError("Lỗi cơ sở dữ liệu: " + e.getMessage());
                    deleteBtn.setDisable(false);
                    cancelBtn.setDisable(false);
                    backBtn.setDisable(false);
                });
            }
        }).start();
    }

    @FXML
    private void handleCancel(ActionEvent event) {
        goBack();
    }

    @FXML
    private void handleBack(ActionEvent event) {
        goBack();
    }

    private void goBack() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/order.fxml"));
            Parent view = loader.load();
            AnchorPane parent = (AnchorPane) txtMaOrder.getScene().lookup("#contentArea");
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

    private void showError(String msg) {
        lblMessage.getStyleClass().removeAll("message-error", "message-success");
        lblMessage.getStyleClass().add("message-error");
        lblMessage.setText(msg);
    }

    private void showSuccess(String msg) {
        lblMessage.getStyleClass().removeAll("message-error", "message-success");
        lblMessage.getStyleClass().add("message-success");
        lblMessage.setText(msg);
    }
}
