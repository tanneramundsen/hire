package dao;

import exception.DaoException;
import model.Applicant;
import model.Course;
import model.StaffMember;
import org.sql2o.Connection;
import org.sql2o.Sql2o;
import org.sql2o.Sql2oException;

import java.util.List;

public class Sql2oCourseDao implements CourseDao {

    private Sql2o sql2o;

    public Sql2oCourseDao(Sql2o sql2o) {
        this.sql2o = sql2o;
    }

    public void add(Course course) throws DaoException {
        if (course.getId() != 0) {
            update(course);
        } else {

            try (Connection conn = sql2o.open()) {
                String sql = "INSERT INTO Courses(name, courseNumber, semester, hiringComplete, courseDescription, " +
                        "interviewLink) VALUES(:name, :courseNumber, :semester, :hiringComplete, :courseDescription, " +
                        ":interviewLink);";
                int id = (int) conn.createQuery(sql)
                        .bind(course)
                        .executeUpdate()
                        .getKey();
                course.setId(id);

                if (course.getInstructors() != null) {
                    for (StaffMember staffMember : course.getInstructors()) {
                        int staffId = staffMember.getId();
                        if (staffId == 0) {
                            sql = "INSERT INTO StaffMembers(name, jhed) " +
                                    "VALUES(:name, :jhed);";
                            staffId = (int) conn.createQuery(sql)
                                    .bind(staffMember)
                                    .executeUpdate()
                                    .getKey();
                            staffMember.setId(staffId);
                        }
                        int courseId = course.getId();
                        sql = "INSERT INTO StaffMembers_Courses(staffId, courseId) " +
                                "VALUES(:staffId, :courseId);";
                        conn.createQuery(sql)
                                .addParameter("staffId", staffId)
                                .addParameter("courseId", courseId)
                                .executeUpdate();
                    }
                }

                if (course.getInterestedApplicants() != null) {
                    for (Applicant applicant : course.getInterestedApplicants()) {
                        int applicantId = applicant.getId();
                        if (applicantId == 0) {
                            sql = "INSERT INTO Applicants(name, email, jhed, year, majorAndMinor, gpa, " +
                                    "registeredCredits, referenceEmail, resumeLink, fws, studentStatus, " +
                                    "mostRecentPayroll, otherJobs, hoursAvailable) " +
                                    "VALUES(:name, :email, :jhed, :year, :majorAndMinor, :gpa, " +
                                    ":registeredCredits, :referenceEmail, :resumeLink, :fws, :studentStatus, " +
                                    ":mostRecentPayroll, :otherJobs, :hoursAvailable);";
                            applicantId = (int) conn.createQuery(sql)
                                    .bind(applicant)
                                    .executeUpdate()
                                    .getKey();
                            applicant.setId(applicantId);
                        }
                        sql = "INSERT INTO Applicants_Courses(applicantId, courseId, interested, grade) " +
                                "VALUES(:applicantId, :courseId, 1, :grade);";
                        conn.createQuery(sql)
                                .addParameter("applicantId", applicant.getId())
                                .addParameter("courseId", course.getId())
                                .addParameter("grade", applicant.getInterestedCourses().get(course))
                                .executeUpdate();
                    }
                }

                if (course.getShortlistedApplicants() != null) {
                    for (Applicant applicant : course.getShortlistedApplicants()) {
                        int applicantId = applicant.getId();
                        if (applicantId == 0) {
                            sql = "INSERT INTO Applicants(name, email, jhed, year, majorAndMinor, gpa, " +
                                    "registeredCredits, referenceEmail, resumeLink, fws, studentStatus, " +
                                    "mostRecentPayroll, otherJobs, hoursAvailable) " +
                                    "VALUES(:name, :email, :jhed, :year, :majorAndMinor, :gpa, " +
                                    ":registeredCredits, :referenceEmail, :resumeLink, :fws, :studentStatus, " +
                                    ":mostRecentPayroll, :otherJobs, :hoursAvailable);";
                            applicantId = (int) conn.createQuery(sql)
                                    .bind(applicant)
                                    .executeUpdate()
                                    .getKey();
                            applicant.setId(applicantId);

                            sql = "INSERT INTO Applicants_Courses(applicantId, courseId, shortlisted) " +
                                    "VALUES applicantId = :applicantId, courseId = :courseId, shortlisted = 1;";
                            conn.createQuery(sql)
                                    .addParameter("applicantId", applicant.getId())
                                    .addParameter("courseId", course.getId())
                                    .executeUpdate();
                        } else {
                            sql = "UPDATE Applicants_Courses " +
                                    "SET shortlisted = 1 " +
                                    "WHERE applicantId = :applicantId AND courseId = :courseId;";
                            conn.createQuery(sql)
                                    .addParameter("applicantId", applicant.getId())
                                    .addParameter("courseId", course.getId())
                                    .executeUpdate();
                        }
                    }
                }

                if (course.getHiredApplicants() != null) {
                    for (Applicant applicant : course.getHiredApplicants()) {
                        int applicantId = applicant.getId();
                        if (applicantId == 0) {
                            sql = "INSERT INTO Applicants(name, email, jhed, year, majorAndMinor, gpa, " +
                                    "registeredCredits, referenceEmail, resumeLink, fws, studentStatus, " +
                                    "mostRecentPayroll, otherJobs, hoursAvailable) " +
                                    "VALUES(:name, :email, :jhed, :year, :majorAndMinor, :gpa, " +
                                    ":registeredCredits, :referenceEmail, :resumeLink, :fws, :studentStatus, " +
                                    ":mostRecentPayroll, :otherJobs, :hoursAvailable);";
                            applicantId = (int) conn.createQuery(sql)
                                    .bind(applicant)
                                    .executeUpdate()
                                    .getKey();
                            applicant.setId(applicantId);
                            applicant.setHiredCourse(course);

                            sql = "INSERT INTO Applicants_Courses(applicantId, courseId, hired) " +
                                    "VALUES(:applicantId, :courseId, 1);";
                            conn.createQuery(sql)
                                    .addParameter("applicantId", applicant.getId())
                                    .addParameter("courseId", course.getId())
                                    .executeUpdate();
                        } else {
                            sql = "UPDATE Applicants_Courses " +
                                    "SET hired = 1 " +
                                    "WHERE applicantId = :applicantId " +
                                    "AND courseId = :courseId;";
                            conn.createQuery(sql)
                                    .addParameter("applicantId", applicant.getId())
                                    .addParameter("courseId", course.getId())
                                    .executeUpdate();
                        }
                    }
                }

            } catch (Sql2oException e) {
                throw new DaoException("Unable to add the course", e);
            }
        }
    }

