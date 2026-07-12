package network;

/**
 * Danh sách các command dùng chung giữa Client và Server.
 */
public final class Command {

    private Command() {
        // Không cho khởi tạo
    }

    // =========================
    // Authentication
    // =========================
    public static final String LOGIN = "LOGIN";
    public static final String LOGOUT = "LOGOUT";
    public static final String CHANGE_PASSWORD = "CHANGE_PASSWORD";

    // =========================
    // Dashboard
    // =========================
    public static final String DASHBOARD = "DASHBOARD";

    // =========================
    // Account
    // =========================
    public static final String ACCOUNT_GET_ALL = "ACCOUNT_GET_ALL";
    public static final String ACCOUNT_GET_BY_ID = "ACCOUNT_GET_BY_ID";
    public static final String ACCOUNT_SEARCH = "ACCOUNT_SEARCH";
    public static final String ACCOUNT_ADD = "ACCOUNT_ADD";
    public static final String ACCOUNT_UPDATE = "ACCOUNT_UPDATE";
    public static final String ACCOUNT_DELETE = "ACCOUNT_DELETE";

    // =========================
    // Employee (Nhân viên)
    // =========================
    public static final String EMPLOYEE_GET_ALL = "EMPLOYEE_GET_ALL";
    public static final String EMPLOYEE_GET_BY_ID = "EMPLOYEE_GET_BY_ID";
    public static final String EMPLOYEE_SEARCH = "EMPLOYEE_SEARCH";
    public static final String EMPLOYEE_ADD = "EMPLOYEE_ADD";
    public static final String EMPLOYEE_UPDATE = "EMPLOYEE_UPDATE";
    public static final String EMPLOYEE_DELETE = "EMPLOYEE_DELETE";

    // =========================
    // Customer (Khách hàng)
    // =========================
    public static final String CUSTOMER_GET_ALL = "CUSTOMER_GET_ALL";
    public static final String CUSTOMER_GET_BY_ID = "CUSTOMER_GET_BY_ID";
    public static final String CUSTOMER_SEARCH = "CUSTOMER_SEARCH";
    public static final String CUSTOMER_ADD = "CUSTOMER_ADD";
    public static final String CUSTOMER_UPDATE = "CUSTOMER_UPDATE";
    public static final String CUSTOMER_DELETE = "CUSTOMER_DELETE";

    // =========================
    // Table (Bàn ăn)
    // =========================
    public static final String TABLE_GET_ALL = "TABLE_GET_ALL";
    public static final String TABLE_GET_BY_ID = "TABLE_GET_BY_ID";
    public static final String TABLE_SEARCH = "TABLE_SEARCH";
    public static final String TABLE_ADD = "TABLE_ADD";
    public static final String TABLE_UPDATE = "TABLE_UPDATE";
    public static final String TABLE_DELETE = "TABLE_DELETE";

    // =========================
    // Category (Loại món)
    // =========================
    public static final String CATEGORY_GET_ALL = "CATEGORY_GET_ALL";
    public static final String CATEGORY_GET_BY_ID = "CATEGORY_GET_BY_ID";
    public static final String CATEGORY_SEARCH = "CATEGORY_SEARCH";
    public static final String CATEGORY_ADD = "CATEGORY_ADD";
    public static final String CATEGORY_UPDATE = "CATEGORY_UPDATE";
    public static final String CATEGORY_DELETE = "CATEGORY_DELETE";

    // =========================
    // Food (Món ăn)
    // =========================
    public static final String FOOD_GET_ALL = "FOOD_GET_ALL";
    public static final String FOOD_GET_BY_ID = "FOOD_GET_BY_ID";
    public static final String FOOD_SEARCH = "FOOD_SEARCH";
    public static final String FOOD_ADD = "FOOD_ADD";
    public static final String FOOD_UPDATE = "FOOD_UPDATE";
    public static final String FOOD_DELETE = "FOOD_DELETE";

    // =========================
    // Booking (Đặt bàn)
    // =========================
    public static final String BOOKING_GET_ALL = "BOOKING_GET_ALL";
    public static final String BOOKING_GET_BY_ID = "BOOKING_GET_BY_ID";
    public static final String BOOKING_SEARCH = "BOOKING_SEARCH";
    public static final String BOOKING_ADD = "BOOKING_ADD";
    public static final String BOOKING_UPDATE = "BOOKING_UPDATE";
    public static final String BOOKING_DELETE = "BOOKING_DELETE";

    // =========================
    // Bill (Hóa đơn)
    // =========================
    public static final String BILL_GET_ALL = "BILL_GET_ALL";
    public static final String BILL_GET_BY_ID = "BILL_GET_BY_ID";
    public static final String BILL_SEARCH = "BILL_SEARCH";
    public static final String BILL_ADD = "BILL_ADD";
    public static final String BILL_UPDATE = "BILL_UPDATE";
    public static final String BILL_DELETE = "BILL_DELETE";

    // =========================
    // Supplier (Nhà cung cấp)
    // =========================
    public static final String SUPPLIER_GET_ALL = "SUPPLIER_GET_ALL";
    public static final String SUPPLIER_GET_BY_ID = "SUPPLIER_GET_BY_ID";
    public static final String SUPPLIER_SEARCH = "SUPPLIER_SEARCH";
    public static final String SUPPLIER_ADD = "SUPPLIER_ADD";
    public static final String SUPPLIER_UPDATE = "SUPPLIER_UPDATE";
    public static final String SUPPLIER_DELETE = "SUPPLIER_DELETE";

    // =========================
    // Warehouse (Kho)
    // =========================
    public static final String WAREHOUSE_GET_ALL = "WAREHOUSE_GET_ALL";
    public static final String WAREHOUSE_GET_BY_ID = "WAREHOUSE_GET_BY_ID";
    public static final String WAREHOUSE_SEARCH = "WAREHOUSE_SEARCH";
    public static final String WAREHOUSE_ADD = "WAREHOUSE_ADD";
    public static final String WAREHOUSE_UPDATE = "WAREHOUSE_UPDATE";
    public static final String WAREHOUSE_DELETE = "WAREHOUSE_DELETE";

    // =========================
    // Import Receipt (Phiếu nhập)
    // =========================
    public static final String IMPORT_RECEIPT_GET_ALL = "IMPORT_RECEIPT_GET_ALL";
    public static final String IMPORT_RECEIPT_GET_BY_ID = "IMPORT_RECEIPT_GET_BY_ID";
    public static final String IMPORT_RECEIPT_SEARCH = "IMPORT_RECEIPT_SEARCH";
    public static final String IMPORT_RECEIPT_ADD = "IMPORT_RECEIPT_ADD";
    public static final String IMPORT_RECEIPT_UPDATE = "IMPORT_RECEIPT_UPDATE";
    public static final String IMPORT_RECEIPT_DELETE = "IMPORT_RECEIPT_DELETE";

    // =========================
    // Shift (Ca làm)
    // =========================
    public static final String SHIFT_GET_ALL = "SHIFT_GET_ALL";
    public static final String SHIFT_GET_BY_ID = "SHIFT_GET_BY_ID";
    public static final String SHIFT_SEARCH = "SHIFT_SEARCH";
    public static final String SHIFT_ADD = "SHIFT_ADD";
    public static final String SHIFT_UPDATE = "SHIFT_UPDATE";
    public static final String SHIFT_DELETE = "SHIFT_DELETE";
}