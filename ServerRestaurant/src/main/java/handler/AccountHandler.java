/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package handler;

import CRUD.AccountDAO;
import java.util.List;
import model.Account;

public class AccountHandler {

    private final AccountDAO accountDAO;

    public AccountHandler() 
    {
        accountDAO = new AccountDAO();
    }

    // Lấy tất cả tài khoản
    public List<Account> getAllAccounts() 
    {
        return accountDAO.getAll();
    }

    // Tìm theo mã nhân viên
    public Account findAccountByID(String maNV) 
    {

        if(maNV == null || maNV.trim().isEmpty()) 
        {
            return null;
        }

        return accountDAO.findID(maNV);
    }

    // Tìm theo tên đăng nhập
    public Account findByUsername(String username) 
    {

        if(username == null || username.trim().isEmpty()) 
        {
            return null;
        }

        return accountDAO.findByUsername(username);
    }

    // Thêm tài khoản
    public boolean addAccount(Account ac) 
    {

        if(ac == null) 
        {
            return false;
        }

        if(ac.getMaNV() == null || ac.getMaNV().trim().isEmpty()) 
        {
            return false;
        }

        if(ac.getTenDN() == null || ac.getTenDN().trim().isEmpty()) 
        {
            return false;
        }

        if(ac.getPassword() == null || ac.getPassword().trim().isEmpty()) 
        {
            return false;
        }

        // Kiểm tra quyền (1: Admin/Quản lý, 2: Thu ngân, 3: Nhân viên)
        if(ac.getQuyen() < 1 || ac.getQuyen() > 3)
        {
            return false;
        }

        // Kiểm tra trùng mã nhân viên
        if(accountDAO.findID(ac.getMaNV()) != null) 
        {
            return false;
        }

        // Kiểm tra trùng tên đăng nhập
        if(accountDAO.findByUsername(ac.getTenDN()) != null) 
        {
            return false;
        }

        return accountDAO.insert(ac);
    }

    // Cập nhật tài khoản
    public boolean updateAccount(Account ac) 
    {

        if(ac == null) 
        {
            return false;
        }

        if(ac.getMaNV() == null || ac.getMaNV().trim().isEmpty()) 
        {
            return false;
        }

        // Kiểm tra quyền (1: Admin/Quản lý, 2: Thu ngân, 3: Nhân viên)
        if(ac.getQuyen() < 1 || ac.getQuyen() > 3)
        {
            return false;
        }

        return accountDAO.update(ac);
    }

    // Xóa tài khoản
    public boolean deleteAccount(String maNV) {

        if(maNV == null || maNV.trim().isEmpty()) 
        {
            return false;
        }

        return accountDAO.delete(maNV);
    }

    // Đăng nhập
    public Account login(String username, String password) 
    {

        if(username == null ||
           username.trim().isEmpty()) {
            return null;
        }

        if(password == null ||
           password.trim().isEmpty()) {
            return null;
        }

        return accountDAO.login(username, password);
    }

    // Đổi mật khẩu
    public boolean changePassword(String maNV, String newPassword) 
    {

        if(maNV == null ||
           maNV.trim().isEmpty()) {
            return false;
        }

        if(newPassword == null ||
           newPassword.trim().isEmpty()) {
            return false;
        }

        return accountDAO.changePassword(maNV,newPassword);
    }
    
    
    
}