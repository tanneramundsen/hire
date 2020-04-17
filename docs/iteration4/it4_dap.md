# OO Design
A UML class diagram reflecting the "model" for that iteration only.
Use a software to draw this (e.g. draw.io) and save the diagram as an image.
Upload the image and link it in here using this syntax.

![UML Diagram](./img/uml.png)

# Wireframe
One (or a few) simple sketch of how the user interacts with the application.
This could be a sketch of your user interface.
You can draw it by hand and insert it here as an image.

![Wireframe](./img/wireframe.png)

# Iteration Backlog
List the User Stories that you will implement in this iteration.

- As a user of the CA application, I want to be able to log in so that only I can have access to my information.
- As a user of the CA application, I want to be able to delete courses off of my interested courses list so that I can update my list if necessary.
- As a staff member looking to hire CAs, I want to be able to delete applicants off of my shortlist so that I can narrow down the candidates.
- As a staff member looking to hire CAs, I want to be able to add applicants to my hired list so that I can finalize my hiring process.
- As a staff member looking to hire CAs, I want to filter CA applications based on the candidate's qualifications so that I can hire the right candidate.
  - As a staff member, I’d like to filter by student’s grade, year, if they have previously CAed and course preferences so that I can hire the right candidate.
- As a CA applicant, I want changes to my application status to be displayed (application received, getting an interview, getting hired etc.) so I’m aware of the status of my application.
- As a CA applicant, I want to easily see a staff member’s availability for interviews (e.g. staff member uploads SignUpGenius link) so that I can select meeting times that will work for both of us.

# Tasks
A tentative list of the "to do" in order to sucessfully complete this iteration.
This list will change and it is good to keep it updated.
It does not need to be exhustive.

- New fields in Applicant
  - previousCACourses: `List<Course>`
    - Add to `studentprofile.hbs`
    - Add to `studentview.hbs`
- Filtering
  - Criteria
    - Taken course/prerequisite courses?
    - Grade
    - Year
    - Head CA Interest?
    - Was a previous CA?
    - Is this a preferred course?
  - Implement in `courseinfo.hbs`
  - Only display students that match filter criteria
- Notifying application status updates and interview links
  - Email shortlisted applicants
- Login/Password with database or SIS/Outlook
- Allow applicants and staff members to remove courses from interested course list
- Allow staff members to add applicants to hired and remove them from shortlist
- Deploy to Heroku
- Add Postman test suite

# Retrospective 

- Items we successfully delivered:
    - As a user of the CA application, I want to be able to delete courses off of my interested courses list so that I can update my list if necessary.
    - As a staff member looking to hire CAs, I want to be able to delete applicants off of my shortlist so that I can narrow down the candidates.
   - As a staff member looking to hire CAs, I want to be able to add applicants to my hired list so that I can finalize my hiring process.
    - As a staff member looking to hire CAs, I want to filter CA applications based on the candidate's qualifications so that I can hire the right candidate.
    - As a staff member, I’d like to filter by student’s grade, year, if they have previously CAed and course preferences so that I can hire the right candidate.
    - Refactored our database schema to be much more productive and manageable
    - Built Postman test suite for autonomous testing

- Items we did not successfully deliver
    - We were not able to add features for the current user stories:
        - As a user of the CA application, I want to be able to log in so that only I can have access to my information.
        - As a CA applicant, I want changes to my application status to be displayed (application received, getting an interview, getting hired etc.) so I’m aware of the status of my application.
        - As a CA applicant, I want to easily see a staff member’s availability for interviews (e.g. staff member uploads SignUpGenius link) so that I can select meeting times that will work for both of us.
    - We’ve already added these user stories to our iteration backlog for iteration 5 and have made significant progress in the right direction to implement them in the Beta release 
- Challenges we faced
    - Building testing suite in Postman has a big learning curve but our team was able to pick it up
    - Tanner ran into some bugs while trying to implement the applicant notification features that made it difficult to make progress. I’ve since realized what was causing the bug and plan to implement this feature for iteration 5
    - Deploying to Heroku took a really long time because our project is called hïre (with an umlaut on the i) and that character is not  UTF8 encoded and so Heroku couldn’t open the jar file for our project. Chester learned how to change the name of our project and was able to deploy our app to Heroku  before the iteration 4 deadline.
   -  Daniela had a hard time following resources on how to add Microsoft authentication to our web app. It was difficult being able to tell if the application request I made on Azure would be authorized by the JHU Microsoft platform specifically. I tried another service Auth0, where I was able to successfully create users, but could not get their information to persist to be able to log them back in. The Piazza post to facilitate classmates helping one another came too close to this iteration deadline for us to be able to integrate successfully with the help of a classmate; so we will add the password functionality for the Beta version. Right now, the password shown on the login and sign-out pages does not serve any actual authentication purposes.
