package cn.edu.sustech.Impl.ServiceImpl;

import cn.edu.sustech.cs307.database.SQLDataSource;
import cn.edu.sustech.cs307.dto.Department;
import cn.edu.sustech.cs307.exception.EntityNotFoundException;
import cn.edu.sustech.cs307.exception.IntegrityViolationException;
import cn.edu.sustech.cs307.service.DepartmentService;
import javax.annotation.ParametersAreNonnullByDefault;
import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
@ParametersAreNonnullByDefault
public class ReferenceDepartmentService implements DepartmentService {

    //获取数据库连接池资源
    SQLDataSource sqlDataSource = SQLDataSource.getInstance();
    //连接对象
    Connection conn = null;
    //sql语句
    String sql = null;
    //预编译指令
    PreparedStatement preparedStatement = null;
    //结果集
    ResultSet rst = null;

    public static void main(String[] args) {

        ReferenceDepartmentService referenceDepartmentService = new ReferenceDepartmentService();
        for(int i = 1; i <= 10; i++)
        {
            referenceDepartmentService.removeDepartment(i);
        }

    }

    @Override//finished
    public int addDepartment(String name){
        //第一步：定义返回的自增ID
        Integer serialID = null;
        //获取连接
        try {
            //从数据库连接池中获取连接对象
            conn = sqlDataSource.getSQLConnection();
            //准备sql语句
            sql = "insert into department (department_name) values (?)";
            //获取预编译指令对象，Statement.RETURN_GENERATED_KEYS表示预编译指令后期可返回自增ID集合
            preparedStatement = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            //添加的系的名称
            preparedStatement.setString(1,name);
            //执行指令
            preparedStatement.executeUpdate();
            //获取自增的ID集合
            rst = preparedStatement.getGeneratedKeys();
            //遍历ID集合，这里是加入一条，所以这个循环的次数为1
            while(rst.next())
            {
                serialID = rst.getInt(1);
            }
            //如果没插入成功，就抛出IntegrityViolationException

        } catch (SQLException throwables) {
            //如果插入了同名的departmentname会在这里抛出Integrity异常
            throw new IntegrityViolationException();
        }
        finally {
            ReferenceInstructorService.doFinally(rst, preparedStatement, conn);
        }
        if(serialID == null) throw new IntegrityViolationException();
        //返回自增ID
        return serialID;
    }

    @Override
    public void removeDepartment(int departmentId) {


        try {
            //获取连接对象
            conn = sqlDataSource.getSQLConnection();
            //准备sql语句

            sql = "delete from department where department_id = ?";
            preparedStatement = conn.prepareStatement(sql);
            preparedStatement.setInt(1,departmentId);
            int execute = preparedStatement.executeUpdate();

        } catch (SQLException throwable) {
            throw new IntegrityViolationException();
        }
        finally {
            ReferenceInstructorService.doFinally(rst, preparedStatement, conn);
        }
    }

    @Override
    public List<Department> getAllDepartments() {

        List<Department> list = new ArrayList<>();

        try {
            conn = sqlDataSource.getSQLConnection();
            sql = "select * from getAllDepartments()";
            preparedStatement = conn.prepareStatement(sql);
            rst = preparedStatement.executeQuery();
            //如果没有查询到数据，rst.next()值为null，循环不会进入，list为空
            while(rst.next())
            {
                Department department = new Department();
                department.id = rst.getInt("department_id");
                department.name = rst.getString("department_name");
                list.add(department);
            }
        } catch (SQLException throwables) {
            throwables.printStackTrace();
            throw new IntegrityViolationException();
        } finally {
            ReferenceInstructorService.doFinally(rst, preparedStatement, conn);
        }
        return list;
    }

    @Override
    public Department getDepartment(int departmentId) {
        //定义要返回的对象
        Department department = null;
        try {
            conn = sqlDataSource.getSQLConnection();
            sql = "select * from getDepartment(?)";
            preparedStatement = conn.prepareStatement(sql);
            preparedStatement.setInt(1,departmentId);
            rst = preparedStatement.executeQuery();

            while(rst.next())
            {
                department = new Department();
                department.id = rst.getInt("department_id");
                department.name = rst.getString("department_name");
            }
            //如果查询没有结果，证明没找到这个系，抛出EntityNotFoundException
            if(department == null) throw new EntityNotFoundException();
        } catch (SQLException throwables) {
            throw new IntegrityViolationException();
        }
        //释放资源
        finally {
            ReferenceInstructorService.doFinally(rst, preparedStatement, conn);
        }
        return department;
    }
}
