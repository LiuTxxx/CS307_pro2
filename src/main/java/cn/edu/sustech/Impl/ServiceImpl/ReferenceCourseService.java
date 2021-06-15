package cn.edu.sustech.Impl.ServiceImpl;

import cn.edu.sustech.Impl.Utils.PrerequisiteUtils;
import cn.edu.sustech.cs307.database.SQLDataSource;
import cn.edu.sustech.cs307.dto.*;
import cn.edu.sustech.cs307.dto.prerequisite.AndPrerequisite;
import cn.edu.sustech.cs307.dto.prerequisite.CoursePrerequisite;
import cn.edu.sustech.cs307.dto.prerequisite.OrPrerequisite;
import cn.edu.sustech.cs307.dto.prerequisite.Prerequisite;
import cn.edu.sustech.cs307.exception.EntityNotFoundException;
import cn.edu.sustech.cs307.exception.IntegrityViolationException;
import cn.edu.sustech.cs307.service.CourseService;
import com.impossibl.postgres.jdbc.ArrayUtils;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.sql.*;
import java.time.DayOfWeek;
import java.util.*;

@ParametersAreNonnullByDefault
public class ReferenceCourseService implements CourseService {

    //获取数据库连接池资源
    SQLDataSource sqlDataSource = SQLDataSource.getInstance();


    //测试
    public static void main(String[] args) {
        ReferenceCourseService referenceCourseService = new ReferenceCourseService();
        referenceCourseService.removeCourseSection(1);

    }

    @Override
    public void addCourse(String courseId, String courseName, int credit, int classHour, Course.CourseGrading grading, @Nullable Prerequisite coursePrerequisite) {
        //连接对象
        Connection conn = null;
        //sql语句
        String sql;
        //预编译指令
        PreparedStatement preparedStatement = null;
        //结果集
        ResultSet rst = null;
        try {
            conn = sqlDataSource.getSQLConnection();
            //H代表百分制，P代表通过/不通过
            String courseGrading = grading == Course.CourseGrading.HUNDRED_MARK_SCORE ? "H" : "P";
            String pre;
            pre = PrerequisiteUtils.cvtString(coursePrerequisite);
            if(pre == null)
            {
                sql = "insert into course (course_id,course_name,credit,class_hour,grading) values (?,?,?,?,?)";
                preparedStatement = conn.prepareStatement(sql);
                preparedStatement.setString(1,courseId);
                preparedStatement.setString(2,courseName);
                preparedStatement.setInt(3,credit);
                preparedStatement.setInt(4,classHour);
                preparedStatement.setString(5,courseGrading);

            }
            else {
                sql = "insert into course (course_id,course_name,credit,class_hour,grading,prerequisite) values (?,?,?,?,?,?)";
                preparedStatement = conn.prepareStatement(sql);
                preparedStatement.setString(1,courseId);
                preparedStatement.setString(2,courseName);
                preparedStatement.setInt(3,credit);
                preparedStatement.setInt(4,classHour);
                preparedStatement.setString(5,courseGrading);
                preparedStatement.setString(6,pre);
            }

            preparedStatement.executeUpdate();


        } catch (SQLException throwables) {
            throw new IntegrityViolationException();
        }finally {
            ReferenceInstructorService.doFinally(rst, preparedStatement, conn);
        }

    }

