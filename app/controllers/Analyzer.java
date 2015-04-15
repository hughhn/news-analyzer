package controllers;

import com.fasterxml.jackson.databind.node.ObjectNode;
import play.libs.Json;
import play.mvc.Result;
import play.mvc.Results;

/**
 * Created by hugo on 4/14/15.
 */
public class Analyzer {
    public static Result getMetadata(String url) {
        ObjectNode result = Json.newObject();

        result.put("url", url);

        return Results.ok(result);
    }
}
