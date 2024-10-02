package ics432.imgapp;

import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.scene.control.Label;

class JobStatisticsWindow extends Stage {
    protected int totalJobs;
    protected int totalImages;
    protected FilterStatistics invertStatistics = new FilterStatistics("Invert");
    protected FilterStatistics solarizeStatistics = new FilterStatistics("Solarize");
    protected FilterStatistics oilStatistics = new FilterStatistics("Oil4");

    protected Label totalJobsLabel;
    protected Label totalImagesLabel;

    protected Label invertFileSizeLabel;
    protected Label solarizeFileSizeLabel;
    protected Label oilFileSizeLabel;

    protected Label invertProcessTimeLabel;
    protected Label solarizeProcessTimeLabel;
    protected Label oilProcessTimeLabel;

    protected Label invertAverageLabel;
    protected Label solarizeAverageLabel;
    protected Label oilAverageLabel;




    JobStatisticsWindow() {
        this.setTitle("Job Statistics");
        VBox layout = new VBox(5);
        HBox row1 = new HBox(5);
        totalJobsLabel = new Label("Total Jobs: " + totalJobs);
        row1.getChildren().add(totalJobsLabel);
        totalImagesLabel = new Label("Total Images: " + totalImages);
        row1.getChildren().add(totalImagesLabel);
        layout.getChildren().add(row1);

        HBox row2 = new HBox(5);
        invertFileSizeLabel = new Label("Invert: " + invertStatistics.totalFileSize + " MB");
        row2.getChildren().add(invertFileSizeLabel);
        solarizeFileSizeLabel = new Label("Solarize: " + solarizeStatistics.totalFileSize + " MB");
        row2.getChildren().add(solarizeFileSizeLabel);
        oilFileSizeLabel = new Label("Oil4: " + oilStatistics.totalFileSize + " MB");
        row2.getChildren().add(oilFileSizeLabel);
        layout.getChildren().add(row2);

        HBox row3 = new HBox(5);
        invertProcessTimeLabel = new Label("Invert: " + invertStatistics.totalProcessTime + " ms");
        row3.getChildren().add(invertProcessTimeLabel);
        solarizeProcessTimeLabel = new Label("Solarize: " + solarizeStatistics.totalProcessTime + " ms");
        row3.getChildren().add(solarizeProcessTimeLabel);
        oilProcessTimeLabel = new Label("Oil4: " + oilStatistics.totalProcessTime + " ms");
        row3.getChildren().add(oilProcessTimeLabel);
        layout.getChildren().add(row3);

        HBox row4 = new HBox(5);
        invertAverageLabel = new Label("Invert: " + 0 + " MB/ms");
        row4.getChildren().add(invertAverageLabel);
        solarizeAverageLabel = new Label("Solarize: " + 0 + " MB/ms");
        row4.getChildren().add(solarizeAverageLabel);
        oilAverageLabel = new Label("Oil4: " + 0 + " MB/ms");
        row4.getChildren().add(oilAverageLabel);
        layout.getChildren().add(row4);

        this.setScene(new Scene(layout, 400, 200));
    }

    class FilterStatistics {
        protected double totalFileSize;
        protected long totalProcessTime;
        protected String filterName;

        public FilterStatistics(String filterName) {
            this.totalFileSize = 0;
            this.totalProcessTime = 0;
            this.filterName = filterName;
        }

        public void addStatistics(double fileSize, long processTime) {
            synchronized (this) {
                this.totalFileSize += fileSize;
                this.totalProcessTime += processTime;
            }
            Platform.runLater(() -> {
                switch (filterName) {
                    case "Invert":
                        invertFileSizeLabel.setText(String.format("Invert: %.2f MB", totalFileSize));
                        invertProcessTimeLabel.setText("Invert: " + totalProcessTime + " ms");
                        invertAverageLabel.setText(String.format("Invert: %.2f MB/s", totalFileSize / (totalProcessTime / 1000.0)));
                        break;
                    case "Solarize":
                        solarizeFileSizeLabel.setText(String.format("Solarize: %.2f MB", totalFileSize));
                        solarizeProcessTimeLabel.setText("Solarize: " + totalProcessTime + " ms");
                        solarizeAverageLabel.setText(String.format("Solarize: %.2f MB/s", totalFileSize / (totalProcessTime / 1000.0)));
                        break;
                    case "Oil4":
                        oilFileSizeLabel.setText(String.format("Oil4: %.2f MB", totalFileSize));
                        oilProcessTimeLabel.setText("Oil4: " + totalProcessTime + " ms");
                        oilAverageLabel.setText(String.format("Oil4: %.2f MB/s", totalFileSize / (totalProcessTime / 1000.0)));
                        break;
                }
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
        switch (filterName) {
            case "Invert":
                invertStatistics.addStatistics(fileSize, processTime);
                break;
            case "Solarize":
                solarizeStatistics.addStatistics(fileSize, processTime);
                break;
            case "Oil4":
                oilStatistics.addStatistics(fileSize, processTime);
                break;
        }
    }
}
