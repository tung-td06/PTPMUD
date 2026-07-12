package CRUD;

import database.DBConnection;
import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalTime;
import java.util.*;
import model.HoaDon;
import java.util.Date;
import network.Response;

public class HoaDonDAO {

    // Lấy tất cả hóa đơn
    public List<HoaDon> getAll() {

        List<HoaDon> list = new ArrayList<>();
        String sql = "SELECT * FROM hoadon";

        try (Connection con = DBConnection.getConnection();
                PreparedStatement ps = con.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {

                HoaDon hd = new HoaDon();

                hd.setMaHD(rs.getString("mahd"));
                hd.setMaKH(rs.getString("makh"));
                hd.setMaBan(rs.getString("maban"));
                hd.setMaNV(rs.getString("manv"));

                hd.setTimeVao(rs.getTimestamp("timevao"));
                hd.setTimeRa(rs.getTimestamp("timera"));

                hd.setTongTien(rs.getBigDecimal("tongtien"));
                hd.setGiamGia(rs.getBigDecimal("giamgia"));
                hd.setThanhToan(rs.getBigDecimal("thanhtoan"));
                hd.setTrangThai(rs.getString("trangthai"));

                list.add(hd);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return list;
    }

    // Tìm theo mã hóa đơn
    public HoaDon findID(String maHD) {

        String sql = "SELECT * FROM hoadon WHERE mahd=?";

        try (Connection con = DBConnection.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, maHD);

            ResultSet rs = ps.executeQuery();

            if (rs.next()) {

                HoaDon hd = new HoaDon();

                hd.setMaHD(rs.getString("mahd"));
                hd.setMaKH(rs.getString("makh"));
                hd.setMaBan(rs.getString("maban"));
                hd.setMaNV(rs.getString("manv"));

                hd.setTimeVao(rs.getTimestamp("timevao"));
                hd.setTimeRa(rs.getTimestamp("timera"));

                hd.setTongTien(rs.getBigDecimal("tongtien"));
                hd.setGiamGia(rs.getBigDecimal("giamgia"));
                hd.setThanhToan(rs.getBigDecimal("thanhtoan"));
                hd.setTrangThai(rs.getString("trangthai"));

                return hd;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    // Thêm hóa đơn
    public boolean insert(HoaDon hd) {

        String sql = "INSERT INTO hoadon(mahd,makh,maban,manv,timevao,timera,tongtien,giamgia,thanhtoan,trangthai) VALUES(?,?,?,?,?,?,?,?,?,?)";

        try (Connection con = DBConnection.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, hd.getMaHD());
            ps.setString(2, hd.getMaKH());
            ps.setString(3, hd.getMaBan());
            ps.setString(4, hd.getMaNV());
            ps.setTimestamp(5, hd.getTimeVao());
            ps.setTimestamp(6, hd.getTimeRa());
            ps.setBigDecimal(7, hd.getTongTien());
            ps.setBigDecimal(8, hd.getGiamGia());
            ps.setBigDecimal(9, hd.getThanhToan());
            ps.setString(10, hd.getTrangThai());

            return ps.executeUpdate() > 0;

        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    // Cập nhật hóa đơn
    public boolean update(HoaDon hd) {

        String sql = "UPDATE hoadon SET makh=?,maban=?,manv=?,timevao=?,timera=?,tongtien=?,giamgia=?,thanhtoan=?,trangthai=? WHERE mahd=?";

        try (Connection con = DBConnection.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, hd.getMaKH());
            ps.setString(2, hd.getMaBan());
            ps.setString(3, hd.getMaNV());
            ps.setTimestamp(4, hd.getTimeVao());
            ps.setTimestamp(5, hd.getTimeRa());
            ps.setBigDecimal(6, hd.getTongTien());
            ps.setBigDecimal(7, hd.getGiamGia());
            ps.setBigDecimal(8, hd.getThanhToan());
            ps.setString(9, hd.getTrangThai());
            ps.setString(10, hd.getMaHD());

            return ps.executeUpdate() > 0;

        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    // Xóa hóa đơn
    public boolean delete(String maHD) {

        String sql = "DELETE FROM hoadon WHERE mahd=?";

        try (Connection con = DBConnection.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, maHD);

            return ps.executeUpdate() > 0;

        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    // Hóa đơn theo khoảng ngày
    public List<HoaDon> getByDate(Date from, Date to) {

        List<HoaDon> list = new ArrayList<>();

        String sql = "SELECT * FROM hoadon WHERE timevao BETWEEN ? AND ?";

        try (Connection con = DBConnection.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setTimestamp(1, toStartTimestamp(from));
            ps.setTimestamp(2, toEndTimestamp(to));

            ResultSet rs = ps.executeQuery();

            while (rs.next()) {

                HoaDon hd = new HoaDon();

                hd.setMaHD(rs.getString("mahd"));
                hd.setMaKH(rs.getString("makh"));
                hd.setMaBan(rs.getString("maban"));
                hd.setMaNV(rs.getString("manv"));

                hd.setTimeVao(rs.getTimestamp("timevao"));
                hd.setTimeRa(rs.getTimestamp("timera"));

                hd.setTongTien(rs.getBigDecimal("tongtien"));
                hd.setGiamGia(rs.getBigDecimal("giamgia"));
                hd.setThanhToan(rs.getBigDecimal("thanhtoan"));
                hd.setTrangThai(rs.getString("trangthai"));

                list.add(hd);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return list;
    }

    // Tìm theo khách hàng
    public List<HoaDon> findByKhachHang(String maKH) {

        List<HoaDon> list = new ArrayList<>();

        String sql = "SELECT * FROM hoadon WHERE makh=?";

        try (Connection con = DBConnection.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, maKH);

            ResultSet rs = ps.executeQuery();

            while (rs.next()) {

                HoaDon hd = new HoaDon();

                hd.setMaHD(rs.getString("mahd"));
                hd.setMaKH(rs.getString("makh"));
                hd.setMaBan(rs.getString("maban"));
                hd.setMaNV(rs.getString("manv"));
                hd.setTimeVao(rs.getTimestamp("timevao"));
                hd.setTimeRa(rs.getTimestamp("timera"));
                hd.setTongTien(rs.getBigDecimal("tongtien"));
                hd.setGiamGia(rs.getBigDecimal("giamgia"));
                hd.setThanhToan(rs.getBigDecimal("thanhtoan"));
                hd.setTrangThai(rs.getString("trangthai"));

                list.add(hd);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return list;
    }

    // Tìm theo nhân viên
    public List<HoaDon> findByNhanVien(String maNV) {

        List<HoaDon> list = new ArrayList<>();

        String sql = "SELECT * FROM hoadon WHERE manv=?";

        try (Connection con = DBConnection.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, maNV);

            ResultSet rs = ps.executeQuery();

            while (rs.next()) {

                HoaDon hd = new HoaDon();

                hd.setMaHD(rs.getString("mahd"));
                hd.setMaKH(rs.getString("makh"));
                hd.setMaBan(rs.getString("maban"));
                hd.setMaNV(rs.getString("manv"));
                hd.setTimeVao(rs.getTimestamp("timevao"));
                hd.setTimeRa(rs.getTimestamp("timera"));
                hd.setTongTien(rs.getBigDecimal("tongtien"));
                hd.setGiamGia(rs.getBigDecimal("giamgia"));
                hd.setThanhToan(rs.getBigDecimal("thanhtoan"));
                hd.setTrangThai(rs.getString("trangthai"));

                list.add(hd);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return list;
    }

    // Tìm theo bàn
    public List<HoaDon> findByBan(String maBan) {

        List<HoaDon> list = new ArrayList<>();

        String sql = "SELECT * FROM hoadon WHERE maban=?";

        try (Connection con = DBConnection.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, maBan);

            ResultSet rs = ps.executeQuery();

            while (rs.next()) {

                HoaDon hd = new HoaDon();

                hd.setMaHD(rs.getString("mahd"));
                hd.setMaKH(rs.getString("makh"));
                hd.setMaBan(rs.getString("maban"));
                hd.setMaNV(rs.getString("manv"));
                hd.setTimeVao(rs.getTimestamp("timevao"));
                hd.setTimeRa(rs.getTimestamp("timera"));
                hd.setTongTien(rs.getBigDecimal("tongtien"));
                hd.setGiamGia(rs.getBigDecimal("giamgia"));
                hd.setThanhToan(rs.getBigDecimal("thanhtoan"));
                hd.setTrangThai(rs.getString("trangthai"));

                list.add(hd);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return list;
    }

    // Hóa đơn chưa thanh toán
    public List<HoaDon> getHoaDonChuaThanhToan() {

        List<HoaDon> list = new ArrayList<>();

        String sql = "SELECT * FROM hoadon WHERE trangthai=N'Chưa thanh toán'";

        try (Connection con = DBConnection.getConnection();
                PreparedStatement ps = con.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {

                HoaDon hd = new HoaDon();

                hd.setMaHD(rs.getString("mahd"));
                hd.setMaKH(rs.getString("makh"));
                hd.setMaBan(rs.getString("maban"));
                hd.setMaNV(rs.getString("manv"));
                hd.setTimeVao(rs.getTimestamp("timevao"));
                hd.setTimeRa(rs.getTimestamp("timera"));
                hd.setTongTien(rs.getBigDecimal("tongtien"));
                hd.setGiamGia(rs.getBigDecimal("giamgia"));
                hd.setThanhToan(rs.getBigDecimal("thanhtoan"));
                hd.setTrangThai(rs.getString("trangthai"));

                list.add(hd);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return list;
    }

    // Cập nhật trạng thái
    public boolean updateTrangThai(String maHD,
            String trangThai) {

        String sql = "UPDATE hoadon SET trangthai=? WHERE mahd=?";

        try (Connection con = DBConnection.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, trangThai);
            ps.setString(2, maHD);

            return ps.executeUpdate() > 0;

        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    // Cập nhật tổng tiền
    public boolean updateTongTien(String maHD,
            BigDecimal tongTien) {

        String sql = "UPDATE hoadon SET tongtien=? WHERE mahd=?";

        try (Connection con = DBConnection.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setBigDecimal(1, tongTien);
            ps.setString(2, maHD);

            return ps.executeUpdate() > 0;

        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    // Tổng doanh thu
    public BigDecimal getTongDoanhThu() {

        String sql = "SELECT SUM(thanhtoan) tong FROM hoadon";

        try (Connection con = DBConnection.getConnection();
                PreparedStatement ps = con.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {

            if (rs.next()) {

                BigDecimal tong = rs.getBigDecimal("tong");

                return tong != null
                        ? tong
                        : BigDecimal.ZERO;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return BigDecimal.ZERO;
    }

    // Tổng doanh thu theo ngày
    public BigDecimal getTongDoanhThu(Date from, Date to) {

        String sql = "SELECT SUM(thanhtoan) tong FROM hoadon WHERE timevao BETWEEN ? AND ?";

        try (Connection con = DBConnection.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setTimestamp(1, toStartTimestamp(from));
            ps.setTimestamp(2, toEndTimestamp(to));

            ResultSet rs = ps.executeQuery();

            if (rs.next()) {

                BigDecimal tong = rs.getBigDecimal("tong");

                return tong != null
                        ? tong
                        : BigDecimal.ZERO;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return BigDecimal.ZERO;
    }

    private Timestamp toStartTimestamp(Date date) {
        return new Timestamp(date.getTime());
    }

    private Timestamp toEndTimestamp(Date date) {
        Timestamp timestamp = new Timestamp(date.getTime());

        if (timestamp.toLocalDateTime().toLocalTime().equals(LocalTime.MIDNIGHT)) {
            return Timestamp.valueOf(timestamp.toLocalDateTime().toLocalDate().atTime(LocalTime.MAX));
        }

        return timestamp;
    }

    public boolean thanhToan(String maOrder, String maNV, BigDecimal giamGia) {
        Connection con = null;
        try {
            con = DBConnection.getConnection();
            con.setAutoCommit(false); // Begin Transaction

            // 1. Lấy thông tin makh, maban từ maOrder ban đầu
            String sqlSelectOrderInfo = "SELECT makh, maban FROM ordermon WHERE maorder = ?";
            String maKH = null;
            String maBan = null;
            try (PreparedStatement psOrderInfo = con.prepareStatement(sqlSelectOrderInfo)) {
                psOrderInfo.setString(1, maOrder);
                try (ResultSet rsOrderInfo = psOrderInfo.executeQuery()) {
                    if (rsOrderInfo.next()) {
                        maKH = rsOrderInfo.getString("makh");
                        maBan = rsOrderInfo.getString("maban");
                    }
                }
            }

            if (maKH == null || maBan == null) {
                System.out.println("Khong tim thay thong tin order: " + maOrder);
                con.rollback();
                return false;
            }

            // 2. Tìm đặt bàn đang sử dụng (Đã nhận bàn) của bàn và khách hàng này
            String sqlSelectBooking = "SELECT TOP 1 madatban, thoigianden, timera FROM datban WHERE makh = ? AND maban = ? AND trangthai IN (N'Đã nhận bàn', N'Chờ thanh toán') ORDER BY thoigianden DESC";
            String maDatBan = null;
            Timestamp thoiGianDen = null;
            Timestamp timeRa = null;
            try (PreparedStatement psBooking = con.prepareStatement(sqlSelectBooking)) {
                psBooking.setString(1, maKH);
                psBooking.setString(2, maBan);
                try (ResultSet rsBooking = psBooking.executeQuery()) {
                    if (rsBooking.next()) {
                        maDatBan = rsBooking.getString("madatban");
                        thoiGianDen = rsBooking.getTimestamp("thoigianden");
                        timeRa = rsBooking.getTimestamp("timera");
                    }
                }
            }

            if (maDatBan == null) {
                System.out.println("Khong tim thay ban dat active cho ban " + maBan + " va khach hang " + maKH);
                con.rollback();
                return false;
            }

            // 3. Lấy tất cả các order chưa thanh toán của lượt đặt bàn hiện tại
            String sqlUnpaidOrders = "SELECT maorder, ngaytao FROM ordermon WHERE maban = ? AND ngaytao >= ? AND mahd IS NULL AND trangthai = N'Hoàn thành'";
            List<String> orderIds = new ArrayList<>();
            Timestamp earliestOrderTime = null;
            try (PreparedStatement psUnpaid = con.prepareStatement(sqlUnpaidOrders)) {
                psUnpaid.setString(1, maBan);
                psUnpaid.setTimestamp(2, thoiGianDen);
                try (ResultSet rsUnpaid = psUnpaid.executeQuery()) {
                    while (rsUnpaid.next()) {
                        orderIds.add(rsUnpaid.getString("maorder"));
                        Timestamp t = rsUnpaid.getTimestamp("ngaytao");
                        if (earliestOrderTime == null || t.before(earliestOrderTime)) {
                            earliestOrderTime = t;
                        }
                    }
                }
            }

            if (orderIds.isEmpty()) {
                System.out.println("Khong tim thay order chua thanh toan nao cho booking!");
                con.rollback();
                return false;
            }

            // 4. Cộng dồn và gộp các món ăn trong tất cả các order chưa thanh toán
            Map<String, model.ChiTietHD> aggregatedDetails = new HashMap<>();
            BigDecimal totalVal = BigDecimal.ZERO;

            for (String oId : orderIds) {
                String sqlSelectDetails = "SELECT mamon, soluong, dongia, trangthai FROM chitietorder WHERE maorder = ?";
                try (PreparedStatement psDetails = con.prepareStatement(sqlSelectDetails)) {
                    psDetails.setString(1, oId);
                    try (ResultSet rsDetails = psDetails.executeQuery()) {
                        while (rsDetails.next()) {
                            String mamon = rsDetails.getString("mamon");
                            int qty = rsDetails.getInt("soluong");
                            BigDecimal price = rsDetails.getBigDecimal("dongia");
                            String state = rsDetails.getString("trangthai");

                            // Bỏ qua các món ăn đã bị hủy
                            if ("Đã hủy".equalsIgnoreCase(state)) {
                                continue;
                            }

                            BigDecimal itemTotal = price.multiply(BigDecimal.valueOf(qty));
                            totalVal = totalVal.add(itemTotal);

                            if (aggregatedDetails.containsKey(mamon)) {
                                model.ChiTietHD existing = aggregatedDetails.get(mamon);
                                existing.setSoLuong(existing.getSoLuong() + qty);
                                existing.setThanhTien(existing.getThanhTien().add(itemTotal));
                            } else {
                                model.ChiTietHD ct = new model.ChiTietHD();
                                ct.setMaMon(mamon);
                                ct.setSoLuong(qty);
                                ct.setDonGia(price);
                                ct.setThanhTien(itemTotal);
                                aggregatedDetails.put(mamon, ct);
                            }
                        }
                    }
                }
            }

            if (aggregatedDetails.isEmpty()) {
                System.out.println("Cac order khong co mon an nao hop le de thanh toan!");
                con.rollback();
                return false;
            }

            // 5. Sinh mã hóa đơn mới (HD + MAX+1)
            String maHD = getNextMaHD(con);

            // 6. Tính thành tiền thực tế sau khi giảm giá
            BigDecimal thanhToanVal = totalVal.subtract(giamGia != null ? giamGia : BigDecimal.ZERO);
            if (thanhToanVal.compareTo(BigDecimal.ZERO) < 0) {
                thanhToanVal = BigDecimal.ZERO;
            }

            Timestamp currentVNTime = Timestamp
                    .valueOf(java.time.LocalDateTime.now(java.time.ZoneId.of("Asia/Ho_Chi_Minh")));

            // 7. Tạo hóa đơn
            String sqlInsertHD = "INSERT INTO hoadon (mahd, makh, maban, manv, timevao, timera, tongtien, giamgia, thanhtoan, trangthai) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
            try (PreparedStatement psInsertHD = con.prepareStatement(sqlInsertHD)) {
                psInsertHD.setString(1, maHD);
                psInsertHD.setString(2, maKH);
                psInsertHD.setString(3, maBan);
                psInsertHD.setString(4, maNV);
                psInsertHD.setTimestamp(5, earliestOrderTime != null ? earliestOrderTime : currentVNTime);
                psInsertHD.setTimestamp(6, currentVNTime);
                psInsertHD.setBigDecimal(7, totalVal);
                psInsertHD.setBigDecimal(8, giamGia != null ? giamGia : BigDecimal.ZERO);
                psInsertHD.setBigDecimal(9, thanhToanVal);
                psInsertHD.setString(10, "Đã thanh toán");
                psInsertHD.executeUpdate();
            }

            // 8. Lưu chi tiết hóa đơn
            String sqlInsertDetail = "INSERT INTO chitiethd (mahd, mamon, soluong, dongia, thanhtien) VALUES (?, ?, ?, ?, ?)";
            try (PreparedStatement psInsertDetail = con.prepareStatement(sqlInsertDetail)) {
                for (model.ChiTietHD item : aggregatedDetails.values()) {
                    psInsertDetail.setString(1, maHD);
                    psInsertDetail.setString(2, item.getMaMon());
                    psInsertDetail.setInt(3, item.getSoLuong());
                    psInsertDetail.setBigDecimal(4, item.getDonGia());
                    psInsertDetail.setBigDecimal(5, item.getThanhTien());
                    psInsertDetail.addBatch();
                }
                psInsertDetail.executeBatch();
            }

            // 9. Cập nhật các Order liên quan
            String sqlUpdateOrder = "UPDATE ordermon SET mahd = ?, trangthai = N'Đã thanh toán' WHERE maorder = ?";
            for (String oId : orderIds) {
                try (PreparedStatement psUpdateOrder = con.prepareStatement(sqlUpdateOrder)) {
                    psUpdateOrder.setString(1, maHD);
                    psUpdateOrder.setString(2, oId);
                    psUpdateOrder.executeUpdate();
                }

                // Cập nhật trạng thái chi tiết của từng order thành 'Đã phục vụ'
                String sqlUpdateOrderDetails = "UPDATE chitietorder SET trangthai = N'Đã phục vụ' WHERE maorder = ? AND trangthai IN (N'Đang chờ', N'Đang chế biến', N'Đã xong')";
                try (PreparedStatement psUpdateOrderDetails = con.prepareStatement(sqlUpdateOrderDetails)) {
                    psUpdateOrderDetails.setString(1, oId);
                    psUpdateOrderDetails.executeUpdate();
                }
            }

            // 10. Cập nhật đặt bàn (DatBan) => Hoàn thành
            String sqlUpdateBooking = "UPDATE datban SET trangthai = N'Hoàn thành' WHERE madatban = ?";
            try (PreparedStatement psUpdateBooking = con.prepareStatement(sqlUpdateBooking)) {
                psUpdateBooking.setString(1, maDatBan);
                psUpdateBooking.executeUpdate();
            }

            // 11. Cập nhật bàn ăn (BanAn) => Trống
            String sqlUpdateBan = "UPDATE banan SET trangthai = N'Trống' WHERE maban = ?";
            try (PreparedStatement psUpdateBan = con.prepareStatement(sqlUpdateBan)) {
                psUpdateBan.setString(1, maBan);
                psUpdateBan.executeUpdate();
            }

            con.commit(); // Hoàn thành toàn bộ Transaction
            System.out.println("Thanh toan hop le! Hoa don consolidated: " + maHD);
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

    private String getNextMaHD(Connection conn) throws SQLException {
        String sql = "SELECT TOP 1 mahd FROM hoadon WHERE mahd LIKE 'HD%' ORDER BY CAST(SUBSTRING(mahd, 3, 10) AS INT) DESC";
        try (PreparedStatement ps = conn.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                String maxCode = rs.getString(1);
                return generateNextCode("HD", maxCode);
            }
        }
        return "HD001";
    }

    private String generateNextCode(String prefix, String maxCode) {
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

    public Response customerCheckoutInfo(String maKH) {
        Connection con = null;
        try {
            con = DBConnection.getConnection();

            // 1. Tìm bàn active
            String sqlBooking = "SELECT TOP 1 maban, thoigianden, timera, timevao FROM datban WHERE makh = ? AND trangthai IN (N'Đã nhận bàn', N'Chờ thanh toán') ORDER BY thoigianden DESC, timevao DESC";
            String maBan = null;
            Timestamp thoiGianDen = null;
            Timestamp timeRa = null;
            Timestamp timeVao = null;
            try (PreparedStatement ps = con.prepareStatement(sqlBooking)) {
                ps.setString(1, maKH);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        maBan = rs.getString("maban");
                        thoiGianDen = rs.getTimestamp("thoigianden");
                        timeRa = rs.getTimestamp("timera");
                        timeVao = rs.getTimestamp("timevao");
                    }
                }
            }

            if (maBan == null) {
                return Response.error("Bạn chỉ có thể thanh toán khi đã nhận bàn.");
            }

            Timestamp checkInTime = thoiGianDen != null ? thoiGianDen : timeVao;

            // 2. Tìm tất cả order chưa thanh toán của lượt đặt bàn này
            String sqlOrders = "SELECT maorder FROM ordermon " +
                    "WHERE maban = ? AND ngaytao >= ? AND mahd IS NULL AND trangthai = N'Hoàn thành' " +
                    "ORDER BY ngaytao ASC";
            List<String> orderIds = new ArrayList<>();
            try (PreparedStatement ps = con.prepareStatement(sqlOrders)) {
                ps.setString(1, maBan);
                ps.setTimestamp(2, thoiGianDen != null ? thoiGianDen : timeVao);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        orderIds.add(rs.getString("maorder"));
                    }
                }
            }

            if (orderIds.isEmpty()) {
                return Response.error("Không tìm thấy đơn đặt món hoạt động nào cho bàn này.");
            }

            // 3. Đọc chi tiết order và gộp món ăn
            BigDecimal subtotal = BigDecimal.ZERO;
            int totalQty = 0;
            int totalMon = 0;
            Map<String, Map<String, Object>> aggregatedItems = new LinkedHashMap<>();

            for (String maOrder : orderIds) {
                String sqlDetails = "SELECT c.mamon, m.tenmon, c.soluong, c.dongia, c.trangthai " +
                        "FROM chitietorder c " +
                        "LEFT JOIN monan m ON c.mamon = m.mamon " +
                        "WHERE c.maorder = ?";
                try (PreparedStatement ps = con.prepareStatement(sqlDetails)) {
                    ps.setString(1, maOrder);
                    try (ResultSet rs = ps.executeQuery()) {
                        while (rs.next()) {
                            String mamon = rs.getString("mamon");
                            String tenmon = rs.getString("tenmon");
                            if (tenmon == null)
                                tenmon = mamon;
                            int qty = rs.getInt("soluong");
                            BigDecimal price = rs.getBigDecimal("dongia");
                            String state = rs.getString("trangthai");

                            if ("Đã hủy".equalsIgnoreCase(state)) {
                                continue;
                            }

                            BigDecimal itemTotal = price.multiply(BigDecimal.valueOf(qty));
                            subtotal = subtotal.add(itemTotal);
                            totalQty += qty;

                            if (aggregatedItems.containsKey(mamon)) {
                                Map<String, Object> existing = aggregatedItems.get(mamon);
                                int oldQty = (int) existing.get("soluong");
                                existing.put("soluong", oldQty + qty);
                            } else {
                                Map<String, Object> itemMap = new HashMap<>();
                                itemMap.put("mamon", mamon);
                                itemMap.put("tenmon", tenmon);
                                itemMap.put("soluong", qty);
                                itemMap.put("dongia", price);
                                itemMap.put("trangthai", state);
                                aggregatedItems.put(mamon, itemMap);
                                totalMon++;
                            }
                        }
                    }
                }
            }

            Map<String, Object> checkoutData = new HashMap<>();
            checkoutData.put("maorder", orderIds.isEmpty() ? "" : orderIds.get(0));
            checkoutData.put("maban", maBan);
            checkoutData.put("totalMon", totalMon);
            checkoutData.put("totalQty", totalQty);
            checkoutData.put("subtotal", subtotal);
            checkoutData.put("tax", BigDecimal.ZERO);
            checkoutData.put("discount", BigDecimal.ZERO);
            checkoutData.put("grandTotal", subtotal);
            checkoutData.put("items", new ArrayList<>(aggregatedItems.values()));

            return Response.success(checkoutData);

        } catch (Exception e) {
            e.printStackTrace();
            return Response.error("Lỗi khi lấy thông tin thanh toán: " + e.getMessage());
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

    public Response customerConfirmCheckout(String maKH, BigDecimal giamGia) {
        return customerConfirmCheckout(maKH, giamGia, "Chuyển khoản", 0);
    }

    public Response customerConfirmCheckout(String maKH, BigDecimal giamGia, String hinhThucThanhToan, int diemTru) {
        Connection con = null;
        try {
            con = DBConnection.getConnection();
            con.setAutoCommit(false); // Begin Transaction

            // 1. Tìm đặt bàn đang sử dụng hoặc đã được employee cập nhật thành Hoàn thành
            String sqlBooking = "SELECT TOP 1 madatban, maban, thoigianden, timera, timevao FROM datban " +
                    "WHERE makh = ? AND trangthai IN (N'Đã nhận bàn', N'Chờ thanh toán', N'Hoàn thành') " +
                    "ORDER BY thoigianden DESC, timevao DESC";
            String maBan = null;
            String maDatBan = null;
            Timestamp thoiGianDen = null;
            Timestamp timeRa = null;
            Timestamp timeVao = null;
            try (PreparedStatement ps = con.prepareStatement(sqlBooking)) {
                ps.setString(1, maKH);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        maDatBan = rs.getString("madatban");
                        maBan = rs.getString("maban");
                        thoiGianDen = rs.getTimestamp("thoigianden");
                        timeRa = rs.getTimestamp("timera");
                        timeVao = rs.getTimestamp("timevao");
                    }
                }
            }

            if (maBan == null) {
                con.rollback();
                return Response.error("Bạn chỉ có thể thanh toán khi đã nhận bàn.");
            }

            Timestamp checkInTime = thoiGianDen != null ? thoiGianDen : timeVao;

            // 2. Tìm tất cả order chưa thanh toán của lượt đặt bàn này
            String sqlUnpaidOrders = "SELECT maorder, manv, ngaytao FROM ordermon " +
                    "WHERE maban = ? AND ngaytao >= ? AND mahd IS NULL AND trangthai = N'Hoàn thành' " +
                    "ORDER BY ngaytao ASC";
            List<String> orderIds = new ArrayList<>();
            String maNV = "NV01"; // Default staff ID if none assigned to orders
            Timestamp earliestOrderTime = null;
            try (PreparedStatement psUnpaid = con.prepareStatement(sqlUnpaidOrders)) {
                psUnpaid.setString(1, maBan);
                psUnpaid.setTimestamp(2, checkInTime);
                try (ResultSet rsUnpaid = psUnpaid.executeQuery()) {
                    while (rsUnpaid.next()) {
                        String oId = rsUnpaid.getString("maorder");
                        orderIds.add(oId);
                        String dbNV = rsUnpaid.getString("manv");
                        if (dbNV != null && !dbNV.trim().isEmpty()) {
                            maNV = dbNV; // Pick the employee assigned to the order
                        }
                        Timestamp t = rsUnpaid.getTimestamp("ngaytao");
                        if (earliestOrderTime == null || t.before(earliestOrderTime)) {
                            earliestOrderTime = t;
                        }
                    }
                }
            }

            if (orderIds.isEmpty()) {
                con.rollback();
                return Response.error("Chưa có món hoàn thành để thanh toán.");
            }

            // 3. Cộng dồn và gộp các món ăn trong tất cả các order chưa thanh toán
            Map<String, model.ChiTietHD> aggregatedDetails = new LinkedHashMap<>();
            BigDecimal totalVal = BigDecimal.ZERO;

            for (String oId : orderIds) {
                String sqlSelectDetails = "SELECT mamon, soluong, dongia, trangthai FROM chitietorder WHERE maorder = ?";
                try (PreparedStatement psDetails = con.prepareStatement(sqlSelectDetails)) {
                    psDetails.setString(1, oId);
                    try (ResultSet rsDetails = psDetails.executeQuery()) {
                        while (rsDetails.next()) {
                            String mamon = rsDetails.getString("mamon");
                            int qty = rsDetails.getInt("soluong");
                            BigDecimal price = rsDetails.getBigDecimal("dongia");
                            String state = rsDetails.getString("trangthai");

                            if ("Đã hủy".equalsIgnoreCase(state)) {
                                continue;
                            }

                            BigDecimal itemTotal = price.multiply(BigDecimal.valueOf(qty));
                            totalVal = totalVal.add(itemTotal);

                            if (aggregatedDetails.containsKey(mamon)) {
                                model.ChiTietHD existing = aggregatedDetails.get(mamon);
                                existing.setSoLuong(existing.getSoLuong() + qty);
                                existing.setThanhTien(existing.getThanhTien().add(itemTotal));
                            } else {
                                model.ChiTietHD ct = new model.ChiTietHD();
                                ct.setMaMon(mamon);
                                ct.setSoLuong(qty);
                                ct.setDonGia(price);
                                ct.setThanhTien(itemTotal);
                                aggregatedDetails.put(mamon, ct);
                            }
                        }
                    }
                }
            }

            if (aggregatedDetails.isEmpty()) {
                con.rollback();
                return Response.error("Đơn hàng không có món ăn nào hợp lệ để thanh toán.");
            }

            // 4. Sinh mã hóa đơn mới (HD + MAX+1)
            String maHD = getNextMaHD(con);

            // 5. Tính thành tiền thực tế từ điểm trừ
            BigDecimal giamGiaVal = BigDecimal.valueOf(diemTru * 10L);
            if (giamGiaVal.compareTo(totalVal) > 0) {
                giamGiaVal = totalVal;
            }
            int actualDiemTru = (int) Math.ceil(giamGiaVal.doubleValue() / 10.0);
            BigDecimal thanhtoanVal = totalVal.subtract(giamGiaVal);
            if (thanhtoanVal.compareTo(BigDecimal.ZERO) < 0) {
                thanhtoanVal = BigDecimal.ZERO;
            }

            Timestamp currentVNTime = Timestamp
                    .valueOf(java.time.LocalDateTime.now(java.time.ZoneId.of("Asia/Ho_Chi_Minh")));

            // 6. Insert hoadon
            String sqlInsertHD = "INSERT INTO hoadon (mahd, makh, maban, manv, timevao, timera, tongtien, giamgia, thanhtoan, trangthai) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
            try (PreparedStatement ps = con.prepareStatement(sqlInsertHD)) {
                ps.setString(1, maHD);
                ps.setString(2, maKH);
                ps.setString(3, maBan);
                ps.setString(4, maNV);
                ps.setTimestamp(5, checkInTime != null ? checkInTime : currentVNTime);
                ps.setTimestamp(6, currentVNTime);
                ps.setBigDecimal(7, totalVal);
                ps.setBigDecimal(8, giamGiaVal);
                ps.setBigDecimal(9, thanhtoanVal);
                ps.setString(10, "Đã thanh toán");
                ps.executeUpdate();
            }

            // 7. Insert chitiethd
            String sqlInsertDetail = "INSERT INTO chitiethd (mahd, mamon, soluong, dongia, thanhtien) VALUES (?, ?, ?, ?, ?)";
            try (PreparedStatement ps = con.prepareStatement(sqlInsertDetail)) {
                for (model.ChiTietHD item : aggregatedDetails.values()) {
                    ps.setString(1, maHD);
                    ps.setString(2, item.getMaMon());
                    ps.setInt(3, item.getSoLuong());
                    ps.setBigDecimal(4, item.getDonGia());
                    ps.setBigDecimal(5, item.getThanhTien());
                    ps.addBatch();
                }
                ps.executeBatch();
            }

            // 8. Update ordermon
            String sqlUpdateOrder = "UPDATE ordermon SET mahd = ?, trangthai = N'Hoàn thành' WHERE maorder = ?";
            for (String oId : orderIds) {
                try (PreparedStatement ps = con.prepareStatement(sqlUpdateOrder)) {
                    ps.setString(1, maHD);
                    ps.setString(2, oId);
                    ps.executeUpdate();
                }

                // Cập nhật trạng thái chi tiết của từng order thành 'Đã phục vụ'
                String sqlUpdateOrderDetails = "UPDATE chitietorder SET trangthai = N'Đã phục vụ' WHERE maorder = ? AND trangthai IN (N'Đang chờ', N'Đang chế biến', N'Đã xong')";
                try (PreparedStatement psDetails = con.prepareStatement(sqlUpdateOrderDetails)) {
                    psDetails.setString(1, oId);
                    psDetails.executeUpdate();
                }
            }

            // 9. Update datban
            if (maDatBan != null) {
                String sqlUpdateBooking = "UPDATE datban SET trangthai = N'Hoàn thành' WHERE madatban = ?";
                try (PreparedStatement ps = con.prepareStatement(sqlUpdateBooking)) {
                    ps.setString(1, maDatBan);
                    ps.executeUpdate();
                }
            }

            // 10. Update banan (giải phóng trạng thái sử dụng của bàn)
            String sqlUpdateBan = "UPDATE banan SET trangthai = N'Trống' WHERE maban = ?";
            try (PreparedStatement ps = con.prepareStatement(sqlUpdateBan)) {
                ps.setString(1, maBan);
                ps.executeUpdate();
            }

            // 11. Cập nhật điểm tích lũy của khách hàng
            int diemCong = thanhtoanVal.divide(BigDecimal.valueOf(1000), 0, java.math.RoundingMode.DOWN).intValue();
            String sqlUpdatePoints = "UPDATE khachhang SET diemtichluy = diemtichluy - ? + ? WHERE makh = ?";
            try (PreparedStatement psPoints = con.prepareStatement(sqlUpdatePoints)) {
                psPoints.setInt(1, actualDiemTru);
                psPoints.setInt(2, diemCong);
                psPoints.setString(3, maKH);
                psPoints.executeUpdate();
            }

            con.commit(); // Commit Transaction

            // Trả về đối tượng HoaDon vừa tạo
            HoaDon hd = new HoaDon();
            hd.setMaHD(maHD);
            hd.setMaKH(maKH);
            hd.setMaBan(maBan);
            hd.setMaNV(maNV);
            hd.setTimeVao(checkInTime != null ? checkInTime : currentVNTime);
            hd.setTimeRa(currentVNTime);
            hd.setTongTien(totalVal);
            hd.setGiamGia(giamGiaVal);
            hd.setThanhToan(thanhtoanVal);
            hd.setTrangThai("Đã thanh toán");

            return Response.success("Thanh toán thành công.", hd);

        } catch (Exception e) {
            e.printStackTrace();
            if (con != null) {
                try {
                    con.rollback();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
            return Response.error("Lỗi khi xác nhận thanh toán: " + e.getMessage());
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
}
