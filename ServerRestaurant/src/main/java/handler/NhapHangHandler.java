package handler;

import CRUD.NhapHangDAO;
import java.util.Date;
import java.util.List;
import model.NhapHang;

public class NhapHangHandler {

    private final NhapHangDAO nhapHangDAO;

    public NhapHangHandler() {
        nhapHangDAO = new NhapHangDAO();
    }

    // Lấy tất cả phiếu nhập
    public List<NhapHang> getAllNhapHang() {
        return nhapHangDAO.getAll();
    }

    // Tìm theo mã
    public NhapHang findNhapHangByID(String maHang) {

        if (maHang == null || maHang.trim().isEmpty()) {
            return null;
        }

        return nhapHangDAO.findID(maHang);
    }

    // Thêm
    public boolean addNhapHang(NhapHang nh) {

        if (nh == null) {
            return false;
        }

        if (nh.getMaHang() == null || nh.getMaHang().trim().isEmpty()) {
            return false;
        }

        if (nh.getMaNCC() == null || nh.getMaNCC().trim().isEmpty()) {
            return false;
        }

        if (nh.getNgayNhap() == null) {
            return false;
        }

        if (nh.getTongTien() == null) {
            return false;
        }

        if (nhapHangDAO.exists(nh.getMaHang())) {
            return false;
        }

        return nhapHangDAO.insert(nh);
    }

    // Cập nhật
    public boolean updateNhapHang(NhapHang nh) {

        if (nh == null) {
            return false;
        }

        if (nh.getMaHang() == null || nh.getMaHang().trim().isEmpty()) {
            return false;
        }

        if (!nhapHangDAO.exists(nh.getMaHang())) {
            return false;
        }

        return nhapHangDAO.update(nh);
    }

    // Xóa
    public boolean deleteNhapHang(String maHang) {

        if (maHang == null || maHang.trim().isEmpty()) {
            return false;
        }

        if (!nhapHangDAO.exists(maHang)) {
            return false;
        }

        return nhapHangDAO.delete(maHang);
    }

    // Lọc theo khoảng ngày
    public List<NhapHang> getNhapHangByDate(Date from, Date to) {

        if (from == null || to == null) {
            return null;
        }

        if (from.after(to)) {
            return null;
        }

        return nhapHangDAO.getByDate(from, to);
    }

    // Tìm theo nhà cung cấp
    public List<NhapHang> searchByNCC(String maNCC) {

        if (maNCC == null || maNCC.trim().isEmpty()) {
            return null;
        }

        return nhapHangDAO.searchByNCC(maNCC);
    }

    // Kiểm tra mã tồn tại
    public boolean exists(String maHang) {

        if (maHang == null || maHang.trim().isEmpty()) {
            return false;
        }

        return nhapHangDAO.exists(maHang);
    }

}