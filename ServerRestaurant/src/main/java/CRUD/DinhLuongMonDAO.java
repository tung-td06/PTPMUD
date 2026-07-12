package CRUD;

import database.DBConnection;
import java.sql.*;
import java.util.*;
import model.DinhLuongMon;

public class DinhLuongMonDAO {

    // Lấy tất cả định lượng món
    public List<DinhLuongMon> getAll() {

        List<DinhLuongMon> list = new ArrayList<>();
        String sql = "SELECT * FROM dinhluongmon";

        try(Connection con = DBConnection.getConnection();
            PreparedStatement ps = con.prepareStatement(sql);
            ResultSet rs = ps.executeQuery()) {

            while(rs.next()) {

                DinhLuongMon dl = new DinhLuongMon();

                dl.setMaMon(rs.getString("mamon"));
                dl.setMaNL(rs.getString("manl"));
                dl.setDinhLuong(rs.getInt("dinhluong"));
                dl.setDonViTinh(rs.getString("donvitinh"));

                list.add(dl);
            }

        } catch(Exception e) {
            e.printStackTrace();
        }

        return list;
    }

    // Tìm theo khóa chính kép
    public DinhLuongMon findID(String maMon,
                               String maNL) {

        String sql =
            "SELECT * FROM dinhluongmon WHERE mamon=? AND manl=?";

        try(Connection con = DBConnection.getConnection();
            PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, maMon);
            ps.setString(2, maNL);

            ResultSet rs = ps.executeQuery();

            if(rs.next()) {

                DinhLuongMon dl = new DinhLuongMon();

                dl.setMaMon(rs.getString("mamon"));
                dl.setMaNL(rs.getString("manl"));
                dl.setDinhLuong(rs.getInt("dinhluong"));
                dl.setDonViTinh(rs.getString("donvitinh"));

                return dl;
            }

        } catch(Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    // Thêm định lượng
    public boolean insert(DinhLuongMon dl) {

        String sql = "INSERT INTO dinhluongmon (mamon, manl, dinhluong, donvitinh) VALUES (?, ?, ?, ?)";

        try(Connection con = DBConnection.getConnection();
            PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, dl.getMaMon());
            ps.setString(2, dl.getMaNL());
            ps.setInt(3, dl.getDinhLuong());
            ps.setString(4, dl.getDonViTinh());

            return ps.executeUpdate() > 0;

        } catch(Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    // Cập nhật định lượng
    public boolean update(DinhLuongMon dl) {

        String sql =
            "UPDATE dinhluongmon SET dinhluong=?, donvitinh=? WHERE mamon=? AND manl=?";

        try(Connection con = DBConnection.getConnection();
            PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, dl.getDinhLuong());
            ps.setString(2, dl.getDonViTinh());
            ps.setString(3, dl.getMaMon());
            ps.setString(4, dl.getMaNL());

            return ps.executeUpdate() > 0;

        } catch(Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    // Xóa định lượng
    public boolean delete(String maMon,
                          String maNL) {

        String sql =
            "DELETE FROM dinhluongmon WHERE mamon=? AND manl=?";

        try(Connection con = DBConnection.getConnection();
            PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, maMon);
            ps.setString(2, maNL);

            return ps.executeUpdate() > 0;

        } catch(Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    // Lấy danh sách nguyên liệu của món ăn
    public List<DinhLuongMon> getByMonAn(String maMon) {

        List<DinhLuongMon> list = new ArrayList<>();

        String sql =
            "SELECT * FROM dinhluongmon WHERE mamon=?";

        try(Connection con = DBConnection.getConnection();
            PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, maMon);

            ResultSet rs = ps.executeQuery();

            while(rs.next()) {

                DinhLuongMon dl = new DinhLuongMon();

                dl.setMaMon(rs.getString("mamon"));
                dl.setMaNL(rs.getString("manl"));
                dl.setDinhLuong(rs.getInt("dinhluong"));
                dl.setDonViTinh(rs.getString("donvitinh"));

                list.add(dl);
            }

        } catch(Exception e) {
            e.printStackTrace();
        }

        return list;
    }
}