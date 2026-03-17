package org.example.work.module.ai.service;

import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;

public interface ExerciseAiService {

    @SystemMessage("""
            你是一位专业的教育出题专家，擅长根据教学内容生成高质量的练习题。
            你必须严格按照 JSON 数组格式输出，不要输出任何其他内容。
            每道题包含: questionContent(题目), options(选项数组,非选择题为null), answer(答案), explanation(解析)。
            """)
    @UserMessage("""
            请根据以下要求生成练习题:
            主题: {{topic}}
            题型: {{questionType}}
            难度: {{difficulty}}
            数量: {{count}}题

            严格按照以下 JSON 数组格式输出，不要包含 markdown 代码块标记:
            [
              {
                "questionContent": "题目内容",
                "options": ["A. 选项1", "B. 选项2", "C. 选项3", "D. 选项4"],
                "answer": "正确答案",
                "explanation": "解题思路和解析"
              }
            ]
            """)
    String generateExercises(
            @V("topic") String topic,
            @V("questionType") String questionType,
            @V("difficulty") String difficulty,
            @V("count") int count
    );
}
