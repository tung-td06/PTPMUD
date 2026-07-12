package server;

import handler.AccountHandler;
import handler.AccountKHHandler;
import handler.BanAnHandler;
import handler.CaLamViecHandler;
import handler.ChiTietHDHandler;
import handler.ChiTietNHHandler;
import handler.DatBanHandler;
import handler.DinhLuongMonHandler;
import handler.HoaDonHandler;
import handler.KhachHangHandler;
import handler.LoaiMonHandler;
import handler.MonAnHandler;
import handler.NguyenLieuHandler;
import handler.NhaCungCapHandler;
import handler.NhanVienHandler;
import handler.NhapHangHandler;
import handler.DashboardHandler;
import handler.OrderHandler;
import model.Order;
import model.OrderDetail;
import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import model.Account;
import model.AccountKH;
import model.BanAn;
import model.CaLamViec;
import model.ChiTietHD;
import model.ChiTietNH;
import model.DatBan;
import model.DinhLuongMon;
import model.HoaDon;
import model.KhachHang;
import model.LoaiMon;
import model.MonAn;
import model.NguyenLieu;
import model.NhaCungCap;
import model.NhanVien;
import model.NhapHang;
import network.Action;
import network.Module;
import network.Request;
import network.Response;

public class Dispatcher {
    private final AccountHandler accountHandler = new AccountHandler();
    private final AccountKHHandler accountKHHandler = new AccountKHHandler();
    private final NhanVienHandler nhanVienHandler = new NhanVienHandler();
    private final KhachHangHandler khachHangHandler = new KhachHangHandler();
    private final BanAnHandler banAnHandler = new BanAnHandler();
    private final LoaiMonHandler loaiMonHandler = new LoaiMonHandler();
    private final MonAnHandler monAnHandler = new MonAnHandler();
    private final NguyenLieuHandler nguyenLieuHandler = new NguyenLieuHandler();
    private final NhaCungCapHandler nhaCungCapHandler = new NhaCungCapHandler();
    private final HoaDonHandler hoaDonHandler = new HoaDonHandler();
    private final ChiTietHDHandler chiTietHDHandler = new ChiTietHDHandler();
    private final ChiTietNHHandler chiTietNHHandler = new ChiTietNHHandler();
    private final DatBanHandler datBanHandler = new DatBanHandler();
    private final NhapHangHandler nhapHangHandler = new NhapHangHandler();
    private final CaLamViecHandler caLamViecHandler = new CaLamViecHandler();
    private final DinhLuongMonHandler dinhLuongMonHandler = new DinhLuongMonHandler();
    private final DashboardHandler dashboardHandler = new DashboardHandler();
    private final OrderHandler orderHandler = new OrderHandler();

    public Response handle(Request request) {
        if (request == null) {
            return new Response(false, "Request rong", null);
        }

        String module = normalize(request.getModule());
        String action = normalize(request.getAction());

        if (module.isEmpty() || action.isEmpty()) {
            return new Response(false, "Module hoac action rong", null);
        }

        try {
            Object data = dispatch(module, action, request.getData());

            if (data instanceof Response response) {
                return response;
            }

            if (module.equals(Module.ACCOUNT) && action.equals(Action.LOGIN)) {
                if (data instanceof Account account) {
                    return new Response(true, "Đăng nhập thành công", account);
                } else {
                    return new Response(false, "Đăng nhập thất bại. Tài khoản hoặc mật khẩu không chính xác.", null);
                }
            }

            if (data instanceof Boolean success) {
                return new Response(success, success ? "Thanh cong" : "That bai", success);
            }

            if (data == null) {
                return new Response(false, "Khong co du lieu phu hop", null);
            }

            return new Response(true, "Thanh cong", data);
        } catch (IllegalArgumentException e) {
            return new Response(false, e.getMessage(), null);
        } catch (Exception e) {
            e.printStackTrace();
            return new Response(false, "Loi xu ly: " + e.getMessage(), null);
        }
    }

