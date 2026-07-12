package handler;

import CRUD.AccountKHDAO;
import java.util.List;
import model.AccountKH;

public class AccountKHHandler {

    private final AccountKHDAO accountKHDAO;

    public AccountKHHandler() {
        accountKHDAO = new AccountKHDAO();
    }

    // Lấy tất cả tài khoản khách hàng
    public List<AccountKH> getAllAccounts() {
        return accountKHDAO.getAll();
    }

    // Tìm theo mã khách hàng
    public AccountKH findAccountByID(String maKH) {

        if(maKH == null || maKH.trim().isEmpty()) {
            return null;
        }

        return accountKHDAO.findID(maKH);
    }

    // Tìm theo tên đăng nhập
    public AccountKH findByUsername(String username) {

        if(username == null || username.trim().isEmpty()) {
            return null;
        }

        return accountKHDAO.findByUsername(username);
    }

    // Thêm tài khoản khách hàng
    public boolean addAccount(AccountKH ac) {

        if(ac == null) {
            return false;
        }

        if(ac.getMaKH() == null ||
           ac.getMaKH().trim().isEmpty()) {
            return false;
        }

        if(ac.getTenDN() == null ||
           ac.getTenDN().trim().isEmpty()) {
            return false;
        }

        if(ac.getPassword() == null ||
           ac.getPassword().trim().isEmpty()) {
            return false;
        }

        // Kiểm tra mã KH tồn tại
        if(accountKHDAO.findID(ac.getMaKH()) != null) {
            return false;
        }

        // Kiểm tra username tồn tại
        if(accountKHDAO.findByUsername(ac.getTenDN()) != null) {
            return false;
        }

        return accountKHDAO.insert(ac);
    }

    // Cập nhật tài khoản khách hàng
    public boolean updateAccount(AccountKH ac) {

        if(ac == null) {
            return false;
        }

        if(ac.getMaKH() == null ||
           ac.getMaKH().trim().isEmpty()) {
            return false;
        }

        return accountKHDAO.update(ac);
    }

    // Xóa tài khoản khách hàng
    public boolean deleteAccount(String maKH) {

        if(maKH == null || maKH.trim().isEmpty()) {
            return false;
        }

        return accountKHDAO.delete(maKH);
    }

    // Đăng nhập
    public AccountKH login(String username,
                           String password) {

        if(username == null ||
           username.trim().isEmpty()) {
            return null;
        }

        if(password == null ||
           password.trim().isEmpty()) {
            return null;
        }

        return accountKHDAO.login(username, password);
    }

    // Đổi mật khẩu
    public boolean changePassword(String maKH,
                                  String newPassword) {

        if(maKH == null ||
           maKH.trim().isEmpty()) {
            return false;
        }

        if(newPassword == null ||
           newPassword.trim().isEmpty()) {
            return false;
        }

        return accountKHDAO.changePassword(
                maKH,
                newPassword
        );
    }
}