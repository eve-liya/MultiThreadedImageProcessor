package ics432.imgapp;

import com.jhlabs.image.InvertFilter;
import com.jhlabs.image.OilFilter;
import com.jhlabs.image.SolarizeFilter;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;

import javax.imageio.ImageIO;
import javax.imageio.stream.ImageOutputStream;
import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import static javax.imageio.ImageIO.createImageOutputStream;

/**
 * A class that defines the "job" abstraction, that is, a  set of input image files
 * to which a filter must be applied, thus generating a set of output
 * image files. Each output file name is the input file name prepended with
 * the ImgTransform name and an underscore.
 */
class Job {

    private final String filterName;
    private final Path targetDir;
    private final List<Path> inputFiles;

    // The list of outcomes for each input file
    private final List<ImgTransformOutcome> outcomes;

    private long readTime;
    private long writeTime;
    private long processTime;
    private long totalTime;

    /**
     * Constructor
     *
     * @param filterName The name of the filter to apply to input images
     * @param targetDir  The target directory in which to generate output images
     * @param inputFiles The list of input file paths
     */
    Job(String filterName,
        Path targetDir,
        List<Path> inputFiles) {

        this.filterName = filterName;
        this.targetDir = targetDir;
        this.inputFiles = inputFiles;

        this.outcomes = new ArrayList<>();
    }

    /**
     * Method to execute the imgTransform job on all files
     */
    void execute() {

        // Go through each input file and process it
        long startTime = System.currentTimeMillis();
        for (Path inputFile : inputFiles) {

            System.err.println("Applying " + this.filterName + " to " + inputFile.toAbsolutePath() + " ...");

            Path outputFile;
            try {
                outputFile = processInputFile(inputFile);
                // Generate a "success" outcome
                this.outcomes.add(new ImgTransformOutcome(true, inputFile, outputFile, null));
            } catch (IOException e) {
                // Generate a "failure" outcome
                this.outcomes.add(new ImgTransformOutcome(false, inputFile, null, e));
            }

        }
        long endTime = System.currentTimeMillis();
        totalTime = endTime - startTime;
    }

    /**
     * Method to process one image file
     */
    ImgTransformOutcome processNextImage(Path inputFile) {
        System.err.println("Applying " + this.filterName + " to " + inputFile.toAbsolutePath() + " ...");
        try {
            Path outputFile = processInputFile(inputFile);
            // Generate a "success" outcome
            return new ImgTransformOutcome(true, inputFile, outputFile, null);
        } catch (IOException e) {
            // Generate a "failure" outcome
            return new ImgTransformOutcome(false, inputFile, null, e);
        }
    }

    /**
     * Getter for job outcomes
     *
     * @return The job outcomes, i.e., a list of ImgTransformOutcome objects
     * (in flux if the job isn't done executing)
     */
    List<ImgTransformOutcome> getOutcomes() {
        return this.outcomes;
    }

    /**
     * Getter for total time
     *
     * @return The total time it took to execute the job
     */
    public long getTotalTime() {
        return this.totalTime;
    }

    /**
     * Getter for read time
     *
     * @return The total time it took to read the images
     */
    public long getReadTime() {
        return this.readTime;
    }

    /**
     * Getter for write time
     *
     * @return the total time it took to read the images
     */
    public long getWriteTime() {
        return this.writeTime;
    }

    /**
     * Getter for processing time
     *
     * @return the total time it took to read the images
     */
    public long getProcessTime() {
        return this.processTime;
    }

    /**
     * Helper method to apply a imgTransform to an input image file
     *
     * @param inputFile The input file path
     * @return the output file path
     */
    private Path processInputFile(Path inputFile) throws IOException {

        // Load the image from file
        Image image;
        long startTime = System.currentTimeMillis();
        try {
            image = new Image(inputFile.toUri().toURL().toString());
            if (image.isError()) {
                throw new IOException("Error while reading from " + inputFile.toAbsolutePath() +
                        " (" + image.getException().toString() + ")");
            }
        } catch (IOException e) {
            throw new IOException("Error while reading from " + inputFile.toAbsolutePath());
        }
        long endTime = System.currentTimeMillis();
        readTime += endTime - startTime;

        // Create the filter
        BufferedImageOp filter = createFilter(filterName);

        // Process the image
        startTime = System.currentTimeMillis();
        BufferedImage img = filter.filter(SwingFXUtils.fromFXImage(image, null), null);
        endTime = System.currentTimeMillis();
        processTime += endTime - startTime;

        // Write the image back to a file
        startTime = System.currentTimeMillis();
        String outputPath = this.targetDir + FileSystems.getDefault().getSeparator() + this.filterName + "_" + inputFile.getFileName();
        try {
            OutputStream os = new FileOutputStream(outputPath);
            ImageOutputStream outputStream = createImageOutputStream(os);
            ImageIO.write(img, "jpg", outputStream);
        } catch (IOException | NullPointerException e) {
            throw new IOException("Error while writing to " + outputPath);
        }
        endTime = System.currentTimeMillis();
        writeTime += endTime - startTime;

        // Success!
        return Paths.get(outputPath);
    }

    /**
     * A helper method to create a Filter object
     *
     * @param filterName the filter's name
     */
    private BufferedImageOp createFilter(String filterName) {
        switch (filterName) {
            case "Invert":
                return new InvertFilter();
            case "Solarize":
                return new SolarizeFilter();
            case "Oil4":
                OilFilter oil4Filter = new OilFilter();
                oil4Filter.setRange(4);
                return oil4Filter;
            default:
                throw new RuntimeException("Unknown filter " + filterName);
        }
    }

    /**
     * A helper nested class to define a imgTransform outcome for a given input file and ImgTransform
     */
    static class ImgTransformOutcome {

        // Whether the image transform is successful or not
        final boolean success;
        // The Input File path
        final Path inputFile;
        // The output file path (or null if failure)
        final Path outputFile;
        // The exception that was raised (or null if success)
        final Exception error;

        /**
         * Constructor
         *
         * @param success     Whether the imgTransform operation worked
         * @param input_file  The input file path
         * @param output_file The output file path  (null if success is false)
         * @param error       The exception raised (null if success is true)
         */
        ImgTransformOutcome(boolean success, Path input_file, Path output_file, Exception error) {
            this.success = success;
            this.inputFile = input_file;
            this.outputFile = output_file;
            this.error = error;
        }

    }
}
