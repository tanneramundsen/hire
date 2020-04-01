import api.ApiServer;
import dao.*;
import model.Applicant;
import model.Course;
import model.Grade;
import model.StaffMember;
import spark.ModelAndView;
import spark.template.handlebars.HandlebarsTemplateEngine;

import java.nio.file.Paths;
import java.util.*;

import static spark.Spark.*;

public class WebServer {
    public static void main(String[] args) {
        DaoFactory.DROP_TABLES_IF_EXIST = true;
        DaoFactory.PATH_TO_DATABASE_FILE = Paths.get("src", "main", "resources").toFile().getAbsolutePath()
                + "/db/Store.db";
        ApiServer.INITIALIZE_WITH_SAMPLE_DATA = true;

        Sql2oStaffMemberDao staffMemberDao = DaoFactory.getStaffMemberDao();
        Sql2oApplicantDao applicantDao = DaoFactory.getApplicantDao();
        Sql2oCourseDao courseDao = DaoFactory.getCourseDao();

        // Add in all courses from SIS API to Courses database
        String school = "whiting school of engineering".replace(" ", "%20");;
        String dept = "EN computer science".replace(" ", "%20");;
        String key = "R6HJMT7GFtXsTjRcjp4zrypfpNpq4108";
        String url = "https://sis.jhu.edu/api/classes/" + school + "/" + dept + "/current?key=" + key;
        List<Course> all_courses = DaoUtil.addSISCourses(courseDao,url);
        // Add in sample applicants
        DaoUtil.addSampleApplicants(courseDao, applicantDao);

        staticFileLocation("/templates");

        before(((request, response) -> {
            if (request.cookie("jhed") != null) {
                request.attribute("jhed", request.cookie("jhed"));
            }
        }));

        get("/", (request, response) -> {
            // TODO: remove cookie username?
            Map<String, String> model = new HashMap<>();
            model.put("jhed", request.attribute("jhed"));
            return new ModelAndView(model, "index.hbs");
        }, new HandlebarsTemplateEngine());

        get("/login", (request, response) -> {
            return new ModelAndView(new HashMap(), "login.hbs");
        }, new HandlebarsTemplateEngine());

        post("/login", (request, response) -> {
            String jhed = request.queryParams("jhed");
            String profileType = request.queryParams("profileType");

            // Store info as cookies
            response.cookie("jhed", jhed);
            response.cookie("profileType", profileType);

            // Go to landing page
            response.redirect("/landing");
            return null;
        }, new HandlebarsTemplateEngine());

        get("/signout", ((request, response) -> {
            response.removeCookie("jhed");
            response.redirect("/");
            return null;
        }), new HandlebarsTemplateEngine());

        get("/signup", (request, response) -> {
            Map<String, Object> model = new HashMap<String, Object>();
            model.put("allCourses", courseDao.findAll());
            return new ModelAndView(model, "signup.hbs");
        }, new HandlebarsTemplateEngine());

        post("/signup", (request, response) -> {
            String name = request.queryParams("firstName")+" "+request.queryParams("lastName");
            String jhed = request.queryParams("jhed");
            String email = jhed + "@jhu.edu";
            String profileType = request.queryParams("profileType");
            String[] courses = request.queryParamsValues("courses");

            // use information to create either an applicant or staff member
            if (profileType.equals("Professor")) {
                List<Course> courseList = new ArrayList<Course>();
                for (String course:courses) {
                    Course newCourse = courseDao.read(course);
                    courseList.add(newCourse);
                }
                StaffMember s = new StaffMember(name,jhed,courseList);
                staffMemberDao.add(s);
                // Update courses to have staff member as instructor
                for (Course c: courseList) {
                    List<StaffMember> instructors = c.getInstructors();
                    instructors.add(s);
                    c.setInstructors(instructors);
                   // TODO: why doesn't this work
                    courseDao.update(c);
                }
            } else {
                HashMap<Course, String> coursesHashMap = new HashMap<Course, String>();
                for (String course:courses) {
                    Course newCourse = courseDao.read(course);
                    coursesHashMap.put(newCourse, "Not Taken");
                }
                applicantDao.add(new Applicant(name,email,jhed,coursesHashMap));
            }

            response.cookie("jhed", jhed);
            response.cookie("profileType", profileType);
            response.redirect("/landing");
            return null;
        }, new HandlebarsTemplateEngine());

        get("/landing", (request, response) -> {
            Map<String, Object> model = new HashMap<String, Object>();
            String jhed = request.cookie("jhed");
            String profileType = request.cookie("profileType");
            String name = null;
            List<Course> courseList = new ArrayList<>();
            boolean isStaffMember = false;

            // Redirect back to login if information not in database
            if (profileType.equals("Professor")) {
                isStaffMember = true;
                StaffMember sm = staffMemberDao.read(jhed);
                if (sm == null) {
                    response.redirect("/login");
                } else {
                    name = sm.getName();
                    courseList = sm.getCourses();
                }
            } else {
                Applicant a = applicantDao.read(jhed);
                if (a == null) {
                    response.redirect("/login");
                } else {
                    name = a.getName();
                    courseList = a.getCoursesList();
                }
            }
            model.put("name", name);
            model.put("courseList", courseList);
            model.put("isStaffMember", isStaffMember);

            return new ModelAndView(model, "landing.hbs");
        }, new HandlebarsTemplateEngine());

        post("/landing", (request, response) -> {
            String jhed = request.cookie("jhed");
            // TODO: add ability to add a new course
            response.cookie("jhed", jhed);
            response.cookie("profileType", "Applicant");
            response.redirect("/landing");
            return null;
        }, new HandlebarsTemplateEngine());

        get("/:id/courseinfo", (request, response) -> {
            Map<String, Object> model = new HashMap<String, Object>();

            int courseId = Integer.parseInt(request.params(":id"));
            Course course = courseDao.read(courseId);
            String name = course.getName();
            String courseNumber = course.getCourseNumber();
            String description = course.getCourseDescription();
            if (description.isEmpty()) {
                description = null;
            }
            String interviewLink = course.getInterviewLink();
            if (interviewLink.isEmpty()) {
                interviewLink = null;
            }
            List<Applicant> interestedApplicants = course.getInterestedApplicants();
            List<Applicant> hiredApplicants = course.getHiredApplicants();

            /* later can put in semester */
            model.put("name", name);
            model.put("id", courseId);
            model.put("courseNumber", courseNumber);
            model.put("description", description);
            model.put("interviewLink", interviewLink);
            model.put("interestedApplicants", interestedApplicants);
            model.put("hiredApplicants", hiredApplicants);

            return new ModelAndView(model, "courseinfo.hbs");
        }, new HandlebarsTemplateEngine());

        post("/:id/courseinfo", (request, response) -> {
            String jhed = request.cookie("jhed");
            int courseId = Integer.parseInt(request.params(":id"));
            Course course = courseDao.read(courseId);

            String description = request.queryParams("description");
            String interviewLink = request.queryParams("interviewLink");

            course.setCourseDescription(description);
            course.setInterviewLink(interviewLink);
            courseDao.update(course);

            response.cookie("jhed", jhed);
            response.cookie("profileType", "Applicant");
            response.redirect("/landing");
            return null;
        }, new HandlebarsTemplateEngine());

        get("/:id/courseprofile", (request, response) -> {
            Map<String, Object> model = new HashMap<String, Object>();

            int courseId = Integer.parseInt(request.params(":id"));
            Course course = courseDao.read(courseId);
            String name = course.getName();
            String courseNumber = course.getCourseNumber();
            String description = course.getCourseDescription();
            if (description.isEmpty()) {
                description = null;
            }
            String interviewLink = course.getInterviewLink();
            if (interviewLink.isEmpty()) {
                interviewLink = null;
            }

            List<StaffMember> instructors = course.getInstructors();

            /* later can put in semester */
            model.put("name", name);
            model.put("courseNumber", courseNumber);
            model.put("description", description);
            model.put("interviewLink", interviewLink);
            model.put("instructors", instructors);

            return new ModelAndView(model, "courseprofile.hbs");
        }, new HandlebarsTemplateEngine());

        get("/studentprofile", (request, response) -> {
            Map<String, Object> model = new HashMap<>();
            String jhed = request.cookie("jhed");
            Applicant student = applicantDao.read(jhed);

            String name = student.getName();
            String email = student.getEmail();
            String year = student.getYear();
            String majorAndMinor = student.getMajorAndMinor();
            Double gpa = student.getGpa();
            Double credits = student.getRegisteredCredits();
            Boolean fws = student.getFws();
            String studentStatus = student.getStudentStatus();
            String mostRecentPayroll = student.getMostRecentPayroll();
            String otherJobs = student.getOtherJobs();
            int hoursAvailable = student.getHoursAvailable();
            String reference = student.getReferenceEmail();
            String resume = student.getResumeLink();

            model.put("name", name);
            model.put("email", email);
            model.put("year", year);
            model.put("majorAndMinor", majorAndMinor);
            model.put("gpa", gpa);
            model.put("credits", credits);
            model.put("fws", fws);
            model.put("studentStatus", studentStatus);
            model.put("mostRecentPayroll", mostRecentPayroll);
            model.put("otherJobs", otherJobs);
            model.put("hoursAvailable", hoursAvailable);
            model.put("reference", reference);
            model.put("resume", resume);

            if (student.getRankOne() != null) {
                model.put("rank1", student.getRankOne().getName());
            } else {
                model.put("rank1", null);
            }
            if (student.getRankTwo() != null) {
                model.put("rank2", student.getRankTwo().getName());
            } else {
                model.put("rank2", null);
            }
            if (student.getRankThree() != null) {
                model.put("rank3", student.getRankThree().getName());
            } else {
                model.put("rank3", null);
            }
            List<Course> courseList = student.getCoursesList();
            model.put("courseList", courseList);

            List<Grade> gradesList = new ArrayList<Grade>();
            List<Course> headCAInterests = student.getHeadCAInterest();
            HashMap<Course, String> interestedGrades = student.getInterestedCourses();
            for (Map.Entry<Course, String> entry : interestedGrades.entrySet()) {
                String courseId = String.valueOf(entry.getKey().getId());
                String courseName = entry.getKey().getName();
                String grade = entry.getValue();
                String headCAInterest = "No";
                if (headCAInterests != null && headCAInterests.contains(entry.getKey())) {
                    headCAInterest = "Yes";
                }
                gradesList.add(new Grade(courseId, courseName, grade, headCAInterest));
            }
            model.put("gradesList", gradesList);
            return new ModelAndView(model, "studentprofile.hbs");
        }, new HandlebarsTemplateEngine());

        post("/studentprofile", (request, response) -> {
            HashMap<Course, String> interestedCourses = new HashMap();
            String rank1 = request.queryParams("rank1");
            String rank2 = request.queryParams("rank2");
            String rank3 = request.queryParams("rank3");
            String jhed = request.cookie("jhed");
            String year = request.queryParams("year");
            String majorAndMinor = request.queryParams("majorAndMinor");
            Double gpa = Double.valueOf(request.queryParams("gpa"));
            Double credits = Double.valueOf(request.queryParams("credits"));
            String fwsStr = request.queryParams("fws");
            Boolean fws = fwsStr.equals("Yes") ? true : false;
            String studentStatus = request.queryParams("studentStatus");
            String mostRecentPayroll = request.queryParams("mostRecentPayroll");
            String otherJobs = request.queryParams("otherJobs");
            int hoursAvailable = Integer.valueOf(request.queryParams("hoursAvailable"));
            String reference = request.queryParams("reference");
            String resume = request.queryParams("resume");

            // For every course, get updated grade ("Not taken" or letter)
            List<Course> headCAInterest = new ArrayList<>();
            for (Course c : all_courses) {
                String grade = request.queryParams(c.getId() + "grade");
                String interest = request.queryParams(c.getId() + "interest");
                if (interest.equals("Yes")) {
                    headCAInterest.add(c);
                }
                interestedCourses.put(c, grade);
            }

            // Update POJO
            Applicant student = applicantDao.read(jhed);
            student.setInterestedCourses(interestedCourses);
            student.setRankOne(courseDao.read(rank1));
            student.setRankTwo(courseDao.read(rank2));
            student.setRankThree(courseDao.read(rank3));
            student.setYear(year);
            student.setMajorAndMinor(majorAndMinor);
            student.setGpa(gpa);
            student.setRegisteredCredits(credits);
            student.setFws(fws);
            student.setStudentStatus(studentStatus);
            student.setMostRecentPayroll(mostRecentPayroll);
            student.setOtherJobs(otherJobs);
            student.setReferenceEmail(reference);
            student.setResumeLink(resume);
            student.setHoursAvailable(hoursAvailable);
            student.setHeadCAInterest(headCAInterest);
            applicantDao.update(student);

            // Set cookies and redirect
            response.cookie("jhed", jhed);
            response.cookie("profileType", "Applicant");
            response.redirect("/landing");
            return null;
        }, new HandlebarsTemplateEngine());

        get("/:jhed/studentview", (request, response) -> {
            Map<String, Object> model = new HashMap<String, Object>();
            String jhed = request.params(":jhed");

            Applicant student = applicantDao.read(jhed);

            /* later: get if they have taken the course, grade, etc */
            String name = student.getName();
            String email = student.getEmail();
            String year = student.getYear();
            Double gpa = student.getGpa();
            Double credits = student.getRegisteredCredits();
            String majorAndMinor = student.getMajorAndMinor();
            String reference = student.getReferenceEmail();
            String resume = student.getResumeLink();

            // Put in context for hbs
            model.put("name", name);
            model.put("jhed", jhed);
            model.put("year", year);
            model.put("majorAndMinor", majorAndMinor);
            model.put("email", email);
            model.put("gpa", gpa);
            model.put("credits", credits);
            model.put("reference", reference);
            model.put("resume", resume);

            // Populate ranked courses
            if (student.getRankOne() != null) {
                model.put("courseOne", student.getRankOne().getName());
            } else {
                model.put("courseOne", null);
            }
            if (student.getRankTwo() != null) {
                model.put("courseTwo", student.getRankTwo().getName());
            } else {
                model.put("courseTwo", null);
            }
            if (student.getRankThree() != null) {
                model.put("courseThree", student.getRankThree().getName());
            } else {
                model.put("courseThree", null);
            }

            // Populate course specific information
            List<Course> headCAInterest = student.getHeadCAInterest();
            HashMap<Course, String> interestedGrades = student.getInterestedCourses();

            // Map course to a map containing the keys "grade" and "headCAInterest"
            HashMap<String, HashMap<String, String>> courseSpecificInfo = new HashMap<>();
            for (Map.Entry<Course, String> entry : interestedGrades.entrySet()) {
                Course course = entry.getKey();
                String courseName = course.getName();
                String grade = entry.getValue();
                String interest = "No";
                if ((headCAInterest != null) && (headCAInterest.contains(course))) {
                    interest = "Yes";
                }
                if (!grade.equals("Not Taken")) {
                    HashMap<String, String> info = new LinkedHashMap<>();
                    info.put("Grade", grade);
                    info.put("Head CA Interest", interest);
                    courseSpecificInfo.put(courseName, info);
                }
            }
            model.put("courseSpecificInfo", courseSpecificInfo);
            return new ModelAndView(model, "studentview.hbs");
        }, new HandlebarsTemplateEngine());

    }


}
