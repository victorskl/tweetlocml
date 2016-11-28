package com.sankholin.comp90049.project2.tool;

import org.apache.lucene.analysis.util.CharTokenizer;

public class GazetteerTokenizer extends CharTokenizer {

    @Override
    protected boolean isTokenChar(int c) {
        return Character.isLetterOrDigit(c);
    }
}