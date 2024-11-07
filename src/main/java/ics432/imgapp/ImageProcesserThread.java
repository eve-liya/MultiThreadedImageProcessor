package ics432.imgapp;
import javafx.embed.swing.SwingFXUtils;


import java.awt.image.BufferedImageOp;
import java.util.concurrent.ArrayBlockingQueue;

public class ImageProcesserThread extends Thread implements Runnable {

    private final ArrayBlockingQueue<ImageUnit> readBuffer;
    private final ArrayBlockingQueue<ImageUnit> writeBuffer;
    private final BufferedImageOp filter;
    private final ImageJobThread.timer timer;

    ImageProcesserThread(ArrayBlockingQueue<ImageUnit> readBuffer,
                         ArrayBlockingQueue<ImageUnit> writeBuffer,
                         BufferedImageOp filter, ImageJobThread.timer timer) {
        this.readBuffer = readBuffer;
        this.writeBuffer = writeBuffer;
        this.filter = filter;
        this.timer = timer;
    }

    @Override
    public void run() {
        ImageUnit toProcess = null;
        try {
            toProcess = readBuffer.take();
        } catch (InterruptedException ignore) {}
        while (!toProcess.last) {
            long startTime = System.currentTimeMillis();
            toProcess.filteredImage = filter.filter(SwingFXUtils.fromFXImage(toProcess.image, null), null);
            long endTime = System.currentTimeMillis();
            timer.processTime += endTime - startTime;
            toProcess.image = null;
            toProcess.processTime += endTime - startTime;
            writeBuffer.offer(toProcess);
            try {
                toProcess = readBuffer.take();
            } catch (InterruptedException ignore) {}
        }
        writeBuffer.offer(toProcess);
    }
}
