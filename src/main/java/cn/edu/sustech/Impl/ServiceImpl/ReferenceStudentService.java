package cn.edu.sustech.Impl.ServiceImpl;

import cn.edu.sustech.Impl.Utils.PrerequisiteUtils;
import cn.edu.sustech.cs307.database.SQLDataSource;
import cn.edu.sustech.cs307.dto.*;
import cn.edu.sustech.cs307.dto.grade.Grade;
import cn.edu.sustech.cs307.dto.grade.HundredMarkGrade;
import cn.edu.sustech.cs307.dto.grade.PassOrFailGrade;
import cn.edu.sustech.cs307.exception.EntityNotFoundException;
import cn.edu.sustech.cs307.exception.IntegrityViolationException;
import cn.edu.sustech.cs307.service.StudentService;
import cn.edu.sustech.Impl.ServiceImpl.ReferenceCourseService;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.io.IOException;
import java.net.ConnectException;
import java.sql.*;
import java.sql.Date;
import java.time.DayOfWeek;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@ParametersAreNonnullByDefault

public class ReferenceStudentService implements StudentService {

    static SQLDataSource sqlDataSource = SQLDataSource.getInstance();


    public static void main(String[] args) {
        ReferenceStudentService referenceStudentService = new ReferenceStudentService();
        Grade grade = new HundredMarkGrade((short) 33);
        System.out.println(grade.toString());
    }



    @Override//finished
    public void addStudent(int userId, int majorId, String firstName, String lastName, Date enrolledDate) {

        Connection conn = null;
        String sql;
        PreparedStatement preparedStatement = null;
        ResultSet rst = null;

        try {
            conn = sqlDataSource.getSQLConnection();
            sql = "insert into student(student_id, first_name, last_name, enrolled_date) values (?,?,?,?)";
            preparedStatement = conn.prepareStatement(sql);
            preparedStatement.setString(1, "" + userId);
            preparedStatement.setString(2,firstName);
            preparedStatement.setString(3,lastName);
            preparedStatement.setDate(4,enrolledDate);
            preparedStatement.executeUpdate();
            sql = "insert into student_to_major(student_id, major_id) values (?,?)";
            preparedStatement = conn.prepareStatement(sql);
            preparedStatement.setString(1,"" + userId);
            preparedStatement.setInt(2,majorId);
            preparedStatement.executeUpdate();

        }
        catch (Exception throwable){
            throwable.printStackTrace();
        }
        finally{
            doFinally(rst, preparedStatement, conn);
        }
    }

