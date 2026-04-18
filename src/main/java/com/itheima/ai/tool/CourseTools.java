package com.itheima.ai.tool;

import com.baomidou.mybatisplus.extension.conditions.query.QueryChainWrapper;
import com.itheima.ai.entity.po.Course;
import com.itheima.ai.entity.po.CourseReservation;
import com.itheima.ai.entity.po.School;
import com.itheima.ai.entity.po.query.CourseQuery;
import com.itheima.ai.service.ICourseReservationService;
import com.itheima.ai.service.ICourseService;
import com.itheima.ai.service.ISchoolService;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import java.util.List;

@RequiredArgsConstructor
@Component
public class CourseTools {
    private final ICourseService courseService;
    private final ISchoolService schoolService;
    private final ICourseReservationService courseReservationService;
    @Tool(description = "根据条件查询课程信息")
    public List<Course> queryCourse(@ToolParam(description = "查询的条件",required = false) CourseQuery query) {
        if (query == null){
            return courseService.list();
        }
        QueryChainWrapper<Course> wrapper = courseService.query()
                .eq(query.getType() != null, "type", query.getType())
                .le(query.getEdu() != null, "edu", query.getEdu());

        if(query.getSorts()!= null&& !query.getSorts().isEmpty()){
            for (CourseQuery.Sort sort : query.getSorts()) {
                wrapper.orderBy(true,sort.getAsc(), sort.getField());

            }
        }
        return wrapper.list();
    }

    @Tool(description = "查询所有校区")
    public List<School> querySchool(){
        return schoolService.list();
    }

    @Tool(description = "生成预约单，返回预约单号")
    public Integer createCourseReservation(@ToolParam(description = "预约课程") String course,
                                           @ToolParam(description = "预约校区") String school,
                                           @ToolParam(description = "学生姓名") String studentName,
                                           @ToolParam(description = "预约人电话") String contactInfo,
                                           @ToolParam(description = "备注", required = false) String remark){

        CourseReservation courseReservation = new CourseReservation();
        courseReservation.setCourse(course);
        courseReservation.setSchool(school);
        courseReservation.setStudentName(studentName);
        courseReservation.setContactInfo(contactInfo);
        courseReservation.setRemark(remark);
        courseReservationService.save(courseReservation);
        return courseReservation.getId();
    }

}
