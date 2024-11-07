# ics432imgapp

This is a simple Java/JavaFX Application with which users can apply filters
to batches of image (jpg) files, and which serves as a basis for
all ICS432 programming assignments. 

The application is structured as a Maven project, with all source code in the 
`src/` directory and the Maven configuration in the `pom.xml` file.

The `external_filters` directory contains additional image filters, not necessarily implemented in Java, 
which will be relevant for later programming assignment. See `README` file within for details.

# Assignment 4
The statistics window, encapsulates the statistics of each job that is run including the total number of executed jobs, since the application was started The total number of successfully processed images in total, since the application was started and the average compute speed (total MB of input divided by total execution time in seconds, computed when a job completes) for each filter since the application was started (0 if the filter has not been used yet).

The statistics window has two variables, totalJobs and totalImages. These are to keep track of the total number of jobs and images processed since the application was started. Since these are shared variables and multiple jobs can update these at once, the only way they should be changed is through the incrementTotalJobs and incrementTotalImages methods. The increment in each method is done in a synchronized block to ensure that only one thread can update the variables at a time. This prevents race conditions and lost updates to the variables.

For each filter there is also additional statistics kept. It has the total size of images processed by the filter in MB and the total time for each filter job. The data for the filters are kept in a FilterStatistics class for which there is one of for each filter. In each filter, the way to update the variables for the filter is through the addStatistics method. The addition to the variables is done in a synchronized block to ensure that only one thread can update the variables at a time. However, multiple threads are able to update the variables for different filters at the same time since the synchronized block only locks the lock for the specific filter being updated. This allows some concurrency in the application since different filters can be updated at the same time.

# Assignment 6
The largest possible value for the buffer sizes for the producer consumer buffer I was able to run without getting an out of memory exception was 2.

## Acceleration factors
Invert(single threaded): 3611 ms
Invert(multithreaded): 1762 ms
Acceleration Factor: 2.05x

Solarize(single threaded): 3207 ms
Solarize(multithreaded): 1748 ms
Acceleration Factor: 1.83x

Oil4(single threaded): 50217 ms
Oil4(multithreaded): 48089 ms
Acceleration Factor: 1.04x

The acceleration factors for the invert and solarize filters were quite high, we reached almost double the performance.
However, the Oil4 multithreaded implementation barely improved. This is because for the invert/solarize filters, the processing
time is not as high compared to the reading and writing times. Since all of these are around the same order of magnitude, running 
the reading, processing, and writing concurrently would improve the performance. However, the Oil4 processing time is quite long compared
to the reading and writing. This means that even though reading/writing can occur at the same time as the processing, we are bottlenecked
and have to wait for the long process to finish before moving on to the next image.

# Assignment 8
The 3 threads are created in the Main Window class. They are created there and set to Daemon threads so the app can close even if they are still running. The producer consumer using the Array blocking queue is also set up in the MainWindow class. There are 3 buffers, one which starts the chain which the reader consumes from, one which the reader produces to and the processor consumes from, and one that the processor produces to and the writer consumes from. When a job is started, it will get a reference to the first input buffer and load all the files in there. Then the readers,processors, and writers can start. 

