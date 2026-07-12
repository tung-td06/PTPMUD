package CRUD;

import database.DBConnection;
import java.sql.*;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import model.NhapHang;

public class NhapHangDAO {

    // Lấy tất cả phiếu nhập hàng
    public List<NhapHang> getAll() {

        List<NhapHang> list = new ArrayList<>();
        String sql = "SELECT * FROM nhaphang";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {

                NhapHang nh = new NhapHang();

                nh.setMaHang(rs.getString("mahang"));
                nh.setMaNCC(rs.getString("mancc"));
                nh.setNgayNhap(rs.getTimestamp("ngaynhap"));
                nh.setTongTien(rs.getBigDecimal("tongtien"));

                list.add(nh);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return list;
    }

    // Tìm theo mã nhập hàng
    public NhapHang findID(String maHang) {

        String sql = "SELECT * FROM nhaphang WHERE mahang=?";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, maHang);

            ResultSet rs = ps.executeQuery();

            if (rs.next()) {

                NhapHang nh = new NhapHang();

                nh.setMaHang(rs.getString("mahang"));
                nh.setMaNCC(rs.getString("mancc"));
                nh.setNgayNhap(rs.getTimestamp("ngaynhap"));
                nh.setTongTien(rs.getBigDecimal("tongtien"));

                return nh;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    // Thêm phiếu nhập
    public boolean insert(NhapHang nh) {

        String sql =
                "INSERT INTO nhaphang(mahang,mancc,ngaynhap,tongtien) VALUES(?,?,?,?)";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, nh.getMaHang());
            ps.setString(2, nh.getMaNCC());
            ps.setTimestamp(3, nh.getNgayNhap());
            ps.setBigDecimal(4, nh.getTongTien());

            return ps.executeUpdate() > 0;

        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    // Cập nhật phiếu nhập
    public boolean update(NhapHang nh) {

        String sql =
                "UPDATE nhaphang SET mancc=?, ngaynhap=?, tongtien=? WHERE mahang=?";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, nh.getMaNCC());
            ps.setTimestamp(2, nh.getNgayNhap());
            ps.setBigDecimal(3, nh.getTongTien());
            ps.setString(4, nh.getMaHang());

            return ps.executeUpdate() > 0;

        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    // Xóa phiếu nhập
    public boolean delete(String maHang) {

        String sql = "DELETE FROM nhaphang WHERE mahang=?";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, maHang);

            return ps.executeUpdate() > 0;

        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    // Lọc theo khoảng ngày
    public List<NhapHang> getByDate(Date from, Date to) {

        List<NhapHang> list = new ArrayList<>();

        String sql =
                "SELECT * FROM nhaphang WHERE ngaynhap BETWEEN ? AND ?";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setTimestamp(1, toStartTimestamp(from));
            ps.setTimestamp(2, toEndTimestamp(to));

            ResultSet rs = ps.executeQuery();

            while (rs.next()) {

                NhapHang nh = new NhapHang();

                nh.setMaHang(rs.getString("mahang"));
                nh.setMaNCC(rs.getString("mancc"));
                nh.setNgayNhap(rs.getTimestamp("ngaynhap"));
                nh.setTongTien(rs.getBigDecimal("tongtien"));

                list.add(nh);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return list;
    }

    // Tìm theo nhà cung cấp
    public List<NhapHang> searchByNCC(String maNCC) {

        List<NhapHang> list = new ArrayList<>();

        String sql =
                "SELECT * FROM nhaphang WHERE mancc=?";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, maNCC);

            ResultSet rs = ps.executeQuery();

            while (rs.next()) {

                NhapHang nh = new NhapHang();

                nh.setMaHang(rs.getString("mahang"));
                nh.setMaNCC(rs.getString("mancc"));
                nh.setNgayNhap(rs.getTimestamp("ngaynhap"));
                nh.setTongTien(rs.getBigDecimal("tongtien"));

                list.add(nh);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return list;
    }

    // Kiểm tra mã nhập hàng đã tồn tại
    public boolean exists(String maHang) {

        String sql =
                "SELECT 1 FROM nhaphang WHERE mahang=?";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, maHang);

            ResultSet rs = ps.executeQuery();

            return rs.next();

        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
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
}
