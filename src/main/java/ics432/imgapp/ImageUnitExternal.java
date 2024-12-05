package ics432.imgapp;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class ImageUnitExternal extends ImageUnit {

    public ImageUnitExternal(String filterName, Path inputFile, Path targetDir, Job job, boolean last) {
        super(filterName, inputFile, targetDir, job, last);
    }

    @Override
    public void readInputFile() throws IOException {}

    @Override
    public void writeImage() throws IOException {
        outputFile = Paths.get(targetDir + "/" + this.filterName + "_" + this.inputFile.getFileName());
        System.err.println(outputFile);
    }

    @Override
    public void processImage() {
        List<String> args = new ArrayList<>();
        args.add("docker");
        args.add("run");
        args.add("--rm");
        args.add("-v");
        args.add(inputFile.getParent() + ":/input");
        args.add("-v");
        args.add(targetDir + ":/output");
        args.add("ics432imgapp_c_filters");
        switch (filterName) {
            case "DPEdge":
                args.add("jpegedge");
                break;
            case "DPFunk1":
                args.add("jpegfunk1");
                break;
            case "DPFunk2":
                args.add("jpegfunk2");
                break;
        }
        args.add("/input/" + inputFile.getFileName());
        args.add("/output/" + this.filterName + "_" + this.inputFile.getFileName());

        ProcessBuilder pb = new ProcessBuilder(args);
        try {
            Process p = pb.inheritIO().start(); // The inheritIO() is important!
            int status = p.waitFor();
            if (status != 0) {
                // Ok to just abort if some error
                System.err.println("Processbuilder-created process failed! [FATAL]");
                System.exit(0);
            }
        } catch (InterruptedException ignore) {
        } catch (IOException e) {
            // Ok to just abort if some error
            System.err.println("Processbuilder-created process failed! [FATAL]");
            System.exit(0);
        }
    }

    }
