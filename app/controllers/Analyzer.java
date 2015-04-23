package controllers;

import com.fasterxml.jackson.databind.node.ObjectNode;
import edu.uci.ics.crawler4j.crawler.Page;
import edu.uci.ics.crawler4j.url.WebURL;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.Results;
import services.SimpleWebCrawler;
import org.neo4j.graphdb.GraphDatabaseService;

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

        String[] args = new String[3];
        args[0] = "./crawler_data";
        args[1] = "2";
        args[2] = url;

//        try {
//            BasicCrawlController.run(args);
//        } catch (Exception e) {
//            e.printStackTrace();
//            logger.error("crawler error!");
//        }

        return Results.ok(result);
    }
}
