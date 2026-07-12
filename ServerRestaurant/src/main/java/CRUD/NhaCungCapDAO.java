package CRUD;

import database.DBConnection;
import java.sql.*;
import java.util.*;
import model.NhaCungCap;

public class NhaCungCapDAO {

    // Lấy tất cả nhà cung cấp
    public List<NhaCungCap> getAll() {

        List<NhaCungCap> list = new ArrayList<>();

        String sql = "SELECT * FROM nhacungcap";

        try(Connection con = DBConnection.getConnection();
            PreparedStatement ps = con.prepareStatement(sql);
            ResultSet rs = ps.executeQuery()) {

            while(rs.next()) {

                NhaCungCap ncc = new NhaCungCap();

                ncc.setMaNCC(rs.getString("mancc"));
                ncc.setTenNCC(rs.getString("tenncc"));
                ncc.setSdt(rs.getString("sdt"));

                list.add(ncc);
            }

        } catch(Exception e) {
            e.printStackTrace();
        }

        return list;
    }

    // Tìm theo mã
    public NhaCungCap findID(String maNCC) {

        String sql = "SELECT * FROM nhacungcap WHERE mancc=?";

        try(Connection con = DBConnection.getConnection();
            PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, maNCC);

            ResultSet rs = ps.executeQuery();

            if(rs.next()) {

                NhaCungCap ncc = new NhaCungCap();

                ncc.setMaNCC(rs.getString("mancc"));
                ncc.setTenNCC(rs.getString("tenncc"));
                ncc.setSdt(rs.getString("sdt"));

                return ncc;
            }

        } catch(Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    // Thêm
    public boolean insert(NhaCungCap ncc) {

        String sql =
                "INSERT INTO nhacungcap(mancc,tenncc,sdt) VALUES(?,?,?)";

        try(Connection con = DBConnection.getConnection();
            PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, ncc.getMaNCC());
            ps.setString(2, ncc.getTenNCC());
            ps.setString(3, ncc.getSdt());

            return ps.executeUpdate() > 0;

        } catch(Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    // Cập nhật
    public boolean update(NhaCungCap ncc) {

        String sql =
                "UPDATE nhacungcap SET tenncc=?, sdt=? WHERE mancc=?";

        try(Connection con = DBConnection.getConnection();
            PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, ncc.getTenNCC());
            ps.setString(2, ncc.getSdt());
            ps.setString(3, ncc.getMaNCC());

            return ps.executeUpdate() > 0;

        } catch(Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    // Xóa
    public boolean delete(String maNCC) {

        String sql =
                "DELETE FROM nhacungcap WHERE mancc=?";

        try(Connection con = DBConnection.getConnection();
            PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, maNCC);

            return ps.executeUpdate() > 0;

        } catch(Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    // Tìm theo tên
    public List<NhaCungCap> searchByName(String keyword) {

        List<NhaCungCap> list = new ArrayList<>();

        String sql =
                "SELECT * FROM nhacungcap WHERE tenncc LIKE ?";

        try(Connection con = DBConnection.getConnection();
            PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, "%" + keyword + "%");

            ResultSet rs = ps.executeQuery();

            while(rs.next()) {

                NhaCungCap ncc = new NhaCungCap();

                ncc.setMaNCC(rs.getString("mancc"));
                ncc.setTenNCC(rs.getString("tenncc"));
                ncc.setSdt(rs.getString("sdt"));

                list.add(ncc);
            }

        } catch(Exception e) {
            e.printStackTrace();
        }

        return list;
    }

    // Tìm theo số điện thoại
    public NhaCungCap findByPhone(String sdt) {

        String sql =
                "SELECT * FROM nhacungcap WHERE sdt=?";

        try(Connection con = DBConnection.getConnection();
            PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, sdt);

            ResultSet rs = ps.executeQuery();

            if(rs.next()) {

                NhaCungCap ncc = new NhaCungCap();

                ncc.setMaNCC(rs.getString("mancc"));
                ncc.setTenNCC(rs.getString("tenncc"));
                ncc.setSdt(rs.getString("sdt"));

                return ncc;
            }

        } catch(Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    // Kiểm tra mã đã tồn tại
    public boolean exists(String maNCC) {

        String sql =
                "SELECT 1 FROM nhacungcap WHERE mancc=?";

        try(Connection con = DBConnection.getConnection();
            PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, maNCC);

            ResultSet rs = ps.executeQuery();

            return rs.next();

        } catch(Exception e) {
            e.printStackTrace();
        }

        return false;
    }

}