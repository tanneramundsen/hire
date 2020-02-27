package persist;

import api.ApiServer;

import dao.*;
import exception.DaoException;
import model.StaffMember;
import model.Course;
import org.junit.*;


import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.*;

public class Sql2oStaffMemberDaoTest {

    private Sql2oStaffMemberDao staffMemberDao;
    private Sql2oCourseDao courseDao;
    private Sql2oApplicantDao applicantDao;

    private String getResourcesPath() {
        Path resourceDirectory = Paths.get("src", "test", "resources");
        return resourceDirectory.toFile().getAbsolutePath();
    }

    @BeforeClass
    public static void beforeClass() throws Exception {
        DaoFactory.DROP_TABLES_IF_EXIST = true;
        DaoFactory.PATH_TO_DATABASE_FILE = Paths.get("src", "test", "resources").toFile().getAbsolutePath()
                + "/db/Test.db";
        ApiServer.INITIALIZE_WITH_SAMPLE_DATA = false;
        ApiServer.start();
    }

    @Before
    public void setUp() {
        // below method drops all dependent tables and creates new ones
        staffMemberDao = DaoFactory.getStaffMemberDao();
        courseDao = DaoFactory.getCourseDao();
        applicantDao = DaoFactory.getApplicantDao();

    }

    @Test
    public void addStaffMemberChangesId() {
        StaffMember staffMember = new StaffMember("Ali Madooei", "madooei1", null);
        assertEquals(0, staffMember.getId());
        staffMemberDao.add(staffMember);
        assertNotEquals(0, staffMember.getId());
    }

    @Test
    public void readStaffMemberWorks() {
        Course c1 = new Course(
                "oose",
                "601.434",
                null,
                "Spring 2020",
                false,
                null,
                null
        );
        StaffMember s1 = new StaffMember("Ali Madooei", "madooei1", null);
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
        StaffMember s1 = new StaffMember("Ali Madooei", "madooei1", null);
        List<StaffMember> staffMembers = Collections.singletonList(s1);
        List<Course> courses1 = Collections.singletonList(c1);
        List<Course> courses2 = Collections.singletonList(c2);
        c1.setInstructors(staffMembers);
        s1.setCourses(courses1);

        courseDao.add(c1);
        staffMemberDao.add(s1);


        s1.setName("Joanne Selinski");
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
        Course c1 = new Course(
                "oose",
                "601.434",
                null,
                "Spring 2020",
                false,
                null,
                null
        );
        StaffMember s1 = new StaffMember("Ali Madooei", "madooei1", null);
        List<StaffMember> staffMembers = Collections.singletonList(s1);
        List<Course> courses = Collections.singletonList(c1);
        c1.setInstructors(staffMembers);
        s1.setCourses(courses);

        courseDao.add(c1);
        staffMemberDao.add(s1);

        staffMemberDao.delete(s1);
        StaffMember s2 = staffMemberDao.read(s1.getId());
        assertNull(s2);
        //make sure delete didn't delete course
        Course c2 = courseDao.read(c1.getId());
        assertNotNull(c2);
        //make sure deleted staffMember does not show up on course's hiredStaffMember or
        //qualified staffMember lists
        assertEquals(0, c2.getInstructors().size());
    }

    @Test (expected = DaoException.class)
    public void addingStaffMemberWithNullNameFails() {
        StaffMember s1 = new StaffMember(null, "madooei1", null);
        staffMemberDao.add(s1);
    }

    @AfterClass
    public static void afterClass() throws Exception {
        ApiServer.stop();
    }

}

