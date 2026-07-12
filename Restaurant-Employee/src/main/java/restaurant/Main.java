package restaurant;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.io.IOException;

public class Main extends Application {

    private static Scene scene;

    @Override
    public void start(Stage stage) throws IOException {
        Parent root = loadFXML("/fxml/login");

        scene = new Scene(root);

        stage.setTitle("Gourmet Hub - Đăng nhập hệ thống");
        stage.setScene(scene);
        
        // Kích thước chuẩn
        stage.setWidth(1400);
        stage.setHeight(800);
        stage.centerOnScreen();

        // Cấu hình co giãn an toàn
        stage.setResizable(true);
        stage.setMinWidth(1100);
        stage.setMinHeight(700);

        stage.show();
    }

    public static void setRoot(String fxml) throws IOException {
        scene.setRoot(loadFXML(fxml));
    }

    private static Parent loadFXML(String fxml) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource(fxml + ".fxml"));
        return fxmlLoader.load();
    }

    public static void main(String[] args) {
        /*try {
            restaurant.DBConnect.getConnection();
        } catch (Exception e) {
            System.out.println("Kiểm tra lại kết nối Cơ sở dữ liệu.");
        }*/
        launch(args);
    }
}