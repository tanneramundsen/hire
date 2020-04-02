package dao;

import exception.DaoException;
import model.Applicant;
import model.Course;
import org.sql2o.Connection;
import org.sql2o.Sql2o;
import org.sql2o.Sql2oException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;

public class Sql2oApplicantDao implements ApplicantDao {

    private Sql2o sql2o;

    public Sql2oApplicantDao(Sql2o sql2o) { this.sql2o = sql2o; }

    public void add(Applicant applicant) throws DaoException {

        //check for duplicates
        if (applicant.getId() != 0) {
            update(applicant);
        } else {
            try (Connection conn = sql2o.open()) {

                String sql;
                //no duplicates --> insert
                sql = "INSERT INTO Applicants(name, email, jhed, year, majorAndMinor, gpa, " +
                        "registeredCredits, referenceEmail, resumeLink, fws, studentStatus, " +
                        "mostRecentPayroll, otherJobs, hoursAvailable, rankOne, rankTwo, rankThree) " +
                        "VALUES(:name, :email, :jhed, :year, :majorAndMinor, :gpa, " +
                        ":registeredCredits, :referenceEmail, :resumeLink, :fws, :studentStatus, " +
                        ":mostRecentPayroll, :otherJobs, :hoursAvailable, :rankOne, :rankTwo, :rankThree);";
                int id = (int) conn.createQuery(sql)
                        .addParameter("name", applicant.getName())
                        .addParameter("email", applicant.getEmail())
                        .addParameter("jhed", applicant.getJhed())
                        // TODO: handle when these are null - not supposed to put in course
                        .addParameter("year", applicant.getYear())
                        .addParameter("majorAndMinor", applicant.getMajorAndMinor())
                        .addParameter("gpa", applicant.getGpa())
                        .addParameter("registeredCredits",applicant.getRegisteredCredits())
                        .addParameter("referenceEmail", applicant.getReferenceEmail())
                        .addParameter("resumeLink", applicant.getResumeLink())
                        .addParameter("fws", applicant.getFws())
                        .addParameter("studentStatus", applicant.getStudentStatus())
                        .addParameter("mostRecentPayroll", applicant.getMostRecentPayroll())
                        .addParameter("otherJobs", applicant.getOtherJobs())
                        .addParameter("hoursAvailable", applicant.getHoursAvailable())
                        .addParameter("rankOne", applicant.getRankOne())
                        .addParameter("rankTwo", applicant.getRankTwo())
                        .addParameter("rankThree", applicant.getRankThree())
                        .executeUpdate()
                        .getKey();
                applicant.setId(id);

                if (applicant.getInterestedCourses() != null) {
                    for (Map.Entry<Course,String> entry : applicant.getInterestedCourses().entrySet()) {
                        Course course = entry.getKey();
                        String grade = entry.getValue();
                        int courseId = course.getId();
                        if (courseId == 0) {
                            sql = "INSERT INTO Courses(name, courseNumber, semester, hiringComplete) " +
                                    "VALUES(:name, :courseNumber, :semester, :hiringComplete);";
                            int sId = (int) conn.createQuery(sql)
                                    .addParameter("name", course.getName())
                                    .addParameter("courseNumber", course.getCourseNumber())
                                    .addParameter("semester", course.getSemester())
                                    .addParameter("hiringComplete", course.isHiringComplete())
                                    .executeUpdate()
                                    .getKey();
                            course.setId(sId);
                        }
                        sql = "INSERT INTO InterestedApplicants_Courses(applicantId, courseId, grade) " +
                                "VALUES(:applicantId, :courseId, :grade);";
                        conn.createQuery(sql)
                                .addParameter("applicantId", applicant.getId())
                                .addParameter("courseId", course.getId())
                                .addParameter("grade", grade)
                                .executeUpdate();
                    }
                }

                if (applicant.getHiredCourse() != null) {
                    Course course = applicant.getHiredCourse();
                    sql = "INSERT INTO HiredApplicants_Courses(applicantId, courseId) " +
                            "VALUES(:applicantId, :courseId);";
                    conn.createQuery(sql)
                            .addParameter("applicantId", applicant.getId())
                            .addParameter("courseId", course.getId())
                            .executeUpdate();
                }

                List<Course> courses = applicant.getHeadCAInterest();
                if (courses != null) {
                    for (Course course: courses) {
                        sql = "INSERT INTO HeadCAInterest_Courses(applicantId, courseId) " +
                                "VALUES(:applicantId, :courseId);";
                        conn.createQuery(sql)
                                .addParameter("applicantId", applicant.getId())
                                .addParameter("courseId", course.getId())
                                .executeUpdate();
                    }
                }
            } catch (Sql2oException ex) {
                throw new DaoException("Unable to add the applicant", ex);
            }
        }
    }

