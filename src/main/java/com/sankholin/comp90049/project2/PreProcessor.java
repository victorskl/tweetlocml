package com.sankholin.comp90049.project2;

import com.sankholin.comp90049.project2.tool.GazetteerAnalyzer;
import com.sankholin.comp90049.project2.tool.Utilities;
import org.apache.commons.configuration2.Configuration;
import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PreProcessor {

    private Configuration config;
    private GazetteerAnalyzer gazetteerAnalyzer = new GazetteerAnalyzer();
    private Utilities util = Utilities.getInstance();
    private int minCharLocationName = 2;
    private int partitionPercent = 1;

    public PreProcessor(Configuration config) {
        this.config = config;
        minCharLocationName = config.getInt("mincharlocationname");
        partitionPercent = config.getInt("tweets.partition.percent");
    }

    public void partitionTweetFile() {
        try {
            String partitionDir = config.getString("tweets.partition.dir");
            if (!partitionDir.endsWith("/")) partitionDir = partitionDir.concat("/");

            File tweetsFile = new File(config.getString("tweets.train"));
            List<String> tweets = FileUtils.readLines(tweetsFile, "UTF-8");
            int total = tweets.size();
            logger.info("\t..::Tweet Partition Scheme::..");
            logger.info("  Total:\t" + total);

            int percent = Math.toIntExact(Math.round(total * partitionPercent / 100));
            logger.info("Percent:\t" + percent + " (" + partitionPercent + "%)");

            int idx=0;
            for (int i=0; i<total;) {

                int tail = i + percent;
                if (tail > total) tail = total;
                logger.info("Writing:\t\t" + i + "\t\t->\t\t" +tail);

                List<String> out = tweets.subList(i, tail);
                FileUtils.writeLines(new File(partitionDir+"train-tweets-"+idx+".txt"), "UTF-8", out);

                i = i + percent; idx++;
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void reprocessGazetteer() {
        try {

            File gazetteerFile = new File(config.getString("gazetteer"));

            List<String> gazetteer = FileUtils.readLines(gazetteerFile, "UTF-8");
            logger.info("Total Size: " + gazetteer.size()); // size

            logger.info("head: [" +gazetteer.get(0)+"]"); // head
            logger.info(" mid: [" +gazetteer.get(gazetteer.size() / 2)+"]"); // mid
            logger.info("tail: [" +gazetteer.get(gazetteer.size() - 1)+"]"); // tail

            //gazetteer is sorted but make sure we sort it again, see
            Collections.sort(gazetteer);

            logger.info("After sorting...");

            // check again
            logger.info("head: [" +gazetteer.get(0) +"]");
            logger.info(" mid: [" +gazetteer.get(gazetteer.size() / 2)+"]"); // mid
            logger.info("tail: [" +gazetteer.get(gazetteer.size() - 1)+"]"); // tail

            // try to re-process the way we want

            logger.info("Applying GazetteerAnalyzer...");

            List<String> gazetteerNew = new ArrayList<>();

            for (String s : gazetteer) {
                s = String.join(" ", util.tokenizeString(gazetteerAnalyzer, s));
                if (s.length() < minCharLocationName) continue;
                gazetteerNew.add(s);
            }

            Collections.sort(gazetteerNew);

            logger.info("head: [" +gazetteerNew.get(0)+"]");
            logger.info(" mid: [" +gazetteerNew.get(gazetteerNew.size() / 2)+"]"); // mid
            logger.info("tail: [" +gazetteerNew.get(gazetteerNew.size() - 1)+"]"); // tail

            File nuFile = new File(config.getString("gazetteer.preprocessed"));
            logger.info("Writing to file... \t" + nuFile.getPath());
            FileUtils.writeLines(nuFile, "UTF-8", gazetteerNew);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static final Logger logger = LogManager.getLogger(PreProcessor.class);
}
