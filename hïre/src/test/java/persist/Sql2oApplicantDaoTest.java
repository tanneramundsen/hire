package persist;

import dao.DaoFactory;
import dao.Sql2oStaffMemberDao;
import dao.Sql2oApplicantDao;
import dao.Sql2oCourseDao;
import exception.DaoException;
import model.Applicant;
import model.Course;
import model.StaffMember;
import org.junit.*;


import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

import static org.junit.Assert.*;

public class Sql2oApplicantDaoTest {

    private Sql2oStaffMemberDao staffMemberDao;
    private Sql2oApplicantDao applicantDao;
    private Sql2oCourseDao courseDao;

    @BeforeClass
    public static void beforeClass() {
        DaoFactory.DROP_TABLES_IF_EXIST = true;
    }

    @Before
    public void setUp() throws URISyntaxException, ClassNotFoundException {
        //below method drops all dependent tables and creates new ones
        staffMemberDao = DaoFactory.getStaffMemberDao();
        applicantDao = DaoFactory.getApplicantDao();
        courseDao = DaoFactory.getCourseDao();
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
        List<StaffMember> instructors = new ArrayList();
        List<Applicant> hiredApplicants = new ArrayList();
        List<Applicant> interestedApplicants = new ArrayList();
        List<Applicant> shortlistedApplicants = new ArrayList();
        Course c1 = new Course(
                "oose",
                "601.434",
                instructors,
                "fall",
                false,
                "course description",
                "interview link",
                false,
                hiredApplicants,
                interestedApplicants,
                shortlistedApplicants);
        Applicant a1 = new Applicant("Tanner Amundsen", "tamunds1@jhu.edu", "tamunds1", null);
        List<Applicant> applicants = Collections.singletonList(a1);
        HashMap<Course, String> interestedCourses = new HashMap();
        interestedCourses.put(c1, "A");
        c1.setInterestedApplicants(applicants);
        c1.setHiredApplicants(applicants);
        a1.setInterestedCourses(interestedCourses);
        a1.setHiredCourse(c1);

        courseDao.add(c1);
        applicantDao.add(a1);

        Applicant a2 = applicantDao.read(a1.getId());
        HashMap<Course, String> interestedCourses2 = a2.getInterestedCourses();
        assertEquals(a1, a2);
        assertNotEquals(0, interestedCourses2.size());
        assertEquals("A", interestedCourses2.get(c1));
        assertEquals(c1, a2.getHiredCourse());
    }

    @Test
    public void updateApplicantWorks() {
        List<StaffMember> instructors = new ArrayList();
        List<Applicant> hiredApplicants = new ArrayList();
        List<Applicant> interestedApplicants = new ArrayList();
        List<Applicant> shortlistedApplicants = new ArrayList();
        Course c1 = new Course(
                "oose",
                "601.434",
                instructors,
                "fall",
                false,
                "course description",
                "interview link",
                false,
                hiredApplicants,
                interestedApplicants,
                shortlistedApplicants);
        Course c2 = new Course(
                "data structures",
                "601.262",
                instructors,
                "fall",
                false,
                "course description",
                "interview link",
                false,
                hiredApplicants,
                interestedApplicants,
                shortlistedApplicants);
        Applicant a1 = new Applicant("Michael Scott", "mscott@jhu.edu", "mscott", null);
        List<Applicant> applicants = Collections.singletonList(a1);
        HashMap<Course, String> interestedCourses = new HashMap();
        interestedCourses.put(c1, "A");
        c1.setInterestedApplicants(applicants);
        c1.setHiredApplicants(applicants);
        a1.setInterestedCourses(interestedCourses);
        a1.setHiredCourse(c1);

        courseDao.add(c1);
        applicantDao.add(a1);

        a1.setEmail("mscott5@jhu.edu");
        interestedCourses.put(c2, "B+");
        a1.setHiredCourse(c2);

        applicantDao.update(a1);

        Applicant a2 = applicantDao.read(a1.getJhed());
        HashMap<Course, String> courses2Check = a2.getInterestedCourses();
        assertEquals(a1, a2);
        assertTrue(a2.getEmail().equals("mscott5@jhu.edu"));
        assertEquals(2, courses2Check.size());
        assertEquals("B+", courses2Check.get(c2));
        assertEquals(c2, a2.getHiredCourse());
    }

