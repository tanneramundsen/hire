package dao;

import model.Applicant;
import model.Course;
//import exception.DaoException;
import java.util.List;

public interface ApplicantDao {

    void add(Applicant applicant) throws RuntimeException;

    void update(Applicant applicant) throws RuntimeException;

    void delete(Applicant applicant) throws RuntimeException;

    List<Applicant> findAll();

    List<Applicant> findByCourseId(int courseId);
}
