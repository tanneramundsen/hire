package dao;

import org.sql2o.Connection;
import org.sql2o.Sql2o;
import model.Applicant;
import model.Course;
import java.util.*;

import java.nio.file.Paths;

public final class DaoFactory {

    public static boolean DROP_TABLES_IF_EXIST = false;
    public static String PATH_TO_DATABASE_FILE = Paths.get("src", "main", "resources").toFile().getAbsolutePath() + "/db/Store.db";
    private static Sql2o sql2o;

    private DaoFactory() {
        // This class is not meant to be instantiated!
    }

    private static void instantiateSql2o() {
        if (sql2o == null) {
            final String URI = "jdbc:sqlite:" + PATH_TO_DATABASE_FILE;
            final String USERNAME = "";
            final String PASSWORD = "";
            sql2o = new Sql2o(URI, USERNAME, PASSWORD);
        }
    }

    public static Sql2oCourseDao getCourseDao() {
        instantiateSql2o();

        createCoursesTable(sql2o);

        // Create sibling tables
        createStaffMembersTable(sql2o);
        createApplicantsTable(sql2o);

        // Create dependent tables
        createStaffMembersCoursesTable(sql2o);
        createApplicantsCoursesTable(sql2o);
        return new Sql2oCourseDao(sql2o);
    }

    public static Sql2oStaffMemberDao getStaffMemberDao() {
        instantiateSql2o();
        createStaffMembersTable(sql2o);
        return new Sql2oStaffMemberDao(sql2o);
    }

    public static Sql2oApplicantDao getApplicantDao() {
        instantiateSql2o();
        createApplicantsTable(sql2o);
        return new Sql2oApplicantDao(sql2o);
    }

    private static void createApplicantsTable(Sql2o sql2o) {
        if (DROP_TABLES_IF_EXIST) dropApplicantsTableIfExists(sql2o);
        String sql = "CREATE TABLE IF NOT EXISTS Applicants(" +
                "id INTEGER PRIMARY KEY, " +
                "name VARCHAR(100) NOT NULL, " +
                "email VARCHAR(100) NOT NULL, " +
                "jhed VARCHAR(100) NOT NULL, " +
                "year VARCHAR(100), " +
                "majorAndMinor VARCHAR(100), " +
                "gpa DOUBLE, " +
                "registeredCredits DOUBLE, " +
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
                "FOREIGN KEY(hiredCourse) REFERENCES Courses(id) ON UPDATE CASCADE ON DELETE CASCADE " +
                "FOREIGN KEY(rankOne) REFERENCES Courses(id) ON UPDATE CASCADE ON DELETE CASCADE " +
                "FOREIGN KEY(rankTwo) REFERENCES Courses(id) ON UPDATE CASCADE ON DELETE CASCADE " +
                "FOREIGN KEY(rankThree) REFERENCES Courses(id) ON UPDATE CASCADE ON DELETE CASCADE " +
                ");";
        try (Connection conn = sql2o.open()) {
            conn.createQuery(sql).executeUpdate();
        }
    }

    private static void createCoursesTable(Sql2o sql2o) {
        if (DROP_TABLES_IF_EXIST) dropCoursesTableIfExists(sql2o);
        String sql = "CREATE TABLE IF NOT EXISTS Courses(" +
                "id INTEGER PRIMARY KEY, " +
                "name VARCHAR(100) NOT NULL, " +
                "courseNumber VARCHAR(100) NOT NULL, " +
                "semester VARCHAR(100) NOT NULL, " +
                "hiringComplete INTEGER, " +
                "courseDescription VARCHAR(1000), " +
                "interviewLink VARCHAR(100)" +
                ");";
        try (Connection conn = sql2o.open()) {
            conn.createQuery(sql).executeUpdate();
        }
    }

    private static void createStaffMembersTable(Sql2o sql2o) {
        if (DROP_TABLES_IF_EXIST) dropStaffMembersTableIfExists(sql2o);
        String sql = "CREATE TABLE IF NOT EXISTS StaffMembers(" +
                "id INTEGER PRIMARY KEY," +
                "name VARCHAR(100) NOT NULL, " +
                "jhed VARCHAR(100) NOT NULL" +
                ");";
        try (Connection conn = sql2o.open()) {
            conn.createQuery(sql).executeUpdate();
        }
    }

    private static void createStaffMembersCoursesTable(Sql2o sql2o) {
        if (DROP_TABLES_IF_EXIST) dropStaffMemberCoursesTableIfExists(sql2o);
        String sql = "CREATE TABLE IF NOT EXISTS StaffMembers_Courses(" +
                "staffId INTEGER NOT NULL, " +
                "courseId INTEGER NOT NULL, " +
                "PRIMARY KEY (staffId, courseId), " +
                "FOREIGN KEY (staffId) REFERENCES StaffMembers(id) ON UPDATE CASCADE ON DELETE CASCADE " +
                "FOREIGN KEY (courseId) REFERENCES Courses(id) ON UPDATE CASCADE ON DELETE CASCADE" +
                ");";
        try (Connection conn = sql2o.open()) {
            conn.createQuery(sql).executeUpdate();
        }
    }