    public void update(Applicant applicant) throws DaoException {
        try(Connection conn = sql2o.open()) {
            String sql = "UPDATE Applicants " +
                    "SET name = :name, email = :email, jhed = :jhed, year = :year, " +
                    "majorAndMinor = :majorAndMinor, gpa = :gpa, registeredCredits = :registeredCredits, " +
                    "referenceEmail = :referenceEmail, resumeLink = :resumeLink, fws = :fws, " +
                    "studentStatus = :studentStatus, mostRecentPayroll = :mostRecentPayroll, " +
                    "otherJobs = :otherJobs, hoursAvailable = :hoursAvailable " +
                    "WHERE id = :id;";
            conn.createQuery(sql)
                    .addParameter("name", applicant.getName())
                    .addParameter("email", applicant.getEmail())
                    .addParameter("jhed", applicant.getJhed())
                    .addParameter("year", applicant.getYear())
                    .addParameter("majorAndMinor", applicant.getMajorAndMinor())
                    .addParameter("gpa", applicant.getGpa())
                    .addParameter("registeredCredits",applicant.getRegisteredCredits())
                    .addParameter("referenceEmail", applicant.getReferenceEmail())
                    .addParameter("resumeLink", applicant.getResumeLink())
                    .addParameter("fws", applicant.getFws())
                    .addParameter("studentStatus", applicant.getStudentStatus())
                    .addParameter("mostRecentPayroll", applicant.getMostRecentPayroll())
                    .addParameter("otherJobs", applicant.getOtherJobs())
                    .addParameter("hoursAvailable", applicant.getHoursAvailable())
                    .addParameter("id", applicant.getId())
                    .executeUpdate();

            if (applicant.getRankOne() != null) {
                sql = "UPDATE Applicants " +
                        "SET rankOne = :rankOne " +
                        "WHERE id = :id;";
                conn.createQuery(sql)
                        .addParameter("rankOne", applicant.getRankOne().getId())
                        .addParameter("id", applicant.getId())
                        .executeUpdate();
            }

            if (applicant.getRankTwo() != null) {
                sql = "UPDATE Applicants " +
                        "SET rankTwo = :rankTwo " +
                        "WHERE id = :id;";
                conn.createQuery(sql)
                        .addParameter("rankTwo", applicant.getRankTwo().getId())
                        .addParameter("id", applicant.getId())
                        .executeUpdate();
            }

            if (applicant.getRankThree() != null) {
                sql = "UPDATE Applicants " +
                        "SET rankThree = :rankThree " +
                        "WHERE id = :id;";
                conn.createQuery(sql)
                        .addParameter("rankThree", applicant.getRankThree().getId())
                        .addParameter("id", applicant.getId())
                        .executeUpdate();
            }

            // Delete existing entries with this applicant in joining tables
            sql = "DELETE FROM InterestedApplicants_Courses " +
                    "WHERE applicantId = :applicantId;";
            conn.createQuery(sql)
                    .addParameter("applicantId", applicant.getId())
                    .executeUpdate();
            sql = "DELETE FROM HeadCAInterest_Courses WHERE applicantId = :applicantId;";
            conn.createQuery(sql)
                    .addParameter("applicantId", applicant.getId())
                    .executeUpdate();
            sql = "DELETE FROM HiredApplicants_Courses " +
                    "WHERE applicantId = :applicantId;";
            conn.createQuery(sql)
                    .addParameter("applicantId", applicant.getId())
                    .executeUpdate();

            // Fresh update to joining tables
            List<Course> interestedCourses = new ArrayList();
            List<String> gradesInterested = new ArrayList();
            if (applicant.getInterestedCourses() != null) {
                for (Map.Entry<Course,String> entry : applicant.getInterestedCourses().entrySet()) {
                    Course course = entry.getKey();
                    String grade = entry.getValue();
                    int courseId = course.getId();
                    if (course.getId() == 0) {
                        sql = "INSERT INTO Courses(name, courseNumber, semester, hiringComplete) " +
                                "VALUES(:name, :courseNumber, :semester, :hiringComplete);";
                        courseId = (int) conn.createQuery(sql)
                                .addParameter("name", course.getName())
                                .addParameter("courseNumber", course.getCourseNumber())
                                .addParameter("semester", course.getSemester())
                                .addParameter("hiringComplete", course.isHiringComplete())
                                .executeUpdate()
                                .getKey();

                        course.setId(courseId);
                    }

                    interestedCourses.add(course);
                    gradesInterested.add(grade);
                }

                for (int i = 0; i < interestedCourses.size(); i++) {
                    sql = "INSERT INTO InterestedApplicants_Courses(applicantId, courseId, grade) " +
                            "VALUES(:applicantId, :courseId, :grade);";
                    conn.createQuery(sql)
                            .addParameter("applicantId", applicant.getId())
                            .addParameter("courseId", interestedCourses.get(i).getId())
                            .addParameter("grade", gradesInterested.get(i))
                            .executeUpdate();
                }
            }

            if (applicant.getHeadCAInterest() != null) {
                for (Course course: applicant.getHeadCAInterest()) {
                    int courseId = course.getId();
                    if (courseId == 0) {
                        sql = "INSERT INTO Courses(name, courseNumber, semester, hiringComplete) " +
                                "VALUES(:name, :courseNumber, :semester, :hiringComplete);";
                        courseId = (int) conn.createQuery(sql)
                                .addParameter("name", course.getName())
                                .addParameter("courseNumber", course.getCourseNumber())
                                .addParameter("semester", course.getSemester())
                                .addParameter("hiringComplete", course.isHiringComplete())
                                .executeUpdate()
                                .getKey();

                        course.setId(courseId);
                    }

                    sql = "INSERT INTO HeadCAInterest_Courses(applicantId, courseId) " +
                            "VALUES(:applicantId, :courseId);";
                    conn.createQuery(sql)
                            .addParameter("applicantId", applicant.getId())
                            .addParameter("courseId", course.getId())
                            .executeUpdate();
                }
            }

            Course hiredCourse = applicant.getHiredCourse();
            if (hiredCourse != null) {
                int hiredCourseId = hiredCourse.getId();

                if (hiredCourseId == 0) {
                    sql = "INSERT INTO Courses(name, courseNumber, semester, hiringComplete) " +
                            "VALUES(:name, :courseNumber, :semester, :hiringComplete);";
                    hiredCourseId = (int) conn.createQuery(sql)
                            .addParameter("name", hiredCourse.getName())
                            .addParameter("courseNumber", hiredCourse.getCourseNumber())
                            .addParameter("semester", hiredCourse.getSemester())
                            .addParameter("hiringComplete", hiredCourse.isHiringComplete())
                            .executeUpdate()
                            .getKey();

                    hiredCourse.setId(hiredCourseId);
                }

                sql = "INSERT INTO HiredApplicants_Courses(applicantId, courseId) " +
                        "VALUES(:applicantId, :courseId);";
                conn.createQuery(sql)
                        .addParameter("applicantId", applicant.getId())
                        .addParameter("courseId", hiredCourseId)
                        .executeUpdate();
            }
        } catch (Sql2oException e) {
            throw new DaoException("Unable to update applicant", e);
        }
    }

