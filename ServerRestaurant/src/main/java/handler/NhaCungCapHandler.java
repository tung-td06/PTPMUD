package handler;

import CRUD.NhaCungCapDAO;
import java.util.List;
import model.NhaCungCap;

public class NhaCungCapHandler {

    private final NhaCungCapDAO dao;

    public NhaCungCapHandler() {
        dao = new NhaCungCapDAO();
    }

    // =========================
    // Lấy toàn bộ nhà cung cấp
    // =========================
    public List<NhaCungCap> getAll() {
        return dao.getAll();
    }

    // =========================
    // Tìm theo mã
    // =========================
    public NhaCungCap findID(String maNCC) {

        if(maNCC == null || maNCC.trim().isEmpty()) {
            return null;
        }

        return dao.findID(maNCC.trim());
    }

    // =========================
    // Tìm theo số điện thoại
    // =========================
    public NhaCungCap findByPhone(String sdt) {

        if(sdt == null || sdt.trim().isEmpty()) {
            return null;
        }

        return dao.findByPhone(sdt.trim());
    }

    // =========================
    // Kiểm tra tồn tại
    // =========================
    public boolean exists(String maNCC) {

        if(maNCC == null || maNCC.trim().isEmpty()) {
            return false;
        }

        return dao.exists(maNCC.trim());
    }

    // =========================
    // Thêm
    // =========================
    public boolean insert(NhaCungCap ncc) {

        if(ncc == null)
            return false;

        if(ncc.getMaNCC() == null || ncc.getMaNCC().trim().isEmpty())
            return false;

        if(ncc.getTenNCC() == null || ncc.getTenNCC().trim().isEmpty())
            return false;

        if(exists(ncc.getMaNCC()))
            return false;

        return dao.insert(ncc);
    }

    // =========================
    // Cập nhật
    // =========================
    public boolean update(NhaCungCap ncc) {

        if(ncc == null)
            return false;

        if(!exists(ncc.getMaNCC()))
            return false;

        return dao.update(ncc);
    }

    // =========================
    // Xóa
    // =========================
    public boolean delete(String maNCC) {

        if(maNCC == null || maNCC.trim().isEmpty())
            return false;

        if(!exists(maNCC))
            return false;

        return dao.delete(maNCC);
    }

    // =========================
    // Tìm theo tên
    // =========================
    public List<NhaCungCap> searchByName(String keyword) {

        if(keyword == null)
            keyword = "";

        return dao.searchByName(keyword.trim());
    }

}