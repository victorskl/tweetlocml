package com.sankholin.comp90049.project2.tool;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.core.LowerCaseFilter;

public class GazetteerAnalyzer extends Analyzer {

    public GazetteerAnalyzer() {
    }

    @Override
    protected TokenStreamComponents createComponents(String fieldName) {
        GazetteerTokenizer src = new GazetteerTokenizer();
        TokenStream tok = new LowerCaseFilter(src);
        return new TokenStreamComponents(src, tok);
    }
}
