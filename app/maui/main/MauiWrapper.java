package maui.main;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.util.*;

import org.apache.commons.io.FileUtils;

import maui.filters.MauiFilter;
import maui.stemmers.PorterStemmer;
import maui.stemmers.Stemmer;
import maui.stopwords.Stopwords;
import maui.stopwords.StopwordsEnglish;
import maui.vocab.Vocabulary;

import org.apache.commons.lang3.StringUtils;
import org.apache.pdfbox.pdfviewer.MapEntry;
import org.wikipedia.miner.model.Wikipedia;
import org.wikipedia.miner.util.WikipediaConfiguration;
import weka.core.Attribute;
import weka.core.FastVector;
import weka.core.Instance;
import weka.core.Instances;


/**
 * This class shows how to use Maui on a single document
 * or just a string of text.
 * @author alyona
 *
 */
public class MauiWrapper {

	/** Maui filter object */
	private MauiFilter extractionModel = null;
	
	private Vocabulary vocabulary = null;
	private Stemmer stemmer;
	private Stopwords stopwords;
	private String language = "en";
	
	/**
	 * Constructor, which loads the data
	 * @param dataDirectory - e.g. Maui's main directory (should has "data" dir in it)
	 * @param vocabularyName - name of the rdf vocabulary
	 * @param modelName - name of the model
	 */
	public MauiWrapper(String dataDirectory, String vocabularyName, String modelName) {
	
		stemmer = new PorterStemmer();
		String englishStopwords = dataDirectory + "data/stopwords/stopwords_en.txt";
		stopwords = new StopwordsEnglish(englishStopwords);
		String vocabularyDirectory = dataDirectory +  "data/vocabularies/";
		String modelDirectory = dataDirectory +  "data/models";
		loadVocabulary(vocabularyDirectory, vocabularyName);
		loadModel(modelDirectory, modelName, vocabularyName);
	}

	/**
	 * Loads a vocabulary from a given directory
	 * @param vocabularyDirectory
	 * @param vocabularyName
	 */
	public void loadVocabulary(String vocabularyDirectory, String vocabularyName) {
		if (vocabulary != null || vocabularyName.equals("none") || vocabularyName.equals("wikipedia"))
			return;
		try {
			vocabulary = new Vocabulary(vocabularyName, "skos", vocabularyDirectory);
			vocabulary.setStemmer(stemmer);
			vocabulary.setStopwords(stopwords);
			vocabulary.setLanguage(language);
			vocabulary.initialize();
		} catch (Exception e) {
			System.err.println("Failed to load vocabulary!");
			e.printStackTrace();
		}
	}
	
	/**
	 * Loads the model
	 * @param modelDirectory
	 * @param modelName
	 * @param vocabularyName
	 */
	public void loadModel(String modelDirectory, String modelName, String vocabularyName) {

		try {
			BufferedInputStream inStream = new BufferedInputStream(
					new FileInputStream(modelDirectory + "/" + modelName));
			ObjectInputStream in = new ObjectInputStream(inStream);
			extractionModel = (MauiFilter) in.readObject();
			in.close();
		} catch (Exception e) {
			System.err.println("Failed to load model!");
			e.printStackTrace();
		}

		extractionModel.setVocabularyName(vocabularyName);
		extractionModel.setVocabularyFormat("skos");
		extractionModel.setDocumentLanguage(language);
		extractionModel.setStemmer(stemmer);
		extractionModel.setStopwords(stopwords);

		extractionModel.setVocabulary(vocabulary);
	}

