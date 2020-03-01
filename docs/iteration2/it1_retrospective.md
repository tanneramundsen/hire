# OO Design

A UML class diagram reflecting the "model" for that iteration only.

Use a software to draw this (e.g. draw.io) and save the diagram as an image. 

Upload the image and link it in here using this syntax

![](path/to/image.png)

User class - boolean isProfessor, name, courses, any other details, functions (applicant preferences?, update profile, list all courses), interviewed schedule?

* admin? later iteration

Course class - session id, professors, number of CAs, is course active?, hired CAs?

Hiring class (for scheduling interviews)

# Wireframe

One (or a few) simple sketch of how the user interacts with the application. 

This could be a sketch of your user interface. 

You can draw it with hand and insert it here as an image.

# Iteration Backlog

List the User Stories that you will implement in this iteration.

* As a CA applicant I want to be able to fill out an application.

* As a CA applicant I want to be able to select all the courses I’ve taken from a drop down so that I don’t have to write them myself.

* As a CA applicant I want to be able to login.

* As a staff member, I want to be able to login.

* As a staff member, I want to mark myself as the professor of the courses that I teach from a list of all CS courses

* As a staff member, I want to view the courses that I teach in list form

* As a staff member, I want to view the names of the CA applicants that have applied to a given course that I am teaching

* As a staff member, I want to see the full application of a CA applicants that have applied to a given course that I am teaching

# Tasks

A tentative list of the "to do" in order to successfully complete this iteration. 

This list will change and it is good to keep it updated. 

It does not need to be exhaustive.

* Login

    * Login page (frontend)

* Create classes (backend)

    * Hardcode CS classes into DB each semester

    * Course, Staff Member, Applicant, User (abstract class)

    * Create CRUD/persistence, Sql2o for above courses

* Profile creation

    * Get information for user profiles

        * Students: Name, email, eligible courses to CA (model off CA application)

        * Staff: Name, courses teaching 

    * Create onboarding pages (frontend)

        * Display profile to students/staff members after finishing

* Visualization from database

    * Display all hired applicants for each course

    * Display all potential applicants for each course

# Retrospective

**Accomplishments from iteration 1:**

* Login Page created

* Created classes: course, applicant, staffMember

* Created onboarding pages (for both student + staff member)

* Created DAOs for courses, staff members, and applicants using Sql2o

    * Implemented joining table support in our database for the many-to-many relationships between courses and students and courses and staff members

* Wrote tests for DAOs

* Implemented most of WebServer (minus database integration)

* Implemented DaoFactory and DaoUtil to aid in database setup and testing.

**Features we did not deliver:**

* We were unable to finish database integration into our frontend (WebServer) before the deadline for iteration 1. This meant that users were able to view the home page, login, and create a profile, but none of the data was persisted or displayed in a meaningful way.

* Could not run the DAO tests successfully because of various SQL errors.

* Did not finish implementing ApiServer or write tests for it. 

**Challenges and plans for next iteration:**

* For this first iteration, we split the team between backend (three people) and frontend (two people). Both teams became a bit blind to what the other was doing as the iteration #1 deadline came about. So, when the backend team started facing problems in testing, the frontend team was not able to help debug. For this next iteration, we are going to try to divide the frontend and backend work more evenly among the team so that we all have a more holistic understanding of the components of our software. That way when something goes wrong, we can all be useful in debugging. 

* We were often all working on the same issue or bug and it was difficult to keep track of who was working on what. Next time, we will use Github’s Issue Tracking to assign team members to specific issues and keep track of our responsibilities.