    public Course read(int id) throws DaoException {
        try (Connection conn = sql2o.open()) {
            String sql = "SELECT * FROM Courses WHERE id = :id";
            Course course = conn.createQuery(sql)
                    .addParameter("id", id)
                    .executeAndFetchFirst(Course.class);

            if (course == null) {
                return null;
            }

            // Get corresponding staff members
            sql = "SELECT StaffMembers.* " +
                    "FROM StaffMembers_Courses " +
                    "INNER JOIN StaffMembers " +
                    "ON StaffMembers_Courses.staffId = StaffMembers.id " +
                    "WHERE StaffMembers_Courses.courseId = :courseId";
            List<StaffMember> staff = conn.createQuery(sql)
                    .addParameter("courseId", id)
                    .executeAndFetch(StaffMember.class);
            course.setInstructors(staff);

            // Get corresponding hired applicants
            sql = "SELECT A.id, A.name, A.email, A.jhed, A.year, A.majorAndMinor, A.gpa, " +
                    "A.registeredCredits, A.referenceEmail, A.resumeLink, A.fws," +
                    "A.studentStatus, A.mostRecentPayroll, A.otherJobs, A.hoursAvailable " +
                    "FROM Applicants_Courses " +
                    "INNER JOIN Applicants A " +
                    "ON Applicants_Courses.applicantId = A.id " +
                    "WHERE Applicants_Courses.courseId = :courseId " +
                    "AND Applicants_Courses.hired = 1;";
            List<Applicant> hiredApps = conn.createQuery(sql)
                    .addParameter("courseId", id)
                    .executeAndFetch(Applicant.class);
            course.setHiredApplicants(hiredApps);

            // Get corresponding interested applicants
            sql = "SELECT A.id, A.name, A.email, A.jhed, A.year, A.majorAndMinor, A.gpa, " +
                    "A.registeredCredits, A.referenceEmail, A.resumeLink, A.fws," +
                    "A.studentStatus, A.mostRecentPayroll, A.otherJobs, A.hoursAvailable " +
                    "FROM Applicants_Courses " +
                    "INNER JOIN Applicants A " +
                    "ON Applicants_Courses.applicantId = A.id " +
                    "WHERE Applicants_Courses.courseId = :courseId " +
                    "AND Applicants_Courses.interested = 1;";
            List<Applicant> interestedApps = conn.createQuery(sql)
                    .addParameter("courseId", id)
                    .executeAndFetch(Applicant.class);
            course.setInterestedApplicants(interestedApps);

            // Get corresponding shortlisted applicants
            sql = "SELECT A.id, A.name, A.email, A.jhed, A.year, A.majorAndMinor, A.gpa, " +
                    "A.registeredCredits, A.referenceEmail, A.resumeLink, A.fws," +
                    "A.studentStatus, A.mostRecentPayroll, A.otherJobs, A.hoursAvailable " +
                    "FROM Applicants_Courses " +
                    "INNER JOIN Applicants A " +
                    "ON Applicants_Courses.applicantId = A.id " +
                    "WHERE Applicants_Courses.courseId = :courseId " +
                    "AND Applicants_Courses.shortlisted = 1;";
            List<Applicant> shortlistedApps = conn.createQuery(sql)
                    .addParameter("courseId", id)
                    .executeAndFetch(Applicant.class);
            course.setShortlistedApplicants(shortlistedApps);

            return course;

        } catch (Sql2oException e) {
            throw new DaoException("Unable to read course", e);
        }
    }