	private HashMap<String, Double> semRelatedRecord = new HashMap<>();
	/**
	 * Main method to extract the main topics from a given text
	 * @param text
	 * @param topicsPerDocument
	 * @return
	 * @throws Exception
	 */
	public ArrayList<String> extractTopicsFromText(String text, int topicsPerDocument) throws Exception {

		if (text.length() < 5) {
			throw new Exception("Text is too short!");
		}

		semRelatedRecord.clear();

		WikipediaConfiguration conf = new WikipediaConfiguration(new File("/Users/hugh_sd/Projects/wikipedia-miner-1.2.0/configs/wikipedia-config.xml"));
		// TODO : not clear cache databases
		conf.clearDatabasesToCache();
		Wikipedia wikipedia = new Wikipedia(conf, false);
		//extractionModel.setWikipedia("");
		extractionModel.setWikipedia(wikipedia);
		extractionModel.setBasicWikipediaFeatures(true);
		extractionModel.setAllWikipediaFeatures(true);
		extractionModel.setMinNumOccur(1);

		FastVector atts = new FastVector(3);
		atts.addElement(new Attribute("filename", (FastVector) null));
		atts.addElement(new Attribute("doc", (FastVector) null));
		atts.addElement(new Attribute("keyphrases", (FastVector) null));
		Instances data = new Instances("keyphrase_training_data", atts, 0);

		double[] newInst = new double[3];

		newInst[0] = (double) data.attribute(0).addStringValue("inputFile");
		newInst[1] = (double) data.attribute(1).addStringValue(text);
		newInst[2] = Instance.missingValue();
		data.add(new Instance(1.0, newInst));

		extractionModel.input(data.instance(0));
		extractionModel.batchFinished();

		data = data.stringFreeStructure();
		Instance[] topRankedInstances = new Instance[topicsPerDocument];
		Instance inst;

		// Iterating over all extracted keyphrases (inst)
		while ((inst = extractionModel.output()) != null) {

			int index = (int) inst.value(extractionModel.getRankIndex()) - 1;

			if (index < topicsPerDocument) {
				topRankedInstances[index] = inst;
			}
		}

		double relatedness;
		double totalRelatedness = 0.0;
		double avgRelatedness = 0.0;
		double[] arrRelatedness = new double[topicsPerDocument];
		int numTopics = 0;
		for (int j = 0; j < topicsPerDocument; j++) {
			if (topRankedInstances[j] != null) {
				relatedness = topRankedInstances[j].value(12); // semantic relatedness
				totalRelatedness += relatedness;
				arrRelatedness[j] = relatedness;
				numTopics++;
			}
		}
		if (weka.core.Utils.grOrEq(totalRelatedness, 0)) {
			System.err.println("[DEBUG] numTopics: " + numTopics);
			avgRelatedness = totalRelatedness / numTopics;
		}

		double[] sortedRelatednesses = Arrays.copyOfRange(arrRelatedness, 0, numTopics);
		Arrays.sort(sortedRelatednesses);
		double medianRelatedness;
		if (sortedRelatednesses.length % 2 == 0)
			medianRelatedness = ((double)sortedRelatednesses[sortedRelatednesses.length/2] + (double)sortedRelatednesses[sortedRelatednesses.length/2 - 1])/2;
		else
			medianRelatedness = (double) sortedRelatednesses[sortedRelatednesses.length/2];
		double threshold = Math.min(avgRelatedness, medianRelatedness);


		ArrayList<String> topics = new ArrayList<>();

		for (int i = 0; i < topicsPerDocument; i++) {
			if (topRankedInstances[i] != null) {
				String topic = topRankedInstances[i].stringValue(extractionModel
						.getOutputFormIndex());

				relatedness = topRankedInstances[i].value(12); // semantic relatedness
				if (weka.core.Utils.grOrEq(threshold, relatedness)) {
					continue; // skip topic if relatedness less than max(avgRelatedness, medianRelatedness)
				}
				semRelatedRecord.put(topRankedInstances[i].stringValue(0), relatedness);
//				topics.add(String.format("%-40s %5.3f",
//						topic,
//						relatedness
//				));

				/**
				 * Indices of attributes in classifierData
				 // General features
				 // 2 term frequency
				 // 3 inverse document frequency
				 // 4 TFxIDF
				 // 5 position of the first occurrence
				 // 6 position of the last occurrence
				 // 7 spread of occurrences
				 // 8 domain keyphraseness
				 // 9 term length
				 // 10 generality
				 // 11 node degree
				 // 12 semantic relatedness
				 // 13 wikipedia keyphraseness
				 // 14 inverse wikipedia frequency
				 // 15 total wikipedia keyphraseness
				 // 16 probability
				 // 17 rank
				 */
				 StringBuffer features = new StringBuffer();
				 for (int j = 2; j <= 17; j++) {
				 double value = topRankedInstances[i].value(j);
				 if (value < 1) {
				 value = Math.round(value * 100.0) / 100.0;
				 } else {
				 value = Math.round(value);
				 }
				 features.append(String.format("  %5.2f", value));
				 }
				 //topics.add(String.format("%-50s %s", topic + " #" + topRankedInstances[i].stringValue(0) + " >> ", features));
				topics.add(topic);


//				String title = topRankedInstances[i].stringValue(extractionModel.getOutputFormIndex());
//				double probability = topRankedInstances[i].value(extractionModel.getProbabilityIndex());
//				topics.add(new Topic(title, id, probability));
			}
		}

		topics.addAll(extractTopicsFromTextHighFreq(text, topicsPerDocument, threshold));

		// Recal threshold
//		avgRelatedness = 0.0;
//		double[] newArrRelatedness = new double[semRelatedRecord.size()];
//		int newCounter = 0;
//		for (Map.Entry<String, Double> entry : semRelatedRecord.entrySet()) {
//			avgRelatedness += entry.getValue();
//			newArrRelatedness[newCounter++] = entry.getValue();
//		}
//		avgRelatedness = avgRelatedness / semRelatedRecord.size();
//
//		Arrays.sort(newArrRelatedness);
//		medianRelatedness = 0.0;
//		if (newArrRelatedness.length % 2 == 0)
//			medianRelatedness = ((double)newArrRelatedness[newArrRelatedness.length/2] + (double)newArrRelatedness[newArrRelatedness.length/2 - 1])/2;
//		else
//			medianRelatedness = (double) newArrRelatedness[newArrRelatedness.length/2];
//		threshold = Math.min(avgRelatedness, medianRelatedness);
//
//		for (Map.Entry<String, Double> entry : semRelatedRecord.entrySet()) {
//			if (weka.core.Utils.grOrEq(threshold, entry.getValue())) {
//				topics.remove(entry.getKey());
//			}
//		}

		String attributesDesc = String.format("  %-5s  %-5s  %-5s  %-5s  %-5s  %-5s  %-5s  %-5s  %-5s  %-5s  %-5s  %-5s  %-5s  %-5s  %-5s  %-5s",
				"TF",
				"InvTF",
				"TFIDF",
				"1st",
				"last",
				"spred",
				"doKey",
				"length",
				"gen",
				"nDegr",
				"semR",
				"wiKey",
				"invWK",
				"toWK",
				"prob",
				"rank");
		System.err.println(String.format("[DEBUG] avgRelatedness    = %5.2f", avgRelatedness));
		System.err.println(String.format("[DEBUG] medianRelatedness = %5.2f", medianRelatedness));
		System.err.println(String.format("[DEBUG] >> threshold         = %5.2f", threshold));
		System.err.println("[DEBUG] Topics:");
		System.err.println(String.format("\t%-50s %s", "", attributesDesc));
//		for (Map.Entry<String, String> entry : topics.entrySet()) {
//			System.err.println("\t" + entry.getValue());
//		}
		System.err.println("\t" + StringUtils.join(topics, "\n\t"));
//		extractionModel.batchFinished();

		extractionModel.getWikipedia().close();
		return topics;
	}


