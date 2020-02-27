
import spark.ModelAndView;
import spark.Redirect;
import spark.template.handlebars.HandlebarsTemplateEngine;

import java.io.Console;
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
            Map<String, Object> model = new HashMap<String, Object>();
            String jhed = request.session().attribute("jhed");
            model.put("jhed", jhed);
            return new ModelAndView(model, "landing.hbs");
        }, new HandlebarsTemplateEngine());

        post("/login", (request, response) -> {
            Map<String, Object> model = new HashMap<String, Object>();
            String jhed = request.queryParams("jhed");
            request.session().attribute("jhed", jhed);
            model.put("jhed", jhed);
            return new ModelAndView(model, "landing.hbs");
        }, new HandlebarsTemplateEngine());
    }

}