    @Test
    public void deleteApplicantWorks() {
        List<StaffMember> instructors = new ArrayList();
        List<Applicant> hiredApplicants = new ArrayList();
        List<Applicant> interestedApplicants = new ArrayList();
        List<Applicant> shortlistedApplicants = new ArrayList();
        Course c1 = new Course(
                "oose",
                "601.434",
                instructors,
                "fall",
                false,
                "course description",
                "interview link",
                false,
                hiredApplicants,
                interestedApplicants,
                shortlistedApplicants);
        Applicant a1 = new Applicant("Tanner Amundsen", "tamunds1@jhu.edu", "tamunds1", null);
        List<Applicant> applicants = Collections.singletonList(a1);
        HashMap<Course, String> interestedCourses = new HashMap();
        interestedCourses.put(c1, "A");
        c1.setInterestedApplicants(applicants);
        c1.setHiredApplicants(applicants);
        a1.setInterestedCourses(interestedCourses);

        courseDao.add(c1);
        applicantDao.add(a1);


        applicantDao.delete(a1);
        Applicant a2 = applicantDao.read(a1.getId());
        assertNull(a2);
        //make sure delete didn 't delete course
        Course c2 = courseDao.read(c1.getId());
        assertNotNull(c2);
        //make sure deleted applicant does not show up on course 's hiredApplicant or qualified applicant lists
        assertEquals(0, c2.getHiredApplicants().size());
        assertEquals(0, c2.getInterestedApplicants().size());
    }

    @Test(expected = DaoException.class)
    public void addingApplicantWithNullNameFails() {
        Applicant a1 = new Applicant(null, "tamunds1@jhu.edu", "tamunds1", null);
        applicantDao.add(a1);
    }

    @Test
    public void findAllApplicantsWorks() {
        List<StaffMember> instructors = new ArrayList();
        List<Applicant> hiredApplicants = new ArrayList();
        List<Applicant> interestedApplicants = new ArrayList();
        List<Applicant> shortlistedApplicants = new ArrayList();
        Course c1 = new Course(
                "oose",
                "601.434",
                instructors,
                "fall",
                false,
                "course description",
                "interview link",
                false,
                hiredApplicants,
                interestedApplicants,
                shortlistedApplicants);
        Applicant applicant1 = new Applicant("Tanner Amundsen", "tamunds1@jhu.edu", "tamunds1", null);
        Applicant applicant2 = new Applicant("Jennifer Lin", "jlin123@jhu.edu", "jlin123", null);
        Applicant applicant3 = new Applicant("Madhu Rajmohan", "mrajmoh1@jhu.edu", "mrajmoh1", null);
        List<Applicant> applicants = Arrays.asList(applicant1, applicant2, applicant3);
        HashMap<Course, String> interestedCourses = new HashMap();
        interestedCourses.put(c1, "A");
        c1.setInterestedApplicants(applicants);
        c1.setHiredApplicants(applicants);
        applicant1.setInterestedCourses(interestedCourses);
        applicant2.setInterestedCourses(interestedCourses);
        applicant3.setInterestedCourses(interestedCourses);

        courseDao.add(c1);
        applicantDao.add(applicant1);
        applicantDao.add(applicant2);
        applicantDao.add(applicant3);

        List<Applicant> results = applicantDao.findAll();
        assertTrue(results.contains(applicant1));
        assertTrue(results.contains(applicant2));
        assertTrue(results.contains(applicant3));
    }

    @Test
    public void findByCourseIdWorks() {
        List<StaffMember> instructors = new ArrayList();
        List<Applicant> hiredApplicants = new ArrayList();
        List<Applicant> interestedApplicants = new ArrayList();
        List<Applicant> shortlistedApplicants = new ArrayList();
        Course c1 = new Course(
                "oose",
                "601.434",
                instructors,
                "fall",
                false,
                "course description",
                "interview link",
                false,
                hiredApplicants,
                interestedApplicants,
                shortlistedApplicants);
        Course c2 = new Course(
                "data structures",
                "601.262",
                instructors,
                "fall",
                false,
                "course description",
                "interview link",
                false,
                hiredApplicants,
                interestedApplicants,
                shortlistedApplicants);
        Applicant applicant1 = new Applicant("Tanner Amundsen", "tamunds1@jhu.edu", "tamunds1", null);
        Applicant applicant2 = new Applicant("Jennifer Lin", "jlin123@jhu.edu", "jlin123", null);
        Applicant applicant3 = new Applicant("Madhu Rajmohan", "mrajmoh1@jhu.edu", "mrajmoh1", null);
        List<Applicant> applicants_c1 = Arrays.asList(applicant1, applicant2, applicant3);
        List<Applicant> applicants_c2 = Arrays.asList(applicant1, applicant2);
        HashMap<Course, String> courses = new HashMap();
        courses.put(c1, "A");
        courses.put(c2, "A-");
        c1.setInterestedApplicants(applicants_c1);
        c1.setHiredApplicants(applicants_c1);
        c2.setInterestedApplicants(applicants_c2);
        c2.setHiredApplicants(applicants_c2);
        applicant1.setInterestedCourses(courses);
        applicant2.setInterestedCourses(courses);
        applicant3.setInterestedCourses(courses);

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
    }
}


