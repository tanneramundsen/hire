package dao;

import model.Course;
import model.StaffMember;
import org.sql2o.Connection;
import org.sql2o.Sql2o;
import org.sql2o.Sql2oException;

import java.util.List;

public class Sql2oStaffMemberDao implements StaffMemberDao {

    private Sql2o sql2o;

    public Sql2oCourseDao(Sql2o sql2o) {
        this.sql2o = sql2o;
    }

    @Override
    public void add(StaffMember staffMember) {

        for(Course course: staffMember.getCourses()) {
            try(Connection conn = sql2o.open()) {
                String sql = "INSERT INTO StaffMembers(name, url) VALUES(:name, :url, :courseId);";
                int id = (int) conn.createQuery(sql)
                        .bind(staffMember)
                        .executeUpdate()
                        .getKey();
            }
        }
    }

    @Override
    public void update(StaffMember staffMember) {

    }

    @Override
    public void delete(StaffMember staffMember) {

    }

    @Override
    public List<Course> getCoursesByStaff(StaffMember staffMember) {
        return null;
    }
}
