package dao;

import exception.DaoException;
import model.Applicant;
import org.sql2o.Connection;
import org.sql2o.Sql2o;
import org.sql2o.Sql2oException;

import java.util.List;

public class Sql2oApplicantDao implements ApplicantDao {

    private Sql2o sql2o;

    public Sql2oApplicantDao(Sql2o sql2o) { this.sql2o = sql2o; }

    @Override
    public List<Applicant> findAll() {
        return null;
    }

    @Override
    public void add(Applicant applicant) throws RuntimeException {

    }
}
