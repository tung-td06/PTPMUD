package customer.controller.util;

import javafx.application.Platform;
import model.Account;
import model.AccountKH;
import model.KhachHang;
import model.DatBan;
import network.Action;
import network.Module;
import network.Request;
import network.Response;
import network.SocketClient;
import java.util.List;

public class CustomerSession {

    private static Account loggedInAccount;
    private static KhachHang currentCustomer;
    private static AccountKH currentAccountKH;
    private static DatBan activeBooking;

    public static void initialize(Account account, Runnable onComplete) {
        loggedInAccount = account;
        new Thread(() -> {
            try {
                if (Login.LoginController.loggedInAccountKH != null) {
                    currentAccountKH = Login.LoginController.loggedInAccountKH;
                } else {
                    // Step 1: Fetch all accountkh to match tenDN
                    Request accReq = new Request(Module.ACCOUNTKH, Action.GET_ALL, null);
                    Response accRes = SocketClient.getInstance().sendRequest(accReq);
                    if (accRes != null && accRes.isSuccess()) {
                        List<AccountKH> accList = (List<AccountKH>) accRes.getData();
                        if (accList != null) {
                            for (AccountKH acc : accList) {
                                if (acc.getTenDN().equalsIgnoreCase(account.getTenDN())) {
                                    currentAccountKH = acc;
                                    break;
                                }
                            }
                        }
                    }
                }

                // Step 2: Fetch all khachhang to match maKH
                if (currentAccountKH != null) {
                    Request khReq = new Request(Module.KHACHHANG, Action.GET_ALL, null);
                    Response khRes = SocketClient.getInstance().sendRequest(khReq);
                    if (khRes != null && khRes.isSuccess()) {
                        List<KhachHang> khList = (List<KhachHang>) khRes.getData();
                        if (khList != null) {
                            for (KhachHang kh : khList) {
                                if (kh.getMaKH().equals(currentAccountKH.getMaKH())) {
                                    currentCustomer = kh;
                                    break;
                                }
                            }
                        }
                    }
                }

                // Fallback if not found (e.g. mock or admin/staff testing customer app)
                if (currentCustomer == null) {
                    String defaultName = account.getTenDN();
                    if (defaultName == null || defaultName.isEmpty()) defaultName = "Khách Hàng";
                    currentCustomer = new KhachHang("KH999", defaultName, "0900000000", 120);
                    currentAccountKH = new AccountKH("KH999", account.getTenDN(), "");
                }
            } catch (Exception e) {
                e.printStackTrace();
                currentCustomer = new KhachHang("KH999", "Khách Hàng", "0900000000", 120);
                currentAccountKH = new AccountKH("KH999", account.getTenDN(), "");
            } finally {
                checkActiveBooking(onComplete);
            }
        }).start();
    }

    private static final java.util.Set<String> placedOrderCodes = new java.util.HashSet<>();

    public static void addPlacedOrderCode(String orderCode) {
        placedOrderCodes.add(orderCode);
    }

    public static boolean hasPlacedOrderCode(String orderCode) {
        return placedOrderCodes.contains(orderCode);
    }

    public static Account getLoggedInAccount() {
        return loggedInAccount;
    }

    public static KhachHang getCurrentCustomer() {
        return currentCustomer;
    }

    public static AccountKH getCurrentAccountKH() {
        return currentAccountKH;
    }

    public static void setCurrentCustomer(KhachHang kh) {
        currentCustomer = kh;
    }

    public static void checkActiveBooking(Runnable onComplete) {
        if (currentCustomer == null) {
            activeBooking = null;
            if (onComplete != null) {
                Platform.runLater(onComplete);
            }
            return;
        }
        new Thread(() -> {
            try {
                Request req = new Request(Module.DATBAN, "GET_ACTIVE_BY_KHACHHANG", currentCustomer.getMaKH());
                Response res = SocketClient.getInstance().sendRequest(req);
                if (res != null && res.isSuccess()) {
                    activeBooking = (DatBan) res.getData();
                } else {
                    activeBooking = null;
                }
            } catch (Exception e) {
                e.printStackTrace();
                activeBooking = null;
            } finally {
                if (onComplete != null) {
                    Platform.runLater(onComplete);
                }
            }
        }).start();
    }

    public static boolean checkBookingValidSync() {
        if (currentCustomer == null) {
            activeBooking = null;
            return false;
        }
        try {
            Request req = new Request(Module.DATBAN, "GET_ACTIVE_BY_KHACHHANG", currentCustomer.getMaKH());
            Response res = SocketClient.getInstance().sendRequest(req);
            if (res != null && res.isSuccess() && res.getData() != null) {
                activeBooking = (DatBan) res.getData();
                return true;
            } else {
                activeBooking = null;
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
            activeBooking = null;
            return false;
        }
    }

    public static DatBan getActiveBooking() {
        return activeBooking;
    }
}
