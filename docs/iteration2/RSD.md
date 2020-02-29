# **Requirement Specification Document**

## **Problem Statement**

The CA hiring and onboarding process can be super tedious and unorganized for both students and professors since the current google form generates a lot of unorganized information and it’s difficult for professors to resolve hiring conflicts.

## **Potential Clients**

The potential clients for this software include students applying for CA positions as well as staff members who are hiring student CAs.

## **Proposed Solution**

A software system to monitor CAs in the CS department by streamlining all processes in the initial applications/hiring process. Features that will make our software more efficient than the current Google form process will include flags on students that are being considered for more than one course, and the ability for professors to easily filter applications based on their own hiring preferences.

## **Functional Requirements**

### **Must have**

* As a staff member looking to hire CAs, I want to filter CA applications based on the candidate's qualifications and availability so that I can hire the right candidate.

    * As a staff member, I’d like to see whether a student has CAd for that course, whether a student has taken courses that have the course as a prerequisite, and if the student has CA’d for the prerequisite course

* As a staff member looking to hire CAs, I want to note when I conduct interviews/hire students so that I can keep better track of each application’s progress.

* As a CA applicant, I want to have an all-in-one and intuitive profile where I can input and update my qualifications and availability so that I can apply for many positions through one application across semesters.

* As a CA applicant, I want to be notified of changes (application received, getting an interview, getting hired etc.) so I’m aware of the status of my application.

* As a CA applicant, I want to be able to input information about my experiences and preferences so that professors have more information about my application when considering me as a candidate.

* As a CA applicant, I want to update my profile so that my application reflects my qualifications. 

* As an admin, I want to be able to see the information that is not needed as a professor (e.g. international or not, work-study eligibility) so that I can handle offload the amount of information displayed to professors.

* As an admin, I want to be able to view the shortlists compiled for each course so that I can help resolve conflicts over popular applicants.

* As an admin, I want to create/initialize each job advertisement so that there is consistency across different courses and postings. 

* As a staff member, I’d like to set a final date for all students to apply to be a CA for my course, so that I can keep the hiring process more organized. 

### **Nice to have**

* As a staff member who hired many CAs, I want the student CA's semester course schedule's linked so that I can effectively assign section availability.

* As a staff member looking to hire CAs, I want automatic generation of hiring confirmation details as well as automatic CA contracts for each student so that there is less labor and faster turnaround time for hiring.

* As a staff member, I want to be able to input CA evaluations at the end of the semester, so that I can note experiences with the student CA to keep in mind for the next semester. 

* As a staff member, I want to see suggestions for which students to hire so that I can select the best CAs quickly

* As a CA applicant, I want to be able to upload information from LinkedIn and SIS so that I can easily input additional information to my application.

* As a staff member looking to hire CAs, I want to easily see a candidate's availability for interviews so that I can arrange meetings during time slots that will work for both of us.

* As a staff member, I want to be able to solicit more information from applicants I’m interested in via automated email, so that I can better gauge if they will be a good fit.

## **Software Architecture**

This will be a web-based application using the tech stack for the course (e.g. SQLite, Java backend, Javalin, Spark). The clients are the users (staff members, admin, students) who will each input their respective information onto the web app.


