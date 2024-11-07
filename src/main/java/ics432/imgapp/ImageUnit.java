package ics432.imgapp;

import javafx.scene.image.Image;

import java.awt.image.BufferedImage;
import java.nio.file.Path;

public class ImageUnit {

    Path inputFile;
    Path targetDir;
    Image image;
    BufferedImage filteredImage;
    String filterName;
    double fileSize;
    long processTime;
    boolean last;
    JobWindow jobWindow;

    ImageUnit(Path inputFile, Path targetDir, Image image, String filterName, double fileSize, long processTime, JobWindow jobWindow, boolean last) {
        this.inputFile = inputFile;
        this.targetDir = targetDir;
        this.image = image;
        this.filterName = filterName;
        this.fileSize = fileSize;
        this.processTime = processTime;
        this.jobWindow = jobWindow;
        this.last = last;
    }
}
