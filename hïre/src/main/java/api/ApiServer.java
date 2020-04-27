package api;

import com.google.gson.Gson;

import dao.ApplicantDao;
import dao.CourseDao;
import dao.StaffMemberDao;
import dao.DaoFactory;
import dao.DaoUtil;
import exception.ApiError;
import exception.DaoException;
import io.javalin.Javalin;
import io.javalin.plugin.json.JavalinJson;
import model.Course;
import model.StaffMember;
import model.Applicant;
import spark.ModelAndView;
import spark.template.handlebars.HandlebarsTemplateEngine;


import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static spark.Spark.*;

public final class ApiServer {

    public static boolean INITIALIZE_WITH_SAMPLE_DATA = true;
    public static int PORT = 7000;
    private static Javalin app;

    public static String school = "whiting school of engineering".replace(" ", "%20");;
    public static String dept = "EN computer science".replace(" ", "%20");;
    public static String key = "R6HJMT7GFtXsTjRcjp4zrypfpNpq4108";
    private static String url = "https://sis.jhu.edu/api/classes/" + school + "/" + dept + "/current?key=" + key;

    private ApiServer() {
        // This class is not meant to be instantiated!
    }

    public static void start() throws URISyntaxException {
        ApplicantDao applicantDao = DaoFactory.getApplicantDao();
        StaffMemberDao staffMemberDao = DaoFactory.getStaffMemberDao();
        CourseDao courseDao = DaoFactory.getCourseDao();

        if (INITIALIZE_WITH_SAMPLE_DATA) {
            DaoUtil.addSISCourses(courseDao, url);
            //  DaoUtil.addSampleCourses(courseDao);
            // DaoUtil.addSampleApplicants(courseDao, applicantDao);
            // DaoUtil.addSampleStaffMembers(courseDao, staffMemberDao);
        }

        Gson gson = new Gson();
        JavalinJson.setFromJsonMapper(gson::fromJson);
        JavalinJson.setToJsonMapper(gson::toJson);
        app = Javalin.create(config -> {
            config.addStaticFiles("/templates");
        }).start(7000);

        // routing
        /**postCourses(courseDao);
        getCourses(courseDao);
        updateCourses(courseDao);
        deleteCourses(courseDao);
        postApplicants(applicantDao);
        getApplicants(applicantDao);
        updateApplicants(applicantDao);
        deleteApplicants(applicantDao);*/

        // Handle exceptions
        app.exception(ApiError.class, (exception, ctx) -> {
            ApiError err = (ApiError) exception;
            Map<String, Object> jsonMap = new HashMap<>();
            jsonMap.put("status", err.getStatus());
            jsonMap.put("errorMessage", err.getMessage());
            ctx.status(err.getStatus());
            ctx.json(jsonMap);
        });

        // runs after every request (even if an exception occurred)
        app.after(ctx -> {
            // run after all requests
            ctx.contentType("application/json");
        });

    }

    private static void postCourses(CourseDao courseDao) {
        // client adds a course through HTTP POST request
        app.post("/courses", ctx -> {
            Course course = ctx.bodyAsClass(Course.class);
            try {
                courseDao.add(course);
                ctx.status(201); // created successfully
                ctx.json(course);
            } catch (DaoException ex) {
                throw new ApiError(ex.getMessage(), 500);
            }
        });
    }

    private static void updateCourses(CourseDao courseDao) {
        // client updates a course through HTTP POST request
        app.post("/coursesUpdate", ctx -> {
            Course course = ctx.bodyAsClass(Course.class);
            try {
                courseDao.update(course);
                ctx.status(201); // created successfully
                ctx.json(course);
            } catch (DaoException ex) {
                throw new ApiError(ex.getMessage(), 500);
            }
        });
    }

    private static void getCourses(CourseDao courseDao) {
        // handle HTTP Get request to retrieve all courses
        app.get("/courses", ctx -> {
            List<Course> courses = courseDao.findAll();
            ctx.json(courses);
            ctx.status(200); // everything ok!
        });
    }

    private static void deleteCourses(CourseDao courseDao) {
        // client delete a course through HTTP POST request
        app.delete("/courses", ctx -> {
            Course course = ctx.bodyAsClass(Course.class);
            try {
                courseDao.delete(course);
                ctx.status(201); // created successfully
                ctx.json(course);
            } catch (DaoException ex) {
                throw new ApiError(ex.getMessage(), 500);
            }
        });
    }

    private static void postApplicants(ApplicantDao applicantDao) {
        // client adds a course through HTTP POST request
        app.post("/applicants", ctx -> {
            Applicant applicant = ctx.bodyAsClass(Applicant.class);
            try {
                applicantDao.add(applicant);
                ctx.status(201); // created successfully
                ctx.json(applicant);
            } catch (DaoException ex) {
                throw new ApiError(ex.getMessage(), 500);
            }
        });
    }

    private static void updateApplicants(ApplicantDao applicantDao) {
        // client updates a course through HTTP POST request
        app.post("/applicantsUpdate", ctx -> {
            Applicant applicant = ctx.bodyAsClass(Applicant.class);
            try {
                applicantDao.update(applicant);
                ctx.status(201); // created successfully
                ctx.json(applicant);
            } catch (DaoException ex) {
                throw new ApiError(ex.getMessage(), 500);
            }
        });
    }

    private static void getApplicants(ApplicantDao applicantDao) {
        // handle HTTP Get request to retrieve all courses
        app.get("/applicants", ctx -> {
            List<Applicant> applicants = applicantDao.findAll();
            ctx.json(applicants);
            ctx.status(200); // everything ok!
        });
    }

    private static void deleteApplicants(ApplicantDao applicantDao) {
        // client delete a course through HTTP POST request
        app.delete("/applicants", ctx -> {
            Applicant applicant = ctx.bodyAsClass(Applicant.class);
            try {
                applicantDao.delete(applicant);
                ctx.status(201); // created successfully
                ctx.json(applicant);
            } catch (DaoException ex) {
                throw new ApiError(ex.getMessage(), 500);
            }
        });
    }

    public static void stop() {
        app.stop();
    }

}
