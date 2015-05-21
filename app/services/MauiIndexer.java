package services;
/*
 *    MauiModelBuilder.java
 *    Copyright (C) 2009 Olena Medelyan
 *
 *    This program is free software; you can redistribute it and/or modify
 *    it under the terms of the GNU General Public License as published by
 *    the Free Software Foundation; either version 2 of the License, or
 *    (at your option) any later version.
 *
 *    This program is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *    GNU General Public License for more details.
 *
 *    You should have received a copy of the GNU General Public License
 *    along with this program; if not, write to the Free Software
 *    Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 */
import gnu.trove.TIntHashSet;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;

import maui.main.MauiModelBuilder;
import maui.main.MauiTopicExtractor;
//import maui.stemmers.FrenchStemmer;
import maui.stemmers.PorterStemmer;
import maui.stemmers.Stemmer;
import maui.stopwords.Stopwords;
import maui.stopwords.StopwordsEnglish;
import maui.stopwords.StopwordsFrench;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.wikipedia.miner.model.Wikipedia;
//import org.wikipedia.miner.util.ProgressNotifier;
import org.wikipedia.miner.util.WikipediaConfiguration;
import org.wikipedia.miner.util.text.CaseFolder;
import org.wikipedia.miner.util.text.TextProcessor;
import weka.core.Instance;

/**
 * Demonstrates how to use Maui for three types of topic indexing  <br>
 * 1. Keyphrase extraction - extracting significant phrases from
 *  the document, also suitable for automatic tagging. <br>
 * 2. Term assignment - indexing documents with terms
 * from a controlled vocabulary in SKOS or text format. <br>
 * 3. Indexing with Wikipedia - indexing documents with
 * terms from Wikipedia, also suitable for
 * keyphrase extraction and tagging, or any case where there is no con	trolled
 * vocabulary available, but consistency is required.
 *
 * @author Olena Medelyan (olena@cs.waikato.ac.nz)
 *
 */
public class MauiIndexer {

    private MauiTopicExtractor topicExtractor;
    private MauiModelBuilder modelBuilder;

    private Wikipedia wikipedia;

    private String wikiconfigPath;
    private String server;
    private String database;
    private String dataDirectory;
    private boolean cache = false;

    public MauiIndexer (String server, String database, String dataDirectory, boolean cache, String wikiconfigPath) throws Exception  {
        this.server = server;
        this.database = database;
        this.dataDirectory = dataDirectory;
        this.cache = cache;
        this.wikiconfigPath = wikiconfigPath;
        loadWikipedia();
    }

    public MauiIndexer ()  {	}

    private void loadWikipedia() throws Exception {
        WikipediaConfiguration conf = new WikipediaConfiguration(new File("/Users/hugh_sd/Projects/wikipedia-miner-1.2.0/configs/wikipedia-config.xml"));
        // TODO : not clear cache databases
        conf.clearDatabasesToCache();
        wikipedia = new Wikipedia(conf, false);

//        TextProcessor textProcessor = new CaseFolder();
//        File dataDir = new File(dataDirectory);
//        wikipedia.getDatabase().loadData(dataDir, this.cache);
//        wikipedia.getDatabase().prepareForTextProcessor(textProcessor);
//
//        if (cache) {
//            ProgressNotifier progress = new ProgressNotifier(5);
//            // cache tables that will be used extensively
//            TIntHashSet validPageIds = wikipedia.getDatabase().getValidPageIds(
//                    dataDir, 2, progress);
//            wikipedia.getDatabase().cachePages(dataDir, validPageIds,
//                    progress);
//            wikipedia.getDatabase().cacheAnchors(dataDir, textProcessor,
//                    validPageIds, 2, progress);
//            wikipedia.getDatabase().cacheInLinks(dataDir, validPageIds,
//                    progress);
//            wikipedia.getDatabase().cacheGenerality(dataDir, validPageIds, progress);
//        }
    }

