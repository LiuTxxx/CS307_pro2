--创建了Department表
create table if not exists Department
(
    department_id serial primary key,
    department_name varchar(30) not null unique
    );

create table if not exists Semester
(
    semester_id serial primary key ,
    semester_name varchar(30) not null unique,
    begin_date Date not null unique ,
    end_date Date not null unique
    );

create table if not exists Student
(
    student_id varchar(20) primary key ,
    first_name varchar(40) not null,
    last_name  varchar(40) not null,
    enrolled_date date not null

    );

create table if not exists Major
(
    major_id serial primary key ,
    major_name varchar(30) not null unique

    );

create table if not exists Student_to_Major
(
    student_id varchar(20) ,
    major_id integer ,
    foreign key (student_id) references Student(student_id),
    foreign key (major_id) references Major(major_id),
    primary key (major_id, student_id)
    );

create table if not exists Major_to_Department
(
    department_id integer ,
    major_id integer ,
    foreign key (department_id) references Department(department_id),
    foreign key (major_id) references Major(major_id),
    primary key (major_id, department_id)
    );

create table if not exists Instructor
(
    instructor_id varchar(20) primary key ,
    first_name varchar(40) not null,
    last_name  varchar(40) not null
    );

create table if not exists Course
(
    course_id varchar(20) primary key ,
    course_name varchar(30) not null,
    credit integer not null,
    class_hour integer not null,
    grading varchar(10) not null
    );

create table if not exists CourseSection
(
    section_id serial primary key,
    Section_name varchar (30) not null,
    totalCapacity integer not null,
    leftCapacity integer not null
    );

create table if not exists CourseSectionClass
(
    class_id serial primary key,
    dayOfWeek integer not null,
    weekList int[] not null,
    classBegin int not null,
    classEnd int not null,
    location varchar(30) not null

    );

create table if not exists CourseSectionClass_to_Instructor
(
    class_id int,
    instructor_id varchar(20),
    foreign key(class_id) references CourseSectionClass(class_id),
    foreign key(instructor_id) references Instructor(instructor_id),
    primary key (class_id, instructor_id)
    );

create table if not exists CourseSection_to_Course
(
    Section_id integer,
    course_id varchar(20),
    primary key(Section_id,course_id),
    foreign key (Section_id) references CourseSection(Section_id),
    foreign key(course_id) references Course(course_id)
    );

create table if not exists CourseSection_to_Semester
(
    Section_id integer,
    semester_id integer,
    primary key(Section_id,semester_id),
    foreign key(Section_id) references CourseSection(Section_id),
    foreign key(semester_id) references Semester(semester_id)
    );

create table if not exists CourseSection_to_Student
(
    student_id varchar (20),
    Section_id integer ,
    grade varchar (5),
    foreign key(Section_id) references CourseSection(Section_id),
    foreign key(student_id) references Student(student_id),
    primary key(Section_id, student_id)
    );

create table if not exists Prerequisite
(
    course_id varchar (20) primary key,
    prerequisite varchar,
    SOP varchar,
    foreign key(course_id) references Course(course_id)
    );

create table if not exists course_to_major
(
    course_id varchar(20) not null,
    major_id integer not null,
    type varchar not null,
    foreign key (course_id) references course(course_id),
    foreign key(major_id) references major(major_id),
    primary key(course_id,major_id)
    );

create table if not exists courseSection_to_class
(
    section_id integer,
    class_id integer,
    foreign key(section_id) references coursesection(section_id),
    foreign key(class_id) references coursesectionclass(class_id),
    primary key (Section_id, class_id)
    );

create table if not exists student_course
(
    student_id varchar,
    section_id integer,
    state varchar,
    grade integer,
    foreign key(student_id) references student(student_id),
    foreign key(section_id) references  coursesection(section_id),
    primary key (section_id, student_id, state)
    );


