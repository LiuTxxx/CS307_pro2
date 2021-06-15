create table if not exists department
(
    department_id serial not null
        constraint department_pkey
            primary key,
    department_name varchar(30) not null
        constraint department_department_name_key
            unique
);


create trigger department_delete_trg
    before delete
    on department
    for each row
execute procedure department_delete();

create table if not exists semester
(
    semester_id serial not null
        constraint semester_pkey
            primary key,
    semester_name varchar(30) not null
        constraint semester_semester_name_key
            unique,
    begin_date date not null
        constraint semester_begin_date_key
            unique,
    end_date date not null
        constraint semester_end_date_key
            unique
);

create trigger semester_delete_trg
    before delete
    on semester
    for each row
execute procedure semester_delete();

create table if not exists student
(
    student_id varchar(20) not null
        constraint student_pkey
            primary key,
    first_name varchar(40) not null,
    last_name varchar(40) not null,
    enrolled_date date not null
);


create trigger student_delete_trg
    before delete
    on student
    for each row
execute procedure student_delete();

create table if not exists major
(
    major_id serial not null
        constraint major_pkey
            primary key,
    major_name varchar(30) not null
        constraint major_major_name_key
            unique
);


create trigger major_delete_trg
    before delete
    on major
    for each row
execute procedure major_delete();

create table if not exists student_to_major
(
    student_id varchar(20) not null
        constraint student_to_major_student_id_fkey
            references student,
    major_id integer not null
        constraint student_to_major_major_id_fkey
            references major,
    constraint student_to_major_pkey
        primary key (major_id, student_id)
);


create table if not exists major_to_department
(
    department_id integer not null
        constraint major_to_department_department_id_fkey
            references department,
    major_id integer not null
        constraint major_to_department_major_id_fkey
            references major,
    constraint major_to_department_pkey
        primary key (major_id, department_id)
);


create table if not exists instructor
(
    instructor_id varchar(20) not null
        constraint instructor_pkey
            primary key,
    first_name varchar(40) not null,
    last_name varchar(40) not null
);


create trigger instructor_delete_trg
    before delete
    on instructor
    for each row
execute procedure instructor_delete();

create table if not exists course
(
    course_id varchar(20) not null
        constraint course_pkey
            primary key,
    course_name varchar(30) not null,
    credit integer not null,
    class_hour integer not null,
    grading varchar(10) not null,
    prerequisite varchar(300)
);


create trigger course_delete_trg
    before delete
    on course
    for each row
execute procedure course_delete();

create table if not exists coursesection
(
    section_id serial not null
        constraint coursesection_pkey
            primary key,
    section_name varchar(30) not null,
    totalcapacity integer not null,
    leftcapacity integer not null
);

create trigger coursesection_delete_trg
    before delete
    on coursesection
    for each row
execute procedure coursesection_delete();

create table if not exists coursesectionclass
(
    class_id serial not null
        constraint coursesectionclass_pkey
            primary key,
    dayofweek integer not null,
    weeklist integer not null,
    classbegin integer not null,
    classend integer not null,
    location varchar(30) not null
);


create trigger coursesectionclass_delete_trg
    before delete
    on coursesectionclass
    for each row
execute procedure coursesectionclass_delete();

create table if not exists coursesectionclass_to_instructor
(
    class_id integer not null
        constraint coursesectionclass_to_instructor_class_id_fkey
            references coursesectionclass,
    instructor_id varchar(20) not null
        constraint coursesectionclass_to_instructor_instructor_id_fkey
            references instructor,
    constraint coursesectionclass_to_instructor_pkey
        primary key (class_id, instructor_id)
);

create table if not exists course_to_coursesection
(
    section_id integer not null
        constraint coursesection_to_course_section_id_fkey
            references coursesection,
    course_id varchar(20) not null
        constraint coursesection_to_course_course_id_fkey
            references course,
    constraint coursesection_to_course_pkey
        primary key (section_id, course_id)
);


create table if not exists coursesection_to_semester
(
    section_id integer not null
        constraint coursesection_to_semester_section_id_fkey
            references coursesection,
    semester_id integer not null
        constraint coursesection_to_semester_semester_id_fkey
            references semester,
    constraint coursesection_to_semester_pkey
        primary key (section_id, semester_id)
);

