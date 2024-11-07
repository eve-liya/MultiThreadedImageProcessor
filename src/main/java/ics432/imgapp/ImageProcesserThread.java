package ics432.imgapp;
import javafx.embed.swing.SwingFXUtils;


import java.awt.image.BufferedImageOp;
import java.util.concurrent.ArrayBlockingQueue;

public class ImageProcesserThread implements Runnable {

    private final ArrayBlockingQueue<ImageUnit> readBuffer;
    private final ArrayBlockingQueue<ImageUnit> writeBuffer;

    ImageProcesserThread(ArrayBlockingQueue<ImageUnit> readBuffer,
                         ArrayBlockingQueue<ImageUnit> writeBuffer) {
        this.readBuffer = readBuffer;
        this.writeBuffer = writeBuffer;
    }

    @Override
    public void run() {
        ImageUnit imageUnit = null;
        try {
            imageUnit = readBuffer.take();
        } catch (InterruptedException ignore) {}
        while (imageUnit != null) {
            if (!imageUnit.last) {
                System.err.println("Processing " + imageUnit.inputFile);
                long startTime = System.currentTimeMillis();
                BufferedImageOp filter = Job.createFilter(imageUnit.filterName);
                imageUnit.filteredImage = filter.filter(SwingFXUtils.fromFXImage(imageUnit.image, null), null);
                long endTime = System.currentTimeMillis();
                imageUnit.jobWindow.timer.processTime += endTime - startTime;
                imageUnit.image = null;
                imageUnit.processTime += endTime - startTime;
            }
            try {
                writeBuffer.put(imageUnit);
            } catch (InterruptedException ignore) {}
            try {
                imageUnit = readBuffer.take();
            } catch (InterruptedException ignore) {}
        }
    }
}
