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
import java.nio.file.Path;
import java.nio.file.Paths;

import static javax.imageio.ImageIO.createImageOutputStream;

/**
 * Class that implement a work unit abstraction
 */
public class ImageUnit {

    public static final ImageUnit theEnd = new ImageUnit(null, null, null, null, true);

    final public String filterName;
    final public Path inputFile;
    final public Path targetDir;
    public Path outputFile;
    public Image inputImage;
    public BufferedImage processedImage;
    public final Job job;
    public final boolean last;

    public ImageUnit(String filterName, Path inputFile, Path targetDir, Job job, boolean last) {
        this.filterName = filterName;
        this.inputFile = inputFile;
        this.targetDir = targetDir;
        this.outputFile = null;
        this.inputImage = null;
        this.processedImage = null;
        this.job = job;
        this.last = last;
    }

    public void readInputFile() throws IOException {
        // Load the image from file
        try {
            this.inputImage = new Image(inputFile.toUri().toURL().toString());
            if (this.inputImage.isError()) {
                throw new IOException("Error while reading from " + inputFile.toAbsolutePath() +
                        " (" + this.inputImage.getException().toString() + ")");
            }
        } catch (IOException e) {
            this.inputImage = null;
            throw new IOException("Error while reading from " + inputFile.toAbsolutePath());
        }
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
            case "MedianFilter":
                return new MedianFilter();
            case "DPMedianFilter":
                return new DPMedianFilter();
            default:
                throw new RuntimeException("Unknown filter " + filterName);
        }
    }

    public void processImage() {
        if (this.inputImage != null) {
            BufferedImageOp filter = createFilter(this.filterName);
            this.processedImage = filter.filter(SwingFXUtils.fromFXImage(this.inputImage, null), null);
            this.inputImage = null; // freeing  memory
        }
    }

    public  void writeImage() throws IOException {
        if (this.processedImage != null) {
            String outputPath = this.targetDir + "/" + this.filterName + "_" + this.inputFile.getFileName();
            try {
                this.outputFile = Paths.get(outputPath);
                OutputStream os = new FileOutputStream(outputPath);
                ImageOutputStream outputStream = createImageOutputStream(os);
                ImageIO.write(this.processedImage, "jpg", outputStream);
            } catch (IOException | NullPointerException e) {
                throw new IOException("Error while writing to " + outputPath);
            }
            this.processedImage = null; // freeing memory
        }
    }

}