    @Override
    public int addCourseSection(String courseId, int semesterId, String sectionName, int totalCapacity) {
        //连接对象
        Connection conn = null;
        //sql语句
        String sql;
        //预编译指令
        PreparedStatement preparedStatement = null;
        //结果集
        ResultSet rst = null;
        Integer key = null;
        try {
            conn = sqlDataSource.getSQLConnection();
            //首先在coursesection表中插入数据
            sql = "insert into coursesection (section_name,totalcapacity,leftcapacity) values (?,?,?)";
            preparedStatement = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            preparedStatement.setString(1,sectionName);
            preparedStatement.setInt(2,totalCapacity);
            preparedStatement.setInt(3,totalCapacity);
            preparedStatement.executeUpdate();
            rst = preparedStatement.getGeneratedKeys();
            while(rst.next())
            {
                key = rst.getInt(1);
            }

            //然后，在coursesection_to_course表中插入数据
            sql = "insert into course_to_coursesection (section_id,course_id) values (?,?)";
            preparedStatement = conn.prepareStatement(sql);
            preparedStatement.setInt(1,key);
            preparedStatement.setString(2,courseId);
            preparedStatement.executeUpdate();

            //之后，在coursesection_to_semester表中插入数据
            sql = "insert into coursesection_to_semester (section_id,semester_id) values (?,?)";
            preparedStatement = conn.prepareStatement(sql);
            preparedStatement.setInt(1,key);
            preparedStatement.setInt(2,semesterId);
            preparedStatement.executeUpdate();

        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }finally{
            ReferenceInstructorService.doFinally(rst, preparedStatement, conn);

        }
        return key;
    }

    @Override
    public int addCourseSectionClass(int sectionId, int instructorId, DayOfWeek dayOfWeek, Set<Short> weekList, short classStart, short classEnd, String location) {
        //连接对象
        Connection conn = null;
        //sql语句
        String sql;
        //预编译指令
        PreparedStatement preparedStatement = null;
        //结果集
        ResultSet rst = null;
        Integer serialID = null;
        try {
            conn = sqlDataSource.getSQLConnection();
            //首先在表coursesectionclass中插入数据
            sql = "insert into coursesectionclass (dayofweek,weeklist,classbegin,classend,location) values (?,?,?,?,?)";
            preparedStatement = conn.prepareStatement(sql,Statement.RETURN_GENERATED_KEYS);
            int day = getdayOfWeek(dayOfWeek);
            Integer weekListNum = cvtListToInt(weekList);
            preparedStatement.setInt(1,day);
            preparedStatement.setInt(2,weekListNum);
            preparedStatement.setShort(3,classStart);
            preparedStatement.setShort(4,classEnd);
            preparedStatement.setString(5,location);
            preparedStatement.executeUpdate();
            rst = preparedStatement.getGeneratedKeys();
            while(rst.next()) serialID = rst.getInt(1);
            if (serialID == null){
                throw new IntegrityViolationException();
            }

            //插入到coursesectionclass_to_instructor表中
            sql = "insert into coursesectionclass_to_instructor (class_id,instructor_id) values (?,?)";
            preparedStatement = conn.prepareStatement(sql);
            preparedStatement.setInt(1,serialID);
            preparedStatement.setString(2,String.valueOf(instructorId));
            preparedStatement.executeUpdate();

            //插入到coursesection_to_class表中
            sql = "insert into coursesection_to_class (section_id,class_id) values (?,?)";
            preparedStatement = conn.prepareStatement(sql);
            preparedStatement.setInt(1,sectionId);
            preparedStatement.setInt(2,serialID);
            preparedStatement.executeUpdate();


        } catch (SQLException throwables) {
            throw new IntegrityViolationException();
        }
        finally {
            ReferenceInstructorService.doFinally(rst, preparedStatement, conn);

        }

        return serialID;
    }

    @Override
    public void removeCourse(String courseId) {

        //连接对象
        Connection conn = null;
        //sql语句
        String sql;
        //预编译指令
        PreparedStatement preparedStatement = null;
        //结果集
        ResultSet rst = null;

        try {
            conn = sqlDataSource.getSQLConnection();
            sql = "delete from course where course_id = ?";
            preparedStatement = conn.prepareStatement(sql);
            preparedStatement.setString(1,courseId);
            int execute = preparedStatement.executeUpdate();

        } catch (SQLException throwables) {
            throw new IntegrityViolationException();
        } finally{
            ReferenceInstructorService.doFinally(rst, preparedStatement, conn);
        }

    }

