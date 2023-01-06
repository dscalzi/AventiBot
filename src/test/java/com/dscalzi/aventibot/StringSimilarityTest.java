package com.dscalzi.aventibot;

import info.debatty.java.stringsimilarity.JaroWinkler;
import org.junit.jupiter.api.Test;

import java.util.regex.Pattern;

public class StringSimilarityTest {

    @Test
    public void testStringSimilarity() {
        JaroWinkler jw = new JaroWinkler();
        System.out.println(jw.similarity(sanitize("Prayer - Original Extended mix "), sanitize("Prayer (Extended mix)")));
        System.out.println(jw.similarity("Stop Teasing My Heart", "Stop Teasing my Soul"));
        System.out.println(jw.similarity("Stop Teasing My Heart", "YES I WILL"));
    }

    public String sanitize(String s) {
        Pattern x = Pattern.compile("[^a-zA-Z0-9 ]");
        Pattern spaces = Pattern.compile(" +");
        return spaces.matcher(x.matcher(s).replaceAll("")).replaceAll(" ");
    }

}
