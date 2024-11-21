package ics432.imgapp;

public class ImageProcessorThread extends Thread {

    private volatile boolean isCancelled = false;

    public void cancel()  {
        this.isCancelled = true;
        this.interrupt();
    }

    @Override
    public void run() {

        while (true) {

            if (this.isCancelled) return;

            ImageUnit imgUnit;
            try {
                imgUnit = ProducerConsumer.toProcess.take();
            } catch (InterruptedException e) {
                if (this.isCancelled) {
                    return;
                } else {
                    continue;
                }
            }

            if (this.isCancelled) return;

            if ((imgUnit.inputImage != null) && (!imgUnit.job.isCanceled)) {
                long t1 = System.currentTimeMillis();
                imgUnit.processImage();
                long t2 = System.currentTimeMillis();
                imgUnit.job.profile.processingTime += (t2 - t1) / 1000F;
            }

            if (this.isCancelled) return;

            try {
                ProducerConsumer.toWrite.put(imgUnit);
            } catch (InterruptedException e) {
                if (this.isCancelled) {
                    return;
                } else {
                    continue;
                }
            }

        }
    }
}