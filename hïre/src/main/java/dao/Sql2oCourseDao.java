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
            String sql = "INSERT INTO Courses(name, courseNumber, instructors, semester, hiringComplete, cas, qualifiedStudents) " +
                    "VALUES(:name, :courseNumber, :instructors, :semester, :hiringComplete, :cas, :qualifiedStudents);";
            int id = (int) conn.createQuery(sql)
                    .bind(course)
                    .executeUpdate()
                    .getKey();
            course.setId(id);
        } catch (Sql2oException e) {
            throw new RuntimeException("Unable to add the course", e);
        }
    }

    public void update(Course course) throws RuntimeException{
        try(Connection conn = sql2o.open()) {
            String sql = "UPDATE Course SET name = :name, courseNumber = :courseNumber, instructors = :instructors, semester = :semester, " +
                    "hiringComplete = :hiringComplete, cas = :cas, qualifiedStudents = :qualifiedStudents";
            conn.createQuery(sql)
                    /**.addParameter("name", course.getName())
                    .addParameter("courseNumber", course.getCourseNumber())
                    .addParameter("instructors", course.getInstructors())
                    .addParameter("semester", course.getSemester())
                    .addParameter("hiringComplete", course.isHiringComplete())
                    .addParameter("cas", course.getCas())
                    .addParameter("qualifiedStudents", course.getQualifiedStudents())
                    .addParameter("id", course.getId())*/
                    .bind(course)
                    .executeUpdate();
        } catch (Sql2oException e) {
            throw new RuntimeException("Unable to update course", e);
        }
    }

    public void delete(Course course) throws RuntimeException{
        int id = course.getId();
        try(Connection conn = sql2o.open()) {
            String sql = "DELETE FROM Course where id = :id";
            conn.createQuery(sql).executeUpdate();
        } catch(Sql2oException e) {
            throw new RuntimeException("Unable to delete course", e);
        }
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
