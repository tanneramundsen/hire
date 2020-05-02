package dao;

import exception.DaoException;
import model.Course;
import model.StaffMember;
import org.sql2o.Connection;
import org.sql2o.Sql2o;
import org.sql2o.Sql2oException;

import java.util.List;
import java.util.Map;

public class Sql2oStaffMemberDao implements StaffMemberDao {

    private Sql2o sql2o;

    public Sql2oStaffMemberDao(Sql2o sql2o) {
        this.sql2o = sql2o;
    }

    @Override
    public void add(StaffMember staffMember) throws DaoException {
        try(Connection conn = sql2o.open()) {
            String sql;
            int staffId = staffMember.getId();

            if (staffId == 0) {
                // Normal insert
                sql = "INSERT INTO StaffMembers(name, jhed, isAdmin) VALUES(:name, :jhed, :isAdmin)";
                staffId = (int) conn.createQuery(sql, true)
                        .bind(staffMember)
                        .executeUpdate()
                        .getKey();
                staffMember.setId(staffId);

                // Add courses
                List<Course> courses = staffMember.getCourses();
                if (courses != null) {
                    for (Course course: courses) {
                        int courseId = course.getId();
                        if (courseId == 0) {
                            sql = "INSERT INTO Courses(name, courseNumber, semester, hiringComplete, courseDescription, " +
                                    "interviewLink) VALUES(:name, :courseNumber, :semester, :hiringComplete, :courseDescription, " +
                                    ":interviewLink);";
                            courseId = (int) conn.createQuery(sql, true)
                                    .bind(course)
                                    .executeUpdate()
                                    .getKey();

                            course.setId(courseId);
                        }

                        // Insert into joining table
                        sql = "INSERT INTO StaffMembers_Courses(staffId, courseId) " +
                                "VALUES(:staffId, :courseId);";
                        conn.createQuery(sql)
                                .addParameter("staffId", staffId)
                                .addParameter("courseId", courseId)
                                .executeUpdate();
                    }
                }
            }
            else {
                // Staff member already exists, just update
                this.update(staffMember);
            }

        } catch (Sql2oException e) {
            throw new DaoException("Unable to add staff member", e);
        }
    }

    public StaffMember read(int id) throws DaoException {
        try (Connection conn = sql2o.open()) {
            // Populate non-list attributes of StaffMember object
            String sql = "SELECT * FROM StaffMembers " +
                    "WHERE id = :id";
            StaffMember staffMember = conn.createQuery(sql)
                    .addParameter("id", id)
                    .executeAndFetchFirst(StaffMember.class);

            if (staffMember == null) {
                return null;
            }

            // Get corresponding courses according to joining table
            List<Course> courses = readCourses(conn, id);
            staffMember.setCourses(courses);

            return staffMember;

        } catch (Sql2oException e) {
            throw new DaoException("Unable to read staff member", e);
        }
    }

    /**
     * Obtain and load StaffMember information that corresponds to a specified jhed
     * into an StaffMember POJO.
     * @param jhed string JHED corresponding to which Applicant to fetch from
     *           StaffMembers table
     * @return StaffMember corresponding to the specified jhed or null
     * @throws DaoException Runtime exception due to failed SQL query
     */
    public StaffMember read(String jhed) throws DaoException {
        try (Connection conn = sql2o.open()) {
            // Populate non-list attributes of StaffMember object
            String sql = "SELECT * FROM StaffMembers " +
                    "WHERE jhed = :jhed";
            StaffMember staffMember = conn.createQuery(sql)
                    .addParameter("jhed", jhed)
                    .executeAndFetchFirst(StaffMember.class);

            if (staffMember == null) {
                return null;
            }

            // Get corresponding courses according to joining table
            List<Course> courses = readCourses(conn, staffMember.getId());
            staffMember.setCourses(courses);

            return staffMember;

        } catch (Sql2oException e) {
            throw new DaoException("Unable to read staff member", e);
        }
    }

    @Override
    public void update(StaffMember staffMember) throws DaoException {
        try(Connection conn = sql2o.open()) {
            int staffId = staffMember.getId();

            String sql = "UPDATE StaffMembers " +
                    "SET name = :name, jhed = :jhed, isAdmin = :isAdmin " +
                    "WHERE id = :id;";
            conn.createQuery(sql)
                    .bind(staffMember)
                    .executeUpdate();

            // Delete existing entries with this staff member in joining table
            sql = "DELETE FROM StaffMembers_Courses " +
                    "WHERE staffId = :staffId;";
            conn.createQuery(sql)
                    .addParameter("staffId", staffId)
                    .executeUpdate();

            // Fresh update to joining table
            List<Course> courses = staffMember.getCourses();
            if (courses != null) {
                for (Course course: courses) {
                    int courseId = course.getId();
                    if (course.getId() == 0) {
                        sql = "INSERT INTO Courses(name, courseNumber, semester, hiringComplete, courseDescription, " +
                                "interviewLink) VALUES(:name, :courseNumber, :semester, :hiringComplete, :courseDescription, " +
                                ":interviewLink);";
                        courseId = (int) conn.createQuery(sql, true)
                                .bind(course)
                                .executeUpdate()
                                .getKey();

                        course.setId(courseId);
                    }

                    sql = "INSERT INTO StaffMembers_Courses(staffId, courseId) " +
                            "VALUES(:staffId, :courseId);";
                    conn.createQuery(sql)
                            .addParameter("staffId", staffId)
                            .addParameter("courseId", courseId)
                            .executeUpdate();
                }
            }
        } catch (Sql2oException e) {
            throw new DaoException("Unable to update staff member", e);
        }
    }

    @Override
    public void delete(StaffMember staffMember) throws DaoException {
        try(Connection conn = sql2o.open()) {
            int staffId = staffMember.getId();

            // Delete from joining table
            String sql = "DELETE FROM StaffMembers_Courses " +
                    "WHERE staffId = :staffId;";
            conn.createQuery(sql)
                    .addParameter("staffId", staffId)
                    .executeUpdate();

            // Delete from StaffMembers
            sql = "DELETE FROM StaffMembers WHERE id = :id";
            conn.createQuery(sql)
                    .addParameter("id", staffId)
                    .executeUpdate();
        } catch(Sql2oException e) {
            throw new DaoException("Unable to delete staff member", e);
        }
    }

    /**
     * Obtain the courses that a StaffMember is responsible for.
     * @param conn SQL connection object to database
     * @param staffId id of StaffMember
     * @return list of courses that the StaffMember is responsible for
     */
    private List<Course> readCourses(Connection conn, int staffId) {
        String sql = "SELECT C.* " +
                "FROM StaffMembers_Courses " +
                "INNER JOIN Courses C " +
                "ON StaffMembers_Courses.courseId = C.id " +
                "WHERE StaffMembers_Courses.staffId = :staffId";

        List<Course> courses = conn.createQuery(sql)
                .addParameter("staffId", staffId)
                .executeAndFetch(Course.class);

        return courses;
    }
}