package customer.controller.notification;

import customer.controller.util.CustomerSession;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import model.DatBan;
import network.Action;
import network.Module;
import network.Request;
import network.Response;
import network.SocketClient;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class CustomerNotificationController implements Initializable {

    @FXML
    private VBox notificationsContainer;

    private final SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        loadNotifications();
    }

    private void loadNotifications() {
        if (notificationsContainer == null) return;
        notificationsContainer.getChildren().clear();

        new Thread(() -> {
            List<NotificationItem> items = new ArrayList<>();

            items.add(new NotificationItem("promo", "Sieu khuyen mai mua he!",
                    "Giam ngay 20% cho hoa don tu 500k khi thanh toan bang vi dien tu hoac diem tich luy.", "Hom nay 08:00"));
            items.add(new NotificationItem("promo", "Doc quyen thanh vien VIP",
                    "Thanh vien VIP dat moc 100 diem se nhan duoc set trang mieng hao hang mien phi.", "Hom qua"));

            try {
                String maKH = CustomerSession.getCurrentCustomer().getMaKH();

                Request bookingReq = new Request(Module.DATBAN, Action.GET_ALL, null);
                Response bookingRes = SocketClient.getInstance().sendRequest(bookingReq);
                if (bookingRes != null && bookingRes.isSuccess() && bookingRes.getData() != null) {
                    List<DatBan> bookings = (List<DatBan>) bookingRes.getData();
                    for (DatBan db : bookings) {
                        if (db.getMaKH() == null || !db.getMaKH().equals(maKH)) continue;
                        String status = db.getTrangThai();
                        String timeStr = db.getTimeVao() != null ? sdf.format(db.getTimeVao()) : "";
                        String displayStatus;
                        if ("Dang cho".equalsIgnoreCase(status) || "Cho xac nhan".equalsIgnoreCase(status)) {
                            displayStatus = "Cho xac nhan";
                        } else if ("Da nhan ban".equalsIgnoreCase(status) || "Da xac nhan".equalsIgnoreCase(status)) {
                            displayStatus = "Da xac nhan";
                        } else if ("Da huy".equalsIgnoreCase(status)) {
                            displayStatus = "Da huy";
                        } else {
                            displayStatus = status != null ? status : "Cho xac nhan";
                        }
                        if ("Da xac nhan".equalsIgnoreCase(displayStatus)) {
                            items.add(new NotificationItem("booking", "Dat ban thanh cong",
                                    "Yeu cau dat ban " + db.getMaDatBan() + " da duoc xac nhan vao luc " + timeStr + ".", "Vua xong"));
                        } else if ("Cho xac nhan".equalsIgnoreCase(displayStatus)) {
                            items.add(new NotificationItem("booking", "Dat ban cho duyet",
                                    "Yeu cau dat ban " + db.getMaDatBan() + " vao luc " + timeStr + " dang cho nhan vien kiem duyet.", "Vua xong"));
                        } else if ("Da huy".equalsIgnoreCase(displayStatus)) {
                            items.add(new NotificationItem("booking", "Dat ban da huy",
                                    "Yeu cau dat ban " + db.getMaDatBan() + " vao luc " + timeStr + " da duoc huy bo.", "Vua xong"));
                        }
                    }
                }

                Request orderReq = new Request(Module.ORDER, "GET_BY_KHACHHANG", maKH);
                Response orderRes = SocketClient.getInstance().sendRequest(orderReq);
                if (orderRes != null && orderRes.isSuccess() && orderRes.getData() != null) {
                    List<model.Order> orders = (List<model.Order>) orderRes.getData();
                    for (model.Order o : orders) {
                        String status = o.getTrangThai();
                        if ("Dang cho".equalsIgnoreCase(status) || "Dang order".equalsIgnoreCase(status)) {
                            items.add(new NotificationItem("order", "Don goi mon cho xac nhan",
                                    "Don hang " + o.getMaOrder() + " da duoc gui di va dang doi nha bep tiep nhan.", "Vua xong"));
                        } else if ("Dang che bien".equalsIgnoreCase(status)) {
                            items.add(new NotificationItem("order", "Don hang dang che bien",
                                    "Don hang " + o.getMaOrder() + " da duoc xac nhan va dang duoc che bien tai nha bep.", "Vua xong"));
                        } else if ("Da xong".equalsIgnoreCase(status) || "Da phuc vu".equalsIgnoreCase(status) || "Hoan thanh".equalsIgnoreCase(status)) {
                            items.add(new NotificationItem("order", "Don hang hoan thanh",
                                    "Don hang " + o.getMaOrder() + " da hoan thanh che bien va phuc vu tai ban cua quy khach!", "Vua xong"));
                        }
                    }
                }

            } catch (Exception e) {
                e.printStackTrace();
            }

            Platform.runLater(() -> renderNotifications(items));
        }).start();
    }

    private void renderNotifications(List<NotificationItem> items) {
        if (notificationsContainer == null) return;
        for (NotificationItem ni : items) {
            HBox card = new HBox();
            card.getStyleClass().add("notification-card");
            VBox iconBg = new VBox();
            iconBg.getStyleClass().add("notification-icon-bg");
            Label iconLbl = new Label();
            iconLbl.getStyleClass().add("notification-icon");
            if (ni.type.equals("promo")) { iconBg.getStyleClass().add("icon-bg-promo"); iconLbl.setText("[P]"); }
            else if (ni.type.equals("booking")) { iconBg.getStyleClass().add("icon-bg-booking"); iconLbl.setText("[B]"); }
            else { iconBg.getStyleClass().add("icon-bg-order"); iconLbl.setText("[O]"); }
            iconBg.getChildren().add(iconLbl);
            iconBg.setAlignment(Pos.CENTER);
            VBox info = new VBox(4);
            Label title = new Label(ni.title);
            title.getStyleClass().add("notification-title");
            Label body = new Label(ni.body);
            body.getStyleClass().add("notification-body");
            body.setWrapText(true);
            Label time = new Label(ni.time);
            time.getStyleClass().add("notification-time");
            info.getChildren().addAll(title, body, time);
            HBox.setHgrow(info, Priority.ALWAYS);
            card.getChildren().addAll(iconBg, info);
            notificationsContainer.getChildren().add(card);
        }
    }

    private static class NotificationItem {
        String type, title, body, time;
        public NotificationItem(String type, String title, String body, String time) {
            this.type = type; this.title = title; this.body = body; this.time = time;
        }
    }
}
