package handler;

import CRUD.CaLamViecDAO;
import java.util.List;
import model.CaLamViec;

public class CaLamViecHandler {

    private final CaLamViecDAO caLamViecDAO;

    public CaLamViecHandler() {
        caLamViecDAO = new CaLamViecDAO();
    }

    // Lấy tất cả ca làm việc
    public List<CaLamViec> getAllCaLamViec() {
        return caLamViecDAO.getAll();
    }

    // Tìm theo mã ca
    public CaLamViec findCaLamViecByID(String maCa) {

        if(maCa == null || maCa.trim().isEmpty()) {
            return null;
        }

        return caLamViecDAO.findID(maCa);
    }

    // Thêm ca làm việc
    public boolean addCaLamViec(CaLamViec ca) {

        if(ca == null) {
            return false;
        }

        if(ca.getMaCa() == null ||
           ca.getMaCa().trim().isEmpty()) {
            return false;
        }

        if(caLamViecDAO.findID(ca.getMaCa()) != null) {
            return false;
        }

        return caLamViecDAO.insert(ca);
    }

    // Cập nhật ca làm việc
    public boolean updateCaLamViec(CaLamViec ca) {

        if(ca == null) {
            return false;
        }

        if(ca.getMaCa() == null ||
           ca.getMaCa().trim().isEmpty()) {
            return false;
        }

        return caLamViecDAO.update(ca);
    }

    // Xóa ca làm việc
    public boolean deleteCaLamViec(String maCa) {

        if(maCa == null ||
           maCa.trim().isEmpty()) {
            return false;
        }

        return caLamViecDAO.delete(maCa);
    }

    // Tìm ca theo nhân viên
    public List<CaLamViec> findByNhanVien(String maNV) {

        if(maNV == null ||
           maNV.trim().isEmpty()) {
            return null;
        }

        return caLamViecDAO.findByNhanVien(maNV);
    }
}