package handler;

import CRUD.NhanVienDao;
import java.util.List;
import model.NhanVien;

public class NhanVienHandler {

    private final NhanVienDao dao;

    public NhanVienHandler() {
        dao = new NhanVienDao();
    }

    // =========================
    // Lấy tất cả nhân viên
    // =========================
    public List<NhanVien> getAll() {
        return dao.getAll();
    }

    // =========================
    // Tìm theo mã
    // =========================
    public NhanVien findID(String maNV) {

        if (maNV == null || maNV.trim().isEmpty()) {
            return null;
        }

        return dao.findID(maNV.trim());
    }

    // =========================
    // Tìm theo số điện thoại
    // =========================
    public NhanVien findByPhone(String sdt) {

        if (sdt == null || sdt.trim().isEmpty()) {
            return null;
        }

        return dao.findByPhone(sdt.trim());
    }

    // =========================
    // Tìm theo Gmail
    // =========================
    public NhanVien findByGmail(String gmail) {

        if (gmail == null || gmail.trim().isEmpty()) {
            return null;
        }

        return dao.findByGmail(gmail.trim());
    }

    // =========================
    // Kiểm tra tồn tại
    // =========================
    public boolean exists(String maNV) {

        if (maNV == null || maNV.trim().isEmpty()) {
            return false;
        }

        return dao.exists(maNV.trim());
    }

    // =========================
    // Thêm nhân viên
    // =========================
    public boolean insert(NhanVien nv) {

        if (nv == null) {
            return false;
        }

        if (nv.getMaNV() == null || nv.getMaNV().trim().isEmpty()) {
            return false;
        }

        if (nv.getHoTen() == null || nv.getHoTen().trim().isEmpty()) {
            return false;
        }

        if (exists(nv.getMaNV())) {
            return false;
        }

        return dao.insert(nv);
    }

    // =========================
    // Cập nhật nhân viên
    // =========================
    public boolean update(NhanVien nv) {

        if (nv == null) {
            return false;
        }

        if (!exists(nv.getMaNV())) {
            return false;
        }

        return dao.update(nv);
    }

    // =========================
    // Xóa nhân viên
    // =========================
    public boolean delete(String maNV) {

        if (maNV == null || maNV.trim().isEmpty()) {
            return false;
        }

        if (!exists(maNV)) {
            return false;
        }

        return dao.delete(maNV);
    }

    // =========================
    // Tìm theo tên
    // =========================
    public List<NhanVien> searchByName(String keyword) {

        if (keyword == null) {
            keyword = "";
        }

        return dao.searchByName(keyword.trim());
    }

}