
import spark.ModelAndView;
import spark.template.handlebars.HandlebarsTemplateEngine;

import java.util.HashMap;
import java.util.Map;

import static spark.Spark.*;

public class WebServer {
    public static void main(String[] args) {
        staticFileLocation("/templates");
        get("/", (request, response) -> {
            return new ModelAndView(new HashMap(), "index.hbs");
        }, new HandlebarsTemplateEngine());

        get("/login", (request, response) -> {
            return new ModelAndView(new HashMap(), "login.hbs");
        }, new HandlebarsTemplateEngine());

        get("/signup", (request, response) -> {
            return new ModelAndView(new HashMap(), "signup.hbs");
        }, new HandlebarsTemplateEngine());

        get("/landing", (request, response) -> {
            Map<String, Object> model = new HashMap<>();
            model.put("jhed", request.cookie("jhed"));
            return new ModelAndView(model, "landing.hbs");
        }, new HandlebarsTemplateEngine());

        post("/login", ((request, response) -> {
            // TODO Capture client's username
            String jhed = request.queryParams("jhed");
            // TODO store that username
            response.cookie("jhed", jhed);
            // TODO refresh homepage
            response.redirect("/landing");
            return null;
        }), new HandlebarsTemplateEngine());
    }
}

