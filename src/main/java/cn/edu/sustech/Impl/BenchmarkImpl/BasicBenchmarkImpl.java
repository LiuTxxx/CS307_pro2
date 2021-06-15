package cn.edu.sustech.Impl.BenchmarkImpl;

import cn.edu.sustech.Impl.FactoryImpl.ReferenceServiceFactory;
import cn.edu.sustech.Impl.ServiceImpl.*;
import cn.edu.sustech.cs307.benchmark.BasicBenchmark;
import cn.edu.sustech.cs307.dto.Semester;
import cn.edu.sustech.cs307.dto.User;
import cn.edu.sustech.cs307.service.*;
import com.zaxxer.hikari.pool.HikariPool;

import java.sql.Date;
import java.util.List;

public class BasicBenchmarkImpl extends BasicBenchmark {

    public BasicBenchmarkImpl()
    {
        super.serviceFactory = new ReferenceServiceFactory();
    }

    public void testUserService()
    {
        ReferenceUserService service = (ReferenceUserService) serviceFactory.createService(UserService.class);

        service.removeUser(123213);
    }

    public void testStudentService()
    {
        ReferenceStudentService service = (ReferenceStudentService) serviceFactory.createService(StudentService.class);
        service.addStudent(123213, 1,"liu", "t", new java.sql.Date(System.currentTimeMillis()));
    }

    public void testSemesterService()
    {
        ReferenceSemesterService service = (ReferenceSemesterService) serviceFactory.createService(SemesterService.class);
        service.addSemester("2021秋季学期",new Date(2000),new Date(1000000000000L));
    }

    public void testMajorService()
    {
        ReferenceMajorService service = (ReferenceMajorService) serviceFactory.createService(MajorService.class);
        for (int i = 1; i <= 10; i++){
            for (int j = 1; j <= 5; j++){
                service.addMajor("M"+ j + i, i);
            }
        }
    }

    public void testInstructorService()
    {
        ReferenceInstructorService service = (ReferenceInstructorService) serviceFactory.createService(InstructorService.class);
        service.addInstructor(12333, "通", "刘");
    }

    public void testDepartmentService()
    {
        ReferenceDepartmentService service = (ReferenceDepartmentService) serviceFactory.createService(DepartmentService.class);
        service.addDepartment("CS");
        service.addDepartment("AA");
        service.addDepartment("BB");
        service.addDepartment("CC");
        service.addDepartment("DD");
        service.addDepartment("EE");
        service.addDepartment("FF");
        service.addDepartment("GG");
        service.addDepartment("HH");
        service.addDepartment("II");
    }

    public void testCourseService()
    {
        ReferenceCourseService service = (ReferenceCourseService) serviceFactory.createService(CourseService.class);
    }


    public static void main(String[] args)
    {
        BasicBenchmarkImpl test = new BasicBenchmarkImpl();
        test.testSemesterService();


    }
}
