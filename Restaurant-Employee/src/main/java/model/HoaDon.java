package model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.sql.Timestamp;

public class HoaDon implements Serializable {

    private static final long serialVersionUID = 1L;
    private String maHD;
    private String maKH;
    private String maBan;
    private String maNV;

    private Timestamp timeVao;
    private Timestamp timeRa;

    private BigDecimal tongTien;
    private BigDecimal giamGia;
    private BigDecimal thanhToan;

    private String trangThai;
    private String hinhThucThanhToan;

    public HoaDon() {
    }

    public HoaDon(String maHD,
                  String maKH,
                  String maBan,
                  String maNV,
                  Timestamp timeVao,
                  Timestamp timeRa,
                  BigDecimal tongTien,
                  BigDecimal giamGia,
                  BigDecimal thanhToan,
                  String trangThai) {

        this.maHD = maHD;
        this.maKH = maKH;
        this.maBan = maBan;
        this.maNV = maNV;
        this.timeVao = timeVao;
        this.timeRa = timeRa;
        this.tongTien = tongTien;
        this.giamGia = giamGia;
        this.thanhToan = thanhToan;
        this.trangThai = trangThai;
    }

    public HoaDon(String maHD,
                  String maKH,
                  String maBan,
                  String maNV,
                  Timestamp timeVao,
                  Timestamp timeRa,
                  BigDecimal tongTien,
                  BigDecimal giamGia,
                  BigDecimal thanhToan,
                  String trangThai,
                  String hinhThucThanhToan) {

        this.maHD = maHD;
        this.maKH = maKH;
        this.maBan = maBan;
        this.maNV = maNV;
        this.timeVao = timeVao;
        this.timeRa = timeRa;
        this.tongTien = tongTien;
        this.giamGia = giamGia;
        this.thanhToan = thanhToan;
        this.trangThai = trangThai;
        this.hinhThucThanhToan = hinhThucThanhToan;
    }

    // Keep backwards compatibility for constructor without thanhToan
    public HoaDon(String maHD,
                  String maKH,
                  String maBan,
                  String maNV,
                  Timestamp timeVao,
                  Timestamp timeRa,
                  BigDecimal tongTien,
                  BigDecimal giamGia,
                  String trangThai) {

        this.maHD = maHD;
        this.maKH = maKH;
        this.maBan = maBan;
        this.maNV = maNV;
        this.timeVao = timeVao;
        this.timeRa = timeRa;
        this.tongTien = tongTien;
        this.giamGia = giamGia;
        this.trangThai = trangThai;
        this.thanhToan = tongTien != null ? (giamGia != null ? tongTien.subtract(giamGia) : tongTien) : BigDecimal.ZERO;
    }

    public BigDecimal getThanhToan() {
        return thanhToan;
    }

    public void setThanhToan(BigDecimal thanhToan) {
        this.thanhToan = thanhToan;
    }

    public String getMaHD() {
        return maHD;
    }

    public void setMaHD(String maHD) {
        this.maHD = maHD;
    }

    public String getMaKH() {
        return maKH;
    }

    public void setMaKH(String maKH) {
        this.maKH = maKH;
    }

    public String getMaBan() {
        return maBan;
    }

    public void setMaBan(String maBan) {
        this.maBan = maBan;
    }

    public String getMaNV() {
        return maNV;
    }

    public void setMaNV(String maNV) {
        this.maNV = maNV;
    }

    public Timestamp getTimeVao() {
        return timeVao;
    }

    public void setTimeVao(Timestamp timeVao) {
        this.timeVao = timeVao;
    }

    public Timestamp getTimeRa() {
        return timeRa;
    }

    public void setTimeRa(Timestamp timeRa) {
        this.timeRa = timeRa;
    }

    public BigDecimal getTongTien() {
        return tongTien;
    }

    public void setTongTien(BigDecimal tongTien) {
        this.tongTien = tongTien;
    }

    public BigDecimal getGiamGia() {
        return giamGia;
    }

    public void setGiamGia(BigDecimal giamGia) {
        this.giamGia = giamGia;
    }

    public String getTrangThai() {
        return trangThai;
    }

    public void setTrangThai(String trangThai) {
        this.trangThai = trangThai;
    }

    public String getHinhThucThanhToan() {
        return hinhThucThanhToan;
    }

    public void setHinhThucThanhToan(String hinhThucThanhToan) {
        this.hinhThucThanhToan = hinhThucThanhToan;
    }

    @Override
    public String toString() {
        return maHD;
    }
}