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
                sql = "INSERT INTO Applicants(name, email, jhed) " +
                        "VALUES(:name, :email, :jhed);";
                int id = (int) conn.createQuery(sql)
                        .addParameter("name", applicant.getName())
                        .addParameter("email", applicant.getEmail())
                        .addParameter("jhed", applicant.getJhed())
                        .executeUpdate()
                        .getKey();
                applicant.setId(id);
                if (applicant.getCoursesTakenGrades() != null) {
                    for (Map.Entry<Course,String> entry : applicant.getCoursesTakenGrades().entrySet()) {
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
            } catch (Sql2oException ex) {
                throw new DaoException("Unable to add the applicant", ex);
            }
        }
    }

    public void update(Applicant applicant) throws DaoException {
        try(Connection conn = sql2o.open()) {
            String sql = "UPDATE Applicants SET name = :name, email = :email, jhed = :jhed WHERE id = :id;";
            conn.createQuery(sql)
                    .addParameter("name", applicant.getName())
                    .addParameter("email", applicant.getEmail())
                    .addParameter("jhed", applicant.getJhed())
                    .addParameter("id", applicant.getId())
                    .executeUpdate();

            // Delete existing entries with this applicant in joining tables
            sql = "DELETE FROM InterestedApplicants_Courses WHERE applicantId = :applicantId;";
            conn.createQuery(sql)
                    .addParameter("applicantId", applicant.getId())
                    .executeUpdate();
            sql = "DELETE FROM HiredApplicants_Courses WHERE applicantId = :applicantId;";
            conn.createQuery(sql)
                    .addParameter("applicantId", applicant.getId())
                    .executeUpdate();

            // Fresh update to joining tables
            if (applicant.getCoursesTakenGrades() != null) {
                for (Map.Entry<Course,String> entry : applicant.getCoursesTakenGrades().entrySet()) {
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

                    sql = "INSERT INTO InterestedApplicants_Courses(applicantId, courseId, grade) " +
                            "VALUES(:applicantId, :courseId, :grade);";
                    conn.createQuery(sql)
                            .addParameter("applicantId", applicant.getId())
                            .addParameter("courseId", courseId)
                            .addParameter("grade", grade)
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
            String sql = "DELETE FROM InterestedApplicants_Courses WHERE applicantId = :applicantId;";
            conn.createQuery(sql)
                    .addParameter("applicantId", id)
                    .executeUpdate();
            sql = "DELETE FROM HiredApplicants_Courses WHERE applicantId = :applicantId;";
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
            String sql = "SELECT * FROM Applicants WHERE id = :id;";
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

            //get corresponding interestedCourses according to joining table
            sql = "SELECT Courses.* " +
                    "FROM InterestedApplicants_Courses INNER JOIN Courses " +
                    "ON InterestedApplicants_Courses.courseId = Courses.id " +
                    "WHERE InterestedApplicants_Courses.applicantId = :applicantId;";
            List<Course> courses = conn.createQuery(sql)
                    .addParameter("applicantId", id)
                    .executeAndFetch(Course.class);
            // TODO maybe make more efficient?
            // Initialize HashMap and append (Course, grade) pairs one by one
            HashMap<Course, String> coursesTakenGrades = new HashMap<Course, String>();
            for (Course course : courses) {
                sql = "SELECT grades " +
                        "FROM InterestedApplicants_Courses " +
                        "WHERE applicantId = :applicantId " +
                        "AND courseId = :courseId;";
                List<Map<String, Object>> grades = conn.createQuery(sql)
                        .addParameter("applicantId", id)
                        .addParameter("courseId", course.getId())
                        .executeAndFetchTable()
                        .asList();
                String grade = (String) grades.get(0).get("grade");
                coursesTakenGrades.put(course, grade);
            }
            applicant.setCoursesTakenGrades(coursesTakenGrades);

            sql = "SELECT Courses.* " +
                    "FROM HiredApplicants_Courses INNER JOIN Courses " +
                    "ON HiredApplicants_Courses.courseId = Courses.id " +
                    "WHERE HiredApplicants_Courses.applicantId = :applicantId;";
            List<Course> hiredCourses = (conn.createQuery(sql)
                    .addParameter("applicantId", id)
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

    public Applicant read(String jhed) throws DaoException {
        try (Connection conn = sql2o.open()) {
            String sql = "SELECT * FROM Applicants WHERE jhed = :jhed;";
            Applicant applicant = conn.createQuery(sql)
                    .addParameter("jhed", jhed)
                    .executeAndFetchFirst(Applicant.class);

            if (applicant == null) {
                return null;
            }

            // TODO: Figure out why executeAndFetch does not fill in name
            sql = "SELECT name FROM Applicants WHERE jhed = :jhed";
            List<Map<String, Object>> names = conn.createQuery(sql)
                    .addParameter("jhed", jhed).executeAndFetchTable().asList();
            applicant.setName((String) names.get(0).get("name"));

            //get corresponding interestedCourses according to joining table
            sql = "SELECT Courses.* " +
                    "FROM InterestedApplicants_Courses INNER JOIN Courses " +
                    "ON InterestedApplicants_Courses.courseId = Courses.id " +
                    "WHERE InterestedApplicants_Courses.applicantId = :applicantId;";
            List<Course> courses = conn.createQuery(sql)
                    .addParameter("applicantId", applicant.getId())
                    .executeAndFetch(Course.class);
            // TODO maybe make more efficient?
            // Initialize HashMap and append (Course, grade) pairs one by one
            HashMap<Course, String> coursesTakenGrades = new HashMap<Course, String>();
            for (Course course : courses) {
                sql = "SELECT grades " +
                        "FROM InterestedApplicants_Courses " +
                        "WHERE jhed = :jhed " +
                        "AND courseId = :courseId;";
                List<Map<String, Object>> grades = conn.createQuery(sql)
                        .addParameter("jhed", jhed)
                        .addParameter("courseId", course.getId())
                        .executeAndFetchTable()
                        .asList();
                String grade = (String) grades.get(0).get("grade");
                coursesTakenGrades.put(course, grade);
            }
            applicant.setCoursesTakenGrades(coursesTakenGrades);

            sql = "SELECT Courses.* " +
                    "FROM HiredApplicants_Courses INNER JOIN Courses " +
                    "ON HiredApplicants_Courses.courseId = Courses.id " +
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
                sql = "SELECT Courses.* " +
                        "FROM InterestedApplicants_Courses INNER JOIN Courses " +
                        "ON InterestedApplicants_Courses.courseId = Courses.id " +
                        "WHERE InterestedApplicants_Courses.applicantId = :id;";
                List<Course> courses = conn.createQuery(sql)
                        .addParameter("id", applicantId)
                        .executeAndFetch(Course.class);
                // TODO maybe make more efficient?
                // Initialize HashMap and append (Course, grade) pairs one by one
                HashMap<Course, String> coursesTakenGrades = new HashMap<Course, String>();
                for (Course course : courses) {
                    sql = "SELECT grades " +
                            "FROM InterestedApplicants_Courses " +
                            "WHERE applicantId = :applicantId " +
                            "AND courseId = :courseId;";
                    List<Map<String, Object>> grades = conn.createQuery(sql)
                            .addParameter("applicantId", applicantId)
                            .addParameter("courseId", course.getId())
                            .executeAndFetchTable()
                            .asList();
                    String grade = (String) grades.get(0).get("grade");
                    coursesTakenGrades.put(course, grade);
                }
                applicant.setCoursesTakenGrades(coursesTakenGrades);

                sql = "SELECT Courses.* " +
                        "FROM HiredApplicants_Courses INNER JOIN Courses " +
                        "ON HiredApplicants_Courses.courseId = Courses.id " +
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

    @Override
    public List<Applicant> findByCourseId(int courseId) throws DaoException{
        try(Connection conn = sql2o.open()) {
            String sql = "SELECT Applicants.* " +
                    "FROM Applicants " +
                    "INNER JOIN InterestedApplicants_Courses " +
                    "ON Applicants.id = InterestedApplicants_Courses.applicantId " +
                    "WHERE InterestedApplicants_Courses.courseId = :courseId";
            return conn.createQuery(sql)
                    .addParameter("courseId", courseId)
                    .executeAndFetch(Applicant.class);
        } catch(Sql2oException e) {
            throw new DaoException("Unable to find applicants by course id", e);
        }
    }


}