package org.example.work.module.prepplan.service;

public interface PrepPlanService {

    /** 生成总体备课方案 */
    String generatePrepPlan(Long lessonPlanId, Long courseId);
}
