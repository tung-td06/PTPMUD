package handler;

import CRUD.KhachHangDAO;
import java.util.List;
import model.KhachHang;

public class KhachHangHandler {

    private final KhachHangDAO khachHangDAO;

    public KhachHangHandler() {
        khachHangDAO = new KhachHangDAO();
    }

    public List<KhachHang> getAllKhachHang() {
        return khachHangDAO.getAll();
    }

    public KhachHang findKhachHangByID(String maKH) {

        if(maKH == null ||
           maKH.trim().isEmpty()) {
            return null;
        }

        return khachHangDAO.findID(maKH);
    }

    public KhachHang findByPhone(String sdt) {

        if(sdt == null ||
           sdt.trim().isEmpty()) {
            return null;
        }

        return khachHangDAO.findByPhone(sdt);
    }

    public boolean addKhachHang(KhachHang kh) {

        if(kh == null) {
            return false;
        }

        if(kh.getMaKH() == null ||
           kh.getMaKH().trim().isEmpty()) {
            return false;
        }

        if(khachHangDAO.findID(
                 kh.getMaKH()) != null) {
            return false;
        }

        return khachHangDAO.insert(kh);
    }

    public boolean updateKhachHang(KhachHang kh) {

        if(kh == null) {
            return false;
        }

        if(kh.getMaKH() == null ||
           kh.getMaKH().trim().isEmpty()) {
            return false;
        }

        return khachHangDAO.update(kh);
    }

    public boolean deleteKhachHang(String maKH) {

        if(maKH == null ||
           maKH.trim().isEmpty()) {
            return false;
        }

        return khachHangDAO.delete(maKH);
    }

    public List<KhachHang> searchByName(String keyword) {

        if (keyword == null) {
            keyword = "";
        }

        return khachHangDAO.searchByName(keyword);
    }

    public boolean updateDiemTichLuy(String maKH,
                                     int diem) {

        if(maKH == null ||
           maKH.trim().isEmpty()) {
            return false;
        }

        if(diem < 0) {
            return false;
        }

        return khachHangDAO.updateDiemTichLuy(
            maKH,
            diem
        );
    }

    public List<KhachHang> getTopKhachHang() {
        return khachHangDAO.getTopKhachHang();
    }
}