package com.sankholin.comp90049.project2;

import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.builder.fluent.Configurations;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class AppTest {

    private File configFile = new File("./config.properties");
    private Configuration config;
    private File gazetteerFile;

    @Before
    public void before() throws ConfigurationException {
        Configurations configs = new Configurations();
        config = configs.properties(configFile);
        gazetteerFile = new File(config.getString("gazetteer.preprocessed"));
    }

    @Test @Ignore
    public void testString() {
        String str = "hire, manager, project, manager, tech, evangelist, type, houston, candidate, houston, area";

        String firstChar = str.substring(0,1);

        System.out.println(firstChar);
    }

    @Test @Ignore
    public void buildGazeDict() throws IOException {
        List<String> gazetteer = FileUtils.readLines(gazetteerFile, "UTF-8");
        Map<String, List<String>> dictionary = new TreeMap<>();
        for (String s : gazetteer) {
            String idx = s.substring(0, 1);
            List<String> category = dictionary.get(idx);
            if (category == null) { // first time encounter
                category = new ArrayList<>();
                category.add(s);
                dictionary.put(idx, category);
            } else {
                category.add(s);
            }
        }

        System.out.println("dictionary category size: " + dictionary.size());
        System.out.println(Arrays.toString(dictionary.keySet().toArray()));

        int hsize = dictionary.get("h").size();
        System.out.println("h category size: " + hsize);
        System.out.println("h category peak: " + dictionary.get("h").get(0));
        System.out.println("h category peak: " + dictionary.get("h").get(hsize-1));
    }
}
