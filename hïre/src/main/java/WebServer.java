
import dao.ApplicantDao;
import dao.Sql2oApplicantDao;
import model.Applicant;
import model.Course;
import org.sql2o.Sql2o;
import spark.ModelAndView;
import spark.Redirect;
import spark.template.handlebars.HandlebarsTemplateEngine;

import java.io.Console;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static spark.Spark.*;

public class WebServer {
    public static void main(String[] args) {

        staticFileLocation("/templates");
        get("/", (request, response) -> {
            // remove cookie username
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
            String jhed = request.cookie("jhed");
            String profileType = request.cookie("profileType");
            boolean isStaffMember = false;
            if (profileType.equals("Staff Member")) {
                isStaffMember = true;
            }
            // using jhed and profile type, extract info from applicant or staff database
            List<Course> courseList = new ArrayList<Course>();
            // add courseList either eligible courses or courses
            model.put("jhed", jhed);
            model.put("courseList", courseList);
            model.put("isStaffMember", isStaffMember);
            return new ModelAndView(model, "landing.hbs");
        }, new HandlebarsTemplateEngine());

        post("/login", (request, response) -> {
            String jhed = request.queryParams("jhed");
            response.cookie("jhed", jhed);
            // also need to have profileType
            response.cookie("profileType", "Student");
            response.redirect("/landing");
            return null;
        }, new HandlebarsTemplateEngine());

        post("/signup", (request, response) -> {
            String name = request.queryParams("firstName")+" "+request.queryParams("lastName");
            String jhed = request.queryParams("jhed");
            String email = jhed + "@jhu.edu";
            String profileType = request.queryParams("profileType");
            String course = request.queryParams("courses");
            // use information to create either an applicant or staff member
            // pass the id to /landing
            response.cookie("jhed", jhed);
            response.cookie("profileType", profileType);
            response.redirect("/landing");
            return null;
        }, new HandlebarsTemplateEngine());
    }

}

