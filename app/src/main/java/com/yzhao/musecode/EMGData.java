package com.yzhao.musecode;

import java.util.LinkedList;

/**
 * Created by Liam on 11/12/2016.
 */

public class EMGData {

    private final int LENGTH = 5; // 5/50 = .1 seconds minimum between actions

    LinkedList<Boolean> data; // holds our boolean values (whether a muscle movement occured or not)

    public EMGData() {
        data = new LinkedList<>(); // initialize data
    }

    public void add(boolean on) {
        data.add(on); // add new data point
        if(data.size() > LENGTH) { // if we go beyond the size of our LinkedList, get rid of the oldest datapoint
            data.poll();
        }
    }

    public boolean actionOccured(int threshold) {
        int totOn = 0;
        if(data.size() == LENGTH) { // if we have sufficient data points, check if we have at least two true values
            for(int i = 0; i<LENGTH; i++) {
                if(data.get(i)) {
                    totOn++;
                }
            }
            if(totOn >= threshold) {
                return true;
            }
        }
        return false;
    }
}
