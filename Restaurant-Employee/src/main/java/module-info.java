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
    opens customer to javafx.fxml;
    opens shift to javafx.fxml;
    opens table to javafx.fxml;
    opens billdetail to javafx.fxml;
    opens model to javafx.fxml;
    opens profile to javafx.fxml;
    opens network to javafx.fxml;
    opens order to javafx.fxml;

    exports restaurant;
    exports Login;
    exports dashboard;
    exports bill;
    exports booking;
    exports customer;
    exports shift;
    exports table;
    exports billdetail;
    exports model;
    exports profile;
    exports network;
    exports order;
}