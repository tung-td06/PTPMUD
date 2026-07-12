package customer.controller.component;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.DialogPane;
import java.util.Optional;

public class Dialogs {

    public static void showSuccess(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        styleDialog(alert, "dialog-success");
        alert.showAndWait();
    }

    public static void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        styleDialog(alert, "dialog-error");
        alert.showAndWait();
    }

    public static boolean showConfirm(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        styleDialog(alert, "dialog-confirm");
        Optional<ButtonType> result = alert.showAndWait();
        return result.isPresent() && result.get() == ButtonType.OK;
    }

    private static void styleDialog(Alert alert, String styleClass) {
        DialogPane dialogPane = alert.getDialogPane();
        try {
            String commonCss = Dialogs.class.getResource("/css/customer/common.css").toExternalForm();
            String componentCss = Dialogs.class.getResource("/css/customer/component.css").toExternalForm();
            dialogPane.getStylesheets().addAll(commonCss, componentCss);
        } catch (Exception e) {
            // Ignore stylesheet loading issues if resources are resolving differently
        }
        dialogPane.getStyleClass().addAll("dialog-card", styleClass);
    }
}
