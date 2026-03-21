package org.example.work.module.prepplan.controller;

import cn.dev33.satoken.stp.StpUtil;
import org.example.work.common.Result;
import org.example.work.common.SseHelper;
import org.example.work.module.prepplan.service.PrepPlanService;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.Map;

@RestController
@RequestMapping("/api/prep-plan")
public class PrepPlanController {

    private final PrepPlanService prepPlanService;

    public PrepPlanController(PrepPlanService prepPlanService) {
        this.prepPlanService = prepPlanService;
    }

    /** 生成总体备课方案（流式） */
    @PostMapping(value = "/generate", produces = "text/event-stream")
    public SseEmitter generate(@RequestBody Map<String, Object> body) {
        Long lessonPlanId = toLong(body.get("lessonPlanId"));
        Long courseId = toLong(body.get("courseId"));
        String requirements = body.get("requirements") != null ? body.get("requirements").toString() : null;
        SseEmitter emitter = SseHelper.createEmitter();
        if (lessonPlanId == null) {
            try {
                emitter.send(SseEmitter.event().data("教案ID不能为空"));
                emitter.send(SseEmitter.event().data("[DONE]"));
                emitter.complete();
            } catch (Exception e) {
                emitter.completeWithError(e);
            }
            return emitter;
        }
        prepPlanService.streamPrepPlan(lessonPlanId, courseId, requirements, emitter);
        return emitter;
    }

    private Long toLong(Object obj) {
        if (obj == null) return null;
        if (obj instanceof Number) return ((Number) obj).longValue();
        try { return Long.valueOf(obj.toString()); } catch (NumberFormatException e) { return null; }
    }
}
