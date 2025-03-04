package ics432.imgapp;

import javafx.geometry.Orientation;
import javafx.scene.control.Separator;
import javafx.scene.image.Image;

import java.net.MalformedURLException;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * A helper class that implements static helper methods
 */
class Util {

    /**
     * Helper method to load an image from a resource file (with a URL)
     *
     * @param filename The resource file name
     * @return an image
     */
    static Image loadImageFromResourceFile(String filename) {

        Path path = Paths.get("src", "main", "resources", filename).toAbsolutePath();
        return loadImageFromPath(path);

    }

    /**
     * Helper method to load an image from an absolute path
     *
     * @param path The path
     * @return an image or null if there was an error
     */
    private static Image loadImageFromPath(Path path) {

        try {
            Image image = new Image(path.toUri().toURL().toString());
            if (image.isError()) {
                return null;
            }
            return image;
        } catch (MalformedURLException e) {
            return null;
        }
    }

    /**
     * Helper method to create  Separator objects
     *
     * @param preferredWidth  Preferred width
     * @param preferredHeight Preferred  Height
     * @param orientation     Orientation
     * @return A new separator
     */
    static Separator createSeparator(double preferredWidth, double preferredHeight, Orientation orientation) {
        Separator s = new Separator(orientation);
        s.setPrefWidth(preferredWidth);
        s.setPrefHeight(preferredHeight);
        return s;
    }


}