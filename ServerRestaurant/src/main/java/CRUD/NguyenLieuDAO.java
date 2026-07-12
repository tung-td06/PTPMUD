package CRUD;

import database.DBConnection;
import java.sql.*;
import java.util.*;
import model.NguyenLieu;

public class NguyenLieuDAO {

    // Lấy tất cả nguyên liệu
    public List<NguyenLieu> getAll() {

        List<NguyenLieu> list = new ArrayList<>();
        String sql = "SELECT * FROM nguyenlieu";

        try(Connection con = DBConnection.getConnection();
            PreparedStatement ps = con.prepareStatement(sql);
            ResultSet rs = ps.executeQuery()) {

            while(rs.next()) {

                NguyenLieu nl = new NguyenLieu();

                nl.setMaNL(rs.getString("manl"));
                nl.setTenNL(rs.getString("tennl"));
                nl.setDonViTinh(rs.getString("donvitinh"));
                nl.setSoLuongKho(rs.getInt("soluongkho"));

                list.add(nl);
            }

        } catch(Exception e) {
            e.printStackTrace();
        }

        return list;
    }

    // Tìm theo mã nguyên liệu
    public NguyenLieu findID(String maNL) {

        String sql = "SELECT * FROM nguyenlieu WHERE manl=?";

        try(Connection con = DBConnection.getConnection();
            PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, maNL);

            ResultSet rs = ps.executeQuery();

            if(rs.next()) {

                NguyenLieu nl = new NguyenLieu();

                nl.setMaNL(rs.getString("manl"));
                nl.setTenNL(rs.getString("tennl"));
                nl.setDonViTinh(rs.getString("donvitinh"));
                nl.setSoLuongKho(rs.getInt("soluongkho"));

                return nl;
            }

        } catch(Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    // Thêm nguyên liệu
    public boolean insert(NguyenLieu nl) {

        String sql = "INSERT INTO nguyenlieu VALUES(?,?,?,?)";

        try(Connection con = DBConnection.getConnection();
            PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, nl.getMaNL());
            ps.setString(2, nl.getTenNL());
            ps.setString(3, nl.getDonViTinh());
            ps.setInt(4, nl.getSoLuongKho());

            return ps.executeUpdate() > 0;

        } catch(Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    // Cập nhật nguyên liệu
    public boolean update(NguyenLieu nl) {

        String sql =
            "UPDATE nguyenlieu SET tennl=?, donvitinh=?, soluongkho=? WHERE manl=?";

        try(Connection con = DBConnection.getConnection();
            PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, nl.getTenNL());
            ps.setString(2, nl.getDonViTinh());
            ps.setInt(3, nl.getSoLuongKho());
            ps.setString(4, nl.getMaNL());

            return ps.executeUpdate() > 0;

        } catch(Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    // Xóa nguyên liệu
    public boolean delete(String maNL) {

        String sql = "DELETE FROM nguyenlieu WHERE manl=?";

        try(Connection con = DBConnection.getConnection();
            PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, maNL);

            return ps.executeUpdate() > 0;

        } catch(Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    // Tìm theo tên nguyên liệu
    public List<NguyenLieu> searchByName(String keyword) {

        List<NguyenLieu> list = new ArrayList<>();

        String sql =
            "SELECT * FROM nguyenlieu WHERE tennl LIKE ?";

        try(Connection con = DBConnection.getConnection();
            PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, "%" + keyword + "%");

            ResultSet rs = ps.executeQuery();

            while(rs.next()) {

                NguyenLieu nl = new NguyenLieu();

                nl.setMaNL(rs.getString("manl"));
                nl.setTenNL(rs.getString("tennl"));
                nl.setDonViTinh(rs.getString("donvitinh"));
                nl.setSoLuongKho(rs.getInt("soluongkho"));

                list.add(nl);
            }

        } catch(Exception e) {
            e.printStackTrace();
        }

        return list;
    }

    // Lấy danh sách nguyên liệu sắp hết
    public List<NguyenLieu> getNguyenLieuSapHet() {

        List<NguyenLieu> list = new ArrayList<>();

        String sql =
            "SELECT * FROM nguyenlieu WHERE soluongkho < 20";

        try(Connection con = DBConnection.getConnection();
            PreparedStatement ps = con.prepareStatement(sql);
            ResultSet rs = ps.executeQuery()) {

            while(rs.next()) {

                NguyenLieu nl = new NguyenLieu();

                nl.setMaNL(rs.getString("manl"));
                nl.setTenNL(rs.getString("tennl"));
                nl.setDonViTinh(rs.getString("donvitinh"));
                nl.setSoLuongKho(rs.getInt("soluongkho"));

                list.add(nl);
            }

        } catch(Exception e) {
            e.printStackTrace();
        }

        return list;
    }
    
    public NguyenLieu findByName(String tenNL) {

    String sql =
        "SELECT * FROM nguyenlieu WHERE tennl=?";

    try(Connection con = DBConnection.getConnection();
        PreparedStatement ps = con.prepareStatement(sql)) {

        ps.setString(1, tenNL);

        ResultSet rs = ps.executeQuery();

        if(rs.next()) {

            NguyenLieu nl = new NguyenLieu();

            nl.setMaNL(rs.getString("manl"));
            nl.setTenNL(rs.getString("tennl"));
            nl.setDonViTinh(rs.getString("donvitinh"));
            nl.setSoLuongKho(rs.getInt("soluongkho"));

            return nl;
        }

    } catch(Exception e) {
        e.printStackTrace();
    }

    return null;
}
    
    public boolean exists(String maNL) {

    String sql =
        "SELECT COUNT(*) FROM nguyenlieu WHERE manl=?";

    try(Connection con = DBConnection.getConnection();
        PreparedStatement ps = con.prepareStatement(sql)) {

        ps.setString(1, maNL);

        ResultSet rs = ps.executeQuery();

        if(rs.next()) {
            return rs.getInt(1) > 0;
        }

    } catch(Exception e) {
        e.printStackTrace();
    }

    return false;
}
    
    public boolean updateSoLuongKho(String maNL,
                                int soLuongKho) {

    String sql =
        "UPDATE nguyenlieu SET soluongkho=? WHERE manl=?";

    try(Connection con = DBConnection.getConnection();
        PreparedStatement ps = con.prepareStatement(sql)) {

        ps.setInt(1, soLuongKho);
        ps.setString(2, maNL);

        return ps.executeUpdate() > 0;

    } catch(Exception e) {
        e.printStackTrace();
    }

    return false;
}
    
    public boolean tangSoLuongKho(String maNL, int soLuong) {

    String sql =
        "UPDATE nguyenlieu "
      + "SET soluongkho = soluongkho + ? "
      + "WHERE manl=?";

    try(Connection con = DBConnection.getConnection();
        PreparedStatement ps = con.prepareStatement(sql)) {

        ps.setInt(1, soLuong);
        ps.setString(2, maNL);

        return ps.executeUpdate() > 0;

    } catch(Exception e) {
        e.printStackTrace();
    }

    return false;
}
    
    public boolean giamSoLuongKho(String maNL,  int soLuong) 
    {

        String sql =
            "UPDATE nguyenlieu "
          + "SET soluongkho = soluongkho - ? "
          + "WHERE manl=?";

        try(Connection con = DBConnection.getConnection();
            PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, soLuong);
            ps.setString(2, maNL);

            return ps.executeUpdate() > 0;

        } catch(Exception e) {
            e.printStackTrace();
        }

        return false;
    }
    
    public int countDinhLuong(String maNL) 
    {

        String sql =
            "SELECT COUNT(*) FROM dinhluongmon WHERE manl=?";

        try(Connection con = DBConnection.getConnection();
            PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, maNL);

            ResultSet rs = ps.executeQuery();

            if(rs.next()) {
                return rs.getInt(1);
            }

        } catch(Exception e) {
            e.printStackTrace();
        }

        return 0;
    }
    
    
    public int countChiTietNhap(String maNL) 
    {

        String sql =
            "SELECT COUNT(*) FROM chitietnh WHERE manl=?";

        try(Connection con = DBConnection.getConnection();
            PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, maNL);

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