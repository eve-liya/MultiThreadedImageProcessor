package ics432.imgapp;

import javafx.application.Platform;

import javax.imageio.ImageIO;
import javax.imageio.stream.ImageOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.Collections;

import static javax.imageio.ImageIO.createImageOutputStream;

public class ImageWriterThread implements Runnable {
    private final Path targetDir;
    private final ProducerConsumerBuffer<ImageUnit> buffer;
    private final JobWindow jobWindow;
    private final ImageJobThread.timer timer;
    private final JobStatisticsWindow statisticsWindow;

    ImageWriterThread(ProducerConsumerBuffer<ImageUnit> buffer, Path targetDir,
                      JobWindow jobWindow, ImageJobThread.timer timer, JobStatisticsWindow statisticsWindow) {
        this.buffer = buffer;
        this.targetDir = targetDir;
        this.jobWindow = jobWindow;
        this.timer = timer;
        this.statisticsWindow = statisticsWindow;
    }

    @Override
    public void run() {
        int imagesProcessed = 0;
        ImageUnit imageUnit = buffer.consume();
        while (!imageUnit.last) {
            long startTime = System.currentTimeMillis();
            long endTime;
            String outputPath = this.targetDir + FileSystems.getDefault().getSeparator() + imageUnit.filterName + "_" + imageUnit.inputFile.getFileName();
            try {
                OutputStream os = new FileOutputStream(outputPath);
                ImageOutputStream outputStream = createImageOutputStream(os);
                ImageIO.write(imageUnit.filteredImage, "jpg", outputStream);
                endTime = System.currentTimeMillis();
                timer.writeTime += endTime - startTime;
                imagesProcessed++;
            } catch (IOException | NullPointerException e) {
                throw new RuntimeException("Error while writing to " + outputPath);
            }
            int finalImagesProcessed = imagesProcessed;
            Platform.runLater(() -> {
                jobWindow.flwvp.addFiles(Collections.singletonList(Path.of(outputPath)));
                jobWindow.progressBar.setProgress((double) finalImagesProcessed /  jobWindow.inputFiles.size());
            });
            statisticsWindow.addFilterStatistic(imageUnit.filterName, imageUnit.fileSize, imageUnit.processTime + (endTime - startTime));
            statisticsWindow.incrementTotalImages();
            imageUnit = buffer.consume();
        }
    }
}
