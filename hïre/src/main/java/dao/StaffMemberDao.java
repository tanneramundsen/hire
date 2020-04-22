package dao;

import exception.DaoException;
import model.StaffMember;

public interface StaffMemberDao {

    /**
     * Insert a new StaffMember to the database.
     * @param staffMember StaffMember POJO object should have at least name, email,
     *                  and JHED
     * @throws DaoException Runtime exception due to failed SQL query
     */
    void add(StaffMember staffMember) throws DaoException;

    /**
     * Obtain and load StaffMember information that corresponds to a specified id
     * into an StaffMember POJO.
     * @param id integer corresponding to which Applicant to fetch from
     *           StaffMembers table
     * @return StaffMember corresponding to the specified id or null
     * @throws DaoException Runtime exception due to failed SQL query
     */
    StaffMember read(int id) throws DaoException;

    /**
     * Update the information for an StaffMember in the database.
     * @param staffMember POJO object with new information to update
     * @throws DaoException Runtime exception due to failed SQL query
     */
    void update(StaffMember staffMember) throws DaoException;

    /**
     * Delete an StaffMember from the database.
     * @param staffMember StaffMember with id corresponding to entry in SQL
     *                    database we want to delete
     * @throws DaoException Runtime exception due to failed SQL query
     */
    void delete(StaffMember staffMember) throws DaoException;
}
