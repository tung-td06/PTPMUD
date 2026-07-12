package model;

import java.io.Serializable;
import java.sql.Timestamp;

public class DatBan implements Serializable {

    private static final long serialVersionUID = 1L;
    private String maDatBan;

    private String maKH;

    private String maBan;

    private Timestamp timeVao;

    private Timestamp timeRa;

    private Timestamp thoiGianDen;

    private int soNguoi;

    private String note;

    private String trangThai;

    public DatBan() {
    }

    public DatBan(String maDatBan,
            String maKH,
            String maBan,
            Timestamp timeVao,
            Timestamp timeRa,
            int soNguoi,
            String note,
            String trangThai) {

        this.maDatBan = maDatBan;
        this.maKH = maKH;
        this.maBan = maBan;
        this.timeVao = timeVao;
        this.timeRa = timeRa;
        this.thoiGianDen = null;
        this.soNguoi = soNguoi;
        this.note = note;
        this.trangThai = trangThai;
    }

    public DatBan(String maDatBan,
            String maKH,
            String maBan,
            Timestamp timeVao,
            Timestamp timeRa,
            Timestamp thoiGianDen,
            int soNguoi,
            String note,
            String trangThai) {

        this.maDatBan = maDatBan;
        this.maKH = maKH;
        this.maBan = maBan;
        this.timeVao = timeVao;
        this.timeRa = timeRa;
        this.thoiGianDen = thoiGianDen;
        this.soNguoi = soNguoi;
        this.note = note;
        this.trangThai = trangThai;
    }

    public String getMaDatBan() {
        return maDatBan;
    }

    public void setMaDatBan(String maDatBan) {
        this.maDatBan = maDatBan;
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

    public Timestamp getThoiGianDen() {
        return thoiGianDen;
    }

    public void setThoiGianDen(Timestamp thoiGianDen) {
        this.thoiGianDen = thoiGianDen;
    }

    public int getSoNguoi() {
        return soNguoi;
    }

    public void setSoNguoi(int soNguoi) {
        this.soNguoi = soNguoi;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public String getTrangThai() {
        return trangThai;
    }

    public void setTrangThai(String trangThai) {
        this.trangThai = trangThai;
    }

    @Override
    public String toString() {
        return maDatBan;
    }
}
