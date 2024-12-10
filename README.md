# ics432imgapp

This is a simple Java/JavaFX Application with which users can apply filters
to batches of image (jpg) files, and which serves as a basis for
all ICS432 programming assignments. 

The application is structured as a Maven project, with all source code in the 
`src/` directory and the Maven configuration in the `pom.xml` file.

The `external_filters` directory contains additional image filters, not necessarily implemented in Java, 
which will be relevant for later programming assignment. See `README` file within for details.

---

# Assignment 4
The statistics window, encapsulates the statistics of each job that is run including the total number of executed jobs, since the application was started The total number of successfully processed images in total, since the application was started and the average compute speed (total MB of input divided by total execution time in seconds, computed when a job completes) for each filter since the application was started (0 if the filter has not been used yet).

The statistics window has two variables, totalJobs and totalImages. These are to keep track of the total number of jobs and images processed since the application was started. Since these are shared variables and multiple jobs can update these at once, the only way they should be changed is through the incrementTotalJobs and incrementTotalImages methods. The increment in each method is done in a synchronized block to ensure that only one thread can update the variables at a time. This prevents race conditions and lost updates to the variables.

For each filter there is also additional statistics kept. It has the total size of images processed by the filter in MB and the total time for each filter job. The data for the filters are kept in a FilterStatistics class for which there is one of for each filter. In each filter, the way to update the variables for the filter is through the addStatistics method. The addition to the variables is done in a synchronized block to ensure that only one thread can update the variables at a time. However, multiple threads are able to update the variables for different filters at the same time since the synchronized block only locks the lock for the specific filter being updated. This allows some concurrency in the application since different filters can be updated at the same time.

---

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

---

# Assignment 8
The 3 threads are created in the Main Window class. They are created there and set to Daemon threads so the app can close even if they are still running. The producer consumer using the Array blocking queue is also set up in the MainWindow class. There are 3 buffers, one which starts the chain which the reader consumes from, one which the reader produces to and the processor consumes from, and one that the processor produces to and the writer consumes from. When a job is started, it will get a reference to the first input buffer and load all the files in there. Then the readers,processors, and writers can start. 

---

# Assignment 9

The data decomposition scheme I chose was to simply split the image into columns.
Each thread will process all the pixels in their respective column. This is defined as height(img) * width(img)/numThreads.

01 thread      1.00x : 16.82 seconds 
02 threads     2.04x : 8.26 seconds
04 threads     3.48x : 4.84 seconds
08 threads     3.95x : 4.26 seconds

I'm pretty happy with my results. When I ran the regular median filter before it took quite a while for to process all the images.
And it sped it up by almost 4 times. We did see limited gains when going from 4 - 8 threads however.  

I'm wondering if since my CPU has 4 physical cores but with hyperthreading I can have up to 8 hyperthreads so when I go above
my number of physical cores then the benefits would be less since it won't be true concurrency?

1 process 1 DP : 502.69 seconds
1 process 8 DP : 130.83 seconds 
2 process 8/2 DP : 129.75 seconds
4 process 2 DP : 130.73
8 process 1 DP : 135.23

It seems like it is better to do a combination of both task and data parallism. 
However, we should maintain a balance, I tried also doing N threads and N DP threads and I had some issues with 
my computer slowing down and taking too much resources. 

---

# Assignment 10

## JPEGEDGE

### Performance Data
| Threads | Execution Time (s) | Speedup | Parallel Efficiency |
|---------|---------------------|---------|---------------------|
| 1       | 3.39               | 1.00x    | 100%                |
| 2       | 2.06               | 1.65x    | 82.5%               |
| 4       | 1.37               | 2.47x    | 61.8%               |
| 6       | 1.35               | 2.51x    | 41.8%               |
| 8       | 1.30               | 2.61x    | 32.6%               |

- Speedup and efficiency decline as the number of threads increases beyond 4

