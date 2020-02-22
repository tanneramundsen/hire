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
}
