package model;

import java.io.Serializable;
import java.math.BigDecimal;

public class MonAn implements Serializable {

    private static final long serialVersionUID = 1L;
    private String maMon;
    private String maLoai;
    private String tenMon;
    private BigDecimal donGia;
    private String anh;
    private String trangThai;

    public MonAn() {
    }

    public MonAn(String maMon, String maLoai, String tenMon,
                 BigDecimal donGia, String anh, String trangThai) {

        this.maMon = maMon;
        this.maLoai = maLoai;
        this.tenMon = tenMon;
        this.donGia = donGia;
        this.anh = anh;
        this.trangThai = trangThai;
    }

    public String getMaMon() {
        return maMon;
    }

    public void setMaMon(String maMon) {
        this.maMon = maMon;
    }

    public String getMaLoai() {
        return maLoai;
    }

    public void setMaLoai(String maLoai) {
        this.maLoai = maLoai;
    }

    public String getTenMon() {
        return tenMon;
    }

    public void setTenMon(String tenMon) {
        this.tenMon = tenMon;
    }

    public BigDecimal getDonGia() {
        return donGia;
    }

    public void setDonGia(BigDecimal donGia) {
        this.donGia = donGia;
    }

    public String getAnh() {
        return anh;
    }

    public void setAnh(String anh) {
        this.anh = anh;
    }

    public String isTrangThai() {
        return trangThai;
    }

    public void setTrangThai(String trangThai) {
        this.trangThai = trangThai;
    }

    @Override
    public String toString() {
        return tenMon;
    }
}
