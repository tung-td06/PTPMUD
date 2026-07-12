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
import java.sql.Timestamp;

public class NhapHang implements Serializable {

    private static final long serialVersionUID = 1L;
    private String maHang;
    private String maNCC;

    private Timestamp ngayNhap;

    private BigDecimal tongTien;

    public NhapHang() {
    }

    public NhapHang(String maHang,
                    String maNCC,
                    Timestamp ngayNhap,
                    BigDecimal tongTien) {

        this.maHang = maHang;
        this.maNCC = maNCC;
        this.ngayNhap = ngayNhap;
        this.tongTien = tongTien;
    }

    public String getMaHang() {
        return maHang;
    }

    public void setMaHang(String maHang) {
        this.maHang = maHang;
    }

    public String getMaNCC() {
        return maNCC;
    }

    public void setMaNCC(String maNCC) {
        this.maNCC = maNCC;
    }

    public Timestamp getNgayNhap() {
        return ngayNhap;
    }

    public void setNgayNhap(Timestamp ngayNhap) {
        this.ngayNhap = ngayNhap;
    }

    public BigDecimal getTongTien() {
        return tongTien;
    }

    public void setTongTien(BigDecimal tongTien) {
        this.tongTien = tongTien;
    }

    @Override
    public String toString() {
        return maHang;
    }
}