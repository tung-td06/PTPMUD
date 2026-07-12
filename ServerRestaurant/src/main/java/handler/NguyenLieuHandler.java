package handler;

import CRUD.NguyenLieuDAO;
import java.util.List;
import model.NguyenLieu;

public class NguyenLieuHandler {

    private final NguyenLieuDAO nguyenLieuDAO;

    public NguyenLieuHandler() {
        nguyenLieuDAO = new NguyenLieuDAO();
    }

    // Lấy tất cả nguyên liệu
    public List<NguyenLieu> getAll() {
        return nguyenLieuDAO.getAll();
    }

    // Tìm theo mã
    public NguyenLieu findID(String maNL) {

        if (maNL == null || maNL.trim().isEmpty()) {
            return null;
        }

        return nguyenLieuDAO.findID(maNL);
    }

    // Tìm theo tên
    public NguyenLieu findByName(String tenNL) {

        if (tenNL == null || tenNL.trim().isEmpty()) {
            return null;
        }

        return nguyenLieuDAO.findByName(tenNL);
    }

    // Tìm kiếm theo tên
    public List<NguyenLieu> searchByName(String keyword) {

        if (keyword == null) {
            keyword = "";
        }

        return nguyenLieuDAO.searchByName(keyword);
    }

    // Danh sách nguyên liệu sắp hết
    public List<NguyenLieu> getNguyenLieuSapHet() {
        return nguyenLieuDAO.getNguyenLieuSapHet();
    }

    // Thêm nguyên liệu
    public boolean insert(NguyenLieu nl) {

        if (nl == null) {
            return false;
        }

        if (nl.getMaNL() == null || nl.getMaNL().trim().isEmpty()) {
            return false;
        }

        if (nl.getTenNL() == null || nl.getTenNL().trim().isEmpty()) {
            return false;
        }

        if (nguyenLieuDAO.exists(nl.getMaNL())) {
            return false;
        }

        return nguyenLieuDAO.insert(nl);
    }

    // Cập nhật nguyên liệu
    public boolean update(NguyenLieu nl) {

        if (nl == null) {
            return false;
        }

        if (!nguyenLieuDAO.exists(nl.getMaNL())) {
            return false;
        }

        return nguyenLieuDAO.update(nl);
    }

    // Xóa nguyên liệu
    public boolean delete(String maNL) {

        if (maNL == null || maNL.trim().isEmpty()) {
            return false;
        }

        // Đang được sử dụng trong định lượng món
        if (nguyenLieuDAO.countDinhLuong(maNL) > 0) {
            return false;
        }

        // Đã từng xuất hiện trong phiếu nhập
        if (nguyenLieuDAO.countChiTietNhap(maNL) > 0) {
            return false;
        }

        return nguyenLieuDAO.delete(maNL);
    }

    // Kiểm tra tồn tại
    public boolean exists(String maNL) {

        if (maNL == null || maNL.trim().isEmpty()) {
            return false;
        }

        return nguyenLieuDAO.exists(maNL);
    }

    // Cập nhật tồn kho
    public boolean updateSoLuongKho(String maNL, int soLuongKho) {

        if (maNL == null || maNL.trim().isEmpty()) {
            return false;
        }

        if (soLuongKho < 0) {
            return false;
        }

        return nguyenLieuDAO.updateSoLuongKho(maNL, soLuongKho);
    }

    // Nhập kho
    public boolean tangSoLuongKho(String maNL, int soLuong) {

        if (maNL == null || maNL.trim().isEmpty()) {
            return false;
        }

        if (soLuong <= 0) {
            return false;
        }

        return nguyenLieuDAO.tangSoLuongKho(maNL, soLuong);
    }

    // Xuất kho
    public boolean giamSoLuongKho(String maNL, int soLuong) {

        if (maNL == null || maNL.trim().isEmpty()) {
            return false;
        }

        if (soLuong <= 0) {
            return false;
        }

        NguyenLieu nl = nguyenLieuDAO.findID(maNL);

        if (nl == null) {
            return false;
        }

        if (nl.getSoLuongKho() < soLuong) {
            return false;
        }

        return nguyenLieuDAO.giamSoLuongKho(maNL, soLuong);
    }

    // Đếm số món đang sử dụng nguyên liệu
    public int countDinhLuong(String maNL) {

        if (maNL == null || maNL.trim().isEmpty()) {
            return 0;
        }

        return nguyenLieuDAO.countDinhLuong(maNL);
    }

    // Đếm số lần đã nhập hàng
    public int countChiTietNhap(String maNL) {

        if (maNL == null || maNL.trim().isEmpty()) {
            return 0;
        }

        return nguyenLieuDAO.countChiTietNhap(maNL);
    }
}