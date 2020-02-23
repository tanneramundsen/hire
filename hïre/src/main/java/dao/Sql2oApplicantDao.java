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

    @Override
    public void add(Applicant applicant) throws RuntimeException {
        try (Connection conn = sql2o.open()) {
            String sql = "INSERT INTO Applicants(name, email, jhed)" +
                    "VALUES(:name, :email, :jhed);";
            int id = (int) conn.createQuery(sql)
                    .bind(applicant)
                    .executeUpdate()
                    .getKey();
            applicant.setId(id);
        } catch (Sql2oException ex) {
            throw new RuntimeException("Unable to add the applicant", ex);
        }
    }

    public void update(Applicant applicant) throws RuntimeException {
        try(Connection conn = sql2o.open()) {
            String sql = "UPDATE Applicants SET name = :name, email = :email, jhed = :jhed, courseId = :courseId WHERE id = :id;";
            conn.createQuery(sql)
                    .addParameter("name", applicant.getName())
                    .addParameter("email", applicant.getEmail())
                    .addParameter("jhed", applicant.getJhed())
                    .addParameter("courseId", applicant.getEligibleCourses())
                    .addParameter("id", applicant.getId())
                    .executeUpdate();
        } catch (Sql2oException e) {
            throw new RuntimeException("Unable to update applicant", e);
        }
    }

    public void delete(Applicant applicant) throws RuntimeException {
        try(Connection conn = sql2o.open()) {
            String sql = "DELETE FROM Applicants where id = :id";
            conn.createQuery(sql)
                    .addParameter("id", applicant.getId())
                    .executeUpdate();
        } catch(Sql2oException e) {
            throw new RuntimeException("Unable to delete applicant", e);
        }
    }

    public Applicant read(int id) {
        try (Connection conn = sql2o.open()) {
            String sql = "SELECT * FROM Applicants WHERE id = :id";
            return conn.createQuery(sql).executeAndFetch(Applicant.class).get(0);
        }
    }

    @Override
    public List<Applicant> findAll() {
        String sql = "SELECT * FROM Applicants;";
        try (Connection conn = sql2o.open()) {
            return conn.createQuery(sql)
                    .executeAndFetch(Applicant.class);
        }
    }

    @Override
    public List<Applicant> findByCourseId(int courseId) {
        try(Connection conn = sql2o.open()) {
            String sql = "SELECT * FROM Applicants WHERE courseId = :courseId";
            return conn.createQuery(sql)
                    .addParameter("courseId", courseId)
                    .executeAndFetch(Applicant.class);
        }
    }


}
