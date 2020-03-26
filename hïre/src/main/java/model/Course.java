package model;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class Course implements Comparable<Course> {
    int id;
    String name;
    String courseNumber;
    List<StaffMember> instructors;
    String semester;
    boolean hiringComplete;
    String courseDescription;
    String interviewLink;
    List<Applicant> hiredApplicants;
    List<Applicant> interestedApplicants;
    List<Applicant> shortlistedApplicants;

    public Course(String name, String courseNumber, List<StaffMember> instructors, String semester,
                  boolean hiringComplete, List<Applicant> cas, List<Applicant> interestedApplicants) {
        this.name = name;
        this.courseNumber = courseNumber;
        this.instructors = instructors;
        this.semester = semester;
        this.hiringComplete = hiringComplete;
        this.hiredApplicants = cas;
        this.interestedApplicants = interestedApplicants;
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

    public String getCourseDescription() { return courseDescription; }

    public void setCourseDescription(String courseDescription) { this.courseDescription = courseDescription; }

    public String getInterviewLink() { return interviewLink; }

    public void setInterviewLink(String interviewLink) { this.interviewLink = interviewLink; }

    public List<Applicant> getHiredApplicants() {
        return hiredApplicants;
    }

    public void setHiredApplicants(List<Applicant> hiredApplicants) {
        this.hiredApplicants = hiredApplicants;
    }

    public List<Applicant> getInterestedApplicants() {
        return interestedApplicants;
    }

    public void setInterestedApplicants(List<Applicant> interestedApplicants) {
        this.interestedApplicants = interestedApplicants;
    }

    public List<Applicant> getShortlistedApplicants() {
        return shortlistedApplicants;
    }

    public void setShortlistedApplicants(List<Applicant> shortlistedApplicants) {
        this.shortlistedApplicants = shortlistedApplicants;
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

    @Override
    public int compareTo(@NotNull Course o) {
        return name.compareTo(o.name);
    }
}