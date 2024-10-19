package ics432.imgapp;
import javafx.embed.swing.SwingFXUtils;


import java.awt.image.BufferedImageOp;

public class ImageProcesserThread extends Thread implements Runnable {

    private final ProducerConsumerBuffer<ImageUnit> readBuffer;
    private final ProducerConsumerBuffer<ImageUnit> writeBuffer;
    private final BufferedImageOp filter;
    private final ImageJobThread.timer timer;

    ImageProcesserThread(ProducerConsumerBuffer<ImageUnit> readBuffer,
                         ProducerConsumerBuffer<ImageUnit> writeBuffer,
                         BufferedImageOp filter, ImageJobThread.timer timer) {
        this.readBuffer = readBuffer;
        this.writeBuffer = writeBuffer;
        this.filter = filter;
        this.timer = timer;
    }

    @Override
    public void run() {
        ImageUnit toProcess = readBuffer.consume();
        while (!toProcess.last) {
            long startTime = System.currentTimeMillis();
            toProcess.filteredImage = filter.filter(SwingFXUtils.fromFXImage(toProcess.image, null), null);
            long endTime = System.currentTimeMillis();
            timer.processTime += endTime - startTime;
            toProcess.image = null;
            toProcess.processTime += endTime - startTime;
            writeBuffer.produce(toProcess);
            toProcess = readBuffer.consume();
        }
        writeBuffer.produce(toProcess);
    }
}
