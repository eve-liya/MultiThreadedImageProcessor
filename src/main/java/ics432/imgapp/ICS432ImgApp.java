package ics432.imgapp;

import javafx.application.Application;
import javafx.stage.Screen;
import javafx.stage.Stage;


/**
 * Top-level class that merely defines the JavaFX start() method that pops up
 * the MainWindow window.
 * <p>
 * It is in this class that one may want to add static variables and objects that
 * should be visible to all (most) classes in this application. Remaining aware
 * that "globals" are a bad idea in general.
 */
public class ICS432ImgApp extends Application {

    /**
     * start() JavaFx Method to start the application
     *
     * @param primaryStage The primary stage, off which hang all windows.
     */
    @Override
    public void start(Stage primaryStage) {
        // Determine screen dimensions
        double screenWidth = Screen.getPrimary().getBounds().getWidth();
        double screenHeight = Screen.getPrimary().getBounds().getHeight();

        // Compute appropriate window dimensions
        int width = (int) (0.8 * screenWidth);
        int height = (int) (0.8 * screenHeight);

        // Pop up the main window
        new MainWindow(primaryStage, width, height);
    }
}
