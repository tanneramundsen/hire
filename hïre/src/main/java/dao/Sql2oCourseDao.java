package dao;
import exception.DaoException;
import model.Course;
import model.Applicant;
import model.StaffMember;
import org.sql2o.Connection;
import org.sql2o.Sql2o;
import org.sql2o.Sql2oException;

import java.util.*;

public class Sql2oCourseDao implements CourseDao {

    private Sql2o sql2o;

    public Sql2oCourseDao(Sql2o sql2o) {
        this.sql2o = sql2o;
    }

    public void add(Course course) throws DaoException {
        if (read(course.getId()) != null) {
            this.update(course);
        }
        try (Connection conn = sql2o.open()) {
            String sql = "INSERT INTO Courses(name, courseNumber, semester, hiringComplete) " +
                    "VALUES(:name, :courseNumber, :semester, :hiringComplete);";
            int id = (int) conn.createQuery(sql)
                    //.bind(course)
                    .addParameter("name", course.getName())
                    .addParameter("courseNumber", course.getCourseNumber())
                    .addParameter("semester", course.getSemester())
                    .addParameter("hiringComplete", course.isHiringComplete())
                    .executeUpdate()
                    .getKey();
            course.setId(id);

            for (StaffMember staffMember: course.getInstructors()) {
                int staffId = staffMember.getId();
                int courseId = course.getId();
                sql = "INSERT INTO StaffMembers_Courses(staffId, courseId) Values(:staffId, :courseId)";
                conn.createQuery(sql)
                        .addParameter("staffId", staffId)
                        .addParameter("courseId", courseId)
                        .executeUpdate();
            }

            for (Applicant hired: course.getHiredApplicants()) {
                int applicantId = hired.getId();
                int courseId = course.getId();
                sql = "INSERT INTO HiredApplicants_Courses(applicantId, courseId) Values(:applicantId, :courseId)";
                conn.createQuery(sql)
                        .addParameter("applicantId", applicantId)
                        .addParameter("courseId", courseId)
                        .executeUpdate();
            }

            for (Applicant qualified: course.getQualifiedApplicants()) {
                int applicantId = qualified.getId();
                int courseId = course.getId();
                sql = "INSERT INTO QualifiedApplicants_Courses(applicantId, courseId) Values(:applicantId, :courseId)";
                conn.createQuery(sql)
                        .addParameter("applicantId", applicantId)
                        .addParameter("courseId", courseId)
                        .executeUpdate();
            }

        } catch (Sql2oException e) {
            throw new DaoException("Unable to add the course", e);
        }
    }

    public Course read(int id) throws DaoException {
        try (Connection conn = sql2o.open()) {
            String sql = "SELECT * FROM Courses WHERE id = :id";
            Course c = conn.createQuery(sql).executeAndFetch(Course.class).get(0);

            //get corresponding staff members
            sql = "SELECT StaffMembers.* " +
                    "FROM StaffMembers_Courses " +
                    "INNER JOIN StaffMembers " +
                    "ON StaffMembers_Courses.staffId = StaffMembers.id" +
                    "WHERE StaffMembers_Courses.courseId = :courseId";
            List<StaffMember> staff = conn.createQuery(sql)
                    .addParameter("courseId", id)
                    .executeAndFetch(StaffMember.class);
            c.setInstructors(staff);

            //get corresponding hired applicants
            sql = "SELECT Applicants.* " +
                    "FROM HiredApplicants_Courses " +
                    "INNER JOIN Applicants " +
                    "ON HiredApplicants_Courses.applicantId = Applicants.id" +
                    "WHERE HiredApplicants_Courses.courseId = :courseId";
            List<Applicant> hired_apps = conn.createQuery(sql)
                    .addParameter("courseId", id)
                    .executeAndFetch(Applicant.class);
            c.setHiredApplicants(hired_apps);

            //get corresponding qualified applicants
            sql = "SELECT Applicants.* " +
                    "FROM QualifiedApplicants_Courses " +
                    "INNER JOIN Applicants " +
                    "ON QualifiedApplicants_Courses.applicantId = Applicants.id" +
                    "WHERE QualifiedApplicants_Courses.courseId = :courseId";
            List<Applicant> qualified_apps = conn.createQuery(sql)
                    .addParameter("courseId", id)
                    .executeAndFetch(Applicant.class);
            c.setQualifiedApplicants(qualified_apps);

            return c;

        } catch (Sql2oException e) {
            throw new DaoException("Unable to read course", e);
        }
    }

