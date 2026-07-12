package order;

import java.sql.Date;
import java.sql.SQLException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import order.OrderController.OrderModel;
import order.OrderController.OrderDetailModel;
import network.Action;
import network.Module;
import network.Request;
import network.Response;
import network.SocketClient;

public class OrderService {

    public List<OrderModel> getAllOrders() throws SQLException {
        Request req = new Request(Module.ORDER, Action.GET_ALL, null);
        Response res = SocketClient.getInstance().sendRequest(req);

        if (res == null) {
            throw new SQLException("Mất kết nối với Server!");
        }
        if (!res.isSuccess()) {
            throw new SQLException(res.getMessage());
        }
        return mapOrders(res);
    }

    /**
     * Lấy danh sách order của một khách hàng cụ thể.
     * Dùng cho màn hình "Đơn hàng" của Customer app.
     */
    public List<OrderModel> getOrdersByKhachHang(String maKH) throws SQLException {
        Request req = new Request(Module.ORDER, "GET_BY_KHACHHANG", maKH);
        Response res = SocketClient.getInstance().sendRequest(req);

        if (res == null) {
            throw new SQLException("Mất kết nối với Server!");
        }
        if (!res.isSuccess()) {
            throw new SQLException(res.getMessage());
        }
        return mapOrders(res);
    }

    private List<OrderModel> mapOrders(Response res) {
        List<OrderModel> mapped = new ArrayList<>();
        List<model.Order> serverList = (List<model.Order>) res.getData();
        if (serverList != null) {
            for (model.Order o : serverList) {
                OrderModel orderModel = new OrderModel(
                        o.getMaOrder(),
                        o.getMaBan(),
                        o.getMaNV(),
                        o.getMaKH(),
                        o.getMaHD(),
                        o.getNgayTao() != null ? new Date(o.getNgayTao().getTime()) : null,
                        o.getTrangThai());
                orderModel.setTongMon(o.getTongMon());
                orderModel.setTongSoLuong(o.getTongSoLuong());
                orderModel.setTongTien(o.getTongTien());
                mapped.add(orderModel);
            }
        }
        return mapped;
    }

    public List<OrderDetailModel> getDetailsForOrder(String maorder) throws SQLException {
        Request req = new Request(Module.ORDER, Action.GET_BY_ID, maorder);
        Response res = SocketClient.getInstance().sendRequest(req);

        if (res == null) {
            throw new SQLException("Mất kết nối với Server!");
        }
        if (!res.isSuccess()) {
            throw new SQLException(res.getMessage());
        }

        List<OrderDetailModel> mapped = new ArrayList<>();
        List<model.OrderDetail> serverList = (List<model.OrderDetail>) res.getData();
        if (serverList != null) {
            for (model.OrderDetail d : serverList) {
                mapped.add(new OrderDetailModel(
                        d.getMaOrder(),
                        d.getMaMon(),
                        d.getTenMon(),
                        d.getSoLuong(),
                        d.getDonGia() != null ? d.getDonGia().doubleValue() : 0.0,
                        d.getTrangThai()));
            }
        }
        return mapped;
    }

    public void addOrder(OrderModel order, List<OrderDetailModel> details) throws SQLException {
        model.Order o = new model.Order(
                order.getMaorder(),
                order.getMaban(),
                order.getManv(),
                order.getMakh(), // maKH - khách hàng tự đặt
                order.getMahd(),
                order.getNgaytao() != null ? new java.sql.Timestamp(order.getNgaytao().getTime())
                        : new java.sql.Timestamp(System.currentTimeMillis()),
                order.getTrangthai());

        List<model.OrderDetail> list = new ArrayList<>();
        for (OrderDetailModel d : details) {
            list.add(new model.OrderDetail(
                    d.getMaorder(),
                    d.getMamon(),
                    d.getTenmon(),
                    d.getSoluong(),
                    BigDecimal.valueOf(d.getDongia()),
                    d.getTrangthai()));
        }

        Request req = new Request(Module.ORDER, Action.ADD, List.of(o, list));
        Response res = SocketClient.getInstance().sendRequest(req);

        if (res == null) {
            throw new SQLException("Mất kết nối với Server!");
        }
        if (!res.isSuccess()) {
            throw new SQLException(res.getMessage());
        }
    }

