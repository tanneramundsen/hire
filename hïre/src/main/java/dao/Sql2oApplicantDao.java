package dao;

import exception.DaoException;
import model.Applicant;
import model.Course;
import org.jetbrains.annotations.NotNull;
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
                //no duplicates --> insert
                String sql;
                sql = "INSERT INTO Applicants(name, email, jhed, year, majorAndMinor, gpa, " +
                        "registeredCredits, referenceEmail, resumeLink, fws, studentStatus, " +
                        "mostRecentPayroll, otherJobs, hoursAvailable) " +
                        "VALUES(:name, :email, :jhed, :year, :majorAndMinor, :gpa, " +
                        ":registeredCredits, :referenceEmail, :resumeLink, :fws, :studentStatus, " +
                        ":mostRecentPayroll, :otherJobs, :hoursAvailable);";
                int id = (int) conn.createQuery(sql)
                        .bind(applicant)
                        .executeUpdate()
                        .getKey();
                applicant.setId(id);

                updateRankedCourseList(applicant, conn);

                if (applicant.getInterestedCourses() != null) {
                    for (Map.Entry<Course,String> entry : applicant.getInterestedCourses().entrySet()) {
                        Course course = entry.getKey();
                        String grade = entry.getValue();
                        int courseId = course.getId();
                        if (courseId == 0) {
                            sql = "INSERT INTO Courses(name, courseNumber, semester, hiringComplete, courseDescription, " +
                                    "interviewLink) VALUES(:name, :courseNumber, :semester, :hiringComplete, :courseDescription, " +
                                    ":interviewLink);";
                            courseId = (int) conn.createQuery(sql)
                                    .bind(course)
                                    .executeUpdate()
                                    .getKey();
                            course.setId(courseId);
                        }
                        sql = "INSERT INTO Applicants_Courses(applicantId, courseId, interested, grade) " +
                                "VALUES(:applicantId, :courseId, 1, :grade);";
                        conn.createQuery(sql)
                                .addParameter("applicantId", id)
                                .addParameter("courseId", course.getId())
                                .addParameter("grade", grade)
                                .executeUpdate();
                    }
                }

                if (applicant.getPreviousCA() != null) {
                    for (Course course: applicant.getPreviousCA()) {
                        int courseId = course.getId();
                        if (courseId == 0) {
                            sql = "INSERT INTO Courses(name, courseNumber, semester, hiringComplete, courseDescription, " +
                                    "interviewLink) VALUES(:name, :courseNumber, :semester, :hiringComplete, :courseDescription, " +
                                    ":interviewLink);";
                            courseId = (int) conn.createQuery(sql)
                                    .bind(course)
                                    .executeUpdate()
                                    .getKey();
                            course.setId(courseId);

                            sql = "INSERT INTO Applicants_Courses(applicantId, courseId, previousCA) " +
                                    "VALUES(:applicantId, :courseId, 1);";
                            conn.createQuery(sql)
                                    .addParameter("applicantId", id)
                                    .addParameter("courseId", courseId)
                                    .executeUpdate();
                        } else {
                            sql = "UPDATE Applicants_Courses " +
                                    "SET previousCA = 1 " +
                                    "WHERE applicantId = :applicantId " +
                                    "AND courseId = :courseId;";
                            conn.createQuery(sql)
                                    .addParameter("applicantId", id)
                                    .addParameter("courseId", courseId)
                                    .executeUpdate();
                        }
                    }
                }

                if (applicant.getHeadCAInterest() != null) {
                    for (Course course: applicant.getHeadCAInterest()) {
                        int courseId = course.getId();
                        if (courseId == 0) {
                            sql = "INSERT INTO Courses(name, courseNumber, semester, hiringComplete, courseDescription interviewLink) " +
                                    "VALUES(:name, :courseNumber, :semester, :hiringComplete, :courseDescription, :interviewLink);";
                            courseId = (int) conn.createQuery(sql)
                                    .bind(course)
                                    .executeUpdate()
                                    .getKey();
                            course.setId(courseId);

                            sql = "INSERT INTO Applicants_Courses(applicantId, courseId, headCAInterest) " +
                                    "VALUES(:applicantId, :courseId, 1);";
                            conn.createQuery(sql)
                                    .addParameter("applicantId", id)
                                    .addParameter("courseId", courseId)
                                    .executeUpdate();
                        } else {
                            sql = "UPDATE Applicants_Courses " +
                                    "SET headCAInterest = 1 " +
                                    "WHERE applicantId = :applicantId " +
                                    "AND courseId = :courseId;";
                            conn.createQuery(sql)
                                    .addParameter("applicantId", id)
                                    .addParameter("courseId", courseId)
                                    .executeUpdate();
                        }
                    }
                }

                if (applicant.getHiredCourse() != null) {
                    Course course = applicant.getHiredCourse();
                    int courseId = course.getId();
                    if (courseId == 0) {
                         sql = "INSERT INTO Courses(name, courseNumber, semester, hiringComplete, courseDescription, interviewLink) " +
                                 "VALUES(:name, :courseNumber, :semester, :hiringComplete, :courseDescription, :interviewLink);";
                         courseId = (int) conn.createQuery(sql)
                                 .bind(course)
                                 .executeUpdate()
                                 .getKey();
                         course.setId(courseId);

                         sql = "INSERT INTO Applicants_Courses(applicantId, courseId, hired) " +
                                 "VALUES(:applicantId, :courseId, 1);";
                         conn.createQuery(sql)
                                 .addParameter("applicantId", id)
                                 .addParameter("courseId", courseId)
                                 .executeUpdate();
                    } else {
                        sql = "UPDATE Applicants_Courses " +
                                "SET hired = 1 " +
                                "WHERE applicantId = :applicantId " +
                                "AND courseId = :courseId;";
                        conn.createQuery(sql)
                                .addParameter("applicantId", id)
                                .addParameter("courseId", courseId)
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
                    .bind(applicant)
                    .executeUpdate();

            int id = applicant.getId();

            // Update rankOne, rankTwo, rankThree
            updateRankedCourseList(applicant, conn);

            // Delete existing entries with this applicant in joining tables
            sql = "DELETE FROM Applicants_Courses " +
                    "WHERE applicantId = :applicantId;";
            conn.createQuery(sql)
                    .addParameter("applicantId", id)
                    .executeUpdate();

            // Fresh update to joining tables
            List<Course> interestedCourses = new ArrayList();
            List<String> gradesInterested = new ArrayList();
            if (applicant.getInterestedCourses() != null) {
                for (Map.Entry<Course,String> entry : applicant.getInterestedCourses().entrySet()) {
                    Course course = entry.getKey();
                    String grade = entry.getValue();
                    int courseId = course.getId();
                    if (courseId == 0) {
                        sql = "INSERT INTO Courses(name, courseNumber, semester, hiringComplete, courseDescription, " +
                                "interviewLink) VALUES(:name, :courseNumber, :semester, :hiringComplete, :courseDescription, " +
                                ":interviewLink);";
                        courseId = (int) conn.createQuery(sql)
                                .bind(course)
                                .executeUpdate()
                                .getKey();

                        course.setId(courseId);
                    }

                    interestedCourses.add(course);
                    gradesInterested.add(grade);
                }

                for (int i = 0; i < interestedCourses.size(); i++) {
                    sql = "INSERT INTO Applicants_Courses(applicantId, courseId, interested, grade) " +
                            "VALUES(:applicantId, :courseId, 1, :grade);";
                    conn.createQuery(sql)
                            .addParameter("applicantId", id)
                            .addParameter("courseId", interestedCourses.get(i).getId())
                            .addParameter("grade", gradesInterested.get(i))
                            .executeUpdate();
                }
            }

            if (applicant.getPreviousCA() != null) {
                for (Course course: applicant.getPreviousCA()) {
                    int courseId = course.getId();
                    if (courseId == 0) {
                        sql = "INSERT INTO Courses(name, courseNumber, semester, hiringComplete, courseDescription, " +
                                "interviewLink) VALUES(:name, :courseNumber, :semester, :hiringComplete, :courseDescription, " +
                                ":interviewLink);";
                        courseId = (int) conn.createQuery(sql)
                                .bind(course)
                                .executeUpdate()
                                .getKey();
                        course.setId(courseId);

                        sql = "INSERT INTO Applicants_Courses(applicantId, courseId, previousCA) " +
                                "VALUES(:applicantId, :courseId, 1);";
                        conn.createQuery(sql)
                                .addParameter("applicantId", id)
                                .addParameter("courseId", courseId)
                                .executeUpdate();
                    } else {
                        sql = "UPDATE Applicants_Courses " +
                                "SET previousCA = 1 " +
                                "WHERE applicantId = :applicantId " +
                                "AND courseId = :courseId;";
                        conn.createQuery(sql)
                                .addParameter("applicantId", id)
                                .addParameter("courseId", courseId)
                                .executeUpdate();
                    }
                }
            }

            if (applicant.getHeadCAInterest() != null) {
                for (Course course: applicant.getHeadCAInterest()) {
                    int courseId = course.getId();
                    if (courseId == 0) {
                        sql = "INSERT INTO Courses(name, courseNumber, semester, hiringComplete, courseDescription, " +
                                "interviewLink) VALUES(:name, :courseNumber, :semester, :hiringComplete, :courseDescription, " +
                                ":interviewLink);";
                        courseId = (int) conn.createQuery(sql)
                                .bind(course)
                                .executeUpdate()
                                .getKey();
                        course.setId(courseId);

                        sql = "INSERT INTO Applicants_Courses(applicantId, courseId, headCAInterest) " +
                                "VALUES(:applicantId, :courseId, 1);";
                        conn.createQuery(sql)
                                .addParameter("applicantId", id)
                                .addParameter("courseId", courseId)
                                .executeUpdate();
                    } else {
                        sql = "UPDATE Applicants_Courses " +
                                "SET headCAInterest = 1 " +
                                "WHERE applicantId = :applicantId " +
                                "AND courseId = :courseId;";
                        conn.createQuery(sql)
                                .addParameter("applicantId", id)
                                .addParameter("courseId", courseId)
                                .executeUpdate();
                    }
                }
            }

            Course hiredCourse = applicant.getHiredCourse();
            if (hiredCourse != null) {
                int hiredCourseId = hiredCourse.getId();

                if (hiredCourseId == 0) {
                    sql = "INSERT INTO Courses(name, courseNumber, semester, hiringComplete, courseDescription, interviewLink) " +
                            "VALUES(:name, :courseNumber, :semester, :hiringComplete, :courseDescription, :interviewLink);";
                    hiredCourseId = (int) conn.createQuery(sql)
                            .bind(hiredCourse)
                            .executeUpdate()
                            .getKey();

                    hiredCourse.setId(hiredCourseId);

                    sql = "INSERT INTO Applicants_Courses(applicantId, courseId, hired)" +
                            "VALUES(:applicantId, :courseId, 1);";
                    conn.createQuery(sql)
                            .addParameter("applicantId", id)
                            .addParameter("courseId", hiredCourseId)
                            .executeUpdate();
                } else {
                    sql = "UPDATE Applicants_Courses " +
                            "SET hired = 1 " +
                            "WHERE applicantId = :applicantId " +
                            "AND courseId = :courseId;";
                    conn.createQuery(sql)
                            .addParameter("applicantId", id)
                            .addParameter("courseId", hiredCourseId)
                            .executeUpdate();
                }
            }
        } catch (Sql2oException e) {
            throw new DaoException("Unable to update applicant", e);
        }
    }

    public void delete(Applicant applicant) throws DaoException {
        try(Connection conn = sql2o.open()) {
            int id = applicant.getId();
            String sql = "DELETE FROM Applicants_Courses " +
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
            String sql = "SELECT id, name, jhed, year, majorAndMinor, gpa, " +
                    "registeredCredits, referenceEmail, resumeLink, fws, studentStatus, " +
                    "mostRecentPayroll, otherJobs, hoursAvailable " +
                    "FROM Applicants " +
                    "WHERE id = :id;";
            Applicant applicant = conn.createQuery(sql)
                    .addParameter("id", id)
                    .executeAndFetchFirst(Applicant.class);

            if (applicant == null) {
                return null;
            }

            //get rankOne, rankTwo, and rankThree
            Course[] rankedCourses = readRankedCourses(id, conn);
            applicant.setRankOne(rankedCourses[0]);
            applicant.setRankTwo(rankedCourses[1]);
            applicant.setRankThree(rankedCourses[2]);

            // Get corresponding interestedCourses according to joining table
            sql = "SELECT C.* " +
                    "FROM Applicants_Courses INNER JOIN Courses C " +
                    "ON Applicants_Courses.courseId = C.id " +
                    "WHERE Applicants_Courses.applicantId = :applicantId " +
                    "AND Applicants_Courses.interested = 1;";
            List<Course> courses = conn.createQuery(sql)
                    .addParameter("applicantId", id)
                    .executeAndFetch(Course.class);

            // Initialize HashMap and append (Course, grade) pairs one by one
            HashMap<Course, String> interestedCourses = new HashMap<Course, String>();
            for (Course course : courses) {
                sql = "SELECT grade " +
                        "FROM Applicants_Courses " +
                        "WHERE applicantId = :applicantId " +
                        "AND courseId = :courseId " +
                        "AND Applicants_Courses.interested = 1;";
                List<Map<String, Object>> grades = conn.createQuery(sql)
                        .addParameter("applicantId", id)
                        .addParameter("courseId", course.getId())
                        .executeAndFetchTable()
                        .asList();
                String grade = (String) grades.get(0).get("grade");
                interestedCourses.put(course, grade);
            }
            applicant.setInterestedCourses(interestedCourses);

            // Add previous CA experience
            sql = "SELECT C.* " +
                    "FROM Applicants_Courses INNER JOIN Courses C " +
                    "ON Applicants_Courses.courseId = C.id " +
                    "WHERE Applicants_Courses.applicantId = :applicantId " +
                    "AND Applicants_Courses.previousCA = 1;";
            List<Course> previousCA = conn.createQuery(sql)
                    .addParameter("applicantId", id)
                    .executeAndFetch(Course.class);
            applicant.setPreviousCA(previousCA);

            // Get headCAInterest from joining table
            sql = "SELECT C.* " +
                    "FROM Applicants_Courses INNER JOIN Courses C " +
                    "ON Applicants_Courses.courseId = C.id " +
                    "WHERE Applicants_Courses.applicantId = :applicantId " +
                    "AND Applicants_Courses.headCAInterest = 1;";
            List<Course> headCAInterestCourses = conn.createQuery(sql)
                    .addParameter("applicantId", id)
                    .executeAndFetch(Course.class);
            applicant.setHeadCAInterest(headCAInterestCourses);

            // Get HiredCourse
            sql = "SELECT C.* " +
                    "FROM Applicants_Courses INNER JOIN Courses C " +
                    "ON Applicants_Courses.courseId = C.id " +
                    "WHERE Applicants_Courses.applicantId = :applicantId " +
                    "AND Applicants_Courses.hired = 1;";
            Course hiredCourse = (conn.createQuery(sql)
                    .addParameter("applicantId", id)
                    .executeAndFetchFirst(Course.class));
            
            applicant.setHiredCourse(hiredCourse);
            return applicant;

        } catch (Sql2oException e) {
            throw new DaoException("Unable to read applicant", e);
        }
    }

    public Applicant read(String jhed) throws DaoException {
        try (Connection conn = sql2o.open()) {
            String sql = "SELECT id, name, email, jhed, year, majorAndMinor, gpa, " +
                "registeredCredits, referenceEmail, resumeLink, fws, studentStatus, " +
                "mostRecentPayroll, otherJobs, hoursAvailable " +
                "FROM Applicants WHERE jhed = :jhed;";
            Applicant applicant = conn.createQuery(sql)
                    .addParameter("jhed", jhed)
                    .executeAndFetchFirst(Applicant.class);
            if (applicant == null) {
                return null;
            }

            int id = applicant.getId();

            // Get rankOne, rankTwo, and rankThree
            Course[] rankedCourses = readRankedCourses(id, conn);
            applicant.setRankOne(rankedCourses[0]);
            applicant.setRankTwo(rankedCourses[1]);
            applicant.setRankThree(rankedCourses[2]);

            // Get corresponding interestedCourses according to joining table
            sql = "SELECT C.* " +
                    "FROM Applicants_Courses INNER JOIN Courses C " +
                    "ON Applicants_Courses.courseId = C.id " +
                    "WHERE Applicants_Courses.applicantId = :applicantId " +
                    "AND Applicants_Courses.interested = 1;";
            List<Course> courses = conn.createQuery(sql)
                    .addParameter("applicantId", id)
                    .executeAndFetch(Course.class);

            // Initialize HashMap and append (Course, grade) pairs one by one
            HashMap<Course, String> interestedCourses = new HashMap<Course, String>();
            for (Course course : courses) {
                sql = "SELECT grade " +
                        "FROM Applicants_Courses " +
                        "WHERE applicantId = :applicantId " +
                        "AND courseId = :courseId " +
                        "AND interested = 1;";
                List<Map<String, Object>> grades = conn.createQuery(sql)
                        .addParameter("applicantId", id)
                        .addParameter("courseId", course.getId())
                        .executeAndFetchTable()
                        .asList();
                String grade = (String) grades.get(0).get("grade");
                interestedCourses.put(course, grade);
            }
            applicant.setInterestedCourses(interestedCourses);

            // Add previous CA experience
            sql = "SELECT C.* " +
                    "FROM Applicants_Courses INNER JOIN Courses C " +
                    "ON Applicants_Courses.courseId = C.id " +
                    "WHERE Applicants_Courses.applicantId = :applicantId " +
                    "AND Applicants_Courses.previousCA = 1;";
            List<Course> previousCA = conn.createQuery(sql)
                    .addParameter("applicantId", id)
                    .executeAndFetch(Course.class);
            applicant.setPreviousCA(previousCA);

            // headCAInterest Table
            sql = "SELECT C.* " +
                    "FROM Applicants_Courses INNER JOIN Courses C " +
                    "ON Applicants_Courses.courseId = C.id " +
                    "WHERE Applicants_Courses.applicantId = :applicantId " +
                    "AND Applicants_Courses.headCAInterest = 1;";
            List<Course> headCAInterestCourses = conn.createQuery(sql)
                    .addParameter("applicantId", id)
                    .executeAndFetch(Course.class);
            applicant.setHeadCAInterest(headCAInterestCourses);

            // Hired course
            sql = "SELECT C.* " +
                    "FROM Applicants_Courses INNER JOIN Courses C " +
                    "ON Applicants_Courses.courseId = C.id " +
                    "WHERE Applicants_Courses.applicantId = :applicantId " +
                    "AND Applicants_Courses.hired = 1;";
            Course hiredCourse = (conn.createQuery(sql)
                    .addParameter("applicantId", id)
                    .executeAndFetchFirst(Course.class));
            applicant.setHiredCourse(hiredCourse);
            return applicant;

        } catch (Sql2oException e) {
            throw new DaoException("Unable to read applicant", e);
        }
    }

    @Override
    public List<Applicant> findAll() throws DaoException {
        try (Connection conn = sql2o.open()) {
            String sql = "SELECT id, name, email, jhed, year, majorAndMinor, gpa, " +
                    "registeredCredits, referenceEmail, resumeLink, fws, studentStatus, " +
                    "mostRecentPayroll, otherJobs, hoursAvailable " +
                    "FROM Applicants WHERE jhed = :jhed;";
            List<Applicant> applicants = conn.createQuery(sql)
                    .executeAndFetch(Applicant.class);
            for (Applicant applicant : applicants) {
                int applicantId = applicant.getId();
                sql = "SELECT C.* " +
                        "FROM Applicants_Courses INNER JOIN Courses C " +
                        "ON Applicants_Courses.courseId = C.id " +
                        "WHERE Applicants_Courses.applicantId = :id " +
                        "AND Applicants_Courses.interested = 1;";
                List<Course> courses = conn.createQuery(sql)
                        .addParameter("id", applicantId)
                        .executeAndFetch(Course.class);

                //get rankOne, rankTwo, and rankThree
                Course[] rankedCourses = readRankedCourses(applicantId, conn);
                applicant.setRankOne(rankedCourses[0]);
                applicant.setRankTwo(rankedCourses[1]);
                applicant.setRankThree(rankedCourses[2]);

                // Initialize HashMap and append (Course, grade) pairs one by one
                HashMap<Course, String> interestedCourses = new HashMap<Course, String>();
                for (Course course : courses) {
                    sql = "SELECT grade " +
                            "FROM Applicants_Courses " +
                            "WHERE applicantId = :applicantId " +
                            "AND courseId = :courseId " +
                            "AND interested = 1;";
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
                        "FROM Applicants_Courses INNER JOIN Courses C " +
                        "ON Applicants_Courses.courseId = C.id " +
                        "WHERE Applicants_Courses.applicantId = :applicantId " +
                        "AND Applicants_Courses.hired = 1;";
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

    public List<Applicant> findByCourseId(int courseId) throws DaoException {
        try(Connection conn = sql2o.open()) {
            String sql = "SELECT A.id, A.name, A.email, A.jhed, A.year, A.majorAndMinor, A.gpa, " +
                    "A.registeredCredits, A.referenceEmail, A.resumeLink, A.fws," +
                    "A.studentStatus, A.mostRecentPayroll, A.otherJobs, A.hoursAvailable " +
                    "FROM Applicants A INNER JOIN Applicants_Courses " +
                    "ON A.id = Applicants_Courses.applicantId " +
                    "WHERE Applicants_Courses.courseId = :courseId " +
                    "AND Applicants_Courses.interested = 1;";
            return conn.createQuery(sql)
                    .addParameter("courseId", courseId)
                    .executeAndFetch(Applicant.class);
        } catch(Sql2oException e) {
            throw new DaoException("Unable to find applicants by course id", e);
        }
    }

    private void updateRankedCourseList(Applicant applicant, Connection conn) {
        String sql;
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
    }

    @NotNull
    private Course[] readRankedCourses(int id, Connection conn) {
        String sql;
        Course[] rankedCourses = new Course[3];
        sql = "SELECT C.* " +
                "FROM Courses C INNER JOIN Applicants A " +
                "ON C.id = A.rankOne " +
                "WHERE A.id = :id;";
        rankedCourses[0] = conn.createQuery(sql)
                .addParameter("id", id)
                .executeAndFetchFirst(Course.class);
        sql = "SELECT C.* " +
                "FROM Courses C INNER JOIN Applicants A " +
                "ON C.id = A.rankTwo " +
                "WHERE A.id = :id;";
        rankedCourses[1] = conn.createQuery(sql)
                .addParameter("id", id)
                .executeAndFetchFirst(Course.class);
        sql = "SELECT C.* " +
                "FROM Courses C INNER JOIN Applicants A " +
                "ON C.id = A.rankThree " +
                "WHERE A.id = :id;";
        rankedCourses[2] = conn.createQuery(sql)
                .addParameter("id", id)
                .executeAndFetchFirst(Course.class);
        return rankedCourses;
    }
}