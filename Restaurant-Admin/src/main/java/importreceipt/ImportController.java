package importreceipt;
 
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.ResourceBundle;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
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
import javafx.util.Duration;
import model.NhaCungCap;
import model.NhapHang;
import network.Action;
import network.Module;
import network.Request;
import network.Response;
import network.SocketClient;
 
public class ImportController implements Initializable {
 
    @FXML private TableView<ImportModel> importTable;
    @FXML private TableColumn<ImportModel, String> mahangColumn;
    @FXML private TableColumn<ImportModel, String> manccColumn;
    @FXML private TableColumn<ImportModel, String> ngaynhapColumn;
    @FXML private TableColumn<ImportModel, Double> tongtienColumn;
    @FXML private TextField searchField;
    @FXML private Label totalLabel;
    @FXML private Label totalAmountLabel;
    @FXML private ComboBox<String> filterCombo;
    @FXML private DatePicker fromDate;
    @FXML private DatePicker toDate;
    @FXML private Button viewDetailBtn;
    @FXML private Button createBtn;
    @FXML private Button editBtn;
    @FXML private Button deleteBtn;
 
    // FXML fields for full-size details view switching
    @FXML private AnchorPane listPane;
    @FXML private AnchorPane detailPane;
    @FXML private Label lblDetailSubtitle;
    @FXML private Label lblDetailMaHang;
    @FXML private Label lblDetailNhaCungCap;
    @FXML private Label lblDetailNhanVien;
    @FXML private Label lblDetailNgayNhap;
    @FXML private Label lblDetailTrangThai;
    @FXML private Label lblDetailGhiChu;
    
    @FXML private TableView<ImportDetailItemModel> detailTable;
    @FXML private TableColumn<ImportDetailItemModel, Object> colStt;
    @FXML private TableColumn<ImportDetailItemModel, String> colMaNL;
    @FXML private TableColumn<ImportDetailItemModel, String> colTenNL;
    @FXML private TableColumn<ImportDetailItemModel, String> colDonVi;
    @FXML private TableColumn<ImportDetailItemModel, Double> colSoLuong;
    @FXML private TableColumn<ImportDetailItemModel, Double> colDonGia;
    @FXML private TableColumn<ImportDetailItemModel, Double> colThanhTien;
    
    @FXML private Label lblDetailTotalQty;
    @FXML private Label lblDetailTotalAmount;
 
    private ObservableList<ImportModel> masterList = FXCollections.observableArrayList();
    private FilteredList<ImportModel> filteredList;
    private SortedList<ImportModel> sortedList;
    private Timeline autoReloadTimeline;
    private boolean isLoading = false;
 