    @Override
    public List<CourseSearchEntry> searchCourse(int studentId, int semesterId, @Nullable String searchCid, @Nullable String searchName, @Nullable String searchInstructor, @Nullable DayOfWeek searchDayOfWeek, @Nullable Short searchClassTime, @Nullable List<String> searchClassLocations, CourseType searchCourseType, boolean ignoreFull, boolean ignoreConflict, boolean ignorePassed, boolean ignoreMissingPrerequisites, int pageSize, int pageIndex) {
        Connection conn = null;
        String sql;
        PreparedStatement preparedStatement = null;
        ResultSet rst = null;
        List<CourseSearchEntry> list = new ArrayList<>();
        try {
            conn = sqlDataSource.getSQLConnection();
            sql = "select * from search_func(?, ?, ?, ?, ?, ?, ?, ?, ?, ?,\n" +
                    "                      ?, ?, ?, ?, ?, ?, ?, ?, ?, ?,\n" +
                    "                      ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
            preparedStatement = conn.prepareStatement(sql);
            for (int i = 1; i <= 30; i++){
                if (i == 9 || i == 15 || i == 18){
                    preparedStatement.setInt(i, i);
                }else if (i == 21) {
                    preparedStatement.setArray(21, null);
                }else{
                    preparedStatement.setString(i, "" + i);
                }
            }
            getPara(searchCid, searchName, searchInstructor, searchDayOfWeek, searchClassTime, searchClassLocations,preparedStatement,conn);
            if (ignoreFull){
                preparedStatement.setString(22, "");
                preparedStatement.setString(23, "");
            }

            switch (searchCourseType){
                case CROSS_MAJOR:
                    preparedStatement.setString(7, "R");
                    break;
                case MAJOR_COMPULSORY:
                    preparedStatement.setString(7, "C");
                    break;
                case MAJOR_ELECTIVE:
                    preparedStatement.setString(7, "E");
                    break;
                case ALL:
                    preparedStatement.setString(7, "A");
                    break;
                case PUBLIC:
                    preparedStatement.setString(7, "P");
                    break;
            }


            preparedStatement.setString(8, "" + studentId);
            preparedStatement.setInt(9, semesterId);
            rst = preparedStatement.executeQuery();
            CourseSearchEntry courseSearchEntry;
            Course course;
            CourseSection courseSection;
            CourseSectionClass courseSectionClass;
            Instructor instructor;
            List<Integer> sectionIDList = new ArrayList<>();
            while(rst.next()) {
                course = new Course();
                courseSection = new CourseSection();
                courseSectionClass = new CourseSectionClass();
                courseSearchEntry = new CourseSearchEntry();

                course.id = rst.getString("course_id1");
                course.name = rst.getString("course_name1");
                course.credit = rst.getInt("credit1");
                course.classHour = rst.getInt("class_hour1");
                String s = rst.getString("grading1");
                if(s.equals("P")){
                    course.grading = Course.CourseGrading.PASS_OR_FAIL;
                }else{
                    course.grading = Course.CourseGrading.HUNDRED_MARK_SCORE;
                }
                if (course.id.equals("CS307")){
                    System.out.print("");
                }
                if (ignorePassed){
                    sql = "select course_id, sc.* from\n" +
                            "    (course_to_coursesection\n" +
                            "        inner join\n" +
                            "    student_coursesection sc on course_to_coursesection.section_id = sc.section_id\n" +
                            "        inner join \n" +
                            "    coursesection_to_semester cts on cts.section_id = sc.section_id)\n" +
                            "where course_id = ? and student_id = ? and (state = 'pass')";
                    preparedStatement = conn.prepareStatement(sql);
                    preparedStatement.setString(1, course.id);
                    preparedStatement.setString(2, "" + studentId);
                    ResultSet rst1 = preparedStatement.executeQuery();
                    if (rst1.next()){
                        rst1.close();
                        continue;
                    }
                }


                courseSection.id = rst.getInt("section_id1");
                courseSection.name = rst.getString("section_name1");
                courseSection.totalCapacity = rst.getInt("totalcapacity1");
                courseSection.leftCapacity = rst.getInt("leftcapacity1");

                if (sectionIDList.contains(courseSection.id)){
                    int index = sectionIDList.indexOf(courseSection.id);
                    getClassInfo(courseSectionClass,rst);
                    list.get(index).sectionClasses.add(courseSectionClass);
                    continue;
                }

                Set<CourseSectionClass> classList = new HashSet<>();
                getClassInfo(courseSectionClass,rst);
                classList.add(courseSectionClass);

                courseSearchEntry.course = course;
                courseSearchEntry.section = courseSection;
                courseSearchEntry.sectionClasses = classList;
                list.add(courseSearchEntry);
                sectionIDList.add(courseSection.id);
            }

            List<String> conflictList;
            List<CourseSearchEntry> deleteList = new ArrayList<>();

            if (ignoreMissingPrerequisites){
                for (CourseSearchEntry courseSearchEntry1 : list){
                    if (!passedPrerequisitesForCourse(studentId, courseSearchEntry1.course.id)) {
                        deleteList.add(courseSearchEntry1);
                    }
                }
            }
            if(studentId == 11711705){
                System.out.print("");
            }
            List<CourseTime> classTimes = getCourseList(studentId);
            String fullName;

            for (CourseSearchEntry courseSearchEntry1 : list) {
                conflictList = new ArrayList<>();
                for (CourseSectionClass class1 : courseSearchEntry1.sectionClasses) {
                    for (CourseTime classTime : classTimes) {
                        if (studentId == 11716141 && courseSearchEntry1.course.id.equals("CS102A") && classTime.courseId.equals("CS102A")){
                            System.out.println();
                        }
                        if (classTime.semester != semesterId) {
                            continue;
                        }
                        if (class1.dayOfWeek.getValue() != classTime.day) {
                            continue;
                        }
                        if ((ReferenceCourseService.cvtListToInt(class1.weekList) & classTime.week) == 0) {
                            continue;
                        }
                        if (class1.classBegin <= classTime.end && classTime.begin <= class1.classEnd) {
                            if (ignoreConflict) {
                                deleteList.add(courseSearchEntry1);
                            }
                            if (!conflictList.contains(classTime.name)) {
                                conflictList.add(classTime.name);
                            }
                        }
                    }
                    courseSearchEntry1.conflictCourseNames = conflictList;
                }
            }

            for (CourseSearchEntry courseSearchEntry1 : deleteList){
                list.remove(courseSearchEntry1);
            }
            deleteList.clear();

            for (CourseSearchEntry courseSearchEntry1 : list) {
                sql = "select * from\n" +
                        "    (select course_id, sc.* from\n" +
                        "        (course_to_coursesection\n" +
                        "            inner join\n" +
                        "        student_coursesection sc on course_to_coursesection.section_id = sc.section_id\n" +
                        "            inner join\n" +
                        "        coursesection_to_semester cts on cts.section_id = sc.section_id)\n" +
                        "     where course_id = ? and student_id = ? and (semester_id = ?))now\n" +
                        "        inner join\n" +
                        "    coursesection cs on cs.section_id = now.section_id\n" +
                        "        inner join course c on c.course_id = now.course_id";
                preparedStatement = conn.prepareStatement(sql);
                preparedStatement.setString(1, courseSearchEntry1.course.id);
                preparedStatement.setString(2, "" + studentId);
                preparedStatement.setInt(3, semesterId);
                ResultSet rst1 = preparedStatement.executeQuery();
                boolean flag = rst1.next();
                if (ignoreConflict && flag) {
                    deleteList.add(courseSearchEntry1);
                }
                if (flag){
                    fullName = rst1.getString("course_name") + '[' + rst1.getString("section_name") + ']';
                    if (!courseSearchEntry1.conflictCourseNames.contains(fullName)){
                        courseSearchEntry1.conflictCourseNames.add(fullName);
                    }
                }
                rst1.close();
            }
            for (CourseSearchEntry courseSearchEntry1 : deleteList){
                list.remove(courseSearchEntry1);
            }
        }
        catch (Exception throwable){
            throwable.printStackTrace();
        }
        finally{
            doFinally(rst, preparedStatement, conn);
        }
        list.sort((o1, o2) -> {
            if (o1.course.id.equals(o2.course.id)){
                String s1 = o1.course.name + "[" + o1.section.name + "]";
                String s2 = o2.course.name + "[" + o2.section.name + "]";
                return s1.toUpperCase(Locale.ROOT).compareTo(s2.toUpperCase(Locale.ROOT));
            } else {
                return o1.course.id.toUpperCase(Locale.ROOT).compareTo(o2.course.id.toUpperCase(Locale.ROOT));
            }
        });
        List<CourseSearchEntry> entries = new ArrayList<>();
        for (int i = pageIndex * pageSize; i < Math.min(list.size(), (pageIndex + 1) * pageSize); i++){
            list.get(i).conflictCourseNames.sort(String::compareTo);
            entries.add(list.get(i));
        }
        return entries;
    }

