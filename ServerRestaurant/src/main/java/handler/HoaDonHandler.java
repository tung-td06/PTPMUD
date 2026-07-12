package handler;

import CRUD.HoaDonDAO;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import model.HoaDon;
import network.Response;

public class HoaDonHandler {

    private final HoaDonDAO hoaDonDAO;

    public HoaDonHandler() {
        hoaDonDAO = new HoaDonDAO();
    }

    public List<HoaDon> getAllHoaDon() {
        return hoaDonDAO.getAll();
    }

    public HoaDon findHoaDonByID(String maHD) {

        if (maHD == null ||
                maHD.trim().isEmpty()) {
            return null;
        }

        return hoaDonDAO.findID(maHD);
    }

    public boolean addHoaDon(HoaDon hd) {

        if (hd == null) {
            return false;
        }

        if (hd.getMaHD() == null ||
                hd.getMaHD().trim().isEmpty()) {
            return false;
        }

        if (hoaDonDAO.findID(
                hd.getMaHD()) != null) {
            return false;
        }

        return hoaDonDAO.insert(hd);
    }

    public boolean updateHoaDon(HoaDon hd) {

        if (hd == null) {
            return false;
        }

        if (hd.getMaHD() == null ||
                hd.getMaHD().trim().isEmpty()) {
            return false;
        }

        return hoaDonDAO.update(hd);
    }

    public boolean deleteHoaDon(String maHD) {

        if (maHD == null ||
                maHD.trim().isEmpty()) {
            return false;
        }

        return hoaDonDAO.delete(maHD);
    }

    public List<HoaDon> getByDate(Date from, Date to) {

        if (from == null || to == null) {
            return null;
        }

        if (from.after(to)) {
            return null;
        }

        return hoaDonDAO.getByDate(from, to);
    }

    public List<HoaDon> findByKhachHang(String maKH) {

        if (maKH == null ||
                maKH.trim().isEmpty()) {
            return null;
        }

        return hoaDonDAO.findByKhachHang(maKH);
    }

    public List<HoaDon> findByNhanVien(String maNV) {

        if (maNV == null ||
                maNV.trim().isEmpty()) {
            return null;
        }

        return hoaDonDAO.findByNhanVien(maNV);
    }

    public List<HoaDon> findByBan(String maBan) {

        if (maBan == null ||
                maBan.trim().isEmpty()) {
            return null;
        }

        return hoaDonDAO.findByBan(maBan);
    }

    public List<HoaDon> getHoaDonChuaThanhToan() {
        return hoaDonDAO.getHoaDonChuaThanhToan();
    }

    public boolean updateTrangThai(String maHD, String trangThai) {

        if (maHD == null ||
                maHD.trim().isEmpty()) {
            return false;
        }

        if (trangThai == null ||
                trangThai.trim().isEmpty()) {
            return false;
        }

        return hoaDonDAO.updateTrangThai(
                maHD,
                trangThai);
    }

    public boolean updateTongTien(String maHD, BigDecimal tongTien) {

        if (maHD == null ||
                maHD.trim().isEmpty()) {
            return false;
        }

        if (tongTien == null ||
                tongTien.compareTo(BigDecimal.ZERO) < 0) {
            return false;
        }

        return hoaDonDAO.updateTongTien(
                maHD,
                tongTien);
    }

    public BigDecimal getTongDoanhThu() {
        return hoaDonDAO.getTongDoanhThu();
    }

    public BigDecimal getTongDoanhThu(Date from, Date to) {

        if (from == null || to == null) {
            return BigDecimal.ZERO;
        }

        if (from.after(to)) {
            return BigDecimal.ZERO;
        }

        return hoaDonDAO.getTongDoanhThu(from, to);
    }

    public boolean thanhToan(String maOrder, String maNV, BigDecimal giamGia) {
        if (maOrder == null || maOrder.trim().isEmpty() ||
                maNV == null || maNV.trim().isEmpty() ||
                giamGia == null || giamGia.compareTo(BigDecimal.ZERO) < 0) {
            return false;
        }
        return hoaDonDAO.thanhToan(maOrder, maNV, giamGia);
    }

    public Response customerCheckoutInfo(String maKH) {
        if (maKH == null || maKH.trim().isEmpty()) {
            return Response.error("Mã khách hàng không hợp lệ.");
        }
        return hoaDonDAO.customerCheckoutInfo(maKH);
    }

    public Response customerConfirmCheckout(String maKH, BigDecimal giamGia) {
        return customerConfirmCheckout(maKH, giamGia, "Chuyển khoản", 0);
    }

    public Response customerConfirmCheckout(String maKH, BigDecimal giamGia, String hinhThucThanhToan, int diemTru) {
        if (maKH == null || maKH.trim().isEmpty()) {
            return Response.error("Mã khách hàng không hợp lệ.");
        }
        return hoaDonDAO.customerConfirmCheckout(maKH, giamGia, hinhThucThanhToan, diemTru);
    }
}