create table if not exists course_to_major
(
    course_id varchar(20) not null
        constraint course_to_major_course_id_fkey
            references course,
    major_id integer not null
        constraint course_to_major_major_id_fkey
            references major,
    type varchar not null,
    constraint course_to_major_pkey
        primary key (course_id, major_id)
);


create table if not exists coursesection_to_class
(
    section_id integer not null
        constraint coursesection_to_class_section_id_fkey
            references coursesection,
    class_id integer not null
        constraint coursesection_to_class_class_id_fkey
            references coursesectionclass,
    constraint coursesection_to_class_pkey
        primary key (section_id, class_id)
);



create table if not exists student_coursesection
(
    student_id varchar not null
        constraint student_course_student_id_fkey
            references student,
    section_id integer not null
        constraint student_course_section_id_fkey
            references coursesection,
    state varchar not null,
    grade integer,
    constraint student_course_pkey
        primary key (section_id, student_id, state)
);



create or replace function student_delete() returns trigger
    language plpgsql
as $$
declare
    sid varchar(20);
begin
    sid := old.student_id;
    delete from student_to_major where student_id  = sid;
    delete from student_coursesection where student_id = sid;
    return old;
end;
$$;


create or replace function instructor_delete() returns trigger
    language plpgsql
as $$
declare
    iid varchar(20);
begin
    iid := old.instructor_id;
    delete from coursesectionclass_to_instructor where instructor_id  = iid;
    return old;
end;
$$;


create or replace function department_delete() returns trigger
    language plpgsql
as $$
declare
    did integer;
begin
    did := old.department_id;
    delete from major_to_department where department_id  = did;
    return old;
end;
$$;


create or replace function major_delete() returns trigger
    language plpgsql
as $$
declare
    mid integer;
begin
    mid := old.major_id;
    delete from student_to_major where major_id  = mid;
    delete from course_to_major where major_id = mid;
    delete from major_to_department where major_id = mid;

    return old;
end;
$$;



create or replace function semester_delete() returns trigger
    language plpgsql
as $$
declare
    smid integer;
begin
    smid := old.semester_id;
    delete from coursesection_to_semester where semester_id  = smid;

    return old;
end;
$$;


create or replace function course_delete() returns trigger
    language plpgsql
as $$
declare
    cid integer;
begin
    cid := old.course_id;
    delete from course_to_major where course_id  = cid;
    delete from course_to_coursesection where course_id = cid;

    return old;
end;
$$;


create or replace function coursesection_delete() returns trigger
    language plpgsql
as $$
declare
    csid integer;
begin
    csid := old.section_id;
    delete from student_coursesection where section_id  = csid;
    delete from course_to_coursesection where section_id = csid;
    delete from coursesection_to_semester where section_id = csid;
    delete from coursesection_to_class where section_id = csid;


    return old;

end;
$$;


create or replace function coursesectionclass_delete() returns trigger
    language plpgsql
as $$
declare
    cscid integer;
begin
    cscid := old.class_id;
    delete from coursesectionclass_to_instructor where class_id  = cscid;
    delete from coursesection_to_class where class_id = cscid;

    return old;
end;
$$;



create or replace function getallusers() returns TABLE(user_id character varying, first_name character varying, last_name character varying)
    language plpgsql
as $$
begin
    return query
        select student_id as user_id,s.first_name,s.last_name from student s
        union all
        select instructor_id as user_id,i.first_name,i.last_name from instructor i;
end;
$$;


create or replace function getuser(uid character varying) returns TABLE(user_id character varying, first_name character varying, last_name character varying)
    language plpgsql
as $$
begin
    return query
        select student_id as user_id,s.first_name,s.last_name from student s
        union all
        select instructor_id as user_id,i.first_name,i.last_name from instructor i
        where user_id = uid;
end;
$$;


create or replace function getallsemesters() returns TABLE(semester_id integer, semester_name character varying, begin_date date, end_date date)
    language plpgsql
as $$
begin
    return query
        select sm.semester_id, sm.semester_name, sm.begin_date, sm.end_date from semester sm;
end
$$;


