package handler;

import CRUD.MonAnDAO;
import java.math.BigDecimal;
import java.util.List;
import model.MonAn;

public class MonAnHandler {

    private final MonAnDAO monAnDAO;

    public MonAnHandler() {
        monAnDAO = new MonAnDAO();
    }

    // Lấy tất cả món ăn
    public List<MonAn> getAll() {
        return monAnDAO.getAll();
    }

    // Tìm theo mã món
    public MonAn findID(String maMon) {

        if (maMon == null || maMon.trim().isEmpty()) {
            return null;
        }

        return monAnDAO.findID(maMon);
    }

    // Tìm theo tên chính xác
    public MonAn findByName(String tenMon) {

        if (tenMon == null || tenMon.trim().isEmpty()) {
            return null;
        }

        return monAnDAO.findByName(tenMon);
    }

    // Tìm kiếm theo tên
    public List<MonAn> searchByName(String keyword) {

        if (keyword == null) {
            keyword = "";
        }

        return monAnDAO.searchByName(keyword);
    }

    // Lấy món theo loại
    public List<MonAn> getByLoai(String maLoai) {

        if (maLoai == null || maLoai.trim().isEmpty()) {
            return null;
        }

        return monAnDAO.getByLoai(maLoai);
    }

    // Lấy món đang bán
    public List<MonAn> getMonDangBan() {
        return monAnDAO.getMonDangBan();
    }

    // Lấy món ngừng bán
    public List<MonAn> getMonNgungBan() {
        return monAnDAO.getMonNgungBan();
    }

    // Thêm món ăn
    public boolean insert(MonAn ma) {

        if (ma == null) {
            return false;
        }

        if (ma.getMaMon() == null || ma.getMaMon().trim().isEmpty()) {
            return false;
        }

        if (monAnDAO.exists(ma.getMaMon())) {
            return false;
        }

        return monAnDAO.insert(ma);
    }

    // Cập nhật món ăn
    public boolean update(MonAn ma) {

        if (ma == null) {
            return false;
        }

        if (!monAnDAO.exists(ma.getMaMon())) {
            return false;
        }

        return monAnDAO.update(ma);
    }

    // Xóa món ăn
    public boolean delete(String maMon) {

        if (maMon == null || maMon.trim().isEmpty()) {
            return false;
        }

        // Không cho xóa nếu đã xuất hiện trong hóa đơn
        if (monAnDAO.countChiTietHD(maMon) > 0) {
            return false;
        }

        // Không cho xóa nếu đang có định lượng
        if (monAnDAO.countDinhLuong(maMon) > 0) {
            return false;
        }

        return monAnDAO.delete(maMon);
    }

    // Cập nhật trạng thái món
    public boolean updateTrangThai(String maMon, boolean trangThai) {

        if (maMon == null || maMon.trim().isEmpty()) {
            return false;
        }

        return monAnDAO.updateTrangThai(maMon, trangThai);
    }

    // Cập nhật giá món
    public boolean updateGia(String maMon, BigDecimal donGia) {

        if (maMon == null || maMon.trim().isEmpty()) {
            return false;
        }

        if (donGia == null || donGia.compareTo(BigDecimal.ZERO) < 0) {
            return false;
        }

        return monAnDAO.updateGia(maMon, donGia);
    }

    // Kiểm tra tồn tại
    public boolean exists(String maMon) {

        if (maMon == null || maMon.trim().isEmpty()) {
            return false;
        }

        return monAnDAO.exists(maMon);
    }

    // Đếm số hóa đơn chứa món
    public int countChiTietHD(String maMon) {

        if (maMon == null || maMon.trim().isEmpty()) {
            return 0;
        }

        return monAnDAO.countChiTietHD(maMon);
    }

    // Đếm số định lượng nguyên liệu
    public int countDinhLuong(String maMon) {

        if (maMon == null || maMon.trim().isEmpty()) {
            return 0;
        }

        return monAnDAO.countDinhLuong(maMon);
    }
}