package ics432.imgapp;

import javafx.event.Event;
import javafx.geometry.Orientation;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Stage;
import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * A class that implements the "Main Window" for the app, which
 * allows the user to add input files for potential filtering and
 * to create an image filtering job
 */
class MainWindow {

    private final Stage primaryStage;
    private final Button quitButton;
    private int pendingJobCount = 0;
    private final FileListWithViewPort fileListWithViewPort;
    private StatisticsWindow statisticsWindow;
    private int jobID = 0;
    private final Slider threadsSlider;
    private final Slider parallelSlider;



    /**
     * Constructor
     *
     * @param primaryStage The primary stage
     */
    MainWindow(Stage primaryStage, int windowWidth, int windowHeight) {

        double buttonPreferredHeight = 27.0;

        // Set up the primaryStage
        this.primaryStage = primaryStage;
        this.primaryStage.setTitle("ICS 432 Image Editing App");

        // Make this primaryStage non-closable
        this.primaryStage.setOnCloseRequest(Event::consume);

        // Create all widgets
        Button addFilesButton = new Button("Add Image Files");
        addFilesButton.setPrefHeight(buttonPreferredHeight);

        Button createJobButton = new Button("Create Job");
        createJobButton.setPrefHeight(buttonPreferredHeight);
        createJobButton.setDisable(true);

        Button viewStatsButton = new Button("View Stats");
        viewStatsButton.setPrefHeight(buttonPreferredHeight);

        this.threadsSlider = new Slider(1, Runtime.getRuntime().availableProcessors(), 1);
        this.threadsSlider.setPrefWidth(300);
        this.threadsSlider.setBlockIncrement(1);
        this.threadsSlider.setMajorTickUnit(1);
        this.threadsSlider.setMinorTickCount(0);
        this.threadsSlider.setShowTickLabels(true);
        this.threadsSlider.setSnapToTicks(true);

        this.parallelSlider = new Slider(1, Runtime.getRuntime().availableProcessors(), 1);
        this.parallelSlider.setPrefWidth(300);
        this.parallelSlider.setBlockIncrement(1);
        this.parallelSlider.setMajorTickUnit(1);
        this.parallelSlider.setMinorTickCount(0);
        this.parallelSlider.setShowTickLabels(true);
        this.parallelSlider.setSnapToTicks(true);
        quitButton = new Button("Quit");
        quitButton.setPrefHeight(buttonPreferredHeight);

        this.fileListWithViewPort = new FileListWithViewPort(
                windowWidth  * 0.98,
                windowHeight - 3 * buttonPreferredHeight - 3 * 5,
                true);

        // Listen for the "nothing is selected" property of the widget
        // to disable the createJobButton dynamically
        this.fileListWithViewPort.addNoSelectionListener(createJobButton::setDisable);

        // Set actions for all widgets
        addFilesButton.setOnAction(e -> addFiles(selectFilesWithChooser()));

        quitButton.setOnAction(e -> {
            // If the button is enabled, it's fine to quit
            this.primaryStage.close();

        });

        createJobButton.setOnAction(e -> {
            this.quitButton.setDisable(true);
            this.threadsSlider.setDisable(true);
            this.parallelSlider.setDisable(true);
            this.pendingJobCount += 1;
            this.jobID += 1;
            JobWindow jw = new JobWindow(
                    (int) (windowWidth * 0.8), (int) (windowHeight * 0.8),
                    this.primaryStage.getX() + 100 + this.pendingJobCount * 10,
                    this.primaryStage.getY() + 50 + this.pendingJobCount * 10,
                    this.jobID,
                    new ArrayList<>(this.fileListWithViewPort.getSelection()));

            jw.addCloseListener(() -> {
                this.pendingJobCount -= 1;
                if (this.pendingJobCount == 0) {
                    this.quitButton.setDisable(false);
                    this.threadsSlider.setDisable(false);
                    this.parallelSlider.setDisable(false);
                }
            });
        });

        this.threadsSlider.valueProperty().addListener((observableValue, oldvalue, newvalue) -> ProducerConsumer.setNumberProcessorThreads(newvalue.intValue()));
        this.parallelSlider.valueProperty().addListener((observableValue, oldvalue, newvalue) -> DPMedianFilter.setNumThreads(newvalue.intValue()));

        viewStatsButton.setOnAction(e -> {

            viewStatsButton.setDisable(true);
            this.statisticsWindow = new StatisticsWindow(
                    350, 120,
                    this.primaryStage.getX() + 100 + this.pendingJobCount * 10,
                    this.primaryStage.getY() + 30 + this.pendingJobCount * 11);

            this.statisticsWindow.addCloseListener(() -> viewStatsButton.setDisable(false));
        });

        //Construct the layout
        VBox layout = new VBox(5);

        layout.getChildren().add(Util.createSeparator(0, 5, Orientation.VERTICAL));

        HBox topRow = new HBox();

        topRow.getChildren().add(addFilesButton);
        topRow.getChildren().add(Util.createSeparator(0, 5, Orientation.VERTICAL));
        topRow.getChildren().add(viewStatsButton);
        topRow.getChildren().add(Util.createSeparator(0, 5, Orientation.VERTICAL));
        topRow.getChildren().add(new Label("#threads"));
        topRow.getChildren().add(Util.createSeparator(0, 5, Orientation.VERTICAL));
        topRow.getChildren().add(this.threadsSlider);
        topRow.getChildren().add(new Label("#Median Threads"));
        topRow.getChildren().add(Util.createSeparator(0, 5, Orientation.VERTICAL));
        topRow.getChildren().add(this.parallelSlider);
        layout.getChildren().add(topRow);

        layout.getChildren().add(Util.createSeparator(0, 5, Orientation.VERTICAL));

        layout.getChildren().add(this.fileListWithViewPort);

        HBox row = new HBox(5);
        row.getChildren().add(createJobButton);
        row.getChildren().add(quitButton);
        layout.getChildren().add(row);

        Scene scene = new Scene(layout, windowWidth, windowHeight);
        this.primaryStage.setScene(scene);
        this.primaryStage.setResizable(false);

        // Make this primaryStage non-closable
        this.primaryStage.setOnCloseRequest(Event::consume);

        //  Show it on  screen.
        this.primaryStage.show();
    }


    /**
     * Method that pops up a file chooser and returns chosen image files
     *
     * @return The list of files
     */
    private List<Path> selectFilesWithChooser() {

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choose Image Files");
        fileChooser.getExtensionFilters().addAll(
                new ExtensionFilter("Jpeg Image Files", "*.jpg", "*.jpeg", "*.JPG", "*.JPEG"));
        List<File>  selectedFiles = fileChooser.showOpenMultipleDialog(this.primaryStage);

        if (selectedFiles == null) {
            return new ArrayList<>();
        } else {
            return selectedFiles.stream().collect(ArrayList::new,
                    (c, e) -> c.add(Paths.get(e.getAbsolutePath())),
                    ArrayList::addAll);
        }
    }

    /**
     * Method that adds files to the list of known files
     *
     * @param files The list of files
     */
    private void addFiles(List<Path> files) {

        if (files != null) {
            this.fileListWithViewPort.addFiles(files);
        }
    }

}