create function temp_func(para1 character varying, para2 character varying, para3 character varying, para4 character varying, para5 character varying, para6 character varying, para7 character varying, para8 character varying, para9 character varying, para10 character varying, para11 character varying, para12 character varying, para13 character varying, para14 character varying, para15 integer, para16 character varying, para17 character varying, para18 character varying, para19 character varying, para20 character varying, para21 integer, para22 character varying, para23 character varying, para24 integer, para25 character varying, para26 character varying, para27 integer, para28 character varying, para29 character varying, para30 character varying)
    returns TABLE(course_id1 character varying, section_id1 integer, class_id1 integer, course_name1 character varying, credit1 integer, class_hour1 integer, grading1 character varying, section_name1 character varying, totalcapacity1 integer, leftcapacity1 integer, instructor_id1 character varying, first_name1 character varying, last_name1 character varying, dayofweek1 integer, weeklist1 integer[], classbegin1 integer, classend1 integer, location1 character varying)
    language plpgsql
as
$$
begin
return query select course_id, gg.section_id, class_id, course_name, credit, class_hour, grading, section_name, totalcapacity, leftcapacity, instructor_id, first_name, last_name, dayofweek, weeklist, classbegin, classend, location from
        (select ff.course_id, course_name, credit, class_hour, grading, section_id from
            (select i.course_id, course_name, credit, class_hour, grading from
                (select * from course
                 where (para1 = para2 or course_id = para3) and (para4 = para5 or course_name = para6))i
                    inner join
                (select course_id, g.major_id from
                    (select * from course_to_major
                     where (para7 = para8 or state = para9) )g
                        inner join
                    (select * from student_to_major
                     where (para10 = para11 or student_id = para12))h on g.major_id = h.major_id) j on i.course_id = j.course_id) ee
                inner join
            coursesection_to_course ff on ee.course_id = ff.course_id) gg

            inner join

        (select section_name,class_id, dd.section_id,totalcapacity,leftcapacity,instructor_id, dayofweek,weeklist,classbegin,classend,location,first_name,last_name from
            (select class_id, section_id,k.instructor_id, dayofweek,weeklist,classbegin,classend,location,first_name,last_name from
                (select f.class_id, e.section_id,semester_id,instructor_id, dayofweek,weeklist,classbegin,classend,location from
                    (select c.section_id, semester_id, class_id from
                        (select * from coursesection_to_semester
                         where (para13 = para14 or semester_id = para15))c
                            inner join
                        coursesection_to_class d on c.section_id = d.section_id)e
                        inner join
                    (select a.class_id,instructor_id, dayofweek,weeklist,classbegin,classend,location from
                        (select * from coursesectionclass_to_instructor
                         where (para16 = para17 or instructor_id = para18)) a
                            inner join
                        (select * from coursesectionclass
                         where (para19 = para20 or dayofweek = para21) and (para22 = para23 or classbegin <= para24) and (para25 = para26 or classend >= para27) and (para28 = para29 or location = para30)) b
                        on a.class_id = b.class_id)f on e.class_id = f.class_id)k
                    inner join
                (select aa.instructor_id, first_name, last_name from
                    (instructor aa
                        inner join
                    coursesectionclass_to_instructor bb on aa.instructor_id = bb.instructor_id)) l on k.instructor_id = l.instructor_id) cc
                inner join
            coursesection dd on cc.section_id = dd.section_id)hh on gg.section_id = hh.section_id;
end
$$;

alter function temp_func(varchar, varchar, varchar, varchar, varchar, varchar, varchar, varchar, varchar, varchar, varchar, varchar, varchar, varchar, integer, varchar, varchar, varchar, varchar, varchar, integer, varchar, varchar, integer, varchar, varchar, integer, varchar, varchar, varchar) owner to lt2496818590;
--创建存储过程,当删除student表时，首先删除与其外键相关联的表
create or replace function student_delete()
returns trigger
as $$
    declare
        sid integer;
    begin
        sid := old.student_id;
        delete from student_to_major where student_id  = sid;
        delete from student_coursesection where student_id = sid;
    end;
    $$language plpgsql;

create trigger student_delete_trg
    before delete on student
    for each row
    execute procedure student_delete();


--创建存储过程,当删除instructor表时，首先删除与其外键相关联的表
create or replace function instructor_delete()
    returns trigger
as $$
declare
    iid integer;
begin
    iid := old.instructor_id;
    delete from coursesectionclass_to_instructor where instructor_id  = iid;
end;
$$language plpgsql;

create trigger instructor_delete_trg
    before delete on instructor
    for each row
execute procedure instructor_delete();



--创建存储过程，当删除department这个表时，首先删除于其外键相关联的表
create or replace function department_delete()
    returns trigger
