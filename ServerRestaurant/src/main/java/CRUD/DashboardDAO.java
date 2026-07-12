package CRUD;

import database.DBConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import model.Dashboard;

public class DashboardDAO {

    public Dashboard loadDashboard() {
        Dashboard dashboard = new Dashboard();
        
        String sqlRevenue = "SELECT ISNULL(SUM(tongtien), 0) FROM hoadon";
        String sqlOrders = "SELECT COUNT(*) FROM hoadon";
        String sqlTables = "SELECT COUNT(*) FROM banan";
        String sqlEmployees = "SELECT COUNT(*) FROM nhanvien";
        
        String sqlRecent = "SELECT TOP 10 h.mahd, k.tenkh, b.tenban, h.tongtien, h.trangthai " +
                           "FROM hoadon h " +
                           "LEFT JOIN khachhang k ON h.makh = k.makh " +
                           "LEFT JOIN banan b ON h.maban = b.maban " +
                           "ORDER BY h.mahd DESC";

        try (Connection con = DBConnection.getConnection()) {
            
            // 1. Tổng doanh thu
            try (PreparedStatement ps = con.prepareStatement(sqlRevenue);
                 ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    dashboard.setRevenue(rs.getDouble(1));
                }
            }
            
            // 2. Tổng hóa đơn
            try (PreparedStatement ps = con.prepareStatement(sqlOrders);
                 ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    dashboard.setTotalOrders(rs.getInt(1));
                }
            }
            
            // 3. Tổng bàn
            try (PreparedStatement ps = con.prepareStatement(sqlTables);
                 ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    dashboard.setTotalTables(rs.getInt(1));
                }
            }
            
            // 4. Tổng nhân viên
            try (PreparedStatement ps = con.prepareStatement(sqlEmployees);
                 ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    dashboard.setTotalEmployees(rs.getInt(1));
                }
            }
            
            // 5. Danh sách 10 hóa đơn mới nhất
            List<Dashboard.RecentBill> recentBills = new ArrayList<>();
            try (PreparedStatement ps = con.prepareStatement(sqlRecent);
                 ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String maHD = rs.getString("mahd");
                    if (maHD == null) maHD = "";
                    
                    String tenKH = rs.getString("tenkh");
                    if (tenKH == null) tenKH = "";
                    
                    String tenBan = rs.getString("tenban");
                    if (tenBan == null) tenBan = "";
                    
                    double tongTien = rs.getDouble("tongtien");
                    
                    String trangThai = rs.getString("trangthai");
                    if (trangThai == null) trangThai = "";
                    
                    Dashboard.RecentBill bill = new Dashboard.RecentBill(maHD, tenKH, tenBan, tongTien, trangThai);
                    recentBills.add(bill);
                }
            }
            dashboard.setRecentBills(recentBills);
            
            return dashboard;

        } catch (Exception e) {
            e.printStackTrace();
        }
        
        dashboard.setRecentBills(new ArrayList<>());
        return dashboard;
    }
}