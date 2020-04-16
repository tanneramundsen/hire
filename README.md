# Hïre

Alpha version release on Heroku: https://oozers-hire.herokuapp.com/

Created by oozers - Tanner Amundsen, Jennifer Lin, Chester Huynh, Daniela Torres, Madhu Rajmohan

Iteration 3: (Things to note when testing)
- Currently, all the SIS classes in the CS department are added into our courses databases when you open the site. 
- There are 5 sample applicants that you can log in as: tamunds1, jlin123, mrajmoh1, dtorre17, xhuynh1. 
  - These applicants are currently "interested" in all of the classes in the CS department.
  - You can also create your own student account and select classes you are interested in.
  - After you sign in as a student, you will be directed to a landing page with all the classes you are interested in. 
  - Students can update their profile information through "My Profile". This includes:
      - Personal information including: Year, major, hours etc. (we mirrored the current CA application)
          - Some of these fields are required!
      - Ranking their top 3 classes (out of the classes that the student is interested in)
      - Adding any grades for classes that the student has taken (other classes have the grade "Not Taken).
      - Indicating interest in a head CA position for a specific class
      - Note that once the student updates this information, it persists the next time the student clicks on "My Profile"
  - Students can now click on a course to see additional information including: the professor who is hiring for this course, a course description (if the professor has added one), and the application status.
- There are no sample professors but you can create a professor account through the sign up page.
  - When creating a professor account, choose all the courses that the professor is teaching.
  - For each course, the professor can click on "Manage Course" see a list of interested applicants, shortlisted applicants and hired applicants.
      - The professor can also input a CA description and an interview link. 
  - The professor can checkbox interested applicants and add them to the shortlist.
  - When the professor clicks on an applicant's name in these lists, they will be able to see the profile of the student including their name, email, jhed, ranked courses, and grades etc. (if the student has inputted this information) and they also have the option to Shortlist the student here.
- On the main landing page, professors and applicants can choose additional courses to add to their interested course list.