as $$
declare
    did integer;
begin
    did := old.department_id;
    delete from major_to_department where department_id  = did;
end;
$$language plpgsql;

create trigger department_delete_trg
    before delete on department
    for each row
execute procedure department_delete();

--创建存储过程，当删除major表时，首先删除外键相关联的数据
create or replace function major_delete()
    returns trigger
as $$
declare
    mid integer;
begin
    mid := old.major_id;
    delete from student_to_major where major_id  = mid;
    delete from course_to_major where major_id = mid;
    delete from major_to_department where major_id = mid;
end;
$$language plpgsql;

create trigger major_delete_trg
    before delete on major
    for each row
execute procedure major_delete();

--创建存储过程,当删除semester表时，首先删除与其外键相关联的表
create or replace function semester_delete()
    returns trigger
as $$
declare
    smid integer;
begin
    smid := old.semester_id;
    delete from coursesection_to_semester where semester_id  = smid;
end;
$$language plpgsql;

create trigger semester_delete_trg
    before delete on semester
    for each row
execute procedure semester_delete();


--创建存储过程,当删除course表时，首先删除与其外键相关联的表
create or replace function course_delete()
    returns trigger
as $$
declare
    cid integer;
begin
    cid := old.course_id;
    delete from course_to_major where course_id  = cid;
    delete from course_to_coursesection where course_id = cid;
end;
$$language plpgsql;

create trigger course_delete_trg
    before delete on course
    for each row
execute procedure course_delete();


--创建存储过程,当删除coursesection表时，首先删除与其外键相关联的表
create or replace function coursesection_delete()
    returns trigger
as $$
declare
    csid integer;
begin
    csid := old.section_id;
    delete from student_coursesection where section_id  = csid;
    delete from course_to_coursesection where section_id = csid;
    delete from coursesection_to_semester where section_id = csid;
    delete from coursesection_to_class where section_id = csid;
end;
$$language plpgsql;

create trigger coursesection_delete_trg
    before delete on coursesection
    for each row
execute procedure coursesection_delete();


--创建存储过程,当删除coursesectionclass表时，首先删除与其外键相关联的表
create or replace function coursesectionclass_delete()
    returns trigger
as $$
declare
    cscid integer;
begin
    cscid := old.class_id;
    delete from coursesectionclass_to_instructor where class_id  = cscid;
    delete from coursesection_to_class where class_id = cscid;
end;
$$language plpgsql;

create trigger coursesectionclass_delete_trg
    before delete on coursesectionclass
    for each row
execute procedure coursesectionclass_delete();


--创建函数：查找所有的users
create or replace function getAllUsers()
returns table
    (
        user_id varchar(20),
        first_name varchar(40),
        last_name varchar(40)
    )
as $body$
    begin
        return query
            select student_id as user_id,s.first_name,s.last_name from student s
            union all
            select instructor_id as user_id,i.first_name,i.last_name from instructor i;
    end;
    $body$
language plpgsql;

--创建函数：查找所有的users
create or replace function getUser(uid varchar(20))
    returns table
            (
                user_id varchar(20),
                first_name varchar(40),
                last_name varchar(40)
            )
as $body$
begin
    return query
        select student_id as user_id,s.first_name,s.last_name from student s
        union all
        select instructor_id as user_id,i.first_name,i.last_name from instructor i
        where user_id = uid;
end;
$body$
    language plpgsql;

--创建函数：查找所有的semesters
create or replace function getAllSemesters()
    returns table
            (
                semester_id integer,
                semester_name varchar(30),
                begin_date date,
                end_date date
            )
as $body$
begin
    return query
        select sm.semester_id, sm.semester_name, sm.begin_date, sm.end_date from semester sm;
end
$body$
    language plpgsql;

--创建函数：查找指定的semester
create or replace function getSemester(smid integer)
    returns table
            (
                semester_id integer,
                semester_name varchar(30),
                begin_date date,
                end_date date
            )
as $body$
begin
    return query
        select sm.semester_id, sm.semester_name, sm.begin_date, sm.end_date from semester sm
            where sm.semester_id = smid;
end
$body$
    language plpgsql;

--创建函数：查找所有的major
create or replace function getAllMajors()
    returns table
            (
                major_id integer,
                major_name varchar(30),
                department_id integer,
                department_name varchar(30)
            )
