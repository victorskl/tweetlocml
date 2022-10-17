package com.sankholin.comp90049.project2;

import info.debatty.java.stringsimilarity.Cosine;
import info.debatty.java.stringsimilarity.NGram;
import org.junit.Ignore;
import org.junit.Test;

import java.util.Map;

public class StringSimTest {

    @Test @Ignore
    public void shingle() throws Exception {
        // Example from https://github.com/tdebatty/java-string-similarity#shingle-n-gram-based-algorithms

        String s1 = "Boston, the W Barcelona opens October 2009";
        String s2 = "millennium bostonian hotel boston";

        // Let's work with sequences of 2 characters...
        Cosine cosine = new Cosine(2);

        // Pre-compute the profile of strings
        Map<String, Integer> profile1 = cosine.getProfile(s1);
        Map<String, Integer> profile2 = cosine.getProfile(s2);

        // Prints 0.516185
        System.out.println(cosine.similarity(profile1, profile2));
    }

    @Test @Ignore
    public void ngram() {
        // Example from https://github.com/tdebatty/java-string-similarity#n-gram

        // produces 0.416666
        NGram twoGram = new NGram(2);
        System.out.println(twoGram.distance("ABCD", "ABTUIO"));

        // produces 0.97222
        String s1 = "Boston, the W Barcelona opens October 2009";
        String s2 = "millennium bostonian hotel boston";
        NGram ngram = new NGram(2);
        System.out.println(ngram.distance(s1, s2));
    }
}
