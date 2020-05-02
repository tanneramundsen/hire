package dao;

import org.sql2o.Connection;
import org.sql2o.Sql2o;
import model.Applicant;
import model.Course;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;

import java.nio.file.Paths;

/**
 * Class to help create database tables.
 */
public final class DaoFactory {

    public static boolean DROP_TABLES_IF_EXIST = false;
    private static Sql2o sql2o;

    private DaoFactory() {
        // This class is not meant to be instantiated!
    }

    /**
     * Helper method to establish connection with database
     */
    private static void instantiateSql2o() throws URISyntaxException, ClassNotFoundException {
        String databaseUrl = System.getenv("DATABASE_URL");
        if (sql2o == null) {
            Class.forName("org.postgresql.Driver");
            // Not on Heroku, so use local username, password etc.
            if (databaseUrl == null) {
                final String URI = "jdbc:postgresql://localhost:5432/store";
                final String USERNAME = "postgres";
                final String PASSWORD = "postgres";

                sql2o = new Sql2o(URI, USERNAME, PASSWORD);
            } else {
                // On Heroku, so use environment databaseUrl
                URI dbUri = new URI(databaseUrl);

                String username = dbUri.getUserInfo().split(":")[0];
                String password = dbUri.getUserInfo().split(":")[1];
                String dbUrl = "jdbc:postgresql://" + dbUri.getHost() + ':'
                        + dbUri.getPort() + dbUri.getPath() + "?sslmode=require";

                sql2o = new Sql2o(dbUrl, username, password);
            }
        }
    }

    public static void dropAllTablesIfExists() throws URISyntaxException, ClassNotFoundException {
        instantiateSql2o();
        if (DROP_TABLES_IF_EXIST) {
            dropApplicantsCoursesTableIfExists(sql2o);
            dropStaffMemberCoursesTableIfExists(sql2o);
            dropApplicantsTableIfExists(sql2o);
            dropStaffMemberCoursesTableIfExists(sql2o);
            dropCoursesTableIfExists(sql2o);
        }
    }

    /**
     * Construct Courses table in database and any dependent tables.
     * @return Sql2oCourseDao object to help the rest of application interact
     * with Courses table and relevant child tables
     */
    public static Sql2oCourseDao getCourseDao() throws URISyntaxException, ClassNotFoundException {
        instantiateSql2o();
        createCoursesTable(sql2o);
        return new Sql2oCourseDao(sql2o);
    }

    /**
     * Construct StaffMembers table in database and any dependent tables.
     * @return Sql2oStaffMemberDao object to help the rest of application
     * interact with StaffMembers table and relevant child tables
     */
    public static Sql2oStaffMemberDao getStaffMemberDao() throws URISyntaxException, ClassNotFoundException {
        instantiateSql2o();
        createStaffMembersTable(sql2o);
        return new Sql2oStaffMemberDao(sql2o);
    }

    /**
     * Construct Applicants table in database and any dependent tables.
     * @return Sql2oApplicantDao object to help the rest of application
     * interact with Applicants table and relevant child tables
     */
    public static Sql2oApplicantDao getApplicantDao() throws URISyntaxException, ClassNotFoundException {
        instantiateSql2o();
        createApplicantsTable(sql2o);
        // Create course dependent tables
        createStaffMembersCoursesTable(sql2o);
        createApplicantsCoursesTable(sql2o);
        return new Sql2oApplicantDao(sql2o);
    }

    /**
     * Create Applicants table in database
     * @param sql2o Object that connects to database
     */
    private static void createApplicantsTable(Sql2o sql2o) {
        String sql = "CREATE TABLE IF NOT EXISTS Applicants(" +
                "id SERIAL PRIMARY KEY, " +
                "name VARCHAR(100) NOT NULL, " +
                "email VARCHAR(100) NOT NULL, " +
                "jhed VARCHAR(100) NOT NULL, " +
                "year VARCHAR(100), " +
                "majorAndMinor VARCHAR(100), " +
                "gpa DOUBLE PRECISION, " +
                "registeredCredits DOUBLE PRECISION, " +
                "referenceEmail VARCHAR(100), " +
                "resumeLink VARCHAR(100), " +
                "fws boolean, " +
                "studentStatus VARCHAR(100), " +
                "mostRecentPayroll VARCHAR(100), " +
                "otherJobs VARCHAR(100), " +
                "hoursAvailable INTEGER, " +
                "hiredCourse INTEGER, " +
                "rankOne INTEGER, " +
                "rankTwo INTEGER, " +
                "rankThree INTEGER, " +
                "FOREIGN KEY(hiredCourse) REFERENCES Courses(id) ON UPDATE CASCADE ON DELETE CASCADE, " +
                "FOREIGN KEY(rankOne) REFERENCES Courses(id) ON UPDATE CASCADE ON DELETE CASCADE, " +
                "FOREIGN KEY(rankTwo) REFERENCES Courses(id) ON UPDATE CASCADE ON DELETE CASCADE, " +
                "FOREIGN KEY(rankThree) REFERENCES Courses(id) ON UPDATE CASCADE ON DELETE CASCADE" +
                ");";
        try (Connection conn = sql2o.open()) {
            conn.createQuery(sql).executeUpdate();
        }
    }

