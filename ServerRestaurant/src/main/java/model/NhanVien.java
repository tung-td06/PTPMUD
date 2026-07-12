/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package model;

import java.io.Serializable;
import java.sql.Date;

/**
 *
 * @author Lenovo
 */
public class NhanVien implements Serializable{
    
    private static final long serialVersionUID = 1L;
    private String maNV, hoTen;
    private Date ngaySinh;
    private String que, gmail, sdt;
    
    private String chucVu, trangThai, note;
    
    public NhanVien()
    {
        
    }
    
    public NhanVien(String maNV, String hoTen, Date ngaySinh, String que, String gmail, String sdt, String chucVu, String trangThai , String note)
    {
        this.maNV=maNV;
        this.hoTen=hoTen;
        this.ngaySinh=ngaySinh;
        this.que=que;
        this.gmail=gmail;
        this.sdt=sdt;
        this.chucVu=chucVu;
        this.trangThai=trangThai;
        this.note=note;
    }

    public String getMaNV() {
        return maNV;
    }

    public void setMaNV(String maNV) {
        this.maNV = maNV;
    }

    public String getHoTen() {
        return hoTen;
    }

    public void setHoTen(String hoTen) {
        this.hoTen = hoTen;
    }

    public Date getNgaySinh() {
        return ngaySinh;
    }

    public void setNgaySinh(Date ngaySinh) {
        this.ngaySinh = ngaySinh;
    }

    public String getQue() {
        return que;
    }

    public void setQue(String que) {
        this.que = que;
    }

    public String getGmail() {
        return gmail;
    }

    public void setGmail(String gmail) {
        this.gmail = gmail;
    }

    public String getSdt() {
        return sdt;
    }

    public void setSdt(String sdt) {
        this.sdt = sdt;
    }

    public String getChucVu() {
        return chucVu;
    }

    public void setChucVu(String chucVu) {
        this.chucVu = chucVu;
    }

    public String getTrangThai() {
        return trangThai;
    }

    public void setTrangThai(String trangThai) {
        this.trangThai = trangThai;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }
    
    @Override
    public String toString()
    {
        return maNV+ " _ " +hoTen;
       
    }

    
           
}
