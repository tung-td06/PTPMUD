package handler;

import CRUD.LoaiMonDAO;
import java.util.List;
import model.LoaiMon;

public class LoaiMonHandler {

    private final LoaiMonDAO loaiMonDAO;

    public LoaiMonHandler() {
        loaiMonDAO = new LoaiMonDAO();
    }

    public List<LoaiMon> getAllLoaiMon() {
        return loaiMonDAO.getAll();
    }

    public LoaiMon findLoaiMonByID(String maLoai) {

        if(maLoai == null ||
           maLoai.trim().isEmpty()) {
            return null;
        }

        return loaiMonDAO.findID(maLoai);
    }

    public LoaiMon findByName(String tenLoai) {

        if(tenLoai == null ||
           tenLoai.trim().isEmpty()) {
            return null;
        }

        return loaiMonDAO.findByName(tenLoai);
    }

    public boolean addLoaiMon(LoaiMon lm) {

        if(lm == null) {
            return false;
        }

        if(lm.getMaLoai() == null ||
           lm.getMaLoai().trim().isEmpty()) {
            return false;
        }

        if(loaiMonDAO.exists(
                 lm.getMaLoai())) {
            return false;
        }

        return loaiMonDAO.insert(lm);
    }

    public boolean updateLoaiMon(LoaiMon lm) {

        if(lm == null) {
            return false;
        }

        if(lm.getMaLoai() == null ||
           lm.getMaLoai().trim().isEmpty()) {
            return false;
        }

        return loaiMonDAO.update(lm);
    }

    public boolean deleteLoaiMon(String maLoai) {

        if(maLoai == null ||
           maLoai.trim().isEmpty()) {
            return false;
        }

        if(loaiMonDAO.countMonAnByLoai(maLoai) > 0) {
            return false;
        }

        return loaiMonDAO.delete(maLoai);
    }

    public List<LoaiMon> searchByName(String keyword) {
        return loaiMonDAO.searchByName(keyword);
    }

    public boolean exists(String maLoai) {
        return loaiMonDAO.exists(maLoai);
    }

    public int countMonAnByLoai(String maLoai) {
        return loaiMonDAO.countMonAnByLoai(maLoai);
    }
}