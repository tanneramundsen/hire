package model;

import dao.CourseDao;
import dao.UnirestCourseDao;
import spark.ModelAndView;
import spark.template.handlebars.HandlebarsTemplateEngine;

import java.util.HashMap;
import java.util.Map;

import static spark.Spark.get;
import static spark.Spark.post;

//import dao.InMemoryCourseDao;

public class WebServer {
    public static void main(String[] args) {

//    CourseDao courseDao = new InMemoryCourseDao();
//    courseDao.add(new Course("OOSE", "jhu-oose.com"));
//    courseDao.add(new Course("Gateway", "jhu-gateway.com"));

        CourseDao courseDao = new UnirestCourseDao();

        get("/", ((request, response) -> {
            Map<String, Object> model = new HashMap<>();
            model.put("JHED", request.cookie("JHED"));
            return new ModelAndView(model, "index.hbs");
        }), new HandlebarsTemplateEngine());

        post("/", ((request, response) -> {
            // TODO Capture client's username
            String JHED = request.queryParams("JHED");
            // TODO store that username
            response.cookie("JHED", JHED);
            // TODO refresh homepage
            response.redirect("/");
            return null;
        }), new HandlebarsTemplateEngine());

        get("/courses", ((request, response) -> {
            Map<String, Object> model = new HashMap<>();
            model.put("courseList", courseDao.findAll());
            return new ModelAndView(model, "courses.hbs");
        }),  new HandlebarsTemplateEngine());

        post("/courses", ((request, response) -> {
            // TODO Capture client's input
            String name = request.queryParams("coursename");
            //String url = request.queryParams("courseurl");
            // TODO create (and add) a course
           // courseDao.add(new Course(name));
            // TODO refresh courses page to show the new addition
            response.redirect("/courses");
            return null;
        }), new HandlebarsTemplateEngine());
    }
}