package model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Dashboard implements Serializable {

    private static final long serialVersionUID = 1L;
    
    private double revenue;   // Doanh thu
    private int totalOrders;  // Đơn hàng
    private int totalTables;  // Số bàn
    private int totalEmployees; // Nhân viên
    private List<RecentBill> recentBills = new ArrayList<>(); // Danh sách 10 hóa đơn gần nhất

    public static class RecentBill implements Serializable {
        private static final long serialVersionUID = 1L;
        
        private String maHD;
        private String tenKH;
        private String tenBan;
        private double tongTien;
        private String trangThai;

        public RecentBill() {
        }

        public RecentBill(String maHD, String tenKH, String tenBan, double tongTien, String trangThai) {
            this.maHD = maHD;
            this.tenKH = tenKH;
            this.tenBan = tenBan;
            this.tongTien = tongTien;
            this.trangThai = trangThai;
        }

        public String getMaHD() {
            return maHD;
        }

        public void setMaHD(String maHD) {
            this.maHD = maHD;
        }

        public String getTenKH() {
            return tenKH;
        }

        public void setTenKH(String tenKH) {
            this.tenKH = tenKH;
        }

        public String getTenBan() {
            return tenBan;
        }

        public void setTenBan(String tenBan) {
            this.tenBan = tenBan;
        }

        public double getTongTien() {
            return tongTien;
        }

        public void setTongTien(double tongTien) {
            this.tongTien = tongTien;
        }

        public String getTrangThai() {
            return trangThai;
        }

        public void setTrangThai(String trangThai) {
            this.trangThai = trangThai;
        }
    }

    public Dashboard() {
    }

    public Dashboard(double revenue, int totalOrders, int totalTables, int totalEmployees) {
        this.revenue = revenue;
        this.totalOrders = totalOrders;
        this.totalTables = totalTables;
        this.totalEmployees = totalEmployees;
        this.recentBills = new ArrayList<>();
    }

    public Dashboard(double revenue, int totalOrders, int totalTables, int totalEmployees, List<RecentBill> recentBills) {
        this.revenue = revenue;
        this.totalOrders = totalOrders;
        this.totalTables = totalTables;
        this.totalEmployees = totalEmployees;
        this.recentBills = recentBills;
    }

    public double getRevenue() {
        return revenue;
    }

    public void setRevenue(double revenue) {
        this.revenue = revenue;
    }

    public int getTotalOrders() {
        return totalOrders;
    }

    public void setTotalOrders(int totalOrders) {
        this.totalOrders = totalOrders;
    }

    public int getTotalTables() {
        return totalTables;
    }

    public void setTotalTables(int totalTables) {
        this.totalTables = totalTables;
    }

    public int getTotalEmployees() {
        return totalEmployees;
    }

    public void setTotalEmployees(int totalEmployees) {
        this.totalEmployees = totalEmployees;
    }

    public List<RecentBill> getRecentBills() {
        return recentBills;
    }

    public void setRecentBills(List<RecentBill> recentBills) {
        this.recentBills = recentBills;
    }
}