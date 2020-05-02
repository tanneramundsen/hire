import dao.*;
import model.Applicant;
import model.Course;
import model.Grade;
import model.StaffMember;
import org.apache.commons.lang3.ArrayUtils;
import spark.ModelAndView;
import spark.template.handlebars.HandlebarsTemplateEngine;

import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.util.*;

import static spark.Spark.*;

public class WebServer {

    //filter list
    static List<String> selectedFilters;

    public static void main(String[] args) throws URISyntaxException, ClassNotFoundException {

        port(getHerokuAssignedPort());

        DaoFactory.DROP_TABLES_IF_EXIST = true;

        DaoFactory.dropAllTablesIfExists();
        Sql2oCourseDao courseDao = DaoFactory.getCourseDao();
        Sql2oStaffMemberDao staffMemberDao = DaoFactory.getStaffMemberDao();
        Sql2oApplicantDao applicantDao = DaoFactory.getApplicantDao();

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
            return new ModelAndView(new HashMap<>(), "index.hbs");
        }, new HandlebarsTemplateEngine());

        get("/login", (request, response) -> {
            return new ModelAndView(new HashMap<>(), "login.hbs");
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
            response.removeCookie("profileType");
            response.redirect("/");
            return null;
        }), new HandlebarsTemplateEngine());

        get("/signup", (request, response) -> {
            Map<String, Object> model = new HashMap<String, Object>();
            model.put("allCourses", courseDao.findAll());
            return new ModelAndView(model, "signup.hbs");
        }, new HandlebarsTemplateEngine());

        post("/signup", (request, response) -> {
            String name = request.queryParams("firstName") + " " + request.queryParams("lastName");
            String jhed = request.queryParams("jhed");
            String email = jhed + "@jhu.edu";
            String profileType = request.queryParams("profileType");
            String[] courses = request.queryParamsValues("courses");

            // use information to create either an applicant or staff member
            if (profileType.equals("Professor")) {
                List<Course> courseList = new ArrayList<Course>();
                for (String course : courses) {
                    Course newCourse = courseDao.read(course);
                    courseList.add(newCourse);
                }
                StaffMember s = new StaffMember(name, jhed, courseList, false);
                staffMemberDao.add(s);
                // Update courses to have staff member as instructor
                for (Course c : courseList) {
                    List<StaffMember> instructors = c.getInstructors();
                    instructors.add(s);
                    c.setInstructors(instructors);
                    courseDao.update(c);
                }
            } else if (profileType.equals("Admin")){
                List<Course> courseList = new ArrayList<Course>();
                StaffMember s = new StaffMember(name, jhed, all_courses, true);
                staffMemberDao.add(s);
                // Update courses to have staff member as instructor
                for (Course c : courseList) {
                    List<StaffMember> instructors = c.getInstructors();
                    instructors.add(s);
                    c.setInstructors(instructors);
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
            List<Course> allCourses = courseDao.findAll();
            List<Course> courseList = new ArrayList<>();
            boolean isStaffMember = false;

            // Redirect back to login if information not in database
            if (profileType.equals("Professor") || profileType.equals("Admin")) {
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
            List<Course> otherCourses = new ArrayList<>();
            for(Course c: allCourses) {
                if (!courseList.contains(c)) {
                    otherCourses.add(c);
                }
            }

            model.put("name", name);
            model.put("courseList", courseList);
            model.put("otherCourses", otherCourses);
            model.put("isStaffMember", isStaffMember);

            return new ModelAndView(model, "landing.hbs");
        }, new HandlebarsTemplateEngine());

        post("/addcourse", (request, response) -> {
            String jhed = request.cookie("jhed");
            String profileType = request.cookie("profileType");
            String[] newCourses = request.queryParamsValues("newCourses");

            if (newCourses == null || newCourses.length == 0) {
                response.cookie("jhed", jhed);
                response.cookie("profileType", profileType);
                response.redirect("/landing");
                return null;
            }

            // use information to create either an applicant or staff member
            if (profileType.equals("Professor") || profileType.equals("Admin")) {
                StaffMember s = staffMemberDao.read(jhed);
                List<Course> courseList = s.getCourses();
                for (String course: newCourses) {
                    Course newCourse = courseDao.read(course);
                    List<StaffMember> instructors = newCourse.getInstructors();
                    instructors.add(s);
                    newCourse.setInstructors(instructors);
                    courseDao.update(newCourse);
                    courseList.add(newCourse);
                }
                s.setCourses(courseList);
                staffMemberDao.update(s);
            } else {
                Applicant a = applicantDao.read(jhed);
                HashMap<Course, String> coursesHashMap = a.getInterestedCourses();
                for (String course: newCourses) {
                    Course newCourse = courseDao.read(course);
                    coursesHashMap.put(newCourse, "Not Taken");
                }
                a.setInterestedCourses(coursesHashMap);
                applicantDao.update(a);
            }
            response.cookie("jhed", jhed);
            response.cookie("profileType", profileType);
            response.redirect("/landing");
            return null;
        }, new HandlebarsTemplateEngine());

        post("/deletecourse", (request, response) -> {
            String jhed = request.cookie("jhed");
            String profileType = request.cookie("profileType");
            String[] newCourses = request.queryParamsValues("deleteCourses");

            if (newCourses == null || newCourses.length == 0) {
                response.cookie("jhed", jhed);
                response.cookie("profileType", profileType);
                response.redirect("/landing");
                return null;
            }

            // use information to create either an applicant or staff member
            if (profileType.equals("Professor") || profileType.equals("Admin")) {
                StaffMember s = staffMemberDao.read(jhed);
                List<Course> courseList = s.getCourses();
                for (String course: newCourses) {
                    Course newCourse = courseDao.read(course);
                    List<StaffMember> instructors = newCourse.getInstructors();
                    instructors.remove(s);
                    newCourse.setInstructors(instructors);
                    courseDao.update(newCourse);
                    courseList.remove(newCourse);
                }
                s.setCourses(courseList);
                staffMemberDao.update(s);
            } else {
                Applicant a = applicantDao.read(jhed);
                HashMap<Course, String> coursesHashMap = a.getInterestedCourses();
                for (String course: newCourses) {
                    Course newCourse = courseDao.read(course);
                    coursesHashMap.remove(newCourse);
                }
                a.setInterestedCourses(coursesHashMap);
                applicantDao.update(a);
            }
            response.cookie("jhed", jhed);
            response.cookie("profileType", profileType);
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

            boolean linkVisible = course.isLinkVisible();
            boolean hiringComplete = course.isHiringComplete();

            List<Applicant> hiredApplicants = course.getHiredApplicants();
            List<Applicant> shortlistedApplicants = course.getShortlistedApplicants();

            model.put("name", name);
            model.put("id", courseId);
            model.put("courseNumber", courseNumber);
            model.put("description", description);
            model.put("hiringComplete", hiringComplete);
            model.put("interviewLink", interviewLink);
            model.put("linkVisible", linkVisible);
            model.put("shortlistedApplicants", shortlistedApplicants);
            model.put("hiredApplicants", hiredApplicants);
            model.put("isStaffMember", true);

            boolean prevCAFilterOn = false;
            boolean CadThisCourseFilterOn = false;
            boolean gradeFilterOn = false;
            boolean headCAFilterOn = false;
            boolean sophomoreFilterOn = false;
            boolean juniorFilterOn = false;
            boolean seniorFilterOn = false;
            boolean combinedFilterOn = false;
            boolean mastersFirstFilterOn = false;
            boolean mastersSecondFilterOn = false;
            boolean phDFilterOn = false;

            if (selectedFilters == null) {
                List<Applicant> interestedApplicants = course.getInterestedApplicants();
                model.put("interestedApplicants", interestedApplicants);
            } else {
                List<List<Applicant>> filterLists = new ArrayList();
                List<List<Applicant>> filterListsYears = new ArrayList();
                for(String s : selectedFilters) {
                    if (s.equals("prevCAExperience")) {
                        List<Applicant> list1 = DaoFactory.filterByHasPrevCAExperience();
                        filterLists.add(list1);
                        prevCAFilterOn = true;
                    }
                    if (s.equals("CAdThisCourse")) {
                        List<Applicant> list2 = DaoFactory.filterByHasPrevCAExperienceForThisClass(course);
                        filterLists.add(list2);
                        CadThisCourseFilterOn = true;
                    }
                    if (s.equals("takenCourseAndGrade")) {
                        List<Applicant> list3 = DaoFactory.filterByHasGottenAboveB(course);
                        filterLists.add(list3);
                        gradeFilterOn = true;
                    }
                    if (s.equals("headCAInterest")) {
                        List<Applicant> list4 = DaoFactory.filterByHeadCAInterest(course);
                        filterLists.add(list4);
                        headCAFilterOn = true;
                    }
                    if (s.equals("sophomore")) {
                        List<Applicant> list5 = DaoFactory.filterBySophomore();
                        filterListsYears.add(list5);
                        sophomoreFilterOn = true;
                    }
                    if (s.equals("junior")) {
                        List<Applicant> list6 = DaoFactory.filterByJunior();
                        filterListsYears.add(list6);
                        juniorFilterOn = true;
                    }
                    if (s.equals("senior")) {
                        List<Applicant> list7 = DaoFactory.filterBySenior();
                        filterListsYears.add(list7);
                        seniorFilterOn = true;
                    }
                    if (s.equals("combined")) {
                        List<Applicant> list8 = DaoFactory.filterByCombined();
                        filterListsYears.add(list8);
                        combinedFilterOn = true;
                    }
                    if (s.equals("mastersFirst")) {
                        List<Applicant> list9 = DaoFactory.filterByMastersFirst();
                        filterListsYears.add(list9);
                        mastersFirstFilterOn = true;
                    }
                    if (s.equals("mastersSecond")) {
                        List<Applicant> list10 = DaoFactory.filterByMastersSecond();
                        filterListsYears.add(list10);
                        mastersSecondFilterOn = true;
                    }
                    if (s.equals("PhD")) {
                        List<Applicant> list11 = DaoFactory.filterByPhD();
                        filterListsYears.add(list11);
                        phDFilterOn = true;
                    }
                }
                if (filterListsYears.size() > 0) {
                    Set<Applicant> set = new HashSet();
                    for (List<Applicant> a : filterListsYears) {
                        set.addAll(a);
                    }
                    List<Applicant> allFilteredYears = new ArrayList(set);
                    filterLists.add(allFilteredYears);
                }
                List<Applicant> filteredInterestedApplicantList = filterLists.get(0);
                for (int i = 1; i < filterLists.size(); i++) {
                    filteredInterestedApplicantList.retainAll(filterLists.get(i));
                }
                model.put("interestedApplicants", filteredInterestedApplicantList);
            }

            model.put("prevCAFilterOn", prevCAFilterOn);
            model.put("CadThisCourseFilterOn", CadThisCourseFilterOn);
            model.put("gradeFilterOn", gradeFilterOn);
            model.put("headCAFilterOn", headCAFilterOn);
            model.put("sophomoreFilterOn", sophomoreFilterOn);
            model.put("juniorFilterOn", juniorFilterOn);
            model.put("seniorFilterOn", seniorFilterOn);
            model.put("combinedFilterOn", combinedFilterOn);
            model.put("mastersFirstFilterOn", mastersFirstFilterOn);
            model.put("mastersSecondFilterOn", mastersSecondFilterOn);
            model.put("phDFilterOn", phDFilterOn);

            return new ModelAndView(model, "courseinfo.hbs");
        }, new HandlebarsTemplateEngine());

        post("/:id/courseinfo/addcourseinfo", (request, response) -> {
            int courseId = Integer.parseInt(request.params(":id"));
            Course course = courseDao.read(courseId);
            List<Applicant> interestedApplicants = course.getInterestedApplicants();
            String description = request.queryParams("description");
            String interviewLink = request.queryParams("interviewLink");
            if (description == null) {
                description = course.getCourseDescription();
            }
            if (interviewLink == null) {
                interviewLink = course.getInterviewLink();
            }
            course.setCourseDescription(description);
            course.setInterviewLink(interviewLink);
            courseDao.update(course);

            String redirect = "/" + courseId + "/courseinfo";
            response.redirect(redirect);
            return null;
        }, new HandlebarsTemplateEngine());

        post("/:id/courseinfo/setFilters", (request, response) -> {
            int courseId = Integer.parseInt(request.params(":id"));
            Course course = courseDao.read(courseId);
            String[] chosenFilters = request.queryParamsValues("filterOptions");
            List<String> filterOptions = Arrays.asList(chosenFilters);
            selectedFilters = filterOptions;
            String redirect = "/" + courseId + "/courseinfo";
            response.redirect(redirect);
            return null;
        }, new HandlebarsTemplateEngine());

        post("/:id/courseinfo/removeFilters", (request, response) -> {
            int courseId = Integer.parseInt(request.params(":id"));
            selectedFilters = null;
            String redirect = "/" + courseId + "/courseinfo";
            response.redirect(redirect);
            return null;
        }, new HandlebarsTemplateEngine());

        post("/:id/courseinfo/addtoshortlist", (request, response) -> {
            int courseId = Integer.parseInt(request.params(":id"));
            Course course = courseDao.read(courseId);
            List<Applicant> interestedApplicants = course.getInterestedApplicants();
            List<Applicant> shortlistedApplicants = course.getShortlistedApplicants();
            String[] shortList = request.queryParamsValues("selectedForShortList");
            if (!ArrayUtils.isEmpty(shortList)) {
                List<String> selectedShortList = Arrays.asList(shortList);
                for (Applicant a : interestedApplicants) {
                    if (selectedShortList.contains(a.getName())) {
                        shortlistedApplicants.add(a);
                    }
                }
            }
            course.setShortlistedApplicants(shortlistedApplicants);
            courseDao.update(course);

            String redirect = "/" + courseId + "/courseinfo";
            response.redirect(redirect);
            return null;
        }, new HandlebarsTemplateEngine());

        post("/:id/courseinfo/addtohired", (request, response) -> {
            int courseId = Integer.parseInt(request.params(":id"));
            Course course = courseDao.read(courseId);
            List<Applicant> shortlistedApplicants = course.getShortlistedApplicants();
            List<Applicant> hiredApplicants = course.getHiredApplicants();
            List<Applicant> newShortList = new ArrayList<Applicant>();
            String[] hiredList = request.queryParamsValues("checkedFromShortList");
            if (!ArrayUtils.isEmpty(hiredList)) {
                List<String> selectedHiredList = Arrays.asList(hiredList);
                for (Applicant a : shortlistedApplicants) {
                    //add to hired list
                    if (selectedHiredList.contains(a.getName())) {
                        hiredApplicants.add(a);
                    }
                    //remove from shortlist
                    if (!selectedHiredList.contains(a.getName())) {
                        newShortList.add(a);
                    }
                }
            }

            course.setShortlistedApplicants(newShortList);
            course.setHiredApplicants(hiredApplicants);
            courseDao.update(course);

            String redirect = "/" + courseId + "/courseinfo";
            response.redirect(redirect);
            return null;
        }, new HandlebarsTemplateEngine());

        post("/:id/courseinfo/deleteshortlist", (request, response) -> {
            int courseId = Integer.parseInt(request.params(":id"));
            Course course = courseDao.read(courseId);
            List<Applicant> shortlistedApplicants = course.getShortlistedApplicants();
            List<Applicant> newShortList = new ArrayList<Applicant>();
            String[] shortList = request.queryParamsValues("checkedFromShortList");
            if (!ArrayUtils.isEmpty(shortList)) {
                List<String> checkedFromShortList = Arrays.asList(shortList);
                for (Applicant a : shortlistedApplicants) {
                    if (!checkedFromShortList.contains(a.getName())) {
                        newShortList.add(a);
                    }
                }
            }
            course.setShortlistedApplicants(newShortList);
            courseDao.update(course);

            String redirect = "/" + courseId + "/courseinfo";
            response.redirect(redirect);
            return null;
        }, new HandlebarsTemplateEngine());

        post("/:id/courseinfo/deletehired", (request, response) -> {
            int courseId = Integer.parseInt(request.params(":id"));
            Course course = courseDao.read(courseId);
            List<Applicant> hiredApplicants = course.getHiredApplicants();
            List<Applicant> shortlistedApplicants = course.getShortlistedApplicants();

            List<Applicant> newHiredList = new ArrayList<Applicant>();
            String[] hiredList = request.queryParamsValues("selectedForHired");
            if (!ArrayUtils.isEmpty(hiredList)) {
                List<String> checkedFromHired = Arrays.asList(hiredList);
                for (Applicant a : hiredApplicants) {
                    if (!checkedFromHired.contains(a.getName())) {
                        newHiredList.add(a);
                    }
                    if (checkedFromHired.contains(a.getName())) {
                        shortlistedApplicants.add(a);
                    }
                }
            }

            course.setShortlistedApplicants(shortlistedApplicants);
            course.setHiredApplicants(newHiredList);
            courseDao.update(course);

            String redirect = "/" + courseId + "/courseinfo";
            response.redirect(redirect);
            return null;
        }, new HandlebarsTemplateEngine());

        post("/:id/courseinfo/sendInterviewLink", (request, response) -> {
            int courseId = Integer.parseInt(request.params(":id"));
            Course course = courseDao.read(courseId);
            course.setLinkVisible(true);
            courseDao.update(course);
            String redirect = "/" + courseId + "/courseinfo";
            response.redirect(redirect);
            return null;
        }, new HandlebarsTemplateEngine());

        post("/:id/courseinfo/sendHiringNotification", (request, response) -> {
            int courseId = Integer.parseInt(request.params(":id"));
            Course course = courseDao.read(courseId);
            course.setHiringComplete(true);
            courseDao.update(course);
            String redirect = "/" + courseId + "/courseinfo";
            response.redirect(redirect);
            return null;
        }, new HandlebarsTemplateEngine());

        get("/:id/courseprofile", (request, response) -> {
            String jhed = request.cookie("jhed");
            Map<String, Object> model = new HashMap<String, Object>();
            int courseId = Integer.parseInt(request.params(":id"));
            Course course = courseDao.read(courseId);
            String name = course.getName();
            String courseNumber = course.getCourseNumber();
            String description = course.getCourseDescription();
            List<Applicant> shortList = course.getShortlistedApplicants();
            List<Applicant> hiredList = course.getHiredApplicants();
            boolean linkVisible = course.isLinkVisible();
            boolean hiringComplete = course.isHiringComplete();
            boolean isShortlisted = false;
            boolean isHired = false;
            for (Applicant a : shortList) {
                if (a.getJhed().equals(jhed)) {
                    isShortlisted = true;
                }
            }
            for (Applicant a : hiredList) {
                if (a.getJhed().equals(jhed)) {
                    isHired = true;
                }
            }
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
            model.put("linkVisible", linkVisible);
            model.put("hiringComplete", hiringComplete);
            model.put("isShortListed", isShortlisted);
            model.put("isHired", isHired);
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
            List<Course> previousCA = student.getPreviousCA();
            HashMap<Course, String> interestedGrades = student.getInterestedCourses();
            for (Map.Entry<Course, String> entry : interestedGrades.entrySet()) {
                String courseId = String.valueOf(entry.getKey().getId());
                String courseName = entry.getKey().getName();
                String grade = entry.getValue();
                String headCAInterest = "No";
                String previousCAd = "No";
                if (headCAInterests != null && headCAInterests.contains(entry.getKey())) {
                    headCAInterest = "Yes";
                }
                if (previousCA != null && previousCA.contains(entry.getKey())) {
                    previousCAd = "Yes";
                }
                gradesList.add(new Grade(courseId, courseName, grade, headCAInterest, previousCAd));
            }
            model.put("gradesList", gradesList);
            return new ModelAndView(model, "studentprofile.hbs");
        }, new HandlebarsTemplateEngine());

        post("/studentprofile", (request, response) -> {
            String jhed = request.cookie("jhed");
            String profileType = request.cookie("profileType");

            HashMap<Course, String> interestedCourses = new HashMap();
            String rank1 = request.queryParams("rank1");
            String rank2 = request.queryParams("rank2");
            String rank3 = request.queryParams("rank3");
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

            // Get student's interested courses
            Applicant student = applicantDao.read(jhed);
            List<Course> curCourses = student.getCoursesList();

            // For every course, get updated grade ("Not taken" or letter)
            List<Course> headCAInterest = new ArrayList<>();
            List<Course> previousCA = new ArrayList<>();
            for (Course c : curCourses) {
                String grade = request.queryParams(c.getId() + "grade");
                String interest = request.queryParams(c.getId() + "interest");
                String prevCA = request.queryParams(c.getId() + "previousCA");
                if (interest.equals("Yes")) {
                    headCAInterest.add(c);
                }
                if (prevCA.equals("Yes")) {
                    previousCA.add(c);
                }
                interestedCourses.put(c, grade);
            }

            // Update POJO
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
            student.setPreviousCA(previousCA);
            applicantDao.update(student);

            // Set cookies and redirect
            response.cookie("jhed", jhed);
            response.cookie("profileType", profileType);
            response.redirect("/landing");
            return null;
        }, new HandlebarsTemplateEngine());

        get("/:courseId/courseinfo/:jhed/studentview", (request, response) -> {
            Map<String, Object> model = new HashMap<String, Object>();
            String applicantJhed = request.params(":jhed");
            int courseId = Integer.parseInt(request.params(":courseId"));
            String profileType = request.cookie("profileType");

            Applicant student = applicantDao.read(applicantJhed);
            Course course1 = courseDao.read(courseId);

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
            model.put("id", courseId);
            model.put("shortlistStatus", course1.getShortlistedApplicants().contains(student));
            model.put("jhed", applicantJhed);
            model.put("year", year);
            model.put("majorAndMinor", majorAndMinor);
            model.put("email", email);
            model.put("gpa", gpa);
            model.put("credits", credits);
            model.put("reference", reference);
            model.put("resume", resume);
            // Only visible to admin
            String fwsStr = "No";
            if (student.getFws()) {
                fwsStr = "Yes";
            }
            model.put("fws", fwsStr);
            model.put("studentStatus", student.getStudentStatus());
            model.put("mostRecentPayroll", student.getMostRecentPayroll());
            model.put("otherJobs", student.getOtherJobs());
            model.put("hoursAvailable", student.getHoursAvailable());

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
            List<Course> previousCA = student.getPreviousCA();
            HashMap<Course, String> interestedGrades = student.getInterestedCourses();

            // Map course to a map containing the keys "grade" and "headCAInterest" and "previousCA"
            HashMap<String, HashMap<String, String>> courseSpecificInfo = new HashMap<>();
            for (Map.Entry<Course, String> entry : interestedGrades.entrySet()) {
                Course course = entry.getKey();
                String courseName = course.getName();
                String grade = entry.getValue();
                String interest = "No";
                String prevCA = "No";
                if ((headCAInterest != null) && (headCAInterest.contains(course))) {
                    interest = "Yes";
                }
                if ((previousCA != null) && (previousCA.contains(course))) {
                    prevCA = "Yes";
                }
                if (!grade.equals("Not Taken")) {
                    HashMap<String, String> info = new LinkedHashMap<>();
                    info.put("Grade", grade);
                    info.put("Head CA Interest", interest);
                    info.put("Previously CA'd", prevCA);
                    courseSpecificInfo.put(courseName, info);
                }
            }
            model.put("courseSpecificInfo", courseSpecificInfo);
            model.put("isAdmin", profileType.equals("Admin"));
            model.put("isStaffMember", true);
            return new ModelAndView(model, "studentview.hbs");
        }, new HandlebarsTemplateEngine());


        post("/:courseId/courseinfo/:jhed/studentview", (request, response) -> {
            String applicantJhed = request.params(":jhed");
            int courseId = Integer.parseInt(request.params(":courseId"));

            Course course = courseDao.read(courseId);
            Applicant applicant = applicantDao.read(applicantJhed);

            String shortlistStatus = request.queryParams("shortlistStatus");
            List<Applicant> shortlist = course.getShortlistedApplicants();
            if (shortlist == null) shortlist = new ArrayList<Applicant>();
            if (shortlistStatus != null) {
                if(!shortlist.contains(applicant)) {
                    shortlist.add(applicant);
                }
            } else {
                if(shortlist.contains(applicant)) {
                    shortlist.remove(applicant);
                }
            }
            course.setShortlistedApplicants(shortlist);

            courseDao.update(course);

            String redirect = "/" + courseId + "/courseinfo";
            response.redirect(redirect);
            return null;
        }, new HandlebarsTemplateEngine());
    }

    /**
     * Obtain PORT to use for the web server.
     * @return integer PORT number
     */
    static int getHerokuAssignedPort() {
        ProcessBuilder processBuilder = new ProcessBuilder();
        if (processBuilder.environment().get("PORT") != null) {
            return Integer.parseInt(processBuilder.environment().get("PORT"));
        }
        return 4567; //return default port if heroku-port isn't set (i.e. on localhost)
    }

    /**
     * Obtain the path to database for the web application.
     * @return path to database file
     */
    static String getHerokuDatabasePath() {
        ProcessBuilder processBuilder = new ProcessBuilder();
        if (processBuilder.environment().get("PORT") != null) {
            return Paths.get("build","resources", "main").toFile().getAbsolutePath()
                    + "/db/Store.db";
        }
        return Paths.get("src", "main", "resources").toFile().getAbsolutePath()
                + "/db/Store.db";
    }
}
