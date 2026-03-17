package org.example.work.module.course.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.example.work.common.Result;
import org.example.work.module.course.entity.Course;
import org.example.work.module.course.service.CourseService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/course")
public class CourseController {

    private final CourseService courseService;

    public CourseController(CourseService courseService) {
        this.courseService = courseService;
    }

    @GetMapping("/list")
    public Result<?> list(@RequestParam(defaultValue = "1") int current,
                          @RequestParam(defaultValue = "10") int size,
                          @RequestParam(required = false) String keyword) {
        Page<Course> page = new Page<>(current, size);
        LambdaQueryWrapper<Course> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Course::getUserId, StpUtil.getLoginIdAsLong());
        if (keyword != null && !keyword.isBlank()) {
            wrapper.and(w -> w.like(Course::getName, keyword).or().like(Course::getSubject, keyword));
        }
        wrapper.orderByDesc(Course::getCreateTime);
        courseService.page(page, wrapper);
        return Result.ok(page);
    }

    @GetMapping("/{id}")
    public Result<Course> getById(@PathVariable Long id) {
        return Result.ok(courseService.getById(id));
    }

    @PostMapping
    public Result<?> create(@RequestBody Course course) {
        course.setUserId(StpUtil.getLoginIdAsLong());
        courseService.save(course);
        return Result.ok();
    }

    @PutMapping("/{id}")
    public Result<?> update(@PathVariable Long id, @RequestBody Course course) {
        course.setId(id);
        courseService.updateById(course);
        return Result.ok();
    }

    @DeleteMapping("/{id}")
    public Result<?> delete(@PathVariable Long id) {
        courseService.removeById(id);
        return Result.ok();
    }
}
