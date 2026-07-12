package order;

import java.net.URL;
import java.sql.Date;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.util.ArrayList;
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
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;

import restaurant.MockDataStore;
import bill.BillController.BillModel;
import table.TableController.TableModel;
import employee.EmployeeController.EmployeeModel;
import food.FoodController.FoodModel;
import order.OrderController.OrderModel;
import order.OrderController.OrderDetailModel;
import network.Action;
import network.Module;
import network.Request;
import network.Response;
import network.SocketClient;

public class OrderAddController implements Initializable {

    @FXML private TextField txtMaOrder;
    @FXML private ComboBox<String> cbBanAn;
    @FXML private ComboBox<String> cbNhanVien;
    @FXML private TextField txtMaHD;
    @FXML private DatePicker dpNgayTao;
    @FXML private ComboBox<String> cbTrangThai;

    // Food Select Fields
    @FXML private ComboBox<String> cbMonAn;
    @FXML private TextField txtSoLuong;

    // Table View Details
    @FXML private TableView<OrderDetailModel> detailTable;
    @FXML private TableColumn<OrderDetailModel, Object> colStt;
    @FXML private TableColumn<OrderDetailModel, String> colMamon;
    @FXML private TableColumn<OrderDetailModel, String> colTenmon;
    @FXML private TableColumn<OrderDetailModel, Integer> colSoluong;
    @FXML private TableColumn<OrderDetailModel, Double> colDongia;
    @FXML private TableColumn<OrderDetailModel, Double> colThanhTien;
    @FXML private TableColumn<OrderDetailModel, String> colTrangThaiMon;
    @FXML private TableColumn<OrderDetailModel, String> colAction;

    // Summary Labels
    @FXML private Label lblTotalItems;
    @FXML private Label lblTotalQuantity;
    @FXML private Label lblTotalAmount;

    @FXML private Label lblMessage;

    @FXML private Button backBtn;
    @FXML private Button cancelBtn;
    @FXML private Button saveBtn;

    private final ObservableList<OrderDetailModel> detailsList = FXCollections.observableArrayList();
    private final OrderService orderService = new OrderService();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Load ComboBox values
        loadComboValues();

        // Prefill Default fields
        dpNgayTao.setValue(LocalDate.now());
        cbTrangThai.setValue("Đang order");

