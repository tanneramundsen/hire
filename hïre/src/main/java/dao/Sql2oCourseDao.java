package dao;
import model.Course;
import model.StaffMember;
import org.sql2o.Connection;
import org.sql2o.Sql2o;
import org.sql2o.Sql2oException;

import java.util.List;

public class Sql2oCourseDao implements CourseDao {

    private Sql2o sql2o;

    public void add(Course course) throws RuntimeException{
        try (Connection conn = sql2o.open()) {
            String sql = "INSERT INTO Courses(name, url) VALUES(:name, :url);";
            int id = (int) conn.createQuery(sql)
                    .bind(course)
                    .executeUpdate()
                    .getKey();
        } catch (Sql2oException ex) {
            throw new RuntimeException("Unable to add the course", ex);
        }
    }

    public void update(Course course) throws RuntimeException{

    }

    public void delete(Course course) throws RuntimeException{

    }

    public List<Course> findCoursesByStaff(StaffMember staff){
        try (Connection conn = sql2o.open()) {
            String sql = "SELECT * FROM Courses WHERE ;";
            return conn.createQuery(sql).executeAndFetch(Course.class);
        }
    }

    public List<Course> findAll(){
        try (Connection conn = sql2o.open()) {
            String sql = "SELECT * FROM Courses;";
            return conn.createQuery(sql).executeAndFetch(Course.class);
        }
    }
}
