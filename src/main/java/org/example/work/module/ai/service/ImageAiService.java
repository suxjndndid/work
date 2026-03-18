package org.example.work.module.ai.service;

import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;

public interface ImageAiService {

    @SystemMessage("""
            你是一位教育多媒体专家，擅长从教案内容中提取用于制作教学图片、示意图和流程图的关键词。
            你只输出纯JSON，不输出任何其他文字。不要输出markdown代码块标记。
            直接输出JSON数组，以 [ 开头，以 ] 结尾。
            """)
    @UserMessage("""
            请从以下教案内容中提取5-8个用于制作教学示意图或流程图的关键词。
            每个关键词应该是一个具体的教学概念或场景，适合生成教学插图。

            教案内容:
            {{content}}

            直接输出JSON数组（不要```标记），格式:
            [{"keyword":"具体关键词","description":"图片用途说明"}]

            现在直接输出JSON：
            """)
    String extractImageKeywords(@V("content") String content);

    @SystemMessage("""
            你是一位专业的教学图表设计师，擅长用Mermaid语法绘制教学流程图、示意图、思维导图。
            你只输出纯JSON，不输出任何其他文字。不要输出markdown代码块标记。
            """)
    @UserMessage("""
            请为以下教学关键词生成一个Mermaid图表。
            根据关键词内容选择最合适的图表类型（flowchart流程图、mindmap思维导图、classDiagram类图、sequenceDiagram时序图等）。

            关键词：{{keyword}}

            直接输出JSON对象（不要```标记），格式:
            {"title":"图表标题","type":"图表类型","mermaid":"mermaid语法代码","description":"图表说明"}

            注意mermaid语法必须正确可渲染。现在直接输出JSON：
            """)
    String generateDiagram(@V("keyword") String keyword);

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
