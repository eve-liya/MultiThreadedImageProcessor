package ics432.imgapp;

import javafx.application.Platform;
import javafx.scene.control.Alert;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class ImageProcessThread extends Thread implements Runnable {

    private volatile boolean shouldStop = false;
    private final JobWindow jobWindow;
    private final String filterName;

    ImageProcessThread(JobWindow jobWindow, String filterName) {
        this.jobWindow = jobWindow;
        this.filterName = filterName;
    }

    public void cancel() {
        shouldStop = true;
    }

    @Override
    public void run() {
        long startTime = System.currentTimeMillis();
        Job job = new Job(this.filterName, jobWindow.targetDir, jobWindow.inputFiles);
        // Go through each input file and process it
        int filesProcessed = 0;
        for (Path inputFile : jobWindow.inputFiles) {
            if (shouldStop) {
                break;
            }
            Job.ImgTransformOutcome result = job.processNextImage(inputFile);
            List<Path> toAddToDisplay = new ArrayList<>();
            StringBuilder errorMessage = new StringBuilder();

            if (result.success) {
                toAddToDisplay.add(result.outputFile);
                filesProcessed++;
                int finalFilesProcessed = filesProcessed;
                Platform.runLater(() -> jobWindow.progressBar.setProgress((double) finalFilesProcessed /  jobWindow.inputFiles.size()));
            } else {
                errorMessage.append(result.inputFile.toAbsolutePath()).append(": ").append(result.error.getMessage()).append("\n");
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
            jobWindow.flwvp.addFiles(toAddToDisplay);
            jobWindow.jobStatistics.incrementTotalImages();
        }
        long endTime = System.currentTimeMillis();

        if (!shouldStop) {
            long filesSize = 0;
            for (Path inputFile : jobWindow.inputFiles) {
                filesSize += inputFile.toFile().length();
            }
            jobWindow.jobStatistics.incrementTotalJobs();
            jobWindow.jobStatistics.addFilterStatistic(filterName, (double) filesSize / 1000000, endTime - startTime);
        }
        // Update with the times and re-enable the close button
        Platform.runLater(() -> {
            if (!shouldStop)
                jobWindow.timeLabel.setText("Total time: " + (endTime - startTime) + " ms" + " " +
                        "Read time: " + job.getReadTime() + "ms" + " " +
                        "Write time: " + job.getWriteTime() + "ms" + " " +
                        "Process time: " + job.getProcessTime() + "ms" + " ");
            else
                jobWindow.timeLabel.setText("CANCELED");

            jobWindow.closeButton.setDisable(false);
            jobWindow.cancelButton.setDisable(true);
            jobWindow.progressBar.setVisible(false);
        });
    }
}
