package persist;

import dao.*;
import exception.DaoException;
import model.Applicant;
import model.StaffMember;
import model.Course;
import org.junit.*;


import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.*;

public class Sql2oStaffMemberDaoTest {

    private Sql2oStaffMemberDao staffMemberDao;
    private Sql2oCourseDao courseDao;
    private Sql2oApplicantDao applicantDao;

    @BeforeClass
    public static void beforeClass() {
        DaoFactory.DROP_TABLES_IF_EXIST = true;
    }

    @Before
    public void setUp() throws URISyntaxException, ClassNotFoundException {
        //below method drops all dependent tables and creates new ones
        applicantDao = DaoFactory.getApplicantDao();
        staffMemberDao = DaoFactory.getStaffMemberDao();
        courseDao = DaoFactory.getCourseDao();
    }

    @Test
    public void addStaffMemberChangesId() {
        StaffMember staffMember = new StaffMember("Ali Madooei", "madooei1", null, false);
        assertEquals(0, staffMember.getId());
        staffMemberDao.add(staffMember);
        assertNotEquals(0, staffMember.getId());
    }

    @Test
    public void readStaffMemberWorks() {
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
        StaffMember s1 = new StaffMember("Ali Madooei", "madooei1", null, false);
        List<StaffMember> staffMembers = Collections.singletonList(s1);
        List<Course> courses = Collections.singletonList(c1);

        s1.setCourses(courses);
        c1.setInstructors(staffMembers);

        staffMemberDao.add(s1);
        courseDao.add(c1);

        StaffMember s2 = staffMemberDao.read(s1.getId());
        List<Course> courses2 = s2.getCourses();
        assertEquals(s1, s2);
        assertNotEquals(0, courses2.size());
        assertEquals(c1, courses2.get(0));
    }

    @Test
    public void updateStaffMemberWorks() {
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
        StaffMember s1 = new StaffMember("Ali Madooei", "madooei1", null, false);
        List<StaffMember> staffMembers = Collections.singletonList(s1);
        List<Course> courses1 = Collections.singletonList(c1);
        List<Course> courses2 = Collections.singletonList(c2);
        c1.setInstructors(staffMembers);
        s1.setCourses(courses1);

        staffMemberDao.add(s1);
        courseDao.add(c1);

        s1.setName("Joanne Selinski");
        s1.setJhed("jselinski");
        s1.setCourses(courses2);

        staffMemberDao.update(s1);

        StaffMember s2 = staffMemberDao.read(s1.getId());
        List<Course> courses2Check = s2.getCourses();

        assertEquals(s1, s2);

        assertTrue(s2.getName().equals("Joanne Selinski"));
        assertNotEquals(0, courses2Check.size());
        assertEquals(c2, courses2Check.get(0));
    }

    @Test
    public void deleteStaffMemberWorks() {
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
        StaffMember s1 = new StaffMember("Ali Madooei", "madooei1", null, false);
        List<StaffMember> staffMembers = Collections.singletonList(s1);
        List<Course> courses = Collections.singletonList(c1);
        c1.setInstructors(staffMembers);
        s1.setCourses(courses);

        staffMemberDao.add(s1);
        courseDao.add(c1);

        staffMemberDao.delete(s1);
        StaffMember s2 = staffMemberDao.read(s1.getId());
        assertNull(s2);
        //make sure delete didn't delete course
        Course c2 = courseDao.read(c1.getId());
        assertNotNull(c2);
        //make sure deleted staffMember does not show up on course's hiredStaffMember or qualified staffMember lists
        assertEquals(0, c2.getInstructors().size());
    }

    @Test (expected = DaoException.class)
    public void addingStaffMemberWithNullNameFails() {
        StaffMember s1 = new StaffMember(null, "madooei1", null, false);
        staffMemberDao.add(s1);
    }

}