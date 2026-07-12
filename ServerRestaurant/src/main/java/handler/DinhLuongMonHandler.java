package handler;

import CRUD.DinhLuongMonDAO;
import java.util.List;
import model.DinhLuongMon;

public class DinhLuongMonHandler {

    private final DinhLuongMonDAO dinhLuongMonDAO;

    public DinhLuongMonHandler() {
        dinhLuongMonDAO = new DinhLuongMonDAO();
    }

    // Lấy tất cả định lượng món
    public List<DinhLuongMon> getAllDinhLuongMon() {
        return dinhLuongMonDAO.getAll();
    }

    // Tìm theo khóa chính kép
    public DinhLuongMon findDinhLuongMon(String maMon,
                                         String maNL) {

        if(maMon == null || maMon.trim().isEmpty()) {
            return null;
        }

        if(maNL == null || maNL.trim().isEmpty()) {
            return null;
        }

        return dinhLuongMonDAO.findID(maMon, maNL);
    }

    // Thêm định lượng
    public boolean addDinhLuongMon(DinhLuongMon dl) {

        if(dl == null) {
            return false;
        }

        if(dl.getMaMon() == null ||
           dl.getMaMon().trim().isEmpty()) {
            return false;
        }

        if(dl.getMaNL() == null ||
           dl.getMaNL().trim().isEmpty()) {
            return false;
        }

        if(dinhLuongMonDAO.findID(
                dl.getMaMon(),
                dl.getMaNL()) != null) {

            return false;
        }

        return dinhLuongMonDAO.insert(dl);
    }

    // Cập nhật định lượng
    public boolean updateDinhLuongMon(DinhLuongMon dl) {

        if(dl == null) {
            return false;
        }

        if(dl.getMaMon() == null ||
           dl.getMaMon().trim().isEmpty()) {
            return false;
        }

        if(dl.getMaNL() == null ||
           dl.getMaNL().trim().isEmpty()) {
            return false;
        }

        return dinhLuongMonDAO.update(dl);
    }

    // Xóa định lượng
    public boolean deleteDinhLuongMon(String maMon,
                                      String maNL) {

        if(maMon == null ||
           maMon.trim().isEmpty()) {
            return false;
        }

        if(maNL == null ||
           maNL.trim().isEmpty()) {
            return false;
        }

        return dinhLuongMonDAO.delete(maMon, maNL);
    }

    // Lấy danh sách nguyên liệu của món ăn
    public List<DinhLuongMon> getByMonAn(String maMon) {

        if(maMon == null ||
           maMon.trim().isEmpty()) {
            return null;
        }

        return dinhLuongMonDAO.getByMonAn(maMon);
    }
}