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

public class NguyenLieu implements Serializable {

    private static final long serialVersionUID = 1L;
    private String maNL;
    private String tenNL;
    private String donViTinh;
    private int soLuongKho;

    public NguyenLieu() {
    }

    public NguyenLieu(String maNL, String tenNL,
                      String donViTinh, int soLuongKho) {

        this.maNL = maNL;
        this.tenNL = tenNL;
        this.donViTinh = donViTinh;
        this.soLuongKho = soLuongKho;
    }

    public String getMaNL() {
        return maNL;
    }

    public void setMaNL(String maNL) {
        this.maNL = maNL;
    }

    public String getTenNL() {
        return tenNL;
    }

    public void setTenNL(String tenNL) {
        this.tenNL = tenNL;
    }

    public String getDonViTinh() {
        return donViTinh;
    }

    public void setDonViTinh(String donViTinh) {
        this.donViTinh = donViTinh;
    }

    public int getSoLuongKho() {
        return soLuongKho;
    }

    public void setSoLuongKho(int soLuongKho) {
        this.soLuongKho = soLuongKho;
    }

    @Override
    public String toString() {
        return tenNL;
    }
}