package api;

import com.google.gson.Gson;

import dao.ApplicantDao;
import dao.CourseDao;
import dao.StaffMemberDao;
import dao.DaoFactory;
import dao.DaoUtil;
import io.javalin.Javalin;
import io.javalin.plugin.json.JavalinJson;
import model.Course;


import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class ApiServer {

    public static boolean INITIALIZE_WITH_SAMPLE_DATA = true;
    public static int PORT = 7000;
    private static Javalin app;

    private ApiServer() {
        // This class is not meant to be instantiated!
    }

    public static void start() {
        CourseDao courseDao = DaoFactory.getCourseDao();
        ApplicantDao applicantDao = DaoFactory.getApplicantDao();
        StaffMemberDao staffMemberDao = DaoFactory.getStaffMemberDao();

        if (INITIALIZE_WITH_SAMPLE_DATA) {
            DaoUtil.addSampleCourses(courseDao);
            DaoUtil.addSampleApplicants(courseDao, applicantDao);
            DaoUtil.addSampleStaffMembers(courseDao, staffMemberDao);
        }

        Gson gson = new Gson();
        JavalinJson.setFromJsonMapper(gson::fromJson);
        JavalinJson.setToJsonMapper(gson::toJson);
        app = Javalin.create(config -> {
            config.addStaticFiles("/public");
        }).start(7000);

        // routing
        postCourses(courseDao);
        getCourses(courseDao);
    }
    private static void postCourses(CourseDao courseDao) {
        // client adds a course through HTTP POST request
        app.post("/courses", ctx -> {
            Course course = ctx.bodyAsClass(Course.class);
            try {
                courseDao.add(course);
                ctx.status(201); // created successfully
                ctx.json(course);
            } catch (RuntimeException ex) {
                throw new RuntimeException(ex.getMessage(), ex); // FIX!!!!
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
        /**
         // Routing
        getHomepage();
        getCourses(courseDao);
        postCourses(courseDao);
        getReviewsForCourse(reviewDao);
        postReviewForCourse(reviewDao);

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
         */

    public static void stop() {
        app.stop();
    }




}