    private Object dispatch(String module, String action, Object data) {
        return switch (module) {
            case Module.ACCOUNT -> handleAccount(action, data);
            case Module.ACCOUNTKH -> handleAccountKH(action, data);
            case Module.NHANVIEN -> handleNhanVien(action, data);
            case Module.KHACHHANG -> handleKhachHang(action, data);
            case Module.BANAN -> handleBanAn(action, data);
            case Module.LOAIMON -> handleLoaiMon(action, data);
            case Module.MONAN -> handleMonAn(action, data);
            case Module.NGUYENLIEU -> handleNguyenLieu(action, data);
            case Module.NHACUNGCAP -> handleNhaCungCap(action, data);
            case Module.HOADON -> handleHoaDon(action, data);
            case Module.CHITIETHD -> handleChiTietHD(action, data);
            case Module.CHITIETNH -> handleChiTietNH(action, data);
            case Module.DATBAN -> handleDatBan(action, data);
            case Module.NHAPHANG -> handleNhapHang(action, data);
            case Module.CALAMVIEC -> handleCaLamViec(action, data);
            case Module.DINHLUONGMON -> handleDinhLuongMon(action, data);
            case Module.DASHBOARD -> handleDashboard(action, data);
            case Module.ORDER -> handleOrder(action, data);
            case Module.CHITIETORDER -> handleOrderDetail(action, data);
            default -> throw new IllegalArgumentException("Module khong ho tro: " + module);
        };
    }

    private Object handleAccount(String action, Object data) {
        return switch (action) {
            case Action.GET_ALL -> accountHandler.getAllAccounts();
            case Action.GET_BY_ID -> accountHandler.findAccountByID(asString(data));
            case Action.ADD -> accountHandler.addAccount(requireType(data, Account.class));
            case Action.UPDATE -> accountHandler.updateAccount(requireType(data, Account.class));
            case Action.DELETE -> accountHandler.deleteAccount(asString(data));
            case Action.LOGIN -> {

                String username = value(data, 0, "username", "tendn");
                String password = value(data, 1, "password", "pwd");

                System.out.println("========== LOGIN REQUEST ==========");
                System.out.println("Username: " + username);
                System.out.println("Password: " + password);

                Account account = accountHandler.login(username, password);

                if (account == null) {
                    System.out.println("LOGIN RESULT: NULL");
                } else {
                    System.out.println("LOGIN RESULT:");
                    System.out.println("MaNV   : " + account.getMaNV());
                    System.out.println("User   : " + account.getTenDN());
                    System.out.println("Quyen  : " + account.getQuyen());
                }

                yield account;
            }
            case "FIND_BY_USERNAME", "GET_BY_USERNAME" -> accountHandler.findByUsername(asString(data));
            case "CHANGE_PASSWORD" -> accountHandler.changePassword(value(data, 0, "manv", "id"),
                    value(data, 1, "newPassword", "password", "pwd"));
            default -> unsupported(action);
        };
    }

    private Object handleAccountKH(String action, Object data) {
        return switch (action) {
            case Action.GET_ALL -> accountKHHandler.getAllAccounts();
            case Action.GET_BY_ID -> accountKHHandler.findAccountByID(asString(data));
            case Action.ADD -> accountKHHandler.addAccount(requireType(data, AccountKH.class));
            case Action.UPDATE -> accountKHHandler.updateAccount(requireType(data, AccountKH.class));
            case Action.DELETE -> accountKHHandler.deleteAccount(asString(data));
            case Action.LOGIN ->
                accountKHHandler.login(value(data, 0, "username", "tendn"), value(data, 1, "password", "pwd"));
            case "FIND_BY_USERNAME", "GET_BY_USERNAME" -> accountKHHandler.findByUsername(asString(data));
            case "CHANGE_PASSWORD" -> accountKHHandler.changePassword(value(data, 0, "makh", "id"),
                    value(data, 1, "newPassword", "password", "pwd"));
            default -> unsupported(action);
        };
    }

