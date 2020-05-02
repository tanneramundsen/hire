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

/**
 * DAO object to abstract away interactions between the database tables
 * and the rest of the application
 */
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
                int id = (int) conn.createQuery(sql, true)
                        .bind(applicant)
                        .executeUpdate()
                        .getKey();
                applicant.setId(id);

                updateRankedCourseList(conn, applicant);

                if (applicant.getInterestedCourses() != null) {
                    for (Map.Entry<Course,String> entry : applicant.getInterestedCourses().entrySet()) {
                        Course course = entry.getKey();
                        String grade = entry.getValue();
                        int courseId = course.getId();
                        if (courseId == 0) {
                            sql = "INSERT INTO Courses(name, courseNumber, semester, hiringComplete, courseDescription, " +
                                    "interviewLink) VALUES(:name, :courseNumber, :semester, :hiringComplete, :courseDescription, " +
                                    ":interviewLink);";
                            courseId = (int) conn.createQuery(sql, true)
                                    .bind(course)
                                    .executeUpdate()
                                    .getKey();
                            course.setId(courseId);
                        }
                        sql = "INSERT INTO Applicants_Courses(applicantId, courseId, interested, grade) " +
                                "VALUES(:applicantId, :courseId, true, :grade) " +
                                "ON CONFLICT (applicantId, courseId) " +
                                "DO UPDATE SET interested = true, grade = excluded.grade;";
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
                            courseId = (int) conn.createQuery(sql, true)
                                    .bind(course)
                                    .executeUpdate()
                                    .getKey();
                            course.setId(courseId);
                        }

                        sql = "INSERT INTO Applicants_Courses(applicantId, courseId, previousCA) " +
                                "VALUES(:applicantId, :courseId, true) " +
                                "ON CONFLICT (applicantId, courseId) " +
                                "DO UPDATE SET previousCA = true;";
                        conn.createQuery(sql)
                                .addParameter("applicantId", id)
                                .addParameter("courseId", courseId)
                                .executeUpdate();
                    }
                }

                if (applicant.getHeadCAInterest() != null) {
                    for (Course course: applicant.getHeadCAInterest()) {
                        int courseId = course.getId();
                        if (courseId == 0) {
                            sql = "INSERT INTO Courses(name, courseNumber, semester, hiringComplete, courseDescription interviewLink) " +
                                    "VALUES(:name, :courseNumber, :semester, :hiringComplete, :courseDescription, :interviewLink);";
                            courseId = (int) conn.createQuery(sql, true)
                                    .bind(course)
                                    .executeUpdate()
                                    .getKey();
                            course.setId(courseId);
                        }

                        sql = "INSERT INTO Applicants_Courses(applicantId, courseId, headCAInterest) " +
                                "VALUES(:applicantId, :courseId, true) " +
                                "ON CONFLICT (applicantId, courseId) " +
                                "DO UPDATE SET headCAInterest = true;";
                        conn.createQuery(sql)
                                .addParameter("applicantId", id)
                                .addParameter("courseId", courseId)
                                .executeUpdate();
                    }
                }

                if (applicant.getHiredCourse() != null) {
                    Course course = applicant.getHiredCourse();
                    int courseId = course.getId();
                    if (courseId == 0) {
                        sql = "INSERT INTO Courses(name, courseNumber, semester, hiringComplete, courseDescription, interviewLink) " +
                                "VALUES(:name, :courseNumber, :semester, :hiringComplete, :courseDescription, :interviewLink);";
                        courseId = (int) conn.createQuery(sql, true)
                                .bind(course)
                                .executeUpdate()
                                .getKey();
                        course.setId(courseId);
                    }

                    sql = "INSERT INTO Applicants_Courses(applicantId, courseId, hired) " +
                            "VALUES(:applicantId, :courseId, true) " +
                            "ON CONFLICT (applicantId, courseId) " +
                            "DO UPDATE SET hired = true;";
                    conn.createQuery(sql)
                            .addParameter("applicantId", id)
                            .addParameter("courseId", courseId)
                            .executeUpdate();
                }
            } catch (Sql2oException ex) {
                throw new DaoException("Unable to add the applicant", ex);
            }
        }
    }

    public Applicant read(int id) throws DaoException {
        try (Connection conn = sql2o.open()) {
            String sql = "SELECT id, name, email, jhed, year, majorAndMinor, gpa, " +
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

            // Get rankOne, rankTwo, and rankThree
            Course[] rankedCourses = readRankedCourses(conn, id);
            applicant.setRankOne(rankedCourses[0]);
            applicant.setRankTwo(rankedCourses[1]);
            applicant.setRankThree(rankedCourses[2]);

            // Get corresponding interestedCourses according to joining table
            List<Course> courses = readInterestedCourses(conn, id);

            // Initialize HashMap and append (Course, grade) pairs one by one
            HashMap<Course, String> interestedCourses = new HashMap<>();
            for (Course course : courses) {
                sql = "SELECT grade " +
                        "FROM Applicants_Courses " +
                        "WHERE applicantId = :applicantId " +
                        "AND courseId = :courseId " +
                        "AND interested = true;";
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
            List<Course> previousCA = readPreviousCACourses(conn, id);
            applicant.setPreviousCA(previousCA);

            // headCAInterest Table
            List<Course> headCAInterestCourses = readHeadCAInterestCourses(conn, id);
            applicant.setHeadCAInterest(headCAInterestCourses);

            // Hired course
            Course hiredCourse = readHiredCourse(conn, id);
            applicant.setHiredCourse(hiredCourse);
            return applicant;


        } catch (Sql2oException e) {
            throw new DaoException("Unable to read applicant", e);
        }
    }

    /**
     * Obtain and load Applicant information that corresponds to a specified jhed
     * into an Applicant POJO.
     * @param jhed integer corresponding to which Applicant to fetch from
     *           Applicants table
     * @return Applicant corresponding to the specified jhed or null
     * @throws DaoException Runtime exception due to failed SQL query
     */
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
            Course[] rankedCourses = readRankedCourses(conn, id);
            applicant.setRankOne(rankedCourses[0]);
            applicant.setRankTwo(rankedCourses[1]);
            applicant.setRankThree(rankedCourses[2]);

            // Get corresponding interestedCourses according to joining table
            List<Course> courses = readInterestedCourses(conn, id);

            // Initialize HashMap and append (Course, grade) pairs one by one
            HashMap<Course, String> interestedCourses = new HashMap<>();
            for (Course course : courses) {
                sql = "SELECT grade " +
                        "FROM Applicants_Courses " +
                        "WHERE applicantId = :applicantId " +
                        "AND courseId = :courseId " +
                        "AND interested = true;";
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
            List<Course> previousCA = readPreviousCACourses(conn, id);
            applicant.setPreviousCA(previousCA);

            // headCAInterest Table
            List<Course> headCAInterestCourses = readHeadCAInterestCourses(conn, id);
            applicant.setHeadCAInterest(headCAInterestCourses);

            // Hired course
            Course hiredCourse = readHiredCourse(conn, id);
            applicant.setHiredCourse(hiredCourse);
            return applicant;

        } catch (Sql2oException e) {
            throw new DaoException("Unable to read applicant", e);
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
                    .addParameter("email", applicant.getEmail())
                    .executeUpdate();

            int id = applicant.getId();

            // Update rankOne, rankTwo, rankThree
            updateRankedCourseList(conn, applicant);

            // Read currently interested courses for this applicant in the database
            // and change to not interested. We will then change them back to
            // interested if the course is still in the interestedCourses list of
            // the applicant Java object
            List<Course> currentInterestedCourses = readInterestedCourses(conn, id);
            if (currentInterestedCourses != null) {
                for (Course course: currentInterestedCourses) {
                    sql = "UPDATE Applicants_Courses " +
                            "SET interested = false " +
                            "WHERE applicantId = :applicantId " +
                            "AND courseId = :courseId;";

                    conn.createQuery(sql)
                            .addParameter("applicantId", id)
                            .addParameter("courseId", course.getId())
                            .executeUpdate();
                }
            }

            // Update database to reflect interestedCourses list of the
            // applicant Java object
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
                        courseId = (int) conn.createQuery(sql, true)
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
                            "VALUES(:applicantId, :courseId, true, :grade) " +
                            "ON CONFLICT (applicantId, courseId) " +
                            "DO UPDATE SET interested = true, grade = excluded.grade;";
                    conn.createQuery(sql)
                            .addParameter("applicantId", id)
                            .addParameter("courseId", interestedCourses.get(i).getId())
                            .addParameter("grade", gradesInterested.get(i))
                            .executeUpdate();
                }
            }

            // Read previously CA'd courses for this applicant in the database
            // and change to not previously CA'd. We will then change them back
            // to previously CA'd if the course is still in the previousCA list of
            // the applicant Java object
            List<Course> currentPreviousCA = readPreviousCACourses(conn, id);
            if (currentPreviousCA != null) {
                for (Course course: currentPreviousCA) {
                    sql = "UPDATE Applicants_Courses " +
                            "SET previousCA = false " +
                            "WHERE applicantId = :applicantId " +
                            "AND courseId = :courseId;";
                    conn.createQuery(sql)
                            .addParameter("applicantId", id)
                            .addParameter("courseId", course.getId())
                            .executeUpdate();
                }
            }

            // Update database to reflect previousCA list of the
            // applicant Java object
            if (applicant.getPreviousCA() != null) {
                for (Course course: applicant.getPreviousCA()) {
                    int courseId = course.getId();
                    if (courseId == 0) {
                        sql = "INSERT INTO Courses(name, courseNumber, semester, hiringComplete, courseDescription, " +
                                "interviewLink) VALUES(:name, :courseNumber, :semester, :hiringComplete, :courseDescription, " +
                                ":interviewLink);";
                        courseId = (int) conn.createQuery(sql, true)
                                .bind(course)
                                .executeUpdate()
                                .getKey();
                        course.setId(courseId);
                    }

                    sql = "INSERT INTO Applicants_Courses(applicantId, courseId, previousCA) " +
                            "VALUES(:applicantId, :courseId, true) " +
                            "ON CONFLICT (applicantId, courseId) " +
                            "DO UPDATE SET previousCA = true;";
                    conn.createQuery(sql)
                            .addParameter("applicantId", id)
                            .addParameter("courseId", courseId)
                            .executeUpdate();
                }
            }

            // Read courses for which this applicant expressed head CA interest
            // from the database and change to not interested. We will then
            // change them back to interested if the course is still in the
            // headCAInterest list of the applicant Java object
            List<Course> currentHeadCAInterest = readHeadCAInterestCourses(conn, id);
            if (currentHeadCAInterest != null) {
                for (Course course: currentHeadCAInterest) {
                    sql = "UPDATE Applicants_Courses " +
                            "SET headCAInterest = false " +
                            "WHERE applicantId = :applicantId " +
                            "AND courseId = :courseId;";
                    conn.createQuery(sql)
                            .addParameter("applicantId", id)
                            .addParameter("courseId", course.getId())
                            .executeUpdate();
                }
            }

            // Update database to reflect headCAInterest list of the
            // applicant Java object
            if (applicant.getHeadCAInterest() != null) {
                for (Course course: applicant.getHeadCAInterest()) {
                    int courseId = course.getId();
                    if (courseId == 0) {
                        sql = "INSERT INTO Courses(name, courseNumber, semester, hiringComplete, courseDescription, " +
                                "interviewLink) VALUES(:name, :courseNumber, :semester, :hiringComplete, :courseDescription, " +
                                ":interviewLink);";
                        courseId = (int) conn.createQuery(sql, true)
                                .bind(course)
                                .executeUpdate()
                                .getKey();
                        course.setId(courseId);
                    }
                    sql = "INSERT INTO Applicants_Courses(applicantId, courseId, headCAInterest) " +
                            "VALUES(:applicantId, :courseId, true) " +
                            "ON CONFLICT (applicantId, courseId) " +
                            "DO UPDATE SET headCAInterest = true;";
                    conn.createQuery(sql)
                            .addParameter("applicantId", id)
                            .addParameter("courseId", courseId)
                            .executeUpdate();
                }
            }

            // Read hired course of this applicant from the database and change
            // to not hired. We will then change it back to hired if the course
            // is still in the hiredCourse of the current applicant Java object.
            Course currentHired = readHiredCourse(conn, id);
            if (currentHired != null) {
                sql = "UPDATE Applicants_Courses " +
                        "SET hired = false " +
                        "WHERE applicantId = :applicantId " +
                        "AND courseId = :courseId;";
                conn.createQuery(sql)
                        .addParameter("applicantId", id)
                        .addParameter("courseId", currentHired.getId())
                        .executeUpdate();
            }

            // Update database to reflect hired course of the applicant Java object
            Course hiredCourse = applicant.getHiredCourse();
            if (hiredCourse != null) {
                int hiredCourseId = hiredCourse.getId();

                if (hiredCourseId == 0) {
                    sql = "INSERT INTO Courses(name, courseNumber, semester, hiringComplete, courseDescription, interviewLink) " +
                            "VALUES(:name, :courseNumber, :semester, :hiringComplete, :courseDescription, :interviewLink);";
                    hiredCourseId = (int) conn.createQuery(sql, true)
                            .bind(hiredCourse)
                            .executeUpdate()
                            .getKey();

                    hiredCourse.setId(hiredCourseId);
                }
                sql = "INSERT INTO Applicants_Courses(applicantId, courseId, hired) " +
                        "VALUES(:applicantId, :courseId, true) " +
                        "ON CONFLICT (applicantId, courseId) " +
                        "DO UPDATE SET hired = true;";
                conn.createQuery(sql)
                        .addParameter("applicantId", id)
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

    @Override
    public List<Applicant> findAll() throws DaoException {
        try (Connection conn = sql2o.open()) {
            String sql = "SELECT * FROM Applicants";
            List<Applicant> applicants = conn.createQuery(sql)
                    .executeAndFetch(Applicant.class);
            for (Applicant applicant : applicants) {
                int applicantId = applicant.getId();
                List<Course> courses = readInterestedCourses(conn, applicantId);

                //get rankOne, rankTwo, and rankThree
                Course[] rankedCourses = readRankedCourses(conn, applicantId);
                applicant.setRankOne(rankedCourses[0]);
                applicant.setRankTwo(rankedCourses[1]);
                applicant.setRankThree(rankedCourses[2]);

                // Initialize HashMap and append (Course, grade) pairs one by one
                HashMap<Course, String> interestedCourses = new HashMap<>();
                for (Course course : courses) {
                    sql = "SELECT grade " +
                            "FROM Applicants_Courses " +
                            "WHERE applicantId = :applicantId " +
                            "AND courseId = :courseId " +
                            "AND interested = true;";
                    List<Map<String, Object>> grades = conn.createQuery(sql)
                            .addParameter("applicantId", applicantId)
                            .addParameter("courseId", course.getId())
                            .executeAndFetchTable()
                            .asList();
                    String grade = (String) grades.get(0).get("grade");
                    interestedCourses.put(course, grade);
                }
                applicant.setInterestedCourses(interestedCourses);

                // Previous CA experience
                List<Course> previousCA = readPreviousCACourses(conn, applicantId);
                if (previousCA != null) {
                    applicant.setPreviousCA(previousCA);
                }

                // Hired course
                Course hiredCourse = readHiredCourse(conn, applicantId);
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
                    "AND Applicants_Courses.interested = true;";
            return conn.createQuery(sql)
                    .addParameter("courseId", courseId)
                    .executeAndFetch(Applicant.class);
        } catch(Sql2oException e) {
            throw new DaoException("Unable to find applicants by course id", e);
        }
    }

    /**
     * Helper method to update ranked preferences of an applicant
     * @param conn SQL connection object to database
     * @param applicant Applicant object with specified ranked preferred courses
     */
    private void updateRankedCourseList(Connection conn, Applicant applicant) {
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

    /**
     * Obtain the ranked preferred courses for an applicant based on their id.
     * @param conn SQL connection object to database
     * @param id Applicant's id
     * @return list of ranked courses for the Applicant
     */
    @NotNull
    private Course[] readRankedCourses(Connection conn, int id) {
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

    /**
     * Obtain the courses that a specified Applicant is interested in.
     * @param conn SQL connection object to database
     * @param applicantId id of Applicant
     * @return list of courses that the Applicant is interested in
     */
    private List<Course> readInterestedCourses(Connection conn, int applicantId) {
        String sql = "SELECT C.* " +
                "FROM Applicants_Courses INNER JOIN Courses C " +
                "ON Applicants_Courses.courseId = C.id " +
                "WHERE Applicants_Courses.applicantId = :applicantId " +
                "AND Applicants_Courses.interested = true;";

        List<Course> interestedCourses = conn.createQuery(sql)
                .addParameter("applicantId", applicantId)
                .executeAndFetch(Course.class);

        return interestedCourses;
    }

    /**
     * Obtain the courses that a specified Applicant has previously CA'd for.
     * @param conn SQL connection object to database
     * @param applicantId id of Applicant
     * @return list of courses that the Applicant previously CA'd for
     */
    private List<Course> readPreviousCACourses(Connection conn, int applicantId) {
        String sql = "SELECT C.* " +
                "FROM Applicants_Courses INNER JOIN Courses C " +
                "ON Applicants_Courses.courseId = C.id " +
                "WHERE Applicants_Courses.applicantId = :applicantId " +
                "AND Applicants_Courses.previousCA = true;";

        List<Course> previousCA = conn.createQuery(sql)
                .addParameter("applicantId", applicantId)
                .executeAndFetch(Course.class);

        return previousCA;
    }

    /**
     * Obtain the courses for which a specified Applicant expressed interest
     * in being a Head CA.
     * @param conn SQL connection object to database
     * @param applicantId id of Applicant
     * @return list of courses that the Applicant expressed interested in being
     * a head CA for
     */
    private List<Course> readHeadCAInterestCourses(Connection conn, int applicantId) {
        String sql = "SELECT C.* " +
                "FROM Applicants_Courses INNER JOIN Courses C " +
                "ON Applicants_Courses.courseId = C.id " +
                "WHERE Applicants_Courses.applicantId = :applicantId " +
                "AND Applicants_Courses.headCAInterest = true;";

        List<Course> headCAInterestCourses = conn.createQuery(sql)
                .addParameter("applicantId", applicantId)
                .executeAndFetch(Course.class);

        return headCAInterestCourses;
    }

    /**
     * Obtain the course that a specified Applicant is hired for.
     * @param conn SQL connection object to database
     * @param applicantId id of Applicant
     * @return a single course that the Applicant has been hired for
     */
    private Course readHiredCourse(Connection conn, int applicantId) {
        String sql = "SELECT C.* " +
                "FROM Applicants_Courses INNER JOIN Courses C " +
                "ON Applicants_Courses.courseId = C.id " +
                "WHERE Applicants_Courses.applicantId = :applicantId " +
                "AND Applicants_Courses.hired = true;";

        Course hired = conn.createQuery(sql)
                .addParameter("applicantId", applicantId)
                .executeAndFetchFirst(Course.class);
        return hired;
    }
}