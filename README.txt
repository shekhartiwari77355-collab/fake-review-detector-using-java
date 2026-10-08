FAKE REVIEW DETECTOR
====================

Technology:
- Java
- HTML
- CSS
- JavaScript

Project type:
Simple rule-based web application for detecting suspicious/fake-looking reviews.

HOW TO RUN
----------

1. Install Java JDK 17 or newer.

2. Put all four project files in the same folder:
   FakeReviewDetector.java
   index.html
   style.css
   script.js

3. Open Command Prompt/Terminal in this folder.

4. Compile:
   javac FakeReviewDetector.java

5. Run:
   java FakeReviewDetector

6. Open this in your browser:
   http://localhost:8080

HOW THE DETECTOR WORKS
----------------------

The Java backend checks the review using simple rules.

Examples:
1. Promotional phrases -> adds points.
2. Too many exclamation marks -> adds points.
3. Repeated characters such as "soooo" -> adds points.
4. Very short review -> adds points.
5. Excessive CAPITAL letters -> adds points.
6. Repeated words -> adds points.

Final score:
0-24   = Likely Genuine
25-49  = Suspicious
50-100 = Likely Fake

IMPORTANT
---------

This is an educational project. The system is NOT a real machine-learning
model and cannot prove that a review is fake. It only detects suspicious
writing patterns using predefined rules.

VIVA EXPLANATION
----------------

Frontend:
HTML creates the webpage, CSS designs it, and JavaScript sends the review
to the Java backend and displays the result.

Backend:
Java uses the built-in HttpServer. It receives the review, applies the
detection rules, calculates a score, and sends the result back as JSON.

Why Java?
Java handles the main logic and acts as the backend server.

Why JavaScript?
JavaScript connects the webpage to the Java backend without reloading
the page.

No external libraries are required.
