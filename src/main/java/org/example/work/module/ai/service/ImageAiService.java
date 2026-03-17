package org.example.work.module.ai.service;

import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;

public interface ImageAiService {

    @SystemMessage("""
            你是一位教育多媒体专家，擅长从教案内容中提取用于制作教学图片、示意图和流程图的关键词。
            请输出 JSON 数组格式，每个元素包含 keyword(关键词) 和 description(用途描述)。
            """)
    @UserMessage("""
            请从以下教案内容中提取制作教学示意图或流程图所需的关键词:

            {{content}}

            输出 JSON 数组格式，不要包含 markdown 代码块标记:
            [
              {"keyword": "关键词", "description": "该图片的用途说明"}
            ]
            """)
    String extractImageKeywords(@V("content") String content);

    @SystemMessage("""
            你是一位教育资源推荐专家，擅长根据学生的学情分析结果推荐个性化的学习资源。
            请使用中文输出，使用 Markdown 格式。
            """)
    @UserMessage("""
            根据以下学情分析数据，为该学生推荐个性化的预习或复习资源:
            {{studentData}}

            请提供:
            1. 需要重点复习的知识点及推荐资源
            2. 推荐的预习内容
            3. 推荐的练习方向和题型
            4. 学习方法建议
            """)
    String recommendResources(@V("studentData") String studentData);
}
