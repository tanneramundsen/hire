import api.ApiServer;
import dao.DaoFactory;
import dao.Sql2oApplicantDao;
import dao.Sql2oCourseDao;
import dao.Sql2oStaffMemberDao;
import model.Applicant;
import model.Course;
import model.StaffMember;
import spark.ModelAndView;
import spark.template.handlebars.HandlebarsTemplateEngine;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static spark.Spark.*;

public class WebServer {
    public static void main(String[] args) {
        DaoFactory.DROP_TABLES_IF_EXIST = true;
        DaoFactory.PATH_TO_DATABASE_FILE = Paths.get("src", "main", "resources").toFile().getAbsolutePath()
                + "/db/Store.db";
        ApiServer.INITIALIZE_WITH_SAMPLE_DATA = true;

        Sql2oStaffMemberDao staffMemberDao = DaoFactory.getStaffMemberDao();
        Sql2oApplicantDao applicantDao = DaoFactory.getApplicantDao();
        Sql2oCourseDao courseDao = DaoFactory.getCourseDao();

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
            String name;
            List<Course> courseList = new ArrayList<Course>();
            boolean isStaffMember = false;
            if (profileType.equals("Professor")) {
                isStaffMember = true;
                name = staffMemberDao.read(jhed).getName();
                courseList = staffMemberDao.read(jhed).getCourses();
            } else {
                name = applicantDao.read(jhed).getName();
                courseList = applicantDao.read(jhed).getInterestedCourses();
            }
            model.put("name", name);
            model.put("courseList", courseList);
            model.put("isStaffMember", isStaffMember);

            return new ModelAndView(model, "landing.hbs");
        }, new HandlebarsTemplateEngine());


        get("/:id/courseinfo", (request, response) -> {
            Map<String, Object> model = new HashMap<String, Object>();

            String jhed = request.cookie("jhed");
            int courseId = Integer.parseInt(request.params(":id"));
            model.put("courseID", courseId);
            String courseName = request.cookie("name");

            List<Applicant> interestedApplicants = new ArrayList<>();
            List<Applicant> hiredApplicants = new ArrayList<>();
            List<StaffMember> instructors = new ArrayList<>();

            String courseNumber = courseDao.read(courseId).getCourseNumber();
            interestedApplicants = courseDao.read(courseId).getInterestedApplicants();
            hiredApplicants = courseDao.read(courseId).getHiredApplicants();
            instructors = courseDao.read(courseId).getInstructors();


            /* later can put in semester */
            model.put("name", courseName);
            model.put("courseNumber", courseNumber);
            model.put("interestedApplicants", interestedApplicants);
            model.put("hiredApplicants", hiredApplicants);
            model.put("instructors", instructors);

            return new ModelAndView(model, "courseinfo.hbs");
        }, new HandlebarsTemplateEngine());

        /*
        get("/student-profile", (request, response) -> {
            Map<String, Object> model = new HashMap<String, Object>();
            String jhed = request.cookie("jhed");
            String profileType = request.cookie("profileType");
            String name;
            List<Course> courseList = new ArrayList<Course>();
            boolean isStaffMember = false;
            if (profileType.equals("Professor")) {
                isStaffMember = true;
                name = staffMemberDao.read(jhed).getName();
                courseList = staffMemberDao.read(jhed).getCourses();
            } else {
                name = applicantDao.read(jhed).getName();
                courseList = applicantDao.read(jhed).getInterestedCourses();
            }
            model.put("name", name);
            model.put("courseList", courseList);
            model.put("isStaffMember", isStaffMember);
            return new ModelAndView(model, "student-profile.hbs");
        }, new HandlebarsTemplateEngine());*/

        post("/login", (request, response) -> {
            String jhed = request.queryParams("jhed");
            response.cookie("jhed", jhed);
            String profileType = request.queryParams("profileType");
            response.cookie("profileType", profileType);
            response.redirect("/landing");
            return null;
        }, new HandlebarsTemplateEngine());

        post("/signup", (request, response) -> {
            String name = request.queryParams("firstName")+" "+request.queryParams("lastName");
            String jhed = request.queryParams("jhed");
            String email = jhed + "@jhu.edu";
            String profileType = request.queryParams("profileType");
            String[] courses = request.queryParamsValues("courses");
            List<Course> courseList = new ArrayList<Course>();
            for (String course:courses) {
                // TODO: find courses using name instead of creating new ones
                Course newCourse = new Course(course,"123",null,"Spring2020",false,null,null);
                courseDao.add(newCourse);
                courseList.add(newCourse);
            }
            if (profileType.equals("Professor")) {
                staffMemberDao.add(new StaffMember(name,jhed,courseList));
            } else {
                applicantDao.add(new Applicant(name,email,jhed,courseList,null));
            }
            // use information to create either an applicant or staff member
            response.cookie("jhed", jhed);
            response.cookie("profileType", profileType);
            response.redirect("/landing");
            return null;
        }, new HandlebarsTemplateEngine());
    }

}
