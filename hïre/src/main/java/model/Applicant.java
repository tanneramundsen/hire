package model;
import java.util.*;

public class Applicant extends User {
    private int id;
    private String email;
    private String jhed;
    private Course hiredCourse;
    private HashMap<Course, String> interestedCourses;
    private Course rankOne = null;
    private Course rankTwo = null;
    private Course rankThree = null;

    //Supplementary Info To be Visible to Staff and Admin
    private String year;
    private String majorAndMinor;
    private Double gpa;
    private Double registeredCredits;
    private String referenceEmail;
    private String resumeLink;
    private List<Course> headCAInterest;
    private List<Course> previousCA;

    //Fields only visible to Admin
    private boolean fws;
    private String studentStatus;
    private String mostRecentPayroll;
    private String otherJobs;
    private int hoursAvailable;



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

    public Course getRankOne() { return rankOne; }

    public void setRankOne(Course rankOne) { this.rankOne = rankOne; }

    public Course getRankTwo() { return rankTwo; }

    public void setRankTwo(Course rankTwo) { this.rankTwo = rankTwo; }

    public Course getRankThree() { return rankThree; }

    public void setRankThree(Course rankThree) { this.rankThree = rankThree; }

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

    public String getYear() { return year; }

    public void setYear(String year) { this.year = year; }

    public String getMajorAndMinor() { return majorAndMinor; }

    public void setMajorAndMinor(String majorAndMinor) { this.majorAndMinor = majorAndMinor; }

    public Double getGpa() {return gpa;}

    public void setGpa(Double gpa) { this.gpa = gpa;}

    public Double getRegisteredCredits() { return registeredCredits; }

    public void setRegisteredCredits(Double registeredCredits) { this.registeredCredits = registeredCredits; }

    public String getReferenceEmail() { return referenceEmail; }

    public void setReferenceEmail(String referenceEmail) { this.referenceEmail = referenceEmail; }

    public String getResumeLink() { return resumeLink; }

    public void setResumeLink(String resumeLink) { this.resumeLink = resumeLink; }

    public List<Course> getHeadCAInterest() { return headCAInterest; }

    public void setHeadCAInterest(List<Course> headCAInterest) { this.headCAInterest = headCAInterest; }

    public boolean getFws() { return fws; }

    public void setFws(boolean fws) { this.fws = fws; }

    public String getStudentStatus() { return studentStatus; }

    public void setStudentStatus(String studentStatus) { this.studentStatus = studentStatus; }

    public String getMostRecentPayroll() { return mostRecentPayroll; }

    public void setMostRecentPayroll(String mostRecentPayroll) { this.mostRecentPayroll = mostRecentPayroll; }

    public String getOtherJobs() { return otherJobs; }

    public void setOtherJobs(String otherJobs) { this.otherJobs = otherJobs; }

    public int getHoursAvailable() { return hoursAvailable; }

    public void setHoursAvailable(int hoursAvailable) { this.hoursAvailable = hoursAvailable; }

    public void setPreviousCA(List<Course> previousCA) { this.previousCA = previousCA; }

    public List<Course> getPreviousCA() { return previousCA; }

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