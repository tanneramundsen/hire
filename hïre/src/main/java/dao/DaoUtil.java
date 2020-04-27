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
                null,
                false,
                null,
                null,
                null
                )
        );
        courseDao.add(new Course(
                "data structures",
                "601.226",
                null,
                "Spring 2020",
                false,
                null,
                null,
                false,
                null,
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
                null,
                false,
                null,
                null,
                null)
        );
    }

    public static void addSampleStaffMembers(CourseDao courseDao, StaffMemberDao staffMemberDao) {
        List<Course> courseList = courseDao.findAll();
        if (courseList.size() == 0) addSampleCourses(courseDao);

        Random random = new Random();

        StaffMember sm1 = new StaffMember("Ali Madooei", "madooei1", null, false);
        StaffMember sm2 = new StaffMember("Joanne Selinski", "jselinski1", null, false);
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
        HashMap<Course, String> notTakenMap = new HashMap<Course, String>();
        HashMap<Course, String> gradeAMap = new HashMap<Course, String>();;
        // Create course map to not taken or A grades
        for (Course course: courseList) {
            notTakenMap.put(course, "Not Taken");
            gradeAMap.put(course, "A");
        }
        if (courseList.size() == 0) addSampleCourses(courseDao);

        // Initialize sample applicants
        Applicant applicant1 = new Applicant("Tanner Amundsen", "tamunds1@jhu.edu", "tamunds1", gradeAMap);
        Applicant applicant2 = new Applicant("Jennifer Lin", "jlin123@jhu.edu", "jlin123", gradeAMap);
        Applicant applicant3 = new Applicant("Madhu Rajmohan", "mrajmoh1@jhu.edu", "mrajmoh1", gradeAMap);
        Applicant applicant4 = new Applicant("Daniela Torres", "dtorre17@jhu.edu", "dtorre17", notTakenMap);
        Applicant applicant5 = new Applicant("Chester Huynh", "xhuynh1@jhu.edu", "xhuynh1", notTakenMap);
        Applicant applicant6 = new Applicant("William Shakespeare", "wshake1@jhu.edu", "wshake1", notTakenMap);
        Applicant applicant7 = new Applicant("Jane Villaneuva", "jvill12@jhu.edu", "jvill12", notTakenMap);
        Applicant applicant8 = new Applicant("Petra Solano", "psolan3@jhu.edu", "psolan3", notTakenMap);
        Applicant applicant9 = new Applicant("Michael Cordero", "mcorder2@jhu.edu", "mcorder2", notTakenMap);
        Applicant applicant10 = new Applicant("Rafael Solano", "rsolan2@jhu.edu", "rsolan2", notTakenMap);
        Applicant applicant11 = new Applicant("Bill Roche", "broche1@jhu.edu", "broche1", notTakenMap);

        applicant1.setYear("Junior");
        applicant2.setYear("Senior");
        applicant3.setYear("Senior");
        applicant4.setYear("Sophomore");
        applicant5.setYear("PhD");
        applicant6.setYear("Sophomore");
        applicant7.setYear("Junior");
        applicant8.setYear("Junior");
        applicant9.setYear("Junior");
        applicant7.setPreviousCA(courseList);
        applicant8.setHeadCAInterest(courseList);
        applicant1.setPreviousCA(courseList);
        List<Applicant> applicantList = Arrays.asList(new Applicant[] {applicant1, applicant2, applicant3, applicant4, applicant5, applicant6, applicant7, applicant8, applicant9, applicant10, applicant11});

        applicant1.setHeadCAInterest(courseList);
        applicant2.setPreviousCA(courseList);
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

            // Add applicant to Applicants table
            applicantDao.add(applicant);

            // Update hiredApplicants list for Course POJO
            course.setHiredApplicants(hiredApplicants);

            // Update course with newly hired applicant
            courseDao.update(course);
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

    public static List<Course> addSISCourses(CourseDao courseDao, String url) {
        JSONArray coursesJSON;
        Set<Course> courses = new TreeSet<Course>();
        try {
            coursesJSON = readJsonFromUrl(url);

            for (Object o : coursesJSON) {
                if (o instanceof JSONObject) {
                    JSONObject courseJSON = (JSONObject) o;
                    String name = courseJSON.getString("Title");
                    String courseNumber = courseJSON.getString("OfferingName");
                    String semester = courseJSON.getString("Term");

                    Course course = new Course(name, courseNumber, null, semester, false,
                            "", "", false,null,
                            null,null);
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
        List<Course> ret = new ArrayList(courses);
        return ret;
    }
}
