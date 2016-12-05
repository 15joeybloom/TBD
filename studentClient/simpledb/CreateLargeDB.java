import java.sql.*;
import simpledb.remote.SimpleDriver;
import java.util.Random;

public class CreateLargeDB {
    public static void main(String[] args) {
        Connection conn = null;
        try {
            Driver d = new SimpleDriver();
            conn = d.connect("jdbc:simpledb://localhost", null);
            Statement stmt = conn.createStatement();

            String s = "create table STUDENT(SId int, SName varchar(10), MajorId int, GradYear int)";
            stmt.executeUpdate(s);
            System.out.println("Table STUDENT created.");

            /*
            String[] studvals = {"(1, 'joe', 10, 2004)",
                "(2, 'amy', 20, 2004)",
                "(3, 'max', 10, 2005)",
                "(4, 'sue', 20, 2005)",
                "(5, 'bob', 30, 2003)",
                "(6, 'kim', 20, 2001)",
                "(7, 'art', 30, 2004)",
                "(8, 'pat', 20, 2001)",
                "(9, 'lee', 10, 2004)"};
            for (int i=0; i<studvals.length; i++)
                stmt.executeUpdate(s + studvals[i]);
            System.out.println("STUDENT records inserted.");
                */

            s = "create table DEPT(DId int, DName varchar(8))";
            stmt.executeUpdate(s);
            System.out.println("Table DEPT created.");

            s = "insert into DEPT(DId, DName) values ";
            String[] deptvals = {"(10, 'compsci')",
                "(20, 'math')",
                "(30, 'drama')"};
            for (int i=0; i<deptvals.length; i++)
                stmt.executeUpdate(s + deptvals[i]);
            System.out.println("DEPT records inserted.");

            s = "create table COURSE(CId int, Title varchar(20), DeptId int)";
            stmt.executeUpdate(s);
            System.out.println("Table COURSE created.");

            s = "insert into COURSE(CId, Title, DeptId) values ";
            String[] coursevals = {"(12, 'db systems', 10)",
                "(22, 'compilers', 10)",
                "(32, 'calculus', 20)",
                "(42, 'algebra', 20)",
                "(52, 'acting', 30)",
                "(62, 'elocution', 30)"};
            for (int i=0; i<coursevals.length; i++)
                stmt.executeUpdate(s + coursevals[i]);
            System.out.println("COURSE records inserted.");

            s = "create table SECTION(SectId int, CourseId int, Prof varchar(8), YearOffered int)";
            stmt.executeUpdate(s);
            System.out.println("Table SECTION created.");

            s = "insert into SECTION(SectId, CourseId, Prof, YearOffered) values ";
            String[] sectvals = {"(13, 12, 'turing', 2004)",
                "(23, 12, 'turing', 2005)",
                "(33, 32, 'newton', 2000)",
                "(43, 32, 'einstein', 2001)",
                "(53, 62, 'brando', 2001)"};
            for (int i=0; i<sectvals.length; i++)
                stmt.executeUpdate(s + sectvals[i]);
            System.out.println("SECTION records inserted.");

            s = "create table ENROLL(EId int, StudentId int, SectionId int, Grade int)";
            stmt.executeUpdate(s);
            System.out.println("Table ENROLL created.");

            String enrolls = "insert into ENROLL(EId, StudentId, SectionId, Grade) values ";
            String students = "insert into STUDENT(SId, SName, MajorId, GradYear) values ";
            int[] courseIds = new int[]{12,22,32,42,52,62};
            int[] sectionIds = new int[]{13, 23, 33, 43, 53};
            int[] deptIds = new int[]{10, 20, 30};

            long seed = 12345678;
            Random rand = new Random(seed);
            for(int i = 0; i < 100; i++) {
                int sid = 1000+i;
                String sname = String.format("Stud%04d", i);
                int majorId = deptIds[rand.nextInt(deptIds.length)];
                int gradyear = 2000+rand.nextInt(10);

                stmt.executeUpdate(students 
                    + String.format("(%d,'%s',%d,%d)", sid, sname, majorId, gradyear));

                for(int j = rand.nextInt(4); j >= 0; j--) {
                    int eid = sid*4+j;
                    int sectionId = sectionIds[rand.nextInt(sectionIds.length)];
                    int grade = 50+rand.nextInt(51);

                    stmt.executeUpdate(enrolls
                        + String.format("(%d,%d,%d,%d)", eid, sid, sectionId, grade));
                }
            }
            /*
            String[] enrollvals = {"(14, 1, 13, 97)",
                "(24, 1, 43, 70 )",
                "(34, 2, 43, 82)",
                "(44, 4, 33, 86 )",
                "(54, 4, 53, 94 )",
                "(64, 6, 53, 93 )",
                "(1, 1, 43, 41 )",
                "(2, 2, 43, 103)",
                "(3, 4, 33, 80 )",
                "(4, 4, 53, 69 )",
                "(5, 6, 53, 99 )",
                "(6, 5, 33, 91 )",
                "(10, 7, 43, 72 )",
                "(9, 7, 13, 65 )",
                "(11, 7, 53, 99 )",
                "(8, 5, 13, 100 )",
                "(7, 5, 43, 78 )",
                "(6, 5, 33, 91) "};
            for (int i=0; i<enrollvals.length; i++)
                stmt.executeUpdate(s + enrollvals[i]);
            System.out.println("ENROLL records inserted.");
            */

        }
        catch(SQLException e) {
            e.printStackTrace();
        }
        finally {
            try {
                if (conn != null)
                    conn.close();
            }
            catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}
