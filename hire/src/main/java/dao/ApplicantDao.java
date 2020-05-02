package dao;

import exception.DaoException;
import model.Applicant;
import java.util.List;

public interface ApplicantDao {

    /**
     * Insert a new applicant to the database.
     * @param applicant Applicant POJO object should have at least name, email,
     *                  and JHED
     * @throws DaoException Runtime exception due to failed SQL query
     */
    void add(Applicant applicant) throws DaoException;

    /**
     * Obtain and load Applicant information that corresponds to a specified id
     * into an Applicant POJO.
     * @param id integer corresponding to which Applicant to fetch from
     *           Applicants table
     * @return Applicant corresponding to the specified id or null
     * @throws DaoException Runtime exception due to failed SQL query
     */
    Applicant read(int id) throws DaoException;

    /**
     * Update the information for an Applicant in the database.
     * @param applicant POJO object with new information to update
     * @throws DaoException Runtime exception due to failed SQL query
     */
    void update(Applicant applicant) throws DaoException;

    /**
     * Delete an Applicant from the database.
     * @param applicant Applicant with id corresponding to entry in SQL
     *                  database we want to delete
     * @throws DaoException Runtime exception due to failed SQL query
     */
    void delete(Applicant applicant) throws DaoException;

    /**
     * Fetch all Applicants currently in the database.
     * @return list of all Applicants currently in the database
     * @throws DaoException Runtime exception due to failed SQL query
     */
    List<Applicant> findAll() throws DaoException;

    /**
     * Find all applicants that are interested in a particular course.
     * @param courseId the id for a course to search
     * @return list of applicants interested in specified course
     * @throws DaoException Runtime exception due to failed SQL query
     */
    List<Applicant> findByCourseId(int courseId) throws DaoException;
}
