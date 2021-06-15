package cn.edu.sustech.Impl.ServiceImpl;

import cn.edu.sustech.cs307.database.SQLDataSource;
import cn.edu.sustech.cs307.dto.Department;
import cn.edu.sustech.cs307.dto.Major;
import cn.edu.sustech.cs307.exception.EntityNotFoundException;
import cn.edu.sustech.cs307.exception.IntegrityViolationException;
import cn.edu.sustech.cs307.service.MajorService;

import javax.annotation.ParametersAreNonnullByDefault;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
@ParametersAreNonnullByDefault
public class ReferenceMajorService implements MajorService {

    SQLDataSource sqlDataSource = SQLDataSource.getInstance();
    Connection conn = null;
    String sql = null;
    PreparedStatement preparedStatement = null;
    ResultSet rst = null;

    public static void main(String[] args) {

        ReferenceMajorService majorService = new ReferenceMajorService();

    }

    @Override//finished
    public int addMajor(String name, int departmentId) {


        //定义major表中返回的自增ID
        Integer serialID = null;

        //获取连接
        try {
            conn = sqlDataSource.getSQLConnection();
            sql = "insert into major (major_name) values (?)";
            preparedStatement = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            preparedStatement.setString(1,name);
            preparedStatement.executeUpdate();

            //获取自增ID，这里只插入一条，ResultSet中只有一个数
            rst = preparedStatement.getGeneratedKeys();
            while(rst.next())
            {
                serialID = rst.getInt(1);
            }

            //如果serialID为null，那么就是插入失败major表中失败，就说明该专业已经被添加过了
            if(serialID == null)
            {
                throw new IntegrityViolationException();
            }

            //在major_to_department中插入数据
            sql = "insert into major_to_department (department_id,major_id) values (?,?)";
            preparedStatement = conn.prepareStatement(sql);
            preparedStatement.setInt(1,departmentId);
            preparedStatement.setInt(2,serialID);

            preparedStatement.executeUpdate();


        } catch (SQLException throwables) {
            throw new IntegrityViolationException();
        }finally{
            ReferenceInstructorService.doFinally(rst, preparedStatement, conn);

        }

        return serialID;
    }


    @Override
    public void removeMajor(int majorId) {
        //获取连接
        try {
            //在major_to_department表中删除数据
            conn = sqlDataSource.getSQLConnection();
            sql = "delete from major where major_id = ?";
            preparedStatement = conn.prepareStatement(sql);
            preparedStatement.setInt(1,majorId);
            int execute = preparedStatement.executeUpdate();

        } catch (SQLException throwables) {
            throw new IntegrityViolationException();
        } finally{
            ReferenceInstructorService.doFinally(rst, preparedStatement, conn);
        }

    }

    @Override
    public List<Major> getAllMajors() {
        //定义返回的对象
        List<Major> AllMajors = new ArrayList<>();

        try {
            //获取连接
            conn = sqlDataSource.getSQLConnection();
            sql = "select * from getAllMajors()";
            preparedStatement = conn.prepareStatement(sql);
            rst = preparedStatement.executeQuery();

            while(rst.next())
            {
                Major major = new Major();
                major.id = rst.getInt("major_id");
                major.name = rst.getString("major_name");
                if(major.department != null) {
                    major.department.id = rst.getInt("department_id");
                    major.department.name = rst.getString("department_name");
                }
                AllMajors.add(major);
            }


        } catch (SQLException throwables) {
            throw new IntegrityViolationException();
        }
        finally{
            ReferenceInstructorService.doFinally(rst, preparedStatement, conn);
        }
        return AllMajors;
    }

    @Override
    public Major getMajor(int majorId) {
        //定义返回对象
        Major major = null;

        //获取连接
        try {
            conn = sqlDataSource.getSQLConnection();
            sql = "select * from getMajor(?)";
            preparedStatement = conn.prepareStatement(sql);
            preparedStatement.setInt(1,majorId);
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
            //如果根据这个major_id没找到对应的专业，那就抛出EntityNotFoundException
            if(major == null) throw new EntityNotFoundException();

        } catch (SQLException throwables) {
            throw new IntegrityViolationException();
        }
        finally{
            ReferenceInstructorService.doFinally(rst, preparedStatement, conn);

        }
        return major;
    }

    @Override
    public void addMajorCompulsoryCourse(int majorId, String courseId) {

        try {
            //获取连接
            conn = sqlDataSource.getSQLConnection();
            sql = "insert into course_to_major (course_id,major_id, type) values (?,?,?)";
            preparedStatement = conn.prepareStatement(sql);
            preparedStatement.setString(1,courseId);
            preparedStatement.setInt(2,majorId);
            preparedStatement.setString(3,"C");
            int rows = preparedStatement.executeUpdate();
            if(rows == 0) throw new IntegrityViolationException();

        } catch (SQLException throwables) {
            throw new IntegrityViolationException();
        }
        finally{
            ReferenceInstructorService.doFinally(rst, preparedStatement, conn);
        }

    }

    @Override
    public void addMajorElectiveCourse(int majorId, String courseId) {

        try {
            //获取连接
            conn = sqlDataSource.getSQLConnection();
            sql = "insert into course_to_major (course_id,major_id, type) values (?,?,?)";
            preparedStatement = conn.prepareStatement(sql);
            preparedStatement.setString(1,courseId);
            preparedStatement.setInt(2,majorId);
            preparedStatement.setString(3,"E");
            int rows = preparedStatement.executeUpdate();
            if(rows == 0) throw new IntegrityViolationException();

        } catch (SQLException throwables) {
            throw new IntegrityViolationException();
        }
        finally{
            ReferenceInstructorService.doFinally(rst, preparedStatement, conn);

        }

    }
}