    public Course read(String name) throws DaoException {
        try (Connection conn = sql2o.open()) {
            String sql = "SELECT * FROM Courses WHERE name = :name";
            Course course = conn.createQuery(sql)
                    .addParameter("name", name)
                    .executeAndFetchFirst(Course.class);

            if (course == null) {
                return null;
            }

            //get corresponding staff members
            sql = "SELECT StaffMembers.* " +
                    "FROM StaffMembers_Courses " +
                    "INNER JOIN StaffMembers " +
                    "ON StaffMembers_Courses.staffId = StaffMembers.id " +
                    "WHERE StaffMembers_Courses.courseId = :courseId";
            List<StaffMember> staff = conn.createQuery(sql)
                    .addParameter("courseId", course.getId())
                    .executeAndFetch(StaffMember.class);
            course.setInstructors(staff);

            //get corresponding hired applicants
            sql = "SELECT A.id, A.name, A.email, A.jhed, A.year, A.majorAndMinor, A.gpa, " +
                    "A.registeredCredits, A.referenceEmail, A.resumeLink, A.fws," +
                    "A.studentStatus, A.mostRecentPayroll, A.otherJobs, A.hoursAvailable " +
                    "FROM Applicants_Courses " +
                    "INNER JOIN Applicants A " +
                    "ON Applicants_Courses.applicantId = A.id " +
                    "WHERE Applicants_Courses.courseId = :courseId " +
                    "AND Applicants_Courses.hired = 1;";
            List<Applicant> hiredApps = conn.createQuery(sql)
                    .addParameter("courseId", course.getId())
                    .executeAndFetch(Applicant.class);
            course.setHiredApplicants(hiredApps);

            //get corresponding interested applicants
            sql = "SELECT A.id, A.name, A.email, A.jhed, A.year, A.majorAndMinor, A.gpa, " +
                    "A.registeredCredits, A.referenceEmail, A.resumeLink, A.fws," +
                    "A.studentStatus, A.mostRecentPayroll, A.otherJobs, A.hoursAvailable " +
                    "FROM Applicants_Courses " +
                    "INNER JOIN Applicants A " +
                    "ON Applicants_Courses.applicantId = A.id " +
                    "WHERE Applicants_Courses.courseId = :courseId " +
                    "AND Applicants_Courses.interested = 1;";
            List<Applicant> interestedApps = conn.createQuery(sql)
                    .addParameter("courseId", course.getId())
                    .executeAndFetch(Applicant.class);
            course.setInterestedApplicants(interestedApps);

            //get corresponding shortlisted applicants
            sql = "SELECT A.id, A.name, A.email, A.jhed, A.year, A.majorAndMinor, A.gpa, " +
                    "A.registeredCredits, A.referenceEmail, A.resumeLink, A.fws," +
                    "A.studentStatus, A.mostRecentPayroll, A.otherJobs, A.hoursAvailable " +
                    "FROM Applicants_Courses " +
                    "INNER JOIN Applicants A " +
                    "ON Applicants_Courses.applicantId = A.id " +
                    "WHERE Applicants_Courses.courseId = :courseId " +
                    "AND Applicants_Courses.shortlisted = 1;";
            List<Applicant> shortlistedApps = conn.createQuery(sql)
                    .addParameter("courseId", course.getId())
                    .executeAndFetch(Applicant.class);
            course.setShortlistedApplicants(shortlistedApps);

            return course;

        } catch (Sql2oException e) {
            throw new DaoException("Unable to read course", e);
        }
    }

