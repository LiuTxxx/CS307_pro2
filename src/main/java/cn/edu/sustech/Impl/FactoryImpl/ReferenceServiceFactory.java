package cn.edu.sustech.Impl.FactoryImpl;

import cn.edu.sustech.Impl.ServiceImpl.*;
import cn.edu.sustech.cs307.factory.ServiceFactory;
import cn.edu.sustech.cs307.service.*;

public class ReferenceServiceFactory extends ServiceFactory {

    public ReferenceServiceFactory() {
        registerService(SemesterService.class, new ReferenceSemesterService());
        registerService(UserService.class, new ReferenceUserService());
        registerService(StudentService.class,new ReferenceStudentService());
        registerService(MajorService.class,new ReferenceMajorService());
        registerService(InstructorService.class, new ReferenceInstructorService());
        registerService(DepartmentService.class,new ReferenceDepartmentService());
        registerService(CourseService.class,new ReferenceCourseService());

        // TODO: register other service implementations here

    }

}
