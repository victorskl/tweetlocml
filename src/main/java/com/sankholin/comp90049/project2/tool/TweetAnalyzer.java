package com.sankholin.comp90049.project2.tool;

import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.core.LowerCaseFilter;
import org.apache.lucene.analysis.core.StopAnalyzer;
import org.apache.lucene.analysis.core.StopFilter;
import org.apache.lucene.analysis.util.CharArraySet;
import org.apache.lucene.analysis.util.StopwordAnalyzerBase;

public class TweetAnalyzer extends StopwordAnalyzerBase {

    public static final CharArraySet STOP_WORDS_SET = StopAnalyzer.ENGLISH_STOP_WORDS_SET;

    public TweetAnalyzer(CharArraySet stopWords) {
        super(stopWords);
    }

    public TweetAnalyzer() {
        this(STOP_WORDS_SET);
    }

    @Override
    protected TokenStreamComponents createComponents(String fieldName) {
        TweetTokenizer src = new TweetTokenizer();
        TokenStream tok = new LowerCaseFilter(src);
        tok = new StopFilter(tok, stopwords);
        return new TokenStreamComponents(src, tok);
    }
}