    public void update(Course course) throws DaoException {
        try (Connection conn = sql2o.open()) {
            String sql = "UPDATE Courses SET name = :name, courseNumber = :courseNumber, " +
                    "semester = :semester, hiringComplete = :hiringComplete, courseDescription = :courseDescription, " +
                    "interviewLink = :interviewLink WHERE id = :id";
            conn.createQuery(sql)
                    .bind(course)
                    .executeUpdate();

            // Delete existing entries with this course in joining tables
            sql = "DELETE FROM Applicants_Courses WHERE courseId = :courseId;";
            conn.createQuery(sql)
                    .addParameter("courseId", course.getId())
                    .executeUpdate();

            sql = "DELETE FROM StaffMembers_Courses WHERE courseId = :courseId;";
            conn.createQuery(sql)
                    .addParameter("courseId", course.getId())
                    .executeUpdate();

            if (course.getInstructors() != null) {
                for (StaffMember staffMember : course.getInstructors()) {
                    int staffId = staffMember.getId();
                    if (staffId == 0) {
                        sql = "INSERT INTO StaffMembers(name, jhed) VALUES(:name, :jhed);";
                        staffId = (int) conn.createQuery(sql)
                                .bind(staffMember)
                                .executeUpdate()
                                .getKey();
                        staffMember.setId(staffId);
                    }

                    int courseId = course.getId();
                    sql = "INSERT INTO StaffMembers_Courses(staffId, courseId) " +
                            "VALUES(:staffId, :courseId);";
                    conn.createQuery(sql)
                            .addParameter("staffId", staffId)
                            .addParameter("courseId", courseId)
                            .executeUpdate();
                }
            }

            if (course.getInterestedApplicants() != null) {
                for (Applicant applicant : course.getInterestedApplicants()) {
                    int applicantId = applicant.getId();
                    if (applicantId == 0) {
                        sql = "INSERT INTO Applicants(name, email, jhed, year, majorAndMinor, gpa, " +
                                "registeredCredits, referenceEmail, resumeLink, fws, studentStatus, " +
                                "mostRecentPayroll, otherJobs, hoursAvailable) " +
                                "VALUES(:name, :email, :jhed, :year, :majorAndMinor, :gpa, " +
                                ":registeredCredits, :referenceEmail, :resumeLink, :fws, :studentStatus, " +
                                ":mostRecentPayroll, :otherJobs, :hoursAvailable);";
                        applicantId = (int) conn.createQuery(sql)
                                .bind(applicant)
                                .executeUpdate()
                                .getKey();
                        applicant.setId(applicantId);
                    }
                    sql = "INSERT INTO Applicants_Courses(applicantId, courseId, interested, grade) " +
                            "VALUES(:applicantId, :courseId, 1, :grade);";
                    conn.createQuery(sql)
                            .addParameter("applicantId", applicantId)
                            .addParameter("courseId", course.getId())
                            .addParameter("grade", "Not Taken")
                            .executeUpdate();
                }
            }

            if (course.getShortlistedApplicants() != null) {
                for (Applicant applicant : course.getShortlistedApplicants()) {
                    int applicantId = applicant.getId();
                    if (applicantId == 0) {
                        sql = "INSERT INTO Applicants(name, email, jhed, year, majorAndMinor, gpa, " +
                                "registeredCredits, referenceEmail, resumeLink, fws, studentStatus, " +
                                "mostRecentPayroll, otherJobs, hoursAvailable) " +
                                "VALUES(:name, :email, :jhed, :year, :majorAndMinor, :gpa, " +
                                ":registeredCredits, :referenceEmail, :resumeLink, :fws, :studentStatus, " +
                                ":mostRecentPayroll, :otherJobs, :hoursAvailable);";
                        applicantId = (int) conn.createQuery(sql)
                                .bind(applicant)
                                .executeUpdate()
                                .getKey();
                        applicant.setId(applicantId);

                        sql = "INSERT INTO Applicants_Courses(applicantId, courseId, shortlisted) " +
                                "VALUES(:applicantId, :courseId, 1);";
                        conn.createQuery(sql)
                                .addParameter("applicantId", applicantId)
                                .addParameter("courseId", course.getId())
                                .executeUpdate();
                    } else {
                        sql = "UPDATE Applicants_Courses " +
                                "SET shortlisted = 1 " +
                                "WHERE applicantId = :applicantId " +
                                "AND courseId = :courseId;";
                        conn.createQuery(sql)
                                .addParameter("applicantId", applicantId)
                                .addParameter("courseId", course.getId())
                                .executeUpdate();
                    }
                }
            }

            if (course.getHiredApplicants() != null) {
                for (Applicant applicant : course.getHiredApplicants()) {
                    int applicantId = applicant.getId();
                    if (applicantId == 0) {
                        sql = "INSERT INTO Applicants(name, email, jhed, year, majorAndMinor, gpa, " +
                                "registeredCredits, referenceEmail, resumeLink, fws, studentStatus, " +
                                "mostRecentPayroll, otherJobs, hoursAvailable) " +
                                "VALUES(:name, :email, :jhed, :year, :majorAndMinor, :gpa, " +
                                ":registeredCredits, :referenceEmail, :resumeLink, :fws, :studentStatus, " +
                                ":mostRecentPayroll, :otherJobs, :hoursAvailable);";
                        applicantId = (int) conn.createQuery(sql)
                                .bind(applicant)
                                .executeUpdate()
                                .getKey();
                        applicant.setId(applicantId);

                        sql = "INSERT INTO Applicants_Courses(applicantId, courseId, hired) " +
                                "VALUES(:applicantId, :courseId, 1);";
                        conn.createQuery(sql)
                                .addParameter("applicantId", applicantId)
                                .addParameter("courseId", course.getId())
                                .executeUpdate();
                    } else {
                        sql = "UPDATE Applicants_Courses " +
                                "SET hired = 1 " +
                                "WHERE applicantId = :applicantId " +
                                "AND courseId = :courseId;";
                        conn.createQuery(sql)
                                .addParameter("applicantId", applicantId)
                                .addParameter("courseId", course.getId());
                    }
                }
            }
        } catch (Sql2oException e) {
            throw new DaoException("Unable to update course", e);
        }
    }

