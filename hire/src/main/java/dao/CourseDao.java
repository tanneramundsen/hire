package dao;

import exception.DaoException;
import model.Course;

import java.util.List;

public interface CourseDao {
    /**
     * Insert a new course to the database.
     * @param course Course POJO object that should have a name, course number,
     *               and semester
     * @throws DaoException Runtime exception due to failed SQL query
     */
    void add(Course course) throws DaoException;

    /**
     * Obtain a Course POJO object based on a specified id.
     * @param id integer corresponding to which Course to fetch from
     *           Courses table
     * @return Course POJO corresponding to the specified id or null
     * @throws DaoException Runtime exception due to failed SQL query
     */
    Course read(int id) throws DaoException;

    /**
     * Update the information for a Course in the database.
     * @param course POJO object with new information to update
     * @throws DaoException Runtime exception due to failed SQL query
     */
    void update(Course course) throws DaoException;

    /**
     * Delete an Course from the database.
     * @param course Course with id corresponding to entry in SQL
     *               database we want to delete
     * @throws DaoException Runtime exception due to failed SQL query
     */
    void delete(Course course) throws DaoException;

    /**
     * Fetch all Courses currently in the database.
     * @return list of all Courses currently in the database
     * @throws DaoException Runtime exception due to failed SQL query
     */
    List<Course> findAll() throws DaoException;
}
