package org.example.work.module.prepplan.controller;

import cn.dev33.satoken.stp.StpUtil;
import org.example.work.common.Result;
import org.example.work.module.prepplan.service.PrepPlanService;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/prep-plan")
public class PrepPlanController {

    private final PrepPlanService prepPlanService;

    public PrepPlanController(PrepPlanService prepPlanService) {
        this.prepPlanService = prepPlanService;
    }

    /** 生成总体备课方案 (整合教案+习题+学情+推荐) */
    @PostMapping("/generate")
    public Result<String> generate(@RequestBody Map<String, Long> body) {
        Long lessonPlanId = body.get("lessonPlanId");
        Long courseId = body.get("courseId");
        if (lessonPlanId == null) {
            return Result.error("教案ID不能为空");
        }
        String plan = prepPlanService.generatePrepPlan(lessonPlanId, courseId);
        return Result.ok(plan);
    }
}
