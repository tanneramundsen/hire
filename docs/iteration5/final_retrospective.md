
**Revisiting original project proposal**

The original project proposal was for an app that solves the “super tedious and unorganized” CA hiring/tracking/payroll process. Some of the must-have requirements that we included were:

As a staff member looking to hire CAs, I want to filter CA applications based on the candidate's qualifications and availability.

As a CA applicant, I want to have an all-in-one and intuitive application where I can input all my qualifications and availability so I can apply for many positions through one application.

As a CA applicant, I always know the status of my application and be notified of changes (getting an interview, getting hired etc.).

As a staff member looking to hire CAs, I want to keep track of the applications and note when I conduct interviews/hire students.

As a staff member looking to hire CAs, I want to easily see candidate's availability for interviews and schedule interviews during free time slots.

As a hired student CA, I want to be reminded every week to enter my hours for payroll.

As a staff member, I want to be notified when CAs complete their time sheets and remind them if necessary.

Very quickly we realized that we would not have the time to make our app track the onboarding and payroll process. We decided that it would be better if we focused solely on facilitating the application, interviewing, and hiring process.
    As such, we delivered the first 4 of the above must-have requirements as well as several other features including a streamlined shortlisting process, the ability for an admin to see select attributes from an applicant’s profile (like Federal Work Study eligibility, etc.), and others. We also allow all users (students, professors, admin) to easily add and delete courses from their course list. This makes it super convenient for students who apply for different positions in different years as well as professors who teach different courses in different years. 
For the interview process, we originally wanted to integrate a calendar/scheduling feature within the app to allows users to put in their availability for interview - we decided to use an external scheduling tool instead and provided a space for the link to the external resource within our app

**Note the challenges you faced**

Trouble accomplish login with JHU authentication: Daniela was in charge of overseeing the login functionality and began working on integrating it starting at iteration 4. Her first attempts were misguided as she was trying to accomplish this through Microsoft Azure and “wasted” much of iteration 4 trying to use this method. At the end of it. 4, she used the other group’s guidance to get SAML login configuration based on a Pac4j demo. By the time she was able to properly configure the metadata file to send to employees in charge of maintaining the JHU IDP, and waiting for their responses, there were only 4 days left until the final deadline. Adequate testing for login with the JHU credentials required longer than these 4 days. We had a lot of problems with configuring the jar file so that the app could be deployed on Heroku. 

Another problem we had and that was extenuated by everyone working remotely, was that Daniela created the Java keystore file on her local machine, and the others could not configure their settings in such a way that they could troubleshoot the issues she was having. So, only one person could adequately try to troubleshoot.

We consistently ran into trouble each time we tried to deploy. One of the biggest bugs we encountered was that the name of the project was “hïre” with an umlaut over the ‘i’ and the heroku build wouldn’t work with this character. Another deployment issue we ran into frequently stemmed from inconsistencies in our build.gradle folder.

**Reflect on how you would have done it again if you could go back in time to iteration 1**

If we could go back in time to iteration 1 we would not want to split the work into front-end and back-end. People who worked on the front-end portion did not get to do much in the beginning since the app was built from the bottom up, focusing on lower level class and the database design. We then had to go back and explain the low level design to the people who wanted to work on the front-end. 

Around half-way into the project, we realized our database tables were over complicated and could be revised to be more concise and easy to use. We decided to refactor the tables, so that fewer joining tables were being used. This refactoring happened in the middle of one of our iterations but we could have simplified our development for all iterations had we thought of the better database design earlier.
