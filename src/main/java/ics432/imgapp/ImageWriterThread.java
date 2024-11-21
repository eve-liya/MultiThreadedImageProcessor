package ics432.imgapp;

import java.io.IOException;

public class ImageWriterThread extends Thread {

    @Override
    public void run() {

        while (true) {

            ImageUnit imgUnit;
            try {
                imgUnit = ProducerConsumer.toWrite.take();
            } catch (InterruptedException e) {
                continue;
            }

            if ((imgUnit.processedImage != null) && (!imgUnit.job.isCanceled)) {

                ImgTransformOutcome outcome;
                try {
                    long t1 = System.currentTimeMillis();
                    imgUnit.writeImage();
                    long t2 = System.currentTimeMillis();
                    imgUnit.job.profile.writeTime += (t2 - t1) / 1000F;
                    outcome = new ImgTransformOutcome(true, imgUnit.inputFile, imgUnit.outputFile, null);
                } catch (IOException e) {
                    outcome = new ImgTransformOutcome(false, imgUnit.inputFile, null, e);
                }
                imgUnit.job.addOutcome(outcome);
            }
        }
    }
}

