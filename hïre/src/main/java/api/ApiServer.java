package api;

import com.google.gson.Gson;

import dao.ApplicantDao;
import dao.CourseDao;
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
        app = startJavalin();

        app.get("/login", ctx -> {

        });
    }

    public static void stop() {
        app.stop();
    }

    private static Javalin startJavalin() {
        Gson gson = new Gson();
        JavalinJson.setFromJsonMapper(gson::fromJson);
        JavalinJson.setToJsonMapper(gson::toJson);

        return Javalin.create(config -> {
            config.addStaticFiles("/public");
        }).start(PORT);
    }


}