        // Setup Table
        setupTableColumns();
    }

    private void loadComboValues() {
        // Status combo
        cbTrangThai.setItems(FXCollections.observableArrayList(
            "Đang order", "Đang phục vụ", "Hoàn thành", "Đã hủy"
        ));

        new Thread(() -> {
            try {
                // Load tables from server
                Request tableReq = new Request(Module.BANAN, Action.GET_ALL, null);
                Response tableRes = SocketClient.getInstance().sendRequest(tableReq);
                List<model.BanAn> serverTables = (List<model.BanAn>) (tableRes != null ? tableRes.getData() : null);

                // Load employees from server
                Request empReq = new Request(Module.NHANVIEN, Action.GET_ALL, null);
                Response empRes = SocketClient.getInstance().sendRequest(empReq);
                List<model.NhanVien> serverEmployees = (List<model.NhanVien>) (empRes != null ? empRes.getData() : null);

                // Load foods from server
                Request foodReq = new Request(Module.MONAN, Action.GET_ALL, null);
                Response foodRes = SocketClient.getInstance().sendRequest(foodReq);
                List<model.MonAn> serverFoods = (List<model.MonAn>) (foodRes != null ? foodRes.getData() : null);

                Platform.runLater(() -> {
                    // Populate cbBanAn
                    ObservableList<String> tableOptions = FXCollections.observableArrayList();
                    if (serverTables != null) {
                        for (model.BanAn ba : serverTables) {
                            tableOptions.add(ba.getMaBan() + " - " + ba.getTenBan() + " (" + ba.getKhuVuc() + ")");
                        }
                    }
                    cbBanAn.setItems(tableOptions);

                    // Populate cbNhanVien
                    ObservableList<String> employeeOptions = FXCollections.observableArrayList();
                    if (serverEmployees != null) {
                        for (model.NhanVien nv : serverEmployees) {
                            employeeOptions.add(nv.getMaNV() + " - " + nv.getHoTen());
                        }
                    }
                    cbNhanVien.setItems(employeeOptions);

                    // Populate cbMonAn
                    ObservableList<String> foodOptions = FXCollections.observableArrayList();
                    if (serverFoods != null) {
                        for (model.MonAn ma : serverFoods) {
                            if (ma.getTrangThai() != null && ma.getTrangThai().equalsIgnoreCase("Còn món")) {
                                double price = ma.getDonGia() != null ? ma.getDonGia().doubleValue() : 0.0;
                                foodOptions.add(ma.getMaMon() + " - " + ma.getTenMon() + " (" + String.format("%,.0f", price) + ")");
                            }
                        }
                    }
                    cbMonAn.setItems(foodOptions);
                });
            } catch (Exception e) {
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

        // Action Column: Delete item button
        colAction.setCellFactory(column -> new TableCell<>() {
            private final Button btn = new Button("Xóa");
            {
                btn.getStyleClass().add("delete-btn");
                btn.setStyle("-fx-pref-height: 28; -fx-font-size: 11; -fx-padding: 0 10 0 10;");
                btn.setOnAction(event -> {
                    OrderDetailModel rowData = getTableView().getItems().get(getIndex());
                    detailsList.remove(rowData);
                    updateSummary();
                });
            }
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    HBox box = new HBox(btn);
                    box.setAlignment(Pos.CENTER);
                    setGraphic(box);
                }
            }
        });

        detailTable.setItems(detailsList);
    }

    @FXML
    private void handleAddItem(ActionEvent event) {
        String selectedFoodItem = cbMonAn.getValue();
        String qtyStr = txtSoLuong.getText().trim();

        if (selectedFoodItem == null) {
            showError("Vui lòng chọn món ăn!");
            return;
        }

        int qty;
        try {
            qty = Integer.parseInt(qtyStr);
            if (qty <= 0) {
                showError("Số lượng món ăn phải lớn hơn 0!");
                return;
            }
        } catch (NumberFormatException e) {
            showError("Số lượng món ăn phải là một số nguyên hợp lệ!");
            return;
        }

        // Parse food code, name and price
        String mamon = selectedFoodItem.split(" - ")[0].trim();
        String tenmon = selectedFoodItem.split(" - ")[1].split(" \\(")[0].trim();
        
        double price = 0.0;
        try {
            String priceStr = selectedFoodItem.substring(selectedFoodItem.lastIndexOf("(") + 1, selectedFoodItem.lastIndexOf(")")).replace(",", "").replace(" VNĐ", "").trim();
            price = Double.parseDouble(priceStr);
        } catch (Exception e) {
            price = 0.0;
        }

        // Check if item already exists in detailsList (Duplicate maorder + mamon is forbidden)
        boolean exists = false;
        for (OrderDetailModel item : detailsList) {
            if (item.getMamon().equalsIgnoreCase(mamon)) {
                int currentQty = item.getSoluong();
                detailsList.remove(item);
                detailsList.add(new OrderDetailModel(txtMaOrder.getText().trim(), mamon, tenmon, currentQty + qty, price, "Đang chờ"));
                exists = true;
                break;
            }
        }

        if (!exists) {
            detailsList.add(new OrderDetailModel(txtMaOrder.getText().trim(), mamon, tenmon, qty, price, "Đang chờ"));
        }

        lblMessage.setText("");
        lblMessage.getStyleClass().removeAll("message-error", "message-success");
        updateSummary();
    }

    private void updateSummary() {
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
    }

    @FXML
    private void handleSave(ActionEvent event) {
        String maorder = txtMaOrder.getText().trim();
        String mabanFull = cbBanAn.getValue();
        String manvFull = cbNhanVien.getValue();
        String mahd = txtMaHD.getText().trim();
        LocalDate localDate = dpNgayTao.getValue();
        String status = cbTrangThai.getValue();

        if (maorder.isEmpty() || mabanFull == null || manvFull == null || localDate == null || status == null) {
            showError("Vui lòng điền đầy đủ các thông tin bắt buộc (*)");
            return;
        }

        if (!maorder.matches("^[a-zA-Z0-9_-]+$")) {
            showError("Mã Order chỉ được chứa chữ cái, số, gạch nối hoặc gạch dưới!");
            return;
        }

        if (maorder.length() > 15) {
            showError("Mã Order không được vượt quá 15 ký tự!");
            return;
        }

        if (detailsList.isEmpty()) {
            showError("Đơn hàng phải chứa ít nhất 1 món ăn!");
            return;
        }

        // Check if mahd exists in the database if entered
        if (!mahd.isEmpty()) {
            boolean hdExists = false;
            if (MockDataStore.bills != null) {
                for (var hd : MockDataStore.bills) {
                    if (hd.getId().equalsIgnoreCase(mahd)) {
                        hdExists = true;
                        break;
                    }
                }
            }
            if (!hdExists) {
                showError("Mã hóa đơn '" + mahd + "' không tồn tại trên hệ thống!");
                return;
            }
        }

        String maban = mabanFull.split(" - ")[0].trim();
        String manv = manvFull.split(" - ")[0].trim();
        Date ngaytao = Date.valueOf(localDate);

        saveBtn.setDisable(true);
        cancelBtn.setDisable(true);
        backBtn.setDisable(true);

        new Thread(() -> {
            try {
                // Check duplicate in database
                if (orderService.existsOrder(maorder)) {
                    Platform.runLater(() -> {
                        showError("Mã Order '" + maorder + "' đã tồn tại trong hệ thống!");
                        saveBtn.setDisable(false);
                        cancelBtn.setDisable(false);
                        backBtn.setDisable(false);
                    });
                    return;
                }

                // Construct and save
                OrderModel newOrder = new OrderModel(maorder, maban, manv, mahd.isEmpty() ? null : mahd, ngaytao, status);
                List<OrderDetailModel> finalDetailsList = new ArrayList<>();
                for (OrderDetailModel d : detailsList) {
                    finalDetailsList.add(new OrderDetailModel(maorder, d.getMamon(), d.getTenmon(), d.getSoluong(), d.getDongia(), d.getTrangthai()));
                }

                orderService.addOrder(newOrder, finalDetailsList);

                // Update Table Status to "Có khách" on Server via socket
                List<String> payload = List.of(maban, "Có khách");
                Request tableRequest = new Request(Module.BANAN, "UPDATE_TRANG_THAI", payload);
                SocketClient.getInstance().sendRequest(tableRequest);

                Platform.runLater(() -> {
                    showSuccess("Lưu Order thành công!");
                    new Thread(() -> {
                        try { Thread.sleep(800); } catch (Exception ignored) {}
                        Platform.runLater(this::goBack);
                    }).start();
                });
            } catch (SQLException e) {
                e.printStackTrace();
                Platform.runLater(() -> {
                    showError("Lỗi cơ sở dữ liệu: " + e.getMessage());
                    saveBtn.setDisable(false);
                    cancelBtn.setDisable(false);
                    backBtn.setDisable(false);
                });
            }
        }).start();
    }

    @FXML
    private void handleCancel(ActionEvent event) {
        clearForm();
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

    private void clearForm() {
        txtMaOrder.clear();
        cbBanAn.setValue(null);
        cbNhanVien.setValue(null);
        txtMaHD.clear();
        dpNgayTao.setValue(LocalDate.now());
        cbTrangThai.setValue("Đang order");
        cbMonAn.setValue(null);
        txtSoLuong.setText("1");
        detailsList.clear();
        lblMessage.setText("");
        lblMessage.getStyleClass().removeAll("message-error", "message-success");
        updateSummary();
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
