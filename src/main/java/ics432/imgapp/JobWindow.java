package ics432.imgapp;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.event.Event;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * A class that implements a "Job Window" on which a user
 * can launch a Job
 */

class JobWindow extends Stage {

    Path targetDir;
    final List<Path> inputFiles;
    final FileListWithViewPort flwvp;
    private final Button changeDirButton;
    private final TextField targetDirTextField;
    private final Button runButton;
    final Button cancelButton;
    final Button closeButton;
    final ComboBox<String> imgTransformList;
    final Label runtimeInformationLabel;

    private JobThread  runningJobThread = null;
    private final ProgressArea progressArea;


    /**
     * Constructor
     *
     * @param windowWidth    The window's width
     * @param windowHeight   The window's height
     * @param X              The horizontal position of the job window
     * @param Y              The vertical position of the job window
     * @param id             The id of the job
     * @param inputFiles     The batch of input image files
     */
    JobWindow(int windowWidth, int windowHeight, double X, double Y, int id,
              List<Path> inputFiles) {

        // The  preferred height of buttons
        double buttonPreferredHeight = 27.0;

        // The preferred height of separators
        double separatorPreferredHeight = 5;

        // Set up instance variables
        targetDir = Paths.get(inputFiles.get(0).getParent().toString()); // Same dir as input images
        this.inputFiles = inputFiles;

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
        this.changeDirButton.setPrefHeight(buttonPreferredHeight);
        Image image = Util.loadImageFromResourceFile("folder-icon.png");
        ImageView imageView = new ImageView(image);
        imageView.setFitWidth(10);
        imageView.setFitHeight(10);
        this.changeDirButton.setGraphic(imageView);

        // Create a "target directory" text field
        this.targetDirTextField = new TextField(this.targetDir.toString());
        this.targetDirTextField.setDisable(true);
        HBox.setHgrow(targetDirTextField, Priority.ALWAYS);

        // Create an informative label
        Label transformLabel = new Label("Transformation: ");
        transformLabel.setPrefWidth(115);

        //  Create the pulldown list of image transforms
        this.imgTransformList = new ComboBox<>();

        this.imgTransformList.setItems(FXCollections.observableArrayList(
                ICS432ImgApp.filterNames
        ));

        this.imgTransformList.getSelectionModel().selectFirst();  //Chooses first imgTransform as default

        // Create a "Run" button
        this.runButton = new Button("Run job (on " + inputFiles.size() + " image" + (inputFiles.size() == 1 ? "" : "s") + ")");
        this.runButton.setPrefHeight(buttonPreferredHeight);

        // Create the FileListWithViewPort display
        this.flwvp = new FileListWithViewPort(windowWidth * 0.98, windowHeight - 4 * buttonPreferredHeight - 3 * 5, false);
        this.flwvp.addFiles(inputFiles);

        // Create a "Close" button
        this.closeButton = new Button("Close");
        this.closeButton.setPrefHeight(buttonPreferredHeight);

        // Create a "Cancel" button
        this.cancelButton = new Button("Cancel");
        this.cancelButton.setPrefHeight(buttonPreferredHeight);
        this.cancelButton.setDisable(true);

        // Create the runtimeInformation label
        this.runtimeInformationLabel = new Label("");
        this.runtimeInformationLabel.setFont(new Font(15));

        // Set actions for all widgets
        this.changeDirButton.setOnAction(e -> {
            DirectoryChooser dirChooser = new DirectoryChooser();
            dirChooser.setTitle("Choose target directory");
            File dir = dirChooser.showDialog(this);
            this.setTargetDir(Paths.get(dir.getAbsolutePath()));
        });

        this.runButton.setOnAction(e -> {
            this.closeButton.setDisable(true);
            this.changeDirButton.setDisable(true);
            this.runButton.setDisable(true);
            this.imgTransformList.setDisable(true);

            startJob();
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

        HBox row3 = new HBox(5);
        row3.getChildren().add(runButton);
        row3.getChildren().add(Util.createSeparator(0, separatorPreferredHeight, Orientation.VERTICAL));
        row3.getChildren().add(closeButton);
        row3.getChildren().add(Util.createSeparator(0, separatorPreferredHeight, Orientation.VERTICAL));
        row3.getChildren().add(cancelButton);
        row3.getChildren().add(Util.createSeparator(0, separatorPreferredHeight, Orientation.VERTICAL));
        row3.getChildren().add(runtimeInformationLabel);

        this.progressArea = new ProgressArea(5);
        row3.getChildren().add(this.progressArea);

        layout.getChildren().add(row3);

        Scene scene = new Scene(layout, windowWidth, windowHeight);

        // Pop up the new window
        this.setScene(scene);
        this.toFront();
        this.show();
    }


    private void startJob() {
        // Clear the display
        this.flwvp.clear();

        // Create the Job
        Job job = new Job(
                this.imgTransformList.getSelectionModel().getSelectedItem(),
                this.targetDir,
                this.inputFiles,
                this);

        this.runningJobThread = new JobThread(job);
        this.runningJobThread.start();
    }

    /**
     * Nested helper class
     */
    private static class ProgressArea extends HBox {

        final ProgressBar progressBar;
        final Label textLabel;

        public ProgressArea(int spacing) {
            super(spacing);
            this.setAlignment(Pos.CENTER_LEFT);

            this.progressBar = new ProgressBar(0.0);
            this.textLabel = new Label("");
            this.textLabel.setFont(new Font(15));
            this.textLabel.setFont(Font.font("Arial", FontWeight.BOLD, 15));
        }

        public void updateProgressBar(double progress) {
            Platform.runLater(() -> {
                this.getChildren().remove(this.textLabel);
                if (!this.getChildren().contains(this.progressBar)) {
                    this.getChildren().add(progressBar);
                }
                this.progressBar.progressProperty().setValue(progress);
            });
        }

        public void showText(String text) {
            Platform.runLater(() -> {
                this.getChildren().remove(this.progressBar);
                this.textLabel.setText("  " + text);
                this.getChildren().add(this.textLabel);
            });
        }
    }


    /**
     * Method to update job progress
     */
    public void updateProgress(double progress) {
        this.progressArea.updateProgressBar(progress);
    }

    /**
     * Method to update the display upon image processed successfully
     *
     * @param outputFile The path to the output file
     */
    public void updateDisplayAfterImgProcessed(Path outputFile) {
        this.flwvp.addFile(outputFile);
    }

    /**
     * Method to update the display upon job completion
     *
     * @param wasCancelled: true if the job was canceled
     * @param outcomes:     Job outcomes
     */
    public void updateDisplayAfterJobCompletion(boolean wasCancelled,
                                                List<ImgTransformOutcome> outcomes,
                                                Job.Profile profile) {
        String textToShow;

        if (!wasCancelled) {
            textToShow =
                    "Total Time: " + String.format("%.2f sec", profile.totalExecutionTime) + " | " +
                            "Read Time: " + String.format("%.2f sec", profile.readTime) + " | " +
                            "Write Time: " + String.format("%.2f sec", profile.writeTime) + " | " +
                            "Processing Time: " + String.format("%.2f sec", profile.processingTime);
        } else {
            textToShow = "CANCELED";
        }

        this.progressArea.showText(textToShow);

        // Process the outcome
        List<Path> toAddToDisplay = new ArrayList<>();

        StringBuilder errorMessage = new StringBuilder();
        for (ImgTransformOutcome o : outcomes) {
            if (o.success) {
                toAddToDisplay.add(o.outputFile);
            } else {
                errorMessage.append(o.inputFile.toAbsolutePath()).append(": ").append(o.error.getMessage()).append("\n");
            }
        }

        // Pop up error dialog if needed
        if (!errorMessage.toString().isEmpty()) {
            Platform.runLater(() -> {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("ImgTransform Job Error");
                alert.setHeaderText(null);
                alert.setContentText(errorMessage.toString());
                alert.showAndWait();
            });
        }

        // Update the viewport
        this.flwvp.addFiles(toAddToDisplay);
        this.closeButton.setDisable(false);
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
     * Nested class
     */
    private static class JobThread extends Thread {

        private final Job job;

        public JobThread(Job job) {
            this.job = job;
        }

        public void cancel() {
            this.job.isCanceled = true;
        }

        @Override
        public void run() {
            this.job.execute();
        }

    }
}