    /**
     * Create Courses table in database
     * @param sql2o Object that connects to database
     */
    private static void createCoursesTable(Sql2o sql2o) {
        String sql = "CREATE TABLE IF NOT EXISTS Courses(" +
                "id SERIAL PRIMARY KEY, " +
                "name VARCHAR(100) NOT NULL, " +
                "courseNumber VARCHAR(100) NOT NULL, " +
                "semester VARCHAR(100) NOT NULL, " +
                "hiringComplete BOOLEAN, " +
                "courseDescription VARCHAR(1000), " +
                "interviewLink VARCHAR(100), " +
                "linkVisible BOOLEAN" +
                ");";
        try (Connection conn = sql2o.open()) {
            conn.createQuery(sql).executeUpdate();
        }
    }

    /**
     * Create StaffMembers table in database
     * @param sql2o Object that connects to database
     */
    private static void createStaffMembersTable(Sql2o sql2o) {
        String sql = "CREATE TABLE IF NOT EXISTS StaffMembers(" +
                "id SERIAL PRIMARY KEY, " +
                "name VARCHAR(100) NOT NULL, " +
                "jhed VARCHAR(100) NOT NULL, " +
                "isAdmin BOOLEAN" +
                ");";
        try (Connection conn = sql2o.open()) {
            conn.createQuery(sql).executeUpdate();
        }
    }

    /**
     * Create junction table in database for StaffMembers and Courses
     * @param sql2o Object that connects to database
     */
    private static void createStaffMembersCoursesTable(Sql2o sql2o) {
        String sql = "CREATE TABLE IF NOT EXISTS StaffMembers_Courses(" +
                "staffId INTEGER NOT NULL, " +
                "courseId INTEGER NOT NULL, " +
                "PRIMARY KEY (staffId, courseId), " +
                "FOREIGN KEY (staffId) REFERENCES StaffMembers(id) ON UPDATE CASCADE ON DELETE CASCADE, " +
                "FOREIGN KEY (courseId) REFERENCES Courses(id) ON UPDATE CASCADE ON DELETE CASCADE" +
                ");";
        try (Connection conn = sql2o.open()) {
            conn.createQuery(sql).executeUpdate();
        }
    }

    /**
     * Create junction table in database for Applicants and Courses
     * @param sql2o Object that connects to database
     */
    private static void createApplicantsCoursesTable(Sql2o sql2o) {
        String sql = "CREATE TABLE IF NOT EXISTS Applicants_Courses(" +
                "applicantId INTEGER NOT NULL, " +
                "courseId INTEGER NOT NULL, " +
                "grade VARCHAR(100) DEFAULT 'Not Taken', " +
                "interested BOOLEAN DEFAULT FALSE, " +
                "shortlisted BOOLEAN DEFAULT FALSE, " +
                "hired BOOLEAN DEFAULT FALSE, " +
                "headCAInterest BOOLEAN DEFAULT FALSE, " +
                "previousCA BOOLEAN DEFAULT FALSE, " +
                "PRIMARY KEY (applicantId, courseId), " +
                "FOREIGN KEY (applicantId) REFERENCES Applicants(id) ON UPDATE CASCADE ON DELETE CASCADE, " +
                "FOREIGN KEY (courseId) REFERENCES Courses(id) ON UPDATE CASCADE ON DELETE CASCADE" +
                ");";
        try (Connection conn = sql2o.open()) {
            conn.createQuery(sql).executeUpdate();
        }
    }

    /**
     * Drop Applicants table in database
     * @param sql2o Object that connects to database
     */
    private static void dropApplicantsTableIfExists(Sql2o sql2o) {
        String sql = "DROP TABLE IF EXISTS Applicants;";
        try (Connection conn = sql2o.open()) {
            conn.createQuery(sql).executeUpdate();
        }
    }

    /**
     * Drop Courses table in database
     * @param sql2o Object that connects to database
     */
    private static void dropCoursesTableIfExists(Sql2o sql2o) {
        String sql = "DROP TABLE IF EXISTS Courses;";
        try (Connection conn = sql2o.open()) {
            conn.createQuery(sql).executeUpdate();
        }
    }

