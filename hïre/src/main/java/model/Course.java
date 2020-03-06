package model;
import java.util.*;

public class Course {
    int id;
    String name;
    String courseNumber;
    List<StaffMember> instructors;
    String semester;
    boolean hiringComplete;
    List<Applicant> hiredApplicants;
    List<Applicant> interestedApplicants;

    public Course(String name, String courseNumber, List<StaffMember> instructors, String semester,
                  boolean hiringComplete, List<Applicant> cas, List<Applicant> qualifiedStudents) {
        this.name = name;
        this.courseNumber = courseNumber;
        this.instructors = instructors;
        this.semester = semester;
        this.hiringComplete = hiringComplete;
        this.hiredApplicants = cas;
        this.interestedApplicants = qualifiedStudents;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
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

    public List<Applicant> getHiredApplicants() {
        return hiredApplicants;
    }

    public void setHiredApplicants(List<Applicant> hiredApplicants) {
        this.hiredApplicants = hiredApplicants;
    }

    public List<Applicant> getInterestedApplicants() {
        return interestedApplicants;
    }

    public void setQualifiedApplicants(List<Applicant> qualifiedApplicants) {
        this.interestedApplicants = qualifiedApplicants;
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

    @Override
    public String toString() {
        return "Course{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", courseNumber='" + courseNumber + '\'' +
                ", semester='" + semester + '\'' +
                ", hiringComplete=" + hiringComplete +
                '}';
    }
}