    private Object handleNhanVien(String action, Object data) {
        return switch (action) {
            case Action.GET_ALL -> nhanVienHandler.getAll();
            case Action.GET_BY_ID -> nhanVienHandler.findID(asString(data));
            case Action.ADD -> nhanVienHandler.insert(requireType(data, NhanVien.class));
            case Action.UPDATE -> nhanVienHandler.update(requireType(data, NhanVien.class));
            case Action.DELETE -> nhanVienHandler.delete(asString(data));
            case Action.SEARCH -> nhanVienHandler.searchByName(asString(data));
            case "FIND_BY_PHONE", "GET_BY_PHONE" -> nhanVienHandler.findByPhone(asString(data));
            case "FIND_BY_GMAIL", "GET_BY_GMAIL" -> nhanVienHandler.findByGmail(asString(data));
            case "EXISTS" -> nhanVienHandler.exists(asString(data));
            default -> unsupported(action);
        };
    }

    private Object handleKhachHang(String action, Object data) {
        return switch (action) {
            case Action.GET_ALL -> khachHangHandler.getAllKhachHang();
            case Action.GET_BY_ID -> khachHangHandler.findKhachHangByID(asString(data));
            case Action.ADD -> khachHangHandler.addKhachHang(requireType(data, KhachHang.class));
            case Action.UPDATE -> khachHangHandler.updateKhachHang(requireType(data, KhachHang.class));
            case Action.DELETE -> khachHangHandler.deleteKhachHang(asString(data));
            case Action.SEARCH -> khachHangHandler.searchByName(asString(data));
            case "FIND_BY_PHONE", "GET_BY_PHONE" -> khachHangHandler.findByPhone(asString(data));
            case "UPDATE_DIEM_TICH_LUY" ->
                khachHangHandler.updateDiemTichLuy(value(data, 0, "makh", "id"), asInt(data, 1, "diem", "diemtichluy"));
            case "GET_TOP", "GET_TOP_KHACHHANG" -> khachHangHandler.getTopKhachHang();
            default -> unsupported(action);
        };
    }

    private Object handleBanAn(String action, Object data) {
        return switch (action) {
            case Action.GET_ALL -> banAnHandler.getAllBanAn();
            case Action.GET_BY_ID -> banAnHandler.findBanAnByID(asString(data));
            case Action.ADD -> banAnHandler.addBanAn(requireType(data, BanAn.class));
            case Action.UPDATE -> banAnHandler.updateBanAn(requireType(data, BanAn.class));
            case Action.DELETE -> banAnHandler.deleteBanAn(asString(data));
            case Action.SEARCH -> banAnHandler.searchByName(asString(data));
            case "GET_BAN_TRONG" -> banAnHandler.getBanTrong();
            case "GET_FREE_TABLES_IN_SLOT" -> {
                java.util.Date dIn = asDate(data, 0, "timeIn");
                java.util.Date dOut = asDate(data, 1, "timeOut");
                Timestamp tsIn = new Timestamp(dIn.getTime());
                Timestamp tsOut = new Timestamp(dOut.getTime());
                yield banAnHandler.getFreeTablesInSlot(tsIn, tsOut);
            }
            case "UPDATE_TRANG_THAI" ->
                banAnHandler.updateTrangThai(value(data, 0, "maban", "id"), value(data, 1, "trangthai", "status"));
            default -> unsupported(action);
        };
    }

