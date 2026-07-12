package employee;

import restaurant.MockDataStore;
import java.net.URL;
import java.time.LocalDate;
import java.util.ResourceBundle;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
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
import javafx.scene.layout.HBox;
import network.Action;
import network.Module;
import network.Request;
import network.Response;
import network.SocketClient;
import javafx.animation.Timeline;
import javafx.animation.KeyFrame;
import javafx.util.Duration;
import javafx.application.Platform;

public class EmployeeController implements Initializable {

    @FXML
    private TableView<EmployeeModel> employeeTable;

    @FXML
    private Button createBtn;

    @FXML
    private Button editBtn;

    @FXML
    private Button deleteBtn;

    @FXML
    private TableColumn<EmployeeModel, String> idColumn;

    @FXML
    private TableColumn<EmployeeModel, String> nameColumn;

    @FXML
    private TableColumn<EmployeeModel, String> roleColumn;

    @FXML
    private TableColumn<EmployeeModel, String> phoneColumn;

    @FXML
    private TableColumn<EmployeeModel, String> shiftColumn;

    @FXML
    private TableColumn<EmployeeModel, String> statusColumn;

    @FXML
    private TableColumn<EmployeeModel, String> ngaySinhColumn;

    @FXML
    private TableColumn<EmployeeModel, String> queColumn;

    @FXML
    private TableColumn<EmployeeModel, String> gmailColumn;

    @FXML
    private TableColumn<EmployeeModel, String> noteColumn;

    @FXML
    private TextField searchField;

    @FXML
    private Label totalLabel;

    @FXML
    private Label activeLabel;

    @FXML
    private Label offLabel;

    @FXML
    private ComboBox<String> filterCombo;

    @FXML
    private DatePicker fromDate;

    @FXML
    private DatePicker toDate;

    private ObservableList<EmployeeModel> masterList = FXCollections.observableArrayList();

    private Timeline autoReloadTimeline;
    private boolean isRequesting = false;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        setupTable();
        loadData();
        setupSearch();
        setupFilters();
        startAutoReloadTimer();

