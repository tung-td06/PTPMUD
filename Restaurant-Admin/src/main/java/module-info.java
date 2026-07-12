module com.mycompany.restaurant {

    requires transitive javafx.controls;
    requires transitive javafx.graphics;
    requires javafx.fxml;
    requires transitive java.sql;
    requires java.base;

    opens restaurant to javafx.fxml;
    opens Login to javafx.fxml;
    opens dashboard to javafx.fxml;
    opens bill to javafx.fxml;
    opens booking to javafx.fxml;
    opens category to javafx.fxml;
    opens customer to javafx.fxml;
    opens employee to javafx.fxml;
    opens food to javafx.fxml;
    opens importreceipt to javafx.fxml;
    opens shift to javafx.fxml;
    opens supplier to javafx.fxml;
    opens table to javafx.fxml;
    opens warehouse to javafx.fxml;
    opens account to javafx.fxml;
    opens recipe to javafx.fxml;
    opens model to javafx.fxml;
    opens order to javafx.fxml;

    exports restaurant;
    exports Login;
    exports dashboard;
    exports bill;
    exports booking;
    exports category;
    exports customer;
    exports employee;
    exports food;
    exports importreceipt;
    exports shift;
    exports supplier;
    exports table;
    exports warehouse;
    exports account;
    exports recipe;
    exports model;
    exports order;
}