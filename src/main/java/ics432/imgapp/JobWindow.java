package ics432.imgapp;

import javafx.collections.FXCollections;
import javafx.event.Event;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javafx.scene.control.ProgressBar;


import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

/**
 * A class that implements a "Job Window" on which a user
 * can launch a Job
 */

class JobWindow extends Stage {

    protected Path targetDir;
    protected final List<Path> inputFiles;
    protected final FileListWithViewPort flwvp;
    protected final Button changeDirButton;
    protected final TextField targetDirTextField;
    protected final Button runButton;
    protected final Button closeButton;
    protected final Button cancelButton;
    protected final Label timeLabel;
    protected final ComboBox<String> imgTransformList;
    protected final ProgressBar progressBar;
    protected final JobStatisticsWindow jobStatistics;
    protected int numImagesRam;

    /**
     * Constructor
     *
     * @param windowWidth  The window's width
     * @param windowHeight The window's height
     * @param X            The horizontal position of the job window
     * @param Y            The vertical position of the job window
     * @param id           The id of the job
     * @param inputFiles   The batch of input image files
     */
    JobWindow(int windowWidth, int windowHeight, double X, double Y, int id, List<Path> inputFiles, String[] filters, JobStatisticsWindow jobStatistics) {
        // The  preferred height of buttons
        double buttonPreferredHeight = 27.0;

        // Set up instance variables
        targetDir = Paths.get(inputFiles.getFirst().getParent().toString()); // Same dir as input images
        this.inputFiles = inputFiles;

        // Set up the job statistics
        this.jobStatistics = jobStatistics;

        // Set up the window
        this.setX(X);
        this.setY(Y);
        this.setTitle("Image Transformation Job #" + id);
        this.setResizable(false);

        // Make this window non-closable
        this.setOnCloseRequest(Event::consume);

        // Create all sub-widgets in the window
        Label targetDirLabel = new Label("Target Directory:");
        targetDirLabel.setPrefWidth(115);

        // Create a "change target directory"  button
        this.changeDirButton = new Button("");
        this.changeDirButton.setId("changeDirButton");
        this.changeDirButton.setPrefHeight(buttonPreferredHeight);
        Image image = Util.loadImageFromResourceFile("folder-icon.png");
        ImageView imageView = new ImageView(image);
        imageView.setFitWidth(10);
        imageView.setFitHeight(10);
        this.changeDirButton.setGraphic(imageView);

        // Create a "target directory"  text field
        this.targetDirTextField = new TextField(this.targetDir.toString());
        this.targetDirTextField.setDisable(true);
        HBox.setHgrow(targetDirTextField, Priority.ALWAYS);

        // Create an informative label
        Label transformLabel = new Label("Transformation: ");
        transformLabel.setPrefWidth(115);

        //  Create the pull-down list of image transforms
        this.imgTransformList = new ComboBox<>();
        this.imgTransformList.setId("imgTransformList");  // For TestFX
        this.imgTransformList.setItems(FXCollections.observableArrayList(
                filters
        ));

        this.imgTransformList.getSelectionModel().selectFirst();  //Chooses first imgTransform as default

        // Create a "Run" button
        this.runButton = new Button("Run job (on " + inputFiles.size() + " image" + (inputFiles.size() == 1 ? "" : "s") + ")");
        this.runButton.setId("runJobButton");
        this.runButton.setPrefHeight(buttonPreferredHeight);

        // Create the FileListWithViewPort display
        this.flwvp = new FileListWithViewPort(windowWidth * 0.98, windowHeight - 4 * buttonPreferredHeight - 3 * 5, false);
        this.flwvp.addFiles(inputFiles);

        // Create a "Close" button
        this.closeButton = new Button("Close");
        this.closeButton.setId("closeButton");
        this.closeButton.setPrefHeight(buttonPreferredHeight);

        // Create a "Cancel" button
        this.cancelButton = new Button("Cancel");
        this.cancelButton.setId("cancelButton");
        this.cancelButton.setPrefHeight(buttonPreferredHeight);
        this.cancelButton.setDisable(true);

        this.progressBar = new ProgressBar();
        this.progressBar.setId("progressBar");
        this.progressBar.setPrefWidth(500);
        this.progressBar.setProgress(0.0);
        this.progressBar.setVisible(false);

        // Set actions for all widgets
        this.changeDirButton.setOnAction(e -> {
            DirectoryChooser dirChooser = new DirectoryChooser();
            dirChooser.setTitle("Choose target directory");
            File dir = dirChooser.showDialog(this);
            this.setTargetDir(Paths.get(dir.getAbsolutePath()));
        });

        this.runButton.setOnAction(e -> {
            this.changeDirButton.setDisable(true);
            this.runButton.setDisable(true);
            this.imgTransformList.setDisable(true);
            executeMultiThreadedJob(imgTransformList.getSelectionModel().getSelectedItem());
        });

        this.closeButton.setOnAction(f -> this.close());

        // Build the scene
        VBox layout = new VBox(5);

        HBox row1 = new HBox(5);
        row1.setAlignment(Pos.CENTER_LEFT);
        row1.getChildren().add(targetDirLabel);
        row1.getChildren().add(changeDirButton);
        row1.getChildren().add(targetDirTextField);
        layout.getChildren().add(row1);

        HBox row2 = new HBox(5);
        row2.setAlignment(Pos.CENTER_LEFT);
        row2.getChildren().add(transformLabel);
        row2.getChildren().add(imgTransformList);
        layout.getChildren().add(row2);

        layout.getChildren().add(flwvp);

        timeLabel = new Label("");
        layout.getChildren().add(timeLabel);

        HBox row3 = new HBox(5);
        row3.getChildren().add(runButton);
        row3.getChildren().add(closeButton);
        row3.getChildren().add(cancelButton);
        row3.getChildren().add(progressBar);
        layout.getChildren().add(row3);

        Scene scene = new Scene(layout, windowWidth, windowHeight);

        // Pop up the new window
        this.setScene(scene);
        this.toFront();
        this.show();
    }

    /**
     * Method to add a listener for the "window was closed" event
     *
     * @param listener The listener method
     */
    public void addCloseListener(Runnable listener) {
        this.addEventHandler(WindowEvent.WINDOW_HIDDEN, (event) -> listener.run());
    }

    /**
     * Method to set the target directory
     *
     * @param dir A directory
     */
    private void setTargetDir(Path dir) {
        if (dir != null) {
            this.targetDir = dir;
            this.targetDirTextField.setText(targetDir.toAbsolutePath().toString());
        }
    }

    /**
     * A method to execute the job
     *
     * @param filterName The name of the filter to apply to input images
     */
    private void executeMultiThreadedJob(String filterName) {

        // Clear the display
        this.flwvp.clear();
        this.closeButton.setDisable(true);
        this.progressBar.setVisible(true);
        Thread imgJobThread = new Thread(new ImageJobThread(this, filterName, targetDir, inputFiles));
        imgJobThread.start();
    }

    private void executeJob(String filterName) {
        // Clear the display
        this.flwvp.clear();
        this.closeButton.setDisable(true);
        this.progressBar.setVisible(true);
        SingleThread imgProcessThread = new SingleThread(this, filterName);
        imgProcessThread.start();
    }
}
