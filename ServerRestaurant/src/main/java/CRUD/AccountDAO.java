package CRUD;

import database.DBConnection;
import java.sql.*;
import java.util.*;
import model.Account;

public class AccountDAO {

    // Lấy tất cả tài khoản
    public List<Account> getAll() {
        List<Account> list = new ArrayList<>();
        String sql = "SELECT * FROM account";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Account ac = new Account();
                ac.setMaNV(rs.getString("manv"));
                ac.setTenDN(rs.getString("tendn"));
                ac.setPassword(rs.getString("pwd"));
                ac.setQuyen(rs.getInt("quyen"));

                list.add(ac);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return list;
    }

    // Tìm theo mã nhân viên
    public Account findID(String maNV) {
        String sql = "SELECT * FROM account WHERE manv=?";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, maNV);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Account ac = new Account();
                    ac.setMaNV(rs.getString("manv"));
                    ac.setTenDN(rs.getString("tendn"));
                    ac.setPassword(rs.getString("pwd"));
                    ac.setQuyen(rs.getInt("quyen"));
                    return ac;
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    // Thêm tài khoản
    public boolean insert(Account ac) {
        String sql = "INSERT INTO account(manv,tendn,pwd,quyen) VALUES(?,?,?,?)";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, ac.getMaNV());
            ps.setString(2, ac.getTenDN());
            ps.setString(3, ac.getPassword());
            ps.setInt(4, ac.getQuyen());

            return ps.executeUpdate() > 0;

        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    // Cập nhật tài khoản
    public boolean update(Account ac) {
        String sql = "UPDATE account SET tendn=?, pwd=?, quyen=? WHERE manv=?";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, ac.getTenDN());
            ps.setString(2, ac.getPassword());
            ps.setInt(3, ac.getQuyen());
            ps.setString(4, ac.getMaNV());

            return ps.executeUpdate() > 0;

        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    // Xóa tài khoản
    public boolean delete(String maNV) {
        String sql = "DELETE FROM account WHERE manv=?";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, maNV);

            return ps.executeUpdate() > 0;

        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    // Đăng nhập
    public Account login(String username, String password) {
        String sql = "SELECT * FROM account WHERE tendn=? AND pwd=?";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, username);
            ps.setString(2, password);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Account ac = new Account();
                    ac.setMaNV(rs.getString("manv"));
                    ac.setTenDN(rs.getString("tendn"));
                    ac.setPassword(rs.getString("pwd"));
                    ac.setQuyen(rs.getInt("quyen"));

                    // Log kiểm tra dữ liệu đọc được tại Server console
                    System.out.println("[SERVER - DAO LOG] Đăng nhập thành công tài khoản: " 
                            + ac.getTenDN() + " - Quyền tải từ DB: " + ac.getQuyen());
                    return ac;
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    // Đổi mật khẩu
    public boolean changePassword(String maNV, String newPassword) {
        String sql = "UPDATE account SET pwd=? WHERE manv=?";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, newPassword);
            ps.setString(2, maNV);

            return ps.executeUpdate() > 0;

        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    // Tìm theo tên đăng nhập
    public Account findByUsername(String username) {
        String sql = "SELECT * FROM account WHERE tendn=?";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, username);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Account ac = new Account();
                    ac.setMaNV(rs.getString("manv"));
                    ac.setTenDN(rs.getString("tendn"));
                    ac.setPassword(rs.getString("pwd"));
                    ac.setQuyen(rs.getInt("quyen"));
                    return ac;
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }
}