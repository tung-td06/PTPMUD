package CRUD;

import database.DBConnection;
import java.sql.*;
import java.util.*;
import model.DatBan;

public class DatBanDAO {

    // Lấy tất cả đơn đặt bàn
    public List<DatBan> getAll() {

        List<DatBan> list = new ArrayList<>();
        String sql = "SELECT * FROM datban";

        try (Connection con = DBConnection.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {

            try (ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {

                DatBan db = new DatBan();

                db.setMaDatBan(rs.getString("madatban"));
                db.setMaKH(rs.getString("makh"));
                db.setMaBan(rs.getString("maban"));
                db.setTimeVao(rs.getTimestamp("timevao"));
                db.setTimeRa(rs.getTimestamp("timera"));
                db.setThoiGianDen(rs.getTimestamp("thoigianden"));
                db.setSoNguoi(rs.getInt("songuoi"));
                db.setNote(rs.getString("note"));
                db.setTrangThai(rs.getString("trangthai"));

                list.add(db);
            }

            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return list;
    }

    // Tìm theo mã đặt bàn
    public DatBan findID(String maDatBan) {

        String sql = "SELECT * FROM datban WHERE madatban=?";

        try (Connection con = DBConnection.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, maDatBan);

            try (ResultSet rs = ps.executeQuery()) {

            if (rs.next()) {

                DatBan db = new DatBan();

                db.setMaDatBan(rs.getString("madatban"));
                db.setMaKH(rs.getString("makh"));
                db.setMaBan(rs.getString("maban"));
                db.setTimeVao(rs.getTimestamp("timevao"));
                db.setTimeRa(rs.getTimestamp("timera"));
                db.setThoiGianDen(rs.getTimestamp("thoigianden"));
                db.setSoNguoi(rs.getInt("songuoi"));
                db.setNote(rs.getString("note"));
                db.setTrangThai(rs.getString("trangthai"));

                return db;
            }

            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    // Thêm đặt bàn
    public boolean insert(DatBan db) {

        String sql = "INSERT INTO datban(madatban,makh,maban,timevao,timera,thoigianden,songuoi,note,trangthai) VALUES(?,?,?,?,?,?,?,?,?)";

        try (Connection con = DBConnection.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, db.getMaDatBan());
            ps.setString(2, db.getMaKH());
            ps.setString(3, db.getMaBan());
            ps.setTimestamp(4, db.getTimeVao());
            ps.setTimestamp(5, db.getTimeRa());

            if (db.getThoiGianDen() != null) {
                ps.setTimestamp(6, db.getThoiGianDen());
            } else {
                ps.setNull(6, java.sql.Types.TIMESTAMP);
            }

            ps.setInt(7, db.getSoNguoi());
            ps.setString(8, db.getNote());
            ps.setString(9, db.getTrangThai());

            return ps.executeUpdate() > 0;

        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    // Cập nhật đặt bàn (bao gồm cả thoigianden)
    public boolean update(DatBan db) {

        String sql = "UPDATE datban SET makh=?, maban=?, timevao=?, timera=?, thoigianden=?, songuoi=?, note=?, trangthai=? WHERE madatban=?";

        try (Connection con = DBConnection.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, db.getMaKH());
            ps.setString(2, db.getMaBan());
            ps.setTimestamp(3, db.getTimeVao());
            ps.setTimestamp(4, db.getTimeRa());
            if (db.getThoiGianDen() != null) {
                ps.setTimestamp(5, db.getThoiGianDen());
            } else {
                ps.setNull(5, java.sql.Types.TIMESTAMP);
            }
            ps.setInt(6, db.getSoNguoi());
            ps.setString(7, db.getNote());
            ps.setString(8, db.getTrangThai());
            ps.setString(9, db.getMaDatBan());

            return ps.executeUpdate() > 0;

        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    // Xóa đặt bàn
    public boolean delete(String maDatBan) {

        String sql = "DELETE FROM datban WHERE madatban=?";

        try (Connection con = DBConnection.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, maDatBan);

            return ps.executeUpdate() > 0;

        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    // Tìm theo khách hàng
    public List<DatBan> findByKhachHang(String maKH) {

        List<DatBan> list = new ArrayList<>();

        String sql = "SELECT * FROM datban WHERE makh=?";

        try (Connection con = DBConnection.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, maKH);

            try (ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {

                DatBan db = new DatBan();

                db.setMaDatBan(rs.getString("madatban"));
                db.setMaKH(rs.getString("makh"));
                db.setMaBan(rs.getString("maban"));
                db.setTimeVao(rs.getTimestamp("timevao"));
                db.setTimeRa(rs.getTimestamp("timera"));
                db.setThoiGianDen(rs.getTimestamp("thoigianden"));
                db.setSoNguoi(rs.getInt("songuoi"));
                db.setNote(rs.getString("note"));
                db.setTrangThai(rs.getString("trangthai"));

                list.add(db);
            }

            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return list;
    }

    // Tìm theo bàn
    public List<DatBan> findByBan(String maBan) {

        List<DatBan> list = new ArrayList<>();

        String sql = "SELECT * FROM datban WHERE maban=?";

        try (Connection con = DBConnection.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, maBan);

            try (ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {

                DatBan db = new DatBan();

                db.setMaDatBan(rs.getString("madatban"));
                db.setMaKH(rs.getString("makh"));
                db.setMaBan(rs.getString("maban"));
                db.setTimeVao(rs.getTimestamp("timevao"));
                db.setTimeRa(rs.getTimestamp("timera"));
                db.setThoiGianDen(rs.getTimestamp("thoigianden"));
                db.setSoNguoi(rs.getInt("songuoi"));
                db.setNote(rs.getString("note"));
                db.setTrangThai(rs.getString("trangthai"));

                list.add(db);
            }

            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return list;
    }

    // Cập nhật trạng thái đặt bàn
    // Khi chuyển sang "Đã nhận bàn" và thoigianden chưa có → tự động ghi GETDATE()
    public boolean updateTrangThai(String maDatBan, String trangThai) {

        // Nếu trạng thái mới là "Đã nhận bàn", tự động ghi thoigianden nếu chưa có
        if ("Đã nhận bàn".equals(trangThai)) {
            String sql = "UPDATE datban SET trangthai=?, thoigianden = CASE WHEN thoigianden IS NULL THEN GETDATE() ELSE thoigianden END WHERE madatban=?";

            try (Connection con = DBConnection.getConnection();
                    PreparedStatement ps = con.prepareStatement(sql)) {

                ps.setString(1, trangThai);
                ps.setString(2, maDatBan);

                return ps.executeUpdate() > 0;

            } catch (Exception e) {
                e.printStackTrace();
            }

            return false;
        }

        // Trạng thái khác: chỉ cập nhật trangthai, giữ nguyên thoigianden
        String sql = "UPDATE datban SET trangthai=? WHERE madatban=?";

        try (Connection con = DBConnection.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, trangThai);
            ps.setString(2, maDatBan);

            return ps.executeUpdate() > 0;

        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    // Đặt bàn độc quyền (kiểm tra trùng lịch trước khi insert)
    public boolean datBanDocQuyen(DatBan db) {
        // 1. Kiểm tra xem bàn này đã có ai đặt trùng khung giờ chưa (áp dụng WITH
        // (UPDLOCK, ROWLOCK))
        // Câu SQL này sẽ khóa dữ liệu liên quan đến chiếc bàn cụ thể này lại
        String sqlCheck = "SELECT madatban FROM datban WITH (UPDLOCK, ROWLOCK) WHERE maban = ? AND trangthai != N'Đã hủy' AND timevao < ? AND timera > ?";

        String sqlInsert = "INSERT INTO datban(madatban, makh, maban, timevao, timera, thoigianden, songuoi, note, trangthai) VALUES(?,?,?,?,?,?,?,?,?)";

        Connection con = null;
        try {
            con = database.DBConnection.getConnection();

            // BẮT BUỘC: Tắt Auto-Commit để bắt đầu một Transaction quản lý ổ khóa
            con.setAutoCommit(false);

            // Bước A: Thực hiện SELECT ... WITH (UPDLOCK, ROWLOCK) để khóa và kiểm tra
            // trùng lịch
            try (PreparedStatement psCheck = con.prepareStatement(sqlCheck)) {
                psCheck.setString(1, db.getMaBan());
                psCheck.setTimestamp(2, db.getTimeRa());
                psCheck.setTimestamp(3, db.getTimeVao());
                try (ResultSet rs = psCheck.executeQuery()) {
                    if (rs.next()) {
                        // Nếu tìm thấy một lịch đặt bàn khác cho bàn này vẫn đang hoạt động (trangthai
                        // != N'Đã hủy')
                        // Luồng này chủ động Rollback để nhả khóa và báo lỗi thất bại
                        con.rollback();
                        System.out.println("[LOCK] Ban " + db.getMaBan() + " da bi nguoi khac dat truoc.");
                        return false;
                    }
                }
            }

            // Bước B: Nếu không trùng, tiến hành INSERT đơn đặt bàn mới
            // Các luồng khác nếu tranh chấp chiếc bàn này ở Bước A cùng lúc sẽ phải xếp
            // hàng đợi ở đây
            try (PreparedStatement psInsert = con.prepareStatement(sqlInsert)) {
                psInsert.setString(1, db.getMaDatBan());
                psInsert.setString(2, db.getMaKH());
                psInsert.setString(3, db.getMaBan());
                psInsert.setTimestamp(4, db.getTimeVao());
                psInsert.setTimestamp(5, db.getTimeRa());

                if (db.getThoiGianDen() != null) {
                    psInsert.setTimestamp(6, db.getThoiGianDen());
                } else {
                    psInsert.setNull(6, java.sql.Types.TIMESTAMP);
                }

                psInsert.setInt(7, db.getSoNguoi());
                psInsert.setString(8, db.getNote());
                psInsert.setString(9, db.getTrangThai());

                int rowsAffected = psInsert.executeUpdate();

                if (rowsAffected > 0) {
                    con.commit(); // XỬ LÝ THÀNH CÔNG: Lưu dữ liệu vĩnh viễn xuống DB và nhả khóa cho luồng sau
                    return true;
                } else {
                    con.rollback();
                    return false;
                }
            }

        } catch (Exception e) {
            // Nếu xảy ra bất kỳ lỗi gì hệ thống sẽ tự rollback để nhả khóa, tránh treo
            // Database
            if (con != null) {
                try {
                    con.rollback();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
            e.printStackTrace();
            return false;
        } finally {
            // Đảm bảo kết nối luôn được đóng lại
            if (con != null) {
                try {
                    con.close();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
        }
    }

    private Timestamp getCurrentVNTime() {
        return Timestamp.valueOf(java.time.LocalDateTime.now(java.time.ZoneId.of("Asia/Ho_Chi_Minh")));
    }

    // Tìm kiếm đặt bàn theo từ khóa
    public List<DatBan> search(String query) {
        List<DatBan> list = new ArrayList<>();
        String sql = "SELECT db.* FROM datban db "
                   + "LEFT JOIN khachhang kh ON db.makh = kh.makh "
                   + "WHERE db.madatban LIKE ? OR db.maban LIKE ? OR db.trangthai LIKE ? OR kh.tenkh LIKE ? OR kh.sdt LIKE ? OR db.note LIKE ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            String likeQuery = "%" + (query == null ? "" : query.trim()) + "%";
            for (int i = 1; i <= 6; i++) {
                ps.setString(i, likeQuery);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    DatBan db = new DatBan();
                    db.setMaDatBan(rs.getString("madatban"));
                    db.setMaKH(rs.getString("makh"));
                    db.setMaBan(rs.getString("maban"));
                    db.setTimeVao(rs.getTimestamp("timevao"));
                    db.setTimeRa(rs.getTimestamp("timera"));
                    db.setThoiGianDen(rs.getTimestamp("thoigianden"));
                    db.setSoNguoi(rs.getInt("songuoi"));
                    db.setNote(rs.getString("note"));
                    db.setTrangThai(rs.getString("trangthai"));
                    list.add(db);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    // Lọc đặt bàn theo trạng thái và khoảng thời gian
    public List<DatBan> filter(String trangThai, Timestamp tuNgay, Timestamp denNgay) {
        List<DatBan> list = new ArrayList<>();
        StringBuilder sb = new StringBuilder("SELECT * FROM datban WHERE 1=1");
        List<Object> params = new ArrayList<>();

        if (trangThai != null && !trangThai.trim().isEmpty() && !"Tất cả".equalsIgnoreCase(trangThai)) {
            sb.append(" AND trangthai = ?");
            params.add(trangThai);
        }
        if (tuNgay != null) {
            sb.append(" AND timevao >= ?");
            params.add(tuNgay);
        }
        if (denNgay != null) {
            sb.append(" AND timevao <= ?");
            params.add(denNgay);
        }

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sb.toString())) {
            for (int i = 0; i < params.size(); i++) {
                Object p = params.get(i);
                if (p instanceof String s) {
                    ps.setString(i + 1, s);
                } else if (p instanceof Timestamp t) {
                    ps.setTimestamp(i + 1, t);
                }
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    DatBan db = new DatBan();
                    db.setMaDatBan(rs.getString("madatban"));
                    db.setMaKH(rs.getString("makh"));
                    db.setMaBan(rs.getString("maban"));
                    db.setTimeVao(rs.getTimestamp("timevao"));
                    db.setTimeRa(rs.getTimestamp("timera"));
                    db.setThoiGianDen(rs.getTimestamp("thoigianden"));
                    db.setSoNguoi(rs.getInt("songuoi"));
                    db.setNote(rs.getString("note"));
                    db.setTrangThai(rs.getString("trangthai"));
                    list.add(db);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    // Tìm bản ghi đặt bàn đang được sử dụng (trạng thái = 'Đã nhận bàn') theo mã bàn
    public DatBan findActiveByBan(String maBan) {

        String sql = "SELECT * FROM datban WHERE maban = ? AND trangthai = N'Đã nhận bàn'";

        try (Connection con = DBConnection.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, maBan);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    DatBan db = new DatBan();
                    db.setMaDatBan(rs.getString("madatban"));
                    db.setMaKH(rs.getString("makh"));
                    db.setMaBan(rs.getString("maban"));
                    db.setTimeVao(rs.getTimestamp("timevao"));
                    db.setTimeRa(rs.getTimestamp("timera"));
                    db.setThoiGianDen(rs.getTimestamp("thoigianden"));
                    db.setSoNguoi(rs.getInt("songuoi"));
                    db.setNote(rs.getString("note"));
                    db.setTrangThai(rs.getString("trangthai"));
                    return db;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    // Tìm bản ghi đặt bàn đang được sử dụng (trạng thái = 'Đã nhận bàn' và chưa quá timera) theo mã khách hàng
    public DatBan findActiveByKhachHang(String maKH) {

        String sql = "SELECT TOP 1 * FROM datban WHERE makh = ? AND trangthai = N'Đã nhận bàn' AND GETDATE() <= timera ORDER BY timera DESC";

        try (Connection con = DBConnection.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, maKH);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    DatBan db = new DatBan();
                    db.setMaDatBan(rs.getString("madatban"));
                    db.setMaKH(rs.getString("makh"));
                    db.setMaBan(rs.getString("maban"));
                    db.setTimeVao(rs.getTimestamp("timevao"));
                    db.setTimeRa(rs.getTimestamp("timera"));
                    db.setThoiGianDen(rs.getTimestamp("thoigianden"));
                    db.setSoNguoi(rs.getInt("songuoi"));
                    db.setNote(rs.getString("note"));
                    db.setTrangThai(rs.getString("trangthai"));
                    return db;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public String getNextMaDatBan() {
        String sql = "SELECT TOP 1 madatban FROM datban WHERE madatban LIKE 'DB%' ORDER BY CAST(SUBSTRING(madatban, 3, 10) AS INT) DESC";
        try (Connection con = DBConnection.getConnection();
                PreparedStatement ps = con.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                String maxCode = rs.getString(1);
                return generateNextCode("DB", maxCode);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "DB001";
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
}