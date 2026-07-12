/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package model;

/**
 *
 * @author Lenovo
 */

import java.io.Serializable;

public class Account implements Serializable {

    private static final long serialVersionUID = 1L;
    private String maNV;
    private String tenDN;
    private String password;
    private int quyen;

    public Account() {
    }

    public Account(String maNV, String tenDN, String password, int quyen) {
        this.maNV = maNV;
        this.tenDN = tenDN;
        this.password = password;
        this.quyen=quyen;
    }

    public String getMaNV() {
        return maNV;
    }

    public void setMaNV(String maNV) {
        this.maNV = maNV;
    }

    public String getTenDN() {
        return tenDN;
    }

    public void setTenDN(String tenDN) {
        this.tenDN = tenDN;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @Override
    public String toString() {
        return tenDN;
    }

    public void setUsername(String username) {
        this.tenDN = username;
    }

    public int getQuyen() {
        return quyen;
    }

    public void setQuyen(int quyen) {
        this.quyen = quyen;
    }
    
    
}
