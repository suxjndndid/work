package org.example.work.module.analytics.service;

import org.example.work.module.analytics.dto.ClassAnalyticsDTO;
import org.example.work.module.analytics.dto.StudentAnalyticsDTO;

public interface AnalyticsService {

    /** 班级整体学情统计 */
    ClassAnalyticsDTO getClassAnalytics(Long courseId);

    /** 学生个人学情统计 */
    StudentAnalyticsDTO getStudentAnalytics(Long studentId);

    /** AI 班级学情分析报告 */
    String generateClassReport(Long courseId);

    /** AI 学生个人分析报告 */
    String generateStudentReport(Long studentId);
}
