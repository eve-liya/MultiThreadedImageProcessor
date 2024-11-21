package ics432.imgapp;

import javafx.beans.property.*;

import java.util.HashMap;

public class Statistics {

    public final HashMap<String, SimpleDoubleProperty> content;

    public Statistics() {
        this.content = new HashMap<>();
        this.content.put("num_completed_jobs", new SimpleDoubleProperty(0));
        this.content.put("num_processed_images", new SimpleDoubleProperty(0));
        ICS432ImgApp.filterNames.forEach((t) -> {
            this.content.put("filter_bytes_" + t,
                    new SimpleDoubleProperty(0));
            this.content.put("filter_time_" + t,
                    new SimpleDoubleProperty(2));
            this.content.put("filter_speed_" + t,
                    new SimpleDoubleProperty(0));
        });
    }

    /**
     * Method to reset the statistics to zero
     */
    public void reset() {
        this.content.get("num_completed_jobs").set(0);
        this.content.get("num_processed_images").set(0);
        ICS432ImgApp.filterNames.forEach((t) -> {
            this.content.get("filter_bytes_" + t).set(0.0F);
            this.content.get("filter_time_" + t).set(0.0F);
            this.content.get("filter_speed_" + t).set(0.0F);
        });
    }

    public synchronized void newlyCompletedJob(String filterName, double mb, double sec)  {
        SimpleDoubleProperty p1 = this.content.get("num_completed_jobs");
        p1.set(p1.get() + 1);
        SimpleDoubleProperty p2 = this.content.get("filter_bytes_" + filterName);
        p2.set(p2.get() + mb);
        SimpleDoubleProperty p3 = this.content.get("filter_time_" + filterName);
        p3.set(p3.get() + sec);
        SimpleDoubleProperty p4 = this.content.get("filter_speed_" + filterName);
        p4.set(p2.get() / p3.get());
    }

    public synchronized void newlyProcessedImage()  {
        SimpleDoubleProperty p1 = this.content.get("num_processed_images");
        p1.set(p1.get()+ 1);
    }

    public String toString(String pName) {
        SimpleDoubleProperty p = this.content.get(pName);
        if (pName.equals("num_completed_jobs") || pName.equals("num_processed_images")) {
            return Integer.toString((int)p.get());
        } else {
            return String.format("%.2f", p.get());
        }
    }
}