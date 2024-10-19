package ics432.imgapp;

import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.scene.control.Label;

import java.util.HashMap;

class JobStatisticsWindow extends Stage {
    private int totalJobs;
    private int totalImages;
    protected Label totalJobsLabel;
    protected Label totalImagesLabel;

    private final HashMap<String, FilterStatistics> filterStatsMap = new HashMap<>();

    JobStatisticsWindow(String[] filters) {
        this.setTitle("Job Statistics");
        VBox layout = new VBox(5);
        HBox row1 = new HBox(5);
        totalJobsLabel = new Label("Total Jobs: " + totalJobs);
        row1.getChildren().add(totalJobsLabel);
        totalImagesLabel = new Label("Total Images: " + totalImages);
        row1.getChildren().add(totalImagesLabel);
        layout.getChildren().add(row1);

        for (String filter : filters) {
            FilterStatistics tempStatistics = new FilterStatistics(filter);
            HBox row = new HBox(5);
            row.getChildren().add(tempStatistics.fileSizeLabel);
            row.getChildren().add(tempStatistics.processTimeLabel);
            row.getChildren().add(tempStatistics.averageLabel);
            layout.getChildren().add(row);
            filterStatsMap.put(filter, tempStatistics);
        }

        this.setScene(new Scene(layout, 400, 200));
    }

    static class FilterStatistics {
        protected double totalFileSize;
        protected long totalProcessTime;
        protected Label fileSizeLabel;
        protected Label processTimeLabel;
        protected Label averageLabel;
        protected String filterName;

        public FilterStatistics(String filterName) {
            this.totalFileSize = 0;
            this.totalProcessTime = 0;
            this.filterName = filterName;
            this.fileSizeLabel = new Label(filterName + ": " + totalFileSize + " MB");
            this.processTimeLabel = new Label(filterName + ": " + totalProcessTime + " ms");
            this.averageLabel = new Label(filterName + ": 0 MB/s");
        }

        public void addStatistics(double fileSize, long processTime) {
            synchronized (this) {
                this.totalFileSize += fileSize;
                this.totalProcessTime += processTime;
            }
            Platform.runLater(() -> {
                fileSizeLabel.setText(String.format(filterName + ": %.2f MB", totalFileSize));
                processTimeLabel.setText(filterName + ": " + totalProcessTime + " ms");
                averageLabel.setText(String.format(filterName + ": %.2f MB/s", totalFileSize / (totalProcessTime / 1000.0)));
            });
        }
    }

    public void incrementTotalJobs() {
        synchronized (this) {
            this.totalJobs++;
        }
        Platform.runLater(() -> totalJobsLabel.setText("Total Jobs: " + totalJobs));
    }

    public void incrementTotalImages() {
        synchronized (this) {
            this.totalImages++;
        }
        Platform.runLater(() -> totalImagesLabel.setText("Total Images: " + totalImages));
    }

    public void addFilterStatistic(String filterName, double fileSize, long processTime) {
        filterStatsMap.get(filterName).addStatistics(fileSize, processTime);
    }
}
