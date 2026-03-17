package org.example.work.module.analytics.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.example.work.common.Result;
import org.example.work.module.analytics.entity.Student;
import org.example.work.module.analytics.mapper.StudentMapper;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/student")
public class StudentController {

    private final StudentMapper studentMapper;

    public StudentController(StudentMapper studentMapper) {
        this.studentMapper = studentMapper;
    }

    @GetMapping("/list")
    public Result<?> list(@RequestParam(defaultValue = "1") int current,
                          @RequestParam(defaultValue = "10") int size,
                          @RequestParam(required = false) String className,
                          @RequestParam(required = false) String keyword) {
        Page<Student> page = new Page<>(current, size);
        LambdaQueryWrapper<Student> wrapper = new LambdaQueryWrapper<>();
        if (className != null && !className.isBlank()) {
            wrapper.eq(Student::getClassName, className);
        }
        if (keyword != null && !keyword.isBlank()) {
            wrapper.and(w -> w.like(Student::getName, keyword).or().like(Student::getStudentNo, keyword));
        }
        wrapper.orderByAsc(Student::getStudentNo);
        studentMapper.selectPage(page, wrapper);
        return Result.ok(page);
    }

    @GetMapping("/{id}")
    public Result<Student> getById(@PathVariable Long id) {
        return Result.ok(studentMapper.selectById(id));
    }

    @PostMapping
    public Result<?> create(@RequestBody Student student) {
        studentMapper.insert(student);
        return Result.ok();
    }

    @PutMapping("/{id}")
    public Result<?> update(@PathVariable Long id, @RequestBody Student student) {
        student.setId(id);
        studentMapper.updateById(student);
        return Result.ok();
    }

    @DeleteMapping("/{id}")
    public Result<?> delete(@PathVariable Long id) {
        studentMapper.deleteById(id);
        return Result.ok();
    }
}
