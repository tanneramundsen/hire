# Requirement Specification Document

## Problem Statement 

> Write a few sentences that describes the problem you are trying to solve. In other words, justify why this software project is needed.

The CA hiring/tracking/payroll process can be super tedious and unorganized since there are so many needs and things to keep track off.

## Potential Clients
> Who are affected by this problem (and would benefit from the proposed solution)? I.e. the potential users of the software you are going to build.

The potential clients for this software include students applying for CA positions, staff members who are hiring/tracking student CAs as well as office staff who take care of the payroll information.

## Proposed Solution
> Write a few sentences that describes how a software solution will solve the problem described above.

A software system to monitor CAs in the department by streamlining all processes from the initial applications/hiring, to tracking timesheets, to CA evaluations at the end of the semester and more.

## Functional Requirements
> List the (functional) requirements that software needs to have in order to solve the problem stated above. It is useful to write the requirements in form of **User Stories** and group them into those that are essential (must have), and those which are non-essential (but nice to have).


### Must have
- As a staff member looking to hire CAs, I want to filter CA applications based on the candidate's qualifications and availability.
- As a staff member looking to hire CAs, I want to easily see candidate's availability for interviews and schedule interviews during free time slots.
- As a staff member looking to hire CAs, I want to keep track of the applications and note when I conduct interviews/hire students.
- As a CA applicant, I want to have an all-in-one and intuitive application where I can input all my qualifications and availability so I can apply for many positions through one application.
- As a CA applicant, I always know the status of my application and be notified of changes (getting an interview, getting hired etc.).
- As a hired student CA, I want to be reminded every week to enter my hours for payroll.
- As a staff member, I want to be notified when CAs complete their time sheets and remind them if necessary.


### Nice to have

- As a staff member who hired many CAs, I want the student CA's semester course schedule's linked so that I can effectively assign section availability.
- As a staff member looking to hire CAs, I want automatic generation of hiring confirmation details as well as automatic CA contracts for each student.
- As a recently hired CA, I want to be able to easily and digitally sign all my contracts and set up my payroll information.
- As a staff member, I want to be able to input CA evaluations at the end of the semester to note my experiences with the student CA.



## Software Architecture
> Will this be a Web/desktop/mobile (all, or some other kind of) application? Would it conform to the Client-Server software architecture? 

This will be a web-based app. The clients are the users (staff members and students) and they input all their respective information onto the server (web app).


# Retrospective

## Accomplishments from iteration 1:

- Login Page created

- Created classes: course, applicant, staffMember

- Created onboarding pages (for both student + staff member)

- Created DAOs for courses, staff members, and applicants using Sql2o

    - Implemented joining table support in our database for the many-to-many relationships between courses and students and courses and staff members

- Wrote tests for DAOs

- Implemented most of WebServer (minus database integration)

- Implemented DaoFactory and DaoUtil to aid in database setup and testing.

## Features we did not deliver:

- We were unable to finish database integration into our frontend (WebServer) before the deadline for iteration 1. This meant that users were able to view the home page, login, and create a profile, but none of the data was persisted or displayed in a meaningful way.

- Could not run the DAO tests successfully because of various SQL errors.

- Did not finish implementing ApiServer or write tests for it. 

## Challenges and plans for next iteration:

- For this first iteration, we split the team between backend (three people) and frontend (two people). Both teams became a bit blind to what the other was doing as the iteration #1 deadline came about. So, when the backend team started facing problems in testing, the frontend team was not able to help debug. For this next iteration, we are going to try to divide the frontend and backend work more evenly among the team so that we all have a more holistic understanding of the components of our software. That way when something goes wrong, we can all be useful in debugging. 

- We were often all working on the same issue or bug and it was difficult to keep track of who was working on what. Next time, we will use Github’s Issue Tracking to assign team members to specific issues and keep track of our responsibilities.