        // Stop timer when view is removed from scene to avoid memory leaks
        employeeTable.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene == null) {
                stopAutoReloadTimeline();
            }
        });

        // Double click to edit
        employeeTable.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2 && employeeTable.getSelectionModel().getSelectedItem() != null) {
                handleEdit();
            }
        });
    }

    private void startAutoReloadTimer() {
        stopAutoReloadTimeline();
        autoReloadTimeline = new Timeline(
            new KeyFrame(Duration.seconds(10), event -> {
                if (!isRequesting) {
                    reloadFromServer();
                }
            })
        );
        autoReloadTimeline.setCycleCount(Timeline.INDEFINITE);
        autoReloadTimeline.play();
    }

    private void stopAutoReloadTimeline() {
        if (autoReloadTimeline != null) {
            autoReloadTimeline.stop();
            autoReloadTimeline = null;
        }
    }

    private void setupFilters() {
        filterCombo.setItems(FXCollections.observableArrayList(
                "Tất cả", "Quản lý", "Thu ngân", "Phục vụ", "Đầu bếp", "Bảo vệ", "Lễ tân"));
        filterCombo.getSelectionModel().select("Tất cả");
        filterCombo.setDisable(false);
        fromDate.setDisable(false);
        fromDate.setEditable(false);
        toDate.setDisable(false);
        toDate.setEditable(false);

        filterCombo.valueProperty().addListener((obs, oldVal, newVal) -> applySearchAndFilter());
        fromDate.valueProperty().addListener((obs, oldVal, newVal) -> applySearchAndFilter());
        toDate.valueProperty().addListener((obs, oldVal, newVal) -> applySearchAndFilter());
    }

    private void setupTable() {
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        roleColumn.setCellValueFactory(new PropertyValueFactory<>("role"));
        phoneColumn.setCellValueFactory(new PropertyValueFactory<>("phone"));
        shiftColumn.setCellValueFactory(new PropertyValueFactory<>("shift"));
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
        ngaySinhColumn.setCellValueFactory(new PropertyValueFactory<>("ngaySinhStr"));
        queColumn.setCellValueFactory(new PropertyValueFactory<>("que"));
        gmailColumn.setCellValueFactory(new PropertyValueFactory<>("gmail"));
        noteColumn.setCellValueFactory(new PropertyValueFactory<>("note"));

        statusColumn.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                    setText(null);
                } else {
                    Label label = new Label(item);
                    label.getStyleClass().add("status-tag");
                    if (item.equalsIgnoreCase("Đang làm việc") || item.equalsIgnoreCase("Đang làm")) {
                        label.getStyleClass().add("status-active");
                    } else if (item.equalsIgnoreCase("Nghỉ phép")) {
                        label.getStyleClass().add("status-pending");
                        label.setStyle("-fx-background-color: #fef08a; -fx-text-fill: #854d0e;");
                    } else {
                        label.getStyleClass().add("status-off");
                    }
                    HBox container = new HBox(label);
                    container.setAlignment(Pos.CENTER);
                    setGraphic(container);
                }
            }
        });
    }

    private void setButtonsDisable(boolean disable) {
        if (createBtn != null) createBtn.setDisable(disable);
        if (editBtn != null) editBtn.setDisable(disable);
        if (deleteBtn != null) deleteBtn.setDisable(disable);
    }

    private void loadData() {
        isRequesting = true;
        setButtonsDisable(true);
        new Thread(() -> {
            try {
                Request req = new Request(Module.NHANVIEN, Action.GET_ALL, null);
                Response res = SocketClient.getInstance().sendRequest(req);

                Request reqShifts = new Request(Module.CALAMVIEC, Action.GET_ALL, null);
                Response resShifts = SocketClient.getInstance().sendRequest(reqShifts);

                Platform.runLater(() -> {
                    if (res != null && res.isSuccess() && res.getData() instanceof List<?> list) {
                        List<model.NhanVien> serverList = (List<model.NhanVien>) list;

                        Map<String, String> shiftMap = new HashMap<>();
                        if (resShifts != null && resShifts.isSuccess() && resShifts.getData() instanceof List<?> shiftList) {
                            for (Object obj : shiftList) {
                                if (obj instanceof model.CaLamViec ca) {
                                    if (ca.getMaNV() != null) {
                                        String current = shiftMap.get(ca.getMaNV());
                                        if (current == null) {
                                            shiftMap.put(ca.getMaNV(), ca.getTenCa());
                                        } else if (!current.contains(ca.getTenCa())) {
                                            shiftMap.put(ca.getMaNV(), current + ", " + ca.getTenCa());
                                        }
                                    }
                                }
                            }
                        }

                        EmployeeModel selected = employeeTable.getSelectionModel().getSelectedItem();
                        String selectedId = selected != null ? selected.getId() : null;

                        masterList.clear();
                        MockDataStore.employees.clear();
                        for (model.NhanVien nv : serverList) {
                            String shiftName = shiftMap.getOrDefault(nv.getMaNV(), "Chưa phân ca");
                            EmployeeModel modelItem = new EmployeeModel(
                                    nv.getMaNV(),
                                    nv.getHoTen(),
                                    nv.getChucVu(),
                                    nv.getSdt(),
                                    shiftName,
                                    nv.getTrangThai(),
                                    nv.getNgaySinh(),
                                    nv.getQue(),
                                    nv.getGmail(),
                                    nv.getNote()
                            );
                            masterList.add(modelItem);
                            MockDataStore.employees.add(modelItem);
                        }

                        applySearchAndFilter();

                        if (selectedId != null) {
                            for (EmployeeModel emp : employeeTable.getItems()) {
                                if (emp.getId().equals(selectedId)) {
                                    employeeTable.getSelectionModel().select(emp);
                                    break;
                                }
                            }
                        }
                    } else {
                        showAlert("Không thể lấy danh sách nhân viên từ Server: " + (res != null ? res.getMessage() : "Không phản hồi"));
                    }
                    isRequesting = false;
                    setButtonsDisable(false);
                });
            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() -> {
                    showAlert("Lỗi kết nối Server: " + e.getMessage());
                    isRequesting = false;
                    setButtonsDisable(false);
                });
            }
        }).start();
    }

    private void reloadFromServer() {
        isRequesting = true;
        new Thread(() -> {
            try {
                Request req = new Request(Module.NHANVIEN, Action.GET_ALL, null);
                Response res = SocketClient.getInstance().sendRequest(req);

                Request reqShifts = new Request(Module.CALAMVIEC, Action.GET_ALL, null);
                Response resShifts = SocketClient.getInstance().sendRequest(reqShifts);

                Platform.runLater(() -> {
                    if (res != null && res.isSuccess() && res.getData() instanceof List<?> list) {
                        List<model.NhanVien> serverList = (List<model.NhanVien>) list;

                        Map<String, String> shiftMap = new HashMap<>();
                        if (resShifts != null && resShifts.isSuccess() && resShifts.getData() instanceof List<?> shiftList) {
                            for (Object obj : shiftList) {
                                if (obj instanceof model.CaLamViec ca) {
                                    if (ca.getMaNV() != null) {
                                        String current = shiftMap.get(ca.getMaNV());
                                        if (current == null) {
                                            shiftMap.put(ca.getMaNV(), ca.getTenCa());
                                        } else if (!current.contains(ca.getTenCa())) {
                                            shiftMap.put(ca.getMaNV(), current + ", " + ca.getTenCa());
                                        }
                                    }
                                }
                            }
                        }

                        EmployeeModel selected = employeeTable.getSelectionModel().getSelectedItem();
                        String selectedId = selected != null ? selected.getId() : null;

                        masterList.clear();
                        MockDataStore.employees.clear();
                        for (model.NhanVien nv : serverList) {
                            String shiftName = shiftMap.getOrDefault(nv.getMaNV(), "Chưa phân ca");
                            EmployeeModel modelItem = new EmployeeModel(
                                    nv.getMaNV(),
                                    nv.getHoTen(),
                                    nv.getChucVu(),
                                    nv.getSdt(),
                                    shiftName,
                                    nv.getTrangThai(),
                                    nv.getNgaySinh(),
                                    nv.getQue(),
                                    nv.getGmail(),
                                    nv.getNote()
                            );
                            masterList.add(modelItem);
                            MockDataStore.employees.add(modelItem);
                        }

                        applySearchAndFilter();

                        if (selectedId != null) {
                            for (EmployeeModel emp : employeeTable.getItems()) {
                                if (emp.getId().equals(selectedId)) {
                                    employeeTable.getSelectionModel().select(emp);
                                    break;
                                }
                            }
                        }
                    }
                    isRequesting = false;
                });
            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() -> {
                    isRequesting = false;
                });
            }
        }).start();
    }

    private void setupSearch() {
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            applySearchAndFilter();
        });
    }

    private void applySearchAndFilter() {
        String keyword = searchField.getText();
        String roleFilter = filterCombo.getValue();
        LocalDate from = fromDate.getValue();
        LocalDate to = toDate.getValue();

        ObservableList<EmployeeModel> filtered = FXCollections.observableArrayList();
        String lowerKeyword = (keyword == null) ? "" : keyword.toLowerCase().trim();

        for (EmployeeModel emp : masterList) {
            boolean matchesKeyword = lowerKeyword.isEmpty() ||
                    (emp.getId() != null && emp.getId().toLowerCase().contains(lowerKeyword)) ||
                    (emp.getName() != null && emp.getName().toLowerCase().contains(lowerKeyword)) ||
                    (emp.getPhone() != null && emp.getPhone().contains(lowerKeyword)) ||
                    (emp.getGmail() != null && emp.getGmail().toLowerCase().contains(lowerKeyword)) ||
                    (emp.getQue() != null && emp.getQue().toLowerCase().contains(lowerKeyword)) ||
                    (emp.getRole() != null && emp.getRole().toLowerCase().contains(lowerKeyword));

            boolean matchesRole = (roleFilter == null) || roleFilter.equals("Tất cả") ||
                    (emp.getRole() != null && emp.getRole().equalsIgnoreCase(roleFilter));

            boolean matchesDate = true;
            if (emp.getNgaySinh() != null) {
                LocalDate bday = emp.getNgaySinh().toLocalDate();
                if (from != null && bday.isBefore(from)) {
                    matchesDate = false;
                }
                if (to != null && bday.isAfter(to)) {
                    matchesDate = false;
                }
            } else if (from != null || to != null) {
                matchesDate = false;
            }

            if (matchesKeyword && matchesRole && matchesDate) {
                filtered.add(emp);
            }
        }

        employeeTable.setItems(filtered);
        updateCards();
    }

    private void updateCards() {
        int total = masterList.size();
        int active = 0;
        for (EmployeeModel emp : masterList) {
            if (emp.getStatus().equalsIgnoreCase("Đang làm việc") || emp.getStatus().equalsIgnoreCase("Đang làm")) {
                active++;
            }
        }
        int off = total - active;

        totalLabel.setText(String.valueOf(total));
        activeLabel.setText(String.valueOf(active));
        offLabel.setText(String.valueOf(off));
    }

    /* ================= MODEL ================= */
    public static class EmployeeModel {
        private final String id;
        private final String name;
        private final String role;
        private final String phone;
        private final String shift;
        private final String status;
        private final java.sql.Date ngaySinh;
        private final String que;
        private final String gmail;
        private final String note;

        public EmployeeModel(String id, String name, String role, String phone, String shift, String status) {
            this(id, name, role, phone, shift, status, null, null, null, null);
        }

        public EmployeeModel(String id, String name, String role, String phone, String shift, String status, java.sql.Date ngaySinh, String que, String gmail, String note) {
            this.id = id;
            this.name = name;
            this.role = role;
            this.phone = phone;
            this.shift = shift;
            this.status = status;
            this.ngaySinh = ngaySinh;
            this.que = que;
            this.gmail = gmail;
            this.note = note;
        }

        public String getId() {
            return id;
        }

        public String getName() {
            return name;
        }

        public String getRole() {
            return role;
        }

        public String getPhone() {
            return phone;
        }

        public String getShift() {
            return shift;
        }

        public String getStatus() {
            return status;
        }

        public java.sql.Date getNgaySinh() {
            return ngaySinh;
        }

        public String getNgaySinhStr() {
            if (ngaySinh == null) return "";
            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd/MM/yyyy");
            return sdf.format(ngaySinh);
        }

        public String getQue() {
            return que;
        }

        public String getGmail() {
            return gmail;
        }

        public String getNote() {
            return note;
        }
    }

    public static EmployeeModel selectedEmployee = null;

    @FXML
    private void handleAddNew() {
        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(
                    getClass().getResource("/fxml/employeeadd.fxml"));
            javafx.scene.Parent view = loader.load();
            javafx.scene.layout.AnchorPane parent = (javafx.scene.layout.AnchorPane) employeeTable.getScene()
                    .lookup("#contentArea");
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
        EmployeeModel selected = employeeTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Vui lòng chọn một nhân viên từ danh sách để sửa.");
            return;
        }
        selectedEmployee = selected;
        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(
                    getClass().getResource("/fxml/employeeedit.fxml"));
            javafx.scene.Parent view = loader.load();
            javafx.scene.layout.AnchorPane parent = (javafx.scene.layout.AnchorPane) employeeTable.getScene()
                    .lookup("#contentArea");
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
        EmployeeModel selected = employeeTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Vui lòng chọn một nhân viên từ danh sách để xóa.");
            return;
        }
        selectedEmployee = selected;
        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(
                    getClass().getResource("/fxml/employeedelete.fxml"));
            javafx.scene.Parent view = loader.load();
            javafx.scene.layout.AnchorPane parent = (javafx.scene.layout.AnchorPane) employeeTable.getScene()
                    .lookup("#contentArea");
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

    private void showAlert(String message) {
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.WARNING);
        alert.setTitle("Thông báo");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