    private Object handleLoaiMon(String action, Object data) {
        return switch (action) {
            case Action.GET_ALL -> loaiMonHandler.getAllLoaiMon();
            case Action.GET_BY_ID -> loaiMonHandler.findLoaiMonByID(asString(data));
            case Action.ADD -> loaiMonHandler.addLoaiMon(requireType(data, LoaiMon.class));
            case Action.UPDATE -> loaiMonHandler.updateLoaiMon(requireType(data, LoaiMon.class));
            case Action.DELETE -> loaiMonHandler.deleteLoaiMon(asString(data));
            case Action.SEARCH -> loaiMonHandler.searchByName(asString(data));
            case "FIND_BY_NAME", "GET_BY_NAME" -> loaiMonHandler.findByName(asString(data));
            case "EXISTS" -> loaiMonHandler.exists(asString(data));
            case "COUNT_MON_AN" -> loaiMonHandler.countMonAnByLoai(asString(data));
            default -> unsupported(action);
        };
    }

    private Object handleMonAn(String action, Object data) {
        return switch (action) {
            case Action.GET_ALL -> monAnHandler.getAll();
            case Action.GET_BY_ID -> monAnHandler.findID(asString(data));
            case Action.ADD -> monAnHandler.insert(requireType(data, MonAn.class));
            case Action.UPDATE -> monAnHandler.update(requireType(data, MonAn.class));
            case Action.DELETE -> monAnHandler.delete(asString(data));
            case Action.SEARCH -> monAnHandler.searchByName(asString(data));
            case "FIND_BY_NAME", "GET_BY_NAME" -> monAnHandler.findByName(asString(data));
            case "GET_BY_LOAI" -> monAnHandler.getByLoai(asString(data));
            case "GET_DANG_BAN" -> monAnHandler.getMonDangBan();
            case "GET_NGUNG_BAN" -> monAnHandler.getMonNgungBan();
            case "UPDATE_TRANG_THAI" ->
                monAnHandler.updateTrangThai(value(data, 0, "mamon", "id"), asBoolean(data, 1, "trangthai", "status"));
            case "UPDATE_GIA" ->
                monAnHandler.updateGia(value(data, 0, "mamon", "id"), asBigDecimal(data, 1, "dongia", "gia"));
            case "EXISTS" -> monAnHandler.exists(asString(data));
            case "COUNT_CHITIETHD" -> monAnHandler.countChiTietHD(asString(data));
            case "COUNT_DINHLUONG" -> monAnHandler.countDinhLuong(asString(data));
            default -> unsupported(action);
        };
    }

    private Object handleNguyenLieu(String action, Object data) {
        return switch (action) {
            case Action.GET_ALL -> nguyenLieuHandler.getAll();
            case Action.GET_BY_ID -> nguyenLieuHandler.findID(asString(data));
            case Action.ADD -> nguyenLieuHandler.insert(requireType(data, NguyenLieu.class));
            case Action.UPDATE -> nguyenLieuHandler.update(requireType(data, NguyenLieu.class));
            case Action.DELETE -> nguyenLieuHandler.delete(asString(data));
            case Action.SEARCH -> nguyenLieuHandler.searchByName(asString(data));
            case "FIND_BY_NAME", "GET_BY_NAME" -> nguyenLieuHandler.findByName(asString(data));
            case "GET_SAP_HET" -> nguyenLieuHandler.getNguyenLieuSapHet();
            case "EXISTS" -> nguyenLieuHandler.exists(asString(data));
            case "UPDATE_SO_LUONG_KHO" -> nguyenLieuHandler.updateSoLuongKho(value(data, 0, "manl", "id"),
                    asInt(data, 1, "soluongkho", "soluong"));
            case "TANG_SO_LUONG_KHO" ->
                nguyenLieuHandler.tangSoLuongKho(value(data, 0, "manl", "id"), asInt(data, 1, "soluong"));
            case "GIAM_SO_LUONG_KHO" ->
                nguyenLieuHandler.giamSoLuongKho(value(data, 0, "manl", "id"), asInt(data, 1, "soluong"));
            case "COUNT_DINHLUONG" -> nguyenLieuHandler.countDinhLuong(asString(data));
            case "COUNT_CHITIETNH" -> nguyenLieuHandler.countChiTietNhap(asString(data));
            default -> unsupported(action);
        };
    }

