package handler;

import CRUD.BanAnDAO;
import java.util.List;
import model.BanAn;

public class BanAnHandler {

    private final BanAnDAO banAnDAO;

    public BanAnHandler() {
        banAnDAO = new BanAnDAO();
    }

    // Lấy tất cả bàn ăn
    public List<BanAn> getAllBanAn() {
        return banAnDAO.getAll();
    }

    // Tìm theo mã bàn
    public BanAn findBanAnByID(String maBan) {

        if (maBan == null || maBan.trim().isEmpty()) {
            return null;
        }

        return banAnDAO.findID(maBan);
    }

    // Thêm bàn ăn
    public boolean addBanAn(BanAn ba) {

        if (ba == null) {
            return false;
        }

        if (ba.getMaBan() == null ||
                ba.getMaBan().trim().isEmpty()) {
            return false;
        }

        if (ba.getTenBan() == null ||
                ba.getTenBan().trim().isEmpty()) {
            return false;
        }

        if (banAnDAO.findID(ba.getMaBan()) != null) {
            return false;
        }

        return banAnDAO.insert(ba);
    }

    // Cập nhật bàn ăn
    public boolean updateBanAn(BanAn ba) {

        if (ba == null) {
            return false;
        }

        if (ba.getMaBan() == null ||
                ba.getMaBan().trim().isEmpty()) {
            return false;
        }

        return banAnDAO.update(ba);
    }

    // Xóa bàn ăn
    public boolean deleteBanAn(String maBan) {

        if (maBan == null ||
                maBan.trim().isEmpty()) {
            return false;
        }

        return banAnDAO.delete(maBan);
    }

    // Tìm kiếm theo tên bàn
    public List<BanAn> searchByName(String keyword) {

        if (keyword == null) {
            keyword = "";
        }

        return banAnDAO.searchByName(keyword);
    }

    // Danh sách bàn trống
    public List<BanAn> getBanTrong() {
        return banAnDAO.getBanTrong();
    }

    // Cập nhật trạng thái bàn
    public boolean updateTrangThai(String maBan,
            String trangThai) {

        if (maBan == null ||
                maBan.trim().isEmpty()) {
            return false;
        }

        if (trangThai == null ||
                trangThai.trim().isEmpty()) {
            return false;
        }

        return banAnDAO.updateTrangThai(
                maBan,
                trangThai);
    }

    public List<BanAn> getFreeTablesInSlot(java.sql.Timestamp timeIn, java.sql.Timestamp timeOut) {
        if (timeIn == null || timeOut == null || !timeIn.before(timeOut)) {
            return List.of();
        }
        return banAnDAO.getFreeTablesInSlot(timeIn, timeOut);
    }
}