package cn.edu.sustech.Impl.ServiceImpl;

import cn.edu.sustech.cs307.database.SQLDataSource;
import cn.edu.sustech.cs307.dto.Semester;
import cn.edu.sustech.cs307.exception.EntityNotFoundException;
import cn.edu.sustech.cs307.exception.IntegrityViolationException;
import cn.edu.sustech.cs307.service.SemesterService;

import javax.annotation.ParametersAreNonnullByDefault;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
@ParametersAreNonnullByDefault
public class ReferenceSemesterService implements SemesterService {

    SQLDataSource sqlDataSource = SQLDataSource.getInstance();
    Connection conn = null;
    String sql = null;
    PreparedStatement preparedStatement = null;
    ResultSet rst = null;

    //test each method in main
    public static void main(String[] args) {
        ReferenceSemesterService referenceSemesterService = new ReferenceSemesterService();
        List<Semester> list = referenceSemesterService.getAllSemesters();
        for (int i = 0; i < list.size(); i++) {
            System.out.println(list.get(i).name);
        }

    }

    @Override
    public int addSemester(String name, Date begin, Date end) {

        //如果给定的结束日期比开始日期还早，就抛出IllegalArgumentException
        if(begin.compareTo(end) >= 0)
        {
            throw new IntegrityViolationException();

        }
        //定义返回的自增ID
        Integer serialID = null;


        try {
            conn = sqlDataSource.getSQLConnection();
            sql = "insert into semester (semester_name,begin_date,end_date) values (?,?,?)";
            preparedStatement = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            preparedStatement.setString(1,name);
            preparedStatement.setDate(2,begin);
            preparedStatement.setDate(3,end);
            preparedStatement.executeUpdate();
            //返回插入的所有自增ID，这里只插入一条，ResultSet中只有一个数
            rst = preparedStatement.getGeneratedKeys();
            while(rst.next())
            {
                serialID = rst.getInt(1);
            }

            //如果返回的自增ID为null，那么就说明这个学期插入不合法，抛出IntegrityViolationException
            if(serialID == null) throw new IntegrityViolationException();

        } catch (Exception throwables) {
            throw new IntegrityViolationException();
        }
        finally {
            ReferenceInstructorService.doFinally(rst, preparedStatement, conn);

        }

        return serialID;
    }

    @Override
    public void removeSemester(int semesterId) {
        try {
            conn = sqlDataSource.getSQLConnection();

            sql = "delete from semester where semester_id = ?";
            preparedStatement = conn.prepareStatement(sql);
            preparedStatement.setInt(1,semesterId);
            int execute = preparedStatement.executeUpdate();


        } catch (SQLException throwables) {
            throw new IntegrityViolationException();
        }
        finally {
            ReferenceInstructorService.doFinally(rst, preparedStatement, conn);
        }
    }

    @Override
    public List<Semester> getAllSemesters() {
        //定义返回对象
        List<Semester> list = new ArrayList<>();
        //获取链接
        try {
            conn = sqlDataSource.getSQLConnection();
            sql = "select * from getAllSemesters()";
            preparedStatement = conn.prepareStatement(sql);
            rst = preparedStatement.executeQuery();
            //根据查询结果生成对象

            while(rst.next())
            {
               Semester s = new Semester();
               s.id = rst.getInt("semester_id");
               s.name = rst.getString("semester_name");
               s.begin = rst.getDate("begin_date");
               s.end = rst.getDate("end_date");
               list.add(s);

            }
        } catch (SQLException throwables) {
            throw new IntegrityViolationException();
        } finally{
            ReferenceInstructorService.doFinally(rst, preparedStatement, conn);

        }
        return list;
    }

    @Override
    public Semester getSemester(int semesterId) {

        //定义返回对象
        Semester s = null;
        try {
            conn = sqlDataSource.getSQLConnection();
            sql = "select * from getSemester(?)";
            preparedStatement = conn.prepareStatement(sql);
            preparedStatement.setInt(1,semesterId);
            rst = preparedStatement.executeQuery();


            while(rst.next())
            {
                s = new Semester();
                s.id = rst.getInt("semester_id");
                s.name = rst.getString("semester_name");
                s.begin = rst.getDate("begin_date");
                s.end = rst.getDate("end_date");
            }

            if(s == null) throw new EntityNotFoundException();

        } catch (SQLException throwables) {
            throw new IntegrityViolationException();
        }finally{
            ReferenceInstructorService.doFinally(rst, preparedStatement, conn);
        }
        return s;
    }
}
