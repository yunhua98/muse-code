package com.yzhao.musecode;

import java.util.LinkedList;

/**
 * Created by yunhuazhao on 11/12/16.
 */

public class SignalQueue {

    private LinkedList<Character> q;

    public SignalQueue() {
        q = new LinkedList<>();
    }

    // string constructor
    public SignalQueue(String code) {
        q = new LinkedList<>();
        for (int i = 0; i < code.length(); ++i) {
            q.add(code.charAt(i));
        }
    }

    public void add(char signal) {
        q.add(signal);
    }

    public char poll() {
        return q.poll();
    }

    public char peek() {
        return q.peek();
    }

    public void clear() {
        q.clear();
    }

    public int size() {
        return q.size();
    }

    public char peekLast() { return q.peekLast(); }

    @Override
    public boolean equals(Object rhs) {
        if (rhs == null) return false;
        SignalQueue temp = (SignalQueue) rhs;
        if (temp.size() != q.size()) return false;
        for (char s1 : q) {
            char s2 = temp.poll();
            if (s1 != s2) return false;
        }
        return true;
    }


}
