package CRUD;

import database.DBConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.math.BigDecimal;
import model.Order;
import model.OrderDetail;
import model.NhanVien;
import network.Response;

public class OrderDAO {

    public List<Order> getAll() {
        List<Order> orders = new ArrayList<>();
        java.util.Map<String, List<OrderDetail>> detailsMap = new java.util.HashMap<>();

        String sqlDetails = "SELECT maorder, mamon, soluong, dongia, trangthai FROM chitietorder";
        String sqlOrders = "SELECT maorder, maban, manv, mahd, ngaytao, trangthai FROM ordermon ORDER BY ngaytao DESC";

        try (Connection conn = DBConnection.getConnection()) {
            // Load details first
            try (PreparedStatement ps = conn.prepareStatement(sqlDetails);
                    ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String maorder = rs.getString("maorder");
                    OrderDetail d = new OrderDetail();
                    d.setMaOrder(maorder);
                    d.setMaMon(rs.getString("mamon"));
                    d.setSoLuong(rs.getInt("soluong"));
                    d.setDonGia(rs.getBigDecimal("dongia"));
                    d.setTrangThai(rs.getString("trangthai"));

                    detailsMap.computeIfAbsent(maorder, k -> new ArrayList<>()).add(d);
                }
            }

            // Load orders
            try (PreparedStatement ps = conn.prepareStatement(sqlOrders);
                    ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Order order = new Order();
                    String maorder = rs.getString("maorder");
                    order.setMaOrder(maorder);
                    order.setMaBan(rs.getString("maban"));
                    order.setMaNV(rs.getString("manv"));
                    order.setMaHD(rs.getString("mahd"));
                    order.setNgayTao(rs.getTimestamp("ngaytao"));
                    order.setTrangThai(rs.getString("trangthai"));

                    // Compute totals
                    List<OrderDetail> details = detailsMap.get(maorder);
                    if (details != null) {
                        int totalMon = details.size();
                        int totalQty = 0;
                        double totalVal = 0.0;
                        for (OrderDetail d : details) {
                            totalQty += d.getSoLuong();
                            totalVal += d.getSoLuong() * (d.getDonGia() != null ? d.getDonGia().doubleValue() : 0.0);
                        }
                        order.setTongMon(totalMon);
                        order.setTongSoLuong(totalQty);
                        order.setTongTien(totalVal);
                    } else {
                        order.setTongMon(0);
                        order.setTongSoLuong(0);
                        order.setTongTien(0.0);
                    }
                    orders.add(order);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return orders;
    }

    public List<OrderDetail> getDetails(String maOrder) {
        List<OrderDetail> list = new ArrayList<>();
        String sql = "SELECT c.maorder, c.mamon, m.tenmon, c.soluong, c.dongia, c.trangthai, c.ghichu, c.thanhtien " +
                "FROM chitietorder c " +
                "LEFT JOIN monan m ON c.mamon = m.mamon " +
                "WHERE c.maorder = ?";

        try (Connection con = DBConnection.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, maOrder);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    OrderDetail d = new OrderDetail();
                    d.setMaOrder(rs.getString("maorder"));
                    d.setMaMon(rs.getString("mamon"));
                    d.setTenMon(rs.getString("tenmon"));
                    if (d.getTenMon() == null) {
                        d.setTenMon(d.getMaMon());
                    }
                    d.setSoLuong(rs.getInt("soluong"));
                    d.setDonGia(rs.getBigDecimal("dongia"));
                    d.setTrangThai(rs.getString("trangthai"));
                    d.setGhiChu(rs.getString("ghichu"));
                    d.setThanhTien(rs.getBigDecimal("thanhtien"));
                    list.add(d);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    public boolean insert(Order order, List<OrderDetail> details) {
        String sqlOrder = "INSERT INTO ordermon (maorder, maban, manv, makh, mahd, ngaytao, trangthai) VALUES (?, ?, ?, ?, ?, ?, ?)";
        String sqlDetail = "INSERT INTO chitietorder (maorder, mamon, soluong, dongia, trangthai, ghichu, thanhtien) VALUES (?, ?, ?, ?, ?, ?, ?)";

        Connection con = null;
        try {
            con = DBConnection.getConnection();
            con.setAutoCommit(false);

            try (PreparedStatement ps = con.prepareStatement(sqlOrder)) {
                ps.setString(1, order.getMaOrder());
                ps.setString(2, order.getMaBan());
                ps.setString(3, order.getMaNV());
                ps.setString(4, order.getMaKH());
                ps.setString(5, order.getMaHD());
                ps.setTimestamp(6, order.getNgayTao());
                ps.setString(7, order.getTrangThai());
                ps.executeUpdate();
            }

            try (PreparedStatement ps = con.prepareStatement(sqlDetail)) {
                for (OrderDetail d : details) {
                    ps.setString(1, order.getMaOrder());
                    ps.setString(2, d.getMaMon());
                    ps.setInt(3, d.getSoLuong());
                    ps.setBigDecimal(4, d.getDonGia());
                    ps.setString(5, d.getTrangThai());
                    ps.setString(6, d.getGhiChu());
                    ps.setBigDecimal(7, d.getThanhTien());
                    ps.addBatch();
                }
                ps.executeBatch();
            }

            con.commit();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            if (con != null) {
                try {
                    con.rollback();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
            return false;
        } finally {
            if (con != null) {
                try {
                    con.close();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
        }
    }

    public boolean update(Order order, List<OrderDetail> details) {
        String sqlUpdateOrder = "UPDATE ordermon SET maban = ?, manv = ?, makh = ?, mahd = ?, ngaytao = ?, trangthai = ? WHERE maorder = ?";
        String sqlDeleteDetails = "DELETE FROM chitietorder WHERE maorder = ?";
        String sqlInsertDetail = "INSERT INTO chitietorder (maorder, mamon, soluong, dongia, trangthai, ghichu, thanhtien) VALUES (?, ?, ?, ?, ?, ?, ?)";

        Connection con = null;
        try {
            con = DBConnection.getConnection();
            con.setAutoCommit(false);

            // 1. Update parent order
            try (PreparedStatement ps = con.prepareStatement(sqlUpdateOrder)) {
                ps.setString(1, order.getMaBan());
                ps.setString(2, order.getMaNV());
                ps.setString(3, order.getMaKH());
                ps.setString(4, order.getMaHD());
                if (order.getTrangThai() != null && order.getTrangThai().equalsIgnoreCase("Hoàn thành")) {
                    ps.setTimestamp(5, java.sql.Timestamp.valueOf(java.time.LocalDateTime.now()));
                } else {
                    ps.setTimestamp(5, order.getNgayTao());
                }
                ps.setString(6, order.getTrangThai());
                ps.setString(7, order.getMaOrder());
                ps.executeUpdate();
            }

            // 2. Delete existing details
            try (PreparedStatement ps = con.prepareStatement(sqlDeleteDetails)) {
                ps.setString(1, order.getMaOrder());
                ps.executeUpdate();
            }

            // 3. Batch insert new details
            try (PreparedStatement ps = con.prepareStatement(sqlInsertDetail)) {
                for (OrderDetail d : details) {
                    ps.setString(1, order.getMaOrder());
                    ps.setString(2, d.getMaMon());
                    ps.setInt(3, d.getSoLuong());
                    ps.setBigDecimal(4, d.getDonGia());
                    ps.setString(5, d.getTrangThai());
                    ps.setString(6, d.getGhiChu());
                    ps.setBigDecimal(7, d.getThanhTien());
                    ps.addBatch();
                }
                ps.executeBatch();
            }

            con.commit();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            if (con != null) {
                try {
                    con.rollback();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
            return false;
        } finally {
            if (con != null) {
                try {
                    con.close();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
        }
    }

    public boolean delete(String maOrder) {
        String sqlDeleteDetails = "DELETE FROM chitietorder WHERE maorder = ?";
        String sqlDeleteOrder = "DELETE FROM ordermon WHERE maorder = ?";

        Connection con = null;
        try {
            con = DBConnection.getConnection();
            con.setAutoCommit(false);

            try (PreparedStatement ps = con.prepareStatement(sqlDeleteDetails)) {
                ps.setString(1, maOrder);
                ps.executeUpdate();
            }

            try (PreparedStatement ps = con.prepareStatement(sqlDeleteOrder)) {
                ps.setString(1, maOrder);
                ps.executeUpdate();
            }

            con.commit();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            if (con != null) {
                try {
                    con.rollback();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
            return false;
        } finally {
            if (con != null) {
                try {
                    con.close();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
        }
    }

    public boolean exists(String maOrder) {
        String sql = "SELECT COUNT(*) FROM ordermon WHERE maorder = ?";
        try (Connection con = DBConnection.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, maOrder);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public Response customerAddOrder(String maKH, String maBan, List<OrderDetail> details) {
        Connection con = null;
        try {
            con = DBConnection.getConnection();
            con.setAutoCommit(false); // Begin Transaction

            // 1. Kiểm tra đặt bàn: trangthai = 'Đã nhận bàn', makh = maKH, maban = maBan
            String sqlCheckBooking = "SELECT thoigianden, timera FROM datban WHERE makh = ? AND maban = ? AND trangthai = N'Đã nhận bàn'";
            Timestamp thoiGianDen = null;
            Timestamp timeRa = null;
            try (PreparedStatement ps = con.prepareStatement(sqlCheckBooking)) {
                ps.setString(1, maKH);
                ps.setString(2, maBan);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        thoiGianDen = rs.getTimestamp("thoigianden");
                        timeRa = rs.getTimestamp("timera");
                    }
                }
            }

            if (thoiGianDen == null || timeRa == null) {
                con.rollback();
                return Response.error("BOOKING_NOT_VALID");
            }

            // Kiểm tra thời gian
            Timestamp now = new Timestamp(System.currentTimeMillis());
            if (now.before(thoiGianDen) || now.after(timeRa)) {
                con.rollback();
                return Response.error("TIME_NOT_VALID");
            }

            // Sinh mã order mới: SELECT MAX(maorder) bắt đầu bằng ORD
            String maOrder = getNextMaOrder(con);

            // Tìm nhân viên phục vụ khả dụng
            model.NhanVien assignedEmp = EmployeeAssignmentService.findAvailableEmployee(con);
            if (assignedEmp == null) {
                con.rollback();
                return Response.error("NO_STAFF_AVAILABLE");
            }

            // Insert ordermon (đổi trạng thái từ N'Đang order' sang N'Đang phục vụ')
            String sqlInsertOrder = "INSERT INTO ordermon (maorder, maban, manv, makh, mahd, ngaytao, trangthai) VALUES (?, ?, ?, ?, NULL, GETDATE(), N'Đang phục vụ')";
            try (PreparedStatement ps = con.prepareStatement(sqlInsertOrder)) {
                ps.setString(1, maOrder);
                ps.setString(2, maBan);
                ps.setString(3, assignedEmp.getMaNV());
                ps.setString(4, maKH);
                ps.executeUpdate();
            }

            // 2. Xử lý chi tiết order (luôn insert món ăn mới vì đây là một order mới)
            String sqlInsertDetail = "INSERT INTO chitietorder (maorder, mamon, soluong, dongia, trangthai, ghichu, thanhtien) VALUES (?, ?, ?, ?, N'Đang chờ', ?, ?)";

            try (PreparedStatement ps = con.prepareStatement(sqlInsertDetail)) {
                for (OrderDetail d : details) {
                    ps.setString(1, maOrder);
                    ps.setString(2, d.getMaMon());
                    ps.setInt(3, d.getSoLuong());
                    ps.setBigDecimal(4, d.getDonGia());
                    ps.setString(5, d.getGhiChu());
                    // Calculate thanhtien
                    BigDecimal price = d.getDonGia() != null ? d.getDonGia() : BigDecimal.ZERO;
                    BigDecimal total = price.multiply(BigDecimal.valueOf(d.getSoLuong()));
                    ps.setBigDecimal(6, total);
                    ps.addBatch();
                }
                ps.executeBatch();
            }

            con.commit(); // Commit Transaction
            return Response.success("Gọi món thành công.",
                    List.of(maOrder, assignedEmp.getHoTen(), assignedEmp.getMaNV()));
        } catch (Exception e) {
            e.printStackTrace();
            if (con != null) {
                try {
                    con.rollback();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
            return Response.error("Lỗi gọi món: " + e.getMessage());
        } finally {
            if (con != null) {
                try {
                    con.close();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
        }
    }

    public List<Order> getValidOrdersForCheckout(String maKH) {
        List<Order> orders = new ArrayList<>();
        // 1. Tìm đặt bàn đang hoạt động
        String sqlBooking = "SELECT TOP 1 maban, thoigianden, timera FROM datban WHERE makh = ? AND trangthai IN (N'Đã nhận bàn', N'Chờ thanh toán') ORDER BY thoigianden DESC";
        String maBan = null;
        Timestamp thoiGianDen = null;
        Timestamp timeRa = null;
        try (Connection conn = DBConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sqlBooking)) {
            ps.setString(1, maKH);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    maBan = rs.getString("maban");
                    thoiGianDen = rs.getTimestamp("thoigianden");
                    timeRa = rs.getTimestamp("timera");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (maBan == null) {
            return orders; // Trả về danh sách trống nếu không có bàn active
        }

        // 2. Lấy danh sách các order hợp lệ (chưa thanh toán và nằm trong khoảng thời
        // gian)
        String sqlOrders = "SELECT maorder, maban, manv, mahd, ngaytao, trangthai FROM ordermon " +
                "WHERE maban = ? AND ngaytao >= ? AND mahd IS NULL AND trangthai = N'Hoàn thành' "
                +
                "ORDER BY ngaytao ASC";

        try (Connection conn = DBConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sqlOrders)) {
            ps.setString(1, maBan);
            ps.setTimestamp(2, thoiGianDen);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Order order = new Order();
                    String maorder = rs.getString("maorder");
                    order.setMaOrder(maorder);
                    order.setMaBan(rs.getString("maban"));
                    order.setMaNV(rs.getString("manv"));
                    order.setMaKH(maKH);
                    order.setMaHD(rs.getString("mahd"));
                    order.setNgayTao(rs.getTimestamp("ngaytao"));
                    order.setTrangThai(rs.getString("trangthai"));
                    orders.add(order);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        // 3. Tính toán các trường tổng cho mỗi order
        for (Order o : orders) {
            List<OrderDetail> details = getDetails(o.getMaOrder());
            int totalMon = details.size();
            int totalQty = 0;
            double totalVal = 0.0;
            for (OrderDetail d : details) {
                totalQty += d.getSoLuong();
                totalVal += d.getSoLuong() * (d.getDonGia() != null ? d.getDonGia().doubleValue() : 0.0);
            }
            o.setTongMon(totalMon);
            o.setTongSoLuong(totalQty);
            o.setTongTien(totalVal);
        }

        return orders;
    }

    private String getNextMaOrder(Connection conn) throws SQLException {
        String sql = "SELECT TOP 1 maorder FROM ordermon WHERE maorder LIKE 'ORD%' ORDER BY CAST(SUBSTRING(maorder, 4, 10) AS INT) DESC";
        try (PreparedStatement ps = conn.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                String maxCode = rs.getString(1);
                return generateNextCode("ORD", maxCode);
            }
        }
        return "ORD001";
    }

    public static String generateNextCode(String prefix, String maxCode) {
        if (maxCode == null || maxCode.trim().isEmpty()) {
            return prefix + "001";
        }
        try {
            String numericPart = maxCode.substring(prefix.length()).trim();
            int val = Integer.parseInt(numericPart);
            int nextVal = val + 1;
            if (nextVal < 10) {
                return prefix + "00" + nextVal;
            } else if (nextVal < 100) {
                return prefix + "0" + nextVal;
            } else {
                return prefix + nextVal;
            }
        } catch (Exception e) {
            return prefix + "001";
        }
    }
}
