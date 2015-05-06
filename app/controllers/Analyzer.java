package controllers;

import com.aliasi.classify.JointClassification;
import com.fasterxml.jackson.databind.node.ObjectNode;
import de.l3s.boilerpipe.BoilerpipeExtractor;
import de.l3s.boilerpipe.BoilerpipeProcessingException;
import de.l3s.boilerpipe.document.TextDocument;
import de.l3s.boilerpipe.extractors.ArticleExtractor;
import de.l3s.boilerpipe.extractors.CommonExtractors;
import de.l3s.boilerpipe.sax.BoilerpipeSAXInput;
import de.l3s.boilerpipe.sax.HTMLDocument;
import de.l3s.boilerpipe.sax.HTMLFetcher;
import edu.uci.ics.crawler4j.crawler.Page;
import edu.uci.ics.crawler4j.url.WebURL;
import maui.main.MauiModelBuilder;
import maui.main.MauiTopicExtractor;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.Results;
import services.ClassifyNews;
import services.CrossValidateNews;
import services.MauiIndexer;
import services.SimpleWebCrawler;
import org.neo4j.graphdb.GraphDatabaseService;

import java.io.IOException;
import java.net.URL;

/**
 * Created by hugo on 4/14/15.
 */
public class Analyzer extends Controller {
    public static play.Logger.ALogger logger = play.Logger.of("application.controllers.Analyzer");
    private static GraphDatabaseService graphDb = null;
    private static SimpleWebCrawler crawler = new SimpleWebCrawler();

    public static Result getMetadata(String url) {
        ObjectNode result = Json.newObject();

        result.put("url", url);

//        WebURL crawlUrl = new WebURL();
//        crawlUrl.setURL(url);

//        String[] args = {"./crawler_data", "2", url};

//        try {
//            BasicCrawlController.run(args);
//        } catch (Exception e) {
//            e.printStackTrace();
//            logger.error("crawler error!");
//        }

//        try {
//            String content = ArticleExtractor.INSTANCE.getText(new URL(url));
//
////            final HTMLDocument htmlDoc = HTMLFetcher.fetch(new URL(url));
////            final TextDocument doc = new BoilerpipeSAXInput(htmlDoc.toInputSource()).getTextDocument();
////            String title = doc.getTitle();
////            logger.debug("URL title extracted: " + title);
////            String content = ArticleExtractor.INSTANCE.getText(doc);
//
////            final BoilerpipeExtractor extractor = CommonExtractors.KEEP_EVERYTHING_EXTRACTOR;
////            final ImageExtractor ie = ImageExtractor.INSTANCE;
////
////            List<Image> images = ie.process(new URL(url), extractor);
////
////            Collections.sort(images);
////            String image = null;
////            if (!images.isEmpty()) {
////                image = images.get(0).getSrc();
////            }
////
////            return new Content(title, content.substring(0, 200), image);
//
//            JointClassification jc = ClassifyNews.classify(url, content);
//            result.put("category", jc.bestCategory());
//        } catch (Exception e) {
//            e.printStackTrace();
//            result.put("category", "unknown");
//        }

        String[] ops = {
                "tagging"
        };
        try {
            MauiIndexer.run(ops);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return Results.ok(result);
    }
}
