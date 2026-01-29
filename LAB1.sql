-- LAB 1
DROP TABLE student;
DROP TABLE course;
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
    grade FLOAT
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