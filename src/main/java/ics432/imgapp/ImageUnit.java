package ics432.imgapp;

import javafx.scene.image.Image;

import java.awt.image.BufferedImage;
import java.nio.file.Path;

public class ImageUnit {
    Path inputFile;
    Image image;
    BufferedImage filteredImage;
    String filterName;
    double fileSize;
    long processTime;
    boolean last;

    ImageUnit(Path inputFile, Image image, String filterName, double fileSize, long processTime, boolean last) {
        this.inputFile = inputFile;
        this.image = image;
        this.filterName = filterName;
        this.fileSize = fileSize;
        this.processTime = processTime;
        this.last = last;
    }
}
