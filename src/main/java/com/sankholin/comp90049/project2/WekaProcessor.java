package com.sankholin.comp90049.project2;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import weka.core.*;
import weka.core.converters.ArffSaver;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class WekaProcessor {

    private List<String> stringList;
    private List<String> attributes;
    private String out;
    private String mode;
    private File output;

    public WekaProcessor(List<String> stringList, List<String> attributes, String out, String mode) {
        this.stringList = stringList;
        this.attributes = attributes;
        this.out = out;
        this.mode = mode;
        output = new File(out + ".arff");

        createArff();
    }

    private void createArff() {
        FastVector atts;
        //FastVector attsRel;
        FastVector attVals;
        //FastVector attValsRel;
        Instances data;
        //Instances dataRel;
        double[] vals;
        //double[] valsRel;

        // 1. set up attributes
        atts = new FastVector();

        atts.addElement(new Attribute("id"));

        for (String attribute : attributes) {
            // - numeric
            atts.addElement(new Attribute(attribute));
        }

        // - nominal
        String[] labels = {"B","H","SD","Se","W"};
        attVals = new FastVector();
        for (int i = 0; i < labels.length; i++)
            attVals.addElement(labels[i]);
        atts.addElement(new Attribute("location", attVals));


        // 2. create Instances object
        data = new Instances(out, atts, 0);

        // just for progress animation
        String anim = "|/-\\"; int animIdx = 0;

        // 3. fill with data
        for (String s : stringList) {

            //if (stringList.indexOf(s) == 5) break;

            String[] strings = s.split("\\t");
            if (strings.length != 4) {
                String msg = "Data anomaly at index: [" + stringList.indexOf(s) + "] " + "\t\t" + s;
                logger.warn(msg);
                continue;
            }

            int attrSize = data.numAttributes();

            vals = new double[attrSize];
            vals[0] = Double.parseDouble(strings[1]);

            for (int i = 0; i < attrSize-2; i++) {
                vals[i+1] = matches(strings[2].toLowerCase(), attributes.get(i));
            }

            if (mode.equalsIgnoreCase("test")) {
                vals[attrSize-1] = Utils.missingValue(); // Instance.missingValue() or Utils.missingValue() for Weka > 3.7.1
            } else {
                vals[attrSize-1] = attVals.indexOf(strings[3]);
            }

            data.add(new DenseInstance(1.0, vals));

            String animData = "\r" + anim.charAt(animIdx % anim.length())  + " " + animIdx;
            try {
                System.out.write(animData.getBytes());
            } catch (IOException e) {
                e.printStackTrace();
            }
            animIdx++;
        }

        // 4. output data
        //System.out.println(data);

        System.out.println();
        logger.info("Saving arff file...");

        ArffSaver saver = new ArffSaver();
        saver.setInstances(data);
        try {
            saver.setFile(output);
            saver.writeBatch();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private int matches(String text, String attr) {
        int count = 0;
        String regex = "\\b".concat(attr).concat("\\b");
        Pattern p = Pattern.compile(regex);
        Matcher m = p.matcher(text);
        while (m.find()) {
            count++;
        }
        return count;
    }

    private static final Logger logger = LogManager.getLogger(WekaProcessor.class);
}
