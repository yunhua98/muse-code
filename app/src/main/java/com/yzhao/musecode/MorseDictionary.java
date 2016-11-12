package com.yzhao.musecode;

import java.util.HashMap;

/**
 * Created by Stanley on 11/12/2016.
 */



public class MorseDictionary {
    HashMap<SignalQueue, Character> morseAlpha = new HashMap<>();

    public MorseDictionary() {
        morseAlpha.put(new SignalQueue(".-"), 'A');
        morseAlpha.put(new SignalQueue("-..."), 'B');
        morseAlpha.put(new SignalQueue("-.-."), 'C');
        morseAlpha.put(new SignalQueue("-.."), 'D');
        morseAlpha.put(new SignalQueue("."), 'E');
        morseAlpha.put(new SignalQueue("..-."), 'F');
        morseAlpha.put(new SignalQueue("--."), 'G');
        morseAlpha.put(new SignalQueue("...."), 'H');
        morseAlpha.put(new SignalQueue(".."), 'I');
        morseAlpha.put(new SignalQueue(".---"), 'J');
        morseAlpha.put(new SignalQueue("-.-"), 'K');
        morseAlpha.put(new SignalQueue(".-.."), 'L');
        morseAlpha.put(new SignalQueue("--"), 'M');
        morseAlpha.put(new SignalQueue("-."), 'N');
        morseAlpha.put(new SignalQueue("---"), 'O');
        morseAlpha.put(new SignalQueue(".--."), 'P');
        morseAlpha.put(new SignalQueue("--.-"), 'Q');
        morseAlpha.put(new SignalQueue(".-."), 'R');
        morseAlpha.put(new SignalQueue("..."), 'S');
        morseAlpha.put(new SignalQueue("-"), 'T');
        morseAlpha.put(new SignalQueue("..-"), 'U');
        morseAlpha.put(new SignalQueue("...-"), 'V');
        morseAlpha.put(new SignalQueue(".--"), 'W');
        morseAlpha.put(new SignalQueue("-..-"), 'X');
        morseAlpha.put(new SignalQueue("-.--"), 'Y');
        morseAlpha.put(new SignalQueue("--.."), 'Z');
    }

    public char translate(SignalQueue s) {
        if (morseAlpha.containsKey(s)) {
            return morseAlpha.get(s);
        }
        Character m = Character.MIN_VALUE;
        return m;
    }
}
