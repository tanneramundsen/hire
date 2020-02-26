package persist;

import api.ApiServer;

import dao.*;
import exception.DaoException;
import model.Applicant;
import model.StaffMember;
import model.Course;
import org.junit.*;


import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.*;

public class Sql2oCourseDaoTest {

    private Sql2oCourseDao courseDao;
    private Sql2oApplicantDao applicantDao;
    private Sql2oStaffMemberDao staffMemberDao;

    private String getResourcesPath() {
        Path resourceDirectory = Paths.get("src", "test", "resources");
        return resourceDirectory.toFile().getAbsolutePath();
    }

    @BeforeClass
    public void beforeClass() throws Exception {
        DaoFactory.DROP_TABLES_IF_EXIST = true;
        DaoFactory.PATH_TO_DATABASE_FILE = Paths.get("src", "test", "resources").toFile().getAbsolutePath()
                + "/db/Test.db";
        ApiServer.INITIALIZE_WITH_SAMPLE_DATA = false;
        ApiServer.start();
    }

    @Before
    public void setUp() {
        // below method drops all dependent tables and creates new ones
        applicantDao = (Sql2oApplicantDao) DaoFactory.getApplicantDao();
        courseDao = (Sql2oCourseDao) DaoFactory.getCourseDao();
        staffMemberDao = (Sql2oStaffMemberDao) DaoFactory.getStaffMemberDao();
    }

    @Test
    public void addCourseChangesId() {
        Course course = new Course(
                "oose",
                "601.434",
                null,
                "Spring 2020",
                false,
                null,
                null
        );
        assertEquals(0, course.getId());
        courseDao.add(course);
        assertNotEquals(0, course.getId());
    }

    @Test
    public void readCourseWorks() {
        Course c1 = new Course(
                "oose",
                "601.434",
                null,
                "Spring 2020",
                false,
                null,
                null
        );
        Applicant a1 = new Applicant("Tanner Amundsen", "tamunds1@jhu.edu", "tamunds1", null);
        StaffMember sm1 = new StaffMember("Ali Madooei", "madooei1", null);
        List<StaffMember> instructors = Collections.singletonList(sm1);
        List<Applicant> applicants = Collections.singletonList(a1);
        List<Course> courses = Collections.singletonList(c1);
        c1.setQualifiedApplicants(applicants);
        c1.setHiredApplicants(applicants);
        c1.setInstructors(instructors);
        a1.setEligibleCourses(courses);
        a1.setHiredCourse(c1);
        sm1.setCourses(courses);

        courseDao.add(c1);
        staffMemberDao.add(sm1);
        applicantDao.add(a1);

        Course c2 = courseDao.read(c1.getId());
        assertEquals(c1, c2);
        List<Applicant> checkQualifiedList = c2.getQualifiedApplicants();
        List<Applicant> checkHiredList = c2.getHiredApplicants();
        List<StaffMember> checkInstructor = c2.getInstructors();
        assertNotEquals(0, checkQualifiedList.size());
        assertNotEquals(0, checkHiredList.size());
        assertNotEquals(0, checkInstructor.size());
        assertEquals(a1, checkQualifiedList.get(0));
        assertEquals(a1, checkHiredList.get(0));
        assertEquals(sm1, checkInstructor.get(0));
    }

    @Test
    public void updateCourseWorks() {
        Course c1 = new Course(
                "oose",
                "601.434",
                null,
                "Spring 2020",
                false,
                null,
                null
        );
        Applicant a1 = new Applicant("Tanner Amundsen", "tamunds1@jhu.edu", "tamunds1", null);
        Applicant a2 = new Applicant("Jennifer Lin", "jlin123@jhu.edu", "jlin123", null);
        StaffMember sm1 = new StaffMember("Ali Madooei", "madooei1", null);
        StaffMember sm2 = new StaffMember("Joanne Selinski", "jselinski1", null);
        List<StaffMember> instructors1 = Collections.singletonList(sm1);
        List<Applicant> applicants1 = Collections.singletonList(a1);
        List<StaffMember> instructors2 = Collections.singletonList(sm2);
        List<Applicant> applicants2 = Collections.singletonList(a2);
        List<Course> courses = Collections.singletonList(c1);
        c1.setQualifiedApplicants(applicants1);
        c1.setHiredApplicants(applicants1);
        c1.setInstructors(instructors1);
        a1.setEligibleCourses(courses);
        a1.setHiredCourse(c1);
        sm1.setCourses(courses);

        courseDao.add(c1);
        staffMemberDao.add(sm1);
        applicantDao.add(a1);


        c1.setCourseNumber("500.500");
        c1.setQualifiedApplicants(applicants2);
        c1.setHiredApplicants(applicants2);
        c1.setInstructors(instructors2);

        courseDao.update(c1);

        Course c2 = courseDao.read(c1.getId());
        assertEquals(c1, c2);
        assertTrue(c2.getCourseNumber().equals("500.500"));

        List<Applicant> checkQualifiedList = c2.getQualifiedApplicants();
        List<Applicant> checkHiredList = c2.getHiredApplicants();
        List<StaffMember> checkInstructor = c2.getInstructors();
        assertNotEquals(0, checkQualifiedList.size());
        assertNotEquals(0, checkHiredList.size());
        assertNotEquals(0, checkInstructor.size());
        assertEquals(a2, checkQualifiedList.get(0));
        assertEquals(a2, checkHiredList.get(0));
        assertEquals(sm2, checkInstructor.get(0));
    }

