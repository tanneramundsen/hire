function validateStudentProfile() {
    const name = document.getElementById("name").value;
    if (name.length < 1) {
        alert("Please fill in your name");
        return false;
    }

    const email = document.getElementById("email").value;
    if (email.length < 1) {
        alert("Please fill in your email");
        return false;
    }

    const gpa = Number(document.getElementById("gpa").value);
    if (isNaN(gpa)) {
        alert("Please input a number for GPA");
        return false;
    } else if (gpa < 0 || gpa > 4) {
        alert("GPA should be between 0.0 and 4.0");
        return false;
    }

    const credits = Number(document.getElementById("credits").value);
    if (isNaN(credits)) {
        alert("Please input a number for credits");
        return false;
    } else if (credits < 0) {
        alert("Number of credits should be at least 0");
        return false;
    }

    const hoursAvailable = Number(document.getElementById("hoursAvailable").value);
    if (isNaN(hoursAvailable)) {
        alert("Please input a number for hours available");
        return false;
    } else if (credits < 0) {
        alert("Hours available should be at least 0");
        return false;
    }

    return true;
}