package com.sankholin.comp90049.project2;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.shingle.ShingleFilter;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.search.spell.NGramDistance;
import org.junit.Ignore;
import org.junit.Test;

import java.io.IOException;
import java.io.StringReader;

public class LuceneTest {

    @Test @Ignore
    public void shingleTest() throws IOException {
        Analyzer analyzer = new StandardAnalyzer();

        String theSentence = "please divide this sentence into shingles";
        StringReader reader = new StringReader(theSentence);
        TokenStream tokenStream = analyzer.tokenStream("content", reader);
        ShingleFilter theFilter = new ShingleFilter(tokenStream);

        //theFilter.setOutputUnigrams(false);

        CharTermAttribute charTermAttribute = theFilter.addAttribute(CharTermAttribute.class);
        theFilter.reset();

        System.out.println(charTermAttribute.length());

        while (theFilter.incrementToken()) {
            System.out.println(charTermAttribute.toString());
        }

        theFilter.end();
        theFilter.close();
    }

    @Test @Ignore
    public void ngramTest() {
        String s1 = "Boston, the W Barcelona opens October 2009";
        String s2 = "millennium bostonian hotel boston";

        NGramDistance nGramDistance = new NGramDistance(2);
        System.out.println(nGramDistance.getDistance(s1, s2)); //0.1666666865348816 ?
    }
}
