package ics432.imgapp;

import java.nio.file.Path;

/**
 * A helper nested class to define a imgTransform outcome for a given input file and ImgTransform
 */
public class ImgTransformOutcome {

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