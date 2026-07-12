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

        List<OrderModel> mapped = new ArrayList<>();
        List<model.Order> serverList = (List<model.Order>) res.getData();
        if (serverList != null) {
            for (model.Order o : serverList) {
                OrderModel model = new OrderModel(
                    o.getMaOrder(),
                    o.getMaBan(),
                    o.getMaNV(),
                    o.getMaHD(),
                    o.getNgayTao() != null ? new Date(o.getNgayTao().getTime()) : null,
                    o.getTrangThai()
                );
                model.setTongMon(o.getTongMon());
                model.setTongSoLuong(o.getTongSoLuong());
                model.setTongTien(o.getTongTien());
                mapped.add(model);
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
                    d.getTrangThai()
                ));
            }
        }
        return mapped;
    }

    public void addOrder(OrderModel order, List<OrderDetailModel> details) throws SQLException {
        model.Order o = new model.Order(
            order.getMaorder(),
            order.getMaban(),
            order.getManv(),
            order.getMahd(),
            order.getNgaytao() != null ? new java.sql.Timestamp(order.getNgaytao().getTime()) : null,
            order.getTrangthai()
        );

        List<model.OrderDetail> list = new ArrayList<>();
        for (OrderDetailModel d : details) {
            list.add(new model.OrderDetail(
                d.getMaorder(),
                d.getMamon(),
                d.getTenmon(),
                d.getSoluong(),
                BigDecimal.valueOf(d.getDongia()),
                d.getTrangthai()
            ));
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
            order.getMahd(),
            order.getNgaytao() != null ? new java.sql.Timestamp(order.getNgaytao().getTime()) : null,
            order.getTrangthai()
        );

        List<model.OrderDetail> list = new ArrayList<>();
        for (OrderDetailModel d : details) {
            list.add(new model.OrderDetail(
                d.getMaorder(),
                d.getMamon(),
                d.getTenmon(),
                d.getSoluong(),
                BigDecimal.valueOf(d.getDongia()),
                d.getTrangthai()
            ));
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
}
