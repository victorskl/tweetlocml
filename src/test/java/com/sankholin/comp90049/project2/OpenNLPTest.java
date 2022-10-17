package com.sankholin.comp90049.project2;

import opennlp.tools.cmdline.PerformanceMonitor;
import opennlp.tools.cmdline.postag.POSModelLoader;
import opennlp.tools.ngram.NGramModel;
import opennlp.tools.postag.POSModel;
import opennlp.tools.postag.POSSample;
import opennlp.tools.postag.POSTaggerME;
import opennlp.tools.sentdetect.SentenceDetectorME;
import opennlp.tools.sentdetect.SentenceModel;
import opennlp.tools.tokenize.WhitespaceTokenizer;
import opennlp.tools.util.MarkableFileInputStreamFactory;
import opennlp.tools.util.ObjectStream;
import opennlp.tools.util.PlainTextByLineStream;
import opennlp.tools.util.StringList;
import org.junit.Ignore;
import org.junit.Test;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

public class OpenNLPTest {

    private String modelBase = "D:\\Projects\\unimelb\\knowledge\\project2\\data\\opennlp\\models-1.5\\";

    private String[] nounTags = {"NN", "NNS", "NNP", "NNPS"};

    @Test @Ignore
    public void sentenceDetect() throws IOException {
        String paragraph = "Hi. How are you? This is Mike.";

        // always start with a model, a model is learned from training data
        InputStream is = new FileInputStream(modelBase + "en-sent.bin");
        SentenceModel model = new SentenceModel(is);
        SentenceDetectorME sdetector = new SentenceDetectorME(model);

        String sentences[] = sdetector.sentDetect(paragraph);

        System.out.println(sentences[0]);
        System.out.println(sentences[1]);
        is.close();
    }

    @Test @Ignore
    public void postag() throws IOException {
        //We do not use adjectives, verbs, prepositions, etc. because they are often
        // generic and may not discriminate among locations.

        POSModel model = new POSModelLoader().load(new File(modelBase + "en-pos-maxent.bin"));

        PerformanceMonitor perfMon = new PerformanceMonitor(System.err, "sent");

        POSTaggerME tagger = new POSTaggerME(model);

        String input = "Boston Tea Party Pics Up I removed the password protection";
        input = "02N28E16C___01 Well";
        input = "Hey Ryan! How are you? How is Portland? Just checking out Twitter. Hope JD went well- say hello to PAF folks!";
        input = "Hey Ryan How are you How is Portland Just checking out Twitter Hope JD went well- say hello to PAF folks";
        input = "i'm back to work";
        //input = "hey, ryan, how, are, you, how, is, portland, just, checking, out, twitter, hope, jd, went, well, say, hello, to, paf, folks";
        ObjectStream<String> lineStream = new PlainTextByLineStream(
                new MarkableFileInputStreamFactory(new File(input)), StandardCharsets.UTF_8
        );

        perfMon.start();

        String line;
        while ((line = lineStream.read()) != null) {

            String whitespaceTokenizerLine[] = WhitespaceTokenizer.INSTANCE.tokenize(line);

            String[] tags = tagger.tag(whitespaceTokenizerLine);

            POSSample sample = new POSSample(whitespaceTokenizerLine, tags);

            System.out.println(sample.toString());

            for (String t : tags) {
                System.out.println(t);
            }

            perfMon.incrementCounter();
        }

        perfMon.stopAndPrintFinalResult();
    }

    @Test @Ignore
    public void posNounFilter() {
        InputStream modelIn = null;
        POSModel model = null;
        try {
            modelIn = new FileInputStream(modelBase + "en-pos-maxent.bin");
            model = new POSModel(modelIn);
        }
        catch (IOException e) {
            // Model loading failed, handle the error
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

        if (model == null) return;

        POSTaggerME tagger = new POSTaggerME(model);

        String str = "hey, ryan, how, are, you, how, is, portland, just, checking, out, twitter, hope, jd, went, well, say, hello, to, paf, folks";

        String sent[] = new String[]{"Most", "large", "cities", "in", "the", "US", "had",
                "morning", "and", "afternoon", "newspapers", "."};

        sent = new String[] {"02N28E16C___01", "Well"};
        sent = str.split(",");

        String tags[] = tagger.tag(sent);

        System.out.println(Arrays.toString(tags));

        POSSample sample = new POSSample(sent, tags);

        System.out.println(sample.toString());

        //System.out.println(Arrays.toString(sample.getSentence()));

        int idx = 0;
        for (String s : sent) {
/*
            if (Arrays.stream(nounTags).parallel().anyMatch(tags[idx]::contains)) {
                System.out.println(s);
            }
*/
            if (Arrays.stream(nounTags).anyMatch(tags[idx]::contains)) {
                System.out.println(s);
            }
            idx++;
        }
    }

    @Ignore @Test
    public void createNgram() {

        List<String> list = new ArrayList<>();
        list.add("this is a car");

        int cutoff = 2;

        try {
            NGramModel ngramModel = new NGramModel();
            POSModel model = new POSModelLoader().load(new File(modelBase + "en-pos-maxent.bin"));
            PerformanceMonitor perfMon = new PerformanceMonitor(System.err, "sent");
            POSTaggerME tagger = new POSTaggerME(model);
            perfMon.start();
            for (int i = 0; i < list.size(); i++) {
                String inputString = list.get(i);
                // ObjectStream<String> lineStream = new PlainTextByLineStream(new StringReader(inputString));
                ObjectStream<String> lineStream = new PlainTextByLineStream(
                        new MarkableFileInputStreamFactory(new File(inputString)), StandardCharsets.UTF_8
                );
                String line;
                while ((line = lineStream.read()) != null) {
                    String whitespaceTokenizerLine[] = WhitespaceTokenizer.INSTANCE.tokenize(line);
                    String[] tags = tagger.tag(whitespaceTokenizerLine);

                    POSSample sample = new POSSample(whitespaceTokenizerLine, tags);

                    perfMon.incrementCounter();

                    String words[] = sample.getSentence();

                    if (words.length > 0) {
                        for (int k = 2; k < 4; k++) {
                            ngramModel.add(new StringList(words), k, k);
                        }
                    }
                }
            }
            ngramModel.cutoff(cutoff, Integer.MAX_VALUE);
            Iterator<StringList> it = ngramModel.iterator();
            while (it.hasNext()) {
                StringList strList = it.next();
                System.out.println(strList.toString());
            }
            perfMon.stopAndPrintFinalResult();
        } catch (Exception e) {
            System.out.println(e.toString());
        }
    }
}
