package com.yzhao.musecode;

/**
 * Created by yunhuazhao on 11/12/16.
 */

public class Signal {

    private boolean isDot;
    private boolean isDash;

    // default ctor: type = true for blink, false for clench
    public Signal(boolean type) {
        isDot = type;
        isDash = !type;
    }

    // overload ctor: takes char
    public Signal (char signal) {
        assert signal == '.' || signal == '-';
        if (signal == '.') {
            isDot = true;
            isDash = false;
        } else {
            isDot = false;
            isDash = true;
        }
    }

    public boolean isDot() {
        return isDot;
    }

    public boolean isDash() {
        return isDash;
    }

}
