package ics432.imgapp;

import javafx.scene.image.Image;

import java.io.IOException;
import java.util.concurrent.ArrayBlockingQueue;

public class ImageReaderThread implements Runnable {

    private final ArrayBlockingQueue<ImageUnit> inputBuffer;
    private final ArrayBlockingQueue<ImageUnit> processBuffer;

    ImageReaderThread(ArrayBlockingQueue<ImageUnit> inputBuffer, ArrayBlockingQueue<ImageUnit> buffer) {
        this.inputBuffer = inputBuffer;
        this.processBuffer = buffer;
    }

    @Override
    public void run() {
        ImageUnit imageUnit = null;
        try {
            imageUnit = inputBuffer.take();
        } catch (InterruptedException ignore) {}
        while (imageUnit != null) {
            try {
                if (!imageUnit.last) {
                    System.err.println("Reading " + imageUnit.inputFile);
                    long startTime = System.currentTimeMillis();
                    imageUnit.image = new Image(imageUnit.inputFile.toUri().toURL().toString());
                    long endTime = System.currentTimeMillis();
                    imageUnit.jobWindow.timer.readTime += endTime - startTime;
                }
                try {
                    processBuffer.put(imageUnit);
                } catch (InterruptedException ignore) {}
            } catch (IOException e) {
                System.err.println("Error while reading from " + imageUnit.inputFile.toAbsolutePath() +
                        " (" + e.getMessage() + ")");
            }
            try {
                imageUnit = inputBuffer.take();
            } catch (InterruptedException ignore) {}
        }
    }
}
