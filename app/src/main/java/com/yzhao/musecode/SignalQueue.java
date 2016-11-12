package com.yzhao.musecode;

import java.util.LinkedList;

/**
 * Created by yunhuazhao on 11/12/16.
 */

public class SignalQueue {

    private LinkedList<Signal> q;

    public SignalQueue() {
        q = new LinkedList<>();
    }

    // string constructor
    public SignalQueue(String code) {
        q = new LinkedList<>();
        for (int i = 0; i < code.length(); ++i) {
            q.add(new Signal(code.charAt(i)));
        }
    }

    public void add(Signal s) {
        q.add(s);
    }

    public Signal poll() {
        return q.poll();
    }

    public Signal peek() {
        return q.peek();
    }

    public void clear() {
        q.clear();
    }

    public int size() {
        return q.size();
    }

    @Override
    public boolean equals(Object rhs) {
        if (rhs == null) return false;
        SignalQueue temp = (SignalQueue) rhs;
        if (temp.size() != q.size()) return false;
        for (Signal s1 : q) {
            Signal s2 = temp.poll();
            if (s1.isDash() != s2.isDash()) return false;
        }
        return true;
    }

}