as $body$
begin
    return query
        select m.major_id,m.major_name,d.department_id,d.department_name
                from major m inner join major_to_department md on
                           m.major_id = md.major_id
                           inner join department d on d.department_id = md.department_id;
end
$body$
    language plpgsql;

--创建函数：查找指定的major
create or replace function getMajor(mid integer)
    returns table
            (
                major_id integer,
                major_name varchar(30),
                department_id integer,
                department_name varchar(30)
            )
as $body$
begin
    return query
        select m.major_id,m.major_name,d.department_id,d.department_name
        from major m inner join major_to_department md on
                m.major_id = md.major_id
                     inner join department d on d.department_id = md.department_id
        where m.major_id = mid;
end
$body$
    language plpgsql;

create or replace function getAllDepartments()
returns table
(
    department_id integer,
    department_name varchar(30)
)
as $body$

    begin
        return query
            select d.department_id,d.department_name from department d;
    end;
$body$
language plpgsql;

create or replace function getDepartment(did integer)
    returns table
            (
                department_id integer,
                department_name varchar(30)
            )
as $body$

begin
    return query
        select d.department_id,d.department_name from department d
        where d.department_id = did;
end;
$body$
    language plpgsql;

create or replace function getCourseSectionsBySemester(cid varchar(20),smid integer)
returns table
    (
        section_id integer,
        section_name varchar(30),
        totalcapacity integer,
        leftcapacity integer
    )

as $body$
    begin
        return query
            select c.section_id,c.section_name,c.totalcapacity,c.leftcapacity from coursesection_to_semester cts
                inner join course_to_coursesection ctc on cts.section_id = ctc.section_id
                inner join coursesection c on ctc.section_id = c.section_id
            where ctc.course_id = cid and cts.semester_id = smid;
    end;

$body$
language plpgsql;

create or replace function getCourseBySection(csid integer)
returns table
(
    course_id varchar(20),
    course_name varchar(30),
    credit integer,
    class_hour integer,
    grading varchar(10),
    prerequisite varchar(300)
)
as $$
    begin
        return query
            select c.course_id,c.course_name,c.credit,c.class_hour,c.grading,c.prerequisite from course c inner join course_to_coursesection ctc on c.course_id = ctc.course_id
            where ctc.section_id = csid;
    end;
$$
language plpgsql;

create or replace function getCourseSectionClassesBySection(csid integer)
returns table
(
    class_id integer,
    dayofweek integer,
    weeklist integer,
    classbegin integer,
    classend integer,
    location varchar(30),
    instructor_id varchar(20),
    first_name varchar(40),
    last_name varchar(40)
)
as $$
    begin
        return query
            select c.class_id, c.dayofweek, c.weeklist, c.classbegin, c.classend, c.location, cti.instructor_id, i.first_name ,i.last_name from coursesection_to_class ctc inner join coursesectionclass c on ctc.class_id = c.class_id
                    inner join coursesectionclass_to_instructor cti on c.class_id = cti.class_id
                    inner join instructor i on cti.instructor_id = i.instructor_id
                    where ctc.section_id = csid;
    end;
$$
language plpgsql;

create or replace function getCourseSectionByClass(classid integer)
returns table
(
    section_id integer,
    section_name varchar(30),
    totalcapacity integer,
    leftcapacity integer
)
as $$
    begin
        return query
            select c.section_id, c.section_name, c.totalcapacity, c.leftcapacity from coursesection c inner join coursesection_to_class ctc on c.section_id = ctc.section_id
                    where ctc.class_id = class_id;
    end;
$$
language plpgsql;

create or replace function getEnrolledStudentsInSemester(cid varchar(20),smid integer)
returns table
(
    student_id varchar(20),
    first_name varchar(40),
    last_name varchar(40),
    enrolled_date date
)
as $$
    begin
        return query
            select s.student_id,s.first_name,s.last_name,s.enrolled_date from
                    coursesection_to_semester cs inner join course_to_coursesection ctc on cs.section_id = ctc.section_id
                    inner join student_coursesection sc on cs.section_id = sc.section_id
                    inner join student s on sc.student_id = s.student_id
                    where cs.semester_id = smid and ctc.course_id = cid;
    end;
