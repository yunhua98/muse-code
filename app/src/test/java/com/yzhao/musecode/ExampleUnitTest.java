package com.yzhao.musecode;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {
    @Test
    public void addition_isCorrect() throws Exception {
        assertEquals(4, 2 + 2);
    }

    @Test
    public void dictionary_isCorrect() throws Exception {
        SignalQueue letter = new SignalQueue("..-");
        MorseDictionary dict = new MorseDictionary();
        assertEquals('U', dict.translate(letter));
    }
}