    public void updateOrder(OrderModel order, List<OrderDetailModel> details) throws SQLException {
        model.Order o = new model.Order(
                order.getMaorder(),
                order.getMaban(),
                order.getManv(),
                order.getMakh(),
                order.getMahd(),
                order.getNgaytao() != null ? new java.sql.Timestamp(order.getNgaytao().getTime())
                        : new java.sql.Timestamp(System.currentTimeMillis()),
                order.getTrangthai());

        List<model.OrderDetail> list = new ArrayList<>();
        for (OrderDetailModel d : details) {
            list.add(new model.OrderDetail(
                    d.getMaorder(),
                    d.getMamon(),
                    d.getTenmon(),
                    d.getSoluong(),
                    BigDecimal.valueOf(d.getDongia()),
                    d.getTrangthai()));
        }

        Request req = new Request(Module.ORDER, Action.UPDATE, List.of(o, list));
        Response res = SocketClient.getInstance().sendRequest(req);

        if (res == null) {
            throw new SQLException("Mất kết nối với Server!");
        }
        if (!res.isSuccess()) {
            throw new SQLException(res.getMessage());
        }
    }

    public void deleteOrder(String maorder) throws SQLException {
        Request req = new Request(Module.ORDER, Action.DELETE, maorder);
        Response res = SocketClient.getInstance().sendRequest(req);

        if (res == null) {
            throw new SQLException("Mất kết nối với Server!");
        }
        if (!res.isSuccess()) {
            throw new SQLException(res.getMessage());
        }
    }

    public boolean existsOrder(String maorder) throws SQLException {
        Request req = new Request(Module.ORDER, "EXISTS", maorder);
        Response res = SocketClient.getInstance().sendRequest(req);

        if (res == null) {
            throw new SQLException("Mất kết nối với Server!");
        }
        if (!res.isSuccess()) {
            throw new SQLException(res.getMessage());
        }

        return (Boolean) res.getData();
    }

    public List<String> customerAddOrder(String maKH, String maBan, List<model.OrderDetail> details)
            throws SQLException {
        Request req = new Request(Module.ORDER, "CUSTOMER_ADD_ORDER", List.of(maKH, maBan, details));
        Response res = SocketClient.getInstance().sendRequest(req);

        if (res == null) {
            throw new SQLException("Mất kết nối với Server!");
        }
        if (!res.isSuccess()) {
            throw new SQLException(res.getMessage());
        }
        return (List<String>) res.getData();
    }

    public List<model.Order> getValidOrdersForCheckout(String maKH) throws SQLException {
        Request req = new Request(Module.ORDER, "GET_VALID_ORDERS_FOR_CHECKOUT", maKH);
        Response res = SocketClient.getInstance().sendRequest(req);

        if (res == null) {
            throw new SQLException("Mất kết nối với Server!");
        }
        if (!res.isSuccess()) {
            throw new SQLException(res.getMessage());
        }
        return (List<model.Order>) res.getData();
    }

    public java.util.Map<String, Object> customerGetCheckoutInfo(String maKH) throws SQLException {
        Request req = new Request(Module.HOADON, "CUSTOMER_GET_CHECKOUT_INFO", maKH);
        Response res = SocketClient.getInstance().sendRequest(req);

        if (res == null) {
            throw new SQLException("Mất kết nối với Server!");
        }
        if (!res.isSuccess()) {
            throw new SQLException(res.getMessage());
        }
        return (java.util.Map<String, Object>) res.getData();
    }

    public model.HoaDon customerConfirmCheckout(String maKH, BigDecimal giamGia) throws SQLException {
        Request req = new Request(Module.HOADON, "CUSTOMER_CONFIRM_CHECKOUT",
                List.of(maKH, giamGia != null ? giamGia : BigDecimal.ZERO));
        Response res = SocketClient.getInstance().sendRequest(req);

        if (res == null) {
            throw new SQLException("Mất kết nối với Server!");
        }
        if (!res.isSuccess()) {
            throw new SQLException(res.getMessage());
        }
        return (model.HoaDon) res.getData();
    }
}
