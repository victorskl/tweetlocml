package com.sankholin.comp90049.project2.feature;

import com.sankholin.comp90049.project2.model.TweetTerm;

import java.util.Map;

public interface Classifier {
    void updateOccurrenceMap(Map<String, TweetTerm> oMap, String tweetText, String userId);
}
