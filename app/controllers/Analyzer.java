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


        WebURL crawlUrl = new WebURL();
        crawlUrl.setURL("https://www.google.com");
        crawler.visit(new Page(crawlUrl));
        logger.debug("crawler initiated!");

        return Results.ok(result);
    }
}