    public void delete(Applicant applicant) throws DaoException {
        try(Connection conn = sql2o.open()) {
            int id = applicant.getId();
            String sql = "DELETE FROM InterestedApplicants_Courses " +
                    "WHERE applicantId = :applicantId;";
            conn.createQuery(sql)
                    .addParameter("applicantId", id)
                    .executeUpdate();
            sql = "DELETE FROM HiredApplicants_Courses " +
                    "WHERE applicantId = :applicantId;";
            conn.createQuery(sql)
                    .addParameter("applicantId", id)
                    .executeUpdate();
            sql = "DELETE FROM ShortlistedApplicants_Courses " +
                    "WHERE applicantId = :applicantId;";
            conn.createQuery(sql)
                    .addParameter("applicantId", id)
                    .executeUpdate();
            sql = "DELETE FROM Applicants WHERE id = :id;";
            conn.createQuery(sql)
                    .addParameter("id", id)
                    .executeUpdate();
        } catch(Sql2oException e) {
            throw new DaoException("Unable to delete applicant", e);
        }
    }

    public Applicant read(int id) throws DaoException {
        try (Connection conn = sql2o.open()) {
            String sql = "SELECT id, name, email, jhed, year, majorAndMinor, gpa, registeredCredits," +
                    "referenceEmail, resumeLink, fws, studentStatus, mostRecentPayroll," +
                    "otherJobs, hoursAvailable" +
                    "FROM Applicants " +
                    "WHERE id = :id;";
            Applicant applicant = conn.createQuery(sql)
                    .addParameter("id", id)
                    .executeAndFetchFirst(Applicant.class);

            if (applicant == null) {
                return null;
            }

            // TODO: Figure out why executeAndFetch does not fill in name
            sql = "SELECT name FROM Applicants WHERE id = :id";
            List<Map<String, Object>> names = conn.createQuery(sql)
                    .addParameter("id", id)
                    .executeAndFetchTable()
                    .asList();
            applicant.setName((String) names.get(0).get("name"));

            //get rankOne, rankTwo, and rankThree
            Course[] rankedCourses = new Course[3];
            sql = "SELECT C.* " +
                    "FROM Courses C INNER JOIN Applicants A " +
                    "ON C.id = A.rankOne " +
                    "WHERE A.id = :id";
            List<Course> coursesRankOne = conn.createQuery(sql)
                    .addParameter("id", id)
                    .executeAndFetch(Course.class);
            if (!coursesRankOne.isEmpty()) {
                rankedCourses[0] = coursesRankOne.get(0);
            }
            sql = "SELECT C.* " +
                    "FROM Courses C INNER JOIN Applicants A " +
                    "ON C.id = A.rankTwo " +
                    "WHERE A.id = :id";
            List<Course> coursesRankTwo = conn.createQuery(sql)
                    .addParameter("id", id)
                    .executeAndFetch(Course.class);
            if (!coursesRankTwo.isEmpty()) {
                rankedCourses[1] = coursesRankTwo.get(0);
            }
            sql = "SELECT C.* " +
                    "FROM Courses C INNER JOIN Applicants A " +
                    "ON C.id = As.rankThree " +
                    "WHERE A.id = :id";
            List<Course> coursesRankThree = conn.createQuery(sql)
                    .addParameter("id", id)
                    .executeAndFetch(Course.class);
            if (!coursesRankThree.isEmpty()) {
                rankedCourses[2] = coursesRankThree.get(0);
            }

            applicant.setRankOne(rankedCourses[0]);
            applicant.setRankTwo(rankedCourses[1]);
            applicant.setRankThree(rankedCourses[2]);

            // Get headCAInterest from joining table
            sql = "SELECT C.* " +
                    "FROM HeadCAInterest_Courses INNER JOIN Courses C " +
                    "ON HeadCAInterest_Courses.courseId = C.id " +
                    "WHERE HeadCAInterest_Courses.applicantId = :applicantId;";
            List<Course> headCAInterestCourses = conn.createQuery(sql)
                    .addParameter("applicantId", id)
                    .executeAndFetch(Course.class);
            applicant.setHeadCAInterest(headCAInterestCourses);

            // Get corresponding interestedCourses according to joining table
            sql = "SELECT C.* " +
                    "FROM InterestedApplicants_Courses INNER JOIN Courses C " +
                    "ON InterestedApplicants_Courses.courseId = C.id " +
                    "WHERE InterestedApplicants_Courses.applicantId = :applicantId;";
            List<Course> courses = conn.createQuery(sql)
                    .addParameter("applicantId", id)
                    .executeAndFetch(Course.class);

            // Initialize HashMap and append (Course, grade) pairs one by one
            HashMap<Course, String> interestedCourses = new HashMap<Course, String>();
            for (Course course : courses) {
                sql = "SELECT grade " +
                        "FROM InterestedApplicants_Courses " +
                        "WHERE applicantId = :applicantId " +
                        "AND courseId = :courseId;";
                List<Map<String, Object>> grades = conn.createQuery(sql)
                        .addParameter("applicantId", id)
                        .addParameter("courseId", course.getId())
                        .executeAndFetchTable()
                        .asList();
                String grade = (String) grades.get(0).get("grade");
                interestedCourses.put(course, grade);
            }
            applicant.setInterestedCourses(interestedCourses);

            // Get HiredCourse
            sql = "SELECT C.* " +
                    "FROM HiredApplicants_Courses INNER JOIN Courses C " +
                    "ON HiredApplicants_Courses.courseId = C.id " +
                    "WHERE HiredApplicants_Courses.applicantId = :applicantId;";
            List<Course> hiredCourses = (conn.createQuery(sql)
                    .addParameter("applicantId", id)
                    .executeAndFetch(Course.class));
            if (!hiredCourses.isEmpty()) {
                // should only be one
                applicant.setHiredCourse(hiredCourses.get(0));
            }
            return applicant;

        } catch (Sql2oException e) {
            throw new DaoException("Unable to read applicant", e);
        }
    }

