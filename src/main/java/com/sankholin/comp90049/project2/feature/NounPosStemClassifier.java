package com.sankholin.comp90049.project2.feature;

import com.sankholin.comp90049.project2.model.TweetTerm;
import opennlp.tools.tokenize.WhitespaceTokenizer;
import org.apache.commons.configuration2.Configuration;
import org.apache.lucene.analysis.en.EnglishAnalyzer;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class NounPosStemClassifier extends AbstractClassifier implements Classifier {

    public NounPosStemClassifier(Configuration config, List<String> gazetteer) {
        super(config, gazetteer);
        analyzer = new EnglishAnalyzer(stopSet);
    }

    @Override
    public void updateOccurrenceMap(Map<String, TweetTerm> oMap, String tweetText, String userId) {

        //System.out.println(tweetText);

        String wText[] = WhitespaceTokenizer.INSTANCE.tokenize(tweetText);
        List<String> tokenizeTweets = posNounFilter(wText);

        tokenizeTweets = util.tokenizeString(analyzer, String.join(" ", tokenizeTweets));

        //System.out.println(Arrays.toString(tokenizeTweets.toArray()));

        for (String token : tokenizeTweets) {

            if (token.length() < MIN_TOKEN_LENGTH) continue;

            collectTerms(oMap, token, userId);
        }
    }
}
