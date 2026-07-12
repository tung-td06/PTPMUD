package model;

import java.io.Serializable;
import java.math.BigDecimal;

public class OrderDetail implements Serializable {
    private static final long serialVersionUID = 1L;
    private String maOrder;
    private String maMon;
    private String tenMon;
    private int soLuong;
    private BigDecimal donGia;
    private String trangThai;
    private String ghiChu;
    private BigDecimal thanhTien;

    public OrderDetail() {}

    public OrderDetail(String maOrder, String maMon, String tenMon, int soLuong, BigDecimal donGia, String trangThai) {
        this.maOrder = maOrder;
        this.maMon = maMon;
        this.tenMon = tenMon;
        this.soLuong = soLuong;
        this.donGia = donGia;
        this.trangThai = trangThai;
        if (donGia != null) {
            this.thanhTien = donGia.multiply(BigDecimal.valueOf(soLuong));
        }
    }

    public OrderDetail(String maOrder, String maMon, String tenMon, int soLuong, BigDecimal donGia, String trangThai, String ghiChu) {
        this(maOrder, maMon, tenMon, soLuong, donGia, trangThai);
        this.ghiChu = ghiChu;
    }

    public OrderDetail(String maOrder, String maMon, String tenMon, int soLuong, BigDecimal donGia, String trangThai, String ghiChu, BigDecimal thanhTien) {
        this.maOrder = maOrder;
        this.maMon = maMon;
        this.tenMon = tenMon;
        this.soLuong = soLuong;
        this.donGia = donGia;
        this.trangThai = trangThai;
        this.ghiChu = ghiChu;
        this.thanhTien = thanhTien;
    }

    public String getMaOrder() {
        return maOrder;
    }

    public void setMaOrder(String maOrder) {
        this.maOrder = maOrder;
    }

    public String getMaMon() {
        return maMon;
    }

    public void setMaMon(String maMon) {
        this.maMon = maMon;
    }

    public String getTenMon() {
        return tenMon;
    }

    public void setTenMon(String tenMon) {
        this.tenMon = tenMon;
    }

    public int getSoLuong() {
        return soLuong;
    }

    public void setSoLuong(int soLuong) {
        this.soLuong = soLuong;
        if (this.donGia != null) {
            this.thanhTien = this.donGia.multiply(BigDecimal.valueOf(soLuong));
        }
    }

    public BigDecimal getDonGia() {
        return donGia;
    }

    public void setDonGia(BigDecimal donGia) {
        this.donGia = donGia;
        if (donGia != null) {
            this.thanhTien = donGia.multiply(BigDecimal.valueOf(this.soLuong));
        }
    }

    public String getTrangThai() {
        return trangThai;
    }

    public void setTrangThai(String trangThai) {
        this.trangThai = trangThai;
    }

    public String getGhiChu() {
        return ghiChu;
    }

    public void setGhiChu(String ghiChu) {
        this.ghiChu = ghiChu;
    }

    public BigDecimal getThanhTien() {
        if (thanhTien == null && donGia != null) {
            return donGia.multiply(BigDecimal.valueOf(soLuong));
        }
        return thanhTien;
    }

    public void setThanhTien(BigDecimal thanhTien) {
        this.thanhTien = thanhTien;
    }
}
