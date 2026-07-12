package model;

import java.io.Serializable;
import java.sql.Timestamp;

public class Order implements Serializable {
    private static final long serialVersionUID = 1L;
    private String maOrder;
    private String maBan;
    private String maNV;
    private String maKH;   // Khách hàng tự đặt qua app (nullable)
    private String maHD;
    private Timestamp ngayTao;
    private String trangThai;
    
    // Aggregated fields
    private int tongMon;
    private int tongSoLuong;
    private double tongTien;

    public Order() {}

    public Order(String maOrder, String maBan, String maNV, String maHD, Timestamp ngayTao, String trangThai) {
        this.maOrder = maOrder;
        this.maBan = maBan;
        this.maNV = maNV;
        this.maHD = maHD;
        this.ngayTao = ngayTao;
        this.trangThai = trangThai;
    }

    public Order(String maOrder, String maBan, String maNV, String maKH, String maHD, Timestamp ngayTao, String trangThai) {
        this.maOrder = maOrder;
        this.maBan = maBan;
        this.maNV = maNV;
        this.maKH = maKH;
        this.maHD = maHD;
        this.ngayTao = ngayTao;
        this.trangThai = trangThai;
    }

    public String getMaOrder() { return maOrder; }
    public void setMaOrder(String maOrder) { this.maOrder = maOrder; }

    public String getMaBan() { return maBan; }
    public void setMaBan(String maBan) { this.maBan = maBan; }

    public String getMaNV() { return maNV; }
    public void setMaNV(String maNV) { this.maNV = maNV; }

    public String getMaKH() { return maKH; }
    public void setMaKH(String maKH) { this.maKH = maKH; }

    public String getMaHD() { return maHD; }
    public void setMaHD(String maHD) { this.maHD = maHD; }

    public Timestamp getNgayTao() { return ngayTao; }
    public void setNgayTao(Timestamp ngayTao) { this.ngayTao = ngayTao; }

    public String getTrangThai() { return trangThai; }
    public void setTrangThai(String trangThai) { this.trangThai = trangThai; }

    public int getTongMon() { return tongMon; }
    public void setTongMon(int tongMon) { this.tongMon = tongMon; }

    public int getTongSoLuong() { return tongSoLuong; }
    public void setTongSoLuong(int tongSoLuong) { this.tongSoLuong = tongSoLuong; }

    public double getTongTien() { return tongTien; }
    public void setTongTien(double tongTien) { this.tongTien = tongTien; }
}
