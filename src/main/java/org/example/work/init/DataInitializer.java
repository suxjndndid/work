package org.example.work.init;

import org.example.work.module.ai.service.RagService;
import org.example.work.module.analytics.entity.*;
import org.example.work.module.analytics.mapper.*;
import org.example.work.module.course.entity.Course;
import org.example.work.module.course.mapper.CourseMapper;
import org.example.work.module.user.entity.User;
import org.example.work.module.user.mapper.UserMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.Random;

@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private final UserMapper userMapper;
    private final CourseMapper courseMapper;
    private final StudentMapper studentMapper;
    private final ScoreMapper scoreMapper;
    private final KnowledgePointMapper knowledgePointMapper;
    private final KnowledgePointMasteryMapper masteryMapper;
    private final RagService ragService;

    public DataInitializer(UserMapper userMapper, CourseMapper courseMapper,
                           StudentMapper studentMapper, ScoreMapper scoreMapper,
                           KnowledgePointMapper knowledgePointMapper,
                           KnowledgePointMasteryMapper masteryMapper,
                           RagService ragService) {
        this.userMapper = userMapper;
        this.courseMapper = courseMapper;
        this.studentMapper = studentMapper;
        this.scoreMapper = scoreMapper;
        this.knowledgePointMapper = knowledgePointMapper;
        this.masteryMapper = masteryMapper;
        this.ragService = ragService;
    }

    @Override
    public void run(String... args) {
        if (userMapper.selectCount(null) > 0) {
            log.info("数据库已有数据，跳过初始化");
            ragService.ingestDocuments();
            return;
        }

        log.info("开始初始化模拟数据...");
        Random random = new Random(42);

        // 1. 创建用户
        User admin = createUser("admin", "管理员", 2);
        User teacher1 = createUser("teacher1", "张老师", 1);
        User teacher2 = createUser("teacher2", "李老师", 1);

        // 2. 创建课程
        Course math = createCourse(teacher1.getId(), "高一数学", "数学", "高一", "2025-2026第一学期");
        Course chinese = createCourse(teacher1.getId(), "高一语文", "语文", "高一", "2025-2026第一学期");
        Course english = createCourse(teacher2.getId(), "高一英语", "英语", "高一", "2025-2026第一学期");

        // 3. 创建知识点
        Long[] mathKps = {
                createKnowledgePoint(math.getId(), "集合与常用逻辑用语", 0L, 1),
                createKnowledgePoint(math.getId(), "一元二次不等式", 0L, 2),
                createKnowledgePoint(math.getId(), "函数的概念与性质", 0L, 3),
                createKnowledgePoint(math.getId(), "指数函数与对数函数", 0L, 4),
                createKnowledgePoint(math.getId(), "三角函数", 0L, 5)
        };

        Long[] chineseKps = {
                createKnowledgePoint(chinese.getId(), "文言文阅读", 0L, 1),
                createKnowledgePoint(chinese.getId(), "现代文阅读", 0L, 2),
                createKnowledgePoint(chinese.getId(), "写作", 0L, 3),
                createKnowledgePoint(chinese.getId(), "古诗词鉴赏", 0L, 4)
        };

        Long[] englishKps = {
                createKnowledgePoint(english.getId(), "阅读理解", 0L, 1),
                createKnowledgePoint(english.getId(), "完形填空", 0L, 2),
                createKnowledgePoint(english.getId(), "语法填空", 0L, 3),
                createKnowledgePoint(english.getId(), "书面表达", 0L, 4)
        };

        // 4. 创建学生 (30人)
        String[] classNames = {"高一(1)班", "高一(2)班"};
        String[] firstNames = {"伟", "芳", "娜", "敏", "静", "强", "磊", "洋", "勇", "艳",
                "杰", "娟", "涛", "明", "超", "秀英", "华", "丽", "军", "平"};
        String[] lastNames = {"张", "王", "李", "赵", "刘", "陈", "杨", "黄", "周", "吴",
                "徐", "孙", "马", "朱", "胡"};

        Long[] studentIds = new Long[30];
        for (int i = 0; i < 30; i++) {
            Student student = new Student();
            student.setStudentNo(String.format("2025%04d", i + 1));
            student.setName(lastNames[random.nextInt(lastNames.length)] + firstNames[random.nextInt(firstNames.length)]);
            student.setGender(random.nextInt(2));
            student.setClassName(classNames[i < 15 ? 0 : 1]);
            student.setGrade("高一");
            studentMapper.insert(student);
            studentIds[i] = student.getId();
        }

        // 5. 生成成绩数据
        String[][] exams = {
                {"随堂测验1", "1"}, {"期中考试", "2"}, {"随堂测验2", "1"}, {"期末考试", "3"}
        };
        LocalDate[] examDates = {
                LocalDate.of(2025, 9, 20), LocalDate.of(2025, 11, 10),
                LocalDate.of(2025, 12, 5), LocalDate.of(2026, 1, 15)
        };

        Long[][] courseKps = {mathKps, chineseKps, englishKps};
        Long[] courseIds = {math.getId(), chinese.getId(), english.getId()};

        for (int c = 0; c < courseIds.length; c++) {
            for (int e = 0; e < exams.length; e++) {
                for (Long studentId : studentIds) {
                    // 生成正态分布成绩，均值 72，标准差 15
                    double rawScore = 72 + random.nextGaussian() * 15;
                    rawScore = Math.max(15, Math.min(100, rawScore));

                    Score score = new Score();
                    score.setStudentId(studentId);
                    score.setCourseId(courseIds[c]);
                    score.setExamName(exams[e][0]);
                    score.setExamType(Integer.parseInt(exams[e][1]));
                    score.setTotalScore(new BigDecimal("100"));
                    score.setScore(BigDecimal.valueOf(rawScore).setScale(1, RoundingMode.HALF_UP));
                    score.setExamDate(examDates[e]);
                    scoreMapper.insert(score);
                }
            }

            // 6. 生成知识点掌握度
            for (Long studentId : studentIds) {
                for (Long kpId : courseKps[c]) {
                    double mastery = 50 + random.nextGaussian() * 20;
                    mastery = Math.max(10, Math.min(100, mastery));

                    KnowledgePointMastery m = new KnowledgePointMastery();
                    m.setStudentId(studentId);
                    m.setKnowledgePointId(kpId);
                    m.setMasteryLevel(BigDecimal.valueOf(mastery).setScale(1, RoundingMode.HALF_UP));
                    m.setAssessmentCount(random.nextInt(5) + 1);
                    m.setLastAssessmentDate(examDates[random.nextInt(examDates.length)]);
                    masteryMapper.insert(m);
                }
            }
        }

        log.info("模拟数据初始化完成: 3用户, 3课程, 30学生, {}条成绩, {}条掌握度",
                30 * 4 * 3, 30 * (mathKps.length + chineseKps.length + englishKps.length));

        // 7. 加载 RAG 文档
        ragService.ingestDocuments();
    }

    private User createUser(String username, String realName, int role) {
        User user = new User();
        user.setUsername(username);
        user.setPassword(String.valueOf("123456".hashCode()));
        user.setRealName(realName);
        user.setRole(role);
        user.setStatus(1);
        userMapper.insert(user);
        return user;
    }

    private Course createCourse(Long userId, String name, String subject, String grade, String semester) {
        Course course = new Course();
        course.setUserId(userId);
        course.setName(name);
        course.setSubject(subject);
        course.setGrade(grade);
        course.setSemester(semester);
        courseMapper.insert(course);
        return course;
    }

    private Long createKnowledgePoint(Long courseId, String name, Long parentId, int sort) {
        KnowledgePoint kp = new KnowledgePoint();
        kp.setCourseId(courseId);
        kp.setName(name);
        kp.setParentId(parentId);
        kp.setSortOrder(sort);
        knowledgePointMapper.insert(kp);
        return kp.getId();
    }
}
