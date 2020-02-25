package dao;

import model.Course;
import model.StaffMember;
import org.sql2o.Connection;
import org.sql2o.Sql2o;
import org.sql2o.Sql2oException;
import org.sql2o.data.Table;

import java.util.List;
import java.util.Map;

public class Sql2oStaffMemberDao implements StaffMemberDao {

    private Sql2o sql2o;

    public Sql2oStaffMemberDao(Sql2o sql2o) {
        this.sql2o = sql2o;
    }

    @Override
    public void add(StaffMember staffMember) {
        try(Connection conn = sql2o.open()) {
            String sql;
            int staffId = staffMember.getId();

            if (staffId == 0) {
                // Normal insert
                sql = "INSERT INTO StaffMembers(name, jhed) VALUES(:name, :url);";
                staffId = (int) conn.createQuery(sql)
                        .bind(staffMember)
                        .executeUpdate()
                        .getKey();
                staffMember.setId(staffId);
            }
            else {
                // Staff member already exists, just update
                this.update(staffMember);
                staffId = staffMember.getId();
            }

            // Add courses
            for (Course course: staffMember.getCourses()) {
                int courseId = course.getId();
                if (courseId == 0) {
                    sql = "INSERT INTO Courses(name, courseNumber, semester, hiringComplete) " +
                            "VALUES(:name, :courseNumber, :semester, :hiringComplete);";
                    courseId = (int) conn.createQuery(sql)
                            .bind(course)
                            .executeUpdate()
                            .getKey();

                    course.setId(courseId);
                }

                // Insert into joining table
                sql = "INSERT INTO StaffMembers_Courses(staffId, courseId) VALUES(:staffId, :courseId);";
                conn.createQuery(sql)
                        .addParameter("staffId", staffId)
                        .addParameter("courseId", courseId)
                        .executeUpdate();
            }
        } catch (Sql2oException e) {
            throw new RuntimeException("Unable to add staff member", e);
        }
    }

    @Override
    public StaffMember read(int id) {
        try (Connection conn = sql2o.open()) {
            // Populate non-list attributes of StaffMember object
            String sql = "SELECT * FROM StaffMembers WHERE id = :id";
            StaffMember staffMember = conn.createQuery(sql)
                    .addParameter("id", id)
                    .executeAndFetch(StaffMember.class)
                    .get(0);

            // Get corresponding courses according to joining table
            sql = "SELECT Courses.* " +
                    "FROM StaffMember_Courses " +
                  "INNER JOIN Courses " +
                    "ON StaffMember_Courses.courseId = Courses.id " +
                  "WHERE StaffMember_Courses.staffId = :staffId";
            List<Course> courses = conn.createQuery(sql)
                    .addParameter("staffId", id)
                    .executeAndFetch(Course.class);
            staffMember.setCourses(courses);

            return staffMember;

        } catch (Sql2oException e) {
            throw new RuntimeException("Unable to read staff member", e);
        }
    }

    @Override
    public void update(StaffMember staffMember) {
        try(Connection conn = sql2o.open()) {
            String sql = "UPDATE StaffMembers SET name = :name, jhed = :jhed WHERE id = :id;";
            conn.createQuery(sql)
                    .addParameter("name", staffMember.getName())
                    .addParameter("jhed", staffMember.getJhed())
                    .addParameter("id", staffMember.getId())
                    .executeUpdate();

            // Delete existing entries with this staff member in joining table
            sql = "DELETE FROM StaffMembers_Courses WHERE staffId = :staffId;";
            conn.createQuery(sql)
                    .addParameter("staffId", staffMember.getId())
                    .executeUpdate();

            // Fresh update to joining table
            int staffId = staffMember.getId();
            for (Course course: staffMember.getCourses()) {
                int courseId = course.getId();
                sql = "INSERT INTO StaffMembers_Courses(staffId, courseId) VALUES(:staffId, :courseId);";
                conn.createQuery(sql)
                        .addParameter("staffId", staffId)
                        .addParameter("courseId", courseId)
                        .executeUpdate();
            }
        } catch (Sql2oException e) {
            throw new RuntimeException("Unable to update staff member", e);
        }
    }

    @Override
    public void delete(StaffMember staffMember) {
        try(Connection conn = sql2o.open()) {
            int staffId = staffMember.getId();

            // Delete from joining table
            String sql = "DELETE FROM StaffMembers_Courses WHERE staffId = :staffId;";
            conn.createQuery(sql)
                    .addParameter("staffId", staffId)
                    .executeUpdate();

            // Delete from StaffMembers
            sql = "DELETE FROM StaffMembers WHERE id = :id";
            conn.createQuery(sql)
                    .addParameter("id", staffId)
                    .executeUpdate();
        } catch(Sql2oException e) {
            throw new RuntimeException("Unable to delete staff member", e);
        }
    }
}
