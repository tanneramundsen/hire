package api;

import dao.DaoFactory;

import java.nio.file.Paths;

/**
 * This driver can be used to run the API server on localhost,
 * for inspecting and manual testing through e.g. Postman application.
 */
public class RunApiServer {
    public static void main(String[] args) {
        ApiServer.start();
    }
}