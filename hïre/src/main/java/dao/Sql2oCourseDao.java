package dao;
import exception.DaoException;
import model.Course;
import model.Applicant;
import model.StaffMember;
import org.sql2o.Connection;
import org.sql2o.Sql2o;
import org.sql2o.Sql2oException;

import java.util.*;

public class Sql2oCourseDao implements CourseDao {

    private Sql2o sql2o;

    public Sql2oCourseDao(Sql2o sql2o) {
        this.sql2o = sql2o;
    }

    public void add(Course course) throws DaoException {
        if (course.getId() != 0) {
            update(course);
        }
        else {

            try (Connection conn = sql2o.open()) {
                String sql = "INSERT INTO Courses(name, courseNumber, semester, hiringComplete, courseDescription, " +
                        "interviewLink) VALUES(:name, :courseNumber, :semester, :hiringComplete, :courseDescription, " +
                        ":interviewLink);";
                int id = (int) conn.createQuery(sql)
                        //.bind(course)
                        .addParameter("name", course.getName())
                        .addParameter("courseNumber", course.getCourseNumber())
                        .addParameter("semester", course.getSemester())
                        .addParameter("hiringComplete", course.isHiringComplete())
                        .addParameter("courseDescription", course.getCourseDescription())
                        .addParameter("interviewLink", course.getInterviewLink())
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
                                    .addParameter("name", staffMember.getName())
                                    .addParameter("jhed", staffMember.getJhed())
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

                if (course.getHiredApplicants() != null) {
                    for (Applicant hired : course.getHiredApplicants()) {
                        int hiredId = hired.getId();
                        if (hiredId == 0) {
                            sql = "INSERT INTO Applicants(name, email, jhed) " +
                                    "VALUES(:name, :email, :jhed);";
                            hiredId = (int) conn.createQuery(sql)
                                    .addParameter("name", hired.getName())
                                    .addParameter("email", hired.getEmail())
                                    .addParameter("jhed", hired.getJhed())
                                    .executeUpdate()
                                    .getKey();
                            hired.setId(hiredId);
                            hired.setHiredCourse(course);
                        }

                        sql = "INSERT INTO HiredApplicants_Courses(applicantId, courseId) " +
                                "VALUES(:applicantId, :courseId);";
                        conn.createQuery(sql)
                                .addParameter("applicantId", hired.getId())
                                .addParameter("courseId", course.getId())
                                .executeUpdate();
                    }
                }

                if(course.getInterestedApplicants() != null) {
                    for (Applicant interested : course.getInterestedApplicants()) {
                        int interestedId = interested.getId();
                        if (interestedId == 0) {
                            sql = "INSERT INTO Applicants(name, email, jhed) " +
                                    "VALUES(:name, :email, :jhed);";
                            interestedId = (int) conn.createQuery(sql)
                                    .addParameter("name", interested.getName())
                                    .addParameter("email", interested.getEmail())
                                    .addParameter("jhed", interested.getJhed())
                                    .executeUpdate()
                                    .getKey();
                            interested.setId(interestedId);
                        }
                        int courseId = course.getId();
                        sql = "INSERT INTO InterestedApplicants_Courses(applicantId, courseId, grade) " +
                                "VALUES(:applicantId, :courseId, :grade);";
                        conn.createQuery(sql)
                                .addParameter("applicantId", interested.getId())
                                .addParameter("courseId", courseId)
                                .addParameter("grade", interested.getInterestedCourses().get(course))
                                .executeUpdate();
                    }
                }

                if (course.getShortlistedApplicants() != null) {
                    for (Applicant shortlisted : course.getShortlistedApplicants()) {
                        int shortlistedId = shortlisted.getId();
                        if (shortlistedId == 0) {
                            sql = "INSERT INTO Applicants(name, email, jhed) " +
                                    "VALUES(:name, :email, :jhed);";
                            shortlistedId = (int) conn.createQuery(sql)
                                    .addParameter("name", shortlisted.getName())
                                    .addParameter("email", shortlisted.getEmail())
                                    .addParameter("jhed", shortlisted.getJhed())
                                    .executeUpdate()
                                    .getKey();
                            shortlisted.setId(shortlistedId);
                        }
                        int courseId = course.getId();
                        sql = "INSERT INTO ShortlistedApplicants_Courses(applicantId, courseId) " +
                                "VALUES(:applicantId, :courseId);";
                        conn.createQuery(sql)
                                .addParameter("applicantId", shortlisted.getId())
                                .addParameter("courseId", courseId)
                                .executeUpdate();
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

            //get corresponding staff members
            sql = "SELECT StaffMembers.* " +
                    "FROM StaffMembers_Courses " +
                    "INNER JOIN StaffMembers " +
                    "ON StaffMembers_Courses.staffId = StaffMembers.id " +
                    "WHERE StaffMembers_Courses.courseId = :courseId";
            List<StaffMember> staff = conn.createQuery(sql)
                    .addParameter("courseId", id)
                    .executeAndFetch(StaffMember.class);
            course.setInstructors(staff);

            for (StaffMember sm: staff) {
                String jhed = sm.getJhed();

                // TODO: Figure out why executeAndFetch does not fill in name
                sql = "SELECT name FROM StaffMembers WHERE jhed = :jhed";
                List<Map<String, Object>> names = conn.createQuery(sql)
                        .addParameter("jhed", jhed)
                        .executeAndFetchTable()
                        .asList();
                sm.setName((String) names.get(0).get("name"));
            }

            //get corresponding hired applicants
            sql = "SELECT A.name, A.email, A.jhed " +
                    "FROM HiredApplicants_Courses " +
                    "INNER JOIN Applicants A " +
                    "ON HiredApplicants_Courses.applicantId = A.id " +
                    "WHERE HiredApplicants_Courses.courseId = :courseId";
            List<Applicant> hiredApps = conn.createQuery(sql)
                    .addParameter("courseId", id)
                    .executeAndFetch(Applicant.class);

            for (Applicant app: hiredApps) {
                String jhed = app.getJhed();

                // TODO: Figure out why executeAndFetch does not fill in name
                sql = "SELECT name FROM Applicants WHERE jhed = :jhed";
                List<Map<String, Object>> names = conn.createQuery(sql)
                        .addParameter("jhed", jhed)
                        .executeAndFetchTable()
                        .asList();
                app.setName((String) names.get(0).get("name"));
            }

            course.setHiredApplicants(hiredApps);

            //get corresponding interested applicants
            sql = "SELECT A.* " +
                    "FROM InterestedApplicants_Courses " +
                    "INNER JOIN Applicants A " +
                    "ON InterestedApplicants_Courses.applicantId = A.id " +
                    "WHERE InterestedApplicants_Courses.courseId = :courseId";
            List<Applicant> interestedApps = conn.createQuery(sql)
                    .addParameter("courseId", id)
                    .executeAndFetch(Applicant.class);

            for (Applicant app: interestedApps) {
                String jhed = app.getJhed();

                // TODO: Figure out why executeAndFetch does not fill in name
                sql = "SELECT name FROM Applicants WHERE jhed = :jhed";
                List<Map<String, Object>> names = conn.createQuery(sql)
                        .addParameter("jhed", jhed)
                        .executeAndFetchTable()
                        .asList();
                app.setName((String) names.get(0).get("name"));
            }

            course.setInterestedApplicants(interestedApps);

            //get corresponding shortlisted applicants
            sql = "SELECT A.name, A.email, A.jhed " +
                    "FROM ShortlistedApplicants_Courses " +
                    "INNER JOIN Applicants A " +
                    "ON ShortlistedApplicants_Courses.applicantId = A.id " +
                    "WHERE ShortlistedApplicants_Courses.courseId = :courseId";
            List<Applicant> shortlistedApps = conn.createQuery(sql)
                    .addParameter("courseId", id)
                    .executeAndFetch(Applicant.class);

            for (Applicant app: shortlistedApps) {
                String jhed = app.getJhed();

                // TODO: Figure out why executeAndFetch does not fill in name
                sql = "SELECT name FROM Applicants WHERE jhed = :jhed";
                List<Map<String, Object>> names = conn.createQuery(sql)
                        .addParameter("jhed", jhed)
                        .executeAndFetchTable()
                        .asList();
                app.setName((String) names.get(0).get("name"));
            }

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

            for (StaffMember sm: staff) {
                String jhed = sm.getJhed();

                // TODO: Figure out why executeAndFetch does not fill in name
                sql = "SELECT name FROM StaffMembers WHERE jhed = :jhed";
                List<Map<String, Object>> names = conn.createQuery(sql)
                        .addParameter("jhed", jhed)
                        .executeAndFetchTable()
                        .asList();
                sm.setName((String) names.get(0).get("name"));
            }

            course.setInstructors(staff);

            //get corresponding hired applicants
            sql = "SELECT A.name, A.email, A.jhed " +
                    "FROM HiredApplicants_Courses " +
                    "INNER JOIN Applicants A " +
                    "ON HiredApplicants_Courses.applicantId = A.id " +
                    "WHERE HiredApplicants_Courses.courseId = :courseId";
            List<Applicant> hiredApps = conn.createQuery(sql)
                    .addParameter("courseId", course.getId())
                    .executeAndFetch(Applicant.class);

            for (Applicant app: hiredApps) {
                String jhed = app.getJhed();

                // TODO: Figure out why executeAndFetch does not fill in name
                sql = "SELECT name FROM Applicants WHERE jhed = :jhed";
                List<Map<String, Object>> names = conn.createQuery(sql)
                        .addParameter("jhed", jhed)
                        .executeAndFetchTable()
                        .asList();
                app.setName((String) names.get(0).get("name"));
            }

            course.setHiredApplicants(hiredApps);

            //get corresponding interested applicants
            sql = "SELECT A.* " +
                    "FROM InterestedApplicants_Courses " +
                    "INNER JOIN Applicants A " +
                    "ON InterestedApplicants_Courses.applicantId = A.id " +
                    "WHERE InterestedApplicants_Courses.courseId = :courseId";
            List<Applicant> interestedApps = conn.createQuery(sql)
                    .addParameter("courseId", course.getId())
                    .executeAndFetch(Applicant.class);

            for (Applicant app: interestedApps) {
                String jhed = app.getJhed();

                // TODO: Figure out why executeAndFetch does not fill in name
                sql = "SELECT name FROM Applicants WHERE jhed = :jhed";
                List<Map<String, Object>> names = conn.createQuery(sql)
                        .addParameter("jhed", jhed)
                        .executeAndFetchTable()
                        .asList();
                app.setName((String) names.get(0).get("name"));
            }

            course.setInterestedApplicants(interestedApps);

            //get corresponding shortlisted applicants
            sql = "SELECT A.name, A.email, A.jhed " +
                    "FROM ShortlistedApplicants_Courses " +
                    "INNER JOIN Applicants A " +
                    "ON ShortlistedApplicants_Courses.applicantId = A.id " +
                    "WHERE ShortlistedApplicants_Courses.courseId = :courseId";
            List<Applicant> shortlistedApps = conn.createQuery(sql)
                    .addParameter("courseId", course.getId())
                    .executeAndFetch(Applicant.class);

            for (Applicant app: shortlistedApps) {
                String jhed = app.getJhed();

                // TODO: Figure out why executeAndFetch does not fill in name
                sql = "SELECT name FROM Applicants WHERE jhed = :jhed";
                List<Map<String, Object>> names = conn.createQuery(sql)
                        .addParameter("jhed", jhed)
                        .executeAndFetchTable()
                        .asList();
                app.setName((String) names.get(0).get("name"));
            }

            course.setShortlistedApplicants(shortlistedApps);

            return course;

        } catch (Sql2oException e) {
            throw new DaoException("Unable to read course", e);
        }
    }

    public void update(Course course) throws DaoException {
        try(Connection conn = sql2o.open()) {
            String sql = "UPDATE Courses SET name = :name, courseNumber = :courseNumber, " +
                    "semester = :semester, hiringComplete = :hiringComplete, courseDescription = :courseDescription, " +
                    "interviewLink = :interviewLink WHERE id = :id";
            conn.createQuery(sql)
                    .addParameter("name", course.getName())
                    .addParameter("courseNumber", course.getCourseNumber())
                    .addParameter("semester", course.getSemester())
                    .addParameter("hiringComplete", course.isHiringComplete())
                    .addParameter("courseDescription", course.getCourseDescription())
                    .addParameter("interviewLink", course.getInterviewLink())
                    .addParameter("id", course.getId())
                    .executeUpdate();

            // Delete existing entries with this applicant in joining tables
            sql = "DELETE FROM InterestedApplicants_Courses WHERE courseId = :courseId;";
            conn.createQuery(sql)
                    .addParameter("courseId", course.getId())
                    .executeUpdate();
            sql = "DELETE FROM HiredApplicants_Courses WHERE courseId = :courseId;";
            conn.createQuery(sql)
                    .addParameter("courseId", course.getId())
                    .executeUpdate();
            sql = "DELETE FROM StaffMembers_Courses WHERE courseId = :courseId;";
            conn.createQuery(sql)
                    .addParameter("courseId", course.getId())
                    .executeUpdate();
            sql = "DELETE FROM ShortlistedApplicants_Courses WHERE courseId = :courseId;";
            conn.createQuery(sql)
                    .addParameter("courseId", course.getId())
                    .executeUpdate();

            if (course.getInstructors() != null) {
                for (StaffMember staffMember : course.getInstructors()) {
                    int staffId = staffMember.getId();
                    if (staffId == 0) {
                        sql = "INSERT INTO StaffMembers(name, jhed) VALUES(:name, :jhed);";
                        staffId = (int) conn.createQuery(sql)
                                .addParameter("name", staffMember.getName())
                                .addParameter("jhed", staffMember.getJhed())
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

            if (course.getHiredApplicants() != null) {
                for (Applicant hired : course.getHiredApplicants()) {
                    int applicantId = hired.getId();
                    if (applicantId == 0) {
                        sql = "INSERT INTO Applicants(name, email, jhed) " +
                                "VALUES(:name, :email, :jhed);";
                        applicantId = (int) conn.createQuery(sql)
                                .addParameter("name", hired.getName())
                                .addParameter("email", hired.getEmail())
                                .addParameter("jhed", hired.getJhed())
                                .executeUpdate()
                                .getKey();
                        hired.setId(applicantId);
                    }

                    int courseId = course.getId();
                    sql = "INSERT INTO HiredApplicants_Courses(applicantId, courseId) " +
                            "VALUES(:applicantId, :courseId);";
                    conn.createQuery(sql)
                            .addParameter("applicantId", applicantId)
                            .addParameter("courseId", courseId)
                            .executeUpdate();
                }
            }

            if(course.getInterestedApplicants() != null) {
                // TODO: System.out.println(course.getInterestedApplicants());
                for (Applicant interested : course.getInterestedApplicants()) {
                    //TODO: System.out.println(interested.getInterestedCourses());
                    int applicantId = interested.getId();
                    if (applicantId == 0) {
                        sql = "INSERT INTO Applicants(name, email, jhed) " +
                                "VALUES(:name, :email, :jhed);";
                        applicantId = (int) conn.createQuery(sql)
                                .addParameter("name", interested.getName())
                                .addParameter("email", interested.getEmail())
                                .addParameter("jhed", interested.getJhed())
                                .executeUpdate()
                                .getKey();
                        interested.setId(applicantId);
                    }

                    int courseId = course.getId();
                    sql = "INSERT INTO InterestedApplicants_Courses(applicantId, courseId, grade) " +
                            "VALUES(:applicantId, :courseId, :grade);";
                    conn.createQuery(sql)
                            .addParameter("applicantId", applicantId)
                            .addParameter("courseId", courseId)
                            .addParameter("grade", "Not Taken") //TODO: figure out why the interestedCourses lists are all null
                            .executeUpdate();
                }
            }

            if(course.getShortlistedApplicants() != null) {
                for (Applicant shortlisted : course.getShortlistedApplicants()) {
                    int applicantId = shortlisted.getId();
                    if (applicantId == 0) {
                        sql = "INSERT INTO Applicants(name, email, jhed) " +
                                "VALUES(:name, :email, :jhed);";
                        applicantId = (int) conn.createQuery(sql)
                                .addParameter("name", shortlisted.getName())
                                .addParameter("email", shortlisted.getEmail())
                                .addParameter("jhed", shortlisted.getJhed())
                                .executeUpdate()
                                .getKey();
                        shortlisted.setId(applicantId);
                    }

                    int courseId = course.getId();
                    sql = "INSERT INTO ShortlistedApplicants_Courses(applicantId, courseId) " +
                            "VALUES(:applicantId, :courseId);";
                    conn.createQuery(sql)
                            .addParameter("applicantId", applicantId)
                            .addParameter("courseId", courseId)
                            .executeUpdate();
                }
            }

        } catch (Sql2oException e) {
            throw new DaoException("Unable to update course", e);
        }
    }

    public void delete(Course course) throws DaoException{
        int id = course.getId();
        try(Connection conn = sql2o.open()) {
            String sql = "DELETE FROM StaffMembers_Courses WHERE courseId = :id";
            conn.createQuery(sql)
                    .addParameter("id", id)
                    .executeUpdate();

            sql = "DELETE FROM HiredApplicants_Courses WHERE courseId = :id";
            conn.createQuery(sql)
                    .addParameter("id", id)
                    .executeUpdate();

            sql = "DELETE FROM InterestedApplicants_Courses WHERE courseId = :id";
            conn.createQuery(sql)
                    .addParameter("id", id)
                    .executeUpdate();

            sql = "DELETE FROM ShortlistedApplicants_Courses WHERE courseId = :id";
            conn.createQuery(sql)
                    .addParameter("id", id)
                    .executeUpdate();

            sql = "DELETE FROM Courses where id = :id";
            conn.createQuery(sql)
                    .addParameter("id", id)
                    .executeUpdate();
        } catch(Sql2oException e) {
            throw new DaoException("Unable to delete course", e);
        }
    }

    public List<Course> findAll() throws DaoException {
        try (Connection conn = sql2o.open()) {
            String sql = "SELECT * FROM Courses;";
            List<Course> courses = conn.createQuery(sql).executeAndFetch(Course.class);
            for (int i = 0; i < courses.size(); i++) {
                Course c = courses.get(i);
                //get corresponding staff members
                sql = "SELECT StaffMembers.* " +
                        "FROM StaffMembers_Courses " +
                        "INNER JOIN StaffMembers " +
                        "ON StaffMembers_Courses.staffId = StaffMembers.id " +
                        "WHERE StaffMembers_Courses.courseId = :courseId";
                List<StaffMember> staff = conn.createQuery(sql)
                        .addParameter("courseId", c.getId())
                        .executeAndFetch(StaffMember.class);

                for (StaffMember sm: staff) {
                    String jhed = sm.getJhed();

                    // TODO: Figure out why executeAndFetch does not fill in name
                    sql = "SELECT name FROM StaffMembers WHERE jhed = :jhed";
                    List<Map<String, Object>> names = conn.createQuery(sql)
                            .addParameter("jhed", jhed)
                            .executeAndFetchTable()
                            .asList();
                    sm.setName((String) names.get(0).get("name"));
                }

                c.setInstructors(staff);

                //get corresponding hired applicants
                sql = "SELECT A.name, A.email, A.jhed " +
                        "FROM HiredApplicants_Courses " +
                        "INNER JOIN Applicants A " +
                        "ON HiredApplicants_Courses.applicantId = A.id " +
                        "WHERE HiredApplicants_Courses.courseId = :courseId";
                List<Applicant> hiredApps = conn.createQuery(sql)
                        .addParameter("courseId", c.getId())
                        .executeAndFetch(Applicant.class);

                for (Applicant app: hiredApps) {
                    String jhed = app.getJhed();

                    // TODO: Figure out why executeAndFetch does not fill in name
                    sql = "SELECT name " +
                            "FROM Applicants " +
                            "WHERE jhed = :jhed";
                    List<Map<String, Object>> names = conn.createQuery(sql)
                            .addParameter("jhed", jhed)
                            .executeAndFetchTable()
                            .asList();
                    app.setName((String) names.get(0).get("name"));
                }

                c.setHiredApplicants(hiredApps);

                // get corresponding interested applicants
                sql = "SELECT A.name, A.email, A.jhed " +
                        "FROM InterestedApplicants_Courses " +
                        "INNER JOIN Applicants A " +
                        "ON InterestedApplicants_Courses.applicantId = A.id " +
                        "WHERE InterestedApplicants_Courses.courseId = :courseId";
                List<Applicant> interestedApps = conn.createQuery(sql)
                        .addParameter("courseId", c.getId())
                        .executeAndFetch(Applicant.class);

                for (Applicant app: interestedApps) {
                    String jhed = app.getJhed();

                    // TODO: Figure out why executeAndFetch does not fill in name
                    sql = "SELECT name FROM Applicants WHERE jhed = :jhed";
                    List<Map<String, Object>> names = conn.createQuery(sql)
                            .addParameter("jhed", jhed)
                            .executeAndFetchTable()
                            .asList();
                    app.setName((String) names.get(0).get("name"));
                }

                c.setInterestedApplicants(interestedApps);

                //get corresponding shortlisted applicants
                sql = "SELECT A.name, A.email, A.jhed " +
                        "FROM ShortlistedApplicants_Courses " +
                        "INNER JOIN Applicants A " +
                        "ON ShortlistedApplicants_Courses.applicantId = A.id " +
                        "WHERE ShortlistedApplicants_Courses.courseId = :courseId";
                List<Applicant> shortlistedApps = conn.createQuery(sql)
                        .addParameter("courseId", c.getId())
                        .executeAndFetch(Applicant.class);

                for (Applicant app: shortlistedApps) {
                    String jhed = app.getJhed();

                    // TODO: Figure out why executeAndFetch does not fill in name
                    sql = "SELECT name FROM Applicants WHERE jhed = :jhed";
                    List<Map<String, Object>> names = conn.createQuery(sql)
                            .addParameter("jhed", jhed)
                            .executeAndFetchTable()
                            .asList();
                    app.setName((String) names.get(0).get("name"));
                }

                c.setShortlistedApplicants(shortlistedApps);
            }

            return courses;
        } catch(Sql2oException e) {
            throw new DaoException("unable to find all courses", e);
        }
    }
}