    private Object handleNhaCungCap(String action, Object data) {
        return switch (action) {
            case Action.GET_ALL -> nhaCungCapHandler.getAll();
            case Action.GET_BY_ID -> nhaCungCapHandler.findID(asString(data));
            case Action.ADD -> nhaCungCapHandler.insert(requireType(data, NhaCungCap.class));
            case Action.UPDATE -> nhaCungCapHandler.update(requireType(data, NhaCungCap.class));
            case Action.DELETE -> nhaCungCapHandler.delete(asString(data));
            case Action.SEARCH -> nhaCungCapHandler.searchByName(asString(data));
            case "FIND_BY_PHONE", "GET_BY_PHONE" -> nhaCungCapHandler.findByPhone(asString(data));
            case "EXISTS" -> nhaCungCapHandler.exists(asString(data));
            default -> unsupported(action);
        };
    }

    private Object handleHoaDon(String action, Object data) {
        return switch (action) {
            case Action.GET_ALL -> hoaDonHandler.getAllHoaDon();
            case Action.GET_BY_ID -> hoaDonHandler.findHoaDonByID(asString(data));
            case Action.ADD -> hoaDonHandler.addHoaDon(requireType(data, HoaDon.class));
            case Action.UPDATE -> hoaDonHandler.updateHoaDon(requireType(data, HoaDon.class));
            case Action.DELETE -> hoaDonHandler.deleteHoaDon(asString(data));
            case "GET_BY_DATE" ->
                hoaDonHandler.getByDate(asDate(data, 0, "from", "tuNgay"), asDate(data, 1, "to", "denNgay"));
            case "GET_BY_KHACHHANG" -> hoaDonHandler.findByKhachHang(asString(data));
            case "GET_BY_NHANVIEN" -> hoaDonHandler.findByNhanVien(asString(data));
            case "GET_BY_BAN" -> hoaDonHandler.findByBan(asString(data));
            case "GET_CHUA_THANH_TOAN" -> hoaDonHandler.getHoaDonChuaThanhToan();
            case "UPDATE_TRANG_THAI" ->
                hoaDonHandler.updateTrangThai(value(data, 0, "mahd", "id"), value(data, 1, "trangthai", "status"));
            case "UPDATE_TONG_TIEN" ->
                hoaDonHandler.updateTongTien(value(data, 0, "mahd", "id"), asBigDecimal(data, 1, "tongtien"));
            case "GET_TONG_DOANH_THU" -> hoaDonHandler.getTongDoanhThu();
            case "GET_TONG_DOANH_THU_BY_DATE" ->
                hoaDonHandler.getTongDoanhThu(asDate(data, 0, "from", "tuNgay"), asDate(data, 1, "to", "denNgay"));
            case "THANH_TOAN" -> hoaDonHandler.thanhToan(value(data, 0, "maorder"), value(data, 1, "manv"),
                    asBigDecimal(data, 2, "giamgia"));
            case "CUSTOMER_GET_CHECKOUT_INFO" -> hoaDonHandler.customerCheckoutInfo(asString(data));
            case "CUSTOMER_CONFIRM_CHECKOUT", "CONFIRMPAYMENTREQUEST" -> {
                List<?> list = requireType(data, List.class);
                String maKH = asString(list.get(0));
                BigDecimal giamGia = asBigDecimal(list, 1, "giamgia");
                String hinhThuc = list.size() > 2 ? asString(list.get(2)) : "Chuyển khoản";
                int diemTru = list.size() > 3 ? asInt(list, 3, "diemtru") : 0;
                yield hoaDonHandler.customerConfirmCheckout(maKH, giamGia, hinhThuc, diemTru);
            }
            default -> unsupported(action);
        };
    }

