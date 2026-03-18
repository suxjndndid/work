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
            你是一位专业的教学图表设计师，擅长用Mermaid语法绘制教学流程图。
            你只输出纯JSON，不输出任何其他文字。不要输出markdown代码块标记。
            """)
    @UserMessage("""
            以下是从教案中提取的教学关键词列表：
            {{keywords}}

            请根据这些关键词，分析它们之间的教学逻辑关系和推荐教学顺序，绘制一个完整的教学流程图。
            要求：
            1. 只画flowchart流程图（使用 flowchart TD 即从上到下方向）
            2. 将关键词按照合理的教学顺序排列
            3. 用箭头标注教学步骤之间的衔接关系（箭头上可加简短说明）
            4. 节点文字简洁明确，使用中文
            5. Mermaid语法必须正确可渲染，节点id只用英文字母和数字，显示文本用中括号包裹中文

            直接输出JSON对象（不要```标记），格式:
            {"title":"教学流程图标题","type":"flowchart","mermaid":"flowchart TD\\n  A[步骤1] -->|说明| B[步骤2]\\n  ...","description":"流程图说明"}

            现在直接输出JSON：
            """)
    String generateDiagram(@V("keywords") String keywords);

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
