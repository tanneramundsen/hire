package api;

import com.google.gson.Gson;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import dao.DaoFactory;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

public class ApiServerTest {

    private static Gson gson = new Gson();

    @BeforeClass
    public static void beforeClass() throws Exception {
        DaoFactory.DROP_TABLES_IF_EXIST = true;
        ApiServer.INITIALIZE_WITH_SAMPLE_DATA = true;
        ApiServer.start();
    }

    @AfterClass
    public static void afterClass() throws Exception {
        ApiServer.stop();
    }
}
