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

public class ChiTietNH implements Serializable {

    private static final long serialVersionUID = 1L;
    private String maHang;
    private String maNL;
    private int soLuong;
    private BigDecimal donGiaNhap;
    private BigDecimal thanhTien;

    public ChiTietNH() {
    }

    public ChiTietNH(String maHang,
                     String maNL,
                     int soLuong,
                     BigDecimal donGiaNhap,
                     BigDecimal thanhTien) {

        this.maHang = maHang;
        this.maNL = maNL;
        this.soLuong = soLuong;
        this.donGiaNhap = donGiaNhap;
        this.thanhTien = thanhTien;
    }

    public String getMaHang() {
        return maHang;
    }

    public void setMaHang(String maHang) {
        this.maHang = maHang;
    }

    public String getMaNL() {
        return maNL;
    }

    public void setMaNL(String maNL) {
        this.maNL = maNL;
    }

    public int getSoLuong() {
        return soLuong;
    }

    public void setSoLuong(int soLuong) {
        this.soLuong = soLuong;
    }

    public BigDecimal getDonGiaNhap() {
        return donGiaNhap;
    }

    public void setDonGiaNhap(BigDecimal donGiaNhap) {
        this.donGiaNhap = donGiaNhap;
    }

    public BigDecimal getThanhTien() {
        return thanhTien;
    }

    public void setThanhTien(BigDecimal thanhTien) {
        this.thanhTien = thanhTien;
    }

    @Override
    public String toString() {
        return maHang + " - " + maNL;
    }
}