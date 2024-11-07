//package ics432.imgapp;
//
//import javafx.application.Platform;
//import java.util.concurrent.ArrayBlockingQueue;
//
//import java.awt.image.BufferedImageOp;
//import java.nio.file.Path;
//import java.util.List;
//
//public class ImageJobThread implements Runnable {
//    private final String filterName;
//    private final Path targetDir;
//    private final int bufferSize;
//    private final List<Path> inputFiles;
//    private final JobWindow jobWindow;
//    private final JobStatisticsWindow jobStatisticsWindow;
//    private final timer timer;
//    static class timer {
//        long readTime = 0;
//        long writeTime = 0;
//        long processTime = 0;
//    }
//
//    /**
//     * Constructor
//     *
//     * @param filterName The name of the filter to apply to input images
//     * @param targetDir  The target directory in which to generate output images
//     * @param inputFiles The list of input file paths
//     */
//    ImageJobThread(JobWindow jobWindow,String filterName, Path targetDir,
//                   List<Path> inputFiles) {
//        this.filterName = filterName;
//        this.targetDir = targetDir;
//        this.inputFiles = inputFiles;
//        this.jobWindow = jobWindow;
//        this.bufferSize = 16;
//        this.jobStatisticsWindow = jobWindow.jobStatistics;
//        this.timer = new timer();
//    }
//
//    @Override
//    public void run() {
//        long startTime = System.currentTimeMillis();
//        ArrayBlockingQueue<ImageUnit> readBuffer = new ArrayBlockingQueue<>(bufferSize);
//        Thread reader = new Thread(new ImageReaderThread(inputFiles, readBuffer, filterName, timer));
//        reader.start();
//
//        ArrayBlockingQueue<ImageUnit> writeBuffer = new ArrayBlockingQueue<>(bufferSize);
//        Thread processer = new Thread(new ImageProcesserThread(readBuffer, writeBuffer, filter, timer));
//        processer.start();
//
//        Thread writer = new Thread(new ImageWriterThread(writeBuffer, targetDir, jobWindow, timer, jobStatisticsWindow));
//        writer.start();
//
//        try {
//            reader.join();
//            processer.join();
//            writer.join();
//        } catch (InterruptedException ignore) {}
//
//        jobStatisticsWindow.incrementTotalJobs();
//
//        Platform.runLater(() -> {
//            jobWindow.timeLabel.setText("Total time: " + (System.currentTimeMillis() - startTime) + " ms" + " " +
//                                "Read time: " + timer.readTime + "ms" + " " +
//                                "Write time: " + timer.writeTime + "ms" + " " +
//                                "Process time: " + timer.processTime + "ms" + " ");
//            jobWindow.closeButton.setDisable(false);
//            jobWindow.progressBar.setVisible(false);
//        });
//    }
//}
