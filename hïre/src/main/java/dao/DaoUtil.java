package dao;

import model.Applicant;
import model.Course;
import model.StaffMember;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.*;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.*;

public final class DaoUtil {

    private DaoUtil() {
        // This class is not meant to be instantiated!
    }

    public static void addSampleCourses(CourseDao courseDao) {
        courseDao.add(new Course(
                "oose",
                "601.434",
                null,
                "Spring 2020",
                false,
                null,
                null)
        );
        courseDao.add(new Course(
                "data structures",
                "601.226",
                null,
                "Spring 2020",
                false,
                null,
                null)
        );
        courseDao.add(new Course(
                "algorithms",
                "601.433",
                null,
                "Spring 2020",
                false,
                null,
                null)
        );
    }

    public static void addSampleStaffMembers(CourseDao courseDao, StaffMemberDao staffMemberDao) {
        List<Course> courseList = courseDao.findAll();
        if (courseList.size() == 0) addSampleCourses(courseDao);

        Random random = new Random();

        StaffMember sm1 = new StaffMember("Ali Madooei", "madooei1", null);
        StaffMember sm2 = new StaffMember("Joanne Selinski", "jselinski1", null);
        List<StaffMember> members = Arrays.asList(new StaffMember[] {sm1, sm2});

        for (Course course: courseList) {
            // Get random StaffMember object
            int index = random.nextInt(members.size());
            StaffMember staff = members.get(index);

            // Turn courses/staff into list
            List<Course> coursesTaught = Arrays.asList(course);
            List<StaffMember> instructors = Arrays.asList(staff);

            // Set the association between course and staff
            course.setInstructors(instructors);
            staff.setCourses(coursesTaught);

            // Add to StaffMembers and Courses tables
            staffMemberDao.add(staff);
            courseDao.update(course);
        }
    }

    public static void addSampleApplicants(CourseDao courseDao, ApplicantDao applicantDao) {
        List<Course> courseList = courseDao.findAll();
        if (courseList.size() == 0) addSampleCourses(courseDao);

        // Initialize sample applicants
        Applicant applicant1 = new Applicant("Tanner Amundsen", "tamunds1@jhu.edu", "tamunds1", courseList);
        Applicant applicant2 = new Applicant("Jennifer Lin", "jlin123@jhu.edu", "jlin123", courseList);
        Applicant applicant3 = new Applicant("Madhu Rajmohan", "mrajmoh1@jhu.edu", "mrajmoh1", courseList);
        Applicant applicant4 = new Applicant("Daniela Torres", "dtorre17@jhu.edu", "dtorre17", courseList);
        Applicant applicant5 = new Applicant("Chester Huynh", "xhuynh1@jhu.edu", "xhuynh1", courseList);
        List<Applicant> applicantList = Arrays.asList(new Applicant[] {applicant1, applicant2, applicant3, applicant4, applicant5});

        Random random = new Random();

        // Assign random courses to applicants
        for (Applicant applicant: applicantList) {
            int index = random.nextInt(courseList.size());
            Course course = courseList.get(index);

            // Assign random course to applicant
            applicant.setHiredCourse(course);

            // Add applicant to hiredApplicants for the course
            List<Applicant> hiredApplicants = course.getHiredApplicants();
            if (hiredApplicants == null) {
                hiredApplicants = Arrays.asList(new Applicant[]{applicant});
            } else {
                hiredApplicants.add(applicant);
            }

            // Update hiredApplicants list for Course POJO
            course.setHiredApplicants(hiredApplicants);

            // Add applicant to Applicants table
            applicantDao.add(applicant);
        }

        for (Course course: courseList) {
            course.setInterestedApplicants(applicantList);
            courseDao.update(course);
        }
    }

    private static String readAll(Reader rd) throws IOException {
        StringBuilder sb = new StringBuilder();
        int cp;
        while ((cp = rd.read()) != -1) {
            sb.append((char) cp);
        }
        return sb.toString();
    }

    public static JSONArray readJsonFromUrl(String url) throws IOException, JSONException {
        InputStream is = new URL(url).openStream();
        try {
            BufferedReader rd = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
            String jsonText = readAll(rd);
            JSONArray json = new JSONArray(jsonText);
            return json;
        } finally {
            is.close();
        }
    }

    public static void addSISCourses(CourseDao courseDao, String url) {
        JSONArray coursesJSON;
        try {
            coursesJSON = readJsonFromUrl(url);

            Set<Course> courses = new TreeSet<Course>();

            for (Object o : coursesJSON) {
                if (o instanceof JSONObject) {
                    JSONObject courseJSON = (JSONObject) o;
                    String name = courseJSON.getString("Title");
                    String courseNumber = courseJSON.getString("OfferingName");
                    String semester = courseJSON.getString("Term");

                    Course course = new Course(name, courseNumber, null, semester, false, null, null);
                    courses.add(course);
                }
            }

            // Add only unique courses
            for (Course course: courses) {
                courseDao.add(course);
            }


        } catch (IOException e) {
            System.err.println("Could access URL");
        } catch (JSONException e) {
            System.err.println("Could not convert fetched data to JSON");
        }
    }
}
