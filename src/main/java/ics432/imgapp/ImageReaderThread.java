package ics432.imgapp;

import java.io.IOException;

public class ImageReaderThread extends Thread {

    @Override
    public void run() {

        while (true) {

            ImageUnit imgUnit;
            try {
                imgUnit = ProducerConsumer.toRead.take();
            } catch (InterruptedException e) {
                continue;
            }

            if ((!imgUnit.last) && (!imgUnit.job.isCanceled)) {

                try {
                    long t1 = System.currentTimeMillis();
                    imgUnit.readInputFile();
                    long t2 = System.currentTimeMillis();
                    imgUnit.job.profile.readTime += (t2 - t1) / 1000F;
                } catch (IOException e) {
                    imgUnit.job.addOutcome(new ImgTransformOutcome(false, imgUnit.inputFile, null, e));
                    continue;
                }
            }

            try {
                ProducerConsumer.toProcess.put(imgUnit);
            } catch (InterruptedException ignore) {
                continue;
            }

        }
    }
}
