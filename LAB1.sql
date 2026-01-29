-- LAB 1
DROP TABLE student;
DROP TABLE course;
DROP TABLE grade_report;
CREATE TABLE student(
  name VARCHAR(100),
  student_number VARCHAR(20) PRIMARY KEY,  
  class VARCHAR(50),
  major VARCHAR(100)  
);

CREATE TABLE course(
    course_number NUMBER PRIMARY KEY,       
    course_name VARCHAR(100),
    credit_hours NUMBER,
    department VARCHAR(100)
);

CREATE TABLE prerequite(
    prerequisite_number NUMBER PRIMARY KEY,
    course_number NUMBER REFERENCES course(course_number)
);

CREATE TABLE section(
    section_identifier NUMBER PRIMARY KEY,
    course_number NUMBER REFERENCES course(course_number),
    semester NUMBER,
    year NUMBER,
    instructor VARCHAR(50)
);

CREATE TABLE grade_report(
    student_number VARCHAR(20) REFERENCES student(student_number),
    section_identifier NUMBER REFERENCES section(section_identifier),
    grade VARCHAR(2)
);

INSERT INTO student (name, student_number, class, major) VALUES
('Emily Johnson', 'S001', 'Junior', 'Computer Science');

INSERT INTO student (name, student_number, class, major) VALUES
('Michael Chen', 'S002', 'Sophomore', 'Biology');

INSERT INTO student (name, student_number, class, major) VALUES
('Sarah Williams', 'S003', 'Senior', 'Mathematics');

INSERT INTO student (name, student_number, class, major) VALUES
('David Martinez', 'S004', 'Freshman', 'Engineering');

INSERT INTO student (name, student_number, class, major) VALUES
('Jessica Brown', 'S005', 'Junior', 'Psychology');

INSERT INTO student (name, student_number, class, major) VALUES
('Ryan Thompson', 'S006', 'Senior', 'Computer Science');

INSERT INTO student (name, student_number, class, major) VALUES
('Amanda Garcia', 'S007', 'Sophomore', 'Business Administration');

INSERT INTO student (name, student_number, class, major) VALUES
('Christopher Lee', 'S008', 'Freshman', 'Chemistry');

INSERT INTO student (name, student_number, class, major) VALUES
('Jennifer Davis', 'S009', 'Junior', 'English Literature');

INSERT INTO student (name, student_number, class, major) VALUES
('Daniel Wilson', 'S010', 'Senior', 'Physics');

INSERT INTO course (course_number, course_name, credit_hours, department) VALUES
(101, 'Introduction to Computer Science', 3, 'Computer Science');

INSERT INTO course (course_number, course_name, credit_hours, department) VALUES
(102, 'Calculus I', 4, 'Mathematics');

INSERT INTO course (course_number, course_name, credit_hours, department) VALUES
(103, 'General Biology', 4, 'Biology');

INSERT INTO course (course_number, course_name, credit_hours, department) VALUES
(104, 'English Composition', 3, 'English');

INSERT INTO course (course_number, course_name, credit_hours, department) VALUES
(105, 'General Chemistry', 4, 'Chemistry');

INSERT INTO section (section_identifier, course_number, semester, year, instructor) VALUES
(1, 101, 1, 2024, 'Dr. Smith');

INSERT INTO section (section_identifier, course_number, semester, year, instructor) VALUES
(2, 102, 1, 2024, 'Dr. Johnson');

INSERT INTO section (section_identifier, course_number, semester, year, instructor) VALUES
(3, 103, 1, 2024, 'Dr. Williams');

INSERT INTO section (section_identifier, course_number, semester, year, instructor) VALUES
(4, 104, 1, 2024, 'Dr. Brown');

INSERT INTO section (section_identifier, course_number, semester, year, instructor) VALUES
(5, 105, 1, 2024, 'Dr. Davis');

INSERT INTO grade_report (student_number, section_identifier, grade) VALUES
('S001', 1, 'A');

INSERT INTO grade_report (student_number, section_identifier, grade) VALUES
('S001', 2, 'B');

INSERT INTO grade_report (student_number, section_identifier, grade) VALUES
('S002', 3, 'A');

INSERT INTO grade_report (student_number, section_identifier, grade) VALUES
('S002', 2, 'B');

INSERT INTO grade_report (student_number, section_identifier, grade) VALUES
('S003', 2, 'A');

INSERT INTO grade_report (student_number, section_identifier, grade) VALUES
('S003', 1, 'A');

INSERT INTO grade_report (student_number, section_identifier, grade) VALUES
('S004', 4, 'C');

INSERT INTO grade_report (student_number, section_identifier, grade) VALUES
('S004', 5, 'B');

INSERT INTO grade_report (student_number, section_identifier, grade) VALUES
('S005', 4, 'B');

INSERT INTO grade_report (student_number, section_identifier, grade) VALUES
('S005', 1, 'B');

INSERT INTO grade_report (student_number, section_identifier, grade) VALUES
('S006', 1, 'A');

INSERT INTO grade_report (student_number, section_identifier, grade) VALUES
('S006', 2, 'A');

INSERT INTO grade_report (student_number, section_identifier, grade) VALUES
('S007', 4, 'B');

INSERT INTO grade_report (student_number, section_identifier, grade) VALUES
('S007', 2, 'C');

INSERT INTO grade_report (student_number, section_identifier, grade) VALUES
('S008', 5, 'A');

INSERT INTO grade_report (student_number, section_identifier, grade) VALUES
('S008', 3, 'B');

INSERT INTO grade_report (student_number, section_identifier, grade) VALUES
('S009', 4, 'A');

INSERT INTO grade_report (student_number, section_identifier, grade) VALUES
('S009', 1, 'C');

INSERT INTO grade_report (student_number, section_identifier, grade) VALUES
('S010', 2, 'A');

INSERT INTO grade_report (student_number, section_identifier, grade) VALUES
('S010', 5, 'B');