    private Object handleChiTietHD(String action, Object data) {
        return switch (action) {
            case Action.GET_ALL -> chiTietHDHandler.getAllChiTietHD();
            case Action.GET_BY_ID -> chiTietHDHandler.findChiTietHD(value(data, 0, "mahd"), value(data, 1, "mamon"));
            case Action.ADD -> chiTietHDHandler.addChiTietHD(requireType(data, ChiTietHD.class));
            case Action.UPDATE -> chiTietHDHandler.updateChiTietHD(requireType(data, ChiTietHD.class));
            case Action.DELETE -> chiTietHDHandler.deleteChiTietHD(value(data, 0, "mahd"), value(data, 1, "mamon"));
            case "GET_BY_HOADON" -> chiTietHDHandler.getByHoaDon(asString(data));
            default -> unsupported(action);
        };
    }

    private Object handleChiTietNH(String action, Object data) {
        return switch (action) {
            case Action.GET_ALL -> chiTietNHHandler.getAllChiTietNH();
            case Action.GET_BY_ID -> chiTietNHHandler.findChiTietNH(value(data, 0, "mahang"), value(data, 1, "manl"));
            case Action.ADD -> chiTietNHHandler.addChiTietNH(requireType(data, ChiTietNH.class));
            case Action.UPDATE -> chiTietNHHandler.updateChiTietNH(requireType(data, ChiTietNH.class));
            case Action.DELETE -> chiTietNHHandler.deleteChiTietNH(value(data, 0, "mahang"), value(data, 1, "manl"));
            case "GET_BY_NHAPHANG" -> chiTietNHHandler.getByNhapHang(asString(data));
            default -> unsupported(action);
        };
    }

    private Object handleDatBan(String action, Object data) {
        return switch (action) {
            case "GET_NEXT_ID" -> datBanHandler.getNextMaDatBan();
            case Action.GET_ALL -> datBanHandler.getAllDatBan();
            case Action.GET_BY_ID -> datBanHandler.findDatBanByID(asString(data));
            case Action.ADD -> datBanHandler.addDatBan(requireType(data, DatBan.class));
            case Action.UPDATE -> datBanHandler.updateDatBan(requireType(data, DatBan.class));
            case Action.DELETE -> datBanHandler.deleteDatBan(asString(data));
            case "GET_BY_KHACHHANG" -> datBanHandler.findByKhachHang(asString(data));
            case "GET_BY_BAN" -> datBanHandler.findByBan(asString(data));
            case "GET_ACTIVE_BY_BAN" -> datBanHandler.findActiveByBan(asString(data));
            case "GET_ACTIVE_BY_KHACHHANG" -> datBanHandler.findActiveByKhachHang(asString(data));
            case "UPDATE_TRANG_THAI" -> datBanHandler.updateTrangThai(value(data, 0, "madatban", "id"),
                    value(data, 1, "trangthai", "status"));
            case Action.SEARCH -> datBanHandler.search(asString(data));
            case "FILTER" -> {
                String trangThai = value(data, 0, "trangthai", "status");
                java.util.Date from = asDate(data, 1, "from", "tuNgay");
                java.util.Date to = asDate(data, 2, "to", "denNgay");
                Timestamp tsFrom = from != null ? new Timestamp(from.getTime()) : null;
                Timestamp tsTo = to != null ? new Timestamp(to.getTime()) : null;
                yield datBanHandler.filter(trangThai, tsFrom, tsTo);
            }
            default -> unsupported(action);
        };
    }

    private Object handleNhapHang(String action, Object data) {
        return switch (action) {
            case Action.GET_ALL -> nhapHangHandler.getAllNhapHang();
            case Action.GET_BY_ID -> nhapHangHandler.findNhapHangByID(asString(data));
            case Action.ADD -> nhapHangHandler.addNhapHang(requireType(data, NhapHang.class));
            case Action.UPDATE -> nhapHangHandler.updateNhapHang(requireType(data, NhapHang.class));
            case Action.DELETE -> nhapHangHandler.deleteNhapHang(asString(data));
            case "GET_BY_DATE" ->
                nhapHangHandler.getNhapHangByDate(asDate(data, 0, "from", "tuNgay"), asDate(data, 1, "to", "denNgay"));
            case "GET_BY_NHACUNGCAP", "SEARCH_BY_NCC" -> nhapHangHandler.searchByNCC(asString(data));
            case "EXISTS" -> nhapHangHandler.exists(asString(data));
            default -> unsupported(action);
        };
    }

