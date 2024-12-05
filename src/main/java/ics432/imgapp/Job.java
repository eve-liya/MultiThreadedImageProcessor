package ics432.imgapp;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * A class that defines the "job" abstraction, that is, a  set of input image files
 * to which an ImgTransform must be applied, thus generating a set of output
 * image files. Each output file name is the input file name prepended with
 * the ImgTransform name and an underscore.
 */
public  class Job {

    protected final String filterName;
    protected final List<Path> inputFiles;
    protected final JobWindow jobWindow;
    protected volatile boolean isCanceled;
    private final Path targetDir;

    protected final Profile profile;

    // The list of outcomes for each input file
    protected final List<ImgTransformOutcome> outcomes;

    /**
     * Constructor
     *
     * @param filterName The name of the filter to apply to input images
     * @param targetDir  The target directory in which to generate output images
     * @param inputFiles The list of input file paths
     * @param jobWindow The window for this job
     */
    Job(String filterName,
        Path targetDir,
        List<Path> inputFiles,
        JobWindow jobWindow) {

        this.profile = new Profile();

        this.filterName = filterName;
        this.targetDir = targetDir;
        this.inputFiles = inputFiles;
        this.jobWindow = jobWindow;

        this.outcomes = new ArrayList<>();
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
     * Method to add an outcome
     */
    void addOutcome(ImgTransformOutcome outcome) {
        this.outcomes.add(outcome);

        if (this.jobWindow != null) {
            double progress = 1.0 * (this.outcomes.size()) / this.inputFiles.size();
            this.jobWindow.updateProgress(progress);
            if (outcome.success) {
                this.jobWindow.updateDisplayAfterImgProcessed(outcome.outputFile);
            }
        }

        if (outcome.success) {
            ICS432ImgApp.statistics.newlyProcessedImage();
            this.profile.totalMBProcessed += (float)(outcome.inputFile.toFile().length()) / 1000000.0F;
        }

        if (this.outcomes.size() == this.inputFiles.size()) {
            synchronized (this) {
                this.notify();
            }
        }
    }




    /**
     * A helper nested class to keep track of profiling information
     */
    static class Profile {
        public double readTime = 0.0;
        public double writeTime = 0.0;
        public double processingTime = 0.0;
        public double totalExecutionTime = 0.0;
        public double totalMBProcessed = 0.0;
    }

    /**
     * Method to execute the job, synchronously (i.e., the method is blocking)
     */
    public void execute() {

        long start = System.currentTimeMillis();

        if (this.jobWindow != null) {
            this.jobWindow.updateProgress(0.0);
        }

        // Populate the toRead buffer
        try {
            // TODO what the hell is this
            if (filterName.equals("DPEdge") || filterName.equals("DPFunk1") || filterName.equals("DPFunk2")) {
                for (Path inputFile : this.inputFiles) {
                    ImageUnit imgUnit = new ImageUnitExternal(this.filterName, inputFile, targetDir, this, false);
                    ProducerConsumer.toRead.put(imgUnit);
                }
            } else {
                for (Path inputFile : this.inputFiles) {
                    ImageUnit imgUnit = new ImageUnit(this.filterName, inputFile, targetDir, this, false);
                    ProducerConsumer.toRead.put(imgUnit);
                }
            }
            // Put a "the end" work unit to signal the end of computing
            ProducerConsumer.toRead.put(ImageUnit.theEnd);
        } catch (InterruptedException ignore) {
        }

        // Wait to be signaled
        synchronized (this) {
            try {
                this.wait();
            } catch (InterruptedException ignore) {}
        }

        this.profile.totalExecutionTime = (System.currentTimeMillis() - start) / 1000F;

        ICS432ImgApp.statistics.newlyCompletedJob(this.filterName, this.profile.totalMBProcessed, this.profile.totalExecutionTime);


        if (this.jobWindow != null) {
            this.jobWindow.updateDisplayAfterJobCompletion(
                    this.isCanceled,
                    this.outcomes,
                    this.profile);
        }
    }

}