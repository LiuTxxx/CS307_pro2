package cn.edu.sustech.Impl.Utils;

import cn.edu.sustech.cs307.dto.prerequisite.AndPrerequisite;
import cn.edu.sustech.cs307.dto.prerequisite.CoursePrerequisite;
import cn.edu.sustech.cs307.dto.prerequisite.OrPrerequisite;
import cn.edu.sustech.cs307.dto.prerequisite.Prerequisite;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

public class PrerequisiteUtils {



    public static void main(String[] args) {
        //测试转换字符串
        List<Prerequisite> list = new ArrayList<>();
        list.add(new CoursePrerequisite("cs202"));
        list.add(new CoursePrerequisite("cs307"));
        List<Prerequisite> list2 = new ArrayList<>();
        list2.add(new CoursePrerequisite("cs111"));
        list2.add(new CoursePrerequisite("cs000"));
        list.add(new OrPrerequisite(list2));
        AndPrerequisite p = new AndPrerequisite(list);
        System.out.println(cvtString(p));
        String aaa = cvtString(p);

        //测试先修课判断
        ArrayList<String> coursePassed = new ArrayList<>();
        coursePassed.add("cs202");
        coursePassed.add("cs307");
        coursePassed.add("cs000");
        System.out.println(judge(coursePassed,aaa));

    }


    //将某个Prerequisite关系转换成常规的逻辑表达式字符串存入数据库
    public static String cvtString(Prerequisite pre)
    {
        if(pre == null) return null;
        if(pre instanceof AndPrerequisite) return cvtAndString((AndPrerequisite)pre);
        else if(pre instanceof OrPrerequisite) return cvtOrString((OrPrerequisite)pre);
        CoursePrerequisite coursePrerequisite = (CoursePrerequisite)pre;
        return coursePrerequisite.courseID;
    }

    private static String cvtAndString(AndPrerequisite pre)
    {
        StringBuilder ans = new StringBuilder();
        for(int i = 0;  i< pre.terms.size(); i++)
        {
            Prerequisite p = pre.terms.get(i);

            if(p instanceof CoursePrerequisite)
            {
                if(i != pre.terms.size()-1){
                    ans.append(String.format("%s&", ((CoursePrerequisite) p).courseID));
                }else{
                    ans.append(String.format("%s", ((CoursePrerequisite) p).courseID));
                }

            }
            else if(p instanceof AndPrerequisite)
            {
                ans.append(cvtAndString((AndPrerequisite) p));
            }
            else
            {
                ans.append(cvtOrString((OrPrerequisite) p));
            }

        }

        return String.format("(%s)", ans);

    }

    private static String cvtOrString(OrPrerequisite pre)
    {
        StringBuilder ans = new StringBuilder();
        for(int i = 0;  i< pre.terms.size(); i++)
        {
            Prerequisite p = pre.terms.get(i);

            if(p instanceof CoursePrerequisite)
            {
                if(i != pre.terms.size()-1){
                    ans.append(String.format("%s|", ((CoursePrerequisite) p).courseID));
                }else{
                    ans.append(String.format("%s", ((CoursePrerequisite) p).courseID));
                }

            }
            else if(p instanceof AndPrerequisite)
            {
                ans.append(cvtAndString((AndPrerequisite) p));
            }
            else
            {
                ans.append(cvtOrString((OrPrerequisite) p));
            }

        }

        return String.format("(%s)", ans);

    }

    //判断是否符合先修课
    //courseIdList为学生已修课程的courseId,exp为从数据库里查出来的代表先修关系的字符串
    public static boolean judge(ArrayList<String> courseIdList,String exp)
    {
        if(exp == null) return true;


        for(String courseID : courseIdList)
        {
            exp = exp.replaceAll(courseID,"1");
        }


        Stack<Integer> num = new Stack<>();
        Stack<Character> op = new Stack<>();


        ArrayList<String> buffer = new ArrayList<>();
        for(int i = 0; i < exp.length(); i++)
        {
            Character c = exp.charAt(i);
            if(isCourseId(c))
            {
                int j = i;

                StringBuilder sb = new StringBuilder();
                while(j < exp.length() && exp.charAt(j) != '&' && exp.charAt(j) != '|' && exp.charAt(j) != '(' && exp.charAt(j) != ')')
                {
                    sb.append(exp.charAt(j++));
                }
                buffer.add(sb.toString());
                i = j-1;
            }
        }

        for(String s : buffer)
        {
            exp = exp.replaceAll(s,"0");
        }

        int len = exp.length();

        for(int i = 0; i < len; i++)
        {
            char c = exp.charAt(i);
            if(c == '1')
            {
                num.push(1);
            }else if(c == '0')
            {
                num.push(0);
            }
            else if(c == '(') op.push('(');
            else if(c == ')')
            {
                while(op.size() != 0 && op.peek() != '(')
                {
                    operate(num, op);
                }
                op.pop();
            }

            else
            {
                while(op.size() != 0 && op.peek() != '(')
                {
                    operate(num, op);
                }
                op.push(c);
            }
        }

        while(op.size() != 0)
        {
            operate(num, op);

        }

        int ans = num.peek();

        return ans == 1;
    }

    private static void operate(Stack<Integer> num, Stack<Character> op) {
        int second = num.pop();
        int first = num.pop();
        Character operator = op.pop();

        int ans = 0;

        if(operator == '&')
        {
            ans = first & second;
        }
        else if(operator == '|')
        {
            ans = first | second;
        }
        num.push(ans);
    }

    private static boolean isCourseId(Character c)
    {
        return c!='1' && c != '0' && c != '&' && c!= '|'&& c != '(' && c != ')';
    }


}

