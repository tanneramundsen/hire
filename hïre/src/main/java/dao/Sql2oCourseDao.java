package dao;
import model.Course;
import model.StaffMember;
import org.sql2o.Connection;
import org.sql2o.Sql2o;
import org.sql2o.Sql2oException;

import java.util.List;

public class Sql2oCourseDao implements CourseDao {

    public void add(Course course) throws RuntimeException{

    }

    public void update(Course course) throws RuntimeException{

    }

    public void delete(Course course) throws RuntimeException{

    }

    public List<Course> findCoursesByStaff(StaffMember staff){
        return null;
    }

    public List<Course> findAll(){
        return null;
    }
}
