package cn.edu.sustech.Impl.ServiceImpl;

import cn.edu.sustech.cs307.database.SQLDataSource;
import cn.edu.sustech.cs307.dto.CourseSection;
import cn.edu.sustech.cs307.exception.IntegrityViolationException;
import cn.edu.sustech.cs307.service.InstructorService;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@ParametersAreNonnullByDefault
public class ReferenceInstructorService implements InstructorService {

    SQLDataSource sqlDataSource = SQLDataSource.getInstance();


    public static void main(String[] args) {
        ReferenceInstructorService referenceInstructorService = new ReferenceInstructorService();

    }

    public void deleteAll(){
        Connection conn = null;
        String sql = null;
        PreparedStatement preparedStatement = null;
        ResultSet rst = null;
        try {
            conn = sqlDataSource.getSQLConnection();
            //sql = "delete from instructor";
            preparedStatement = conn.prepareStatement(sql);
        }catch (Exception throwable){
            throwable.printStackTrace();
        }
        finally {
            try {
                conn.close();
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }
        }
    }

    @Override//finished
    public void addInstructor(int userId, String firstName, String lastName) {
        Connection conn = null;
        String sql = null;
        PreparedStatement preparedStatement = null;
        ResultSet rst = null;

        try {
            conn = sqlDataSource.getSQLConnection();
            sql = "insert into instructor(instructor_id, first_name, last_name) values (?,?,?)";
            preparedStatement = conn.prepareStatement(sql);
            preparedStatement.setInt(1, userId);
            preparedStatement.setString(2,firstName);
            preparedStatement.setString(3,lastName);
            preparedStatement.executeUpdate();
        }
        catch (Exception throwable){
            throw new IntegrityViolationException();
        }
        finally{
            doFinally(rst, preparedStatement, conn);

        }
    }



    @Override
    public List<CourseSection> getInstructedCourseSections(int instructorId, int semesterId) {
        Connection conn = null;
        String sql = null;
        PreparedStatement preparedStatement = null;
        ResultSet rst = null;
        List<CourseSection> list = new ArrayList<>();
        try {
            conn = sqlDataSource.getSQLConnection();
            sql = "select f.section_id,section_name,totalcapacity,leftcapacity from\n" +
                    "    ((select a.section_id,section_name,totalcapacity,leftcapacity from\n" +
                    "        (select * from coursesection_to_semester\n" +
                    "            where semester_id = ?) a\n" +
                    "        inner join coursesection b on a.section_id = b.section_id) c\n" +
                    "    inner join\n" +
                    "        (select * from\n" +
                    "            (select * from coursesectionclass_to_instructor\n" +
                    "                where instructor_id = ?) d\n" +
                    "            inner join coursesection_to_class e on d.class_id = e.class_id) f\n" +
                    "    on f.section_id = c.section_id)\n";
            preparedStatement = conn.prepareStatement(sql);
            preparedStatement.setInt(1, semesterId);
            preparedStatement.setString(2, "" + instructorId);
            rst = preparedStatement.executeQuery();

            CourseSection courseSection;
            while(rst.next()) {
                courseSection = new CourseSection();
                courseSection.id = rst.getInt("section_id");
                courseSection.name = rst.getString("section_name");
                courseSection.totalCapacity = rst.getInt("totalcapacity");
                courseSection.leftCapacity = rst.getInt("leftcapacity");
                list.add(courseSection);
            }
        }

        catch (Exception throwable){
            throwable.printStackTrace();
        }
        finally{
            doFinally(rst, preparedStatement, conn);
        }
        return list;
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
}
