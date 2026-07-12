package CRUD;

import database.DBConnection;
import java.sql.*;
import java.util.*;
import model.LoaiMon;

public class LoaiMonDAO {

    public List<LoaiMon> getAll() {

        List<LoaiMon> list = new ArrayList<>();

        String sql = "SELECT * FROM loaimon";

        try(Connection con = DBConnection.getConnection();
            PreparedStatement ps = con.prepareStatement(sql);
            ResultSet rs = ps.executeQuery()) {

            while(rs.next()) {

                LoaiMon lm = new LoaiMon();

                lm.setMaLoai(rs.getString("maloai"));
                lm.setTenLoai(rs.getString("tenloai"));

                list.add(lm);
            }

        } catch(Exception e) {
            e.printStackTrace();
        }

        return list;
    }

    public LoaiMon findID(String maLoai) {

        String sql =
            "SELECT * FROM loaimon WHERE maloai=?";

        try(Connection con = DBConnection.getConnection();
            PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, maLoai);

            ResultSet rs = ps.executeQuery();

            if(rs.next()) {

                LoaiMon lm = new LoaiMon();

                lm.setMaLoai(rs.getString("maloai"));
                lm.setTenLoai(rs.getString("tenloai"));

                return lm;
            }

        } catch(Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public LoaiMon findByName(String tenLoai) {

        String sql =
            "SELECT * FROM loaimon WHERE tenloai=?";

        try(Connection con = DBConnection.getConnection();
            PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, tenLoai);

            ResultSet rs = ps.executeQuery();

            if(rs.next()) {

                LoaiMon lm = new LoaiMon();

                lm.setMaLoai(rs.getString("maloai"));
                lm.setTenLoai(rs.getString("tenloai"));

                return lm;
            }

        } catch(Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public boolean insert(LoaiMon lm) {

        String sql =
            "INSERT INTO loaimon VALUES(?,?)";

        try(Connection con = DBConnection.getConnection();
            PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, lm.getMaLoai());
            ps.setString(2, lm.getTenLoai());

            return ps.executeUpdate() > 0;

        } catch(Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    public boolean update(LoaiMon lm) {

        String sql =
            "UPDATE loaimon SET tenloai=? WHERE maloai=?";

        try(Connection con = DBConnection.getConnection();
            PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, lm.getTenLoai());
            ps.setString(2, lm.getMaLoai());

            return ps.executeUpdate() > 0;

        } catch(Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    public boolean delete(String maLoai) {

        String sql =
            "DELETE FROM loaimon WHERE maloai=?";

        try(Connection con = DBConnection.getConnection();
            PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, maLoai);

            return ps.executeUpdate() > 0;

        } catch(Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    public List<LoaiMon> searchByName(String keyword) {

        List<LoaiMon> list = new ArrayList<>();

        String sql =
            "SELECT * FROM loaimon WHERE tenloai LIKE ?";

        try(Connection con = DBConnection.getConnection();
            PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, "%" + keyword + "%");

            ResultSet rs = ps.executeQuery();

            while(rs.next()) {

                LoaiMon lm = new LoaiMon();

                lm.setMaLoai(rs.getString("maloai"));
                lm.setTenLoai(rs.getString("tenloai"));

                list.add(lm);
            }

        } catch(Exception e) {
            e.printStackTrace();
        }

        return list;
    }

    public boolean exists(String maLoai) {
        return findID(maLoai) != null;
    }

    public int countMonAnByLoai(String maLoai) {

        String sql =
            "SELECT COUNT(*) tong FROM monan WHERE maloai=?";

        try(Connection con = DBConnection.getConnection();
            PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, maLoai);

            ResultSet rs = ps.executeQuery();

            if(rs.next()) {
                return rs.getInt("tong");
            }

        } catch(Exception e) {
            e.printStackTrace();
        }

        return 0;
    }
}