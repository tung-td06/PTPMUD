package account;

import restaurant.MockDataStore;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.List;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.Button;
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

public class AccountController implements Initializable {

    @FXML
    private TableView<AccountModel> accountTable;

    @FXML
    private TableColumn<AccountModel, String> maNVColumn;

    @FXML
    private TableColumn<AccountModel, String> usernameColumn;

    @FXML
    private TableColumn<AccountModel, String> quyenColumn;

    @FXML
    private TableColumn<AccountModel, String> passwordColumn;

    @FXML
    private TextField searchField;

    @FXML
    private Label totalLabel;

    @FXML
    private Label adminLabel;

    @FXML
    private Label staffLabel;

    @FXML
    private Button createBtn;

    @FXML
    private Button editBtn;

    @FXML
    private Button deleteBtn;

    private ObservableList<AccountModel> masterList = FXCollections.observableArrayList();
    private Timeline autoReloadTimeline;
    private boolean isRequesting = false;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        setupTable();
        loadData();
        setupSearch();
        startAutoReloadTimer();

        // Stop timer when view is removed from scene to avoid memory leaks
        accountTable.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene == null) {
                stopAutoReloadTimeline();
            }
        });

        // Double click to edit
        accountTable.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2 && accountTable.getSelectionModel().getSelectedItem() != null) {
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

    private void setupTable() {
        maNVColumn.setCellValueFactory(new PropertyValueFactory<>("maNV"));
        usernameColumn.setCellValueFactory(new PropertyValueFactory<>("username"));
        quyenColumn.setCellValueFactory(new PropertyValueFactory<>("quyen"));
        passwordColumn.setCellValueFactory(new PropertyValueFactory<>("password"));

        // Mask password column
        passwordColumn.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText("••••••");
                }
            }
        });

        quyenColumn.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                    setText(null);
                } else {
                    Label label = new Label(item);
                    label.getStyleClass().add("status-tag");
                    if (item.equalsIgnoreCase("Admin")) {
                        label.getStyleClass().add("status-active");
                    } else if (item.equalsIgnoreCase("Quản lý")) {
                        label.getStyleClass().add("status-pending");
                        label.setStyle("-fx-background-color: #e0f2fe; -fx-text-fill: #0369a1;");
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
                Request req = new Request(Module.ACCOUNT, Action.GET_ALL, null);
                Response res = SocketClient.getInstance().sendRequest(req);

                Platform.runLater(() -> {
                    if (res != null && res.isSuccess() && res.getData() != null) {
                        @SuppressWarnings("unchecked")
                        List<model.Account> serverList = (List<model.Account>) res.getData();

                        AccountModel selected = accountTable.getSelectionModel().getSelectedItem();
                        String selectedId = selected != null ? selected.getMaNV() : null;

                        masterList.clear();
                        MockDataStore.accounts.clear();
                        for (model.Account ac : serverList) {
                            String roleName = switch (ac.getQuyen()) {
                                case 1 -> "Admin";
                                case 2 -> "Quản lý";
                                case 3 -> "Nhân viên";
                                default -> "Nhân viên";
                            };
                            AccountModel modelItem = new AccountModel(
                                    ac.getMaNV(),
                                    ac.getTenDN(),
                                    roleName,
                                    ac.getPassword()
                            );
                            masterList.add(modelItem);
                            MockDataStore.accounts.add(modelItem);
                        }

                        applySearch();

                        if (selectedId != null) {
                            for (AccountModel acc : accountTable.getItems()) {
                                if (acc.getMaNV().equals(selectedId)) {
                                    accountTable.getSelectionModel().select(acc);
                                    break;
                                }
                            }
                        }
                    } else {
                        showAlert("Không thể lấy danh sách tài khoản từ Server: " + (res != null ? res.getMessage() : "Không phản hồi"));
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
                Request req = new Request(Module.ACCOUNT, Action.GET_ALL, null);
                Response res = SocketClient.getInstance().sendRequest(req);

                Platform.runLater(() -> {
                    if (res != null && res.isSuccess() && res.getData() != null) {
                        @SuppressWarnings("unchecked")
                        List<model.Account> serverList = (List<model.Account>) res.getData();

                        AccountModel selected = accountTable.getSelectionModel().getSelectedItem();
                        String selectedId = selected != null ? selected.getMaNV() : null;

                        masterList.clear();
                        MockDataStore.accounts.clear();
                        for (model.Account ac : serverList) {
                            String roleName = switch (ac.getQuyen()) {
                                case 1 -> "Admin";
                                case 2 -> "Quản lý";
                                case 3 -> "Nhân viên";
                                default -> "Nhân viên";
                            };
                            AccountModel modelItem = new AccountModel(
                                    ac.getMaNV(),
                                    ac.getTenDN(),
                                    roleName,
                                    ac.getPassword()
                            );
                            masterList.add(modelItem);
                            MockDataStore.accounts.add(modelItem);
                        }

                        applySearch();

                        if (selectedId != null) {
                            for (AccountModel acc : accountTable.getItems()) {
                                if (acc.getMaNV().equals(selectedId)) {
                                    accountTable.getSelectionModel().select(acc);
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
            applySearch();
        });
    }

    private void applySearch() {
        String keyword = searchField.getText();
        if (keyword == null || keyword.trim().isEmpty()) {
            accountTable.setItems(masterList);
            updateCards();
            return;
        }
        String lowerKeyword = keyword.toLowerCase().trim();
        ObservableList<AccountModel> filtered = FXCollections.observableArrayList();
        for (AccountModel acc : masterList) {
            if ((acc.getMaNV() != null && acc.getMaNV().toLowerCase().contains(lowerKeyword)) ||
                    (acc.getUsername() != null && acc.getUsername().toLowerCase().contains(lowerKeyword)) ||
                    (acc.getQuyen() != null && acc.getQuyen().toLowerCase().contains(lowerKeyword))) {
                filtered.add(acc);
            }
        }
        accountTable.setItems(filtered);
        updateCards();
    }

    private void updateCards() {
        int total = masterList.size();
        int admin = 0;
        for (AccountModel acc : masterList) {
            if (acc.getQuyen().equalsIgnoreCase("Admin"))
                admin++;
        }
        int staff = total - admin;
        totalLabel.setText(String.valueOf(total));
        adminLabel.setText(String.valueOf(admin));
        staffLabel.setText(String.valueOf(staff));
    }

    /* ================= MODEL ================= */
    public static class AccountModel {
        private final String maNV;
        private final String username;
        private final String quyen;
        private final String password;

        public AccountModel(String maNV, String username, String quyen, String password) {
            this.maNV = maNV;
            this.username = username;
            this.quyen = quyen;
            this.password = password;
        }

        public String getMaNV() {
            return maNV;
        }

        public String getUsername() {
            return username;
        }

        public String getQuyen() {
            return quyen;
        }

        public String getPassword() {
            return password;
        }
    }

    public static AccountModel selectedAccount = null;

    @FXML
    private void handleAddNew() {
        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/fxml/accountadd.fxml"));
            javafx.scene.Parent view = loader.load();
            javafx.scene.layout.AnchorPane parent = (javafx.scene.layout.AnchorPane) accountTable.getScene()
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
        AccountModel selected = accountTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Vui lòng chọn một tài khoản từ danh sách để sửa.");
            return;
        }
        selectedAccount = selected;
        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(
                    getClass().getResource("/fxml/accountedit.fxml"));
            javafx.scene.Parent view = loader.load();
            javafx.scene.layout.AnchorPane parent = (javafx.scene.layout.AnchorPane) accountTable.getScene()
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
        AccountModel selected = accountTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Vui lòng chọn một tài khoản từ danh sách để xóa.");
            return;
        }
        selectedAccount = selected;
        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(
                    getClass().getResource("/fxml/accountdelete.fxml"));
            javafx.scene.Parent view = loader.load();
            javafx.scene.layout.AnchorPane parent = (javafx.scene.layout.AnchorPane) accountTable.getScene()
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
    private void handleRefresh() {
        searchField.clear();
        loadData();
    }

    private void showAlert(String message) {
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.WARNING);
        alert.setTitle("Thông báo");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
