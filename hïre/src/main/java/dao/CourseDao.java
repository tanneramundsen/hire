package dao;

import model.Course;
import model.StaffMember;

import java.util.List;

public interface CourseDao {
    void add(Course course) throws RuntimeException;

    void update(Course course) throws RuntimeException;

    void delete(Course course) throws RuntimeException;

    List<Course> findAll();
}
