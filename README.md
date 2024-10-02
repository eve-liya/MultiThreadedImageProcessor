# ics432imgapp

This is a simple Java/JavaFX Application with which users can apply filters
to batches of image (jpg) files, and which serves as a basis for
all ICS432 programming assignments. 

The application is structured as a Maven project, with all source code in the 
`src/` directory and the Maven configuration in the `pom.xml` file.

The `external_filters` directory contains additional image filters, not necessarily implemented in Java, 
which will be relevant for later programming assignment. See `README` file within for details.

The statistics window, encapsulates the statistics of each job that is run including the total number of executed jobs, since the application was started The total number of successfully processed images in total, since the application was started and the average compute speed (total MB of input divided by total execution time in seconds, computed when a job completes) for each filter since the application was started (0 if the filter has not been used yet).

The statistics window has two variables, totalJobs and totalImages. These are to keep track of the total number of jobs and images processed since the application was started. Since these are shared variables and multiple jobs can update these at once, the only way they should be changed is through the incrementTotalJobs and incrementTotalImages methods. The increment in each method is done in a synchronized block to ensure that only one thread can update the variables at a time. This prevents race conditions and lost updates to the variables.

For each filter there is also additional statistics kept. It has the total size of images processed by the filter in MB and the total time for each filter job. The data for the filters are kept in a FilterStatistics class for which there is one of for each filter. In each filter, the way to update the variables for the filter is through the addStatistics method. The addition to the variables is done in a synchronized block to ensure that only one thread can update the variables at a time. However, multiple threads are able to update the variables for different filters at the same time since the synchronized block only locks the lock for the specific filter being updated. This allows some concurrency in the application since different filters can be updated at the same time.
