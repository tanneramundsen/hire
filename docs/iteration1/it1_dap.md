# OO Design
A UML class diagram reflecting the "model" for that iteration only.
Use a software to draw this (e.g. draw.io) and save the diagram as an image. 
Upload the image and link it in here using this syntax

![UML Diagram](./img/uml.png)

# Wireframe
One (or a few) simple sketch of how the user interacts with the application. 
This could be a sketch of your user interface. 
You can draw it by hand and insert it here as an image.

![Wireframe](./img/wireframe.png)

# Iteration Backlog
List the User Stories that you will implement in this iteration.

- As a CA applicant I want to be able to fill out the application once and update it every semester so I don’t have to fill out a google survey every semester.
- As a CA applicant I want to be able to select all the courses I’ve taken and the grade I got in them from a drop down so that I don’t have to write them myself.
- As a CA applicant I want to be able to select a time slot for an interview without having to email the professor back and forth.
- As a CA applicant I want to be able to login using my jhu credentials.
- As a staff member, I want to be able to login using my jhu credentials
- As a staff member, I want to mark myself as the professor of the courses that I teach from a list of all CS courses
- As a staff member, I want to list the number of CAs that I will require for each of the courses that I teach
- As a staff member, I want to list my availability in xx-minute time slots and share that availability with interviewees
- As a staff member, I want to view the courses that I teach in list form
- As a staff member, I want to view the names of the CA applicants that have applied to a given course that I am teaching
- As a staff member, I want to see the full application of a CA applicants that have applied to a given course that I am teaching


# Tasks
A tentative list of the "to do" in order to sucessfully complete this iteration. 
This list will change and it is good to keep it updated. 
It does not need to be exhustive.

- Log in (using JHU credentials)
- Create user profiles for students and staff
    - Students: first name, last name, email, year, most recent semester you were on JHU payroll, courses taken, preferences, etc. (model off CA application)
    - Staff: first name, last name, email, courses teaching, number of CAs needed, etc.
- Staff can set up potential interview times
- Staff can notify students selected to be interviewed
- Students will be notified and can sign up for interviews
