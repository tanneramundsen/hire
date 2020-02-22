package model;
import java.util.*;

public class Course {
    String name;
    String courseNumber;
    List<StaffMember> instructors;
    String semester;
    boolean hiringComplete;
    List<Applicant> cas;
    List<Applicant> qualifiedStudents;

    public Course(String name, String courseNumber, List<StaffMember> instructors, String semester,
                  boolean hiringComplete, List<Applicant> cas, List<Applicant> qualifiedStudents) {
        this.name = name;
        this.courseNumber = courseNumber;
        this.instructors = instructors;
        this.semester = semester;
        this.hiringComplete = hiringComplete;
        this.cas = cas;
        this.qualifiedStudents = qualifiedStudents;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCourseNumber() {
        return courseNumber;
    }

    public void setCourseNumber(String courseNumber) {
        this.courseNumber = courseNumber;
    }

    public List<StaffMember> getInstructors() {
        return instructors;
    }

    public void setInstructors(List<StaffMember> instructors) {
        this.instructors = instructors;
    }

    public String getSemester() {
        return semester;
    }

    public void setSemester(String semester) {
        this.semester = semester;
    }

    public boolean isHiringComplete() {
        return hiringComplete;
    }

    public void setHiringComplete(boolean hiringComplete) {
        this.hiringComplete = hiringComplete;
    }

    public List<Applicant> getCas() {
        return cas;
    }

    public void setCas(List<Applicant> cas) {
        this.cas = cas;
    }

    public List<Applicant> getQualifiedStudents() {
        return qualifiedStudents;
    }

    public void setQualifiedStudents(List<Applicant> qualifiedStudents) {
        this.qualifiedStudents = qualifiedStudents;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Course)) return false;
        Course course = (Course) o;
        return Objects.equals(getName(), course.getName());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getName());
    }
}