create or replace function getallsemesters(smid integer) returns TABLE(semester_id integer, semester_name character varying, begin_date date, end_date date)
    language plpgsql
as $$
begin
    return query
        select sm.semester_id, sm.semester_name, sm.begin_date, sm.end_date from semester sm
        where sm.semester_id = smid;
end
$$;


create or replace function getsemester(smid integer) returns TABLE(semester_id integer, semester_name character varying, begin_date date, end_date date)
    language plpgsql
as $$
begin
    return query
        select sm.semester_id, sm.semester_name, sm.begin_date, sm.end_date from semester sm
        where sm.semester_id = smid;
end
$$;


create or replace function getallmajors() returns TABLE(major_id integer, major_name character varying, department_id integer, department_name character varying)
    language plpgsql
as $$
begin
    return query
        select m.major_id,m.major_name,d.department_id,d.department_name
        from major m inner join major_to_department md on
                m.major_id = md.major_id
                     inner join department d on d.department_id = md.department_id;
end
$$;


create or replace function getmajor(mid integer) returns TABLE(major_id integer, major_name character varying, department_id integer, department_name character varying)
    language plpgsql
as $$
begin
    return query
        select m.major_id,m.major_name,d.department_id,d.department_name
        from major m inner join major_to_department md on
                m.major_id = md.major_id
                     inner join department d on d.department_id = md.department_id
        where m.major_id = mid;
end
$$;


create or replace function getalldepartments() returns TABLE(department_id integer, department_name character varying)
    language plpgsql
as $$
begin
    return query
        select d.department_id,d.department_name from department d;
end;
$$;


create or replace function getalldepartment(did integer) returns TABLE(department_id integer, department_name character varying)
    language plpgsql
as $$
begin
    return query
        select d.department_id,d.department_name from department d
        where d.department_id = did;
end;
$$;


create or replace function getdepartment(did integer) returns TABLE(department_id integer, department_name character varying)
    language plpgsql
as $$
begin
    return query
        select d.department_id,d.department_name from department d
        where d.department_id = did;
end;
$$;


create or replace function getcoursesectionsbysemester(cid character varying, smid integer) returns TABLE(section_id integer, section_name character varying, totalcapacity integer, leftcapacity integer)
    language plpgsql
as $$
begin
    return query
        select c.section_id,c.section_name,c.totalcapacity,c.leftcapacity from coursesection_to_semester cts
                                                                                   inner join course_to_coursesection ctc on cts.section_id = ctc.section_id
                                                                                   inner join coursesection c on ctc.section_id = c.section_id
        where ctc.course_id = cid and cts.semester_id = smid;
end;

$$;


create or replace function getcoursebysection(csid integer) returns TABLE(course_id character varying, course_name character varying, credit integer, class_hour integer, grading character varying, prerequisite character varying)
    language plpgsql
as $$
begin
    return query
        select c.course_id,c.course_name,c.credit,c.class_hour,c.grading,c.prerequisite from course c inner join course_to_coursesection ctc on c.course_id = ctc.course_id
        where ctc.section_id = csid;
end;
$$;


create or replace function getcoursesectionclassesbysection(csid integer) returns TABLE(class_id integer, dayofweek integer, weeklist integer, classbegin integer, classend integer, location character varying, instructor_id character varying, first_name character varying, last_name character varying)
    language plpgsql
as $$
begin
    return query
        select c.class_id, c.dayofweek, c.weeklist, c.classbegin, c.classend, c.location, cti.instructor_id, i.first_name ,i.last_name from coursesection_to_class ctc inner join coursesectionclass c on ctc.class_id = c.class_id
                                                                                                                                                                       inner join coursesectionclass_to_instructor cti on c.class_id = cti.class_id
                                                                                                                                                                       inner join instructor i on cti.instructor_id = i.instructor_id
        where ctc.section_id = csid;
end;
$$;


create or replace function getcoursesectionbyclass(classid integer) returns TABLE(section_id integer, section_name character varying, totalcapacity integer, leftcapacity integer)
    language plpgsql
as $$
begin
    return query
        select c.section_id, c.section_name, c.totalcapacity, c.leftcapacity from coursesection c inner join coursesection_to_class ctc on c.section_id = ctc.section_id
        where ctc.class_id = class_id;
