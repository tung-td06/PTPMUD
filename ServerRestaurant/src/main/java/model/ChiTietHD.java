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
import java.math.BigDecimal;

public class ChiTietHD implements Serializable {

    private static final long serialVersionUID = 1L;
    private String maHD;
    private String maMon;

    private int soLuong;

    private BigDecimal donGia;
    private BigDecimal thanhTien;

    public ChiTietHD() {
    }

    public ChiTietHD(String maHD,
                     String maMon,
                     int soLuong,
                     BigDecimal donGia,
                     BigDecimal thanhTien) {

        this.maHD = maHD;
        this.maMon = maMon;
        this.soLuong = soLuong;
        this.donGia = donGia;
        this.thanhTien = thanhTien;
    }

    public String getMaHD() {
        return maHD;
    }

    public void setMaHD(String maHD) {
        this.maHD = maHD;
    }

    public String getMaMon() {
        return maMon;
    }

    public void setMaMon(String maMon) {
        this.maMon = maMon;
    }

    public int getSoLuong() {
        return soLuong;
    }

    public void setSoLuong(int soLuong) {
        this.soLuong = soLuong;
    }

    public BigDecimal getDonGia() {
        return donGia;
    }

    public void setDonGia(BigDecimal donGia) {
        this.donGia = donGia;
    }

    public BigDecimal getThanhTien() {
        return thanhTien;
    }

    public void setThanhTien(BigDecimal thanhTien) {
        this.thanhTien = thanhTien;
    }

    @Override
    public String toString() {
        return maHD + " - " + maMon;
    }
}