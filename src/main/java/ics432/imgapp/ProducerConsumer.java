package ics432.imgapp;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class ProducerConsumer {

    /** Class variables and initialization code */
    public static LinkedBlockingQueue<ImageUnit> toRead;
    public static BlockingQueue<ImageUnit> toProcess;
    public static BlockingQueue<ImageUnit> toWrite;

    static ImageReaderThread readerThread;
    static List<ImageProcessorThread> processorThreads;
    static ImageWriterThread writerThread;

    public static void init() {
        ProducerConsumer.toRead = new LinkedBlockingQueue<>();
        ProducerConsumer.toProcess  = new ArrayBlockingQueue<>(16);
        ProducerConsumer.toWrite  = new ArrayBlockingQueue<>(16);

        ProducerConsumer.readerThread = new ImageReaderThread();
        ProducerConsumer.readerThread.setDaemon(true);
        ProducerConsumer.readerThread.start();

        ProducerConsumer.processorThreads = new ArrayList<>();
        setNumberProcessorThreads(1);

        ProducerConsumer.writerThread = new ImageWriterThread();
        ProducerConsumer.writerThread.setDaemon(true);
        ProducerConsumer.writerThread.start();
    }

    public static void setNumberProcessorThreads(int numThreads) {

        while (ProducerConsumer.processorThreads.size() != numThreads) {
            if (ProducerConsumer.processorThreads.size() < numThreads) {
                ImageProcessorThread t = new ImageProcessorThread();
                t.setDaemon(true);
                t.start();
                ProducerConsumer.processorThreads.add(t);
            } else {
                ImageProcessorThread t = ProducerConsumer.processorThreads.remove(0);
                t.cancel();
            }
        }
    }

}