    /**
     * Drop StaffMembers table in database
     * @param sql2o Object that connects to database
     */
    private static void dropStaffMembersTableIfExists(Sql2o sql2o) {
        String sql = "DROP TABLE IF EXISTS StaffMembers;";
        try (Connection conn = sql2o.open()) {
            conn.createQuery(sql).executeUpdate();
        }
    }

    /**
     * Drop StaffMemberCourses table in database
     * @param sql2o Object that connects to database
     */
    private static void dropStaffMemberCoursesTableIfExists(Sql2o sql2o) {
        String sql = "DROP TABLE IF EXISTS StaffMembers_Courses;";
        try (Connection conn = sql2o.open()) {
            conn.createQuery(sql).executeUpdate();
        }
    }

    /**
     * Drop ApplicantsCourses table in database
     * @param sql2o Object that connects to database
     */
    private static void dropApplicantsCoursesTableIfExists(Sql2o sql2o) {
        String sql = "DROP TABLE IF EXISTS Applicants_Courses;";
        try (Connection conn = sql2o.open()) {
            conn.createQuery(sql).executeUpdate();
        }
    }

    /**
     * Select applicants that have previous CA experience.
     * @return list of Applicants that previously CA'd a course
     */
    public static List<Applicant> filterByHasPrevCAExperience() {
        String sql = "SELECT DISTINCT id, name, email, jhed, year, majorAndMinor, gpa, registeredCredits, " +
                "referenceEmail, resumeLink, fws, studentStatus, mostRecentPayroll, otherJobs, hoursAvailable, hiredCourse, " +
                "rankOne, rankTwo, rankThree " +
                "FROM Applicants " +
                "JOIN Applicants_Courses ON Applicants.id = Applicants_Courses.applicantId " +
                "WHERE previousCA = true ";
        try (Connection conn = sql2o.open()) {
            return conn.createQuery(sql).executeAndFetch(Applicant.class);
        }
    }

    /**
     * Select applicants that have previous CA experience for a specified course.
     * @param c course for which to query for
     * @return list of applicants that previously CA'd for the specified course
     */
    public static List<Applicant> filterByHasPrevCAExperienceForThisClass(Course c) {
        int id = c.getId();
        String sql = "SELECT id, name, email, jhed, year, majorAndMinor, gpa, registeredCredits, " +
                "referenceEmail, resumeLink, fws, studentStatus, mostRecentPayroll, otherJobs, hoursAvailable, hiredCourse, " +
                "rankOne, rankTwo, rankThree " +
                "FROM Applicants " +
                "JOIN Applicants_Courses ON Applicants.id = Applicants_Courses.applicantId " +
                "WHERE previousCA = true AND courseId = :id ";
        try (Connection conn = sql2o.open()) {
            return conn.createQuery(sql)
                    .addParameter("id", id)
                    .executeAndFetch(Applicant.class);
        }
    }

    /**
     * Select applicants that have gotten above a B in a specified course.
     * @param c course for which to query for
     * @return list of applicants that have gotten a B in the course
     */
    public static List<Applicant> filterByHasGottenAboveB(Course c) {
        int id = c.getId();
        String sql = "SELECT id, name, email, jhed, year, majorAndMinor, gpa, registeredCredits, " +
                "referenceEmail, resumeLink, fws, studentStatus, mostRecentPayroll, otherJobs, hoursAvailable, hiredCourse, " +
                "rankOne, rankTwo, rankThree " +
                "FROM Applicants " +
                "JOIN Applicants_Courses ON Applicants.id = Applicants_Courses.applicantId " +
                "WHERE courseId = :id AND (grade='A+' OR grade='A' OR grade='A-' OR grade='B+') ";
        try (Connection conn = sql2o.open()) {
            return conn.createQuery(sql)
                    .addParameter("id", id)
                    .executeAndFetch(Applicant.class);
        }
    }

    /**
     * Select applicants that have expressed interest in being a head CA
     * for a specified course.
     * @param c course for which to query for
     * @return list of applicants that expressed interest in being a head CA
     * for the course
     */
    public static List<Applicant> filterByHeadCAInterest(Course c) {
        int id = c.getId();
        String sql = "SELECT id, name, email, jhed, year, majorAndMinor, gpa, registeredCredits, " +
                "referenceEmail, resumeLink, fws, studentStatus, mostRecentPayroll, otherJobs, hoursAvailable, hiredCourse, " +
                "rankOne, rankTwo, rankThree " +
                "FROM Applicants " +
                "JOIN Applicants_Courses ON Applicants.id = Applicants_Courses.applicantId " +
                "WHERE courseId = :id AND headCAInterest = true";
        try (Connection conn = sql2o.open()) {
            return conn.createQuery(sql)
                    .addParameter("id", id)
                    .executeAndFetch(Applicant.class);
        }
    }

