package com.sankholin.comp90049.project2;

import info.debatty.java.stringsimilarity.KShingling;
import info.debatty.java.stringsimilarity.NGram;
import info.debatty.java.stringsimilarity.StringProfile;
import org.junit.Ignore;
import org.junit.Test;

public class StringSimTest {

    @Test @Ignore
    public void shingle() throws Exception {
        String s1 = "Boston, the W Barcelona opens October 2009";
        String s2 = "millennium bostonian hotel boston";

        // Let's work with sequences of n characters...
        KShingling ks = new KShingling(5);

        // For cosine similarity I need the profile of strings
        StringProfile profile1 = ks.getProfile(s1);
        StringProfile profile2 = ks.getProfile(s2);

        // Prints 0.516185
        System.out.println(profile1.cosineSimilarity(profile2));
    }

    @Test @Ignore
    public void ngram() {

        // produces 0.416666
        NGram twogram = new NGram(2);
        System.out.println(twogram.distance("ABCD", "ABTUIO"));

        // produces 0.97222
        String s1 = "Boston, the W Barcelona opens October 2009";
        String s2 = "millennium bostonian hotel boston";
        NGram ngram = new NGram(2);
        System.out.println(ngram.distance(s1, s2));
    }
}