end;
$$;


create or replace function getenrolledstudentsinsemester(cid character varying, smid integer) returns TABLE(student_id character varying, first_name character varying, last_name character varying, enrolled_date date)
    language plpgsql
as $$
begin
    return query
        select s.student_id,s.first_name,s.last_name,s.enrolled_date from
            coursesection_to_semester cs inner join course_to_coursesection ctc on cs.section_id = ctc.section_id
                                         inner join student_coursesection sc on cs.section_id = sc.section_id
                                         inner join student s on sc.student_id = s.student_id
        where cs.semester_id = smid and ctc.course_id = cid;
end;
$$;


create or replace function getstudentmajor(studentid integer) returns TABLE(major_id integer, major_name character varying, department_id integer, department_name character varying)
    language plpgsql
as $$
begin
    return query
        select m.major_id,m.major_name,d.department_id,d.department_name from student s
                                                                                  inner join student_to_major stm on s.student_id = stm.student_id
                                                                                  inner join major m on stm.major_id = m.major_id
                                                                                  inner join major_to_department mtd on m.major_id = mtd.major_id
                                                                                  inner join department d on mtd.department_id = d.department_id
        where s.student_id = studentId;
end;
$$;


create or replace function getstudentmajor(studentid character varying) returns TABLE(major_id integer, major_name character varying, department_id integer, department_name character varying)
    language plpgsql
as $$
begin
    return query
        select m.major_id,m.major_name,d.department_id,d.department_name from student s
                                                                                  inner join student_to_major stm on s.student_id = stm.student_id
                                                                                  inner join major m on stm.major_id = m.major_id
                                                                                  inner join major_to_department mtd on m.major_id = mtd.major_id
                                                                                  inner join department d on mtd.department_id = d.department_id
        where s.student_id = studentId;
end;
$$;


create or replace function getstudentcoursetable(studentid character varying, dateinput date, day_of_week integer) returns TABLE(course_name character varying, section_name character varying, instructor_id character varying, first_name character varying, last_name character varying, class_begin integer, class_end integer, location character varying)
    language plpgsql
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
$$;


create or replace function querystudentpassedcourse(studentid character varying) returns TABLE(course_id character varying)
    language plpgsql
as $$
begin
    return query
        select ctc.course_id from student s inner join student_coursesection sc on s.student_id = sc.student_id
                                            inner join course_to_coursesection ctc on sc.section_id = ctc.section_id
        where s.student_id = studentId and sc.state = 'Pass';
end;
$$;


create or replace function getallenrolledcourseandgrade(studentid character varying) returns TABLE(course_id character varying, course_name character varying, credit integer, class_hour integer, grading character varying, state character varying, grade integer, begin_date date)
    language plpgsql
as $$
begin
    return query
        select c.course_id,c.course_name,c.credit,c.class_hour,c.grading,sc.state,sc.grade,s2.begin_date from student s
                                                                                                                  inner join student_coursesection sc on s.student_id = sc.student_id
                                                                                                                  inner join coursesection_to_semester cts on sc.section_id = cts.section_id
                                                                                                                  inner join course_to_coursesection ctc on sc.section_id = ctc.section_id
                                                                                                                  inner join course c on ctc.course_id = c.course_id
                                                                                                                  inner join semester s2 on cts.semester_id = s2.semester_id
        where s.student_id = studentId;
end;
$$;


create or replace function getenrolledcoursesandgrades(studentid character varying, semesterid integer) returns TABLE(course_id character varying, course_name character varying, credit integer, class_hour integer, grading character varying, state character varying, grade integer, begin_date date)
    language plpgsql
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
$$;


create or replace function search_func(para1 character varying, para2 character varying, para3 character varying, para4 character varying, para5 character varying, para6 character varying, para7 character varying, para8 character varying, para9 integer, para10 character varying, para11 character varying, para12 character varying, para13 character varying, para14 character varying, para15 integer, para16 character varying, para17 character varying, para18 integer, para19 character varying, para20 character varying, para21 character varying, para22 character varying, para23 character varying, para24 character varying, para25 character varying, para26 character varying, para27 character varying, para28 character varying, para29 character varying, para30 character varying) returns TABLE(course_id1 character varying, section_id1 integer, class_id1 integer, course_name1 character varying, credit1 integer, class_hour1 integer, grading1 character varying, section_name1 character varying, totalcapacity1 integer, leftcapacity1 integer, instructor_id1 character varying, first_name1 character varying, last_name1 character varying, dayofweek1 integer, weeklist1 integer, classbegin1 integer, classend1 integer, location1 character varying, state1 character varying)
    language plpgsql
