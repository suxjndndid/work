package org.example.work.module.ai.service;

import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;

public interface ExerciseAiService {

    @SystemMessage("""
            你是一位专业的教育出题专家。你只输出纯JSON，不输出任何其他文字。
            不要输出markdown代码块标记，不要输出```，不要输出任何解释性文字。
            直接输出JSON数组，以 [ 开头，以 ] 结尾。
            """)
    @UserMessage("""
            生成{{count}}道{{difficulty}}难度的{{questionType}}，主题：{{topic}}

            直接输出JSON数组（不要```标记），格式如下：
            [{"questionContent":"题目","options":["A. x","B. x","C. x","D. x"],"answer":"A","explanation":"解析"}]

            非选择题options设为null。现在直接输出JSON：
            """)
    String generateExercises(
            @V("topic") String topic,
            @V("questionType") String questionType,
            @V("difficulty") String difficulty,
            @V("count") int count
    );
}
