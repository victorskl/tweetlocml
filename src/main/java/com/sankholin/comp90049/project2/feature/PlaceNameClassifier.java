package com.sankholin.comp90049.project2.feature;

import com.sankholin.comp90049.project2.model.TweetTerm;
import opennlp.tools.tokenize.WhitespaceTokenizer;
import org.apache.commons.configuration2.Configuration;
import org.apache.lucene.search.spell.LevenshteinDistance;
import org.apache.lucene.search.spell.StringDistance;

import java.util.*;

public class PlaceNameClassifier extends AbstractClassifier implements Classifier {

    private StringDistance stringDistance;
    private Map<String, List<String>> dictionary;

    private final static int NGRAM_SIZE = 2;
    private final static float DISTANCE_THRESHOLD = 0.98f;

    public PlaceNameClassifier(Configuration config, List<String> gazetteer) {
        super(config, gazetteer);
        dictionary = new TreeMap<>();
        buildGazeDictionary();
        //stringDistance = new NGramDistance(NGRAM_SIZE);
        stringDistance = new LevenshteinDistance();
    }

    private void buildGazeDictionary() {
        for (String s : gazetteer) {
            String idx = s.substring(0, 1);
            List<String> category = dictionary.get(idx);
            if (category == null) {
                category = new ArrayList<>();
                category.add(s);
                dictionary.put(idx, category);
            } else {
                category.add(s);
            }
        }
    }

    @Override
    public void updateOccurrenceMap(Map<String, TweetTerm> oMap, String tweetText, String userId) {

        //System.out.println(tweetText);

        String wText[] = WhitespaceTokenizer.INSTANCE.tokenize(tweetText);
        List<String> tokenizeTweets = posNounFilter(wText);

        tokenizeTweets = util.tokenizeString(analyzer, String.join(" ", tokenizeTweets));

        //System.out.println(Arrays.toString(tokenizeTweets.toArray()));

        int totalTokenSize = tokenizeTweets.size();
        for (String tok : tokenizeTweets) {

            List<String> gazeEntries = dictionary.get(tok.substring(0,1));

            if (gazeEntries == null) continue;

            for (String aGazetteer : gazeEntries) {
                int gazeLength = aGazetteer.split(" ").length;

                if (totalTokenSize < gazeLength) {

                    String tweet = String.join(" ", tokenizeTweets);
                    if (tweet.length() < MIN_TOKEN_LENGTH) break;
                    float d = stringDistance.getDistance(aGazetteer, tweet);
                    if (d < DISTANCE_THRESHOLD) continue;
                    //System.out.println(tweet + ", " + aGazetteer + ", " + d);
                    collectTerms(oMap, tweet, userId);

                } else {

                    int i = tokenizeTweets.indexOf(tok);
                    int toIndex = i+gazeLength;
                    String tweet;
                    if (toIndex > totalTokenSize) {
                        tweet = String.join(" ", tokenizeTweets.subList(i, totalTokenSize));
                    } else {
                        tweet = String.join(" ", tokenizeTweets.subList(i, toIndex));
                    }

                    if (tweet.length() < MIN_TOKEN_LENGTH) break;
                    float d = stringDistance.getDistance(aGazetteer, tweet);
                    if (d < DISTANCE_THRESHOLD) continue;
                    //System.out.println(tweet + ", " + aGazetteer + ", " + d);
                    collectTerms(oMap, tweet, userId);
                }
            }
            //--
        }
    }
}
