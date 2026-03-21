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
    public SseEmitter generate(@RequestBody Map<String, Long> body) {
        Long lessonPlanId = body.get("lessonPlanId");
        Long courseId = body.get("courseId");
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
        prepPlanService.streamPrepPlan(lessonPlanId, courseId, emitter);
        return emitter;
    }
}