    @Override
    public  EnrollResult enrollCourse(int studentId, int sectionId) {
        //System.out.println(studentId+" enroll "+sectionId);
        Connection conn = null;
        String sql;
        PreparedStatement preparedStatement = null;
        ResultSet rst = null;
        try {
            //从coursesection中查询，如果没查到就是COURSE_NOT_FOUND
            conn = sqlDataSource.getSQLConnection();
            sql = "select * from coursesection\n" +
                    "    where section_id = ?";
            preparedStatement = conn.prepareStatement(sql);
            preparedStatement.setInt(1, sectionId);
            rst = preparedStatement.executeQuery();

            if (!rst.next()){
                return EnrollResult.COURSE_NOT_FOUND;
            }

            /*
            ALREADY_ENROLLED:
            1.首先，根据sectionid和studentid在student_course中选出
            2.如果存在记录
                (1).就说明在本学期sectionid已经选过了，ALREADY_ENROLLED
             */
            sql = "select * from student_coursesection\n" +
                    "    where section_id = ? and student_id = ?";
            preparedStatement = conn.prepareStatement(sql);
            preparedStatement.setInt(1, sectionId);
            preparedStatement.setString(2, "" + studentId);
            rst = preparedStatement.executeQuery();

            if (rst.next()){
                return EnrollResult.ALREADY_ENROLLED;
            }

            //根据我要选的coursesectionId把对应的course和semester拿到
            sql = "select course_id,cts.semester_id from course_to_coursesection inner join coursesection_to_semester cts on course_to_coursesection.section_id = cts.section_id\n" +
                    "where cts.section_id = ?";
            preparedStatement = conn.prepareStatement(sql);
            preparedStatement.setInt(1, sectionId);
            rst = preparedStatement.executeQuery();
            rst.next();
            String courseID = rst.getString("course_id");
            int semesterId = rst.getInt("semester_id");
            //如果有course
            if (courseID!=null) {

                sql = "select *, count(*) over () as cnt from\n" +
                        "    (course_to_coursesection\n" +
                        "        inner join\n" +
                        "    student_coursesection sc on course_to_coursesection.section_id = sc.section_id)\n" +
                        "where course_id = ? and student_id = ? and state = 'pass'";
                preparedStatement = conn.prepareStatement(sql);
                preparedStatement.setString(1, courseID);
                preparedStatement.setString(2, "" + studentId);
                rst = preparedStatement.executeQuery();
                //如果有数据，说明学生已经过了
                if (rst.next()) {
                    return EnrollResult.ALREADY_PASSED;
                }
                //如果没有数据，判断先修课
                if (!passedPrerequisitesForCourse(studentId, courseID)){
                    return EnrollResult.PREREQUISITES_NOT_FULFILLED;
                }
            }


            //第一步判断，时间是否有冲突
            List<CourseTime> courseTimes = new ArrayList<>();
            //courseTimes:这个sectionId对应的所有class的时间
            getClassList(sectionId, courseTimes, conn);
            //enrolledList:这个学生选择的所有section对应的时间
            List<CourseTime> enrolledList = getCourseListWithSemester(studentId,semesterId);


            for (CourseTime courseTime1 : courseTimes){

                for (CourseTime courseTime2 : enrolledList){
                    if (courseTime1.day != courseTime2.day){
                        continue;
                    }
                    if ((courseTime1.week & courseTime2.week) == 0){
                        continue;
                    }
//                    System.out.printf("The interval of Time1 is [%d,%d]\n",courseTime1.begin,courseTime1.end);
//                    System.out.printf("The interval of Time2 is [%d,%d]\n",courseTime2.begin,courseTime2.end);
                    //如果这个classTime存在冲突,那么这个coursesection的这个class就有冲突了
                    if ((courseTime1.begin <= courseTime2.end && courseTime2.begin <= courseTime1.end)){
                        return EnrollResult.COURSE_CONFLICT_FOUND;
                    }
                }
            }


            //第二步判断：我选的这个coursesection对应的course是不是已经被选过了
            sql = "select * from (select ctc.course_id from student_coursesection inner join course_to_coursesection ctc on student_coursesection.section_id = ctc.section_id\n" +
                    "                                    inner join coursesection_to_semester cts on ctc.section_id = cts.section_id\n" +
                    "                                   where student_id = ? and cts.semester_id = ?)x\n" +
                    "                    where x.course_id = ?";
            preparedStatement = conn.prepareStatement(sql);
            preparedStatement.setString(1,""+studentId);
            preparedStatement.setInt(2,semesterId);
            preparedStatement.setString(3,courseID);
            rst = preparedStatement.executeQuery();
            if(rst.next()) return EnrollResult.COURSE_CONFLICT_FOUND;


            sql = "select * from coursesection\n" +
                    "    where section_id = ?";
            preparedStatement = conn.prepareStatement(sql);
            preparedStatement.setInt(1, sectionId);
            rst = preparedStatement.executeQuery();
            if (rst.next() && rst.getInt("leftcapacity") <= 0){
                return EnrollResult.COURSE_IS_FULL;
            }

            sql = "insert into student_coursesection\n" +
                    "values (?,?,'enroll',null);" +
                    "update coursesection set leftcapacity = leftcapacity - 1\n" +
                    "                    where section_id = ?";
            preparedStatement = conn.prepareStatement(sql);
            preparedStatement.setString(1, "" + studentId);
            preparedStatement.setInt(2,sectionId);
            preparedStatement.setInt(3,sectionId);
            preparedStatement.executeUpdate();

        } catch (SQLException throwables) {
            doFinally(rst, preparedStatement, conn);

            return EnrollResult.UNKNOWN_ERROR;
        }
        finally{
            doFinally(rst, preparedStatement, conn);
        }
        return EnrollResult.SUCCESS;

    }

