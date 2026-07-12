package CRUD;

import database.DBConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import model.NhanVien;

public class EmployeeAssignmentService {

    private static final Random random = new Random();

    /**
     * Tìm nhân viên đang làm việc, trong ca hiện tại và không nhận order trong 10 phút gần đây.
     * Sử dụng kết nối có sẵn từ bên ngoài (để đồng bộ Transaction).
     */
    public static NhanVien findAvailableEmployee(Connection conn) throws SQLException {
        String sql = "SELECT DISTINCT c.manv, n.hoten " +
                     "FROM calamviec c " +
                     "JOIN nhanvien n ON c.manv = n.manv " +
                     "WHERE GETDATE() BETWEEN c.giobatdau AND c.gioketthuc " +
                     "  AND NOT EXISTS ( " +
                     "      SELECT 1 " +
                     "      FROM ordermon o " +
                     "      WHERE o.manv = c.manv " +
                     "        AND o.ngaytao >= DATEADD(minute, -10, GETDATE()) " +
                     "  )";

        List<NhanVien> eligibleStaff = new ArrayList<>();

        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                NhanVien nv = new NhanVien();
                nv.setMaNV(rs.getString("manv"));
                nv.setHoTen(rs.getString("hoten"));
                eligibleStaff.add(nv);
            }
        }

        if (eligibleStaff.isEmpty()) {
            return null; // Không có nhân viên nào thỏa mãn
        }

        // Chọn ngẫu nhiên 1 nhân viên từ danh sách hợp lệ
        int index = random.nextInt(eligibleStaff.size());
        return eligibleStaff.get(index);
    }
}
