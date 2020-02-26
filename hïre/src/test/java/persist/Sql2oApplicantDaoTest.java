package persist;

import api.ApiServer;

import dao.DaoFactory;
import dao.Sql2oApplicantDao;
import dao.Sql2oCourseDao;
import exception.DaoException;
import model.Applicant;
import model.Course;
import org.junit.*;


import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.*;

public class Sql2oApplicantDaoTest {

    private Sql2oApplicantDao applicantDao;
    private Sql2oCourseDao courseDao;

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
    }

    @Test
    public void addApplicantChangesId() {
        Applicant applicant = new Applicant("Tanner Amundsen", "tamunds1@jhu.edu", "tamunds1", null);
        assertEquals(0, applicant.getId());
        applicantDao.add(applicant);
        assertNotEquals(0, applicant.getId());
    }

    @Test
    public void readApplicantWorks() {
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
        List<Applicant> applicants = Collections.singletonList(a1);
        List<Course> courses = Collections.singletonList(c1);
        c1.setQualifiedApplicants(applicants);
        c1.setHiredApplicants(applicants);
        a1.setEligibleCourses(courses);
        a1.setHiredCourse(c1);

        courseDao.add(c1);
        applicantDao.add(a1);

        Applicant a2 = applicantDao.read(a1.getId());
        List<Course> courses2 = a2.getEligibleCourses();
        assertEquals(a1, a2);
        assertNotEquals(0, courses2.size());
        assertEquals(c1, courses2.get(0));
        assertEquals(c1, a2.getHiredCourse());
    }

    @Test
    public void updateApplicantWorks() {
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
        Applicant a1 = new Applicant("Tanner Amundsen", "tamunds1@jhu.edu", "tamunds1", null);
        List<Applicant> applicants = Collections.singletonList(a1);
        List<Course> courses1 = Collections.singletonList(c1);
        List<Course> courses2 = Collections.singletonList(c2);
        c1.setQualifiedApplicants(applicants);
        c1.setHiredApplicants(applicants);
        a1.setEligibleCourses(courses1);
        a1.setHiredCourse(c1);

        courseDao.add(c1);
        applicantDao.add(a1);


        a1.setEmail("NOTtamunds1@jhu.edu");
        a1.setEligibleCourses(courses2);
        a1.setHiredCourse(c2);

        applicantDao.update(a1);

        Applicant a2 = applicantDao.read(a1.getId());
        List<Course> courses2Check = a2.getEligibleCourses();
        assertEquals(a1, a2);
        assertTrue(a2.getEmail().equals("NOTtamunds1@jhu.edu"));
        assertNotEquals(0, courses2Check.size());
        assertEquals(c2, courses2Check.get(0));
        assertEquals(c2, a2.getHiredCourse());
    }

    @Test
    public void deleteApplicantWorks() {
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
        List<Applicant> applicants = Collections.singletonList(a1);
        List<Course> courses = Collections.singletonList(c1);
        c1.setQualifiedApplicants(applicants);
        c1.setHiredApplicants(applicants);
        a1.setEligibleCourses(courses);

        courseDao.add(c1);
        applicantDao.add(a1);


        applicantDao.delete(a1);
        Applicant a2 = applicantDao.read(a1.getId());
        assertNull(a2);
        //make sure delete didn't delete course
        Course c2 = courseDao.read(c1.getId());
        assertNotNull(c2);
        //make sure deleted applicant does not show up on course's hiredApplicant or
        //qualified applicant lists
        assertEquals(0, c2.getHiredApplicants().size());
        assertEquals(0, c2.getQualifiedApplicants().size());
    }

    @Test (expected = DaoException.class)
    public void addingApplicantWithNullNameFails() {
        Applicant a1 = new Applicant(null, "tamunds1@jhu.edu", "tamunds1", null);
        applicantDao.add(a1);
    }

    @Test
    public void findAllApplicantsWorks() {
        Course c1 = new Course(
                "oose",
                "601.434",
                null,
                "Spring 2020",
                false,
                null,
                null
        );
        Applicant applicant1 = new Applicant("Tanner Amundsen", "tamunds1@jhu.edu", "tamunds1", null);
        Applicant applicant2 = new Applicant("Jennifer Lin", "jlin123@jhu.edu", "jlin123", null);
        Applicant applicant3 = new Applicant("Madhu Rajmohan", "mrajmoh1@jhu.edu", "mrajmoh1", null);
        List<Applicant> applicants = Arrays.asList(applicant1, applicant2, applicant3);
        List<Course> courses = Collections.singletonList(c1);
        c1.setQualifiedApplicants(applicants);
        c1.setHiredApplicants(applicants);
        applicant1.setEligibleCourses(courses);
        applicant2.setEligibleCourses(courses);
        applicant3.setEligibleCourses(courses);

        courseDao.add(c1);
        applicantDao.add(applicant1);
        applicantDao.add(applicant2);
        applicantDao.add(applicant3);

        List<Applicant> results = applicantDao.findAll();
        assertTrue(results.contains(applicant1));
        assertTrue(results.contains(applicant2));
        assertTrue(results.contains(applicant3));
        for (Applicant applicant : results) {
            List<Course> courses2 = applicant.getEligibleCourses();
            assertNotEquals(0, courses2.size());
            assertEquals(c1, courses2.get(0));
        }
    }

    @Test
    public void findByCourseIdWorks() {
        Course c1 = new Course(
                "oose",
                "601.434",
                null,
                "Spring 2020",
                false,
                null,
                null
        );
        Course c2 =  new Course(
                "data structures",
                "601.226",
                null,
                "Spring 2020",
                false,
                null,
                null
        );
        Applicant applicant1 = new Applicant("Tanner Amundsen", "tamunds1@jhu.edu", "tamunds1", null);
        Applicant applicant2 = new Applicant("Jennifer Lin", "jlin123@jhu.edu", "jlin123", null);
        Applicant applicant3 = new Applicant("Madhu Rajmohan", "mrajmoh1@jhu.edu", "mrajmoh1", null);
        List<Applicant> applicants_c1 = Arrays.asList(applicant1, applicant2, applicant3);
        List<Applicant> applicants_c2 = Arrays.asList(applicant1, applicant2);
        List<Course> courses_just_c1 = Collections.singletonList(c1);
        List<Course> courses_c1_and_c2 = Arrays.asList(c1, c2);
        c1.setQualifiedApplicants(applicants_c1);
        c1.setHiredApplicants(applicants_c1);
        c2.setQualifiedApplicants(applicants_c2);
        c2.setHiredApplicants(applicants_c2);
        applicant1.setEligibleCourses(courses_c1_and_c2);
        applicant2.setEligibleCourses(courses_c1_and_c2);
        applicant3.setEligibleCourses(courses_just_c1);

        courseDao.add(c1);
        courseDao.add(c2);
        applicantDao.add(applicant1);
        applicantDao.add(applicant2);
        applicantDao.add(applicant3);

        List<Applicant> results_c1 = applicantDao.findByCourseId(c1.getId());
        assertTrue(results_c1.contains(applicant1));
        assertTrue(results_c1.contains(applicant2));
        assertTrue(results_c1.contains(applicant3));
        List<Applicant> results_c2 = applicantDao.findByCourseId(c2.getId());
        assertTrue(results_c2.contains(applicant1));
        assertTrue(results_c2.contains(applicant2));
        assertFalse(results_c2.contains(applicant3));
    }

    @AfterClass
    public static void afterClass() throws Exception {
        ApiServer.stop();
    }

}
