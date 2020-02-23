package dao;

import model.Applicant;
//import exception.DaoException;
import java.util.List;

public interface ApplicantDao {

    void add(Applicant applicant) throws RuntimeException;

    List<Applicant> findAll();

    List<Applicant> findByCourseId(int courseId);
}
