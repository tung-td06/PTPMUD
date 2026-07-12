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

public class AccountKH implements Serializable {

    private static final long serialVersionUID = 1L;
    private String maKH;
    private String tenDN;
    private String password;

    public AccountKH() {
    }

    public AccountKH(String maKH, String tenDN, String password) {
        this.maKH = maKH;
        this.tenDN = tenDN;
        this.password = password;
    }

    public String getMaKH() {
        return maKH;
    }

    public void setMaKH(String maKH) {
        this.maKH = maKH;
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
}