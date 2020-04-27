package w101buddy;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.stage.Stage;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;

public class W101Buddy extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        Platform.setImplicitExit(false);
        primaryStage.hide();

        if (!SystemTray.isSupported()) {
            Alert noSystemTray = new Alert(Alert.AlertType.ERROR, "No System Tray support");
            noSystemTray.showAndWait();
            Platform.exit();
        }

        SystemTray systemTray = SystemTray.getSystemTray();
        systemTray.add(buildTrayIcon());
    }

    private TrayIcon buildTrayIcon() {
        return new TrayIcon(getIconImage(), W101Buddy.class.getSimpleName(), getPopupMenu());
    }

    private Image getIconImage() {
        File icon = new File("resources/hat.png"); // https://www.pngfind.com/pngs/m/96-965739_wizard-hat-png-wizard-hat-clipart-black-and.png

        try {
            return ImageIO.read(icon);
        } catch (IOException e) {
            System.err.println("Icon not found");
            throw new RuntimeException(e);
        }
    }

    private PopupMenu getPopupMenu() {
        PopupMenu popupMenu = new PopupMenu("W101Buddy");
        ActionListener quitListener = action -> System.exit(0);
        MenuItem quitItem = new MenuItem("Quit");
        quitItem.addActionListener(quitListener);
        popupMenu.add(quitItem);
        return popupMenu;
    }
}
