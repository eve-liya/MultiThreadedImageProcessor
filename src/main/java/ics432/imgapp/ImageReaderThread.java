package ics432.imgapp;

import javafx.scene.image.Image;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class ImageReaderThread implements Runnable {

    private final List<Path> inputFiles;
    private final ProducerConsumerBuffer<ImageUnit> buffer;
    private final String filterName;
    private final ImageJobThread.timer timer;

    ImageReaderThread(List<Path> inputFiles, ProducerConsumerBuffer<ImageUnit> buffer,
                      String filterName, ImageJobThread.timer timer) {
        this.inputFiles = inputFiles;
        this.buffer = buffer;
        this.filterName = filterName;
        this.timer = timer;
    }

    @Override
    public void run() {
        for (Path inputFile : inputFiles) {
            try {
                long startTime = System.currentTimeMillis();
                Image image = new Image(inputFile.toUri().toURL().toString());
                long endTime = System.currentTimeMillis();
                timer.readTime += endTime - startTime;
                buffer.produce(new ImageUnit(inputFile, image, filterName, Files.size(inputFile) / 1048576.0,
                        endTime - startTime, false));
            } catch (IOException e) {
                System.err.println("Error while reading from " + inputFile.toAbsolutePath() +
                        " (" + e.getMessage() + ")");
            }
        }
        // poison pill
        buffer.produce(new ImageUnit(null, null, null, 0,0, true));
    }
}
