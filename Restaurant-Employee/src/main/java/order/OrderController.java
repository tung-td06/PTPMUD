package order;

import java.net.URL;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.*;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import model.BanAn;
import model.ChiTietHD;
import model.HoaDon;
import model.KhachHang;
import model.MonAn;
import network.Action;
import network.Module;
import network.Request;
import network.Response;
import network.SocketClient;

public class OrderController implements Initializable {

    @FXML
    private FlowPane cardsContainer;

    @FXML
    private ComboBox<String> statusCombo;

    @FXML
    private ComboBox<String> tableCombo;

    @FXML
    private DatePicker datePicker;

    @FXML
    private ComboBox<String> sortCombo;

    @FXML
    private TextField searchField;

    @FXML
    private Button refreshBtn;

    @FXML
    private Label preparingCountLabel;

    @FXML
    private Label cookingCountLabel;

    @FXML
    private Label completedCountLabel;

    @FXML
    private Label servedCountLabel;

    @FXML
    private Label processingTotalLabel;

    private final ObservableList<BillCardData> allBills = FXCollections.observableArrayList();
    private FilteredList<BillCardData> filteredBills;
    private SortedList<BillCardData> sortedBills;
    private javafx.animation.Timeline autoReloadTimeline;
    private boolean isReloading = false;

    private final Map<String, String> customerMap = new HashMap<>();
    private final Map<String, String> tableMap = new HashMap<>();
    private final Map<String, String> foodNameMap = new HashMap<>();
    private final Map<String, Double> foodPriceMap = new HashMap<>();
    private final Map<String, HoaDon> billMap = new HashMap<>();

    private static final SimpleDateFormat timeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        filteredBills = new FilteredList<>(allBills, p -> true);
        sortedBills = new SortedList<>(filteredBills);

        setupFilters();

        // Listen to changes to rebuild cards automatically (backup)
        sortedBills.addListener((javafx.collections.ListChangeListener.Change<? extends BillCardData> c) -> {
            renderCards();
        });

