import api.ApiServer;
import dao.*;
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

        // Add in all courses from SIS API to Courses database
        String school = "whiting school of engineering".replace(" ", "%20");;
        String dept = "EN computer science".replace(" ", "%20");;
        String key = "R6HJMT7GFtXsTjRcjp4zrypfpNpq4108";
        String url = "https://sis.jhu.edu/api/classes/" + school + "/" + dept + "/current?key=" + key;
        DaoUtil.addSISCourses(courseDao,url);

        staticFileLocation("/templates");
        get("/", (request, response) -> {
            // TODO: remove cookie username?
            return new ModelAndView(new HashMap(), "index.hbs");
        }, new HandlebarsTemplateEngine());

        get("/login", (request, response) -> {
            return new ModelAndView(new HashMap(), "login.hbs");
        }, new HandlebarsTemplateEngine());

        post("/login", (request, response) -> {
            String jhed = request.queryParams("jhed");
            response.cookie("jhed", jhed);
            String profileType = request.queryParams("profileType");
            response.cookie("profileType", profileType);
            response.redirect("/landing");
            return null;
        }, new HandlebarsTemplateEngine());

        get("/signup", (request, response) -> {
            Map<String, Object> model = new HashMap<String, Object>();
            model.put("allCourses", courseDao.findAll());
            return new ModelAndView(model, "signup.hbs");
        }, new HandlebarsTemplateEngine());

        post("/signup", (request, response) -> {
            String name = request.queryParams("firstName")+" "+request.queryParams("lastName");
            String jhed = request.queryParams("jhed");
            String email = jhed + "@jhu.edu";
            String profileType = request.queryParams("profileType");
            String[] courses = request.queryParamsValues("courses");
            List<Course> courseList = new ArrayList<Course>();
            HashMap<Course, String> coursesHashMap = new HashMap<Course, String>();
            for (String course:courses) {
                Course newCourse = courseDao.read(course);
                courseList.add(newCourse);
                coursesHashMap.put(newCourse, null);
            }
            if (profileType.equals("Professor")) {
                staffMemberDao.add(new StaffMember(name,jhed,courseList));
            } else {
                applicantDao.add(new Applicant(name,email,jhed,coursesHashMap));
            }
            // use information to create either an applicant or staff member
            response.cookie("jhed", jhed);
            response.cookie("profileType", profileType);
            response.redirect("/landing");
            return null;
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
                courseList = applicantDao.read(jhed).getCoursesList();
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

            String courseNumber = courseDao.read(courseId).getCourseNumber();
            List<Applicant> interestedApplicants = courseDao.read(courseId).getInterestedApplicants();
            List<Applicant> hiredApplicants = courseDao.read(courseId).getHiredApplicants();
            List<StaffMember> instructors = courseDao.read(courseId).getInstructors();
            
            /* later can put in semester */
            model.put("name", courseName);
            model.put("courseNumber", courseNumber);
            model.put("interestedApplicants", interestedApplicants);
            model.put("hiredApplicants", hiredApplicants);
            model.put("instructors", instructors);

            return new ModelAndView(model, "courseinfo.hbs");
        }, new HandlebarsTemplateEngine());

        get("/studentprofile", (request, response) -> {
            Map<String, Object> model = new HashMap<String, Object>();
            String jhed = request.cookie("jhed");
            Applicant student = applicantDao.read(jhed);
            String name = student.getName();
            String email = student.getEmail();
            System.out.println(student.toString());
            List<Course> courseList = student.getCoursesList();
            model.put("name", name);
            model.put("email", email);
            if (student.getRankOne() != null) {
                model.put("rank1", student.getRankOne().getName());
            } else {
                model.put("rank1", null);
            }
            if (student.getRankTwo() != null) {
                model.put("rank2", student.getRankTwo().getName());
            } else {
                model.put("rank2", null);
            }
            if (student.getRankThree() != null) {
                model.put("rank3", student.getRankThree().getName());
            } else {
                model.put("rank3", null);
            }
            model.put("courseList", courseList);
            return new ModelAndView(model, "studentprofile.hbs");
        }, new HandlebarsTemplateEngine());

        post("/studentprofile", (request, response) -> {
            String rank1 = request.queryParams("rank1");
            String rank2 = request.queryParams("rank2");
            String rank3 = request.queryParams("rank3");
            String jhed = request.cookie("jhed");
            Applicant student = applicantDao.read(jhed);
            student.setRankOne(courseDao.read(rank1));
            student.setRankTwo(courseDao.read(rank2));
            student.setRankThree(courseDao.read(rank3));
            applicantDao.update(student);
            response.cookie("jhed", jhed);
            response.cookie("profileType", "Applicant");
            response.redirect("/landing");
            return null;
        }, new HandlebarsTemplateEngine());
    }

}
