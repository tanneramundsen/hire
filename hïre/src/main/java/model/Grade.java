package model;

import com.sun.org.apache.xpath.internal.operations.Bool;

public class Grade {
    String courseId;
    String courseName;
    String grade;
    Boolean headCAInterestBool;
    String headCAInterest;

    public Grade(String courseId, String courseName, String grade, String headCAInterest) {
        this.courseId = courseId;
        this.courseName = courseName;
        this.grade = grade;
        this.headCAInterest = headCAInterest;
        this.headCAInterestBool = headCAInterest.equals("Yes") ? true : false;
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

    public String getHeadCAInterest() { return headCAInterest; }

    public void setHeadCAInterest(String headCAInterest) {
        this.headCAInterest = headCAInterest;
        this.headCAInterestBool = headCAInterest.equals("Yes") ? true : false;
    }

}
