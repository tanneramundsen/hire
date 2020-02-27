<<<<<<< HEAD

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
=======
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
>>>>>>> 03f7366e877c93edcc4cfca99662b97d34bbff96

public class WebServer {
    public static void main(String[] args) {

<<<<<<< HEAD
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
            if (profileType.equals("Professor")) {
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

=======
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
>>>>>>> 03f7366e877c93edcc4cfca99662b97d34bbff96
