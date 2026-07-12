package CRUD;

import database.DBConnection;
import java.sql.*;
import java.util.*;
import model.BanAn;

public class BanAnDAO {

    public List<BanAn> getAll() {
        List<BanAn> list = new ArrayList<>();
        String sql = "SELECT * FROM banan";

        try (Connection con = DBConnection.getConnection()) {
            syncTableStatus(con);
            try (PreparedStatement ps = con.prepareStatement(sql);
                 ResultSet rs = ps.executeQuery()) {

                while (rs.next()) {
                    BanAn ba = new BanAn();
                    ba.setMaBan(rs.getString("maban"));
                    ba.setTenBan(rs.getString("tenban"));
                    ba.setKhuVuc(rs.getString("khuvuc"));
                    ba.setTrangThai(rs.getString("trangthai"));
                    list.add(ba);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return list;
    }

    public BanAn findID(String maBan) {
        String sql = "SELECT * FROM banan WHERE maban=?";

        try (Connection con = DBConnection.getConnection()) {
            syncTableStatus(con);
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setString(1, maBan);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        BanAn ba = new BanAn();
                        ba.setMaBan(rs.getString("maban"));
                        ba.setTenBan(rs.getString("tenban"));
                        ba.setKhuVuc(rs.getString("khuvuc"));
                        ba.setTrangThai(rs.getString("trangthai"));
                        return ba;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public boolean insert(BanAn ba) {

        String sql = "INSERT INTO banan VALUES(?,?,?,?)";

        try (Connection con = DBConnection.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, ba.getMaBan());
            ps.setString(2, ba.getTenBan());
            ps.setString(3, ba.getKhuVuc());
            ps.setString(4, ba.getTrangThai());

            return ps.executeUpdate() > 0;

        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    public boolean update(BanAn ba) {

        String sql = "UPDATE banan SET tenban=?, khuvuc=?, trangthai=? WHERE maban=?";

        try (Connection con = DBConnection.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, ba.getTenBan());
            ps.setString(2, ba.getKhuVuc());
            ps.setString(3, ba.getTrangThai());
            ps.setString(4, ba.getMaBan());

            return ps.executeUpdate() > 0;

        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    public boolean delete(String maBan) {

        String sql = "DELETE FROM banan WHERE maban=?";

        try (Connection con = DBConnection.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, maBan);

            return ps.executeUpdate() > 0;

        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    public List<BanAn> searchByName(String keyword) {
        List<BanAn> list = new ArrayList<>();
        String sql = "SELECT * FROM banan WHERE tenban LIKE ?";

        try (Connection con = DBConnection.getConnection()) {
            syncTableStatus(con);
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setString(1, "%" + keyword + "%");
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        BanAn ba = new BanAn();
                        ba.setMaBan(rs.getString("maban"));
                        ba.setTenBan(rs.getString("tenban"));
                        ba.setKhuVuc(rs.getString("khuvuc"));
                        ba.setTrangThai(rs.getString("trangthai"));
                        list.add(ba);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return list;
    }

    // Lấy danh sách bàn trống
    public List<BanAn> getBanTrong() {
        List<BanAn> list = new ArrayList<>();
        String sql = "SELECT * FROM banan WHERE trangthai=N'Trống'";

        try (Connection con = DBConnection.getConnection()) {
            syncTableStatus(con);
            try (PreparedStatement ps = con.prepareStatement(sql);
                 ResultSet rs = ps.executeQuery()) {

                while (rs.next()) {
                    BanAn ba = new BanAn();
                    ba.setMaBan(rs.getString("maban"));
                    ba.setTenBan(rs.getString("tenban"));
                    ba.setKhuVuc(rs.getString("khuvuc"));
                    ba.setTrangThai(rs.getString("trangthai"));
                    list.add(ba);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return list;
    }

    // Cập nhật trạng thái bàn
    public boolean updateTrangThai(String maBan,
            String trangThai) {

        String sql = "UPDATE banan SET trangthai=? WHERE maban=?";

        try (Connection con = DBConnection.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, trangThai);
            ps.setString(2, maBan);

            return ps.executeUpdate() > 0;

        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    // Đồng bộ trạng thái bàn dựa trên datban
    private void syncTableStatus(Connection con) {
        String sqlTables = "SELECT maban, trangthai FROM banan";
        String sqlBookings = "SELECT maban, trangthai, timevao, timera FROM datban WHERE CAST(timevao AS DATE) = CAST(GETDATE() AS DATE)";
        
        List<Map<String, String>> tables = new ArrayList<>();
        try (PreparedStatement ps = con.prepareStatement(sqlTables);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Map<String, String> table = new HashMap<>();
                table.put("maban", rs.getString("maban"));
                table.put("trangthai", rs.getString("trangthai"));
                tables.add(table);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return;
        }

        List<Map<String, Object>> bookings = new ArrayList<>();
        try (PreparedStatement ps = con.prepareStatement(sqlBookings);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Map<String, Object> booking = new HashMap<>();
                booking.put("maban", rs.getString("maban"));
                booking.put("trangthai", rs.getString("trangthai"));
                booking.put("timevao", rs.getTimestamp("timevao"));
                booking.put("timera", rs.getTimestamp("timera"));
                bookings.add(booking);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return;
        }

        long now = System.currentTimeMillis();
        String sqlUpdate = "UPDATE banan SET trangthai = ? WHERE maban = ?";
        try (PreparedStatement psUpdate = con.prepareStatement(sqlUpdate)) {
            boolean hasUpdates = false;
            for (Map<String, String> table : tables) {
                String maBan = table.get("maban");
                String oldStatus = table.get("trangthai");
                
                String computedStatus = "Trống";
                boolean hasOccupied = false;
                boolean hasReserved = false;
                
                for (Map<String, Object> booking : bookings) {
                    if (maBan.equalsIgnoreCase((String) booking.get("maban"))) {
                        String bookingStatus = (String) booking.get("trangthai");
                        Timestamp timeRa = (Timestamp) booking.get("timera");
                        
                        if ("Đã hủy".equalsIgnoreCase(bookingStatus) || (timeRa != null && timeRa.getTime() < now)) {
                            continue;
                        }
                        
                        if ("Đã nhận bàn".equalsIgnoreCase(bookingStatus)) {
                            hasOccupied = true;
                        } else if ("Đã xác nhận".equalsIgnoreCase(bookingStatus)) {
                            hasReserved = true;
                        }
                    }
                }
                
                if (hasOccupied) {
                    computedStatus = "Đang dùng";
                } else if (hasReserved) {
                    computedStatus = "Đặt trước";
                }
                
                if (!computedStatus.equalsIgnoreCase(oldStatus)) {
                    psUpdate.setString(1, computedStatus);
                    psUpdate.setString(2, maBan);
                    psUpdate.addBatch();
                    hasUpdates = true;
                }
            }
            if (hasUpdates) {
                psUpdate.executeBatch();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public List<BanAn> getFreeTablesInSlot(Timestamp timeIn, Timestamp timeOut) {
        List<BanAn> list = new ArrayList<>();
        String sql = "SELECT * FROM banan " +
                     "WHERE maban NOT IN (" +
                     "    SELECT DISTINCT maban FROM datban " +
                     "    WHERE maban IS NOT NULL " +
                     "      AND trangthai NOT IN (N'Đã hủy', N'Hoàn thành') " +
                     "      AND timevao < ? AND timera > ?" +
                     ")";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setTimestamp(1, timeOut);
            ps.setTimestamp(2, timeIn);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    BanAn b = new BanAn();
                    b.setMaBan(rs.getString("maban"));
                    b.setTenBan(rs.getString("tenban"));
                    b.setKhuVuc(rs.getString("khuvuc"));
                    b.setTrangThai(rs.getString("trangthai"));
                    list.add(b);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }
}