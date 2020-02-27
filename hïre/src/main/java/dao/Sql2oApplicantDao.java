package dao;

import exception.DaoException;
import model.Applicant;
import model.Course;
import org.sql2o.Connection;
import org.sql2o.Sql2o;
import org.sql2o.Sql2oException;

import java.util.List;

public class Sql2oApplicantDao implements ApplicantDao {

    private Sql2o sql2o;

    public Sql2oApplicantDao(Sql2o sql2o) { this.sql2o = sql2o; }

    public void add(Applicant applicant) throws DaoException {

        //check for duplicates
        if (read(applicant.getId()) != null) {
            update(applicant);
        } else {
            try (Connection conn = sql2o.open()) {

                String sql;
                //no duplicates --> insert
                sql = "INSERT INTO Applicants(name, email, jhed)" +
                        "VALUES(:name, :email, :jhed);";
                int id = (int) conn.createQuery(sql)
                        .addParameter("name", applicant.getName())
                        .addParameter("email", applicant.getEmail())
                        .addParameter("jhed", applicant.getJhed())
                        .executeUpdate()
                        .getKey();
                applicant.setId(id);
                if (applicant.getEligibleCourses() != null) {
                    for (Course course : applicant.getEligibleCourses()) {
                        int courseId = course.getId();
                        if (courseId == 0) {
                            sql = "INSERT INTO Courses(name, courseNumber, semester, hiringComplete) " +
                                    "VALUES(:name, :courseNumber, :semester, :hiringComplete);";
                            int sId = (int) conn.createQuery(sql)
                                    .addParameter("name", course.getName())
                                    .addParameter("courseNumber", course.getCourseNumber())
                                    .addParameter("semester", course.getSemester())
                                    .addParameter("hiringComplete", course.isHiringComplete())
                                    .executeUpdate()
                                    .getKey();
                            course.setId(sId);
                        }
                        sql = "INSERT INTO QualifiedApplicants_Courses(applicantId, courseId) " +
                                "VALUES(:applicantId, :courseId);";
                        conn.createQuery(sql)
                                .addParameter("applicantId", applicant.getId())
                                .addParameter("courseId", course.getId())
                                .executeUpdate();
                    }
                }
                if (applicant.getHiredCourse() != null) {
                    Course course = applicant.getHiredCourse();
                    sql = "INSERT INTO HiredApplicants_Courses(applicantId, courseId) " +
                            "VALUES(:applicantId, :courseId);";
                    conn.createQuery(sql)
                            .addParameter("applicantId", applicant.getId())
                            .addParameter("courseId", course.getId())
                            .executeUpdate();
                }
            } catch (Sql2oException ex) {
                throw new DaoException("Unable to add the applicant", ex);
            }
        }
    }

    public void update(Applicant applicant) throws DaoException {
        try(Connection conn = sql2o.open()) {
            String sql = "UPDATE Applicants SET name = :name, email = :email, jhed = :jhed, " +
                    "courseId = :courseId WHERE id = :id;";
            conn.createQuery(sql)
                    .addParameter("name", applicant.getName())
                    .addParameter("email", applicant.getEmail())
                    .addParameter("jhed", applicant.getJhed())
                    .addParameter("id", applicant.getId())
                    .executeUpdate();

            // Delete existing entries with this applicant in joining tables
            sql = "DELETE FROM QualifiedApplicants_Courses WHERE applicantId = :applicantId;";
            conn.createQuery(sql)
                    .addParameter("applicantId", applicant.getId())
                    .executeUpdate();
            sql = "DELETE FROM HiredApplicants_Courses WHERE applicantId = :applicantId;";
            conn.createQuery(sql)
                    .addParameter("applicantId", applicant.getId())
                    .executeUpdate();

            // Fresh update to joining tables
            if (applicant.getEligibleCourses() != null) {
                for (Course course : applicant.getEligibleCourses()) {
                    int courseId = course.getId();
                    sql = "INSERT INTO QualifiedApplicants_Courses(applicantId, courseId) " +
                            "VALUES(:applicantId, :courseId);";
                    conn.createQuery(sql)
                            .addParameter("applicantId", applicant.getId())
                            .addParameter("courseId", courseId)
                            .executeUpdate();
                }
            }
            if (applicant.getHiredCourse() != null) {
                int hiredCourseId = applicant.getHiredCourse().getId();

                sql = "INSERT INTO HiredApplicants_Courses(applicantId, courseId) " +
                        "VALUES(:applicantId, :courseId);";
                conn.createQuery(sql)
                        .addParameter("applicantId", applicant.getId())
                        .addParameter("courseId", hiredCourseId)
                        .executeUpdate();
            }
        } catch (Sql2oException e) {
            throw new DaoException("Unable to update applicant", e);
        }
    }