    // Local lookup mapping for supplier names
    private final java.util.Map<String, String> supplierMap = new java.util.HashMap<>();
 
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        setupTable();
        setupSearchAndFilter();
        loadSuppliers();
        loadData();
        setupAutoReload();
    }
 
    private void setupTable() {
        mahangColumn.setCellValueFactory(new PropertyValueFactory<>("mahang"));
        manccColumn.setCellValueFactory(new PropertyValueFactory<>("mancc"));
        ngaynhapColumn.setCellValueFactory(new PropertyValueFactory<>("ngaynhap"));
        tongtienColumn.setCellValueFactory(new PropertyValueFactory<>("tongtien"));
 
        tongtienColumn.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(String.format("%,.0f VNĐ", item));
                }
            }
        });
 
        importTable.setRowFactory(tv -> {
            javafx.scene.control.TableRow<ImportModel> row = new javafx.scene.control.TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && (!row.isEmpty())) {
                    ImportModel rowData = row.getItem();
                    showDetails(rowData);
                }
            });
            return row;
        });
 
        setupDetailTable();
    }
 
    private void setupDetailTable() {
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
 
        colMaNL.setCellValueFactory(new PropertyValueFactory<>("maNL"));
        colTenNL.setCellValueFactory(new PropertyValueFactory<>("tenNL"));
        colDonVi.setCellValueFactory(new PropertyValueFactory<>("donVi"));
        colSoLuong.setCellValueFactory(new PropertyValueFactory<>("soLuong"));
        colDonGia.setCellValueFactory(new PropertyValueFactory<>("donGia"));
        colThanhTien.setCellValueFactory(new PropertyValueFactory<>("thanhTien"));
 
        colDonGia.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : String.format("%,.0f VNĐ", item));
            }
        });
 
        colThanhTien.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : String.format("%,.0f VNĐ", item));
            }
        });
 
        colSoLuong.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : String.format("%.2f", item));
            }
        });
    }
 
    private void setupSearchAndFilter() {
        filteredList = new FilteredList<>(masterList, p -> true);
 
        searchField.textProperty().addListener((obs, oldVal, newVal) -> updatePredicate());
        filterCombo.valueProperty().addListener((obs, oldVal, newVal) -> updatePredicate());
        fromDate.valueProperty().addListener((obs, oldVal, newVal) -> updatePredicate());
        toDate.valueProperty().addListener((obs, oldVal, newVal) -> updatePredicate());
 
        sortedList = new SortedList<>(filteredList);
        sortedList.comparatorProperty().bind(importTable.comparatorProperty());
        importTable.setItems(sortedList);
    }
 
    private void loadSuppliers() {
        new Thread(() -> {
            try {
                Request req = new Request(Module.NHACUNGCAP, Action.GET_ALL, null);
                Response res = SocketClient.getInstance().sendRequest(req);
                if (res != null && res.isSuccess()) {
                    List<NhaCungCap> list = (List<NhaCungCap>) res.getData();
                    Platform.runLater(() -> {
                        ObservableList<String> items = FXCollections.observableArrayList();
                        items.add("Tất cả");
                        supplierMap.clear();
                        if (list != null) {
                            for (NhaCungCap ncc : list) {
                                items.add(ncc.getMaNCC() + " - " + ncc.getTenNCC());
                                supplierMap.put(ncc.getMaNCC(), ncc.getTenNCC());
                            }
                        }
                        filterCombo.setItems(items);
                        filterCombo.getSelectionModel().select("Tất cả");
                    });
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }
 
    private void loadData() {
        setLoadingState(true);
        isLoading = true;
 
        new Thread(() -> {
            try {
                Request req = new Request(Module.NHAPHANG, Action.GET_ALL, null);
                Response res = SocketClient.getInstance().sendRequest(req);
                if (res != null && res.isSuccess()) {
                    List<NhapHang> serverList = (List<NhapHang>) res.getData();
                    Platform.runLater(() -> {
                        masterList.clear();
                        if (serverList != null) {
                            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                            for (NhapHang nh : serverList) {
                                String dateStr = nh.getNgayNhap() != null ? sdf.format(nh.getNgayNhap()) : "";
                                masterList.add(new ImportModel(
                                    nh.getMaHang(),
                                    nh.getMaNCC(),
                                    dateStr,
                                    nh.getTongTien() != null ? nh.getTongTien().doubleValue() : 0.0
                                ));
                            }
                        }
                        restaurant.MockDataStore.imports.clear();
                        restaurant.MockDataStore.imports.addAll(masterList);
                        updatePredicate();
                    });
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                Platform.runLater(() -> {
                    setLoadingState(false);
                    isLoading = false;
                });
            }
        }).start();
    }
 
    private void loadDataSilently() {
        if (isLoading) return;
        isLoading = true;
 
        new Thread(() -> {
            try {
                Request req = new Request(Module.NHAPHANG, Action.GET_ALL, null);
                Response res = SocketClient.getInstance().sendRequest(req);
                if (res != null && res.isSuccess()) {
                    List<NhapHang> serverList = (List<NhapHang>) res.getData();
                    Platform.runLater(() -> {
                        ImportModel selected = importTable.getSelectionModel().getSelectedItem();
                        String selectedId = selected != null ? selected.getMahang() : null;
 
                        masterList.clear();
                        if (serverList != null) {
                            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                            for (NhapHang nh : serverList) {
                                String dateStr = nh.getNgayNhap() != null ? sdf.format(nh.getNgayNhap()) : "";
                                masterList.add(new ImportModel(
                                    nh.getMaHang(),
                                    nh.getMaNCC(),
                                    dateStr,
                                    nh.getTongTien() != null ? nh.getTongTien().doubleValue() : 0.0
                                ));
                            }
                        }
                        restaurant.MockDataStore.imports.clear();
                        restaurant.MockDataStore.imports.addAll(masterList);
                        updatePredicate();
                        if (selectedId != null) {
                            selectRowById(selectedId);
                        }
                    });
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                isLoading = false;
            }
        }).start();
    }
 
    private void selectRowById(String id) {
        if (id == null) return;
        for (ImportModel item : importTable.getItems()) {
            if (item.getMahang().equals(id)) {
                importTable.getSelectionModel().select(item);
                break;
            }
        }
    }
 
    private void updatePredicate() {
        String keyword = searchField.getText() == null ? "" : searchField.getText().trim().toLowerCase();
        String selectedNccRaw = filterCombo.getValue();
        String selectedNccId = (selectedNccRaw == null || selectedNccRaw.equals("Tất cả")) ? null 
                               : selectedNccRaw.split(" - ")[0].trim().toLowerCase();
        java.time.LocalDate start = fromDate.getValue();
        java.time.LocalDate end = toDate.getValue();
 
        filteredList.setPredicate(item -> {
            if (selectedNccId != null && !item.getMancc().toLowerCase().contains(selectedNccId)) {
                return false;
            }
            if (start != null || end != null) {
                String dateStr = item.getNgaynhap();
                try {
                    java.time.LocalDate itemDate = java.time.LocalDate.parse(dateStr.substring(0, 10));
                    if (start != null && itemDate.isBefore(start)) {
                        return false;
                    }
                    if (end != null && itemDate.isAfter(end)) {
                        return false;
                    }
                } catch (Exception e) {
                    // parse error
                }
            }
            if (!keyword.isEmpty()) {
                boolean match = item.getMahang().toLowerCase().contains(keyword)
                        || item.getMancc().toLowerCase().contains(keyword)
                        || item.getNgaynhap().toLowerCase().contains(keyword);
                if (!match) {
                    return false;
                }
            }
            return true;
        });
 
        updateCards();
    }
 
    private void updateCards() {
        int total = filteredList.size();
        double totalAmt = 0;
        for (ImportModel imp : filteredList) {
            if (imp.getTongtien() != null) {
                totalAmt += imp.getTongtien();
            }
        }
 
        totalLabel.setText(String.valueOf(total));
        totalAmountLabel.setText(String.format("%,.0f VNĐ", totalAmt));
    }
 
    private void setupAutoReload() {
        autoReloadTimeline = new Timeline(new KeyFrame(Duration.seconds(10), e -> loadDataSilently()));
        autoReloadTimeline.setCycleCount(Animation.INDEFINITE);
        autoReloadTimeline.play();
 
        importTable.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene == null && autoReloadTimeline != null) {
                autoReloadTimeline.stop();
            }
        });
        importTable.parentProperty().addListener((obs, oldParent, newParent) -> {
            if (newParent == null && autoReloadTimeline != null) {
                autoReloadTimeline.stop();
            }
        });
    }
 
    public static ImportModel selectedImport = null;
 
    @FXML
    private void handleAddNew() {
        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/fxml/importadd.fxml"));
            javafx.scene.Parent view = loader.load();
            javafx.scene.layout.AnchorPane parent = (javafx.scene.layout.AnchorPane) importTable.getScene().lookup("#contentArea");
            if (parent != null) {
                parent.getChildren().clear();
                parent.getChildren().add(view);
                javafx.scene.layout.AnchorPane.setTopAnchor(view, 0.0);
                javafx.scene.layout.AnchorPane.setBottomAnchor(view, 0.0);
                javafx.scene.layout.AnchorPane.setLeftAnchor(view, 0.0);
                javafx.scene.layout.AnchorPane.setRightAnchor(view, 0.0);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
 
    @FXML
    private void handleEdit() {
        ImportModel selected = importTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Vui lòng chọn một phiếu nhập hàng từ danh sách để sửa.");
            return;
        }
        selectedImport = selected;
        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/fxml/importedit.fxml"));
            javafx.scene.Parent view = loader.load();
            javafx.scene.layout.AnchorPane parent = (javafx.scene.layout.AnchorPane) importTable.getScene().lookup("#contentArea");
            if (parent != null) {
                parent.getChildren().clear();
                parent.getChildren().add(view);
                javafx.scene.layout.AnchorPane.setTopAnchor(view, 0.0);
                javafx.scene.layout.AnchorPane.setBottomAnchor(view, 0.0);
                javafx.scene.layout.AnchorPane.setLeftAnchor(view, 0.0);
                javafx.scene.layout.AnchorPane.setRightAnchor(view, 0.0);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
 
    @FXML
    private void handleDelete() {
        ImportModel selected = importTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Vui lòng chọn một phiếu nhập hàng từ danh sách để xóa.");
            return;
        }
        selectedImport = selected;
        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/fxml/importdelete.fxml"));
            javafx.scene.Parent view = loader.load();
            javafx.scene.layout.AnchorPane parent = (javafx.scene.layout.AnchorPane) importTable.getScene().lookup("#contentArea");
            if (parent != null) {
                parent.getChildren().clear();
                parent.getChildren().add(view);
                javafx.scene.layout.AnchorPane.setTopAnchor(view, 0.0);
                javafx.scene.layout.AnchorPane.setBottomAnchor(view, 0.0);
                javafx.scene.layout.AnchorPane.setLeftAnchor(view, 0.0);
                javafx.scene.layout.AnchorPane.setRightAnchor(view, 0.0);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
 
    @FXML
    private void handleViewDetail() {
        ImportModel selected = importTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Vui lòng chọn một phiếu nhập hàng từ danh sách để xem chi tiết.");
            return;
        }
        showDetails(selected);
    }
 
    private void showDetails(ImportModel item) {
        if (item == null) return;
        selectedImport = item;
 
        lblDetailMaHang.setText(item.getMahang());
        lblDetailNhaCungCap.setText(supplierMap.getOrDefault(item.getMancc(), item.getMancc()));
        lblDetailNgayNhap.setText(item.getNgaynhap());
         
        String employeeName = "Admin";
        if (Login.LoginController.loggedInAccount != null) {
            employeeName = Login.LoginController.loggedInAccount.getTenDN();
        }
        lblDetailNhanVien.setText(employeeName);
         
        lblDetailSubtitle.setText("Mã phiếu: " + item.getMahang() + " | Nhà cung cấp: " + supplierMap.getOrDefault(item.getMancc(), item.getMancc()));
 
        // Clear table and totals first
        detailTable.setItems(FXCollections.observableArrayList());
        lblDetailTotalQty.setText("0");
        lblDetailTotalAmount.setText("0 VNĐ");
 
        // Load details in background
        new Thread(() -> {
            try {
                // Fetch ingredient mapping for names and units
                java.util.Map<String, model.NguyenLieu> nguyenLieuCache = new java.util.HashMap<>();
                Request nlReq = new Request(Module.NGUYENLIEU, Action.GET_ALL, null);
                Response nlRes = SocketClient.getInstance().sendRequest(nlReq);
                if (nlRes != null && nlRes.isSuccess()) {
                    List<model.NguyenLieu> nls = (List<model.NguyenLieu>) nlRes.getData();
                    if (nls != null) {
                        for (model.NguyenLieu nl : nls) {
                            nguyenLieuCache.put(nl.getMaNL(), nl);
                        }
                    }
                }
 
                // Fetch details
                Request ctReq = new Request(Module.CHITIETNH, Action.GET_ALL, null);
                Response ctRes = SocketClient.getInstance().sendRequest(ctReq);
                if (ctRes != null && ctRes.isSuccess()) {
                    List<model.ChiTietNH> serverList = (List<model.ChiTietNH>) ctRes.getData();
                    ObservableList<ImportDetailItemModel> list = FXCollections.observableArrayList();
                    double totalQty = 0;
                    double totalAmt = 0;
 
                    if (serverList != null) {
                        for (model.ChiTietNH ct : serverList) {
                            if (ct.getMaHang().equalsIgnoreCase(item.getMahang())) {
                                model.NguyenLieu nl = nguyenLieuCache.get(ct.getMaNL());
                                String tenNL = nl != null ? nl.getTenNL() : ct.getMaNL();
                                String donVi = nl != null ? nl.getDonViTinh() : "--";
                                 
                                double qty = ct.getSoLuong();
                                double price = ct.getDonGiaNhap() != null ? ct.getDonGiaNhap().doubleValue() : 0.0;
                                double sum = ct.getThanhTien() != null ? ct.getThanhTien().doubleValue() : qty * price;
 
                                list.add(new ImportDetailItemModel(
                                    ct.getMaNL(),
                                    tenNL,
                                    donVi,
                                    qty,
                                    price,
                                    sum
                                ));
                                totalQty += qty;
                                totalAmt += sum;
                            }
                        }
                    }
 
                    final double finalQty = totalQty;
                    final double finalAmt = totalAmt;
 
                    Platform.runLater(() -> {
                        detailTable.setItems(list);
                        lblDetailTotalQty.setText(String.format("%.2f", finalQty));
                        lblDetailTotalAmount.setText(String.format("%,.0f VNĐ", finalAmt));
                    });
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
 
        // Swap view panels
        listPane.setVisible(false);
        detailPane.setVisible(true);
    }
 
    @FXML
    private void handleCloseDetail() {
        detailPane.setVisible(false);
        listPane.setVisible(true);
        selectedImport = null;
    }
  
    private void setLoadingState(boolean loading) {
        if (viewDetailBtn != null) viewDetailBtn.setDisable(loading);
        if (createBtn != null) createBtn.setDisable(loading);
        if (editBtn != null) editBtn.setDisable(loading);
        if (deleteBtn != null) deleteBtn.setDisable(loading);
        if (searchField != null) searchField.setDisable(loading);
        if (filterCombo != null) filterCombo.setDisable(loading);
        if (fromDate != null) fromDate.setDisable(loading);
        if (toDate != null) toDate.setDisable(loading);
    }
 
    private void showAlert(String message) {
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.WARNING);
        alert.setTitle("Thông báo");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
  
    /* ================= MODEL ================= */
    public static class ImportModel {
        private final String mahang;
        private final String mancc;
        private final String ngaynhap;
        private final Double tongtien;
 
        public ImportModel(String mahang, String mancc, String ngaynhap, Double tongtien) {
            this.mahang = mahang;
            this.mancc = mancc;
            this.ngaynhap = ngaynhap;
            this.tongtien = tongtien;
        }
 
        public String getMahang() { return mahang; }
        public String getMancc() { return mancc; }
        public String getNgaynhap() { return ngaynhap; }
        public Double getTongtien() { return tongtien; }
    }
 
    public static class ImportDetailItemModel {
        private final String maNL;
        private final String tenNL;
        private final String donVi;
        private final Double soLuong;
        private final Double donGia;
        private final Double thanhTien;
 
        public ImportDetailItemModel(String maNL, String tenNL, String donVi, Double soLuong, Double donGia, Double thanhTien) {
            this.maNL = maNL;
            this.tenNL = tenNL;
            this.donVi = donVi;
            this.soLuong = soLuong;
            this.donGia = donGia;
            this.thanhTien = thanhTien;
        }
 
        public String getMaNL() { return maNL; }
        public String getTenNL() { return tenNL; }
        public String getDonVi() { return donVi; }
        public Double getSoLuong() { return soLuong; }
        public Double getDonGia() { return donGia; }
        public Double getThanhTien() { return thanhTien; }
    }
}
