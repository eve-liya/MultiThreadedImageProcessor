package ics432.imgapp;

import java.awt.RenderingHints;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;
import java.awt.image.ColorModel;
import java.util.ArrayList;
import java.util.Collections;

public class DPMedianFilter implements BufferedImageOp {

    private static int numThreads = 1;

    public DPMedianFilter() { }

    public static void setNumThreads(int threads) {
        DPMedianFilter.numThreads = threads;
    }

    /**
     * A Helper method to determining whether a set of coordinates are in bounds
     * @param x: horizontal coordinates
     * @param y: vertical coordinates
     * @param width: image width
     * @param height: image height
     * @return true if the pixel is in bounds
     */
    private boolean inBounds(int x, int y, int width, int height) {
        return ((x >= 0) && (x < width) && (y >= 0) && (y < height));
    }

    /**
     * A method to process a single pixel
     * @param image: the image that contains the pixel
     * @param x: the horizontal coordinate of the pixel
     * @param y: the vertical coordinate of the pixel
     * @return the RGB integer encoding of the new pixel value
     */
    protected int processPixel(BufferedImage image, int x, int y) {
        int width = image.getWidth();
        int height = image.getHeight();

        byte[] newbytes = new byte[3];

        for (int channel = 0; channel < 3; channel++) {

            ArrayList<Byte> neighbors = new ArrayList<>();

            for (int nx=x-1; nx <= x+1; nx++) {
                for (int ny=y-1; ny <= y+1; ny++) {
                    if (inBounds(nx,ny,width,height)) { // could instead catch an exception
                        neighbors.add(RGB.intToBytes(image.getRGB(nx,ny))[channel]);
                    }
                }
            }

            Collections.sort(neighbors);
            newbytes[channel] = neighbors.get(neighbors.size()/2);
        }
        return RGB.bytesToInt(newbytes);
    }


    /**
     * Overriden filter() method
     * @param src: the source image
     * @param dest: the destination image
     * @return the destination image
     */
    @Override
    public BufferedImage filter(BufferedImage src, BufferedImage dest) {
        System.err.println("Source img: height: " + src.getHeight() + ", width: " + src.getWidth());
        int width = src.getWidth();
        int height = src.getHeight() / numThreads;

        // Create output image
        dest = new BufferedImage(src.getWidth(), src.getHeight(), src.getType());
        Thread[] medianThreads = new Thread[numThreads];

        for (int t = 0; t < numThreads - 1; t++) {
            medianThreads[t] = new Thread(new MedianThread(src, dest, height, width, height * t, t));
            medianThreads[t].start();
        }
        medianThreads[numThreads - 1] = new Thread(new MedianThread(src, dest, height, width, height + src.getHeight() % numThreads, numThreads));
        for (int t = 0; t < numThreads; t++) {
            try {
                medianThreads[t].join();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        return dest;
    }

    @Override
    public Rectangle2D getBounds2D(BufferedImage src) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public BufferedImage createCompatibleDestImage(BufferedImage src,
                                                   ColorModel destCM) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Point2D getPoint2D(Point2D srcPt, Point2D dstPt) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public RenderingHints getRenderingHints() {
        // TODO Auto-generated method stub
        return null;
    }

    private class MedianThread implements Runnable {
        BufferedImage dest;
        BufferedImage src;
        int height;
        int width;
        int initial;
        int id;

        MedianThread(BufferedImage src, BufferedImage dest, int height, int width, int initial, int id) {
            this.dest = dest;
            this.src = src;
            this.height = height;
            this.width = width;
            this.initial = initial;
            this.id = id;
        }
        @Override
        public void run() {
            for (int i=initial; i < height * (id + 1); i++) {
                for (int j=0; j < width; j++) {
                    dest.setRGB(j,i,processPixel(src,j,i));
                }
            }
        }

    }
}