$$
language plpgsql;

--获取学生某周某一天的课程表
create or replace function getStudentCourseTable(studentId varchar(20),dateInput date,day_of_week integer)
    returns table
            (
                course_name varchar(30),
                section_name varchar(30),
                instructor_id varchar(20),
                first_name varchar(40),
                last_name varchar(40),
                class_begin integer,
                class_end integer,
                location varchar(30)
            )


as $$
declare
    num_of_week integer;
    begin_date_of_semester date;

begin

    select begin_date into begin_date_of_semester from semester
    where dateInput > begin_date
      and dateInput < end_date;
    select ceil(date_part('day',dateInput::timestamp-begin_date_of_semester::timestamp)/7)+1 into num_of_week;

    return query
        select course.course_name,
               css.section_name,
               instructor.instructor_id,
               instructor.first_name,
               instructor.last_name,
               coursesectionclass.classbegin,
               coursesectionclass.classend,
               coursesectionclass.location
        from coursesectionclass inner join coursesectionclass_to_instructor cti on coursesectionclass.class_id = cti.class_id
                                inner join instructor on instructor.instructor_id = cti.instructor_id
                                inner join coursesection_to_class ctc on coursesectionclass.class_id = ctc.class_id
                                inner join course_to_coursesection c on ctc.section_id = c.section_id
                                inner join course on c.course_id = course.course_id
                                inner join coursesection_to_semester cts on c.section_id = cts.section_id
                                inner join coursesection css on css.section_id = cts.section_id
                                inner join semester sm on cts.semester_id = sm.semester_id
                                inner join student_coursesection sc on c.section_id = sc.section_id
        where sc.student_id = studentId
          and   sm.end_date > dateInput
          and   sm.begin_date < dateInput
          and   (coursesectionclass.weeklist >> (num_of_week-1)) & 1 = 1
          and   coursesectionclass.dayofweek = day_of_week;
end;
$$
    language plpgsql;

--根据studentId和semesterId选出这个学生
create or replace function getEnrolledCoursesAndGrades(studentId varchar,semesterId integer)
    returns table
            (
                course_id varchar(20),
                course_name varchar(30),
                credit integer,
                class_hour integer,
                grading varchar(10), --课程的评分方式
                state varchar,
                grade integer,
                begin_date date
            )
as $$

begin
    return query
        select c.course_id,c.course_name,c.credit,c.class_hour,c.grading,sc.state,sc.grade,s2.begin_date from student s inner join student_coursesection sc on s.student_id = sc.student_id
                                                                                                                        inner join coursesection_to_semester cts on sc.section_id = cts.section_id
                                                                                                                        inner join course_to_coursesection ctc on sc.section_id = ctc.section_id
                                                                                                                        inner join course c on ctc.course_id = c.course_id
                                                                                                                        inner join semester s2 on cts.semester_id = s2.semester_id
        where s.student_id = studentId
          and cts.semester_id = semesterId;
end;
$$
    language plpgsql;


create or replace function getAllEnrolledCourseAndGrade(studentId varchar(20))
    returns table
            (
                course_id varchar(20),
                course_name varchar(30),
                credit integer,
                class_hour integer,
                grading varchar(10), --课程的评分方式
                state varchar,
                grade integer,
                begin_date date
            )
as $$

begin
    return query
        select c.course_id,c.course_name,c.credit,c.class_hour,c.grading,sc.state,sc.grade,s2.begin_date from student s
            inner join student_coursesection sc on s.student_id = sc.student_id
            inner join coursesection_to_semester cts on sc.section_id = cts.section_id
            inner join course_to_coursesection ctc on sc.section_id = ctc.section_id
            inner join course c on ctc.course_id = c.course_id                                                                                                          inner join semester s2 on cts.semester_id = s2.semester_id
        where s.student_id = studentId;
end;
$$
    language plpgsql;

create or replace function querystudentpassedcourse(studentid character varying) returns TABLE(course_id character varying)
    language plpgsql
as $$
begin
    return query
        select ctc.course_id from student s inner join student_coursesection sc on s.student_id = sc.student_id
                                            inner join course_to_coursesection ctc on sc.section_id = ctc.section_id
        where s.student_id = studentId and sc.state = 'Pass';
end;
$$




