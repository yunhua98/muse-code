package com.yzhao.musecode;

import java.util.LinkedList;

/**
 * Created by yunhuazhao on 11/12/16.
 */

public class SignalQueue { // just a wrapper for queue/linkedlist with alternate constructor to make dictionary construciton easier

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

    public char get(int i) { return q.get(i); }

    public char peekLast() { return q.peekLast(); }

    public String toString() {
        String output = "";
        for (char c : q) output += c;
        return output;
    }

    @Override
    public boolean equals(Object rhs) {
        if (rhs == null) return false;
        SignalQueue temp = (SignalQueue) rhs;
        if (temp.size() != q.size()) return false;
        for (int i = 0; i < q.size(); ++i) {
            char s1 = q.get(i);
            char s2 = temp.get(i);
            if (s1 != s2) return false;
        }
        return true;
    }


}
