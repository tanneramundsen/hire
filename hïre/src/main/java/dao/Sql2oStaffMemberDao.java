package dao;

import model.Course;
import model.StaffMember;
import org.sql2o.Connection;
import org.sql2o.Sql2o;
import org.sql2o.Sql2oException;

import java.util.List;

public class Sql2oStaffMemberDao implements StaffMemberDao {

    private Sql2o sql2o;

    public Sql2oStaffMemberDao(Sql2o sql2o) {
        this.sql2o = sql2o;
    }

    @Override
    public void create(StaffMember staffMember) {
        try(Connection conn = sql2o.open()) {
            for (Course course: staffMember.getCourses()) {
                String sql = "INSERT INTO StaffMembers(name, jhed, courseId) VALUES(:name, :url, :courseId);";
                int id = (int) conn.createQuery(sql)
                        .addParameter("name", staffMember.getName())
                        .addParameter("jhed", staffMember.getJhed())
                        .addParameter("courseId", course.getId());
                        .executeUpdate()
                        .getKey();

                staffMember.setId(id);
            }
        } catch (Sql2oException e) {
            throw new RuntimeException("Unable to add staff member", e);
        }
    }

    @Override
    public StaffMember read(int id) {
        try (Connection conn = sql2o.open()) {
            String sql = "SELECT * FROM Reviews WHERE id = :id";
            return conn.createQuery(sql).executeAndFetch(StaffMember.class).get(0);
        }
    }

    @Override
    public void update(StaffMember staffMember) {
        try(Connection conn = sql2o.open()) {
            String sql = "UPDATE StaffMembers SET name = :name, jhed = :jhed, courseId = :courseId WHERE id = :id;";
            conn.createQuery(sql)
                    .addParameter("name", staffMember.getName())
                    .addParameter("jhed", staffMember.getJhed())
                    .addParameter("courseId", staffMember.getCourses())
                    .addParameter("id", staffMember.getId())
                    .executeUpdate();
        } catch (Sql2oException e) {
            throw new RuntimeException("Unable to update staff member", e);
        }
    }

    @Override
    public void delete(StaffMember staffMember) {
        try(Connection conn = sql2o.open()) {
            String sql = "DELETE FROM StaffMembers where id = :id";
            conn.createQuery(sql)
                    .addParameter("id", staffMember.getId())
                    .executeUpdate();
        } catch(Sql2oException e) {
            throw new RuntimeException("Unable to delete staff member", e);
        }
    }

    @Override
    public List<Course> getCoursesByStaff(StaffMember staffMember) {
        return staffMember.getCourses();
    }
}