    public static List<Applicant> filterBySophomore() {
        String sql = "SELECT DISTINCT id, name, email, jhed, year, majorAndMinor, gpa, registeredCredits, " +
                "referenceEmail, resumeLink, fws, studentStatus, mostRecentPayroll, otherJobs, hoursAvailable, hiredCourse, " +
                "rankOne, rankTwo, rankThree " +
                "FROM Applicants " +
                "JOIN Applicants_Courses ON Applicants.id = Applicants_Courses.applicantId " +
                "WHERE year='Sophomore'";
        try (Connection conn = sql2o.open()) {
            return conn.createQuery(sql).executeAndFetch(Applicant.class);
        }
    }

    public static List<Applicant> filterByJunior() {
        String sql = "SELECT DISTINCT id, name, email, jhed, year, majorAndMinor, gpa, registeredCredits, " +
                "referenceEmail, resumeLink, fws, studentStatus, mostRecentPayroll, otherJobs, hoursAvailable, hiredCourse, " +
                "rankOne, rankTwo, rankThree " +
                "FROM Applicants " +
                "JOIN Applicants_Courses ON Applicants.id = Applicants_Courses.applicantId " +
                "WHERE year='Junior'";
        try (Connection conn = sql2o.open()) {
            return conn.createQuery(sql).executeAndFetch(Applicant.class);
        }
    }

    public static List<Applicant> filterBySenior() {
        String sql = "SELECT DISTINCT id, name, email, jhed, year, majorAndMinor, gpa, registeredCredits, " +
                "referenceEmail, resumeLink, fws, studentStatus, mostRecentPayroll, otherJobs, hoursAvailable, hiredCourse, " +
                "rankOne, rankTwo, rankThree " +
                "FROM Applicants " +
                "JOIN Applicants_Courses ON Applicants.id = Applicants_Courses.applicantId " +
                "WHERE year='Senior'";
        try (Connection conn = sql2o.open()) {
            return conn.createQuery(sql).executeAndFetch(Applicant.class);
        }
    }

    public static List<Applicant> filterByCombined() {
        String sql = "SELECT DISTINCT id, name, email, jhed, year, majorAndMinor, gpa, registeredCredits, " +
                "referenceEmail, resumeLink, fws, studentStatus, mostRecentPayroll, otherJobs, hoursAvailable, hiredCourse, " +
                "rankOne, rankTwo, rankThree " +
                "FROM Applicants " +
                "JOIN Applicants_Courses ON Applicants.id = Applicants_Courses.applicantId " +
                "WHERE year='Combined (ugrad/grad)'";
        try (Connection conn = sql2o.open()) {
            return conn.createQuery(sql).executeAndFetch(Applicant.class);
        }
    }

    public static List<Applicant> filterByMastersFirst() {
        String sql = "SELECT DISTINCT id, name, email, jhed, year, majorAndMinor, gpa, registeredCredits, " +
                "referenceEmail, resumeLink, fws, studentStatus, mostRecentPayroll, otherJobs, hoursAvailable, hiredCourse, " +
                "rankOne, rankTwo, rankThree " +
                "FROM Applicants " +
                "JOIN Applicants_Courses ON Applicants.id = Applicants_Courses.applicantId " +
                "WHERE year='Masters (1st semester)'";
        try (Connection conn = sql2o.open()) {
            return conn.createQuery(sql).executeAndFetch(Applicant.class);
        }
    }

    public static List<Applicant> filterByMastersSecond() {
        String sql = "SELECT DISTINCT id, name, email, jhed, year, majorAndMinor, gpa, registeredCredits, " +
                "referenceEmail, resumeLink, fws, studentStatus, mostRecentPayroll, otherJobs, hoursAvailable, hiredCourse, " +
                "rankOne, rankTwo, rankThree " +
                "FROM Applicants " +
                "JOIN Applicants_Courses ON Applicants.id = Applicants_Courses.applicantId " +
                "WHERE year='Masters (2-4 semester)'";
        try (Connection conn = sql2o.open()) {
            return conn.createQuery(sql).executeAndFetch(Applicant.class);
        }
    }

    public static List<Applicant> filterByPhD() {
        String sql = "SELECT DISTINCT id, name, email, jhed, year, majorAndMinor, gpa, registeredCredits, " +
                "referenceEmail, resumeLink, fws, studentStatus, mostRecentPayroll, otherJobs, hoursAvailable, hiredCourse, " +
                "rankOne, rankTwo, rankThree " +
                "FROM Applicants " +
                "JOIN Applicants_Courses ON Applicants.id = Applicants_Courses.applicantId " +
                "WHERE year='PhD'";
        try (Connection conn = sql2o.open()) {
            return conn.createQuery(sql).executeAndFetch(Applicant.class);
        }
    }
}
