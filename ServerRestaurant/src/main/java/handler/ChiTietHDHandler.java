package handler;

import CRUD.ChiTietHDDAO;
import java.util.List;
import model.ChiTietHD;

public class ChiTietHDHandler {

    private final ChiTietHDDAO chiTietHDDAO;

    public ChiTietHDHandler() {
        chiTietHDDAO = new ChiTietHDDAO();
    }

    // Lấy tất cả chi tiết hóa đơn
    public List<ChiTietHD> getAllChiTietHD() {
        return chiTietHDDAO.getAll();
    }

    // Tìm theo khóa chính kép
    public ChiTietHD findChiTietHD(String maHD,
                                   String maMon) {

        if(maHD == null || maHD.trim().isEmpty()) {
            return null;
        }

        if(maMon == null || maMon.trim().isEmpty()) {
            return null;
        }

        return chiTietHDDAO.findID(maHD, maMon);
    }

    // Thêm chi tiết hóa đơn
    public boolean addChiTietHD(ChiTietHD ct) {

        if(ct == null) {
            return false;
        }

        if(ct.getMaHD() == null ||
           ct.getMaHD().trim().isEmpty()) {
            return false;
        }

        if(ct.getMaMon() == null ||
           ct.getMaMon().trim().isEmpty()) {
            return false;
        }

        if(chiTietHDDAO.findID(
                ct.getMaHD(),
                ct.getMaMon()) != null) {

            return false;
        }

        return chiTietHDDAO.insert(ct);
    }

    // Cập nhật chi tiết hóa đơn
    public boolean updateChiTietHD(ChiTietHD ct) {

        if(ct == null) {
            return false;
        }

        if(ct.getMaHD() == null ||
           ct.getMaHD().trim().isEmpty()) {
            return false;
        }

        if(ct.getMaMon() == null ||
           ct.getMaMon().trim().isEmpty()) {
            return false;
        }

        return chiTietHDDAO.update(ct);
    }

    // Xóa chi tiết hóa đơn
    public boolean deleteChiTietHD(String maHD,
                                   String maMon) {

        if(maHD == null ||
           maHD.trim().isEmpty()) {
            return false;
        }

        if(maMon == null ||
           maMon.trim().isEmpty()) {
            return false;
        }

        return chiTietHDDAO.delete(maHD, maMon);
    }

    // Lấy danh sách món theo hóa đơn
    public List<ChiTietHD> getByHoaDon(String maHD) {

        if(maHD == null ||
           maHD.trim().isEmpty()) {
            return null;
        }

        return chiTietHDDAO.getByHoaDon(maHD);
    }
}