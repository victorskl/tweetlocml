package com.sankholin.comp90049.project2.feature;

import com.sankholin.comp90049.project2.model.TweetTerm;
import com.sankholin.comp90049.project2.tool.TweetAnalyzer;
import com.sankholin.comp90049.project2.tool.Utilities;
import opennlp.tools.postag.POSModel;
import opennlp.tools.postag.POSTaggerME;
import org.apache.commons.configuration2.Configuration;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.output.StringBuilderWriter;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.util.CharArraySet;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class AbstractClassifier {

    protected Utilities util = Utilities.getInstance();
    protected Analyzer analyzer;
    private POSModel model;
    protected CharArraySet stopSet;

    private Configuration config;
    protected List<String> gazetteer;

    private final static String[] NOUN_TAGS = {"NN", "NNS", "NNP", "NNPS"};
    public final static int MIN_TOKEN_LENGTH = 2;

    public AbstractClassifier(Configuration config, List<String> gazetteer) {
        this.config = config;
        this.gazetteer = gazetteer;
        analyzer = new TweetAnalyzer(stopSet);
        buildStopWords();
        setupPosModel();
    }

    private void buildStopWords() {
        String stopwordsString = config.getString("stopwords.std319");
        try {
            List<String> stops = FileUtils.readLines(new File(stopwordsString), "UTF-8");
            stopSet = CharArraySet.copy(StandardAnalyzer.STOP_WORDS_SET);
            stopSet.addAll(stops);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void setupPosModel() {
        InputStream modelIn = null;

        try {
            modelIn = new FileInputStream(config.getString("opennlp.model.pos.en"));
            model = new POSModel(modelIn);
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        finally {
            if (modelIn != null) {
                try {
                    modelIn.close();
                }
                catch (IOException ignored) {
                }
            }
        }
    }

    protected List<String> posNounFilter(String[] tokens) {
        List<String> filterStrings = new ArrayList<>();

        POSTaggerME tagger = new POSTaggerME(model);

        //String[] strings = tokens.stream().toArray(String[]::new);
        String tags[] = tagger.tag(tokens);

        int idx = 0;
        for (String s : tokens) {
            if (Arrays.stream(NOUN_TAGS).anyMatch(tags[idx]::contains)) {
                //System.out.println(s);
                filterStrings.add(s);
            }
            idx++;
        }

        return filterStrings;
    }

    protected List<String> posNounFilter(List<String> tokens) {
        List<String> filterStrings = new ArrayList<>();

        if (model == null) return filterStrings;

        POSTaggerME tagger = new POSTaggerME(model);

        String[] strings = tokens.stream().toArray(String[]::new);
        String tags[] = tagger.tag(strings);

        int idx = 0;
        for (String s : tokens) {
            if (Arrays.stream(NOUN_TAGS).parallel().anyMatch(tags[idx]::contains)) {
                //System.out.println(s);
                filterStrings.add(s);
            }
            idx++;
        }

        return filterStrings;
    }

    protected void collectTerms(Map<String, TweetTerm> oMap, String token, String userId) {
        TweetTerm tweetTerm = oMap.get(token);

        if (tweetTerm == null) {
            tweetTerm = new TweetTerm(token);
            tweetTerm.setCount(1);
            oMap.put(token, tweetTerm);
            tweetTerm.getUsers().add(userId);
        } else {
            tweetTerm.setCount(tweetTerm.getCount() + 1);
            //oMap.put(token, count + 1);
            if (!tweetTerm.getUsers().contains(userId)) {
                tweetTerm.getUsers().add(userId);
            }
        }
    }
}
