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
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

import static javax.imageio.ImageIO.createImageOutputStream;

public class ImageWriterThread implements Runnable {
    private final ArrayBlockingQueue<ImageUnit> buffer;

    ImageWriterThread(ArrayBlockingQueue<ImageUnit> buffer) {
        this.buffer = buffer;
    }

    @Override
    public void run() {
        AtomicInteger imagesProcessed = new AtomicInteger();
        ImageUnit imageUnit = null;
        try {
            imageUnit = buffer.take();
        } catch (InterruptedException ignore) {
        }
        while (imageUnit != null) {
            final JobWindow finalJobWindow = imageUnit.jobWindow;
            if (!imageUnit.last) {
                System.err.println("writing " + imageUnit.inputFile + " to " + imageUnit.targetDir);
                long startTime = System.currentTimeMillis();
                long endTime;
                String outputPath = imageUnit.targetDir + FileSystems.getDefault().getSeparator() + imageUnit.filterName + "_" + imageUnit.inputFile.getFileName();
                try {
                    OutputStream os = new FileOutputStream(outputPath);
                    ImageOutputStream outputStream = createImageOutputStream(os);
                    ImageIO.write(imageUnit.filteredImage, "jpg", outputStream);
                    endTime = System.currentTimeMillis();
                    imageUnit.jobWindow.timer.writeTime += endTime - startTime;
                    imagesProcessed.getAndIncrement();
                } catch (IOException | NullPointerException e) {
                    throw new RuntimeException("Error while writing to " + outputPath);
                }
                int finalImagesProcessed = imagesProcessed.get();
                Platform.runLater(() -> {
                    finalJobWindow.flwvp.addFiles(Collections.singletonList(Path.of(outputPath)));
                    finalJobWindow.progressBar.setProgress((double) finalImagesProcessed / finalJobWindow.inputFiles.size());
                });
                imageUnit.jobWindow.jobStatistics.addFilterStatistic(imageUnit.filterName, imageUnit.fileSize, imageUnit.processTime + (endTime - startTime));
                imageUnit.jobWindow.jobStatistics.incrementTotalImages();
            } else {
                Platform.runLater(() -> {
                    imagesProcessed.set(0);
                    finalJobWindow.timeLabel.setText("Total time: " + (System.currentTimeMillis() - finalJobWindow.timer.startTime) + " ms" + " " +
                            "Read time: " + finalJobWindow.timer.readTime + "ms" + " " +
                            "Write time: " + finalJobWindow.timer.writeTime + "ms" + " " +
                            "Process time: " + finalJobWindow.timer.processTime + "ms" + " ");
                    finalJobWindow.closeButton.setDisable(false);
                    finalJobWindow.progressBar.setVisible(false);
                });
            }

            try {
                imageUnit = buffer.take();
            } catch (InterruptedException ignore) {
            }
        }
    }
}