## Naive JPEGFUNK

### Performance Data
| Threads | Execution Time (s) | Speedup | Parallel Efficiency |
|---------|---------------------|---------|---------------------|
| 1       | 151.91             | 1.00x    | 100%                |
| 2       | 124.94             | 1.22x    | 61.0%               |
| 4       | 83.32              | 1.82x    | 45.5%               |
| 6       | 66.00              | 2.30x    | 38.4%               |
| 8       | 50.22              | 3.02x    | 37.8%               |

- The results are worse than the DP of the JpegEdge filter. We have lower parallel efficiency per # of cores and lower speedup.
- The speedup does get better as we increase the number of threads. This is most likely because of amdalhs law. And the filter being very compute heavy
- As shown below, the threads are very unbalanced in work time. Thread 0 takes a lot less time than the N thread, which means we aren't utilizing the full potential.

### Thread Execution Times (4 Threads)
| Thread | Elapsed Time (s) |
|--------|-------------------|
| 0      | 5.79             |
| 1      | 21.86            |
| 2      | 48.82            |
| 3      | 83.02            |

### Thread Execution Times (6 Threads)
| Thread | Elapsed Time (s) |
|--------|-------------------|
| 0      | 4.26             |
| 1      | 12.86            |
| 2      | 17.05            |
| 3      | 29.95            |
| 4      | 49.21            |
| 5      | 65.67            |

### Thread Execution Times (8 Threads)
| Thread | Elapsed Time (s) |
|--------|-------------------|
| 0      | 2.57             |
| 1      | 6.81             |
| 2      | 13.26            |
| 3      | 18.29            |
| 4      | 23.57            |
| 5      | 35.69            |
| 6      | 46.02            |
| 7      | 49.92            |

## Clever JPEGFUNK

### Performance Data
| Threads | Execution Time (s) | Speedup | Parallel Efficiency |
|---------|---------------------|---------|---------------------|
| 1       | 156.49             | 1.00x    | 100%                |
| 2       | 78.49              | 1.99x    | 99.5%               |
| 4       | 43.95              | 3.56x    | 89.0%               |
| 6       | 39.23              | 3.99x    | 66.5%               |
| 8       | 35.74              | 4.38x    | 54.8%               |

- This is much better than the previous JpegFunk.
- We have higher parallel efficiency for each # of threads and much better speedup.
- As we can see below, the work spread between each thread is much more equally distributed.
- The main difference here is using the dynamic scheduler so that when threads finish their work they will recieve new work to do.
- Also we collapse the loop so that each thread gets stuck doing a smaller chunk of work rather than an entire row. Previously a thread would get stuck doing an entire row and take a long time then would get assigned another long row. Now, a thread will do a small chunk of the image, and then get the next available chunk leading to much better load balancing. I experimented with a couple different chunk sizes and it seems on my computer using a chunk size of 25 got the best results. Too large and a thread would get stuck on computing a large section, too small and the overhead of dynamic scheduling would start affecting the time.

### Thread Execution Times
### Thread Execution Times (4 Threads)

| Thread | Elapsed Time (s) |
| ------ | ---------------- |
| 3      | 43.63            |
| 2      | 43.63            |
| 1      | 43.63            |
| 0      | 43.63            |

### Thread Execution Times (6 Threads)

| Thread | Elapsed Time (s) |
| ------ | ---------------- |
| 3      | 38.88            |
| 2      | 38.88            |
| 4      | 38.88            |
| 1      | 38.88            |
| 5      | 38.88            |
| 0      | 38.88            |

### Thread Execution Times (8 Threads)

| Thread | Elapsed Time (s) |
| ------ | ---------------- |
| 1      | 35.40            |
| 4      | 35.40            |
| 5      | 35.40            |
| 7      | 35.40            |
| 2      | 35.40            |
| 0      | 35.39            |
| 3      | 35.40            |
| 6      | 35.40            |





