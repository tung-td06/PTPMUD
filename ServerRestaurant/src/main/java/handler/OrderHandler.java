package handler;

import CRUD.OrderDAO;
import java.util.List;
import model.Order;
import model.OrderDetail;
import network.Response;

public class OrderHandler {
    private final OrderDAO orderDAO = new OrderDAO();

    public List<Order> getAllOrders() {
        return orderDAO.getAll();
    }

    public List<OrderDetail> getDetails(String maOrder) {
        if (maOrder == null || maOrder.trim().isEmpty()) {
            return List.of();
        }
        return orderDAO.getDetails(maOrder);
    }

    public boolean addOrder(Order order, List<OrderDetail> details) {
        if (order == null || order.getMaOrder() == null || order.getMaOrder().trim().isEmpty()) {
            return false;
        }
        return orderDAO.insert(order, details);
    }

    public boolean updateOrder(Order order, List<OrderDetail> details) {
        if (order == null || order.getMaOrder() == null || order.getMaOrder().trim().isEmpty()) {
            return false;
        }
        return orderDAO.update(order, details);
    }

    public boolean deleteOrder(String maOrder) {
        if (maOrder == null || maOrder.trim().isEmpty()) {
            return false;
        }
        return orderDAO.delete(maOrder);
    }

    public boolean existsOrder(String maOrder) {
        if (maOrder == null || maOrder.trim().isEmpty()) {
            return false;
        }
        return orderDAO.exists(maOrder);
    }

    public Response customerAddOrder(String maKH, String maBan, List<OrderDetail> details) {
        if (maKH == null || maKH.trim().isEmpty() || maBan == null || maBan.trim().isEmpty() || details == null) {
            return Response.error("Dữ liệu không hợp lệ.");
        }
        return orderDAO.customerAddOrder(maKH, maBan, details);
    }

    public List<Order> getValidOrdersForCheckout(String maKH) {
        if (maKH == null || maKH.trim().isEmpty()) {
            return List.of();
        }
        return orderDAO.getValidOrdersForCheckout(maKH);
    }
}
