package com.sankholin.comp90049.project2;

import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;
import com.opencsv.exceptions.CsvException;
import com.sankholin.comp90049.project2.model.PartedTweetTerm;
import com.sankholin.comp90049.project2.tool.Utilities;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MasterParted {

    private String mastering;
    private String out;
    private Utilities util = Utilities.getInstance();

    public MasterParted(String mastering, String out) {
        this.mastering = mastering;
        this.out = out;

        //--
        performMastering();
    }

    public void performMastering() {
        //../data/feature/place/parted/train00
        String[] ss = mastering.split("/");

        String lastString = ss[ss.length-1];
        //System.out.println(lastString);

        StringBuilder r = new StringBuilder();
        for (int k = 0; k < lastString.length(); k++) {
            if(Character.isLetter(lastString.charAt(k)))
                r.append(lastString.charAt(k));
        }
        //System.out.println(r.toString());

        String[] sss = Arrays.copyOf(ss, ss.length-1);
        //System.out.println(Arrays.toString(sss));
        StringBuilder sb = new StringBuilder();
        for (String s : sss) {
            sb.append(s);
            sb.append("/");
        }
        //System.out.println(sb.toString());

        File partedBase = new File(sb.toString());
        int parts = partedBase.listFiles().length;
        //System.out.println(parts);

        String[] labels = {"B","H","SD","Se","W"};

        //sb.append("/");
        sb.append(r.toString());
        //System.out.println(sb.toString());

        logger.info("Mastering parted output files...");

        try {

            for (String l : labels) {

                HashMap<String, PartedTweetTerm> oMap = new HashMap<>();

                for (int i = 0; i < parts; i++) {
                    String d = sb.toString();
                    if (i < 10) d = d + "0";
                    String csv =  d + i + "/" + out + "_" + l + ".csv";

                    logger.info(csv);

                    CSVReader reader = new CSVReader(new FileReader(csv));
                    List<String[]> entries = reader.readAll();
                    for (String[] entry : entries) {
                        if (entries.indexOf(entry) == 0 || entries.indexOf(entry) == 1 || entries.indexOf(entry) == entries.size()-1) {
                            continue;
                        }

                        PartedTweetTerm p = oMap.get(entry[1]);
                        if (p == null) {
                            PartedTweetTerm partedTweetTerm = new PartedTweetTerm(entry[1]);
                            partedTweetTerm.setCount(Integer.valueOf(entry[0]));
                            //partedTweetTerm.setUserCount(Integer.valueOf(entry[2]));
                            oMap.put(entry[1], partedTweetTerm);
                        } else {
                            p.setCount(p.getCount() + Integer.valueOf(entry[0]));
                            //p.setUserCount(p.getUserCount() + Integer.valueOf(entry[2]));
                        }
                    }
                }

                printOccurrenceMap(util.sortByValue(oMap), l);

                //System.out.println();
            }

        } catch (IOException | CsvException e) {
            e.printStackTrace();
        }
    }

    private void printOccurrenceMap(Map<String, PartedTweetTerm> oMap, String city) {
        CSVWriter writer = null;
        try {
            writer = new CSVWriter(new FileWriter(out.concat("_").concat(city).concat(".csv")));
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (writer == null) {
            System.err.println("writer is null...");
            return;
        }

        //System.out.println("Total size: " + oMap.size());
        writer.writeNext(new String[]{"Total terms size", oMap.size()+""});
        writer.writeNext(new String[]{"Frequency", "Term"});

        for (Map.Entry entry : oMap.entrySet()) {
            String key = (String) entry.getKey();
            PartedTweetTerm value = (PartedTweetTerm) entry.getValue();

            //writer.writeNext(new String[]{value.getCount()+"", key, value.getUserCount()+""});
            writer.writeNext(new String[]{value.getCount()+"", key});
        }

        try {
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static final Logger logger = LogManager.getLogger(MasterParted.class);
}