	/**
	 * Main method to extract the main topics from a given text
	 * @param text
	 * @param topicsPerDocument
	 * @return
	 * @throws Exception
	 */
	public ArrayList<String> extractTopicsFromTextHighFreq(String text, int topicsPerDocument, double relatednessThreshold) throws Exception {

		if (text.length() < 5) {
			throw new Exception("Text is too short!");
		}
		extractionModel.setMinNumOccur(2);

		FastVector atts = new FastVector(3);
		atts.addElement(new Attribute("filename", (FastVector) null));
		atts.addElement(new Attribute("doc", (FastVector) null));
		atts.addElement(new Attribute("keyphrases", (FastVector) null));
		Instances data = new Instances("keyphrase_training_data", atts, 0);

		double[] newInst = new double[3];

		newInst[0] = (double) data.attribute(0).addStringValue("inputFile");
		newInst[1] = (double) data.attribute(1).addStringValue(text);
		newInst[2] = Instance.missingValue();
		data.add(new Instance(1.0, newInst));

		extractionModel.input(data.instance(0));
		extractionModel.batchFinished();

		data = data.stringFreeStructure();
		Instance[] topRankedInstancesHigherFreq = new Instance[topicsPerDocument];
		Instance instHigherFreq;

		// Iterating over all extracted keyphrases (inst)
		while ((instHigherFreq = extractionModel.output()) != null) {

			int index = (int) instHigherFreq.value(extractionModel.getRankIndex()) - 1;

			if (index < topicsPerDocument) {
				topRankedInstancesHigherFreq[index] = instHigherFreq;
			}
		}

		double prob;
		double totalProb = 0.0;
		double avgProb = 0.0;
		double medianProb;
		double[] arrProbs = new double[topicsPerDocument];
		int numTopics = 0;
		for (int j = 0; j < topicsPerDocument; j++) {
			if (topRankedInstancesHigherFreq[j] != null) {
				prob = topRankedInstancesHigherFreq[j].value(16); // probability
				totalProb += prob;
				arrProbs[j] = prob;
				numTopics++;
			}
		}
		if (weka.core.Utils.grOrEq(totalProb, 0)) {
			System.err.println("[DEBUG] numTopics (highFreq): " + numTopics);
			avgProb = totalProb / numTopics;
		}
		double[] sortedProbs = Arrays.copyOfRange(arrProbs, 0, numTopics);
		Arrays.sort(sortedProbs);
		if (sortedProbs.length % 2 == 0)
			medianProb = ((double)sortedProbs[sortedProbs.length/2] + (double)sortedProbs[sortedProbs.length/2 - 1])/2;
		else
			medianProb = (double) sortedProbs[sortedProbs.length/2];
		double thresholdHighFreq = Math.min(avgProb, medianProb);

		System.err.println(String.format("[DEBUG] avgProb           = %5.2f", avgProb));
		System.err.println(String.format("[DEBUG] medianProb        = %5.2f", medianProb));
		System.err.println(String.format("[DEBUG] >> thresholdProb     = %5.2f", thresholdHighFreq));

		double relatedness;
		ArrayList<String> topics = new ArrayList<>();
		for (int i = 0; i < topicsPerDocument; i++) {
			if (topRankedInstancesHigherFreq[i] != null) {
				String topic = topRankedInstancesHigherFreq[i].stringValue(extractionModel
						.getOutputFormIndex());

				// skip topic if prob < median prob
				relatedness = topRankedInstancesHigherFreq[i].value(12);
				prob = topRankedInstancesHigherFreq[i].value(16); // probability
				if (weka.core.Utils.grOrEq(relatednessThreshold, relatedness) ||
						weka.core.Utils.grOrEq(thresholdHighFreq, prob) ||
						semRelatedRecord.containsKey(topRankedInstancesHigherFreq[i].stringValue(0))) {
					continue; // skip if too low probability or topics already added
				}
				semRelatedRecord.put(topRankedInstancesHigherFreq[i].stringValue(0), relatedness);


				StringBuffer features = new StringBuffer();
				for (int j = 2; j <= 17; j++) {
					double value = topRankedInstancesHigherFreq[i].value(j);
					if (value < 1) {
						value = Math.round(value * 100.0) / 100.0;
					} else {
						value = Math.round(value);
					}
					features.append(String.format("  %5.2f", value));
				}
				String addedTopic = String.format("%-50s %s", topic + " #" + topRankedInstancesHigherFreq[i].stringValue(0) + " >> ", features);
				//topics.add(addedTopic);
				topics.add(topic);
				//System.err.println("[DEBUG] adding: " + addedTopic);
			}
		}
		return topics;
	}

	/**
	 * Triggers topic extraction from a text file
	 * @param filePath
	 * @param numberOfTopics
	 * @return
	 * @throws Exception
	 */
	public ArrayList<String> extractTopicsFromFile(String filePath, int numberOfTopics) throws Exception {
		File documentTextFile = new File(filePath);
		String documentText = FileUtils.readFileToString(documentTextFile);
		return extractTopicsFromText(documentText, numberOfTopics);
	}
	
	/**
	 * Main method for testing MauiWrapper
	 * Add the path to a text file as command line argument
	 * @param args
	 */
	public static void main(String[] args) {
		
		String vocabularyName = "agrovoc_en";
		String modelName = "fao30";
		String dataDirectory = "../Maui1.2/";
		
		MauiWrapper wrapper = new MauiWrapper(dataDirectory, vocabularyName, modelName);
		
		String filePath = args[0];
		
		try {
			
//			ArrayList<String> keywords = wrapper.extractTopicsFromFile(filePath, 15);
//			for (String keyword : keywords) {
//				System.out.println("Keyword: " + keyword);
//			}
//
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}

}
