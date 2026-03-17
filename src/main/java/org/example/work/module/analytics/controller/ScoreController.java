package org.example.work.module.analytics.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.example.work.common.Result;
import org.example.work.module.analytics.entity.Score;
import org.example.work.module.analytics.mapper.ScoreMapper;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/score")
public class ScoreController {

    private final ScoreMapper scoreMapper;

    public ScoreController(ScoreMapper scoreMapper) {
        this.scoreMapper = scoreMapper;
    }

    @GetMapping("/list")
    public Result<?> list(@RequestParam(defaultValue = "1") int current,
                          @RequestParam(defaultValue = "10") int size,
                          @RequestParam(required = false) Long studentId,
                          @RequestParam(required = false) Long courseId) {
        Page<Score> page = new Page<>(current, size);
        LambdaQueryWrapper<Score> wrapper = new LambdaQueryWrapper<>();
        if (studentId != null) {
            wrapper.eq(Score::getStudentId, studentId);
        }
        if (courseId != null) {
            wrapper.eq(Score::getCourseId, courseId);
        }
        wrapper.orderByDesc(Score::getExamDate);
        scoreMapper.selectPage(page, wrapper);
        return Result.ok(page);
    }

    @PostMapping
    public Result<?> create(@RequestBody Score score) {
        scoreMapper.insert(score);
        return Result.ok();
    }

    @PostMapping("/batch")
    public Result<?> batchCreate(@RequestBody List<Score> scores) {
        for (Score score : scores) {
            scoreMapper.insert(score);
        }
        return Result.ok();
    }

    @DeleteMapping("/{id}")
    public Result<?> delete(@PathVariable Long id) {
        scoreMapper.deleteById(id);
        return Result.ok();
    }
}