    public void delete(Applicant applicant) throws DaoException {
        try(Connection conn = sql2o.open()) {
            int id = applicant.getId();
            String sql = "DELETE FROM QualifiedApplicants_Courses WHERE applicantId = :applicantId;";
            conn.createQuery(sql)
                    .addParameter("applicantId", id)
                    .executeUpdate();
            sql = "DELETE FROM HiredApplicants_Courses WHERE applicantId = :applicantId;";
            conn.createQuery(sql)
                    .addParameter("applicantId", id)
                    .executeUpdate();
            sql = "DELETE FROM Applicants WHERE id = :id;";
            conn.createQuery(sql)
                    .addParameter("id", id)
                    .executeUpdate();
        } catch(Sql2oException e) {
            throw new DaoException("Unable to delete applicant", e);
        }
    }

    public Applicant read(int id) throws DaoException {
        try (Connection conn = sql2o.open()) {
            String sql = "SELECT * FROM Applicants WHERE id = :id;";
            Applicant applicant;
            List<Applicant> applicants = conn.createQuery(sql)
                    .addParameter("id", id)
                    .executeAndFetch(Applicant.class);
            if (applicants.isEmpty()) {
                return null;
            } else {
                applicant = applicants.get(0);
            }
            //get corresponding eligibleCourses according to joining table
            sql = "SELECT Courses.* " +
                    "FROM QualifiedApplicants_Courses INNER JOIN Courses " +
                    "ON QualifiedApplicants_Courses.courseId = Courses.id " +
                    "WHERE QualifiedApplicants_Courses.applicantId = :applicantId;";
            List<Course> courses = conn.createQuery(sql)
                    .addParameter("applicantId", id)
                    .executeAndFetch(Course.class);
            applicant.setEligibleCourses(courses);

            sql = "SELECT Courses.* " +
                    "FROM HiredApplicants_Courses INNER JOIN Courses " +
                    "ON HiredApplicants_Courses.courseId = Courses.id " +
                    "WHERE HiredApplicants_Courses.applicantId = :applicantId;";
            List<Course> hiredCourses = (conn.createQuery(sql)
                    .addParameter("applicantId", id)
                    .executeAndFetch(Course.class));
            if (!hiredCourses.isEmpty()) {
                applicant.setHiredCourse(hiredCourses.get(0));
                //should only be one
            }
            return applicant;

        } catch (Sql2oException e) {
            throw new DaoException("Unable to read applicant", e);
        }
    }

    @Override
    public List<Applicant> findAll() throws DaoException {
        try (Connection conn = sql2o.open()) {
            String sql = "SELECT * FROM Applicants;";
            List<Applicant> applicants = conn.createQuery(sql)
                    .executeAndFetch(Applicant.class);
            for (Applicant applicant : applicants) {
                int applicantId = applicant.getId();
                sql = "SELECT Courses.* " +
                        "FROM QualifiedApplicants_Courses INNER JOIN Courses " +
                        "ON QualifiedApplicants_Courses.courseId = Courses.id " +
                        "WHERE QualifiedApplicants_Courses.applicantId = :id;";
                List<Course> courses = conn.createQuery(sql)
                        .addParameter("id", applicantId)
                        .executeAndFetch(Course.class);
                applicant.setEligibleCourses(courses);

                sql = "SELECT Courses.* " +
                        "FROM HiredApplicants_Courses INNER JOIN Courses " +
                        "ON HiredApplicants_Courses.courseId = Course.id " +
                        "WHERE HiredApplicants_Courses.applicantId = :applicantId;";
                List<Course> hiredCourses = (conn.createQuery(sql)
                        .addParameter("applicantId", applicantId)
                        .executeAndFetch(Course.class));
                if (!hiredCourses.isEmpty()) {
                    applicant.setHiredCourse(hiredCourses.get(0));
                    //should only be one
                }
            }
            return applicants;
        } catch(Sql2oException e) {
            throw new DaoException("Unable to find all applicants", e);
        }
    }

    @Override
    public List<Applicant> findByCourseId(int courseId) throws DaoException{
        try(Connection conn = sql2o.open()) {
            //TODO: confirm that this SQL is correct
            String sql = "SELECT Applicants.* " +
                    "FROM (" +
                        "(SELECT * " +
                        "QualifiedApplicants_Courses " +
                        "WHERE courseId = :courseId) AS C), Applicants " +
                    "WHERE C.applicantId = Applicants.id;";
            return conn.createQuery(sql)
                    .addParameter("courseId", courseId)
                    .executeAndFetch(Applicant.class);
        } catch(Sql2oException e) {
            throw new DaoException("Unable to find applicants by course id", e);
        }
    }


}
