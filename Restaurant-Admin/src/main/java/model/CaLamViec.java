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
import java.sql.Timestamp;

public class CaLamViec implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    private String maCa;

    private String maNV;

    private String tenCa;

    private Timestamp gioBatDau;

    private Timestamp gioKetThuc;

    public CaLamViec() {
    }

    public CaLamViec(String maCa,
                     String maNV,
                     String tenCa,
                     Timestamp gioBatDau,
                     Timestamp gioKetThuc) {

        this.maCa = maCa;
        this.maNV = maNV;
        this.tenCa = tenCa;
        this.gioBatDau = gioBatDau;
        this.gioKetThuc = gioKetThuc;
    }

    public String getMaCa() {
        return maCa;
    }

    public void setMaCa(String maCa) {
        this.maCa = maCa;
    }

    public String getMaNV() {
        return maNV;
    }

    public void setMaNV(String maNV) {
        this.maNV = maNV;
    }

    public String getTenCa() {
        return tenCa;
    }

    public void setTenCa(String tenCa) {
        this.tenCa = tenCa;
    }

    public Timestamp getGioBatDau() {
        return gioBatDau;
    }

    public void setGioBatDau(Timestamp gioBatDau) {
        this.gioBatDau = gioBatDau;
    }

    public Timestamp getGioKetThuc() {
        return gioKetThuc;
    }

    public void setGioKetThuc(Timestamp gioKetThuc) {
        this.gioKetThuc = gioKetThuc;
    }

    @Override
    public String toString() {
        return tenCa;
    }
}
