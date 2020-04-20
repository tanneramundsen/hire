package model;
import java.util.*;

public class StaffMember extends User {
    private int id;
    private String jhed;
    private List<Course> courses;
    boolean isAdmin;

    public List<Applicant> getCAsForCourse(Course course) {
        return null;
    }

    public void updateCourses() {

    }

    public StaffMember(String name, String jhed, List<Course> courses, boolean isAdmin) {
        this.name = name;
        this.jhed = jhed;
        this.courses = courses;
        this.isAdmin = isAdmin;
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

    public String getJhed() {
        return jhed;
    }

    public void setName(String name) { this.name = name; }

    public void setJhed(String jhed) {
        this.jhed = jhed;
    }

    public List<Course> getCourses() {
        return courses;
    }

    public void setCourses(List<Course> courses) {
        this.courses = courses;
    }

    public void setAdmin(boolean admin) { this.isAdmin = admin; }

    public boolean getAdmin() { return this.isAdmin; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof StaffMember)) return false;
        StaffMember that = (StaffMember) o;
        return Objects.equals(getJhed(), that.getJhed());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getJhed());
    }

    @Override
    public String toString() {
        return "StaffMember{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", jhed='" + jhed + '\'' +
                '}';
    }

}