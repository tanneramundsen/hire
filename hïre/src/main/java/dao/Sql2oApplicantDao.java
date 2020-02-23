package dao;

//import exception.DaoException;
import model.Applicant;
import model.Course;
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
            for(Course course: applicant.getEligibleCourses()) {
                String sql = "INSERT INTO Applicants(name, email, jhed, courseId)" +
                        "VALUES(:name, :email, :jhed, :courseId);";
                int id = (int) conn.createQuery(sql)
                        .addParameter("name", applicant.getName())
                        .addParameter("email", applicant.getEmail())
                        .addParameter("jhed", applicant.getJhed())
                        .addParameter("courseId", course.getId())
                        .executeUpdate()
                        .getKey();
                course.setId(id);
            }
        } catch (Sql2oException ex) {
            throw new RuntimeException("Unable to add the Applicant", ex);
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
