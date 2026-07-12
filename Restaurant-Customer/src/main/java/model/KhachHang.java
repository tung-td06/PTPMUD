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

public class KhachHang implements Serializable {

    private static final long serialVersionUID = 1L;
    
    private String maKH;
    private String tenKH;
    private String sdt;
    private int diemTichLuy;

    public KhachHang() {
    }

    public KhachHang(String maKH,
                     String tenKH,
                     String sdt,
                     int diemTichLuy) {

        this.maKH = maKH;
        this.tenKH = tenKH;
        this.sdt = sdt;
        this.diemTichLuy = diemTichLuy;
    }

    public String getMaKH() {
        return maKH;
    }

    public void setMaKH(String maKH) {
        this.maKH = maKH;
    }

    public String getTenKH() {
        return tenKH;
    }

    public void setTenKH(String tenKH) {
        this.tenKH = tenKH;
    }

    public String getSdt() {
        return sdt;
    }

    public void setSdt(String sdt) {
        this.sdt = sdt;
    }

    public int getDiemTichLuy() {
        return diemTichLuy;
    }

    public void setDiemTichLuy(int diemTichLuy) {
        this.diemTichLuy = diemTichLuy;
    }

    @Override
    public String toString() {
        return tenKH;
    }
}