    private static void createApplicantsCoursesTable(Sql2o sql2o) {
        if (DROP_TABLES_IF_EXIST) dropApplicantsCoursesTableIfExists(sql2o);
        String sql = "CREATE TABLE IF NOT EXISTS Applicants_Courses(" +
                "applicantId INTEGER NOT NULL, " +
                "courseId INTEGER NOT NULL, " +
                "grade VARCHAR(100) DEFAULT 'Not Taken', " +
                "interested INTEGER DEFAULT 0, " +
                "shortlisted INTEGER DEFAULT 0, " +
                "hired INTEGER DEFAULT 0, " +
                "headCAInterest INTEGER DEFAULT 0, " +
                "previousCA INTEGER DEFAULT 0, " +
                "PRIMARY KEY (applicantId, courseId), " +
                "FOREIGN KEY (applicantId) REFERENCES Applicants(id) ON UPDATE CASCADE ON DELETE CASCADE " +
                "FOREIGN KEY (courseId) REFERENCES Courses(id) ON UPDATE CASCADE ON DELETE CASCADE" +
                ");";
        try (Connection conn = sql2o.open()) {
            conn.createQuery(sql).executeUpdate();
        }
    }

    private static void dropApplicantsTableIfExists(Sql2o sql2o) {
        String sql = "DROP TABLE IF EXISTS Applicants;";
        try (Connection conn = sql2o.open()) {
            conn.createQuery(sql).executeUpdate();
        }
    }

    private static void dropCoursesTableIfExists(Sql2o sql2o) {
        String sql = "DROP TABLE IF EXISTS Courses;";
        try (Connection conn = sql2o.open()) {
            conn.createQuery(sql).executeUpdate();
        }
    }

    private static void dropStaffMembersTableIfExists(Sql2o sql2o) {
        String sql = "DROP TABLE IF EXISTS StaffMembers;";
        try (Connection conn = sql2o.open()) {
            conn.createQuery(sql).executeUpdate();
        }
    }

    private static void dropStaffMemberCoursesTableIfExists(Sql2o sql2o) {
        String sql = "DROP TABLE IF EXISTS StaffMembers_Courses;";
        try (Connection conn = sql2o.open()) {
            conn.createQuery(sql).executeUpdate();
        }
    }

    private static void dropApplicantsCoursesTableIfExists(Sql2o sql2o) {
        String sql = "DROP TABLE IF EXISTS Applicants_Courses;";
        try (Connection conn = sql2o.open()) {
            conn.createQuery(sql).executeUpdate();
        }
    }

    public static List<Applicant> filterByHasPrevCAExperience() {
        String sql = "SELECT DISTINCT id, name, email, jhed, year, majorAndMinor, gpa, registeredCredits, " +
                "referenceEmail, resumeLink, fws, studentStatus, mostRecentPayroll, otherJobs, hoursAvailable, hiredCourse, " +
                "rankOne, rankTwo, rankThree " +
                "FROM Applicants " +
                "JOIN Applicants_Courses ON Applicants.id = Applicants_Courses.applicantId " +
                "WHERE previousCA = 1 ";
        try (Connection conn = sql2o.open()) {
            return conn.createQuery(sql).executeAndFetch(Applicant.class);
        }
    }

    public static List<Applicant> filterByHasPrevCAExperienceForThisClass(Course c) {
        int id = c.getId();
        String sql = "SELECT id, name, email, jhed, year, majorAndMinor, gpa, registeredCredits, " +
                "referenceEmail, resumeLink, fws, studentStatus, mostRecentPayroll, otherJobs, hoursAvailable, hiredCourse, " +
                "rankOne, rankTwo, rankThree " +
                "FROM Applicants " +
                "JOIN Applicants_Courses ON Applicants.id = Applicants_Courses.applicantId " +
                "WHERE previousCA = 1 AND courseId = :id ";
        try (Connection conn = sql2o.open()) {
            return conn.createQuery(sql)
                    .addParameter("id", id)
                    .executeAndFetch(Applicant.class);
        }
    }

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

    public static List<Applicant> filterByHeadCAInterest(Course c) {
        int id = c.getId();
        String sql = "SELECT id, name, email, jhed, year, majorAndMinor, gpa, registeredCredits, " +
                "referenceEmail, resumeLink, fws, studentStatus, mostRecentPayroll, otherJobs, hoursAvailable, hiredCourse, " +
                "rankOne, rankTwo, rankThree " +
                "FROM Applicants " +
                "JOIN Applicants_Courses ON Applicants.id = Applicants_Courses.applicantId " +
                "WHERE courseId = :id AND headCAInterest = 1 ";
        try (Connection conn = sql2o.open()) {
            return conn.createQuery(sql)
                    .addParameter("id", id)
                    .executeAndFetch(Applicant.class);
        }
    }

}