    private Object handleCaLamViec(String action, Object data) {
        return switch (action) {
            case Action.GET_ALL -> caLamViecHandler.getAllCaLamViec();
            case Action.GET_BY_ID -> caLamViecHandler.findCaLamViecByID(asString(data));
            case Action.ADD -> caLamViecHandler.addCaLamViec(requireType(data, CaLamViec.class));
            case Action.UPDATE -> caLamViecHandler.updateCaLamViec(requireType(data, CaLamViec.class));
            case Action.DELETE -> caLamViecHandler.deleteCaLamViec(asString(data));
            case "GET_BY_NHANVIEN" -> caLamViecHandler.findByNhanVien(asString(data));
            default -> unsupported(action);
        };
    }

    private Object handleDinhLuongMon(String action, Object data) {
        return switch (action) {
            case Action.GET_ALL -> dinhLuongMonHandler.getAllDinhLuongMon();
            case Action.GET_BY_ID ->
                dinhLuongMonHandler.findDinhLuongMon(value(data, 0, "mamon"), value(data, 1, "manl"));
            case Action.ADD -> dinhLuongMonHandler.addDinhLuongMon(requireType(data, DinhLuongMon.class));
            case Action.UPDATE -> dinhLuongMonHandler.updateDinhLuongMon(requireType(data, DinhLuongMon.class));
            case Action.DELETE ->
                dinhLuongMonHandler.deleteDinhLuongMon(value(data, 0, "mamon"), value(data, 1, "manl"));
            case "GET_BY_MONAN" -> dinhLuongMonHandler.getByMonAn(asString(data));
            default -> unsupported(action);
        };
    }

    private static String normalize(String value) {
        return value == null ? "" : value.trim().toUpperCase(Locale.ROOT);
    }

    private static String asString(Object data) {
        return data == null ? null : String.valueOf(data);
    }

    private static String value(Object data, int index, String... keys) {
        Object value = rawValue(data, index, keys);
        return value == null ? null : String.valueOf(value);
    }

    private static Object rawValue(Object data, int index, String... keys) {
        if (data == null) {
            return null;
        }

        if (data instanceof List<?> list) {
            return index < list.size() ? list.get(index) : null;
        }

        if (data instanceof Object[] array) {
            return index < array.length ? array[index] : null;
        }

        if (data instanceof Map<?, ?> map) {
            for (String key : keys) {
                if (map.containsKey(key)) {
                    return map.get(key);
                }

                String normalizedKey = normalize(key);
                for (Map.Entry<?, ?> entry : map.entrySet()) {
                    if (entry.getKey() != null && normalize(String.valueOf(entry.getKey())).equals(normalizedKey)) {
                        return entry.getValue();
                    }
                }
            }
        }

        if (index == 0 && keys.length == 0) {
            return data;
        }

        return null;
    }

    private static int asInt(Object data, int index, String... keys) {
        Object value = rawValue(data, index, keys);

        if (value instanceof Number number) {
            return number.intValue();
        }

        if (value != null) {
            return Integer.parseInt(String.valueOf(value));
        }

        throw new IllegalArgumentException("Thieu gia tri so nguyen");
    }

    private static boolean asBoolean(Object data, int index, String... keys) {
        Object value = rawValue(data, index, keys);

        if (value instanceof Boolean bool) {
            return bool;
        }

        if (value instanceof Number number) {
            return number.intValue() != 0;
        }

        if (value != null) {
            return Boolean.parseBoolean(String.valueOf(value));
        }

        throw new IllegalArgumentException("Thieu gia tri boolean");
    }

