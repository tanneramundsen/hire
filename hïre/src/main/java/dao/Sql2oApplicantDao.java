package dao;

//import exception.DaoException;
import model.Applicant;
import model.Course;
import model.StaffMember;
import org.sql2o.Connection;
import org.sql2o.Sql2o;
import org.sql2o.Sql2oException;

import java.util.List;

public class Sql2oApplicantDao implements ApplicantDao {

    private Sql2o sql2o;

    public Sql2oApplicantDao(Sql2o sql2o) { this.sql2o = sql2o; }

    public void add(Applicant applicant) throws RuntimeException {

        try (Connection conn = sql2o.open()) {

            //check for duplicates
            String sql = "SELECT id FROM Applicants WHERE id = :id AND name = :name;";
            List<Applicant> duplicates = conn.createQuery(sql)
                    .addParameter("id", applicant.getId())
                    .addParameter("name", applicant.getName())
                    .executeAndFetch(Applicant.class);
            if(duplicates.isEmpty()) {
                //no duplicates --> insert
                sql = "INSERT INTO Applicants(name, email, jhed)" +
                        "VALUES(:name, :email, :jhed);";
                int id = (int) conn.createQuery(sql)
                        .bind(applicant)
                        .executeUpdate()
                        .getKey();
                applicant.setId(id);
                for (Course course : applicant.getEligibleCourses()) {
                    sql = "INSERT INTO QualifiedApplicants_Courses(applicantId, courseId) " +
                            "VALUES(:applicantId, :courseId);";
                    conn.createQuery(sql)
                            .addParameter("applicantId", applicant.getId())
                            .addParameter("courseId", course.getId())
                            .executeUpdate()
                            .getKey();
                }
            } else {
                //yes duplicates --> update
                this.update(applicant);
            }

        } catch (Sql2oException ex) {
            throw new RuntimeException("Unable to add the applicant", ex);
        }
    }

    public void update(Applicant applicant) throws RuntimeException {
        try(Connection conn = sql2o.open()) {
            String sql = "UPDATE Applicants SET name = :name, email = :email, jhed = :jhed, " +
                    "courseId = :courseId WHERE id = :id;";
            conn.createQuery(sql)
                    .addParameter("name", applicant.getName())
                    .addParameter("email", applicant.getEmail())
                    .addParameter("jhed", applicant.getJhed())
                    .addParameter("id", applicant.getId())
                    .executeUpdate();

            // Delete existing entries with this applicant in joining table
            sql = "DELETE FROM QualifiedApplicants_Courses WHERE applicantId = :id;";
            conn.createQuery(sql)
                    .addParameter("id", applicant.getId())
                    .executeUpdate();

            // Fresh update to joining table
            for (Course course : applicant.getEligibleCourses()) {
                int courseId = course.getId();
                sql = "INSERT INTO QualifiedApplicants_Courses(applicantId, courseId) " +
                        "VALUES(:applicantId, :courseId);";
                conn.createQuery(sql)
                        .addParameter("applicantId", applicant.getId())
                        .addParameter("courseId", courseId)
                        .executeUpdate();
            }
        } catch (Sql2oException e) {
            throw new RuntimeException("Unable to update applicant", e);
        }
    }

    public void delete(Applicant applicant) throws RuntimeException {
        try(Connection conn = sql2o.open()) {
            int id = applicant.getId();
            String sql = "DELETE FROM Applicants WHERE id = :id;";
            conn.createQuery(sql)
                    .addParameter("id", id)
                    .executeUpdate();
            sql = "DELETE FROM QualifiedApplicants_Courses WHERE applicantId = :id;";
            conn.createQuery(sql)
                    .addParameter("id", id)
                    .executeUpdate();
        } catch(Sql2oException e) {
            throw new RuntimeException("Unable to delete applicant", e);
        }
    }

    public Applicant read(int id) {
        try (Connection conn = sql2o.open()) {
            String sql = "SELECT * FROM Applicants WHERE id = :id;";
            Applicant applicant = conn.createQuery(sql)
                    .addParameter("id", id)
                    .executeAndFetch(Applicant.class)
                    .get(0);
            //get corresponding courses according to joining table
            sql = "SELECT Courses.* " +
                    "FROM QualifiedApplicants_Courses INNER JOIN Courses " +
                    "ON QualifiedApplicants_Courses.courseId = Courses.id " +
                    "WHERE QualifiedApplicants_Courses.applicantId = :id;";
            List<Course> courses = conn.createQuery(sql)
                    .addParameter("id", id)
                    .executeAndFetch(Course.class);
            applicant.setEligibleCourses(courses);

            return applicant;

        } catch (Sql2oException e) {
            throw new RuntimeException("Unable to read applicant", e);
        }
    }

    @Override
    public List<Applicant> findAll() {
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
            }
            return applicants;
        } catch(Sql2oException e) {
            throw new RuntimeException("Unable to find all applicants", e);
        }
    }

    @Override
    public List<Applicant> findByCourseId(int courseId) {
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
        }
    }


}
