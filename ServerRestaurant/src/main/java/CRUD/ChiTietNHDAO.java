package CRUD;

import database.DBConnection;
import java.sql.*;
import java.util.*;
import model.ChiTietNH;

public class ChiTietNHDAO {

    // Lấy tất cả chi tiết nhập hàng
    public List<ChiTietNH> getAll() {

        List<ChiTietNH> list = new ArrayList<>();
        String sql = "SELECT * FROM chitietnh";

        try(Connection con = DBConnection.getConnection();
            PreparedStatement ps = con.prepareStatement(sql);
            ResultSet rs = ps.executeQuery()) {

            while(rs.next()) {

                ChiTietNH ct = new ChiTietNH();

                ct.setMaHang(rs.getString("mahang"));
                ct.setMaNL(rs.getString("manl"));
                ct.setSoLuong(rs.getInt("soluong"));
                ct.setDonGiaNhap(rs.getBigDecimal("dongianhap"));
                ct.setThanhTien(rs.getBigDecimal("thanhtien"));

                list.add(ct);
            }

        } catch(Exception e) {
            e.printStackTrace();
        }

        return list;
    }

    // Tìm theo khóa chính kép
    public ChiTietNH findID(String maHang,
                            String maNL) {

        String sql =
            "SELECT * FROM chitietnh WHERE mahang=? AND manl=?";

        try(Connection con = DBConnection.getConnection();
            PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, maHang);
            ps.setString(2, maNL);

            ResultSet rs = ps.executeQuery();

            if(rs.next()) {

                ChiTietNH ct = new ChiTietNH();

                ct.setMaHang(rs.getString("mahang"));
                ct.setMaNL(rs.getString("manl"));
                ct.setSoLuong(rs.getInt("soluong"));
                ct.setDonGiaNhap(rs.getBigDecimal("dongianhap"));
                ct.setThanhTien(rs.getBigDecimal("thanhtien"));

                return ct;
            }

        } catch(Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    // Thêm chi tiết nhập hàng
    public boolean insert(ChiTietNH ct) {

        String sql =
            "INSERT INTO chitietnh(mahang,manl,soluong,dongianhap,thanhtien) VALUES(?,?,?,?,?)";

        try(Connection con = DBConnection.getConnection();
            PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, ct.getMaHang());
            ps.setString(2, ct.getMaNL());
            ps.setInt(3, ct.getSoLuong());
            ps.setBigDecimal(4, ct.getDonGiaNhap());
            ps.setBigDecimal(5, ct.getThanhTien());

            return ps.executeUpdate() > 0;

        } catch(Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    // Cập nhật chi tiết nhập hàng
    public boolean update(ChiTietNH ct) {

        String sql =
            "UPDATE chitietnh " +
            "SET soluong=?, dongianhap=?, thanhtien=? " +
            "WHERE mahang=? AND manl=?";

        try(Connection con = DBConnection.getConnection();
            PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, ct.getSoLuong());
            ps.setBigDecimal(2, ct.getDonGiaNhap());
            ps.setBigDecimal(3, ct.getThanhTien());
            ps.setString(4, ct.getMaHang());
            ps.setString(5, ct.getMaNL());

            return ps.executeUpdate() > 0;

        } catch(Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    // Xóa chi tiết nhập hàng
    public boolean delete(String maHang,
                          String maNL) {

        String sql =
            "DELETE FROM chitietnh WHERE mahang=? AND manl=?";

        try(Connection con = DBConnection.getConnection();
            PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, maHang);
            ps.setString(2, maNL);

            return ps.executeUpdate() > 0;

        } catch(Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    // Lấy danh sách nguyên liệu theo phiếu nhập
    public List<ChiTietNH> getByNhapHang(String maHang) {

        List<ChiTietNH> list = new ArrayList<>();

        String sql =
            "SELECT * FROM chitietnh WHERE mahang=?";

        try(Connection con = DBConnection.getConnection();
            PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, maHang);

            ResultSet rs = ps.executeQuery();

            while(rs.next()) {

                ChiTietNH ct = new ChiTietNH();

                ct.setMaHang(rs.getString("mahang"));
                ct.setMaNL(rs.getString("manl"));
                ct.setSoLuong(rs.getInt("soluong"));
                ct.setDonGiaNhap(rs.getBigDecimal("dongianhap"));
                ct.setThanhTien(rs.getBigDecimal("thanhtien"));

                list.add(ct);
            }

        } catch(Exception e) {
            e.printStackTrace();
        }

        return list;
    }
}