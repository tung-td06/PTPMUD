package CRUD;

import database.DBConnection;
import java.sql.*;
import java.util.*;
import model.CaLamViec;

public class CaLamViecDAO {

    // Lấy tất cả ca làm việc
    public List<CaLamViec> getAll() {

        List<CaLamViec> list = new ArrayList<>();
        String sql = "SELECT * FROM calamviec";

        try(Connection con = DBConnection.getConnection();
            PreparedStatement ps = con.prepareStatement(sql);
            ResultSet rs = ps.executeQuery()) {

            while(rs.next()) {

                CaLamViec ca = new CaLamViec();

                ca.setMaCa(rs.getString("maca"));
                ca.setMaNV(rs.getString("manv"));
                ca.setTenCa(rs.getString("tenca"));
                ca.setGioBatDau(rs.getTimestamp("giobatdau"));
                ca.setGioKetThuc(rs.getTimestamp("gioketthuc"));

                list.add(ca);
            }

        } catch(Exception e) {
            e.printStackTrace();
        }

        return list;
    }

    // Tìm theo mã ca
    public CaLamViec findID(String maCa) {

        String sql =
            "SELECT * FROM calamviec WHERE maca=?";

        try(Connection con = DBConnection.getConnection();
            PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, maCa);

            ResultSet rs = ps.executeQuery();

            if(rs.next()) {

                CaLamViec ca = new CaLamViec();

                ca.setMaCa(rs.getString("maca"));
                ca.setMaNV(rs.getString("manv"));
                ca.setTenCa(rs.getString("tenca"));
                ca.setGioBatDau(rs.getTimestamp("giobatdau"));
                ca.setGioKetThuc(rs.getTimestamp("gioketthuc"));

                return ca;
            }

        } catch(Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    // Thêm ca làm việc
    public boolean insert(CaLamViec ca) {

        String sql =
            "INSERT INTO calamviec VALUES(?,?,?,?,?)";

        try(Connection con = DBConnection.getConnection();
            PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, ca.getMaCa());
            ps.setString(2, ca.getMaNV());
            ps.setString(3, ca.getTenCa());
            ps.setTimestamp(4, ca.getGioBatDau());
            ps.setTimestamp(5, ca.getGioKetThuc());

            return ps.executeUpdate() > 0;

        } catch(Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    // Cập nhật ca làm việc
    public boolean update(CaLamViec ca) {

        String sql =
            "UPDATE calamviec " +
            "SET manv=?, tenca=?, giobatdau=?, gioketthuc=? " +
            "WHERE maca=?";

        try(Connection con = DBConnection.getConnection();
            PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, ca.getMaNV());
            ps.setString(2, ca.getTenCa());
            ps.setTimestamp(3, ca.getGioBatDau());
            ps.setTimestamp(4, ca.getGioKetThuc());
            ps.setString(5, ca.getMaCa());

            return ps.executeUpdate() > 0;

        } catch(Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    // Xóa ca làm việc
    public boolean delete(String maCa) {

        String sql =
            "DELETE FROM calamviec WHERE maca=?";

        try(Connection con = DBConnection.getConnection();
            PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, maCa);

            return ps.executeUpdate() > 0;

        } catch(Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    // Tìm theo nhân viên
    public List<CaLamViec> findByNhanVien(String maNV) {

        List<CaLamViec> list = new ArrayList<>();

        String sql =
            "SELECT * FROM calamviec WHERE manv=?";

        try(Connection con = DBConnection.getConnection();
            PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, maNV);

            ResultSet rs = ps.executeQuery();

            while(rs.next()) {

                CaLamViec ca = new CaLamViec();

                ca.setMaCa(rs.getString("maca"));
                ca.setMaNV(rs.getString("manv"));
                ca.setTenCa(rs.getString("tenca"));
                ca.setGioBatDau(rs.getTimestamp("giobatdau"));
                ca.setGioKetThuc(rs.getTimestamp("gioketthuc"));

                list.add(ca);
            }

        } catch(Exception e) {
            e.printStackTrace();
        }

        return list;
    }
}