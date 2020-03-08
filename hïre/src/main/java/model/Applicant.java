package model;
import java.util.*;

public class Applicant extends User {
    int id;
    String name;
    String email;
    String jhed;
    Course hiredCourse;
    HashMap<Course, String> interestedCourses;

    @Override
    public void updateCourses() {

    }

    String getApplicationSummary() {

        return null;
    }

    public Applicant(String name, String email, String jhed, HashMap<Course, String> interestedCourses) {
        this.name = name;
        this.email = email;
        this.jhed = jhed;
        this.interestedCourses = interestedCourses;
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

    // TODO: return list of keys from hashmap
    public List<Course> getCoursesList() {
        List<Course> courseList = new ArrayList<Course>();
        courseList.addAll(interestedCourses.keySet());
        return courseList;
    }

    public HashMap<Course, String> getInterestedCourses() {
        return interestedCourses;
    }

    public void setInterestedCourses(HashMap<Course, String> interestedCourses) {
        this.interestedCourses = interestedCourses;
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