as $$
begin
    return query
        select course_id, ss.section_id, class_id, course_name, credit, class_hour, grading, section_name, totalcapacity, leftcapacity, instructor_id, first_name, last_name, dayofweek, weeklist, classbegin, classend, location, state from
            (select course_id, gg.section_id, class_id, course_name, credit, class_hour, grading, section_name, totalcapacity, leftcapacity, instructor_id, first_name, last_name, dayofweek, weeklist, classbegin, classend, location from
                (select ff.course_id, course_name, credit, class_hour, grading, section_id from
                    (select i.course_id, course_name, credit, class_hour, grading from
                        (select * from course
                         where (para1 = para2 or course_id = para3) and (para4 = para5 or course_name = para6))i
                            inner join
                        (select course_id, g.major_id from
                            (select * from course_to_major
                             where (type = para7) )g
                                inner join
                            (select * from student_to_major
                             where (student_id = para8))h on g.major_id = h.major_id) j on i.course_id = j.course_id) ee
                        inner join
                    course_to_coursesection ff on ee.course_id = ff.course_id) gg

                    inner join

                (select section_name,class_id, dd.section_id,totalcapacity,leftcapacity,instructor_id, dayofweek,weeklist,classbegin,classend,location,first_name,last_name from
                    (select class_id, section_id,k.instructor_id, dayofweek,weeklist,classbegin,classend,location,first_name,last_name from
                        (select f.class_id, e.section_id,semester_id,instructor_id, dayofweek,weeklist,classbegin,classend,location from
                            (select c.section_id, semester_id, class_id from
                                (select * from coursesection_to_semester
                                 where (semester_id = para9))c
                                    inner join
                                coursesection_to_class d on c.section_id = d.section_id)e
                                inner join
                            (select a.class_id,instructor_id, dayofweek,weeklist,classbegin,classend,location from
                                (select * from coursesectionclass_to_instructor
                                 where (para10 = para11 or instructor_id = para12)) a
                                    inner join
                                (select * from coursesectionclass
                                 where (para13 = para14 or dayofweek = para15) and (para16 = para17 or classbegin <= para18) and (para16 = para17 or classend >= para18) and (para19 = para20 or para21 ~ location)) b
                                on a.class_id = b.class_id)f on e.class_id = f.class_id)k
                            inner join
                        (select aa.instructor_id, first_name, last_name from
                            (instructor aa
                                inner join
                            coursesectionclass_to_instructor bb on aa.instructor_id = bb.instructor_id)) l on k.instructor_id = l.instructor_id) cc
                        inner join
                    (select * from coursesection
                     where para22 = para23 or leftcapacity > 0) dd on cc.section_id = dd.section_id)hh on gg.section_id = hh.section_id)ss
                left join
            student_coursesection tt on ss.section_id = tt.section_id
        where state != 'enroll';
end
$$;


create or replace function getconflict(para1 integer, para2 integer, para3 integer, para4 integer, para5 integer) returns TABLE(coursename character varying)
    language plpgsql
as $$
begin
    return query
        select course_name from
            (select course_id from
                (select f.section_id from
                    (select a.section_id from
                        (coursesection a
                            inner join
                        coursesection_to_semester b on a.section_id = b.section_id)
                     where semester_id = para1 ) c
                        inner join
                    (select * from
                        (select * from coursesectionclass
                         where dayofweek = para2 and weeklist & para3 != 0 and classbegin > para4 and para5 > classend)d
                            inner join
                        coursesection_to_class e on d.class_id = e.class_id)f on f.section_id = c.section_id) h
                    inner join
                course_to_coursesection j on h.section_id = j.section_id)hh
                inner join
            course jj on hh.course_id = jj.course_id;
end
$$;







