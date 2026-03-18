package org.example.work.module.lessonplan.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.example.work.common.exception.BusinessException;
import org.example.work.module.ai.service.LessonPlanAiService;
import org.example.work.module.lessonplan.dto.LessonPlanGenerateRequest;
import org.example.work.module.lessonplan.entity.LessonPlan;
import org.example.work.module.lessonplan.entity.LessonPlanVersion;
import org.example.work.module.lessonplan.mapper.LessonPlanMapper;
import org.example.work.module.lessonplan.mapper.LessonPlanVersionMapper;
import org.example.work.module.lessonplan.service.LessonPlanService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class LessonPlanServiceImpl implements LessonPlanService {

    private static final Logger log = LoggerFactory.getLogger(LessonPlanServiceImpl.class);

    private final LessonPlanMapper lessonPlanMapper;
    private final LessonPlanVersionMapper versionMapper;
    private final LessonPlanAiService lessonPlanAiService;

    public LessonPlanServiceImpl(LessonPlanMapper lessonPlanMapper,
                                  LessonPlanVersionMapper versionMapper,
                                  LessonPlanAiService lessonPlanAiService) {
        this.lessonPlanMapper = lessonPlanMapper;
        this.versionMapper = versionMapper;
        this.lessonPlanAiService = lessonPlanAiService;
    }

    @Override
    public LessonPlan generate(LessonPlanGenerateRequest request, Long userId) {
        log.info("开始为用户 {} 生成教案: {}", userId, request.getTopic());

        // 调用 AI 生成教案内容
        long startTime = System.currentTimeMillis();
        log.debug("[AI] 教案生成请求 - subject={}, grade={}, topic={}, duration={}",
                request.getSubject(), request.getGrade(), request.getTopic(), request.getDuration());

        String content = lessonPlanAiService.generateLessonPlan(
                request.getSubject(),
                request.getGrade(),
                request.getTopic(),
                request.getObjectives() != null ? request.getObjectives() : "根据课程标准自动确定",
                request.getDuration() != null ? request.getDuration() : 45
        );

        long elapsed = System.currentTimeMillis() - startTime;
        log.debug("[AI] 教案生成完成 - 耗时 {}ms, 内容长度 {} 字符", elapsed, content != null ? content.length() : 0);

        // 构建教案实体
        LessonPlan lessonPlan = new LessonPlan();
        lessonPlan.setUserId(userId);
        lessonPlan.setCourseId(request.getCourseId());
        lessonPlan.setTitle(request.getSubject() + " - " + request.getTopic());
        lessonPlan.setSubject(request.getSubject());
        lessonPlan.setGrade(request.getGrade());
        lessonPlan.setTopic(request.getTopic());
        lessonPlan.setObjectives(request.getObjectives());
        lessonPlan.setDuration(request.getDuration());
        lessonPlan.setContent(content);
        lessonPlan.setStatus(0);
        lessonPlan.setVersion(1);
        lessonPlanMapper.insert(lessonPlan);

        // 保存版本记录
        saveVersion(lessonPlan.getId(), 1, content, "AI 初次生成");

        log.info("教案生成完成, id={}", lessonPlan.getId());
        return lessonPlan;
    }

    @Override
    public LessonPlan regenerate(Long id) {
        LessonPlan existing = lessonPlanMapper.selectById(id);
        if (existing == null) {
            throw new BusinessException("教案不存在");
        }

        // 重新调用 AI 生成
        long startTime = System.currentTimeMillis();
        log.debug("[AI] 教案重新生成 - id={}, topic={}", id, existing.getTopic());

        String content = lessonPlanAiService.generateLessonPlan(
                existing.getSubject(),
                existing.getGrade(),
                existing.getTopic(),
                existing.getObjectives() != null ? existing.getObjectives() : "根据课程标准自动确定",
                existing.getDuration() != null ? existing.getDuration() : 45
        );

        long elapsed = System.currentTimeMillis() - startTime;
        log.debug("[AI] 教案重新生成完成 - 耗时 {}ms", elapsed);

        // 更新教案
        int newVersion = existing.getVersion() + 1;
        existing.setContent(content);
        existing.setVersion(newVersion);
        lessonPlanMapper.updateById(existing);

        // 保存版本记录
        saveVersion(id, newVersion, content, "AI 重新生成");

        return existing;
    }

    @Override
    public Page<LessonPlan> page(int current, int size, Long userId, String keyword) {
        Page<LessonPlan> page = new Page<>(current, size);
        LambdaQueryWrapper<LessonPlan> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(LessonPlan::getUserId, userId);
        if (keyword != null && !keyword.isBlank()) {
            wrapper.and(w -> w.like(LessonPlan::getTitle, keyword)
                    .or().like(LessonPlan::getTopic, keyword)
                    .or().like(LessonPlan::getSubject, keyword));
        }
        wrapper.orderByDesc(LessonPlan::getCreateTime);
        return lessonPlanMapper.selectPage(page, wrapper);
    }

    @Override
    public LessonPlan getById(Long id) {
        return lessonPlanMapper.selectById(id);
    }

    @Override
    public void update(LessonPlan lessonPlan) {
        LessonPlan existing = lessonPlanMapper.selectById(lessonPlan.getId());
        if (existing == null) {
            throw new BusinessException("教案不存在");
        }

        // 如果内容变化了，保存版本
        if (lessonPlan.getContent() != null && !lessonPlan.getContent().equals(existing.getContent())) {
            int newVersion = existing.getVersion() + 1;
            lessonPlan.setVersion(newVersion);
            saveVersion(lessonPlan.getId(), newVersion, lessonPlan.getContent(), "手动编辑");
        }

        lessonPlanMapper.updateById(lessonPlan);
    }

    @Override
    public void delete(Long id) {
        lessonPlanMapper.deleteById(id);
    }

    @Override
    public List<LessonPlanVersion> getVersions(Long lessonPlanId) {
        LambdaQueryWrapper<LessonPlanVersion> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(LessonPlanVersion::getLessonPlanId, lessonPlanId)
               .orderByDesc(LessonPlanVersion::getVersionNumber);
        return versionMapper.selectList(wrapper);
    }

    private void saveVersion(Long lessonPlanId, int versionNumber, String content, String changeNote) {
        LessonPlanVersion version = new LessonPlanVersion();
        version.setLessonPlanId(lessonPlanId);
        version.setVersionNumber(versionNumber);
        version.setContent(content);
        version.setChangeNote(changeNote);
        version.setCreateTime(LocalDateTime.now());
        versionMapper.insert(version);
    }
}
