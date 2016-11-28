package com.sankholin.comp90049.project2.tool;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;

import java.io.IOException;
import java.io.StringReader;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Singleton Utilities
 */
public final class Utilities {

    private static Utilities instance = null;

    private Utilities() {
    }

    public static synchronized Utilities getInstance() {
        if (instance == null) instance = new Utilities();
        return instance;
    }

    public List<String> tokenizeString(Analyzer analyzer, String string) {
        return tokenizeString(analyzer, null, string);
    }

    private List<String> tokenizeString(Analyzer analyzer, String field, String string) {
        List<String> result = new ArrayList<>();
        try {
            TokenStream stream = analyzer.tokenStream(field, new StringReader(string));
            stream.reset();
            while (stream.incrementToken()) {
                result.add(stream.getAttribute(CharTermAttribute.class).toString());
            }
            stream.end();
            stream.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return result;
    }

    public int min(int a, int b, int... more) {
        int min = Math.min(a, b);
        for (int m : more)
            min = Math.min(min, m);
        return min;
    }

    public int max(int a, int b, int... more) {
        int max = Math.max(a, b);
        for (int m : more)
            max = Math.max(max, m);
        return max;
    }

    /**
     * Remove all non alphabet character
     */
    public String removeAllNonAlphabets(String s) {
        return s.replaceAll("[^a-zA-Z]", "");
    }

    /**
     * https://www.mkyong.com/java/how-to-sort-a-map-in-java/
     *
     * @param unsortMap
     * @param <K>
     * @param <V>
     * @return
     */
    public <K, V extends Comparable<? super V>> Map<K, V> sortByValue1(Map<K, V> unsortMap) {

        List<Map.Entry<K, V>> list =
                new LinkedList<Map.Entry<K, V>>(unsortMap.entrySet());

        Collections.sort(list, new Comparator<Map.Entry<K, V>>() {
            public int compare(Map.Entry<K, V> o1, Map.Entry<K, V> o2) {
                return (o1.getValue()).compareTo(o2.getValue());
            }
        });

        Map<K, V> result = new LinkedHashMap<K, V>();
        for (Map.Entry<K, V> entry : list) {
            result.put(entry.getKey(), entry.getValue());
        }

        return result;
    }

    /**
     * http://stackoverflow.com/questions/109383/sort-a-mapkey-value-by-values-java
     *
     * @param map
     * @param <K>
     * @param <V>
     * @return
     */
    public <K, V extends Comparable<? super V>> Map<K, V> sortByValue(Map<K, V> map) {
        return map.entrySet()
                .stream()
                .sorted(Map.Entry.comparingByValue(Collections.reverseOrder()))
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (e1, e2) -> e1,
                        LinkedHashMap::new
                ));
    }

    private static final Logger logger = LogManager.getLogger(Utilities.class);
}
