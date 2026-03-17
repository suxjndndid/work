package org.example.work.module.analytics.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.example.work.common.Result;
import org.example.work.module.analytics.entity.KnowledgePoint;
import org.example.work.module.analytics.mapper.KnowledgePointMapper;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/knowledge-point")
public class KnowledgePointController {

    private final KnowledgePointMapper knowledgePointMapper;

    public KnowledgePointController(KnowledgePointMapper knowledgePointMapper) {
        this.knowledgePointMapper = knowledgePointMapper;
    }

    @GetMapping("/list")
    public Result<List<KnowledgePoint>> list(@RequestParam Long courseId) {
        LambdaQueryWrapper<KnowledgePoint> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(KnowledgePoint::getCourseId, courseId)
               .orderByAsc(KnowledgePoint::getSortOrder);
        return Result.ok(knowledgePointMapper.selectList(wrapper));
    }

    @PostMapping
    public Result<?> create(@RequestBody KnowledgePoint knowledgePoint) {
        knowledgePointMapper.insert(knowledgePoint);
        return Result.ok();
    }

    @PutMapping("/{id}")
    public Result<?> update(@PathVariable Long id, @RequestBody KnowledgePoint knowledgePoint) {
        knowledgePoint.setId(id);
        knowledgePointMapper.updateById(knowledgePoint);
        return Result.ok();
    }

    @DeleteMapping("/{id}")
    public Result<?> delete(@PathVariable Long id) {
        knowledgePointMapper.deleteById(id);
        return Result.ok();
    }
}
