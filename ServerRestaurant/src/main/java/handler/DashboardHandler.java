package handler;

import CRUD.DashboardDAO;
import model.Dashboard;
import network.Action;
import network.Request;
import network.Response;

public class DashboardHandler {

    private final DashboardDAO dashboardDAO;

    public DashboardHandler() {
        this.dashboardDAO = new DashboardDAO();
    }

    public Response handle(Request request) {
        if (request == null) {
            return Response.error("Request rỗng");
        }
        
        String action = request.getAction();
        if (action == null) {
            return Response.error("Action không hợp lệ");
        }
        
        if (action.equalsIgnoreCase(Action.LOAD) || action.equalsIgnoreCase("LOAD")) {
            try {
                Dashboard dashboard = dashboardDAO.loadDashboard();
                if (dashboard != null) {
                    return Response.success(dashboard);
                } else {
                    return Response.error("Không thể tải dữ liệu dashboard");
                }
            } catch (Exception e) {
                return Response.error("Lỗi tải dashboard: " + e.getMessage());
            }
        }
        
        return Response.error("Action không hỗ trợ: " + action);
    }
}