    /**
     * Sets general parameters: debugging printout, language specific options
     * like stemmer, stopwords.
     * @throws Exception
     */
    private void setGeneralOptions()  {


        modelBuilder.debugMode = true;
        modelBuilder.wikipedia = wikipedia;

		/* language specific options
		Stemmer stemmer = new FrenchStemmer();
		Stopwords stopwords = new StopwordsFrench();
		String language = "fr";
		String encoding = "UTF-8";
		modelBuilder.stemmer = stemmer;
		modelBuilder.stopwords = stopwords;
		modelBuilder.documentLanguage = language;
		modelBuilder.documentEncoding = encoding;
		topicExtractor.stemmer = stemmer;
		topicExtractor.stopwords = stopwords;
		topicExtractor.documentLanguage = language;
		*/

		/* specificity options
		modelBuilder.minPhraseLength = 1;
		modelBuilder.maxPhraseLength = 5;
		*/

        topicExtractor.debugMode = true;
        topicExtractor.topicsPerDocument = 30;
        topicExtractor.wikipedia = wikipedia;
    }

    /**
     * Set which features to use
     */
    private void setFeatures() {
        modelBuilder.setBasicFeatures(true);
        modelBuilder.setKeyphrasenessFeature(true);
        modelBuilder.setFrequencyFeatures(true);
        modelBuilder.setPositionsFeatures(true);
        modelBuilder.setLengthFeature(true);
        modelBuilder.setNodeDegreeFeature(true);
        modelBuilder.setBasicWikipediaFeatures(true);
        modelBuilder.setAllWikipediaFeatures(false);
    }

    /**
     * Demonstrates how to perform automatic tagging. Also applicable to
     * keyphrase extraction.
     *
     * @throws Exception
     */
    public void testAutomaticTagging() throws Exception {
        topicExtractor = new MauiTopicExtractor();
        modelBuilder = new MauiModelBuilder();
        setGeneralOptions();
        setFeatures();

        // Directories with train & test data
        String trainDir = "data/automatic_tagging/train";
        String testDir = "data/automatic_tagging/test";

        // name of the file to save the model
        modelBuilder.modelPath = "test_maui_model";

        // Settings for the model builder
        modelBuilder.inputDirectoryName = trainDir;
        //modelBuilder.modelName = modelName;

        // change to 1 for short documents
        modelBuilder.minNumOccur = 2;

        // Run model builder
        HashSet<String> fileNames = modelBuilder.collectStems();
        modelBuilder.buildModel(fileNames);
        modelBuilder.saveModel();

        // Settings for topic extractor
        topicExtractor.inputDirectoryName = testDir;
        //topicExtractor.modelName = modelName;


        // Run topic extractor
        topicExtractor.loadModel();
        fileNames = topicExtractor.collectStems();
        topicExtractor.extractKeyphrases(fileNames);
    }

    /**
     * Demonstrates how to perform term assignment. Applicable to any vocabulary
     * in SKOS or text format.
     *
     * @throws Exception
     */
    public void testTermAssignment() throws Exception {
        topicExtractor = new MauiTopicExtractor();
        modelBuilder = new MauiModelBuilder();
        setGeneralOptions();
        setFeatures();

        // Directories with train & test data
        String trainDir = "data/term_assignment/train";
        String testDir = "data/term_assignment/test";

        // Vocabulary
        String vocabulary = "agrovoc_sample";
        String format = "skos";

        // name of the file to save the model
        String modelName = "test_maui_model";
        HashSet<String> fileNames;

        // Settings for the model builder
        modelBuilder.inputDirectoryName = trainDir;
        //modelBuilder.modelName = modelName;
        modelBuilder.vocabularyFormat = format;
        modelBuilder.vocabularyName = vocabulary;

        // Run model builder
        fileNames = modelBuilder.collectStems();
        modelBuilder.buildModel(fileNames);
        modelBuilder.saveModel();

        // Settings for topic extractor
        topicExtractor.inputDirectoryName = testDir;
        //topicExtractor.modelName = modelName;
        topicExtractor.vocabularyName = vocabulary;
        topicExtractor.vocabularyFormat = format;

        // Run topic extractor
        topicExtractor.loadModel();
        fileNames = topicExtractor.collectStems();
        topicExtractor.extractKeyphrases(fileNames);

    }

