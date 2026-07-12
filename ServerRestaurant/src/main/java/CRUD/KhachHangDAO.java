package CRUD;

import database.DBConnection;
import java.sql.*;
import java.util.*;
import model.KhachHang;

public class KhachHangDAO {

    public List<KhachHang> getAll() {

        List<KhachHang> list = new ArrayList<>();
        String sql = "SELECT * FROM khachhang";

        try(Connection con = DBConnection.getConnection();
            PreparedStatement ps = con.prepareStatement(sql);
            ResultSet rs = ps.executeQuery()) {

            while(rs.next()) {

                KhachHang kh = new KhachHang();

                kh.setMaKH(rs.getString("makh"));
                kh.setTenKH(rs.getString("tenkh"));
                kh.setSdt(rs.getString("sdt"));
                kh.setDiemTichLuy(rs.getInt("diemtichluy"));

                list.add(kh);
            }

        } catch(Exception e) {
            e.printStackTrace();
        }

        return list;
    }

    public KhachHang findID(String maKH) {

        String sql =
            "SELECT * FROM khachhang WHERE makh=?";

        try(Connection con = DBConnection.getConnection();
            PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, maKH);

            ResultSet rs = ps.executeQuery();

            if(rs.next()) {

                KhachHang kh = new KhachHang();

                kh.setMaKH(rs.getString("makh"));
                kh.setTenKH(rs.getString("tenkh"));
                kh.setSdt(rs.getString("sdt"));
                kh.setDiemTichLuy(rs.getInt("diemtichluy"));

                return kh;
            }

        } catch(Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public KhachHang findByPhone(String sdt) {

        String sql =
            "SELECT * FROM khachhang WHERE sdt=?";

        try(Connection con = DBConnection.getConnection();
            PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, sdt);

            ResultSet rs = ps.executeQuery();

            if(rs.next()) {

                KhachHang kh = new KhachHang();

                kh.setMaKH(rs.getString("makh"));
                kh.setTenKH(rs.getString("tenkh"));
                kh.setSdt(rs.getString("sdt"));
                kh.setDiemTichLuy(rs.getInt("diemtichluy"));

                return kh;
            }

        } catch(Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public boolean insert(KhachHang kh) {

        String sql =
            "INSERT INTO khachhang VALUES(?,?,?,?)";

        try(Connection con = DBConnection.getConnection();
            PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, kh.getMaKH());
            ps.setString(2, kh.getTenKH());
            ps.setString(3, kh.getSdt());
            ps.setInt(4, kh.getDiemTichLuy());

            return ps.executeUpdate() > 0;

        } catch(Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    public boolean update(KhachHang kh) {

        String sql =
            "UPDATE khachhang SET tenkh=?, sdt=?, diemtichluy=? WHERE makh=?";

        try(Connection con = DBConnection.getConnection();
            PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, kh.getTenKH());
            ps.setString(2, kh.getSdt());
            ps.setInt(3, kh.getDiemTichLuy());
            ps.setString(4, kh.getMaKH());

            return ps.executeUpdate() > 0;

        } catch(Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    public boolean delete(String maKH) {

        String sql =
            "DELETE FROM khachhang WHERE makh=?";

        try(Connection con = DBConnection.getConnection();
            PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, maKH);

            return ps.executeUpdate() > 0;

        } catch(Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    public List<KhachHang> searchByName(String keyword) {

        List<KhachHang> list = new ArrayList<>();

        String sql =
            "SELECT * FROM khachhang WHERE tenkh LIKE ?";

        try(Connection con = DBConnection.getConnection();
            PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, "%" + keyword + "%");

            ResultSet rs = ps.executeQuery();

            while(rs.next()) {

                KhachHang kh = new KhachHang();

                kh.setMaKH(rs.getString("makh"));
                kh.setTenKH(rs.getString("tenkh"));
                kh.setSdt(rs.getString("sdt"));
                kh.setDiemTichLuy(rs.getInt("diemtichluy"));

                list.add(kh);
            }

        } catch(Exception e) {
            e.printStackTrace();
        }

        return list;
    }

    public boolean updateDiemTichLuy(String maKH,
                                     int diem) {

        String sql =
            "UPDATE khachhang SET diemtichluy=? WHERE makh=?";

        try(Connection con = DBConnection.getConnection();
            PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, diem);
            ps.setString(2, maKH);

            return ps.executeUpdate() > 0;

        } catch(Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    public List<KhachHang> getTopKhachHang() {

        List<KhachHang> list = new ArrayList<>();

        String sql =
            "SELECT * FROM khachhang ORDER BY diemtichluy DESC";

        try(Connection con = DBConnection.getConnection();
            PreparedStatement ps = con.prepareStatement(sql);
            ResultSet rs = ps.executeQuery()) {

            while(rs.next()) {

                KhachHang kh = new KhachHang();

                kh.setMaKH(rs.getString("makh"));
                kh.setTenKH(rs.getString("tenkh"));
                kh.setSdt(rs.getString("sdt"));
                kh.setDiemTichLuy(rs.getInt("diemtichluy"));

                list.add(kh);
            }

        } catch(Exception e) {
            e.printStackTrace();
        }

        return list;
    }
}