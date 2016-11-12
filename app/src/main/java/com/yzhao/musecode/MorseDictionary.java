package com.yzhao.musecode;

import java.util.HashMap;
import java.util.Iterator;

/**
 * Created by Stanley on 11/12/2016.
 */



public class MorseDictionary {
    HashMap<Character, SignalQueue> morseAlpha = new HashMap<>();

    public MorseDictionary() {
        morseAlpha.put('A', new SignalQueue(".-"));
        morseAlpha.put('B', new SignalQueue("-..."));
        morseAlpha.put('C', new SignalQueue("-.-."));
        morseAlpha.put('D', new SignalQueue("-.."));
        morseAlpha.put('E', new SignalQueue("."));
        morseAlpha.put('F', new SignalQueue("..-."));
        morseAlpha.put('G', new SignalQueue("--."));
        morseAlpha.put('H', new SignalQueue("...."));
        morseAlpha.put('I', new SignalQueue(".."));
        morseAlpha.put('J', new SignalQueue(".---"));
        morseAlpha.put('K', new SignalQueue("-.-"));
        morseAlpha.put('L', new SignalQueue(".-.."));
        morseAlpha.put('M', new SignalQueue("--"));
        morseAlpha.put('N', new SignalQueue("-."));
        morseAlpha.put('O', new SignalQueue("---"));
        morseAlpha.put('P', new SignalQueue(".--."));
        morseAlpha.put('Q', new SignalQueue("--.-"));
        morseAlpha.put('R', new SignalQueue(".-."));
        morseAlpha.put('S', new SignalQueue("..."));
        morseAlpha.put('T', new SignalQueue("-"));
        morseAlpha.put('U', new SignalQueue("..-"));
        morseAlpha.put('V', new SignalQueue("...-"));
        morseAlpha.put('W', new SignalQueue(".--"));
        morseAlpha.put('X', new SignalQueue("-..-"));
        morseAlpha.put('Y', new SignalQueue("-.--"));
        morseAlpha.put('Z', new SignalQueue("--.."));
    }

    public char translate(SignalQueue s) {
        Iterator it = morseAlpha.keySet().iterator();
        while (it.hasNext()) {
            char letter = (char) it.next();
            if (s.equals(morseAlpha.get(letter))) return letter;
        }
        Character m = Character.MIN_VALUE;
        return m;
    }
}