    /**
     * Demonstrates how to perform topic indexing
     * with Wikipedia.
     *
     * @throws Exception
     */
    public void testIndexingWithWikipedia() throws Exception {
        topicExtractor = new MauiTopicExtractor();
        modelBuilder = new MauiModelBuilder();
        topicExtractor.stopwords = new StopwordsEnglish("/Users/hugh_sd/Projects/news-analyzer/data/stopwords/stopwords_en.txt");
        modelBuilder.stopwords = new StopwordsEnglish("/Users/hugh_sd/Projects/news-analyzer/data/stopwords/stopwords_en.txt");
        setGeneralOptions();
        setFeatures();

        // Directories with train & test data
        String trainDir = "/Users/hugh_sd/Projects/news-analyzer/data/wikipedia_indexing/train";
        String testDir = "/Users/hugh_sd/Projects/news-analyzer/data/wikipedia_indexing/test/";

        // Vocabulary
        String vocabulary = "wikipedia";

        // name of the file to save the model
        modelBuilder.modelPath = "test_maui_model";
        HashSet<String> fileNames;

        // Settings for the model builder
        modelBuilder.inputDirectoryName = trainDir;
        //modelBuilder.modelName = modelName;
        modelBuilder.vocabularyName = vocabulary;

        // Run model builder
        fileNames = modelBuilder.collectStems();
        modelBuilder.buildModel(fileNames);
        modelBuilder.saveModel();

		// Settings for topic extractor
        topicExtractor.modelPath = "test_maui_model";
		topicExtractor.inputDirectoryName = testDir;
		topicExtractor.vocabularyName = vocabulary;

		// Run topic extractor
		topicExtractor.loadModel();

        fileNames = topicExtractor.collectStems();
        topicExtractor.extractKeyphrases(fileNames);

//        int numTopics = 10;
//        Instance[] topRankedInstances = new Instance[numTopics];
//        Instance inst;
//
//        // Iterating over all extracted keyphrases (inst)
//        while ((inst = topicExtractor.getMauiFilter().output()) != null) {
//
//            int index = (int) inst.value(topicExtractor.getMauiFilter().getRankIndex()) - 1;
//
//            if (index < numTopics) {
//                topRankedInstances[index] = inst;
//            }
//        }
//
//        ArrayList<String> topics = new ArrayList<String>();
//
//        for (int i = 0; i < numTopics; i++) {
//            if (topRankedInstances[i] != null) {
//                String topic = topRankedInstances[i].stringValue(topicExtractor.getMauiFilter()
//                        .getOutputFormIndex());
//
//                topics.add(topic);
//            }
//        }
//
//        System.err.println("[DEBUG] topics:");
//        System.err.println(StringUtils.join(topics, ", "));
    }



    /**
     * Main method for running the three types of topic indexing. Comment out
     * the required one.
     *
     * @param args
     * @throws Exception
     */
    public static void run(String[] args) throws Exception {

        String mode = args[0];

        if (!mode.equals("tagging") && !mode.equals("term_assignment") && !mode.equals("indexing_with_wikipedia")) {
            throw new Exception("Choose one of the three modes: tagging, term_assignment or indexing_with_wikipedia");
        }

        Date todaysDate = new java.util.Date();
        SimpleDateFormat formatter = new SimpleDateFormat(
                "EEE, dd-MMM-yyyy HH:mm:ss");
        String formattedDate1 = formatter.format(todaysDate);
        MauiIndexer indexer;

        if (mode.equals("tagging")) {
            indexer = new MauiIndexer();
            indexer.testAutomaticTagging();
        } else if (mode.equals("term_assignment")) {
            indexer = new MauiIndexer();
            indexer.testTermAssignment();
        } else if (mode.equals("indexing_with_wikipedia")) {
            // Access to Wikipedia
            String server = "localhost";
            String database = "enwiki";
            String dataDirectory = "/Users/hugh_sd/Projects/news-analyzer/enwiki-csv";
            String wikiPath = "";
            boolean cache = false;
            indexer = new MauiIndexer(server, database, dataDirectory, cache, wikiPath);
            indexer.testIndexingWithWikipedia();
        }

        todaysDate = new java.util.Date();
        String formattedDate2 = formatter.format(todaysDate);
        System.err.print("Run from " + formattedDate1);
        System.err.println(" to " + formattedDate2);
    }

}

