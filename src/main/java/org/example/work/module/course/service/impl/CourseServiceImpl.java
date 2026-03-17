package org.example.work.module.course.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.example.work.module.course.entity.Course;
import org.example.work.module.course.mapper.CourseMapper;
import org.example.work.module.course.service.CourseService;
import org.springframework.stereotype.Service;

@Service
public class CourseServiceImpl extends ServiceImpl<CourseMapper, Course> implements CourseService {
}
