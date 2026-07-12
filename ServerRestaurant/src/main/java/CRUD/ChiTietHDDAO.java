package CRUD;

import database.DBConnection;
import java.sql.*;
import java.util.*;
import model.ChiTietHD;

public class ChiTietHDDAO {

    // Lấy tất cả chi tiết hóa đơn
    public List<ChiTietHD> getAll() {

        List<ChiTietHD> list = new ArrayList<>();
        String sql = "SELECT * FROM chitiethd";

        try(Connection con = DBConnection.getConnection();
            PreparedStatement ps = con.prepareStatement(sql);
            ResultSet rs = ps.executeQuery()) {

            while(rs.next()) {

                ChiTietHD ct = new ChiTietHD();

                ct.setMaHD(rs.getString("mahd"));
                ct.setMaMon(rs.getString("mamon"));
                ct.setSoLuong(rs.getInt("soluong"));
                ct.setDonGia(rs.getBigDecimal("dongia"));
                ct.setThanhTien(rs.getBigDecimal("thanhtien"));

                list.add(ct);
            }
        }
        catch(Exception e) {
            e.printStackTrace();
        }

        return list;
    }

    // Tìm theo khóa chính kép
    public ChiTietHD findID(String maHD, String maMon) {

        String sql =
            "SELECT * FROM chitiethd WHERE mahd=? AND mamon=?";

        try(Connection con = DBConnection.getConnection();
            PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, maHD);
            ps.setString(2, maMon);

            ResultSet rs = ps.executeQuery();

            if(rs.next()) {

                ChiTietHD ct = new ChiTietHD();

                ct.setMaHD(rs.getString("mahd"));
                ct.setMaMon(rs.getString("mamon"));
                ct.setSoLuong(rs.getInt("soluong"));
                ct.setDonGia(rs.getBigDecimal("dongia"));
                ct.setThanhTien(rs.getBigDecimal("thanhtien"));

                return ct;
            }

        }
        catch(Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    // Thêm chi tiết hóa đơn
    public boolean insert(ChiTietHD ct) {

        String sql =
            "INSERT INTO chitiethd(mahd,mamon,soluong,dongia,thanhtien) VALUES(?,?,?,?,?)";

        try(Connection con = DBConnection.getConnection();
            PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, ct.getMaHD());
            ps.setString(2, ct.getMaMon());
            ps.setInt(3, ct.getSoLuong());
            ps.setBigDecimal(4, ct.getDonGia());
            ps.setBigDecimal(5, ct.getThanhTien());

            return ps.executeUpdate() > 0;

        }
        catch(Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    // Cập nhật chi tiết hóa đơn
    public boolean update(ChiTietHD ct) {

        String sql =
            "UPDATE chitiethd "
          + "SET soluong=?, dongia=?, thanhtien=? "
          + "WHERE mahd=? AND mamon=?";

        try(Connection con = DBConnection.getConnection();
            PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, ct.getSoLuong());
            ps.setBigDecimal(2, ct.getDonGia());
            ps.setBigDecimal(3, ct.getThanhTien());
            ps.setString(4, ct.getMaHD());
            ps.setString(5, ct.getMaMon());

            return ps.executeUpdate() > 0;

        }
        catch(Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    // Xóa chi tiết hóa đơn
    public boolean delete(String maHD, String maMon) {

        String sql =
            "DELETE FROM chitiethd WHERE mahd=? AND mamon=?";

        try(Connection con = DBConnection.getConnection();
            PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, maHD);
            ps.setString(2, maMon);

            return ps.executeUpdate() > 0;

        }
        catch(Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    // Lấy danh sách món theo hóa đơn
    public List<ChiTietHD> getByHoaDon(String maHD) {

        List<ChiTietHD> list = new ArrayList<>();

        String sql =
            "SELECT * FROM chitiethd WHERE mahd=?";

        try(Connection con = DBConnection.getConnection();
            PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, maHD);

            ResultSet rs = ps.executeQuery();

            while(rs.next()) {

                ChiTietHD ct = new ChiTietHD();

                ct.setMaHD(rs.getString("mahd"));
                ct.setMaMon(rs.getString("mamon"));
                ct.setSoLuong(rs.getInt("soluong"));
                ct.setDonGia(rs.getBigDecimal("dongia"));
                ct.setThanhTien(rs.getBigDecimal("thanhtien"));

                list.add(ct);
            }

        }
        catch(Exception e) {
            e.printStackTrace();
        }

        return list;
    }
}