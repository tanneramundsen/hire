package model;

public class Grade {
    String courseId;
    String courseName;
    String grade;

    public Grade(String courseId, String courseName, String grade) {
        this.courseId = courseId;
        this.courseName = courseName;
        this.grade = grade;
    }

    public String getCourseId() {
        return courseId;
    }

    public void setCourseId(String courseId) {
        this.courseId = courseId;
    }

    public String getCourseName() {
        return courseName;
    }

    public void setCourseName(String courseName) {
        this.courseName = courseName;
    }

    public String getGrade() {
        return grade;
    }

    public void setGrade(String grade) {
        this.grade = grade;
    }

    @Override
    public String toString() {
        return "Grade{" +
                "courseId='" + courseId + '\'' +
                ", courseName='" + courseName + '\'' +
                ", grade='" + grade + '\'' +
                '}';
    }
}