    private static BigDecimal asBigDecimal(Object data, int index, String... keys) {
        Object value = rawValue(data, index, keys);

        if (value instanceof BigDecimal bigDecimal) {
            return bigDecimal;
        }

        if (value instanceof Number number) {
            return BigDecimal.valueOf(number.doubleValue());
        }

        if (value != null) {
            return new BigDecimal(String.valueOf(value));
        }

        throw new IllegalArgumentException("Thieu gia tri tien");
    }

    private static java.util.Date asDate(Object data, int index, String... keys) {
        Object value = rawValue(data, index, keys);

        if (value instanceof java.util.Date date) {
            return date;
        }

        if (value instanceof Number number) {
            return new java.util.Date(number.longValue());
        }

        if (value instanceof CharSequence text) {
            String dateText = text.toString().trim();

            try {
                return Date.valueOf(LocalDate.parse(dateText));
            } catch (DateTimeParseException ignored) {
            }

            try {
                return Timestamp.valueOf(LocalDateTime.parse(dateText));
            } catch (DateTimeParseException ignored) {
            }

            try {
                Instant instant = Instant.parse(dateText);
                return java.util.Date.from(instant.atZone(ZoneId.systemDefault()).toInstant());
            } catch (DateTimeParseException ignored) {
                throw new IllegalArgumentException("Ngay khong hop le: " + dateText);
            }
        }

        throw new IllegalArgumentException("Thieu gia tri ngay");
    }

    private static <T> T requireType(Object data, Class<T> type) {
        if (type.isInstance(data)) {
            return type.cast(data);
        }

        throw new IllegalArgumentException("Data phai la " + type.getSimpleName());
    }

    private static Object unsupported(String action) {
        throw new IllegalArgumentException("Action khong ho tro: " + action);
    }

    private Object handleDashboard(String action, Object data) {
        return switch (action) {
            case Action.LOAD -> dashboardHandler.handle(new Request(Module.DASHBOARD, Action.LOAD, data));
            default -> throw new IllegalArgumentException("Action khong ho tro cho Dashboard: " + action);
        };
    }

    private Object handleOrder(String action, Object data) {
        return switch (action) {
            case Action.GET_ALL -> orderHandler.getAllOrders();
            case Action.GET_BY_ID -> orderHandler.getDetails(asString(data));
            case Action.ADD -> {
                List<?> list = requireType(data, List.class);
                Order order = requireType(list.get(0), Order.class);
                List<OrderDetail> details = (List<OrderDetail>) list.get(1);
                yield orderHandler.addOrder(order, details);
            }
            case Action.UPDATE -> {
                List<?> list = requireType(data, List.class);
                Order order = requireType(list.get(0), Order.class);
                List<OrderDetail> details = (List<OrderDetail>) list.get(1);
                yield orderHandler.updateOrder(order, details);
            }
            case Action.DELETE -> orderHandler.deleteOrder(asString(data));
            case "EXISTS" -> orderHandler.existsOrder(asString(data));
            case "CUSTOMER_ADD_ORDER" -> {
                List<?> list = requireType(data, List.class);
                String maKH = asString(list.get(0));
                String maBan = asString(list.get(1));
                List<OrderDetail> details = (List<OrderDetail>) list.get(2);
                yield orderHandler.customerAddOrder(maKH, maBan, details);
            }
            case "GET_VALID_ORDERS_FOR_CHECKOUT" -> {
                yield orderHandler.getValidOrdersForCheckout(asString(data));
            }
            default -> unsupported(action);
        };
    }

    private Object handleOrderDetail(String action, Object data) {
        return switch (action) {
            case Action.GET_ALL -> {
                yield orderHandler.getDetails(asString(data));
            }
            default -> unsupported(action);
        };
    }
}
