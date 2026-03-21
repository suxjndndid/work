package org.example.work.module.analytics.service;

import org.example.work.module.analytics.dto.ClassAnalyticsDTO;
import org.example.work.module.analytics.dto.StudentAnalyticsDTO;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

public interface AnalyticsService {

    /** 班级整体学情统计 */
    ClassAnalyticsDTO getClassAnalytics(Long courseId);

    /** 学生个人学情统计 */
    StudentAnalyticsDTO getStudentAnalytics(Long studentId);

    /** AI 班级学情分析报告 */
    String generateClassReport(Long courseId);

    /** AI 学生个人分析报告 */
    String generateStudentReport(Long studentId);

    /** AI 班级学情分析报告（流式） */
    void streamClassReport(Long courseId, SseEmitter emitter);

    /** AI 学生个人分析报告（流式） */
    void streamStudentReport(Long studentId, SseEmitter emitter);
}
