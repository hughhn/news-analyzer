package services;

import com.aliasi.classify.Classification;
import com.aliasi.classify.Classified;
import com.aliasi.classify.ConfusionMatrix;
import com.aliasi.classify.DynamicLMClassifier;
import com.aliasi.classify.JointClassification;
import com.aliasi.classify.JointClassifier;
import com.aliasi.classify.JointClassifierEvaluator;
import com.aliasi.classify.LMClassifier;

import com.aliasi.lm.NGramProcessLM;

import com.aliasi.util.AbstractExternalizable;

import java.io.File;
import java.io.IOException;

import com.aliasi.util.Files;

public class ClassifyNews {

    private static File TRAINING_DIR
        = new File("app/services/lingpipe_data/fourNewsGroups/20news-bydate-train");

    private static File TESTING_DIR
        =  new File("app/services/lingpipe_data/fourNewsGroups/20news-bydate-test");

    private static String[] CATEGORIES
        = { "alt.atheism",
            "comp.graphics",
            "comp.os.ms-windows.misc",
            "comp.sys.ibm.pc.hardware",
            "comp.sys.mac.hardware",
            "comp.windows.x",
            "misc.forsale",
            "rec.autos",
            "rec.motorcycles",
            "rec.sport.baseball",
            "rec.sport.hockey",
            "sci.crypt",
            "sci.electronics",
            "sci.med",
            "sci.space",
            "soc.religion.christian",
            "talk.politics.guns",
            "talk.politics.mideast",
            "talk.politics.misc",
            "talk.religion.misc" };

    private static int NGRAM_SIZE = 6;

    @SuppressWarnings("unchecked") // we created object so know it's safe
    private static JointClassifier<CharSequence> compiledClassifier;

    public static void train(String[] args)
        throws ClassNotFoundException, IOException {

        DynamicLMClassifier<NGramProcessLM> classifier
            = DynamicLMClassifier.createNGramProcess(CATEGORIES,NGRAM_SIZE);

        for(int i=0; i<CATEGORIES.length; ++i) {
            File classDir = new File(TRAINING_DIR,CATEGORIES[i]);
            if (!classDir.isDirectory()) {
                String msg = "Could not find training directory="
                    + classDir
                    + "\nHave you unpacked 4 newsgroups?";
                System.out.println(msg); // in case exception gets lost in shell
                throw new IllegalArgumentException(msg);
            }

            String[] trainingFiles = classDir.list();
            for (int j = 0; j < trainingFiles.length; ++j) {
                File file = new File(classDir,trainingFiles[j]);
                String text = Files.readFromFile(file,"ISO-8859-1");
                System.out.println("Training on " + CATEGORIES[i] + "/" + trainingFiles[j]);
                Classification classification
                    = new Classification(CATEGORIES[i]);
                Classified<CharSequence> classified
                    = new Classified<CharSequence>(text,classification);
                classifier.handle(classified);
            }
        }
        //compiling
        System.out.println("Compiling");

        compiledClassifier
            = (JointClassifier<CharSequence>)
            AbstractExternalizable.compile(classifier);

        boolean storeCategories = true;
        JointClassifierEvaluator<CharSequence> evaluator
            = new JointClassifierEvaluator<CharSequence>(compiledClassifier,
                                                         CATEGORIES,
                                                         storeCategories);
        for(int i = 0; i < CATEGORIES.length; ++i) {
            File classDir = new File(TESTING_DIR,CATEGORIES[i]);
            String[] testingFiles = classDir.list();
            for (int j=0; j<testingFiles.length;  ++j) {
                String text
                    = Files
                    .readFromFile(new File(classDir,testingFiles[j]),"ISO-8859-1");
                System.out.print("Testing on " + CATEGORIES[i] + "/" + testingFiles[j] + " ");
                Classification classification
                    = new Classification(CATEGORIES[i]);
                Classified<CharSequence> classified
                    = new Classified<CharSequence>(text,classification);
                evaluator.handle(classified);
                JointClassification jc =
                    compiledClassifier.classify(text);
                String bestCategory = jc.bestCategory();
                String details = jc.toString();
                System.out.println("Got best category of: " + bestCategory);
                System.out.println(jc.toString());
                System.out.println("---------------");
            }
        }
        ConfusionMatrix confMatrix = evaluator.confusionMatrix();
        System.out.println("Total Accuracy: " + confMatrix.totalAccuracy());

        System.out.println("\nFULL EVAL");
        System.out.println(evaluator);
    }

    public static JointClassification classify(String url, String text) {
        if (compiledClassifier == null) {
            try {
                train(new String[0]);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        System.out.println("Testing on: " + url);
        JointClassification jc =
                compiledClassifier.classify(text);
        String bestCategory = jc.bestCategory();
        System.out.println("Got best category of: " + bestCategory);
        System.out.println(jc.toString());
        System.out.println("---------------");

        return jc;
    }
}
