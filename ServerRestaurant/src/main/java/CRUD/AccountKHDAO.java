package CRUD;

import database.DBConnection;
import java.sql.*;
import java.util.*;
import model.AccountKH;

public class AccountKHDAO {

    // Lấy tất cả tài khoản khách hàng
    public List<AccountKH> getAll() {

        List<AccountKH> list = new ArrayList<>();
        String sql = "SELECT * FROM accountkh";

        try(Connection con = DBConnection.getConnection();
            PreparedStatement ps = con.prepareStatement(sql);
            ResultSet rs = ps.executeQuery()) {

            while(rs.next()) {

                AccountKH ac = new AccountKH();

                ac.setMaKH(rs.getString("makh"));
                ac.setTenDN(rs.getString("tendn"));
                ac.setPassword(rs.getString("pwd"));

                list.add(ac);
            }
        }
        catch(Exception e) {
            e.printStackTrace();
        }

        return list;
    }

    // Tìm theo mã khách hàng
    public AccountKH findID(String maKH) {

        String sql = "SELECT * FROM accountkh WHERE makh=?";

        try(Connection con = DBConnection.getConnection();
            PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, maKH);

            ResultSet rs = ps.executeQuery();

            if(rs.next()) {

                AccountKH ac = new AccountKH();

                ac.setMaKH(rs.getString("makh"));
                ac.setTenDN(rs.getString("tendn"));
                ac.setPassword(rs.getString("pwd"));

                return ac;
            }

        }
        catch(Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    // Thêm tài khoản khách hàng
    public boolean insert(AccountKH ac) {

        String sql =
            "INSERT INTO accountkh(makh,tendn,pwd) VALUES(?,?,?)";

        try(Connection con = DBConnection.getConnection();
            PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, ac.getMaKH());
            ps.setString(2, ac.getTenDN());
            ps.setString(3, ac.getPassword());

            return ps.executeUpdate() > 0;

        }
        catch(Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    // Cập nhật tài khoản
    public boolean update(AccountKH ac) {

        String sql =
            "UPDATE accountkh SET tendn=?, pwd=? WHERE makh=?";

        try(Connection con = DBConnection.getConnection();
            PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, ac.getTenDN());
            ps.setString(2, ac.getPassword());
            ps.setString(3, ac.getMaKH());

            return ps.executeUpdate() > 0;

        }
        catch(Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    // Xóa tài khoản
    public boolean delete(String maKH) {

        String sql =
            "DELETE FROM accountkh WHERE makh=?";

        try(Connection con = DBConnection.getConnection();
            PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, maKH);

            return ps.executeUpdate() > 0;

        }
        catch(Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    // Đăng nhập
    public AccountKH login(String username, String password) {

        String sql =
            "SELECT * FROM accountkh WHERE tendn=? AND pwd=?";

        try(Connection con = DBConnection.getConnection();
            PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, username);
            ps.setString(2, password);

            ResultSet rs = ps.executeQuery();

            if(rs.next()) {

                AccountKH ac = new AccountKH();

                ac.setMaKH(rs.getString("makh"));
                ac.setTenDN(rs.getString("tendn"));
                ac.setPassword(rs.getString("pwd"));

                return ac;
            }

        }
        catch(Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    // Đổi mật khẩu
    public boolean changePassword(String maKH,
                                  String newPassword) {

        String sql =
            "UPDATE accountkh SET pwd=? WHERE makh=?";

        try(Connection con = DBConnection.getConnection();
            PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, newPassword);
            ps.setString(2, maKH);

            return ps.executeUpdate() > 0;

        }
        catch(Exception e) {
            e.printStackTrace();
        }

        return false;
    }
    
    
    // Tìm theo tên đăng nhập
    public AccountKH findByUsername(String username) 
    {

        String sql = "SELECT * FROM accountkh WHERE tendn=?";

        try(Connection con = DBConnection.getConnection();
            PreparedStatement ps = con.prepareStatement(sql)) 
        {
            ps.setString(1, username);

            ResultSet rs = ps.executeQuery();

            if(rs.next()) {

                AccountKH ac = new AccountKH();

                ac.setMaKH(rs.getString("makh"));
                ac.setTenDN(rs.getString("tendn"));
                ac.setPassword(rs.getString("pwd"));

                return ac;
            }

        } catch(Exception e) {
            e.printStackTrace();
        }

        return null;
    }   
}