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

public class DinhLuongMon implements Serializable {

    private static final long serialVersionUID = 1L;
    private String maMon;
    private String maNL;
    private int dinhLuong;
    private String donViTinh;

    public DinhLuongMon() {
    }

    public DinhLuongMon(String maMon, String maNL, int dinhLuong, String donViTinh) {
        this.maMon = maMon;
        this.maNL = maNL;
        this.dinhLuong = dinhLuong;
        this.donViTinh = donViTinh;
    }

    public String getMaMon() {
        return maMon;
    }

    public void setMaMon(String maMon) {
        this.maMon = maMon;
    }

    public String getMaNL() {
        return maNL;
    }

    public void setMaNL(String maNL) {
        this.maNL = maNL;
    }

    public int getDinhLuong() {
        return dinhLuong;
    }

    public void setDinhLuong(int dinhLuong) {
        this.dinhLuong = dinhLuong;
    }

    public String getDonViTinh() {
        return donViTinh;
    }

    public void setDonViTinh(String donViTinh) {
        this.donViTinh = donViTinh;
    }

    @Override
    public String toString() {
        return maMon + " - " + maNL;
    }
}