    @Test
    public void deleteCourseWorks() {
        Course c1 = new Course(
                "oose",
                "601.434",
                null,
                "Spring 2020",
                false,
                null,
                null
        );
        Applicant a1 = new Applicant("Tanner Amundsen", "tamunds1@jhu.edu", "tamunds1", null);
        StaffMember sm1 = new StaffMember("Ali Madooei", "madooei1", null);
        List<StaffMember> instructors = Collections.singletonList(sm1);
        List<Applicant> applicants = Collections.singletonList(a1);
        List<Course> courses = Collections.singletonList(c1);
        c1.setQualifiedApplicants(applicants);
        c1.setHiredApplicants(applicants);
        c1.setInstructors(instructors);
        a1.setEligibleCourses(courses);
        a1.setHiredCourse(c1);
        sm1.setCourses(courses);

        courseDao.add(c1);
        staffMemberDao.add(sm1);
        applicantDao.add(a1);


        courseDao.delete(c1);
        Course c2 = courseDao.read(c1.getId());
        assertNull(c2);
        //make sure delete didn't delete Instructors and Applicants
        Applicant a2 = applicantDao.read(a1.getId());
        StaffMember sm2 = staffMemberDao.read(sm1.getId());
        assertNotNull(a2);
        assertNotNull(sm2);
        //make sure deleted course does not show up on applicants's eligibleCourses list or hiredCourse field
        assertEquals(0, a2.getEligibleCourses().size());
        assertNull(a2.getHiredCourse());
        //make sure deleted course does not show up on instructor's course list
        assertEquals(0, sm2.getCourses());
    }

    @Test (expected = DaoException.class)
    public void addingCourseWithNullNameFails() {
        Course c1 = new Course(
                null,
                "601.434",
                null,
                "Spring 2020",
                false,
                null,
                null
        );
        courseDao.add(c1);
    }

    @Test
    public void findAllCoursesWorks() {
        Course c1 = new Course(
                "oose",
                "601.434",
                null,
                "Spring 2020",
                false,
                null,
                null
        );
        Course c2 = new Course(
                "data structures",
                "601.226",
                null,
                "Spring 2020",
                false,
                null,
                null
        );
        Course c3 = new Course(
                "algorithms",
                "601.433",
                null,
                "Spring 2020",
                false,
                null,
                null
        );
        Applicant applicant1 = new Applicant("Tanner Amundsen", "tamunds1@jhu.edu", "tamunds1", null);
        Applicant applicant2 = new Applicant("Jennifer Lin", "jlin123@jhu.edu", "jlin123", null);
        Applicant applicant3 = new Applicant("Madhu Rajmohan", "mrajmoh1@jhu.edu", "mrajmoh1", null);
        StaffMember sm1 = new StaffMember("Ali Madooei", "madooei1", null);
        StaffMember sm2 = new StaffMember("Joanne Selinski", "jselinski1", null);

        List<Course> courses = Arrays.asList(c1, c2);
        List<Applicant> applicants = Arrays.asList(applicant1, applicant2, applicant3);
        List<StaffMember> instructors = Arrays.asList(sm1, sm2);

        c1.setQualifiedApplicants(applicants);
        c1.setHiredApplicants(applicants);
        c1.setInstructors(instructors);
        c2.setQualifiedApplicants(applicants);
        c2.setInstructors(instructors);
        applicant1.setEligibleCourses(courses);
        applicant2.setEligibleCourses(courses);
        applicant3.setEligibleCourses(courses);
        applicant1.setHiredCourse(c1);
        applicant2.setHiredCourse(c1);
        applicant3.setHiredCourse(c1);
        sm1.setCourses(courses);
        sm2.setCourses(courses);

        courseDao.add(c1);
        courseDao.add(c2);
        applicantDao.add(applicant1);
        applicantDao.add(applicant2);
        applicantDao.add(applicant3);
        staffMemberDao.add(sm1);
        staffMemberDao.add(sm2);

        List<Course> results = courseDao.findAll();
        assertTrue(results.contains(c1));
        assertTrue(results.contains(c2));
        assertFalse(results.contains(c3));
        for (Course course : results) {
            List<Applicant> qualifiedApplicantsCheck = course.getQualifiedApplicants();
            assertNotEquals(0, qualifiedApplicantsCheck.size());
            assertTrue(qualifiedApplicantsCheck.contains(applicant1));
            assertTrue(qualifiedApplicantsCheck.contains(applicant2));
            assertTrue(qualifiedApplicantsCheck.contains(applicant3));
            List<StaffMember> instructorsCheck = course.getInstructors();
            assertNotEquals(0, instructorsCheck.size());
            assertTrue(instructorsCheck.contains(sm1));
            assertTrue(instructorsCheck.contains(sm2));
        }

        List<Applicant> hiredApplicantsCheck = results.get(0).getHiredApplicants();
        assertTrue(hiredApplicantsCheck.contains(applicant1));
        assertTrue(hiredApplicantsCheck.contains(applicant2));
        assertTrue(hiredApplicantsCheck.contains(applicant3));
    }

    @AfterClass
    public static void afterClass() throws Exception {
        ApiServer.stop();
    }

}

