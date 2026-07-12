package CRUD;

import database.DBConnection;
import java.sql.*;
import java.util.*;
import model.MonAn;
import java.math.BigDecimal;

public class MonAnDAO {

    // Lấy tất cả món ăn
    public List<MonAn> getAll() {

        List<MonAn> list = new ArrayList<>();

        String sql = "SELECT * FROM monan";

        try(Connection con = DBConnection.getConnection();
            PreparedStatement ps = con.prepareStatement(sql);
            ResultSet rs = ps.executeQuery()) {

            while(rs.next()) {

                MonAn ma = new MonAn();

                ma.setMaMon(rs.getString("mamon"));
                ma.setMaLoai(rs.getString("maloai"));
                ma.setTenMon(rs.getString("tenmon"));
                ma.setDonGia(rs.getBigDecimal("dongia"));
                ma.setAnh(rs.getString("anh"));
                ma.setTrangThai(rs.getString("trangthai"));

                list.add(ma);
            }

        } catch(Exception e) {
            e.printStackTrace();
        }

        return list;
    }

    // Tìm theo mã món
    public MonAn findID(String maMon) {

        String sql = "SELECT * FROM monan WHERE mamon=?";

        try(Connection con = DBConnection.getConnection();
            PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, maMon);

            ResultSet rs = ps.executeQuery();

            if(rs.next()) {

                MonAn ma = new MonAn();

                ma.setMaMon(rs.getString("mamon"));
                ma.setMaLoai(rs.getString("maloai"));
                ma.setTenMon(rs.getString("tenmon"));
                ma.setDonGia(rs.getBigDecimal("dongia"));
                ma.setAnh(rs.getString("anh"));
                ma.setTrangThai(rs.getString("trangthai"));

                return ma;
            }

        } catch(Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    // Thêm món ăn
    public boolean insert(MonAn ma) {

        String sql =
            "INSERT INTO monan(mamon,maloai,tenmon,dongia,anh,trangthai) VALUES(?,?,?,?,?,?)";

        try(Connection con = DBConnection.getConnection();
            PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, ma.getMaMon());
            ps.setString(2, ma.getMaLoai());
            ps.setString(3, ma.getTenMon());
            ps.setBigDecimal(4, ma.getDonGia());
            ps.setString(5, ma.getAnh());
            ps.setString(6, ma.isTrangThai());

            return ps.executeUpdate() > 0;

        } catch(Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    // Cập nhật món ăn
    public boolean update(MonAn ma) {

        String sql =
            "UPDATE monan SET maloai=?, tenmon=?, dongia=?, anh=?, trangthai=? WHERE mamon=?";

        try(Connection con = DBConnection.getConnection();
            PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, ma.getMaLoai());
            ps.setString(2, ma.getTenMon());
            ps.setBigDecimal(3, ma.getDonGia());
            ps.setString(4, ma.getAnh());
            ps.setString(5, ma.isTrangThai());
            ps.setString(6, ma.getMaMon());

            return ps.executeUpdate() > 0;

        } catch(Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    // Xóa món ăn
    public boolean delete(String maMon) {

        String sql = "DELETE FROM monan WHERE mamon=?";

        try(Connection con = DBConnection.getConnection();
            PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, maMon);

            return ps.executeUpdate() > 0;

        } catch(Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    // Tìm theo tên món
    public List<MonAn> searchByName(String keyword) {

        List<MonAn> list = new ArrayList<>();

        String sql =
            "SELECT * FROM monan WHERE tenmon LIKE ?";

        try(Connection con = DBConnection.getConnection();
            PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, "%" + keyword + "%");

            try(ResultSet rs = ps.executeQuery()) {

                while(rs.next()) {

                    MonAn ma = new MonAn();

                    ma.setMaMon(rs.getString("mamon"));
                    ma.setMaLoai(rs.getString("maloai"));
                    ma.setTenMon(rs.getString("tenmon"));
                    ma.setDonGia(rs.getBigDecimal("dongia"));
                    ma.setAnh(rs.getString("anh"));
                    ma.setTrangThai(rs.getString("trangthai"));

                    list.add(ma);
                }
            }

        } catch(Exception e) {
            e.printStackTrace();
        }

        return list;
    }

    // Lấy món theo loại
    public List<MonAn> getByLoai(String maLoai) {

        List<MonAn> list = new ArrayList<>();

        String sql =
            "SELECT * FROM monan WHERE maloai=?";

        try(Connection con = DBConnection.getConnection();
            PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, maLoai);

            ResultSet rs = ps.executeQuery();

            while(rs.next()) {

                MonAn ma = new MonAn();

                ma.setMaMon(rs.getString("mamon"));
                ma.setMaLoai(rs.getString("maloai"));
                ma.setTenMon(rs.getString("tenmon"));
                ma.setDonGia(rs.getBigDecimal("dongia"));
                ma.setAnh(rs.getString("anh"));
                ma.setTrangThai(rs.getString("trangthai"));

                list.add(ma);
            }

        } catch(Exception e) {
            e.printStackTrace();
        }

        return list;
    }

    // Lấy các món đang bán
    public List<MonAn> getMonDangBan() {

        List<MonAn> list = new ArrayList<>();

        String sql =
            "SELECT * FROM monan WHERE trangthai=1";

        try(Connection con = DBConnection.getConnection();
            PreparedStatement ps = con.prepareStatement(sql);
            ResultSet rs = ps.executeQuery()) {

            while(rs.next()) {

                MonAn ma = new MonAn();

                ma.setMaMon(rs.getString("mamon"));
                ma.setMaLoai(rs.getString("maloai"));
                ma.setTenMon(rs.getString("tenmon"));
                ma.setDonGia(rs.getBigDecimal("dongia"));
                ma.setAnh(rs.getString("anh"));
                ma.setTrangThai(rs.getString("trangthai"));

                list.add(ma);
            }

        } catch(Exception e) {
            e.printStackTrace();
        }

        return list;
    }
        
    // Tìm chính xác theo tên món
    public MonAn findByName(String tenMon) {

    String sql = "SELECT * FROM monan WHERE tenmon=?";

    try(Connection con = DBConnection.getConnection();
        PreparedStatement ps = con.prepareStatement(sql)) {

        ps.setString(1, tenMon);

        ResultSet rs = ps.executeQuery();

        if(rs.next()) {

            MonAn ma = new MonAn();

            ma.setMaMon(rs.getString("mamon"));
            ma.setMaLoai(rs.getString("maloai"));
            ma.setTenMon(rs.getString("tenmon"));
            ma.setDonGia(rs.getBigDecimal("dongia"));
            ma.setAnh(rs.getString("anh"));
            ma.setTrangThai(rs.getString("trangthai"));

            return ma;
        }

    } catch(Exception e) {
        e.printStackTrace();
    }

    return null;
    }

    // Cập nhật trạng thái món
    public boolean updateTrangThai(String maMon,
                               boolean trangThai) {

    String sql =
        "UPDATE monan SET trangthai=? WHERE mamon=?";

    try(Connection con = DBConnection.getConnection();
        PreparedStatement ps = con.prepareStatement(sql)) {

        ps.setBoolean(1, trangThai);
        ps.setString(2, maMon);

        return ps.executeUpdate() > 0;

    } catch(Exception e) {
        e.printStackTrace();
    }

    return false;
    }

    // Cập nhật giá
    public boolean updateGia(String maMon,  BigDecimal donGia) {

    String sql =
        "UPDATE monan SET dongia=? WHERE mamon=?";

    try(Connection con = DBConnection.getConnection();
        PreparedStatement ps = con.prepareStatement(sql)) {

        ps.setBigDecimal(1, donGia);
        ps.setString(2, maMon);

        return ps.executeUpdate() > 0;

    } catch(Exception e) {
        e.printStackTrace();
    }

    return false;
    }

    // Danh sách món ngừng bán
    public List<MonAn> getMonNgungBan() {

    List<MonAn> list = new ArrayList<>();

    String sql =
        "SELECT * FROM monan WHERE trangthai=0";

    try(Connection con = DBConnection.getConnection();
        PreparedStatement ps = con.prepareStatement(sql);
        ResultSet rs = ps.executeQuery()) {

        while(rs.next()) {

            MonAn ma = new MonAn();

            ma.setMaMon(rs.getString("mamon"));
            ma.setMaLoai(rs.getString("maloai"));
            ma.setTenMon(rs.getString("tenmon"));
            ma.setDonGia(rs.getBigDecimal("dongia"));
            ma.setAnh(rs.getString("anh"));
            ma.setTrangThai(rs.getString("trangthai"));

            list.add(ma);
        }

    } catch(Exception e) {
        e.printStackTrace();
    }

    return list;
    }

    // Kiểm tra mã món tồn tại
    public boolean exists(String maMon) {

    String sql =
        "SELECT COUNT(*) FROM monan WHERE mamon=?";

    try(Connection con = DBConnection.getConnection();
        PreparedStatement ps = con.prepareStatement(sql)) {

        ps.setString(1, maMon);

        ResultSet rs = ps.executeQuery();

        if(rs.next()) {
            return rs.getInt(1) > 0;
        }

    } catch(Exception e) {
        e.printStackTrace();
    }

    return false;
    }

    // Đếm số hóa đơn chứa món
    public int countChiTietHD(String maMon) {

    String sql =
        "SELECT COUNT(*) FROM chitiethd WHERE mamon=?";

    try(Connection con = DBConnection.getConnection();
        PreparedStatement ps = con.prepareStatement(sql)) {

        ps.setString(1, maMon);

        ResultSet rs = ps.executeQuery();

        if(rs.next()) {
            return rs.getInt(1);
        }

    } catch(Exception e) {
        e.printStackTrace();
    }

    return 0;
    }

    // Đếm số định lượng của món
    public int countDinhLuong(String maMon) {

    String sql =
        "SELECT COUNT(*) FROM dinhluongmon WHERE mamon=?";

    try(Connection con = DBConnection.getConnection();
        PreparedStatement ps = con.prepareStatement(sql)) {

        ps.setString(1, maMon);

        ResultSet rs = ps.executeQuery();

        if(rs.next()) {
            return rs.getInt(1);
        }

    } catch(Exception e) {
        e.printStackTrace();
    }

    return 0;
    }
}