    public void update(Course course) throws DaoException {
        try(Connection conn = sql2o.open()) {
            String sql = "UPDATE Courses SET name = :name, courseNumber = :courseNumber, semester = :semester, " +
                    "hiringComplete = :hiringComplete";
            conn.createQuery(sql)
                    .addParameter("name", course.getName())
                    .addParameter("courseNumber", course.getCourseNumber())
                    .addParameter("semester", course.getSemester())
                    .addParameter("hiringComplete", course.isHiringComplete())
                    .executeUpdate();

            for (StaffMember staffMember: course.getInstructors()) {
                int staffId = staffMember.getId();
                int courseId = course.getId();
                sql = "UPDATE StaffMembers_Courses SET staffId = :staffId, courseId = :courseId";
                conn.createQuery(sql)
                        .addParameter("staffId", staffId)
                        .addParameter("courseId", courseId)
                        .executeUpdate();
            }

            for (Applicant hired: course.getHiredApplicants()) {
                int applicantId = hired.getId();
                int courseId = course.getId();
                sql = "UPDATE HiredApplicants_Courses SET applicantId = :applicantId, courseId = :courseId";
                conn.createQuery(sql)
                        .addParameter("applicantId", applicantId)
                        .addParameter("courseId", courseId)
                        .executeUpdate();
            }

            for (Applicant qualified: course.getQualifiedApplicants()) {
                int applicantId = qualified.getId();
                int courseId = course.getId();
                sql = "UPDATE QualifiedApplicants_Courses SET applicantId = :applicantId, courseId = :courseId";
                conn.createQuery(sql)
                        .addParameter("applicantId", applicantId)
                        .addParameter("courseId", courseId)
                        .executeUpdate();
            }

        } catch (Sql2oException e) {
            throw new DaoException("Unable to update course", e);
        }
    }

    public void delete(Course course) throws DaoException{
        int id = course.getId();
        try(Connection conn = sql2o.open()) {
            String sql = "DELETE FROM Courses where id = :id";
            conn.createQuery(sql)
                    .addParameter("id", id)
                    .executeUpdate();

            sql = "DELETE FROM StaffMembers_Courses WHERE courseId = :id";
            conn.createQuery(sql)
                    .addParameter("id", id)
                    .executeUpdate();

            sql = "DELETE FROM HiredApplicants_Courses WHERE courseId = :id";
            conn.createQuery(sql)
                    .addParameter("id", id)
                    .executeUpdate();

            sql = "DELETE FROM QualifiedApplicants_Courses WHERE courseId = :id";
            conn.createQuery(sql)
                    .addParameter("id", id)
                    .executeUpdate();
        } catch(Sql2oException e) {
            throw new DaoException("Unable to delete course", e);
        }
    }

    public List<Course> findAll() throws DaoException {
        try (Connection conn = sql2o.open()) {
            String sql = "SELECT * FROM Courses;";
            List<Course> courses = conn.createQuery(sql).executeAndFetch(Course.class);
            for (int i = 0; i < courses.size(); i++) {
                Course c = courses.get(i);
                //get corresponding staff members
                sql = "SELECT StaffMembers.* " +
                        "FROM StaffMembers_Courses " +
                        "INNER JOIN StaffMembers " +
                        "ON StaffMembers_Courses.staffId = StaffMembers.id" +
                        "WHERE StaffMembers_Courses.courseId = :courseId";
                List<StaffMember> staff = conn.createQuery(sql)
                        .addParameter("courseId", c.getId())
                        .executeAndFetch(StaffMember.class);
                c.setInstructors(staff);

                //get corresponding hired applicants
                sql = "SELECT Applicants.* " +
                        "FROM HiredApplicants_Courses " +
                        "INNER JOIN Applicants " +
                        "ON HiredApplicants_Courses.applicantId = Applicants.id" +
                        "WHERE HiredApplicants_Courses.courseId = :courseId";
                List<Applicant> hired_apps = conn.createQuery(sql)
                        .addParameter("courseId", c.getId())
                        .executeAndFetch(Applicant.class);
                c.setHiredApplicants(hired_apps);

                //get corresponding qualified applicants
                sql = "SELECT Applicants.* " +
                        "FROM QualifiedApplicants_Courses " +
                        "INNER JOIN Applicants " +
                        "ON QualifiedApplicants_Courses.applicantId = Applicants.id" +
                        "WHERE QualifiedApplicants_Courses.courseId = :courseId";
                List<Applicant> qualified_apps = conn.createQuery(sql)
                        .addParameter("courseId", c.getId())
                        .executeAndFetch(Applicant.class);
                c.setQualifiedApplicants(qualified_apps);
            }

            return courses;
        } catch(Sql2oException e) {
            throw new DaoException("unable to find all courses", e);
        }
    }
}
