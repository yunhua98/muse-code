package com.yzhao.musecode;

import java.util.LinkedList;

/**
 * Created by yunhuazhao on 11/12/16.
 */

public class AccelerometerData {

    private final int MAX_WINDOW = 50; // maximum size of linkedlist, data given 50 Hz
    private final int NUM_ABOVE_THRESH_REQUIRED = 3; // minimum number of values past threshold that we need to detect to count as a tilt
    private float threshold; // threshold of detection
    private int indexOfLastTilt;
    private LinkedList<Float> xQ;
    private boolean tiltedRecently; // whether the data has been tilted in last

    public AccelerometerData (float threshold) {
        this.threshold = threshold;
        xQ = new LinkedList<>();
        tiltedRecently = false;
        indexOfLastTilt = -1; // none detected yet
    }

    public void add(float x) {
        if (xQ.size() > MAX_WINDOW) xQ.poll();
        xQ.add(x);
        if (indexOfLastTilt >= 0) --indexOfLastTilt;
        if (indexOfLastTilt == -1) tiltedRecently = false;
    }

    public boolean isTilt() { // checks within last second for a value past threshold
        if (tiltedRecently) return false;
        int pastThreshCount = 0;
        for (int i = 0; i < xQ.size(); ++i) {
            float x = xQ.get(i);
            if (Math.abs(x) >= Math.abs(threshold) && x / threshold > 0) {
                ++pastThreshCount;
                indexOfLastTilt = i;
            }
        }
        if (pastThreshCount >= NUM_ABOVE_THRESH_REQUIRED) {
            tiltedRecently = true;
            return true;
        } else {
            return false;
        }
    }

}
