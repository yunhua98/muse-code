package com.yzhao.musecode;

import java.util.LinkedList;

/**
 * Created by yunhuazhao on 11/12/16.
 */

public class AccelerometerData {

    private final int MAX_WINDOW = 50; // maximum size of linkedlist, data given 50 Hz
    private final float THRESHOLD = -0.5f; // threshold of detection
    private int indexOfLastTilt;
    private LinkedList<Float> xQ;
    private boolean tiltedRecently; // whether the data has been tilted in last

    public AccelerometerData () {
        xQ = new LinkedList<>();
        tiltedRecently = false;
        indexOfLastTilt = -1; // none detected yet
    }

    public void add(float x) {
        if (xQ.size() > 10) xQ.poll();
        xQ.add(x);
        if (indexOfLastTilt >= 0) --indexOfLastTilt;
        if (indexOfLastTilt == -1) tiltedRecently = false;
    }

    public boolean isLeftTilt() { // checks within last second for a value past threshold
        if (tiltedRecently) return false;
        for (int i = 0; i < xQ.size(); ++i) {
            float x = xQ.get(i);
            if (x <= THRESHOLD) {
                indexOfLastTilt = i;
                tiltedRecently = true;
                return true;
            }
        }
        return false;
    }

}
