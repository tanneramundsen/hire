package dao;

import model.Course;
import model.StaffMember;
import org.sql2o.Connection;
import org.sql2o.Sql2o;
import org.sql2o.Sql2oException;

import java.util.List;

public interface StaffMemberDao {

    void add(StaffMember staffMember);

    StaffMember read(int id);

    void update(StaffMember staffMember);

    void delete(StaffMember staffMember);
}
