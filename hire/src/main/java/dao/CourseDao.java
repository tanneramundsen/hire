package dao;

import exception.DaoException;
import model.Course;

import java.util.List;

public interface CourseDao {
    void add(Course course) throws DaoException;

    Course read(int id) throws DaoException;

    void update(Course course) throws DaoException;

    void delete(Course course) throws DaoException;

    List<Course> findAll() throws DaoException;
}
