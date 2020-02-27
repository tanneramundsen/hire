package dao;

import exception.DaoException;
import model.StaffMember;

public interface StaffMemberDao {

    void add(StaffMember staffMember) throws DaoException;

    StaffMember read(int id) throws DaoException;

    void update(StaffMember staffMember) throws DaoException;

    void delete(StaffMember staffMember) throws DaoException;
}
