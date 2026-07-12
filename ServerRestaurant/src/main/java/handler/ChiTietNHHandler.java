package handler;

import CRUD.ChiTietNHDAO;
import java.util.List;
import model.ChiTietNH;

public class ChiTietNHHandler {

    private final ChiTietNHDAO chiTietNHDAO;

    public ChiTietNHHandler() {
        chiTietNHDAO = new ChiTietNHDAO();
    }

    // Lấy tất cả chi tiết nhập hàng
    public List<ChiTietNH> getAllChiTietNH() {
        return chiTietNHDAO.getAll();
    }

    // Tìm theo khóa chính kép
    public ChiTietNH findChiTietNH(String maHang,
                                   String maNL) {

        if(maHang == null || maHang.trim().isEmpty()) {
            return null;
        }

        if(maNL == null || maNL.trim().isEmpty()) {
            return null;
        }

        return chiTietNHDAO.findID(maHang, maNL);
    }

    // Thêm chi tiết nhập hàng
    public boolean addChiTietNH(ChiTietNH ct) {

        if(ct == null) {
            return false;
        }

        if(ct.getMaHang() == null ||
           ct.getMaHang().trim().isEmpty()) {
            return false;
        }

        if(ct.getMaNL() == null ||
           ct.getMaNL().trim().isEmpty()) {
            return false;
        }

        if(chiTietNHDAO.findID(
                ct.getMaHang(),
                ct.getMaNL()) != null) {

            return false;
        }

        return chiTietNHDAO.insert(ct);
    }

    // Cập nhật chi tiết nhập hàng
    public boolean updateChiTietNH(ChiTietNH ct) {

        if(ct == null) {
            return false;
        }

        if(ct.getMaHang() == null ||
           ct.getMaHang().trim().isEmpty()) {
            return false;
        }

        if(ct.getMaNL() == null ||
           ct.getMaNL().trim().isEmpty()) {
            return false;
        }

        return chiTietNHDAO.update(ct);
    }

    // Xóa chi tiết nhập hàng
    public boolean deleteChiTietNH(String maHang,
                                   String maNL) {

        if(maHang == null ||
           maHang.trim().isEmpty()) {
            return false;
        }

        if(maNL == null ||
           maNL.trim().isEmpty()) {
            return false;
        }

        return chiTietNHDAO.delete(maHang, maNL);
    }

    // Lấy danh sách nguyên liệu theo phiếu nhập
    public List<ChiTietNH> getByNhapHang(String maHang) {

        if(maHang == null ||
           maHang.trim().isEmpty()) {
            return null;
        }

        return chiTietNHDAO.getByNhapHang(maHang);
    }
}