    @Override
    public void removeCourseSection(int sectionId) {
        //连接对象
        Connection conn = null;
        //sql语句
        String sql;
        //预编译指令
        PreparedStatement preparedStatement = null;
        //结果集
        ResultSet rst = null;
        try {

            conn = sqlDataSource.getSQLConnection();
            sql = "delete from coursesection where section_id = ?";
            preparedStatement = conn.prepareStatement(sql);
            preparedStatement.setInt(1,sectionId);
            int execute = preparedStatement.executeUpdate();
            System.out.println(execute);


        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        finally{
            ReferenceInstructorService.doFinally(rst, preparedStatement, conn);

        }
    }


    @Override
    public void removeCourseSectionClass(int classId) {
        //连接对象
        Connection conn = null;
        //sql语句
        String sql;
        //预编译指令
        PreparedStatement preparedStatement = null;
        //结果集
        ResultSet rst = null;
        try {
            conn = sqlDataSource.getSQLConnection();
            sql = "delete from coursesectionclass where class_id = ?";
            preparedStatement = conn.prepareStatement(sql);
            preparedStatement.setInt(1,classId);
            int execute = preparedStatement.executeUpdate();

        } catch (SQLException throwables) {
            throw new IntegrityViolationException();
        }
        finally{
            ReferenceInstructorService.doFinally(rst, preparedStatement, conn);

        }

    }


    /*
    1.根据courseId和semesterId把所有的coursesection返回
    2.首先用select* 判断courseId和semesterId是否存在记录
     */
    @Override
    public List<CourseSection> getCourseSectionsInSemester(String courseId, int semesterId) {
        //连接对象
        Connection conn = null;
        //sql语句
        String sql;
        //预编译指令
        PreparedStatement preparedStatement = null;
        //结果集
        ResultSet rst = null;
        //定义放入列表的对象
        CourseSection courseSection;
        //定义返回的列表
        List<CourseSection> list = new ArrayList<>();
        try {
            conn = sqlDataSource.getSQLConnection();
            sql = "select * from course where course_id = ?";
            preparedStatement = conn.prepareStatement(sql);
            preparedStatement.setString(1,courseId);
            rst = preparedStatement.executeQuery();
            if(!rst.next()) throw new EntityNotFoundException();

            sql = "select * from semester where semester_id = ?";
            preparedStatement = conn.prepareStatement(sql);
            preparedStatement.setInt(1,semesterId);
            rst = preparedStatement.executeQuery();
            if(!rst.next()) throw new EntityNotFoundException();

            sql = "select * from getCourseSectionsBySemester(?,?)";
            preparedStatement = conn.prepareStatement(sql);
            preparedStatement.setString(1,courseId);
            preparedStatement.setInt(2,semesterId);
            rst = preparedStatement.executeQuery();
            while(rst.next())
            {
                courseSection = new CourseSection();
                courseSection.id = rst.getInt("section_id");
                courseSection.name = rst.getString("section_name");
                courseSection.totalCapacity = rst.getInt("totalcapacity");
                courseSection.leftCapacity = rst.getInt("leftcapacity");
                list.add(courseSection);
            }
        } catch (SQLException throwables) {
            throw new IntegrityViolationException();
        } finally{
            ReferenceInstructorService.doFinally(rst, preparedStatement, conn);
        }
        return list;
    }

    /*
    1.根据sectionId把对应的course选出来
    2.首先用select * 判断这个sectionId在表中是否有记录，没有就抛Entity异常
    3.联表查询，将course和course_to_coursesection进行连接
     */
    @Override
    public Course getCourseBySection(int sectionId) {
        //连接对象
        Connection conn = null;
        //sql语句
        String sql;
        //预编译指令
        PreparedStatement preparedStatement = null;
        //结果集
        ResultSet rst = null;
        //定义Course对象
        Course course = null;

        try {
            conn = sqlDataSource.getSQLConnection();
            sql = "select * from coursesection where section_id = ?";
            preparedStatement = conn.prepareStatement(sql);
            preparedStatement.setInt(1,sectionId);
            rst = preparedStatement.executeQuery();
            if(!rst.next()) throw new EntityNotFoundException();

            sql = "select * from getCourseBySection(?)";
            preparedStatement = conn.prepareStatement(sql);
            preparedStatement.setInt(1,sectionId);
            rst = preparedStatement.executeQuery();
            while(rst.next())
            {
                course = new Course();
                course.id = rst.getString("course_id");
                course.name = rst.getString("course_name");
                course.credit = rst.getInt("credit");
                course.classHour = rst.getInt("class_hour");
                course.grading = rst.getString("grading").equals("H") ? Course.CourseGrading.HUNDRED_MARK_SCORE : Course.CourseGrading.PASS_OR_FAIL;
            }
            if(course == null) throw new EntityNotFoundException();

        } catch (SQLException throwables) {
            throw new IntegrityViolationException();
        }
        finally{
            ReferenceInstructorService.doFinally(rst, preparedStatement, conn);
        }

        return course;
    }

    /*
    1.根据section_id把对应的coursesectionclass选出来
    2.首先判断section_id是否存在记录，如果不存在抛出Entity异常
    3.联表查询，将coursesection_to_class和coursesectionclass和coursesectionclass_to_instructor进行连接
     */
    @Override
    public List<CourseSectionClass> getCourseSectionClasses(int sectionId) {
        //连接对象
        Connection conn = null;
        //sql语句
        String sql;
        //预编译指令
        PreparedStatement preparedStatement = null;
        //结果集
        ResultSet rst = null;

        CourseSectionClass courseSectionClass;
        List<CourseSectionClass> list = new ArrayList<>();


        try {
            conn = sqlDataSource.getSQLConnection();
            sql = "select * from coursesection where section_id = ?";
            preparedStatement = conn.prepareStatement(sql);
            preparedStatement.setInt(1,sectionId);
            rst = preparedStatement.executeQuery();
            if(!rst.next()) throw new EntityNotFoundException();

            sql = "select * from getCourseSectionClassesBySection(?)";
            preparedStatement = conn.prepareStatement(sql);
            preparedStatement.setInt(1, sectionId);
            rst = preparedStatement.executeQuery();
            while(rst.next())
            {
                courseSectionClass = new CourseSectionClass();
                courseSectionClass.id = rst.getInt("class_id");
                courseSectionClass.dayOfWeek = DayOfWeek.of(rst.getShort("dayofweek"));
                courseSectionClass.classBegin = rst.getShort("classbegin");
                courseSectionClass.classEnd = rst.getShort("classend");
                courseSectionClass.location = rst.getString("location");
                courseSectionClass.weekList = cvtIntToList(rst.getInt("weeklist"));
                courseSectionClass.instructor = new Instructor();
                courseSectionClass.instructor.id = rst.getInt("instructor_id");
                courseSectionClass.instructor.fullName = ReferenceStudentService.getFullName(rst.getString("first_name"),rst.getString("last_name"));
                list.add(courseSectionClass);
            }


        } catch (SQLException throwables) {
            throw new IntegrityViolationException();
        }
        finally{
            ReferenceInstructorService.doFinally(rst, preparedStatement, conn);

        }
        return list;
    }

    /*
    1. 根据classId把对应的coursesection选出来
    2.联表查询，将coursesection和coursesection_to_class连接
     */
    @Override
    public CourseSection getCourseSectionByClass(int classId) {

        CourseSection courseSection = null;
        //连接对象
        Connection conn = null;
        //sql语句
        String sql;
        //预编译指令
        PreparedStatement preparedStatement = null;
        //结果集
        ResultSet rst = null;


        try {
            conn = preparedStatement.getConnection();
            sql = "select * from getCourseSectionByClass(?)";
            preparedStatement = conn.prepareStatement(sql);
            preparedStatement.setInt(1,classId);
            rst = preparedStatement.executeQuery();
            while(rst.next())
            {
                courseSection = new CourseSection();
                courseSection.id = rst.getInt("section_id");
                courseSection.name = rst.getString("section_name");
                courseSection.totalCapacity = rst.getInt("totalcapacity");
                courseSection.leftCapacity = rst.getInt("leftcapacity");
            }
            if(courseSection == null) throw new EntityNotFoundException();


        } catch (SQLException throwables) {
            throw new IntegrityViolationException();
        } finally{
            ReferenceInstructorService.doFinally(rst, preparedStatement, conn);

        }

        return courseSection;
    }

    /*
    1.将某一semester中某一个course里面的学生都选出来
    2.首先利用select * 判断courseId和semesterId对应的记录是否存在，不存在抛出异常
    3.采用联表查询，将coursesection_to_semester
                和course_to_coursesection
                和student_coursesection连接
     */
    @Override
    public List<Student> getEnrolledStudentsInSemester(String courseId, int semesterId) {
        //连接对象
        Connection conn = null;
        //sql语句
        String sql;
        //预编译指令
        PreparedStatement preparedStatement = null;
        //结果集
        ResultSet rst = null;
        List<Student> list = new ArrayList<>();

        try {
            conn = sqlDataSource.getSQLConnection();
            sql = "select * from course where course_id = ?";
            preparedStatement = conn.prepareStatement(sql);
            preparedStatement.setString(1,courseId);
            rst = preparedStatement.executeQuery();
            if(!rst.next()) throw new EntityNotFoundException();

            sql = "select * from semester where semester_id = ?";
            preparedStatement = conn.prepareStatement(sql);
            preparedStatement.setInt(1,semesterId);
            rst = preparedStatement.executeQuery();
            if(!rst.next()) throw new EntityNotFoundException();

            sql = "select * from getEnrolledStudentsInSemester(?,?)";
            preparedStatement = conn.prepareStatement(sql);
            preparedStatement.setString(1,courseId);
            preparedStatement.setInt(2,semesterId);
            rst = preparedStatement.executeQuery();
            while(rst.next())
            {
                Student student = new Student();
                student.id = rst.getInt(1);
                student.fullName = rst.getString(2)+rst.getString(3);
                student.enrolledDate = rst.getDate(4);
                list.add(student);
            }

        } catch (SQLException throwables) {
            throw new IntegrityViolationException();
        }
        finally{
            ReferenceInstructorService.doFinally(rst, preparedStatement, conn);
        }
        return list;
    }

    /*
    利用select * from course获取所有course对象
     */
    @Override
    public List<Course> getAllCourses() {
        //连接对象
        Connection conn;
        //sql语句
        String sql;
        //预编译指令
        PreparedStatement preparedStatement;
        //结果集
        ResultSet rst;
        List<Course> courses = new ArrayList<>();
        try {
            conn = sqlDataSource.getSQLConnection();
            sql = "select * from course";
            preparedStatement = conn.prepareStatement(sql);
            rst = preparedStatement.executeQuery();
            //如果一条记录都没有搜索到，rst.next()为空，courses为empty list
            while(rst.next()){
                Course course = new Course();
                course.id = rst.getString("course_id");
                course.name = rst.getString("course_name");
                course.credit = rst.getInt("credit");
                course.classHour = rst.getInt("class_hour");
                course.grading = rst.getString("grading").equals("H") ? Course.CourseGrading.HUNDRED_MARK_SCORE : Course.CourseGrading.PASS_OR_FAIL;
                courses.add(course);
            }
        } catch (SQLException throwables) {
            throw new IntegrityViolationException();
        }
        return courses;
    }

    /*
    将枚举类型的DayOfWeek换算成数字，表示是星期几
     */
    private static int getdayOfWeek(DayOfWeek day)
    {
        switch (day)
        {
            case MONDAY:return 1;
            case TUESDAY:return 2;
            case WEDNESDAY:return 3;
            case THURSDAY:return 4;
            case FRIDAY:return 5;
            case SATURDAY:return 6;
            case SUNDAY:return 7;
            default:return 0;
        }
    }

    /*
    将上课周列表weekList转换成对应的二进制数进行存储：
     */
    public static Integer cvtListToInt(Set<Short> list)
    {
        int ans = 0;

        for (Short aShort : list) {
            ans += (1 << (aShort - 1));
        }
        return ans;
    }

    /*
    将二进制数转换为weekList的格式
     */
    public static Set<Short> cvtIntToList(Integer num)
    {
        Set<Short> list = new HashSet<>();
        for(int i = 0; i < 20; i++)
        {
            int x = (num >> i) & 1;
            if(x != 0) list.add((short) (i+1));
        }
        return list;
    }
}
