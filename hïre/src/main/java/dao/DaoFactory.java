package dao;

import org.sql2o.Connection;
import org.sql2o.Sql2o;

public final class DaoFactory {

    public static boolean DROP_TABLES_IF_EXIST = false;
    public static String PATH_TO_DATABASE_FILE = "./Store.db";
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
        return new Sql2oCourseDao(sql2o);
    }

    public static Sql2oStaffMemberDao getStaffMemberDao() {
        instantiateSql2o();

        // Create parent tables
        createCoursesTable(sql2o);
        createStaffMembersTable(sql2o);

        // Create dependent tables
        createStaffMembersCoursesTable(sql2o);
        return new Sql2oStaffMemberDao(sql2o);
    }

    public static Sql2oApplicantDao getApplicantDao() {
        instantiateSql2o();

        // Create parent tables
        createCoursesTable(sql2o);
        createApplicantsTable(sql2o);

        // Create dependent tables
        createQualifiedApplicantsCoursesTable(sql2o);
        createHiredApplicantsCoursesTable(sql2o);
        return new Sql2oApplicantDao(sql2o);
    }

    private static void createApplicantsTable(Sql2o sql2o) {
        if (DROP_TABLES_IF_EXIST) dropApplicantsTableIfExists(sql2o);
        String sql = "CREATE TABLE IF NOT EXISTS Applicants(" +
                "id INTEGER PRIMARY KEY," +
                "name VARCHAR(100) NOT NULL," +
                "email VARCHAR(100) NOT NULL," +
                "jhed VARCHAR(100) NOT NULL, " +
                "hiredCourse INTEGER," +
                "FOREIGN KEY(hiredCourse) REFERENCES Courses(id)" +
                ");";
        try (Connection conn = sql2o.open()) {
            conn.createQuery(sql).executeUpdate();
        }
    }

    private static void createCoursesTable(Sql2o sql2o) {
        if (DROP_TABLES_IF_EXIST) dropCoursesTableIfExists(sql2o);
        String sql = "CREATE TABLE IF NOT EXISTS Courses(" +
                "id INTEGER PRIMARY KEY," +
                "name VARCHAR(100) NOT NULL, " +
                "courseNumber VARCHAR(100) NOT NULL, " +
                "semester VARCHAR(100) NOT NULL," +
                "hiringComplete BOOLEAN" +
                ");";
        try (Connection conn = sql2o.open()) {
            conn.createQuery(sql).executeUpdate();
        }
    }

    private static void createStaffMembersTable(Sql2o sql2o) {
        if (DROP_TABLES_IF_EXIST) dropStaffMembersTableIfExists(sql2o);
        String sql = "CREATE TABLE IF NOT EXISTS StaffMembers(" +
                "id INTEGER PRIMARY KEY," +
                "name VARCHAR(100) NOT NULL," +
                "jhed VARCHAR(100) NOT NULL" +
                ");";
        try (Connection conn = sql2o.open()) {
            conn.createQuery(sql).executeUpdate();
        }
    }

    private static void createStaffMembersCoursesTable(Sql2o sql2o) {
        if (DROP_TABLES_IF_EXIST) dropStaffMemberCoursesTableIfExists(sql2o);
        String sql = "CREATE TABLE IF NOT EXISTS StaffMembers_Courses(" +
                "id INTEGER PRIMARY KEY," +
                "staffId INTEGER," +
                "courseId INTEGER," +
                "FOREIGN KEY (staffId) REFERENCES StaffMembers(id)" +
                "FOREIGN KEY (courseId) REFERENCES Courses(id)" +
                ");";
        try (Connection conn = sql2o.open()) {
            conn.createQuery(sql).executeUpdate();
        }
    }

    private static void createQualifiedApplicantsCoursesTable(Sql2o sql2o) {
        if (DROP_TABLES_IF_EXIST) dropQualifiedApplicantsCoursesTableIfExists(sql2o);
        String sql = "CREATE TABLE IF NOT EXISTS QualifiedApplicants_Courses(" +
                "id INTEGER PRIMARY KEY," +
                "applicantId INTEGER," +
                "courseId INTEGER," +
                "FOREIGN KEY (applicantId) REFERENCES Applicants(id)" +
                "FOREIGN KEY (courseId) REFERENCES Courses(id)" +
                ");";
        try (Connection conn = sql2o.open()) {
            conn.createQuery(sql).executeUpdate();
        }
    }

    private static void createHiredApplicantsCoursesTable(Sql2o sql2o) {
        if (DROP_TABLES_IF_EXIST) dropHiredApplicantsCoursesTableIfExists(sql2o);
        String sql = "CREATE TABLE IF NOT EXISTS HiredApplicants_Courses(" +
                "id INTEGER PRIMARY KEY," +
                "applicantId INTEGER," +
                "courseId INTEGER," +
                "FOREIGN KEY (applicantId) REFERENCES Applicants(id)" +
                "FOREIGN KEY (courseId) REFERENCES Courses(id)" +
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


    private static void dropHiredApplicantsCoursesTableIfExists(Sql2o sql2o) {
        String sql = "DROP TABLE IF EXISTS HiredApplicants_Courses;";
        try (Connection conn = sql2o.open()) {
            conn.createQuery(sql).executeUpdate();
        }
    }

    private static void dropQualifiedApplicantsCoursesTableIfExists(Sql2o sql2o) {
        String sql = "DROP TABLE IF EXISTS QualifiedApplicants_Courses;";
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
}
