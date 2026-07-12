/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package CRUD;

import database.DBConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import model.NhanVien;

/**
 *
 * @author Lenovo
 */
public class NhanVienDao {
    
    // gọi tất cả
    public List<NhanVien> getAll()
    {
        List<NhanVien> list =new ArrayList<>();
        String sql="select * from nhanvien";
        
        try(Connection con= DBConnection.getConnection();
            PreparedStatement ps = con.prepareStatement(sql);
            ResultSet rs= ps.executeQuery();)
        {
            while(rs.next())
            {
                NhanVien nv=new NhanVien();
                
                nv.setMaNV(rs.getString("manv"));
                nv.setHoTen(rs.getString("hoten"));
                nv.setNgaySinh(rs.getDate("ngaysinh"));
                nv.setQue(rs.getString("que"));
                nv.setGmail(rs.getString("gmail"));
                nv.setSdt(rs.getString("sdt"));
                
                nv.setChucVu(rs.getString("chucvu"));
                nv.setTrangThai(rs.getString("trangthai"));
                nv.setNote(rs.getString("note"));
                
                list.add(nv);
            
            }
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
        return list;
    }
    
    // tìm theo ID
    public NhanVien findID(String maNV)
    {
        String sql="select * from nhanvien where manv=?";
        try(Connection con=DBConnection.getConnection();
            PreparedStatement ps=con.prepareStatement(sql);)
        {
            ps.setString(1 , maNV);
            ResultSet rs=ps.executeQuery();
            
            if(rs.next())
            {
                NhanVien nv=new NhanVien();
                nv.setMaNV(rs.getString("manv"));
                nv.setHoTen(rs.getString("hoten"));
                nv.setNgaySinh(rs.getDate("ngaysinh"));
                nv.setQue(rs.getString("que"));
                nv.setGmail(rs.getString("gmail"));
                nv.setSdt(rs.getString("sdt"));
                
                nv.setChucVu(rs.getString("chucvu"));
                nv.setTrangThai(rs.getString("trangthai"));
                nv.setNote(rs.getString("note"));
                
                return nv;
            }
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
        return null;
    }
    
    
    // Thêm nhân viên
    public boolean insert(NhanVien nv)
    {
        String sql="Insert into NhanVien(manv,hoten,ngaysinh,que,gmail,sdt,chucvu,trangthai,note) values (?,?,?,?,?,?,?,?,?)";
        
        try(Connection con = DBConnection.getConnection();
            PreparedStatement ps= con.prepareStatement(sql);)
        {
            ps.setString(1, nv.getMaNV());
            ps.setString(2, nv.getHoTen());
            ps.setDate(3, nv.getNgaySinh());
            ps.setString(4, nv.getQue());
            ps.setString(5,nv.getGmail());
            ps.setString(6, nv.getSdt());
            ps.setString(7, nv.getChucVu());
            ps.setString(8, nv.getTrangThai());
            ps.setString(9, nv.getNote());
            
            return ps.executeUpdate()>0;
            
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
        return false;
    }
    
    // Update 
    public boolean update(NhanVien nv)
    {
        String sql = "Update nhanvien SET hoten=?,ngaysinh=?,que=?,gmail=?,sdt=?,chucvu=?,trangthai=?,note=? where manv=?";
        
        try(Connection con=DBConnection.getConnection();
            PreparedStatement ps=con.prepareStatement(sql);)
        {
            ps.setString(1, nv.getHoTen());
            ps.setDate(2, nv.getNgaySinh());
            ps.setString(3, nv.getQue());
            ps.setString(4, nv.getGmail());
            ps.setString(5, nv.getSdt());
            ps.setString(6, nv.getChucVu());
            ps.setString(7, nv.getTrangThai());
            ps.setString(8, nv.getNote());
            ps.setString(9, nv.getMaNV());

            return ps.executeUpdate() > 0;
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
        return false;
    }
    
    public boolean delete(String maNV)
    {
        String sql = "DELETE FROM nhanvien WHERE manv=?";

        try(Connection con = DBConnection.getConnection();
            PreparedStatement ps = con.prepareStatement(sql);)
        {
            ps.setString(1, maNV);

            return ps.executeUpdate() > 0;
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
        return false;
    }
    
        // Tìm theo tên
    public List<NhanVien> searchByName(String keyword)
    {
        List<NhanVien> list = new ArrayList<>();
        String sql = "SELECT * FROM nhanvien WHERE hoten LIKE ?";

        try(Connection con = DBConnection.getConnection();
            PreparedStatement ps = con.prepareStatement(sql);)
        {
            ps.setString(1, "%" + keyword + "%");

            try(ResultSet rs = ps.executeQuery())
            {
                while(rs.next())
                {
                    NhanVien nv = new NhanVien();

                    nv.setMaNV(rs.getString("manv"));
                    nv.setHoTen(rs.getString("hoten"));
                    nv.setNgaySinh(rs.getDate("ngaysinh"));
                    nv.setQue(rs.getString("que"));
                    nv.setGmail(rs.getString("gmail"));
                    nv.setSdt(rs.getString("sdt"));
                    
                    nv.setChucVu(rs.getString("chucvu"));
                    nv.setTrangThai(rs.getString("trangthai"));
                    nv.setNote(rs.getString("note"));

                    list.add(nv);
                }
            }
        }   
        catch(Exception e)
        {
            e.printStackTrace();
        }
        return list;
        }
    
    
    public boolean exists(String maNV) {

        String sql = "SELECT 1 FROM nhanvien WHERE manv=?";

        try(Connection con = DBConnection.getConnection();
            PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, maNV);

            ResultSet rs = ps.executeQuery();

            return rs.next();

        } catch(Exception e) {
            e.printStackTrace();
        }

        return false;
    }
    
    
    //tìm theo sđt
    public NhanVien findByPhone(String sdt) {

        String sql = "SELECT * FROM nhanvien WHERE sdt=?";

        try(Connection con = DBConnection.getConnection();
            PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, sdt);

            ResultSet rs = ps.executeQuery();

            if(rs.next()) {

                NhanVien nv = new NhanVien();

                nv.setMaNV(rs.getString("manv"));
                nv.setHoTen(rs.getString("hoten"));
                nv.setNgaySinh(rs.getDate("ngaysinh"));
                nv.setQue(rs.getString("que"));
                nv.setGmail(rs.getString("gmail"));
                nv.setSdt(rs.getString("sdt"));
               
                nv.setChucVu(rs.getString("chucvu"));
                nv.setTrangThai(rs.getString("trangthai"));
                nv.setNote(rs.getString("note"));

                return nv;
            }

        } catch(Exception e) {
            e.printStackTrace();
        }

        return null;
    }
    
    
    //tìm theo gmail 
    public NhanVien findByGmail(String gmail) {

        String sql = "SELECT * FROM nhanvien WHERE gmail=?";

        try(Connection con = DBConnection.getConnection();
            PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, gmail);

            ResultSet rs = ps.executeQuery();

            if(rs.next()) {

                NhanVien nv = new NhanVien();

                nv.setMaNV(rs.getString("manv"));
                nv.setHoTen(rs.getString("hoten"));
                nv.setNgaySinh(rs.getDate("ngaysinh"));
                nv.setQue(rs.getString("que"));
                nv.setGmail(rs.getString("gmail"));
                nv.setSdt(rs.getString("sdt"));
                
                nv.setChucVu(rs.getString("chucvu"));
                nv.setTrangThai(rs.getString("trangthai"));
                nv.setNote(rs.getString("note"));

                return nv;
            }

        } catch(Exception e) {
            e.printStackTrace();
        }

        return null;
    }
    
    
}
