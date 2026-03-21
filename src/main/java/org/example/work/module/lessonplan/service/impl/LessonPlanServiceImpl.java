package org.example.work.module.lessonplan.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.chat.StreamingChatModel;
import org.example.work.common.SseHelper;
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
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class LessonPlanServiceImpl implements LessonPlanService {

    private static final Logger log = LoggerFactory.getLogger(LessonPlanServiceImpl.class);

    private final LessonPlanMapper lessonPlanMapper;
    private final LessonPlanVersionMapper versionMapper;
    private final LessonPlanAiService lessonPlanAiService;
    private final StreamingChatModel streamingChatModel;

    public LessonPlanServiceImpl(LessonPlanMapper lessonPlanMapper,
                                  LessonPlanVersionMapper versionMapper,
                                  LessonPlanAiService lessonPlanAiService,
                                  StreamingChatModel streamingChatModel) {
        this.lessonPlanMapper = lessonPlanMapper;
        this.versionMapper = versionMapper;
        this.lessonPlanAiService = lessonPlanAiService;
        this.streamingChatModel = streamingChatModel;
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
    public void streamGenerate(LessonPlanGenerateRequest request, Long userId, SseEmitter emitter) {
        log.info("流式生成教案: userId={}, topic={}", userId, request.getTopic());

        // 先创建空壳记录拿到ID
        LessonPlan lessonPlan = new LessonPlan();
        lessonPlan.setUserId(userId);
        lessonPlan.setCourseId(request.getCourseId());
        lessonPlan.setTitle(request.getSubject() + " - " + request.getTopic());
        lessonPlan.setSubject(request.getSubject());
        lessonPlan.setGrade(request.getGrade());
        lessonPlan.setTopic(request.getTopic());
        lessonPlan.setObjectives(request.getObjectives());
        lessonPlan.setDuration(request.getDuration());
        lessonPlan.setContent("");
        lessonPlan.setStatus(0);
        lessonPlan.setVersion(1);
        lessonPlanMapper.insert(lessonPlan);

        // 发送ID作为第一条消息
        try {
            emitter.send(SseEmitter.event().name("id").data(String.valueOf(lessonPlan.getId())));
        } catch (IOException e) {
            emitter.completeWithError(e);
            return;
        }

        var messages = buildLessonPlanMessages(request);
        SseHelper.streamChat(streamingChatModel, messages, emitter, content -> {
            // 流式完成后更新DB
            lessonPlan.setContent(content);
            lessonPlanMapper.updateById(lessonPlan);
            saveVersion(lessonPlan.getId(), 1, content, "AI 初次生成");
            log.info("流式教案生成完成, id={}", lessonPlan.getId());
        });
    }

    @Override
    public void streamRegenerate(Long id, SseEmitter emitter) {
        LessonPlan existing = lessonPlanMapper.selectById(id);
        if (existing == null) {
            try {
                emitter.send(SseEmitter.event().data("教案不存在"));
                emitter.send(SseEmitter.event().data("[DONE]"));
                emitter.complete();
            } catch (Exception e) {
                emitter.completeWithError(e);
            }
            return;
        }

        log.info("流式重新生成教案: id={}, topic={}", id, existing.getTopic());
        int newVersion = existing.getVersion() + 1;

        LessonPlanGenerateRequest request = new LessonPlanGenerateRequest();
        request.setSubject(existing.getSubject());
        request.setGrade(existing.getGrade());
        request.setTopic(existing.getTopic());
        request.setObjectives(existing.getObjectives());
        request.setDuration(existing.getDuration());

        var messages = buildLessonPlanMessages(request);
        SseHelper.streamChat(streamingChatModel, messages, emitter, content -> {
            existing.setContent(content);
            existing.setVersion(newVersion);
            lessonPlanMapper.updateById(existing);
            saveVersion(id, newVersion, content, "AI 重新生成");
            log.info("流式重新生成完成, id={}", id);
        });
    }

    private List<dev.langchain4j.data.message.ChatMessage> buildLessonPlanMessages(LessonPlanGenerateRequest request) {
        String subject = request.getSubject();
        String grade = request.getGrade();
        String topic = request.getTopic();
        String objectives = request.getObjectives() != null ? request.getObjectives() : "根据课程标准自动确定";
        int duration = request.getDuration() != null ? request.getDuration() : 45;

        String systemMsg = "你是一位经验丰富的教学设计专家，擅长根据课程标准和教学理论设计高质量的教案。\n"
                + "你的教案必须满足以下要求：\n"
                + "1. 结构完整、目标明确、重难点突出\n"
                + "2. 教案中必须包含至少3个互动环节（如小组讨论、角色扮演、案例分析、课堂抢答、思维导图等）\n"
                + "3. 每个互动环节需标注预计时间和活动目标\n"
                + "4. 注重学生活动和互动，体现以学生为中心的教学理念\n"
                + "如果提供了参考资料，请结合参考资料进行教案设计。\n"
                + "请使用中文输出，使用 Markdown 格式。";

        String userMsg = "请为以下课程生成一份详细的教案：\n"
                + "学科：" + subject + "\n"
                + "年级：" + grade + "\n"
                + "课题：" + topic + "\n"
                + "教学目标：" + objectives + "\n"
                + "课时：" + duration + "分钟\n\n"
                + "请按照以下结构输出：\n"
                + "# 教案：" + topic + "\n\n"
                + "## 一、教学目标\n### 1. 知识与技能目标\n### 2. 过程与方法目标\n### 3. 情感态度与价值观目标\n\n"
                + "## 二、教学重点\n\n## 三、教学难点\n\n"
                + "## 四、教学过程\n### 1. 导入环节（约5分钟）\n### 2. 新授环节\n### 3. 练习巩固\n### 4. 课堂小结\n### 5. 作业布置\n\n"
                + "## 五、互动环节设计（至少3个）\n### 互动环节1: [名称]（约X分钟）\n### 互动环节2: [名称]（约X分钟）\n### 互动环节3: [名称]（约X分钟）\n\n"
                + "## 六、课堂评估方案\n\n## 七、板书设计\n\n## 八、教学资源建议";

        return List.of(new SystemMessage(systemMsg), new UserMessage(userMsg));
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
