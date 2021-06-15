package cn.edu.sustech.Impl.ServiceImpl;

import cn.edu.sustech.cs307.database.SQLDataSource;
import cn.edu.sustech.cs307.dto.Instructor;
import cn.edu.sustech.cs307.dto.Student;
import cn.edu.sustech.cs307.dto.User;
import cn.edu.sustech.cs307.exception.EntityNotFoundException;
import cn.edu.sustech.cs307.exception.IntegrityViolationException;
import cn.edu.sustech.cs307.service.UserService;

import javax.annotation.ParametersAreNonnullByDefault;
import javax.swing.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
@ParametersAreNonnullByDefault
public class ReferenceUserService implements UserService {

    SQLDataSource sqlDataSource = SQLDataSource.getInstance();
    Connection conn = null;
    String sql = null;
    PreparedStatement preparedStatement = null;
    ResultSet rst = null;

    public static void main(String[] args) {
        ReferenceUserService referenceUserService = new ReferenceUserService();
        referenceUserService.removeUser(123213);

    }

    @Override//finished
    public void removeUser(int userId) {
        try {
            conn = sqlDataSource.getSQLConnection();
            sql = "delete from student where student_id = ?";
            preparedStatement = conn.prepareStatement(sql);
            preparedStatement.setString(1,"" + userId);
            int row = preparedStatement.executeUpdate();
            if(row != 0) return;
            sql = "delete from instructor where instructor_id = ?";
            preparedStatement = conn.prepareStatement(sql);
            preparedStatement.setString(1,"" + userId);
            row = preparedStatement.executeUpdate();

        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        finally{
            ReferenceInstructorService.doFinally(rst, preparedStatement, conn);

        }

    }

    @Override//finished
    public List<User> getAllUsers() {
        //定义要返回的对象
        User user;
        List<User> list = new ArrayList<>();

        try {
            conn = sqlDataSource.getSQLConnection();
            sql = "select * from getAllUsers()";
            preparedStatement = conn.prepareStatement(sql);
            rst = preparedStatement.executeQuery();

            while(rst.next()) {
                user = new Student();
                user.id = rst.getInt("user_id");
                String first_name = rst.getString("first_name");
                String last_name = rst.getString("last_name");
                user.fullName = ReferenceStudentService.getFullName(first_name, last_name);
                list.add(user);
            }


        } catch (SQLException throwables) {
            throw new IntegrityViolationException();
        }
        finally {
            ReferenceInstructorService.doFinally(rst, preparedStatement, conn);

        }
        return list;
    }

    @Override//finished
    public User getUser(int userId) {
        User user = null;

        try {
            conn = sqlDataSource.getSQLConnection();
            sql = "select * from getUser(?)";
            preparedStatement = conn.prepareStatement(sql);
            preparedStatement.setString(1,userId+"");
            rst = preparedStatement.executeQuery();

            while(rst.next())
            {
                user = new Student();
                user.id = Integer.parseInt(rst.getString("user_id"));
                user.fullName = ReferenceStudentService.getFullName(rst.getString("first_name"),rst.getString("last_name"));
            }
            if(user == null) throw new EntityNotFoundException();


        } catch (SQLException throwables) {
            throw new IntegrityViolationException();
        }
        return user;
    }
}