        loadData();
        startAutoReload();
    }

    private void setupFilters() {
        statusCombo.setItems(FXCollections.observableArrayList(
                "Tất cả", "Đang chờ", "Đang chế biến", "Hoàn thành", "Đã phục vụ"));
        statusCombo.getSelectionModel().select("Tất cả");

        tableCombo.setItems(FXCollections.observableArrayList("Tất cả"));
        tableCombo.getSelectionModel().select("Tất cả");

        sortCombo.setItems(FXCollections.observableArrayList(
                "Thời gian (Mới nhất)", "Thời gian (Cũ nhất)", "Bàn ăn (A-Z)", "Trạng thái"
        ));
        sortCombo.getSelectionModel().select("Thời gian (Mới nhất)");
        applySort(); // Apply default comparator

        statusCombo.valueProperty().addListener((obs, oldVal, newVal) -> applyFilters());
        tableCombo.valueProperty().addListener((obs, oldVal, newVal) -> applyFilters());
        datePicker.valueProperty().addListener((obs, oldVal, newVal) -> applyFilters());
        searchField.textProperty().addListener((obs, oldVal, newVal) -> applyFilters());
        sortCombo.valueProperty().addListener((obs, oldVal, newVal) -> applySort());
    }

    private void loadData() {
        setLoadingState(true);
        new Thread(() -> {
            try {
                // 1. Fetch Customers
                Request khReq = new Request(Module.KHACHHANG, Action.GET_ALL, null);
                Response khRes = SocketClient.getInstance().sendRequest(khReq);
                if (khRes == null || !khRes.isSuccess()) {
                    loadMockDataFallback();
                    return;
                }
                List<KhachHang> khList = (List<KhachHang>) khRes.getData();
                customerMap.clear();
                if (khList != null) {
                    for (KhachHang kh : khList) {
                        customerMap.put(kh.getMaKH(), kh.getTenKH());
                    }
                }

                // 2. Fetch Tables
                Request banReq = new Request(Module.BANAN, Action.GET_ALL, null);
                Response banRes = SocketClient.getInstance().sendRequest(banReq);
                if (banRes == null || !banRes.isSuccess()) {
                    loadMockDataFallback();
                    return;
                }
                List<String> tableNames = new ArrayList<>();
                List<BanAn> banList = (List<BanAn>) banRes.getData();
                tableMap.clear();
                if (banList != null) {
                    for (BanAn ban : banList) {
                        tableMap.put(ban.getMaBan(), ban.getTenBan());
                        tableNames.add(ban.getTenBan());
                    }
                }
                Collections.sort(tableNames);

                // 3. Fetch Foods
                Request foodReq = new Request(Module.MONAN, Action.GET_ALL, null);
                Response foodRes = SocketClient.getInstance().sendRequest(foodReq);
                if (foodRes == null || !foodRes.isSuccess()) {
                    loadMockDataFallback();
                    return;
                }
                List<MonAn> foods = (List<MonAn>) foodRes.getData();
                foodNameMap.clear();
                foodPriceMap.clear();
                if (foods != null) {
                    for (MonAn ma : foods) {
                        foodNameMap.put(ma.getMaMon(), ma.getTenMon());
                        double price = ma.getDonGia() != null ? ma.getDonGia().doubleValue() : 0.0;
                        foodPriceMap.put(ma.getMaMon(), price);
                    }
                }

                // 4. Fetch Invoices
                Request billReq = new Request(Module.HOADON, Action.GET_ALL, null);
                Response billRes = SocketClient.getInstance().sendRequest(billReq);
                if (billRes == null || !billRes.isSuccess()) {
                    loadMockDataFallback();
                    return;
                }
                List<HoaDon> serverBills = (List<HoaDon>) billRes.getData();
                billMap.clear();
                if (serverBills != null) {
                    for (HoaDon hd : serverBills) {
                        billMap.put(hd.getMaHD(), hd);
                    }
                }

                // 5. Fetch Invoice Details
                Request detailReq = new Request(Module.CHITIETHD, Action.GET_ALL, null);
                Response detailRes = SocketClient.getInstance().sendRequest(detailReq);
                if (detailRes == null || !detailRes.isSuccess()) {
                    loadMockDataFallback();
                    return;
                }

                Platform.runLater(() -> {
                    List<ChiTietHD> details = (List<ChiTietHD>) detailRes.getData();
                    updateTableAndCards(details);

                    // Populate table filter combobox
                    String selectedTable = tableCombo.getValue();
                    tableCombo.getItems().clear();
                    tableCombo.getItems().add("Tất cả");
                    tableCombo.getItems().addAll(tableNames);
                    if (tableNames.contains(selectedTable)) {
                        tableCombo.getSelectionModel().select(selectedTable);
                    } else {
                        tableCombo.getSelectionModel().select("Tất cả");
                    }
                    setLoadingState(false);
                });

            } catch (Exception e) {
                e.printStackTrace();
                loadMockDataFallback();
            }
        }).start();
    }

    private void loadMockDataFallback() {
        System.out.println("No server connection found. Loading mock data...");
        
        // 1. Customers
        customerMap.clear();
        customerMap.put("KH001", "Nguyễn Văn A");
        customerMap.put("KH002", "Trần Thị Bích");
        customerMap.put("KH003", "Lê Minh C");
        customerMap.put("KH004", "Phạm Văn D");
        customerMap.put("KH005", "Hoàng Anh Tuấn");
        customerMap.put("KH006", "Đặng Thị Thắm");
        customerMap.put("KH007", "Phan Thanh Hùng");
        
        // 2. Tables
        tableMap.clear();
        List<String> tableNames = new ArrayList<>();
        tableMap.put("T01", "Bàn 01");
        tableMap.put("T02", "Bàn 02");
        tableMap.put("T03", "Bàn 03");
        tableMap.put("T04", "Bàn 04");
        tableMap.put("T05", "Bàn 05");
        tableMap.put("T06", "Bàn 06");
        tableMap.put("T07", "Bàn VIP 01");
        tableMap.put("T08", "Bàn VIP 02");
        tableNames.addAll(tableMap.values());
        Collections.sort(tableNames);

        // 3. Foods
        foodNameMap.clear();
        foodPriceMap.clear();
        foodNameMap.put("F001", "Phở bò Kobe");
        foodPriceMap.put("F001", 250000.0);
        foodNameMap.put("F002", "Gỏi cuốn tôm thịt");
        foodPriceMap.put("F002", 65000.0);
        foodNameMap.put("F003", "Cá hồi áp chảo sốt chanh leo");
        foodPriceMap.put("F003", 320000.0);
        foodNameMap.put("F004", "Nước ép cam tươi");
        foodPriceMap.put("F004", 45000.0);
        foodNameMap.put("F005", "Bánh tiramisu");
        foodPriceMap.put("F005", 55000.0);
        foodNameMap.put("F006", "Súp bào ngư vi cá");
        foodPriceMap.put("F006", 450000.0);
        foodNameMap.put("F007", "Cơm chiên hải sản");
        foodPriceMap.put("F007", 150000.0);
        foodNameMap.put("F009", "Kem bơ Đà Lạt");
        foodPriceMap.put("F009", 40000.0);
        foodNameMap.put("F010", "Sườn cừu nướng BBQ");
        foodPriceMap.put("F010", 380000.0);

        // 4. Invoices
        billMap.clear();
        long now = System.currentTimeMillis();
        
        // HD001 - Bàn 01 (Vào cách đây 3 phút -> Đang chuẩn bị / Đang chờ)
        HoaDon hd1 = new HoaDon();
        hd1.setMaHD("HD001");
        hd1.setMaBan("T01");
        hd1.setMaKH("KH001");
        hd1.setTrangThai("Chưa thanh toán");
        hd1.setTimeVao(new Timestamp(now - (3 * 60 * 1000)));
        billMap.put(hd1.getMaHD(), hd1);
        
        // HD002 - Bàn 05 (Vào cách đây 10 phút -> Đang chế biến)
        HoaDon hd2 = new HoaDon();
        hd2.setMaHD("HD002");
        hd2.setMaBan("T05");
        hd2.setMaKH("KH002");
        hd2.setTrangThai("Chưa thanh toán");
        hd2.setTimeVao(new Timestamp(now - (10 * 60 * 1000)));
        billMap.put(hd2.getMaHD(), hd2);

        // HD003 - Bàn VIP 01 (Vào cách đây 25 phút -> Hoàn thành)
        HoaDon hd3 = new HoaDon();
        hd3.setMaHD("HD003");
        hd3.setMaBan("T07");
        hd3.setMaKH("KH003");
        hd3.setTrangThai("Chưa thanh toán");
        hd3.setTimeVao(new Timestamp(now - (25 * 60 * 1000)));
        billMap.put(hd3.getMaHD(), hd3);

        // HD004 - Bàn VIP 02 (Đã thanh toán -> Đã phục vụ)
        HoaDon hd4 = new HoaDon();
        hd4.setMaHD("HD004");
        hd4.setMaBan("T08");
        hd4.setMaKH("KH004");
        hd4.setTrangThai("Đã thanh toán");
        hd4.setTimeVao(new Timestamp(now - (40 * 60 * 1000)));
        billMap.put(hd4.getMaHD(), hd4);

        // 5. Details
        List<ChiTietHD> details = new ArrayList<>();
        
        // HD001
        ChiTietHD ct1 = new ChiTietHD();
        ct1.setMaHD("HD001");
        ct1.setMaMon("F001");
        ct1.setSoLuong(2);
        details.add(ct1);
        
        ChiTietHD ct2 = new ChiTietHD();
        ct2.setMaHD("HD001");
        ct2.setMaMon("F004");
        ct2.setSoLuong(2);
        details.add(ct2);

        // HD002
        ChiTietHD ct3 = new ChiTietHD();
        ct3.setMaHD("HD002");
        ct3.setMaMon("F003");
        ct3.setSoLuong(1);
        details.add(ct3);
        
        ChiTietHD ct4 = new ChiTietHD();
        ct4.setMaHD("HD002");
        ct4.setMaMon("F007");
        ct4.setSoLuong(2);
        details.add(ct4);

        // HD003
        ChiTietHD ct5 = new ChiTietHD();
        ct5.setMaHD("HD003");
        ct5.setMaMon("F006");
        ct5.setSoLuong(2);
        details.add(ct5);
        
        ChiTietHD ct6 = new ChiTietHD();
        ct6.setMaHD("HD003");
        ct6.setMaMon("F010");
        ct6.setSoLuong(1);
        details.add(ct6);

        // HD004
        ChiTietHD ct7 = new ChiTietHD();
        ct7.setMaHD("HD004");
        ct7.setMaMon("F002");
        ct7.setSoLuong(3);
        details.add(ct7);

        Platform.runLater(() -> {
            updateTableAndCards(details);
            
            tableCombo.getItems().clear();
            tableCombo.getItems().add("Tất cả");
            tableCombo.getItems().addAll(tableNames);
            tableCombo.getSelectionModel().select("Tất cả");
            
            setLoadingState(false);
        });
    }

    private void loadDataSilently() {
        if (isReloading) return;
        isReloading = true;
        new Thread(() -> {
            try {
                // Fetch latest Invoices
                Request billReq = new Request(Module.HOADON, Action.GET_ALL, null);
                Response billRes = SocketClient.getInstance().sendRequest(billReq);
                if (billRes == null || !billRes.isSuccess()) {
                    isReloading = false;
                    return;
                }
                List<HoaDon> serverBills = (List<HoaDon>) billRes.getData();
                billMap.clear();
                if (serverBills != null) {
                    for (HoaDon hd : serverBills) {
                        billMap.put(hd.getMaHD(), hd);
                    }
                }

                // Fetch latest details
                Request detailReq = new Request(Module.CHITIETHD, Action.GET_ALL, null);
                Response detailRes = SocketClient.getInstance().sendRequest(detailReq);
                if (detailRes == null || !detailRes.isSuccess()) {
                    isReloading = false;
                    return;
                }

                Platform.runLater(() -> {
                    List<ChiTietHD> details = (List<ChiTietHD>) detailRes.getData();
                    updateTableAndCards(details);
                    isReloading = false;
                });
            } catch (Exception e) {
                isReloading = false;
            }
        }).start();
    }

    private void updateTableAndCards(List<ChiTietHD> details) {
        if (details == null) return;

        long now = System.currentTimeMillis();
        
        // Group ChiTietHD by Bill ID (maHD)
        Map<String, List<OrderItemModel>> groupedItems = new HashMap<>();
        
        for (ChiTietHD ct : details) {
            HoaDon hd = billMap.get(ct.getMaHD());
            if (hd == null) continue;

            String maHD = ct.getMaHD();
            String tableName = tableMap.getOrDefault(hd.getMaBan(), hd.getMaBan());
            String tenMon = foodNameMap.getOrDefault(ct.getMaMon(), ct.getMaMon());
            int soLuong = ct.getSoLuong();
            String timeOrder = hd.getTimeVao() != null ? timeFormat.format(hd.getTimeVao()) : "";
            
            // Calculate item status
            String trangThai;
            if (hd.getTrangThai() != null && hd.getTrangThai().equalsIgnoreCase("Đã thanh toán")) {
                trangThai = "Đã phục vụ";
            } else if (hd.getTimeVao() != null) {
                long diffMinutes = (now - hd.getTimeVao().getTime()) / (1000 * 60);
                if (diffMinutes < 5) {
                    trangThai = "Đang chuẩn bị";
                } else if (diffMinutes < 15) {
                    trangThai = "Đang chế biến";
                } else {
                    trangThai = "Hoàn thành";
                }
            } else {
                trangThai = "Đang chuẩn bị";
            }

            String custName = customerMap.getOrDefault(hd.getMaKH(), "Khách vãng lai");
            LocalDate localDate = hd.getTimeVao() != null ? hd.getTimeVao().toLocalDateTime().toLocalDate() : null;

            OrderItemModel itemModel = new OrderItemModel(
                    maHD, tableName, tenMon, soLuong, timeOrder, trangThai,
                    hd.getTimeVao(), ct.getMaMon(), hd.getMaBan(), hd.getMaKH(),
                    custName, hd.getTrangThai(), localDate
            );
            
            groupedItems.computeIfAbsent(maHD, k -> new ArrayList<>()).add(itemModel);
        }

        // Build BillCardData list
        List<BillCardData> billList = new ArrayList<>();
        for (Map.Entry<String, List<OrderItemModel>> entry : groupedItems.entrySet()) {
            String maHD = entry.getKey();
            List<OrderItemModel> items = entry.getValue();
            if (items.isEmpty()) continue;
            
            OrderItemModel firstItem = items.get(0); // contains common fields
            
            HoaDon hd = billMap.get(maHD);
            String billStatus = hd != null ? hd.getTrangThai() : "Không xác định";
            Timestamp timeVao = hd != null ? hd.getTimeVao() : null;
            LocalDate localDate = timeVao != null ? timeVao.toLocalDateTime().toLocalDate() : null;

            billList.add(new BillCardData(
                    maHD,
                    firstItem.getBan(),
                    firstItem.getCustomerName(),
                    firstItem.getTimeOrder(),
                    timeVao,
                    billStatus,
                    localDate,
                    items
            ));
        }

        allBills.setAll(billList);
        
        applyFilters();
        updateCards();
        renderCards(); // Force direct render
    }

    private void applyFilters() {
        String statusFilter = statusCombo.getValue();
        String tableFilter = tableCombo.getValue();
        LocalDate selectedDate = datePicker.getValue();
        String keyword = searchField.getText() == null ? "" : searchField.getText().trim().toLowerCase();

        filteredBills.setPredicate(bill -> {
            // Keyword filter
            if (!keyword.isEmpty()) {
                boolean matchesHeader = bill.getMaHD().toLowerCase().contains(keyword) ||
                        bill.getBan().toLowerCase().contains(keyword) ||
                        bill.getCustomerName().toLowerCase().contains(keyword);
                
                boolean matchesItems = false;
                for (OrderItemModel item : bill.getItems()) {
                    if (item.getTenMon().toLowerCase().contains(keyword)) {
                        matchesItems = true;
                        break;
                    }
                }
                if (!matchesHeader && !matchesItems) return false;
            }

            // Status filter
            if (statusFilter != null && !statusFilter.equals("Tất cả")) {
                boolean hasMatchingStatus = false;
                for (OrderItemModel item : bill.getItems()) {
                    String status = item.getTrangThai();
                    boolean match;
                    if (statusFilter.equalsIgnoreCase("Đang chờ")) {
                        match = status.equalsIgnoreCase("Đang chờ") || status.equalsIgnoreCase("Đang chuẩn bị");
                    } else {
                        match = status.equalsIgnoreCase(statusFilter);
                    }
                    if (match) {
                        hasMatchingStatus = true;
                        break;
                    }
                }
                if (!hasMatchingStatus) return false;
            }

            // Table filter
            if (tableFilter != null && !tableFilter.equals("Tất cả")) {
                if (!bill.getBan().equalsIgnoreCase(tableFilter)) {
                    return false;
                }
            }

            // Date filter
            if (selectedDate != null) {
                if (bill.getLocalDate() == null || !bill.getLocalDate().equals(selectedDate)) {
                    return false;
                }
            }

            return true;
        });

        renderCards(); // Force direct render on predicate change
    }

    private void applySort() {
        String sortType = sortCombo.getValue();
        if (sortType == null) return;
        
        Comparator<BillCardData> comparator = null;
        switch (sortType) {
            case "Thời gian (Mới nhất)":
                comparator = (b1, b2) -> {
                    if (b1.getTimeOrderRaw() == null && b2.getTimeOrderRaw() == null) return 0;
                    if (b1.getTimeOrderRaw() == null) return 1;
                    if (b2.getTimeOrderRaw() == null) return -1;
                    return b2.getTimeOrderRaw().compareTo(b1.getTimeOrderRaw());
                };
                break;
            case "Thời gian (Cũ nhất)":
                comparator = (b1, b2) -> {
                    if (b1.getTimeOrderRaw() == null && b2.getTimeOrderRaw() == null) return 0;
                    if (b1.getTimeOrderRaw() == null) return 1;
                    if (b2.getTimeOrderRaw() == null) return -1;
                    return b1.getTimeOrderRaw().compareTo(b2.getTimeOrderRaw());
                };
                break;
            case "Bàn ăn (A-Z)":
                comparator = (b1, b2) -> {
                    String ban1 = b1.getBan() != null ? b1.getBan() : "";
                    String ban2 = b2.getBan() != null ? b2.getBan() : "";
                    return ban1.compareToIgnoreCase(ban2);
                };
                break;
            case "Trạng thái":
                comparator = (b1, b2) -> {
                    int p1 = getBillPriority(b1);
                    int p2 = getBillPriority(b2);
                    return Integer.compare(p1, p2);
                };
                break;
        }
        
        sortedBills.setComparator(comparator);
        renderCards(); // Force direct render on comparator change
    }
    
    private int getBillPriority(BillCardData bill) {
        int highestPriority = 4;
        for (OrderItemModel item : bill.getItems()) {
            String status = item.getTrangThai();
            int pri = 4;
            if (status.equalsIgnoreCase("Đang chuẩn bị") || status.equalsIgnoreCase("Đang chờ")) {
                pri = 1;
            } else if (status.equalsIgnoreCase("Đang chế biến")) {
                pri = 2;
            } else if (status.equalsIgnoreCase("Hoàn thành")) {
                pri = 3;
            } else if (status.equalsIgnoreCase("Đã phục vụ")) {
                pri = 4;
            }
            if (pri < highestPriority) {
                highestPriority = pri;
            }
        }
        return highestPriority;
    }

    private void updateCards() {
        int preparing = 0;
        int cooking = 0;
        int completed = 0;
        int served = 0;

        for (BillCardData bill : allBills) {
            for (OrderItemModel item : bill.getItems()) {
                String status = item.getTrangThai();
                if (status.equalsIgnoreCase("Đang chuẩn bị") || status.equalsIgnoreCase("Đang chờ")) {
                    preparing++;
                } else if (status.equalsIgnoreCase("Đang chế biến")) {
                    cooking++;
                } else if (status.equalsIgnoreCase("Hoàn thành")) {
                    completed++;
                } else if (status.equalsIgnoreCase("Đã phục vụ")) {
                    served++;
                }
            }
        }

        preparingCountLabel.setText(String.valueOf(preparing));
        cookingCountLabel.setText(String.valueOf(cooking));
        completedCountLabel.setText(String.valueOf(completed));
        servedCountLabel.setText(String.valueOf(served));
        processingTotalLabel.setText(String.valueOf(preparing + cooking + completed + served));
    }

    private void renderCards() {
        cardsContainer.getChildren().clear();
        if (sortedBills.isEmpty()) {
            Label lblEmpty = new Label("Không tìm thấy món ăn/hóa đơn nào khớp bộ lọc");
            lblEmpty.setStyle("-fx-font-size: 14px; -fx-text-fill: #64748b; -fx-font-weight: bold;");
            cardsContainer.getChildren().add(lblEmpty);
            return;
        }

        for (BillCardData bill : sortedBills) {
            Node cardNode = createCardNode(bill);
            cardsContainer.getChildren().add(cardNode);
        }
    }

    private Node createCardNode(BillCardData bill) {
        VBox card = new VBox();
        card.getStyleClass().add("order-card");
        card.setSpacing(12);
        card.setPadding(new Insets(16));
        card.setPrefWidth(300);
        card.setMinWidth(280);
        card.setMaxWidth(320);

        // Header: Table name, Bill ID
        HBox topRow = new HBox();
        topRow.setAlignment(Pos.CENTER_LEFT);
        
        Label lblTableName = new Label(bill.getBan().toUpperCase());
        lblTableName.getStyleClass().add("card-table-name");
        
        Region spacer1 = new Region();
        HBox.setHgrow(spacer1, Priority.ALWAYS);
        
        Label lblBillId = new Label(bill.getMaHD());
        lblBillId.getStyleClass().add("card-bill-id");
        
        topRow.getChildren().addAll(lblTableName, spacer1, lblBillId);
        
        // Customer Name
        Label lblCustomer = new Label("Khách: " + bill.getCustomerName());
        lblCustomer.getStyleClass().add("card-detail-line");
        lblCustomer.setStyle("-fx-font-weight: bold;");
        
        // Time
        Label lblTime = new Label("Giờ vào: " + bill.getTimeOrder());
        lblTime.getStyleClass().add("card-detail-line");

        card.getChildren().addAll(topRow, lblCustomer, lblTime);

        // Items divider container
        VBox itemsContainer = new VBox();
        itemsContainer.getStyleClass().add("card-items-divider");
        itemsContainer.setSpacing(6);

        // List of dishes
        for (OrderItemModel item : bill.getItems()) {
            HBox itemRow = new HBox();
            itemRow.setAlignment(Pos.CENTER_LEFT);
            itemRow.getStyleClass().add("card-item-row");

            Label lblItemText = new Label(item.getSoLuong() + "x " + item.getTenMon());
            lblItemText.getStyleClass().add("card-item-text");
            lblItemText.setWrapText(true);
            lblItemText.setMaxWidth(160);
            
            Region spacerItem = new Region();
            HBox.setHgrow(spacerItem, Priority.ALWAYS);

            // Badge status
            String status = item.getTrangThai();
            String displayStatus = status;
            String badgeClass = "badge-preparing";
            if (status.equalsIgnoreCase("Đang chuẩn bị") || status.equalsIgnoreCase("Đang chờ")) {
                displayStatus = "Đang chờ";
                badgeClass = "badge-preparing";
            } else if (status.equalsIgnoreCase("Đang chế biến")) {
                displayStatus = "Đang nấu";
                badgeClass = "badge-cooking";
            } else if (status.equalsIgnoreCase("Hoàn thành")) {
                displayStatus = "Xong";
                badgeClass = "badge-completed";
            } else if (status.equalsIgnoreCase("Đã phục vụ")) {
                displayStatus = "Phục vụ";
                badgeClass = "badge-served";
            }

            Label lblBadge = new Label(displayStatus);
            lblBadge.getStyleClass().addAll("badge-sm", badgeClass);
            
            itemRow.getChildren().addAll(lblItemText, spacerItem, lblBadge);
            itemsContainer.getChildren().add(itemRow);
        }
        
        card.getChildren().add(itemsContainer);

        // Footer Total
        HBox totalRow = new HBox();
        totalRow.getStyleClass().add("card-total-row");
        totalRow.setAlignment(Pos.CENTER_LEFT);
        
        Label lblTotal = new Label("Tổng: " + bill.getTotalQuantity() + " món");
        lblTotal.getStyleClass().add("card-total-text");
        
        totalRow.getChildren().add(lblTotal);
        card.getChildren().add(totalRow);

        // Double Click to Open Details Dialog
        card.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                showInvoiceDetailDialog(bill);
            }
        });

        return card;
    }

    private void showInvoiceDetailDialog(BillCardData selectedBill) {
        Stage dialog = new Stage();
        dialog.initOwner(cardsContainer.getScene().getWindow());
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setTitle("Chi tiết hóa đơn - " + selectedBill.getMaHD());

        VBox root = new VBox(15);
        root.setPadding(new Insets(20));
        root.setStyle("-fx-background-color: #f4f6fb;");

        // Title Label
        Label titleLabel = new Label("Chi Tiết Hóa Đơn " + selectedBill.getMaHD());
        titleLabel.getStyleClass().add("dialog-header-title");

        // Info Grid
        GridPane infoGrid = new GridPane();
        infoGrid.setHgap(30);
        infoGrid.setVgap(10);
        infoGrid.setStyle("-fx-background-color: white; -fx-padding: 15; -fx-background-radius: 10; -fx-border-color: #e2e8f0; -fx-border-radius: 10;");

        Label lblCustTitle = new Label("Khách hàng:");
        lblCustTitle.getStyleClass().add("dialog-info-label");
        Label lblCustVal = new Label(selectedBill.getCustomerName());
        lblCustVal.getStyleClass().add("dialog-info-value");

        Label lblTableTitle = new Label("Bàn phục vụ:");
        lblTableTitle.getStyleClass().add("dialog-info-label");
        Label lblTableVal = new Label(selectedBill.getBan());
        lblTableVal.getStyleClass().add("dialog-info-value");

        Label lblTimeTitle = new Label("Thời gian order:");
        lblTimeTitle.getStyleClass().add("dialog-info-label");
        Label lblTimeVal = new Label(selectedBill.getTimeOrder());
        lblTimeVal.setStyle("-fx-text-fill: #1a202c;");

        Label lblStatusTitle = new Label("Trạng thái HD:");
        lblStatusTitle.getStyleClass().add("dialog-info-label");
        Label lblStatusVal = new Label(selectedBill.getBillStatus());
        lblStatusVal.setStyle("-fx-text-fill: #3b82f6; -fx-font-weight: bold;");

        Label lblNoteTitle = new Label("Ghi chú:");
        lblNoteTitle.getStyleClass().add("dialog-info-label");
        Label lblNoteVal = new Label("Không có");
        lblNoteVal.setStyle("-fx-text-fill: #64748b; -fx-font-style: italic;");

        infoGrid.add(lblCustTitle, 0, 0);
        infoGrid.add(lblCustVal, 1, 0);
        infoGrid.add(lblTableTitle, 0, 1);
        infoGrid.add(lblTableVal, 1, 1);
        infoGrid.add(lblTimeTitle, 2, 0);
        infoGrid.add(lblTimeVal, 3, 0);
        infoGrid.add(lblStatusTitle, 2, 1);
        infoGrid.add(lblStatusVal, 3, 1);
        infoGrid.add(lblNoteTitle, 0, 2);
        infoGrid.add(lblNoteVal, 1, 2);

        // Dishes Table Title
        Label tableHeader = new Label("Danh Sách Món Ăn");
        tableHeader.getStyleClass().add("dialog-section-title");

        // Table
        TableView<DialogDishModelNew> dishTable = new TableView<>();
        dishTable.setStyle("-fx-background-color: white; -fx-background-radius: 10; -fx-border-color: #e2e8f0; -fx-border-radius: 10;");
        dishTable.setPrefHeight(200);

        TableColumn<DialogDishModelNew, String> colName = new TableColumn<>("Tên món");
        colName.setCellValueFactory(new PropertyValueFactory<>("tenMon"));
        colName.setPrefWidth(180);

        TableColumn<DialogDishModelNew, Integer> colQty = new TableColumn<>("Số lượng");
        colQty.setCellValueFactory(new PropertyValueFactory<>("soLuong"));
        colQty.setPrefWidth(80);
        colQty.setStyle("-fx-alignment: CENTER;");

        TableColumn<DialogDishModelNew, String> colPrice = new TableColumn<>("Đơn giá");
        colPrice.setCellValueFactory(new PropertyValueFactory<>("donGia"));
        colPrice.setPrefWidth(100);
        colPrice.setStyle("-fx-alignment: CENTER_RIGHT;");

        TableColumn<DialogDishModelNew, String> colTotal = new TableColumn<>("Thành tiền");
        colTotal.setCellValueFactory(new PropertyValueFactory<>("thanhTien"));
        colTotal.setPrefWidth(110);
        colTotal.setStyle("-fx-alignment: CENTER_RIGHT;");

        TableColumn<DialogDishModelNew, String> colStatus = new TableColumn<>("Trạng thái món");
        colStatus.setCellValueFactory(new PropertyValueFactory<>("trangThai"));
        colStatus.setPrefWidth(120);
        colStatus.setStyle("-fx-alignment: CENTER;");
        
        colStatus.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                    setText(null);
                } else {
                    Label label = new Label(item);
                    label.getStyleClass().add("badge-sm");

                    if (item.equalsIgnoreCase("Đang chờ") || item.equalsIgnoreCase("Đang chuẩn bị")) {
                        label.getStyleClass().add("badge-preparing");
                    } else if (item.equalsIgnoreCase("Đang chế biến") || item.equalsIgnoreCase("Đang nấu")) {
                        label.getStyleClass().add("badge-cooking");
                    } else if (item.equalsIgnoreCase("Hoàn thành") || item.equalsIgnoreCase("Xong")) {
                        label.getStyleClass().add("badge-completed");
                    } else if (item.equalsIgnoreCase("Đã phục vụ") || item.equalsIgnoreCase("Phục vụ")) {
                        label.getStyleClass().add("badge-served");
                    }

                    HBox container = new HBox(label);
                    container.setAlignment(Pos.CENTER);
                    setGraphic(container);
                }
            }
        });

        dishTable.getColumns().addAll(colName, colQty, colPrice, colTotal, colStatus);
        dishTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        // Populate Table
        ObservableList<DialogDishModelNew> dishList = FXCollections.observableArrayList();
        for (OrderItemModel item : selectedBill.getItems()) {
            double donGia = foodPriceMap.getOrDefault(item.getMaMon(), 0.0);
            double total = donGia * item.getSoLuong();
            
            String status = item.getTrangThai();
            String displayStatus = status;
            if (status.equalsIgnoreCase("Đang chuẩn bị") || status.equalsIgnoreCase("Đang chờ")) {
                displayStatus = "Đang chờ";
            } else if (status.equalsIgnoreCase("Đang chế biến")) {
                displayStatus = "Đang nấu";
            } else if (status.equalsIgnoreCase("Hoàn thành")) {
                displayStatus = "Xong";
            } else if (status.equalsIgnoreCase("Đã phục vụ")) {
                displayStatus = "Phục vụ";
            }

            dishList.add(new DialogDishModelNew(
                    item.getTenMon(),
                    item.getSoLuong(),
                    String.format("%,.0f VNĐ", donGia),
                    String.format("%,.0f VNĐ", total),
                    displayStatus
            ));
        }
        dishTable.setItems(dishList);

        // Close Button
        Button closeBtn = new Button("Đóng");
        closeBtn.getStyleClass().add("edit-btn");
        closeBtn.setPrefWidth(100);
        closeBtn.setOnAction(e -> dialog.close());

        HBox btnBox = new HBox(closeBtn);
        btnBox.setAlignment(Pos.CENTER_RIGHT);

        root.getChildren().addAll(titleLabel, infoGrid, tableHeader, dishTable, btnBox);

        Scene scene = new Scene(root, 650, 520);
        scene.getStylesheets().add(getClass().getResource("/css/order.css").toExternalForm());
        dialog.setScene(scene);
        dialog.setResizable(false);
        dialog.showAndWait();
    }

    private void setLoadingState(boolean loading) {
        searchField.setDisable(loading);
        statusCombo.setDisable(loading);
        tableCombo.setDisable(loading);
        datePicker.setDisable(loading);
        sortCombo.setDisable(loading);
        if (loading) {
            cardsContainer.getChildren().clear();
            Label lblLoading = new Label("Đang tải danh sách món đang order...");
            lblLoading.setStyle("-fx-font-size: 14px; -fx-text-fill: #64748b; -fx-font-weight: bold;");
            cardsContainer.getChildren().add(lblLoading);
        } else if (allBills.isEmpty()) {
            cardsContainer.getChildren().clear();
            Label lblEmpty = new Label("Không có món ăn nào đang được order");
            lblEmpty.setStyle("-fx-font-size: 14px; -fx-text-fill: #64748b; -fx-font-weight: bold;");
            cardsContainer.getChildren().add(lblEmpty);
        }
    }

    private void startAutoReload() {
        if (autoReloadTimeline != null) {
            autoReloadTimeline.stop();
        }
        autoReloadTimeline = new javafx.animation.Timeline(
                new javafx.animation.KeyFrame(javafx.util.Duration.seconds(10), event -> {
                    if (cardsContainer.getScene() == null) {
                        autoReloadTimeline.stop();
                        return;
                    }
                    loadDataSilently();
                }));
        autoReloadTimeline.setCycleCount(javafx.animation.Timeline.INDEFINITE);
        autoReloadTimeline.play();

        cardsContainer.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene == null && autoReloadTimeline != null) {
                autoReloadTimeline.stop();
            }
        });
    }

    @FXML
    private void handleRefresh() {
        searchField.clear();
        statusCombo.getSelectionModel().select("Tất cả");
        tableCombo.getSelectionModel().select("Tất cả");
        datePicker.setValue(null);
        sortCombo.getSelectionModel().select("Thời gian (Mới nhất)");
        loadData();
    }

    /* ================= ITEM DISPLAY MODEL ================= */
    public static class OrderItemModel {
        private final String maHD;
        private final String ban;
        private final String tenMon;
        private final int soLuong;
        private final String timeOrder;
        private final String trangThai;
        
        private final Timestamp timeOrderRaw;
        private final String maMon;
        private final String maBan;
        private final String maKH;
        private final String customerName;
        private final String billStatus;
        private final LocalDate localDate;

        public OrderItemModel(String maHD, String ban, String tenMon, int soLuong, String timeOrder, String trangThai, 
                              Timestamp timeOrderRaw, String maMon, String maBan, String maKH, String customerName, 
                              String billStatus, LocalDate localDate) {
            this.maHD = maHD;
            this.ban = ban;
            this.tenMon = tenMon;
            this.soLuong = soLuong;
            this.timeOrder = timeOrder;
            this.trangThai = trangThai;
            this.timeOrderRaw = timeOrderRaw;
            this.maMon = maMon;
            this.maBan = maBan;
            this.maKH = maKH;
            this.customerName = customerName;
            this.billStatus = billStatus;
            this.localDate = localDate;
        }

        public String getMaHD() { return maHD; }
        public String getBan() { return ban; }
        public String getTenMon() { return tenMon; }
        public int getSoLuong() { return soLuong; }
        public String getTimeOrder() { return timeOrder; }
        public String getTrangThai() { return trangThai; }
        public Timestamp getTimeOrderRaw() { return timeOrderRaw; }
        public String getMaMon() { return maMon; }
        public String getMaBan() { return maBan; }
        public String getMaKH() { return maKH; }
        public String getCustomerName() { return customerName; }
        public String getBillStatus() { return billStatus; }
        public LocalDate getLocalDate() { return localDate; }
    }

    /* ================= BILL CARD DISPLAY DATA ================= */
    public static class BillCardData {
        private final String maHD;
        private final String ban;
        private final String customerName;
        private final String timeOrder;
        private final Timestamp timeOrderRaw;
        private final String billStatus;
        private final LocalDate localDate;
        private final List<OrderItemModel> items;

        public BillCardData(String maHD, String ban, String customerName, String timeOrder,
                            Timestamp timeOrderRaw, String billStatus, LocalDate localDate,
                            List<OrderItemModel> items) {
            this.maHD = maHD;
            this.ban = ban;
            this.customerName = customerName;
            this.timeOrder = timeOrder;
            this.timeOrderRaw = timeOrderRaw;
            this.billStatus = billStatus;
            this.localDate = localDate;
            this.items = items;
        }

        public String getMaHD() { return maHD; }
        public String getBan() { return ban; }
        public String getCustomerName() { return customerName; }
        public String getTimeOrder() { return timeOrder; }
        public Timestamp getTimeOrderRaw() { return timeOrderRaw; }
        public String getBillStatus() { return billStatus; }
        public LocalDate getLocalDate() { return localDate; }
        public List<OrderItemModel> getItems() { return items; }
        
        public int getTotalQuantity() {
            int total = 0;
            for (OrderItemModel item : items) {
                total += item.getSoLuong();
            }
            return total;
        }
    }

    /* ================= DIALOG TABLE DISPLAY MODEL ================= */
    public static class DialogDishModelNew {
        private final String tenMon;
        private final int soLuong;
        private final String donGia;
        private final String thanhTien;
        private final String trangThai;

        public DialogDishModelNew(String tenMon, int soLuong, String donGia, String thanhTien, String trangThai) {
            this.tenMon = tenMon;
            this.soLuong = soLuong;
            this.donGia = donGia;
            this.thanhTien = thanhTien;
            this.trangThai = trangThai;
        }

        public String getTenMon() { return tenMon; }
        public int getSoLuong() { return soLuong; }
        public String getDonGia() { return donGia; }
        public String getThanhTien() { return thanhTien; }
        public String getTrangThai() { return trangThai; }
    }

    public static class OrderDetailModel {
        private String maorder;
        private String mamon;
        private String tenmon;
        private int soluong;
        private double dongia;
        private String trangthai;

        public OrderDetailModel(String maorder, String mamon, String tenmon, int soluong, double dongia, String trangthai) {
            this.maorder = maorder;
            this.mamon = mamon;
            this.tenmon = tenmon;
            this.soluong = soluong;
            this.dongia = dongia;
            this.trangthai = trangthai;
        }

        public String getMaorder() { return maorder; }
        public String getMamon() { return mamon; }
        public String getTenmon() { return tenmon; }
        public int getSoluong() { return soluong; }
        public double getDongia() { return dongia; }
        public double getThanhtien() { return soluong * dongia; }
        public String getTrangthai() { return trangthai; }
    }
}
