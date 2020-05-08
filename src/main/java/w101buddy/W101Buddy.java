package w101buddy;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.stage.Stage;

import javax.imageio.ImageIO;
import javax.swing.BoxLayout;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import javax.swing.text.Document;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.StyleSheet;
import java.awt.Dimension;
import java.awt.GraphicsEnvironment;
import java.awt.Image;
import java.awt.Insets;
import java.awt.MenuItem;
import java.awt.Point;
import java.awt.PopupMenu;
import java.awt.Rectangle;
import java.awt.SystemTray;
import java.awt.TrayIcon;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowEvent;
import java.awt.event.WindowFocusListener;
import java.io.File;
import java.io.IOException;
import java.util.Optional;

public class W101Buddy extends Application {

    private static final int WIDTH = 40;
    private static final int RESULT_LENGTH = 400;

    private Rectangle boundingBox;
    private int windowX;
    private int windowY;

    // initially only the search bar
    private boolean onlySearchBar = true;
    private JFrame window;
    private JTextField searchBar;
    private JEditorPane resultArea;
    private JScrollPane resultScroller;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void init() {
        window = new JFrame();
        window.setAlwaysOnTop(true);
        window.setUndecorated(true);
        window.setLayout(new BoxLayout(window.getContentPane(), BoxLayout.Y_AXIS));
        window.setVisible(false);

        window.addWindowFocusListener(new WindowFocusListener() {
            @Override
            public void windowGainedFocus(WindowEvent e) {}

            @Override
            public void windowLostFocus(WindowEvent e) {
                window.setVisible(false);
            }
        });

        searchBar = new JTextField(WIDTH);
        searchBar.addActionListener(this::performSearch);
        window.add(searchBar);
        window.pack();

        // scrapped from https://alvinalexander.com/blog/post/jfc-swing/how-create-simple-swing-html-viewer-browser-java/
        resultArea = new JEditorPane();
        resultArea.setEditable(false);
        HTMLEditorKit kit = new HTMLEditorKit();
        resultArea.setEditorKit(kit);
        StyleSheet styleSheet = kit.getStyleSheet();
        // styleSheet.addRule() for any rules TODO
        Document styledDocument = kit.createDefaultDocument();
        resultArea.setDocument(styledDocument);
        resultArea.setMargin(new Insets(5, 5, 5, 5));
        resultArea.setBorder(new EmptyBorder(new Insets(10, 10, 10, 10)));
        resultArea.setPreferredSize(new Dimension(WIDTH, RESULT_LENGTH));

        resultScroller = new JScrollPane();
        resultScroller.setViewportView(resultArea);
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
        TrayIcon icon = new TrayIcon(getIconImage(), W101Buddy.class.getSimpleName(), getPopupMenu());
        icon.addMouseListener(getLocationOfIcon());
        return icon;
    }

    private Image getIconImage() {
        File icon = new File("src/main/resources/white_spiral.png");
        try {
            return ImageIO.read(icon);
        } catch (IOException e) {
            System.err.println("Icon not found");
            throw new RuntimeException(e);
        }
    }

    private PopupMenu getPopupMenu() {
        PopupMenu popupMenu = new PopupMenu("W101Buddy");

        MenuItem searchButton = new MenuItem("Search...");
        searchButton.addActionListener(event -> showWindow());
        popupMenu.add(searchButton);

        popupMenu.addSeparator();

        MenuItem quitItem = new MenuItem("Quit");
        quitItem.addActionListener(action -> System.exit(0));
        popupMenu.add(quitItem);
        return popupMenu;
    }

    private void showWindow() {
        int bufferedX = W101Buddy.applyWindowSize(windowX, window.getPreferredSize().width, boundingBox.width);
        int bufferedY = W101Buddy.applyWindowSize(windowY, window.getPreferredSize().height, boundingBox.height);

        System.out.println("Placing window at x=" + bufferedX + " y=" + bufferedY);
        window.setLocation(bufferedX, bufferedY);
        window.setVisible(true);
    }

    private void performSearch(ActionEvent event) {
        String term = event.getActionCommand();
        if (term.isEmpty()) {
            return;
        } else if (onlySearchBar) {
            addResultAreaToWindow();
        }

        String result;
        try {
            Optional<String> wikiResult = W101WikiClient.getWikiPage(term, PageType.Reagent);
            result = wikiResult.orElseGet(() -> W101Buddy.htmlWrap("Wiki page does not exist, did you misspell anything?"));
        } catch (IOException e) {
            result = W101Buddy.htmlWrap("Request to Wizard101 Central failed!\nCheck your internet connection and try again.");
        }
        resultArea.setText(result);
    }

    private void addResultAreaToWindow() {
        window.add(resultScroller);
        window.pack();
        onlySearchBar = false;
    }

    private MouseAdapter getLocationOfIcon() {
        // salvaged most of this from https://stackoverflow.com/questions/51459612/how-to-add-text-field-in-a-pop-up-menu
        return new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                boundingBox = GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds();

                Point clickedPoint = e.getPoint();
                System.out.println("Click at " + clickedPoint.x + "," + clickedPoint.y);

                windowX = applyBounds(clickedPoint.x, boundingBox.x, boundingBox.width);
                windowY = applyBounds(clickedPoint.y, boundingBox.y, boundingBox.height);

                System.out.println("Window coordinates are " + windowX + "," + windowY);
            }

            private int applyBounds(int position, int bound, int length) {
                if (position < bound) {
                    position = bound;
                } else if (position > (bound + length)) {
                    position = bound + length;
                }
                return position;
            }
        };
    }

    private static int applyWindowSize(int position, int windowSize, int max) {
        return position + windowSize > max ? max - windowSize : position;
    }

    private static String htmlWrap(String s) {
        return "<html><body>" + s + "</body></html>";
    }
}
