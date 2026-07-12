package handler;

import CRUD.DatBanDAO;
import java.util.List;
import model.DatBan;

public class DatBanHandler {

    private final DatBanDAO datBanDAO;

    public DatBanHandler() {
        datBanDAO = new DatBanDAO();
    }

    public List<DatBan> getAllDatBan() {
        return datBanDAO.getAll();
    }

    public DatBan findDatBanByID(String maDatBan) {

        if (maDatBan == null || maDatBan.trim().isEmpty()) {
            return null;
        }

        return datBanDAO.findID(maDatBan);
    }

    public boolean addDatBan(DatBan db) {

        if (db == null) {
            return false;
        }

        if (db.getMaDatBan() == null || db.getMaDatBan().trim().isEmpty()) {
            return false;
        }

        return datBanDAO.datBanDocQuyen(db);
    }

    public boolean updateDatBan(DatBan db) {

        if (db == null) {
            return false;
        }

        if (db.getMaDatBan() == null ||
                db.getMaDatBan().trim().isEmpty()) {
            return false;
        }

        return datBanDAO.update(db);
    }

    public boolean deleteDatBan(String maDatBan) {

        if (maDatBan == null ||
                maDatBan.trim().isEmpty()) {
            return false;
        }

        return datBanDAO.delete(maDatBan);
    }

    public List<DatBan> findByKhachHang(String maKH) {
        if (maKH == null || maKH.trim().isEmpty()) {
            return null;
        }
        return datBanDAO.findByKhachHang(maKH);
    }

    public List<DatBan> findByBan(String maBan) {
        if (maBan == null || maBan.trim().isEmpty()) {
            return null;
        }
        return datBanDAO.findByBan(maBan);
    }

    public boolean updateTrangThai(String maDatBan, String trangThai) {
        if (maDatBan == null || maDatBan.trim().isEmpty()) {
            return false;
        }
        return datBanDAO.updateTrangThai(maDatBan, trangThai);
    }

    public List<DatBan> search(String query) {
        return datBanDAO.search(query);
    }

    public List<DatBan> filter(String trangThai, java.sql.Timestamp tuNgay, java.sql.Timestamp denNgay) {
        return datBanDAO.filter(trangThai, tuNgay, denNgay);
    }

    // Lấy bản ghi đặt bàn đang sử dụng bàn (trangthai = 'Đã nhận bàn')
    public DatBan findActiveByBan(String maBan) {
        if (maBan == null || maBan.trim().isEmpty()) {
            return null;
        }
        return datBanDAO.findActiveByBan(maBan);
    }

    // Lấy bản ghi đặt bàn đang sử dụng (trangthai = 'Đã nhận bàn') theo mã khách hàng
    public DatBan findActiveByKhachHang(String maKH) {
        if (maKH == null || maKH.trim().isEmpty()) {
            return null;
        }
        return datBanDAO.findActiveByKhachHang(maKH);
    }

    public String getNextMaDatBan() {
        return datBanDAO.getNextMaDatBan();
    }
}