    private void getClassList(int sectionId, List<CourseTime> courseTimes, Connection conn) {
        Connection conn1 = null;
        String sql;
        PreparedStatement preparedStatement = null;
        ResultSet rst = null;
        try {
            sql = "select * from getclassinfo(?)";
            preparedStatement = conn.prepareStatement(sql);
            preparedStatement.setInt(1, sectionId);
            rst = preparedStatement.executeQuery();
            while (rst.next()){
                int day = rst.getInt("weekday");
                int weekList = rst.getInt("weeknum");
                int classBegin = rst.getInt("begintime");
                int classEnd = rst.getInt("endtime");
                int semester = rst.getInt("semesterid");
                String name = rst.getString("coursename") + '[' + rst.getString("sectionname") + ']';
                String courseId = rst.getString("courseid");
                CourseTime courseTime = new CourseTime(day, weekList, classBegin, classEnd, semester, name, courseId);
                courseTimes.add(courseTime);
            }
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        finally {
            doFinally(rst, preparedStatement, conn1);
        }

    }

    public List<CourseTime> getCourseList(int studentId){
        Connection conn = null;
        String sql;
        PreparedStatement preparedStatement = null;
        ResultSet rst = null;

        List<CourseTime> enrolledList = new ArrayList<>();
        try {
            conn = sqlDataSource.getSQLConnection();
            sql = "select section_id from student_coursesection\n" +
                    "    where student_id = ?";
            preparedStatement = conn.prepareStatement(sql);
            preparedStatement.setString(1, "" + studentId);
            rst = preparedStatement.executeQuery();
            int sectionID;
            List<Integer> sectionList = new ArrayList<>();
            while (rst.next()){
                sectionID = rst.getInt("section_id");
                sectionList.add(sectionID);
            }
            for (int k : sectionList){
                getClassList(k,  enrolledList, conn);
            }
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        finally {
            doFinally(rst, preparedStatement, conn);
            }
        return enrolledList;
    }
    public List<CourseTime> getCourseListWithSemester(int studentId,Integer semesterId){
        Connection conn = null;
        String sql;
        PreparedStatement preparedStatement = null;
        ResultSet rst = null;

        List<CourseTime> enrolledList = new ArrayList<>();
        try {
            conn = sqlDataSource.getSQLConnection();
            sql = "select * from student_coursesection sc\n" +
                    "inner join coursesection_to_semester cts on sc.section_id = cts.section_id"+
                    "    where student_id = ? and cts.semester_id = ?";
            preparedStatement = conn.prepareStatement(sql);
            preparedStatement.setString(1, "" + studentId);
            preparedStatement.setInt(2,semesterId);
            rst = preparedStatement.executeQuery();
            int sectionID;
            List<Integer> sectionList = new ArrayList<>();
            while (rst.next()){
                sectionID = rst.getInt("section_id");
                sectionList.add(sectionID);
            }
            for (int k : sectionList){
                getClassList(k,  enrolledList, conn);
            }
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        finally {
            doFinally(rst, preparedStatement, conn);
        }
        return enrolledList;
    }

    @Override
    public  void dropCourse(int studentId, int sectionId) throws IllegalStateException {
        Connection conn = null;
        String sql;
        PreparedStatement preparedStatement = null;
        ResultSet rst = null;

        try {
            conn = sqlDataSource.getSQLConnection();
            sql = "select * from student_coursesection\n" +
                    "where student_id = ? and section_id = ?";
            preparedStatement = conn.prepareStatement(sql);
            preparedStatement.setString(1, "" + studentId);
            preparedStatement.setInt(2, sectionId);
            rst = preparedStatement.executeQuery();
            if (rst.next()) {
                if (rst.getString("state").equals("enroll")) {
                    sql = "delete from student_coursesection\n" +
                            "where student_id = ? and section_id = ?;" +
                            "update coursesection set leftcapacity = leftcapacity + 1 \n" +
                            "where section_id = ?";
                    preparedStatement = conn.prepareStatement(sql);
                    preparedStatement.setString(1, "" + studentId);
                    preparedStatement.setInt(2, sectionId);
                    preparedStatement.setInt(3,sectionId);
                    preparedStatement.executeUpdate();
                } else {
                    throw new IllegalStateException();
                }
            } else {
                throw new IllegalStateException();
            }
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        finally {
            doFinally(rst, preparedStatement, conn);
        }
    }

    @Override
    public void addEnrolledCourseWithGrade(int studentId, int sectionId, @Nullable Grade grade) {
        Connection conn = null;
        String sql;
        PreparedStatement preparedStatement = null;
        ResultSet rst = null;
        //把course选出来
        if (studentId == 11718169 && grade == null){
            System.out.print("");
        }
        try {
            conn = sqlDataSource.getSQLConnection();
            sql = "select * from\n" +
                    "    course_to_coursesection cc\n" +
                    "        inner join\n" +
                    "    course c  on c.course_id = cc.course_id\n" +
                    "where section_id = ?";
            preparedStatement = conn.prepareStatement(sql,ResultSet.TYPE_SCROLL_SENSITIVE,ResultSet.CONCUR_UPDATABLE);
            preparedStatement.setInt(1, sectionId);
            rst = preparedStatement.executeQuery();
            boolean haveCourse = false;
            char dbGrading = 1;
            if (rst.next()){
                haveCourse = true;
                dbGrading = rst.getString("grading").charAt(0);
            }
            char grading = dbGrading;
            String grade1 = null;
            if (grade != null) {
                grading = grade.when(new Grade.Cases<>() {
                    @Override
                    public Character match(PassOrFailGrade self) {
                        return 'P';
                    }

                    @Override
                    public Character match(HundredMarkGrade self) {
                        return 'H';
                    }
                });
                grade1 = grade.when(new Grade.Cases<>() {
                    @Override
                    public String match(PassOrFailGrade self) {
                        return self.name();
                    }

                    @Override
                    public String match(HundredMarkGrade self) {
                        return Short.toString(self.mark);
                    }
                });
            }
            boolean gradeType = grading != dbGrading;

            boolean haveStudent = true;
            sql = "select * from student\n" +
                    "where student_id = ?";
            preparedStatement = conn.prepareStatement(sql,ResultSet.TYPE_SCROLL_SENSITIVE,ResultSet.CONCUR_UPDATABLE);
            preparedStatement.setString(1, "" + studentId);
            rst = preparedStatement.executeQuery();
            rst.last();
            if (rst.getRow() == 0) {
                haveStudent = false;
            }

            if (!haveCourse || !haveStudent || gradeType) {
                throw new IntegrityViolationException();
            }
            sql = "insert into student_coursesection(student_id, section_id, state, grade) \n" +
                    "values (?,?,?,?)";
            preparedStatement = conn.prepareStatement(sql, ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);
            preparedStatement.setString(1, "" + studentId);
            preparedStatement.setInt(2, sectionId);
            preparedStatement.setNull(4, Types.INTEGER);
            if (grade == null){
                preparedStatement.setString(3, "graded");
            }else if (grade1.equals("PASS")) {
                preparedStatement.setString(3, "pass");
            } else if (grade1.equals("FAIL")) {
                preparedStatement.setString(3, "fail");
            } else if (Integer.parseInt(grade1) >= 60) {
                preparedStatement.setString(3, "pass");
                preparedStatement.setInt(4, Integer.parseInt(grade1));
            } else if (Integer.parseInt(grade1) < 60) {
                preparedStatement.setString(3, "fail");
                preparedStatement.setInt(4, Integer.parseInt(grade1));
            }
            preparedStatement.execute();
        } catch (Exception ignored) {

        } finally {
            doFinally(rst, preparedStatement, conn);
        }
    }

    @Override
    public void setEnrolledCourseGrade(int studentId, int sectionId, Grade grade) {
        Connection conn = null;
        String sql;
        PreparedStatement preparedStatement = null;
        ResultSet rst = null;

        try {
            char grading;
            String grade1;
            conn = sqlDataSource.getSQLConnection();
            grading = grade.when(new Grade.Cases<>() {
                @Override
                public Character match(PassOrFailGrade self) {
                    return 'P';
                }

                @Override
                public Character match(HundredMarkGrade self) {
                    return 'H';
                }
            });
            grade1 = grade.when(new Grade.Cases<>() {
                @Override
                public String match(PassOrFailGrade self) {
                    return self.name();
                }

                @Override
                public String match(HundredMarkGrade self) {
                    return Short.toString(self.mark);
                }
            });
            if (grading == 'P') {
                if (grade1.equals("PASS")) {
                    sql = "update student_coursesection set state = 'pass'\n" +
                            "where student_id = ? and section_id = ?";
                } else {
                    sql = "update student_coursesection set state = 'fail'\n" +
                            "where student_id = ? and section_id = ?";
                }
                preparedStatement = conn.prepareStatement(sql);
                preparedStatement.setString(1, "" + studentId);
                preparedStatement.setInt(2, sectionId);

            } else {
                if (Integer.parseInt(grade1) >= 60) {
                    sql = "update student_coursesection set state = 'pass', grade = ?\n" +
                            "where student_id = ? and section_id = ?";
                } else {
                    sql = "update student_coursesection set state = 'fail', grade = ?\n" +
                            "where student_id = ? and section_id = ?";
                }
                preparedStatement = conn.prepareStatement(sql);
                preparedStatement.setInt(1, Integer.parseInt(grade1));
                preparedStatement.setString(2, "" + studentId);
                preparedStatement.setInt(3, sectionId);
            }
            preparedStatement.execute();
        } catch (SQLException throwables) {
            throw new EntityNotFoundException();
        } finally {
            doFinally(rst, preparedStatement, conn);
        }
    }


    @Override
    public Map<Course, Grade> getEnrolledCoursesAndGrades(int studentId, @Nullable Integer semesterId) {
        Connection conn = null;
        String sql;
        PreparedStatement preparedStatement = null;
        ResultSet rst = null;
        //定义返回对象
        Map<Course,Grade> map = new HashMap<>();
        //为了处理之前学期先修过，但是后面学期过了的这种情况，只记录后面学期的成绩
        Set<String> courseIdSet = new HashSet<>();
        HashMap<String,Date> courseIdToSemester = new HashMap<>();
        Course course;
        Grade g;
        try {
            conn = sqlDataSource.getSQLConnection();
            if(semesterId == null) {
                sql = "select * from getAllEnrolledCourseAndGrade(?)";
                preparedStatement = conn.prepareStatement(sql);
                preparedStatement.setString(1,studentId+"");
            }
            else{
                sql = "select * from getEnrolledCoursesAndGrades(?,?)";
                preparedStatement = conn.prepareStatement(sql);
                preparedStatement.setString(1,studentId+"");
                preparedStatement.setInt(2,semesterId);
            }
            rst = preparedStatement.executeQuery();
            course = new Course();
            course.id = rst.getString("course_id");
            course.name = rst.getString("course_name");
            course.credit = rst.getInt("credit");
            course.classHour = rst.getInt("class_hour");
            String grading = rst.getString("grading");
            course.grading = grading.equals("H") ? Course.CourseGrading.HUNDRED_MARK_SCORE : Course.CourseGrading.PASS_OR_FAIL;
            String state = rst.getString("state");
            //可能为nulls
            Integer grade = rst.getInt("grade");
            Date date = rst.getDate("begin_date");
            g = getCourseGrade(state,grade);

            if(semesterId == null)
            {
                if(courseIdSet.contains(course.id) && courseIdToSemester.get(course.id).before(date))
                {
                    map.replace(course,g);
                }else if(!courseIdSet.contains(course.id)){
                    courseIdSet.add(course.id);
                    courseIdToSemester.put(course.id,date);
                    map.put(course,g);
                }
            }else map.put(course,g);

        } catch (SQLException throwables) {
            throw new IntegrityViolationException();
        } finally {
            doFinally(rst,preparedStatement,conn);
        }
        return map;
    }

    @Override
    public CourseTable getCourseTable(int studentId, Date date) {
        Connection conn = null;
        String sql;
        PreparedStatement preparedStatement = null;
        ResultSet rst = null;
        CourseTable courseTable = new CourseTable();
        courseTable.table = new HashMap<>();
        if (studentId == 11712447){
            System.out.print("");
        }
        try {
            conn = sqlDataSource.getSQLConnection();
            sql = "select * from getStudentCourseTable(?,?,?)";
            for(int i = 1; i <= 7; i++)
            {
                preparedStatement = conn.prepareStatement(sql);
                preparedStatement.setString(1,studentId+"");
                preparedStatement.setDate(2,date);
                preparedStatement.setInt(3,i);
                rst = preparedStatement.executeQuery();
                Set<CourseTable.CourseTableEntry> courseTableEntries = new HashSet<>();
                while(rst.next())
                {
                    CourseTable.CourseTableEntry courseTableEntry = new CourseTable.CourseTableEntry();
                    courseTableEntry.courseFullName = String.format("%s[%s]", rst.getString("course_name"), rst.getString("section_name"));
                    courseTableEntry.instructor = new Instructor();
                    courseTableEntry.instructor.id = Integer.parseInt(rst.getString("instructor_id"));
                    courseTableEntry.instructor.fullName = getFullName(rst.getString("first_name"),rst.getString("last_name"));
                    courseTableEntry.classBegin = (short)rst.getInt("class_begin");
                    courseTableEntry.classEnd = (short)rst.getInt("class_end");
                    courseTableEntry.location = rst.getString("location");
                    courseTableEntries.add(courseTableEntry);
                }
                courseTable.table.put(DayOfWeek.of(i),courseTableEntries);
            }

        } catch (SQLException throwables) {
            throw new IntegrityViolationException();
        }finally{
            doFinally(rst,preparedStatement,conn);
        }
        return courseTable;
    }

    @Override
    public boolean passedPrerequisitesForCourse(int studentId, String courseId) {
        Connection conn = null;
        String sql;
        PreparedStatement preparedStatement = null;
        ResultSet rst = null;
        ArrayList<String> passedCourseId = new ArrayList<>();
        //courseId对应的先修课字符串
        String prerequisite = null;
        try {
            conn = sqlDataSource.getSQLConnection();
            sql = "select * from QueryStudentPassedCourse(?)";
            preparedStatement = conn.prepareStatement(sql);
            preparedStatement.setString(1,studentId+"");
            rst = preparedStatement.executeQuery();
            while(rst.next())
            {
                passedCourseId.add(rst.getString("course_id"));
            }

            sql = "select prerequisite from course where course_id = ?";
            preparedStatement = conn.prepareStatement(sql);
            preparedStatement.setString(1,courseId);
            rst = preparedStatement.executeQuery();
            while(rst.next()) prerequisite = rst.getString("prerequisite");
            if(prerequisite == null) return true;
            return PrerequisiteUtils.judge(passedCourseId,prerequisite);

        } catch (SQLException throwables) {
            throwables.printStackTrace();
            throw new IntegrityViolationException();
        }
        finally{
            doFinally(rst,preparedStatement,conn);
        }

    }

    @Override
    public Major getStudentMajor(int studentId) {
        Connection conn = null;
        String sql;
        PreparedStatement preparedStatement = null;
        ResultSet rst = null;
        Major major = null;
        try {
            conn = sqlDataSource.getSQLConnection();
            sql = "select * from getStudentMajor(?)";
            preparedStatement = conn.prepareStatement(sql);
            preparedStatement.setString(1,studentId+"");
            rst = preparedStatement.executeQuery();

            while(rst.next())
            {
                major = new Major();
                major.id = rst.getInt("major_id");
                major.name = rst.getString("major_name");
                major.department = new Department();
                major.department.id = rst.getInt("department_id");
                major.department.name = rst.getString("department_name");
            }
            if(major == null) throw new EntityNotFoundException();

        } catch (SQLException throwables) {
            throw new IntegrityViolationException();
        } finally{
            doFinally(rst,preparedStatement,conn);
        }
        return major;
    }

    private static Grade getCourseGrade(String state,@Nullable Integer grade) {
        if(state.equals("Attending")) return null;
        else if(state.equals("Pass"))
        {
            if(grade == null) return PassOrFailGrade.PASS;
            else return new HundredMarkGrade(grade.shortValue());
        }
        else
        {
            if(grade == null) return PassOrFailGrade.FAIL;
            else return new HundredMarkGrade(grade.shortValue());
        }
    }

    static void doFinally(@Nullable ResultSet rst, @Nullable PreparedStatement preparedStatement, @Nullable Connection conn) {
        if(rst != null){
            try {
                rst.close();
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }
        }
        if(preparedStatement !=null){
            try {
                preparedStatement.close();
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }
        }
        if(conn !=null){
            try {
                conn.close();
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }
        }
    }

    public static String getFullName(String first, String last){
        String regex=".*[a-zA-Z]+.*";
        Matcher m = Pattern.compile(regex).matcher(first);
        if (m.matches()){
            return first + " " + last;
        } else {
            return first + last;
        }
    }

    private void getClassInfo(CourseSectionClass courseSectionClass,ResultSet rst) throws SQLException {
        Instructor instructor;
        instructor = new Instructor();
        courseSectionClass.id = rst.getInt("class_id1");
        courseSectionClass.dayOfWeek = DayOfWeek.MONDAY.plus(rst.getLong("dayofweek1") - 1);
        courseSectionClass.weekList = ReferenceCourseService.cvtIntToList(rst.getInt("weeklist1"));
        courseSectionClass.classBegin = (short)rst.getInt("classbegin1");
        courseSectionClass.classEnd = (short)rst.getInt("classend1");
        courseSectionClass.location = rst.getString("location1");
        instructor.id = rst.getInt("instructor_id1");
        instructor.fullName = getFullName(rst.getString("first_name1"), rst.getString("last_name1"));
        courseSectionClass.instructor = instructor;
    }

    public void getPara(@Nullable String searchCid, @Nullable String searchName, @Nullable String searchInstructor, @Nullable DayOfWeek searchDayOfWeek, @Nullable Short searchClassTime, @Nullable List<String> searchClassLocations,PreparedStatement preparedStatement, Connection conn) {

        try {
            if (searchCid == null) {
                preparedStatement.setString(1, "");
                preparedStatement.setString(2, "");
            } else {
                preparedStatement.setString(3,searchCid);
            }

            if (searchName == null) {
                preparedStatement.setString(4, "");
                preparedStatement.setString(5, "");
            } else {
                //searchName += "\\";
                searchName = searchName.replace("[", "\\[");
                searchName = searchName.replace("]", "\\]");
                searchName = searchName.replace("+", "\\+");
                searchName = searchName.replace("-", "\\-");
                preparedStatement.setString(6,searchName);
            }

            if (searchInstructor == null) {
                preparedStatement.setString(10, "");
                preparedStatement.setString(11, "");
            } else {
                preparedStatement.setString(12,searchInstructor);
            }

            if (searchDayOfWeek == null) {
                preparedStatement.setString(13, "");
                preparedStatement.setString(14, "");
            } else {
                preparedStatement.setInt(15, searchDayOfWeek.getValue());
            }

            if (searchClassTime == null) {
                preparedStatement.setString(16, "");
                preparedStatement.setString(17, "");
            } else {
                preparedStatement.setInt(18,searchClassTime);
            }

            if (searchClassLocations == null) {
                preparedStatement.setString(19, "");
                preparedStatement.setString(20, "");
            } else {
                String[] s = new String[searchClassLocations.size()];
                int cnt = 0;
                for (String s1 : searchClassLocations){
                    s[cnt++] = "%" + s1 + "%";
                }
                Array array = conn.createArrayOf("varchar", s);
                preparedStatement.setArray(21, array);
            }
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }

    static class CourseTime{
        String courseId;
        String name;
        int day;
        int week;
        int begin;
        int end;
        int semester;
        public CourseTime(int day, int week, int begin, int end, int semester, String name, String courseId){
            this.courseId = courseId;
            this.day = day;
            this.week = week;
            this.begin = begin;
            this.end = end;
            this.semester = semester;
            this.name = name;
        }
    }
}
