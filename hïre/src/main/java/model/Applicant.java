package model;
import java.util.*;

public class Applicant extends User {
    int id;
    String name;
    String email;
    String jhed;
    Course hiredCourse;
    List<Course> eligibleCourses;

    @Override
    public void updateCourses() {

    }

    String getApplicationSummary() {

        return null;
    }

    public Applicant(String name, String email, String jhed, List<Course> eligibleCourses) {
        this.name = name;
        this.email = email;
        this.jhed = jhed;
        this.eligibleCourses = eligibleCourses;
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

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getJhed() {
        return jhed;
    }

    public void setJhed(String jhed) {
        this.jhed = jhed;
    }

    public List<Course> getEligibleCourses() {
        return eligibleCourses;
    }

    public void setEligibleCourses(List<Course> eligibleCourses) {
        this.eligibleCourses = eligibleCourses;
    }

    public Course getHiredCourse() {
        return hiredCourse;
    }

    public void setHiredCourse(Course hiredCourse) {
        this.hiredCourse = hiredCourse;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Applicant)) return false;
        Applicant applicant = (Applicant) o;
        return Objects.equals(getJhed(), applicant.getJhed());
    }

    @Override
    public int hashCode() { return Objects.hash(getJhed()); }

    @Override
    public String toString() {
        return "Applicant{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", email='" + email + '\'' +
                ", jhed='" + jhed + '\'' +
                '}';
    }
}