    public Applicant read(String jhed) throws DaoException {
        try (Connection conn = sql2o.open()) {
            String sql = "SELECT id, name, email, jhed, year, majorAndMinor, gpa, registeredCredits, " +
                    "referenceEmail, resumeLink, fws, studentStatus, mostRecentPayroll, " +
                    "otherJobs, hoursAvailable " +
                    "FROM Applicants " +
                    "WHERE jhed = :jhed;";
            Applicant applicant = conn.createQuery(sql)
                    .addParameter("jhed", jhed)
                    .executeAndFetchFirst(Applicant.class);

            System.out.println(applicant);

            if (applicant == null) {
                return null;
            }

            // TODO: Figure out why executeAndFetch does not fill in name
            sql = "SELECT name FROM Applicants WHERE jhed = :jhed";
            List<Map<String, Object>> names = conn.createQuery(sql)
                    .addParameter("jhed", jhed).executeAndFetchTable().asList();
            applicant.setName((String) names.get(0).get("name"));

            int id = applicant.getId();

            //get rankOne, rankTwo, and rankThree
            Course[] rankedCourses = new Course[3];
            sql = "SELECT C.* " +
                    "FROM Courses C INNER JOIN Applicants A " +
                    "ON C.id = A.rankOne " +
                    "WHERE A.id = :id";
            List<Course> coursesRankOne = conn.createQuery(sql)
                    .addParameter("id", id)
                    .executeAndFetch(Course.class);
            if (!coursesRankOne.isEmpty()) {
                rankedCourses[0] = coursesRankOne.get(0);
            }
            sql = "SELECT C.* " +
                    "FROM Courses C INNER JOIN Applicants A " +
                    "ON C.id = A.rankTwo " +
                    "WHERE A.id = :id";
            List<Course> coursesRankTwo = conn.createQuery(sql)
                    .addParameter("id", id)
                    .executeAndFetch(Course.class);
            if (!coursesRankTwo.isEmpty()) {
                rankedCourses[1] = coursesRankTwo.get(0);
            }
            sql = "SELECT C.* " +
                    "FROM Courses C INNER JOIN Applicants A " +
                    "ON C.id = A.rankThree " +
                    "WHERE A.id = :id";
            List<Course> coursesRankThree = conn.createQuery(sql)
                    .addParameter("id", id)
                    .executeAndFetch(Course.class);
            if (!coursesRankThree.isEmpty()) {
                rankedCourses[2] = coursesRankThree.get(0);
            }
            applicant.setRankOne(rankedCourses[0]);
            applicant.setRankTwo(rankedCourses[1]);
            applicant.setRankThree(rankedCourses[2]);

            // headCAInterest Table
            sql = "SELECT C.* " +
                    "FROM HeadCAInterest_Courses INNER JOIN Courses C " +
                    "ON HeadCAInterest_Courses.courseId = C.id " +
                    "WHERE HeadCAInterest_Courses.applicantId = :applicantId;";
            List<Course> headCAInterestCourses = conn.createQuery(sql)
                    .addParameter("applicantId", id)
                    .executeAndFetch(Course.class);
            applicant.setHeadCAInterest(headCAInterestCourses);

            //get corresponding interestedCourses according to joining table
            sql = "SELECT C.* " +
                    "FROM InterestedApplicants_Courses INNER JOIN Courses C " +
                    "ON InterestedApplicants_Courses.courseId = C.id " +
                    "WHERE InterestedApplicants_Courses.applicantId = :applicantId;";
            List<Course> courses = conn.createQuery(sql)
                    .addParameter("applicantId", applicant.getId())
                    .executeAndFetch(Course.class);

            // Initialize HashMap and append (Course, grade) pairs one by one
            HashMap<Course, String> interestedCourses = new HashMap<Course, String>();
            for (Course course : courses) {
                sql = "SELECT grade " +
                        "FROM InterestedApplicants_Courses " +
                        "WHERE applicantId = :applicantId " +
                        "AND courseId = :courseId;";
                List<Map<String, Object>> grades = conn.createQuery(sql)
                        .addParameter("applicantId", applicant.getId())
                        .addParameter("courseId", course.getId())
                        .executeAndFetchTable()
                        .asList();
                String grade = (String) grades.get(0).get("grade");
                interestedCourses.put(course, grade);
            }
            applicant.setInterestedCourses(interestedCourses);

            sql = "SELECT C.* " +
                    "FROM HiredApplicants_Courses INNER JOIN Courses C " +
                    "ON HiredApplicants_Courses.courseId = C.id " +
                    "WHERE HiredApplicants_Courses.applicantId = :applicantId;";
            List<Course> hiredCourses = (conn.createQuery(sql)
                    .addParameter("applicantId", applicant.getId())
                    .executeAndFetch(Course.class));
            if (!hiredCourses.isEmpty()) {
                applicant.setHiredCourse(hiredCourses.get(0));
                //should only be one
            }
            return applicant;

        } catch (Sql2oException e) {
            throw new DaoException("Unable to read applicant", e);
        }
    }

