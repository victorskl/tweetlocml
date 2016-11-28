package com.sankholin.comp90049.project2;

import com.opencsv.CSVWriter;
import com.sankholin.comp90049.project2.feature.Classifier;
import com.sankholin.comp90049.project2.feature.NounPosClassifier;
import com.sankholin.comp90049.project2.feature.NounPosStemClassifier;
import com.sankholin.comp90049.project2.feature.PlaceNameClassifier;
import com.sankholin.comp90049.project2.model.TweetTerm;
import com.sankholin.comp90049.project2.tool.Utilities;
import org.apache.commons.collections4.map.HashedMap;
import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.builder.fluent.Configurations;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class App {

    @Option(name = "-a", usage = "pos=Part-of-Speech,place=Place Name,stem=Stemming")
    private String algorithm = "pos";

    @Option(name = "-c", usage = "config file")
    private File configFile = new File("./config.properties");

    @Option(name = "-o", usage = "output to this file", metaVar = "OUTPUT")
    private String out = "output";

    @Option(name = "-d", usage = "Dryrun for first few lines up to d")
    private int dryRun = 0;

    @Option(name = "-i", usage = "Start index of Tweet")
    private int startIdx = 0;

    @Option(name = "--preprocess", usage = "parted=Partition Tweets,gaze=Re-process Gazetteer")
    private String preProcess = null;

    @Option(name = "--arff", usage = "arff=train Or dev Or test")
    private String arff = null;

    @Option(name = "--mastering", usage = "mastering= path to first parted directory")
    private String mastering = null;

    private Configuration config;
    private List<String> tweetsDev, tweetsTrain, tweetsTest, gazetteer;

    private Classifier classifier;

    private Utilities util = Utilities.getInstance();

    private Map<String, Integer> uniqueUserMap;

    public static void main(String[] args) {
        new App().doMain(args);
    }

    private void doMain(String[] args) {

        CmdLineParser parser = new CmdLineParser(this);
        logger.info("Parsing args...");
        try {
            parser.parseArgument(args);
        } catch (CmdLineException e) {
            e.printStackTrace();
            return;
        }

        logger.info("option: -o " + out);
        logger.info("option: -c " + configFile.toString());
        logger.info("Reading config file: " + configFile.toString());
        Configurations configs = new Configurations();
        try {
            config = configs.properties(configFile);
        } catch (ConfigurationException e) {
            e.printStackTrace();
        }

        //--

        if (mastering != null) {
            logger.info("option: --mastering " + mastering);
            logger.warn("Redirect to mastering parted files. Init...");
            if (!mastering.equalsIgnoreCase("")) {
                new MasterParted(mastering, out);
            }
            else {
                logger.error("Unrecognized mastering argument. '--mastering [" + mastering + "]'");
                return;
            }

            logger.info("DONE!");
            //logger.info("Master file is saved under " + out + ".csv");
            logger.info("Please re-run application without '--mastering' option to continue main routine.");
            return;
        }

        //--

        logger.info("option: -a " + algorithm);
        logger.info("option: -d " + dryRun);
        logger.info("option: -i " + startIdx);

        if (preProcess != null) {
            logger.info("option: --preprocess " + preProcess);
            logger.warn("Redirect to pre-processing steps. Init...");
            PreProcessor processor = new PreProcessor(config);
            if (preProcess.equalsIgnoreCase("parted")) {
                logger.warn("Tweet partition option is given. Please re-invoke application after partition has done.");
                processor.partitionTweetFile();
                logger.info("DONE!");
                logger.info("Tweet partition files are saved under " + config.getString("tweets.partition.dir"));
                logger.info("You might want to point to the new parted tweet file before next invocation.");
                logger.info("\te.g. at config.properties, change 'tweets.train=../data/tweets/partitions/train-tweets-0.txt'");
                logger.info("Please re-run application without '--preprocess parted' option to continue main routine.");
                return;
            }
            else if (preProcess.equalsIgnoreCase("gaze")) {
                processor.reprocessGazetteer();
                logger.info("Gazetteer pre-processing has done. Continue with main routine...");
            }
            else {
                logger.error("Unrecognized PreProcessor argument. '--preprocess " + processor + "'");
                return;
            }
        }

        File gazetteerFile = new File(config.getString("gazetteer.preprocessed"));
        if (!gazetteerFile.canRead()) {
            logger.error("The preprocessed gazetteer file is not found. Please run with option '--preprocess gaze'");
            logger.error("This Gazetteer preprocessing is only need to be done ONCE.");
            return;
        }

        //--

        String tweetDevFileString = config.getString("tweets.dev");
        String tweetTrainFileString = config.getString("tweets.train");
        String tweetTestFileString = config.getString("tweets.test");

        try {
            tweetsDev = FileUtils.readLines(new File(tweetDevFileString), "UTF-8");
            tweetsTest = FileUtils.readLines(new File(tweetTestFileString), "UTF-8");
            tweetsTrain = FileUtils.readLines(new File(tweetTrainFileString), "UTF-8");
            gazetteer = FileUtils.readLines(gazetteerFile, "UTF-8");
        } catch (IOException e) {
            e.printStackTrace();
        }

        logger.info("Tweet dev: " + tweetDevFileString + "\t[" + tweetsDev.size() + "]");
        logger.info("Tweet test: " + tweetTestFileString + "\t[" + tweetsTest.size() + "]");
        logger.info("Tweet train: " + tweetTrainFileString + "\t[" + tweetsTrain.size() + "]");
        logger.info("Gazetteer: " + gazetteerFile.getName() + "\t[" + gazetteer.size() + "]");

        //--

        if (arff != null) {
            logger.info("option: --arff " + arff);
            logger.warn("Creating arff option is given. Please re-invoke application after arff creating has done.");

            try {

                out = arff + "-twitter-loc-" + out;

                File attrFile = new File(config.getString("attributes"));
                if (!attrFile.canRead()) {
                    logger.error("The attribute file is not found. Please set attribute file location at config.properties");
                    return;
                }

                List<String> attributes = FileUtils.readLines(attrFile, "UTF-8");

                if (arff.equalsIgnoreCase("train")) {
                    new WekaProcessor(tweetsTrain, attributes, out, arff);
                }
                else if (arff.equalsIgnoreCase("test")) {
                    new WekaProcessor(tweetsTest, attributes, out, arff);
                }
                else if (arff.equalsIgnoreCase("dev")) {
                    new WekaProcessor(tweetsDev, attributes, out, arff);
                }

                else {
                    logger.error("Unrecognized arff argument. '--arff " + arff + "'");
                    return;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            logger.info("DONE!");
            logger.info("Arff is saved under " + out + ".arff");
            logger.info("Please re-run application without '--arff' option to continue main routine.");

            return;
        }

        //--

        if (algorithm.equalsIgnoreCase("place")) {
            classifier = new PlaceNameClassifier(config, gazetteer);
        }
        if (algorithm.equalsIgnoreCase("pos")) {
            classifier = new NounPosClassifier(config, gazetteer);
        }
        if (algorithm.equalsIgnoreCase("stem")) {
            classifier = new NounPosStemClassifier(config, gazetteer);
        }

        if (classifier == null) {
            logger.error("Unrecognized classifier algorithm option '-a " + algorithm  + "'. Halt!");
            return;
        }

        start(tweetsTrain);
    }

    private void start(List<String> tweets) {
        if (startIdx < 0) startIdx = 0;
        if (startIdx > 0 && dryRun > 0) dryRun = dryRun + startIdx;

        Map<String, TweetTerm> bMap = new HashedMap<>();
        Map<String, TweetTerm> hMap = new HashedMap<>();
        Map<String, TweetTerm> sdMap = new HashedMap<>();
        Map<String, TweetTerm> seMap = new HashedMap<>();
        Map<String, TweetTerm> wMap = new HashedMap<>();
        uniqueUserMap = new HashedMap<>();

        for (int i = startIdx; i < tweets.size(); i++) {

            if (dryRun > 0 && i == dryRun) break; //just test first few lines

            String tweet = tweets.get(i);

            String[] aRawTweet = tweet.split("\\t");
            if (aRawTweet.length != 4) {
                String msg = "Tweet anomaly at index: [" + i + "] " + "\t\t" + tweet;
                logger.warn(msg);
                continue;
            }

            logger.info("Processing tweet [" + i + "] [" + aRawTweet[3] + "]\t" + aRawTweet[2]);

            String userId = aRawTweet[0];
            String tweetText = sanitizing(aRawTweet[2]);

            Integer uOccur = uniqueUserMap.get(userId);
            if (uOccur == null) {
                uniqueUserMap.put(userId, 1);
            } else {
                uniqueUserMap.put(userId, uOccur + 1);
            }

            switch (aRawTweet[3]) {
                case "B":
                    classifier.updateOccurrenceMap(bMap, tweetText, userId);
                    break;
                case "H":
                    classifier.updateOccurrenceMap(hMap, tweetText, userId);
                    break;
                case "SD":
                    classifier.updateOccurrenceMap(sdMap, tweetText, userId);
                    break;
                case "Se":
                    classifier.updateOccurrenceMap(seMap, tweetText, userId);
                    break;
                case "W":
                    classifier.updateOccurrenceMap(wMap, tweetText, userId);
                    break;
            }
        }

        printOccurrenceMap(util.sortByValue(bMap), "B");
        printOccurrenceMap(util.sortByValue(hMap), "H");
        printOccurrenceMap(util.sortByValue(sdMap), "SD");
        printOccurrenceMap(util.sortByValue(seMap), "Se");
        printOccurrenceMap(util.sortByValue(wMap), "W");

        logger.info("DONE!");
    }

    private String sanitizing(String tweet) {
        // Remove urls
        tweet = tweet.replaceAll(URL_REGEX, "");

        // Remove @username
        tweet = tweet.replaceAll("@([^\\s]+)", "");

        // Remove character repetition
        //tweet = tweet.replaceAll(CONSECUTIVE_CHARS, "$1");

        // Remove words starting with a number
        //tweet = tweet.replaceAll(STARTS_WITH_NUMBER, "");

        // Escape HTML
        //tweet = tweet.replaceAll("&amp;", "&");
        //tweet = StringEscapeUtils.unescapeHtml4(tweet);

        return tweet;
    }

    private void printOccurrenceMap(Map<String, TweetTerm> oMap, String city) {
        CSVWriter writer = null;
        try {
            writer = new CSVWriter(new FileWriter(out.concat("_").concat(city).concat(".csv")));
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (writer == null) {
            System.out.println("writer is null...");
            return;
        }

        //System.out.println("Total size: " + oMap.size());
        writer.writeNext(new String[]{"Total terms size", oMap.size()+""});
        writer.writeNext(new String[]{"Total unique users", uniqueUserMap.size()+""});
        writer.writeNext(new String[]{"Term Frequency", "Term", "Unique User Frequency"});

        Set<String> uniqueUserByCity = new HashSet<>();

        for (Map.Entry entry : oMap.entrySet()) {
            String key = (String) entry.getKey();
            TweetTerm value = (TweetTerm) entry.getValue();
            uniqueUserByCity.addAll(value.getUsers());
            writer.writeNext(new String[]{value.getCount()+"", key, value.getUsers().size()+""});
        }
        writer.writeNext(new String[]{"Total Unique User By City", uniqueUserByCity.size()+""});

        try {
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private final static String URL_REGEX = "((www\\.[\\s]+)|(https?://[^\\s]+))";
    private final static String CONSECUTIVE_CHARS = "([a-z])\\1{1,}";
    private final static String STARTS_WITH_NUMBER = "[1-9]\\s*(\\w+)";

    private static final Logger logger = LogManager.getLogger(App.class);
}
