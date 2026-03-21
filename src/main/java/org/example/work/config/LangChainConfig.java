package org.example.work.config;

import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.model.openai.OpenAiStreamingChatModel;
import dev.langchain4j.rag.content.retriever.EmbeddingStoreContentRetriever;
import dev.langchain4j.service.AiServices;
import org.example.work.module.ai.service.AnalyticsAiService;
import org.example.work.module.ai.service.ExerciseAiService;
import org.example.work.module.ai.service.ImageAiService;
import org.example.work.module.ai.service.LessonPlanAiService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Configuration
public class LangChainConfig {

    private static final Logger log = LoggerFactory.getLogger(LangChainConfig.class);

    @Value("${ai.openai.base-url}")
    private String baseUrl;

    @Value("${ai.openai.api-key}")
    private String apiKey;

    @Value("${ai.openai.chat-model-name}")
    private String chatModelName;

    @Value("${ai.openai.temperature}")
    private double temperature;

    @Value("${ai.openai.max-tokens}")
    private int maxTokens;

    @Bean
    public ChatModel chatModel() {
        log.info("AI配置: baseUrl={}, model={}, maxTokens={}", baseUrl, chatModelName, maxTokens);
        return OpenAiChatModel.builder()
                .baseUrl(baseUrl)
                .apiKey(apiKey)
                .modelName(chatModelName)
                .temperature(temperature)
                .maxTokens(maxTokens)
                .timeout(Duration.ofSeconds(300))
                .build();
    }

    @Bean
    public StreamingChatModel streamingChatModel() {
        log.info("流式AI配置: baseUrl={}, model={}", baseUrl, chatModelName);
        return OpenAiStreamingChatModel.builder()
                .baseUrl(baseUrl)
                .apiKey(apiKey)
                .modelName(chatModelName)
                .temperature(temperature)
                .maxTokens(maxTokens)
                .timeout(Duration.ofSeconds(300))
                .build();
    }

    @Bean
    public LessonPlanAiService lessonPlanAiService(
            ChatModel chatModel,
            @Autowired(required = false) EmbeddingStoreContentRetriever contentRetriever) {
        var builder = AiServices.builder(LessonPlanAiService.class)
                .chatModel(chatModel);
        if (contentRetriever != null) {
            builder.contentRetriever(contentRetriever);
            log.info("教案生成已启用 RAG 课标检索");
        } else {
            log.info("未配置向量模型，教案生成将不使用 RAG 课标检索");
        }
        return builder.build();
    }

    @Bean
    public AnalyticsAiService analyticsAiService(ChatModel chatModel) {
        return AiServices.builder(AnalyticsAiService.class)
                .chatModel(chatModel)
                .build();
    }

    @Bean
    public ExerciseAiService exerciseAiService(ChatModel chatModel) {
        return AiServices.builder(ExerciseAiService.class)
                .chatModel(chatModel)
                .build();
    }

    @Bean
    public ImageAiService imageAiService(ChatModel chatModel) {
        return AiServices.builder(ImageAiService.class)
                .chatModel(chatModel)
                .build();
    }
}