    @Override
    public List<Applicant> findAll() throws DaoException {
        try (Connection conn = sql2o.open()) {
            String sql = "SELECT * FROM Applicants;";
            List<Applicant> applicants = conn.createQuery(sql)
                    .executeAndFetch(Applicant.class);
            for (Applicant applicant : applicants) {
                int applicantId = applicant.getId();
                sql = "SELECT C.* " +
                        "FROM InterestedApplicants_Courses INNER JOIN Courses C " +
                        "ON InterestedApplicants_Courses.courseId = C.id " +
                        "WHERE InterestedApplicants_Courses.applicantId = :id;";
                List<Course> courses = conn.createQuery(sql)
                        .addParameter("id", applicantId)
                        .executeAndFetch(Course.class);

                //get rankOne, rankTwo, and rankThree
                Course[] rankedCourses = new Course[3];
                sql = "SELECT C.* " +
                        "FROM Courses C INNER JOIN Applicants A " +
                        "ON C.id = A.rankOne " +
                        "WHERE A.id = :id";
                List<Course> coursesRankOne = conn.createQuery(sql)
                        .addParameter("id", applicantId)
                        .executeAndFetch(Course.class);
                if (!coursesRankOne.isEmpty()) {
                    rankedCourses[0] = coursesRankOne.get(0);
                }
                sql = "SELECT C.* " +
                        "FROM Courses C INNER JOIN Applicants A " +
                        "ON C.id = A.rankTwo " +
                        "WHERE A.id = :id";
                List<Course> coursesRankTwo = conn.createQuery(sql)
                        .addParameter("id", applicantId)
                        .executeAndFetch(Course.class);
                if (!coursesRankTwo.isEmpty()) {
                    rankedCourses[1] = coursesRankTwo.get(0);
                }
                sql = "SELECT C.* " +
                        "FROM Courses C INNER JOIN Applicants A " +
                        "ON C.id = A.rankThree " +
                        "WHERE A.id = :id";
                List<Course> coursesRankThree = conn.createQuery(sql)
                        .addParameter("id", applicantId)
                        .executeAndFetch(Course.class);
                if (!coursesRankThree.isEmpty()) {
                    rankedCourses[2] = coursesRankThree.get(0);
                }

                applicant.setRankOne(rankedCourses[0]);
                applicant.setRankTwo(rankedCourses[1]);
                applicant.setRankThree(rankedCourses[2]);

                // Initialize HashMap and append (Course, grade) pairs one by one
                HashMap<Course, String> interestedCourses = new HashMap<Course, String>();
                for (Course course : courses) {
                    sql = "SELECT grade " +
                            "FROM InterestedApplicants_Courses " +
                            "WHERE applicantId = :applicantId " +
                            "AND courseId = :courseId;";
                    List<Map<String, Object>> grades = conn.createQuery(sql)
                            .addParameter("applicantId", applicantId)
                            .addParameter("courseId", course.getId())
                            .executeAndFetchTable()
                            .asList();
                    String grade = (String) grades.get(0).get("grade");
                    interestedCourses.put(course, grade);
                }
                applicant.setInterestedCourses(interestedCourses);

                sql = "SELECT C.* " +
                        "FROM HiredApplicants_Courses INNER JOIN Courses C " +
                        "ON HiredApplicants_Courses.courseId = C.id " +
                        "WHERE HiredApplicants_Courses.applicantId = :applicantId;";
                Course hiredCourse = (conn.createQuery(sql)
                        .addParameter("applicantId", applicantId)
                        .executeAndFetchFirst(Course.class));
                if (hiredCourse != null) {
                    applicant.setHiredCourse(hiredCourse);
                }
            }
            return applicants;
        } catch(Sql2oException e) {
            throw new DaoException("Unable to find all applicants", e);
        }
    }

    public List<Applicant> findByCourseId(int courseId) throws DaoException{
        try(Connection conn = sql2o.open()) {
            String sql = "SELECT A.name, A.email, A.jhed " +
                    "FROM Applicants A INNER JOIN InterestedApplicants_Courses " +
                    "ON A.id = InterestedApplicants_Courses.applicantId " +
                    "WHERE InterestedApplicants_Courses.courseId = :courseId";
            return conn.createQuery(sql)
                    .addParameter("courseId", courseId)
                    .executeAndFetch(Applicant.class);
        } catch(Sql2oException e) {
            throw new DaoException("Unable to find applicants by course id", e);
        }
    }


}