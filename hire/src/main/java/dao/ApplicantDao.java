package dao;

import exception.DaoException;
import model.Applicant;
import java.util.List;

public interface ApplicantDao {

    void add(Applicant applicant) throws DaoException;

    void update(Applicant applicant) throws DaoException;

    void delete(Applicant applicant) throws DaoException;

    Applicant read(int id) throws DaoException;

    List<Applicant> findAll() throws DaoException;

    List<Applicant> findByCourseId(int courseId) throws DaoException;
}
