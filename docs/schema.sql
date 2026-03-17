-- 智能教案生成与学情分析系统 - 数据库初始化脚本

CREATE DATABASE IF NOT EXISTS lesson_plan_db DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
USE lesson_plan_db;

-- 系统用户表
CREATE TABLE IF NOT EXISTS sys_user (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(50) NOT NULL UNIQUE COMMENT '用户名',
    password VARCHAR(100) NOT NULL COMMENT '密码(BCrypt加密)',
    real_name VARCHAR(50) COMMENT '真实姓名',
    role TINYINT NOT NULL DEFAULT 1 COMMENT '角色: 1=教师, 2=管理员',
    avatar VARCHAR(255) COMMENT '头像URL',
    email VARCHAR(100) COMMENT '邮箱',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '状态: 0=禁用, 1=启用',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除',
    INDEX idx_username (username)
) COMMENT='系统用户表';

-- 课程表
CREATE TABLE IF NOT EXISTS course (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL COMMENT '创建教师ID',
    name VARCHAR(100) NOT NULL COMMENT '课程名称',
    subject VARCHAR(50) NOT NULL COMMENT '学科',
    grade VARCHAR(30) NOT NULL COMMENT '年级',
    semester VARCHAR(20) COMMENT '学期',
    description TEXT COMMENT '课程描述',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT NOT NULL DEFAULT 0,
    INDEX idx_user_id (user_id)
) COMMENT='课程表';

-- 教案表
CREATE TABLE IF NOT EXISTS lesson_plan (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL COMMENT '教师ID',
    course_id BIGINT COMMENT '关联课程ID',
    title VARCHAR(200) NOT NULL COMMENT '教案标题',
    subject VARCHAR(50) NOT NULL COMMENT '学科',
    grade VARCHAR(30) NOT NULL COMMENT '年级',
    topic VARCHAR(200) NOT NULL COMMENT '课题',
    objectives TEXT COMMENT '教学目标(输入)',
    duration INT COMMENT '课时(分钟)',
    content LONGTEXT COMMENT '教案内容(AI生成)',
    teaching_goals TEXT COMMENT '教学目标(结构化)',
    key_points TEXT COMMENT '教学重点',
    difficult_points TEXT COMMENT '教学难点',
    teaching_process TEXT COMMENT '教学过程',
    activities TEXT COMMENT '教学活动',
    assessment TEXT COMMENT '评估方案',
    status TINYINT NOT NULL DEFAULT 0 COMMENT '0=草稿, 1=已发布',
    version INT NOT NULL DEFAULT 1 COMMENT '当前版本号',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT NOT NULL DEFAULT 0,
    INDEX idx_user_id (user_id),
    INDEX idx_course_id (course_id)
) COMMENT='教案表';

-- 教案版本历史表
CREATE TABLE IF NOT EXISTS lesson_plan_version (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    lesson_plan_id BIGINT NOT NULL COMMENT '教案ID',
    version_number INT NOT NULL COMMENT '版本号',
    content LONGTEXT NOT NULL COMMENT '该版本教案内容',
    change_note VARCHAR(500) COMMENT '变更说明',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_lesson_plan_id (lesson_plan_id)
) COMMENT='教案版本历史表';

-- 学生表
CREATE TABLE IF NOT EXISTS student (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    student_no VARCHAR(30) NOT NULL UNIQUE COMMENT '学号',
    name VARCHAR(50) NOT NULL COMMENT '姓名',
    gender TINYINT COMMENT '性别: 0=女, 1=男',
    class_name VARCHAR(50) COMMENT '班级',
    grade VARCHAR(30) COMMENT '年级',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT NOT NULL DEFAULT 0,
    INDEX idx_class (class_name)
) COMMENT='学生表';

-- 知识点表
CREATE TABLE IF NOT EXISTS knowledge_point (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    course_id BIGINT NOT NULL COMMENT '所属课程ID',
    name VARCHAR(100) NOT NULL COMMENT '知识点名称',
    parent_id BIGINT DEFAULT 0 COMMENT '父知识点ID(树形结构)',
    sort_order INT DEFAULT 0 COMMENT '排序',
    description TEXT COMMENT '描述',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_course_id (course_id),
    INDEX idx_parent_id (parent_id)
) COMMENT='知识点表';

-- 成绩表
CREATE TABLE IF NOT EXISTS score (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    student_id BIGINT NOT NULL COMMENT '学生ID',
    course_id BIGINT NOT NULL COMMENT '课程ID',
    exam_name VARCHAR(100) COMMENT '考试名称',
    exam_type TINYINT COMMENT '考试类型: 1=随堂测验, 2=期中, 3=期末',
    total_score DECIMAL(5,1) DEFAULT 100.0 COMMENT '总分',
    score DECIMAL(5,1) NOT NULL COMMENT '得分',
    exam_date DATE COMMENT '考试日期',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_student_id (student_id),
    INDEX idx_course_id (course_id)
) COMMENT='成绩表';

-- 知识点掌握度表
CREATE TABLE IF NOT EXISTS knowledge_point_mastery (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    student_id BIGINT NOT NULL COMMENT '学生ID',
    knowledge_point_id BIGINT NOT NULL COMMENT '知识点ID',
    mastery_level DECIMAL(5,2) COMMENT '掌握程度(0-100百分比)',
    assessment_count INT DEFAULT 0 COMMENT '评估次数',
    last_assessment_date DATE COMMENT '最近评估日期',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE INDEX uk_student_kp (student_id, knowledge_point_id)
) COMMENT='知识点掌握度表';

-- 练习题表
CREATE TABLE IF NOT EXISTS exercise (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    lesson_plan_id BIGINT COMMENT '关联教案ID',
    course_id BIGINT COMMENT '关联课程ID',
    topic VARCHAR(200) COMMENT '题目所属主题',
    question_type VARCHAR(30) NOT NULL COMMENT '题型: 选择题/填空题/判断题/简答题',
    difficulty VARCHAR(20) NOT NULL COMMENT '难度: 简单/中等/困难',
    question_content TEXT NOT NULL COMMENT '题目内容',
    options TEXT COMMENT '选项(JSON数组, 选择题用)',
    answer TEXT NOT NULL COMMENT '答案',
    explanation TEXT COMMENT '解析',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT NOT NULL DEFAULT 0,
    INDEX idx_lesson_plan_id (lesson_plan_id),
    INDEX idx_course_id (course_id)
) COMMENT='练习题表';
