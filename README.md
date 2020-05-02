# Hïre

**Beta version release on Heroku**: https://oozers-hire.herokuapp.com/

**Postman test suite invitation**: https://app.getpostman.com/join-team?invite_code=8810c19b2e5a3d3024cdcec79f6e2c75&ws=667bb9e0-4540-48c5-95fa-3add0af7f404

Created by oozers - Tanner Amundsen, Jennifer Lin, Chester Huynh, Daniela Torres, Madhu Rajmohan

NOTE: In our directory we have two folders: hïre and hire
Both have the same code, the folder without ï was easier to deploy.

Iteration 5: (Things to note when testing)
- Currently, all the SIS classes in the CS department are added into our courses databases when you open the site. 
- There are a few sample applicants: tamunds1, jlin123, mrajmoh1, dtorre17, xhuynh1, wshake1, jvill12, psolan3, mcorder2, rsolan2, broche1. 
  - Some of these applicants are currently "interested" in all of the classes in the CS department.
  - The Applicants are also of varying years, grades, head CA interest etc. (this is to help demonstrate the filtering features)
  - You can also create your own student account and select classes you are interested in.
- After you sign in as a student, you will be directed to a landing page with all the classes you are interested in. 
  - Students can update their profile information through "My Profile". This includes:
      - Personal information including: Year, major, hours etc. (we mirrored the current CA application)
          - Some of these fields are required!
      - Ranking their top 3 classes (out of the classes that the student is interested in)
      - Adding any grades for classes that the student has taken (other classes have the grade "Not Taken).
      - Indicating interest in a head CA position for a specific class
      - Note that once the student updates this information, it persists the next time the student clicks on "My Profile"
  - Students can click on a course to see additional information including: the professor who is hiring for this course, a course description (if the professor has added one), and the application status.
      - The application status will be either: "Under Review", "Being interviewed" (along with an interview link that the professor needs to add, or "Hired!/Not hired!"
- There are no sample professors but you can create a professor account through the sign up page.
  - When creating a professor account, choose all the courses that the professor is teaching.
  - For each course, the professor can click on "Manage Course" see a list of interested applicants, shortlisted applicants and hired applicants.
      - The professor can also input a CA description and an interview link. 
      - The professor can also apply various filters:
          - Has previous CA experience
          - Has previously CAd this specific course
          - Has taken the course and gotten a grade above a B
          - Has Head CA interest
          - Student's year
      - Note that selecting various years as filters will preform a union rather than intersection since there exists no student who is multiple years.
  - The professor can checkbox interested applicants and add them to the shortlist.
  - The professor can checkbox shortlisted applicants and add them to hired or delete them from the shortlist.
  - The professor can checkbox hired applicants and delete them from hired.
  - The professor can make the interview link visible to the students on the shortlist. On the student's side, their application status will change to "Being interviewed" and display an interview link. This button also disables professors from being able to delete students from the shortlist.
  - The professor can send out hiring notification to the students on the hired list. On the student's side, their application status will change to "Hired!". This button also disables professors from being able to delete students from the hired list. 
  - When the professor clicks on an applicant's name in these lists, they will be able to see the profile of the student including their name, email, jhed, ranked courses, and grades etc. (if the student has inputted this information) and they also have the option to Shortlist the student here.
- There are no sample admins but you can create an admin account through the sign up page.
  - The admin account has the mostly the same functionality as staff member.
  - However, the admin will automatically have access to all the courses. This allows the admin to have full visibility of every course's interested, shortlisted, and hired applicants.
  - Furthermore, when an admin selects an applicant to view their profile, some additional information such as FWS eligibility, student status and others will be available. (These fields are not available to regular staff members)
- On the main landing page, all users can choose additional courses to add to their interested course list or delete courses from their interested course list.