    public void delete(Course course) throws DaoException {
        int id = course.getId();
        try (Connection conn = sql2o.open()) {
            String sql = "DELETE FROM StaffMembers_Courses WHERE courseId = :id";
            conn.createQuery(sql)
                    .addParameter("id", id)
                    .executeUpdate();

            sql = "DELETE FROM Applicants_Courses WHERE courseId = :id";
            conn.createQuery(sql)
                    .addParameter("id", id)
                    .executeUpdate();

            sql = "DELETE FROM Courses where id = :id";
            conn.createQuery(sql)
                    .addParameter("id", id)
                    .executeUpdate();
        } catch (Sql2oException e) {
            throw new DaoException("Unable to delete course", e);
        }
    }

    public List<Course> findAll() throws DaoException {
        try (Connection conn = sql2o.open()) {
            String sql = "SELECT * FROM Courses;";
            List<Course> courses = conn.createQuery(sql).executeAndFetch(Course.class);
            for (Course course: courses) {
                //get corresponding staff members
                sql = "SELECT StaffMembers.* " +
                        "FROM StaffMembers_Courses " +
                        "INNER JOIN StaffMembers " +
                        "ON StaffMembers_Courses.staffId = StaffMembers.id " +
                        "WHERE StaffMembers_Courses.courseId = :courseId";
                List<StaffMember> staff = conn.createQuery(sql)
                        .addParameter("courseId", course.getId())
                        .executeAndFetch(StaffMember.class);
                course.setInstructors(staff);

                //get corresponding hired applicants
                sql = "SELECT A.id, A.name, A.email, A.jhed, A.year, A.majorAndMinor, A.gpa, " +
                        "A.registeredCredits, A.referenceEmail, A.resumeLink, A.fws," +
                        "A.studentStatus, A.mostRecentPayroll, A.otherJobs, A.hoursAvailable " +
                        "FROM Applicants_Courses " +
                        "INNER JOIN Applicants A " +
                        "ON Applicants_Courses.applicantId = A.id " +
                        "WHERE Applicants_Courses.courseId = :courseId " +
                        "AND Applicants_Courses.hired = 1;";
                List<Applicant> hiredApps = conn.createQuery(sql)
                        .addParameter("courseId", course.getId())
                        .executeAndFetch(Applicant.class);
                course.setHiredApplicants(hiredApps);

                // get corresponding interested applicants
                sql = "SELECT A.id, A.name, A.email, A.jhed, A.year, A.majorAndMinor, A.gpa, " +
                        "A.registeredCredits, A.referenceEmail, A.resumeLink, A.fws," +
                        "A.studentStatus, A.mostRecentPayroll, A.otherJobs, A.hoursAvailable " +
                        "FROM Applicants_Courses " +
                        "INNER JOIN Applicants A " +
                        "ON Applicants_Courses.applicantId = A.id " +
                        "WHERE Applicants_Courses.courseId = :courseId " +
                        "AND Applicants_Courses.interested = 1;";
                List<Applicant> interestedApps = conn.createQuery(sql)
                        .addParameter("courseId", course.getId())
                        .executeAndFetch(Applicant.class);
                course.setInterestedApplicants(interestedApps);

                //get corresponding shortlisted applicants
                sql = "SELECT A.id, A.name, A.email, A.jhed, A.year, A.majorAndMinor, A.gpa, " +
                        "A.registeredCredits, A.referenceEmail, A.resumeLink, A.fws," +
                        "A.studentStatus, A.mostRecentPayroll, A.otherJobs, A.hoursAvailable " +
                        "FROM Applicants_Courses " +
                        "INNER JOIN Applicants A " +
                        "ON Applicants_Courses.applicantId = A.id " +
                        "WHERE Applicants_Courses.courseId = :courseId " +
                        "AND Applicants_Courses.shortlisted = 1;";
                List<Applicant> shortlistedApps = conn.createQuery(sql)
                        .addParameter("courseId", course.getId())
                        .executeAndFetch(Applicant.class);
                course.setShortlistedApplicants(shortlistedApps);
            }

            return courses;
        } catch (Sql2oException e) {
            throw new DaoException("unable to find all courses", e);
        }
    }
}