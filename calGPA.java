import java.io.*;
import java.sql.*;
import java.util.Scanner;
//javac -cp ojdbc17.jar calGPA.java
//java -cp .:ojdbc17.jar calGPA
class calGPA{
    public static void main (String args[]) throws SQLException, IOException{
        try{
            // Load Oracle JDBC Driver
            Class.forName("oracle.jdbc.driver.OracleDriver");
        }catch (ClassNotFoundException x){
            System.out.println("Driver could not be loaded.");
        }
        // Creating scanner object to read input
        Scanner scanner = new Scanner(System.in);
        String dbacct, passwrd, name; // Initialize variables for db connection
        char grade;
        int credit;
        //use scanner to record user input
        System.out.println("Enter database account: "); 
        dbacct = scanner.nextLine();
        System.out.println("Enter password: ");
        passwrd = scanner.nextLine();
        // Create a connection to oracle db using user input username and password
        Connection conn = DriverManager.getConnection("jdbc:oracle:thin:@orsrv1.cs.torontomu.ca:1521/orcldb", dbacct, passwrd);
        String stmt1 = "select G.Grade, C.Credit_hours from STUDENT S, GRADE_REPORT G, SECTION SEC, COURSE C where G.Student_number=S.Student_number AND G.Section_identifier=SEC.Section_identifier AND SEC.Course_number=C.Course_number AND TRIM(S.Name)=?";
        PreparedStatement p = conn.prepareStatement(stmt1); // Create a prepared statement p from the sql command
        System.out.println("Please enter your name: "); // Ask user for student name for whom to calculate GPA
        name = scanner.nextLine();
        scanner.close();
        p.clearParameters(); // clear all previous parameters on the prepared statement
        p.setString(1, name); // sets pos1 paramerter to value of name, replacing ? in sql command
        ResultSet r = p.executeQuery(); // execute query and store result in ResultSet r
        double count=0, sum=0, avg=0;
        while(r.next()){
            grade = r.getString(1).charAt(0); // extract grade and credit hours from result set
            credit = r.getInt(2);
            switch (grade){ // switch case to calculate grade points based on grade received
                case 'A': sum=sum+(4*credit); count=credit+1; break;
                case 'B': sum=sum+(3*credit); count=credit+1; break;
                case 'C': sum=sum+(2*credit); count=credit+1; break;
                case 'D': sum=sum+(1*credit); count=credit+1; break;
                case 'F': sum=sum+(0*credit); count=credit+1; break;
                default: System.out.println("This grade "+grade+" will not be calculated."); break;
            }
        };
        avg = sum/count; // calculate GPA
        System.out.println("Student named "+name+" has a grade point average "+avg+".");
        r.close();
    }
}
