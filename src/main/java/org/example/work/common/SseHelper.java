package org.example.work.common;

import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.model.chat.response.StreamingChatResponseHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;

/**
 * SSE 流式 AI 调用工具类
 * 注意：token 通过 JSON 编码后发送，确保换行符等特殊字符不被 SSE 协议吞掉
 */
public class SseHelper {

    private static final Logger log = LoggerFactory.getLogger(SseHelper.class);
    private static final long SSE_TIMEOUT = 300_000L; // 300秒
    private static final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 创建一个配置好超时的 SseEmitter
     */
    public static SseEmitter createEmitter() {
        SseEmitter emitter = new SseEmitter(SSE_TIMEOUT);
        emitter.onTimeout(() -> log.warn("[SSE] 连接超时"));
        emitter.onCompletion(() -> log.debug("[SSE] 连接完成"));
        emitter.onError(e -> log.error("[SSE] 连接异常", e));
        return emitter;
    }

    /**
     * 使用 StreamingChatModel 进行流式调用，将结果通过 SseEmitter 推送
     *
     * @param streamingChatModel 流式模型
     * @param messages           聊天消息列表
     * @param emitter            SSE发射器
     */
    public static void streamChat(StreamingChatModel streamingChatModel,
                                   List<ChatMessage> messages,
                                   SseEmitter emitter) {
        streamChat(streamingChatModel, messages, emitter, null);
    }

    /**
     * 使用 StreamingChatModel 进行流式调用，将结果通过 SseEmitter 推送
     *
     * @param streamingChatModel 流式模型
     * @param messages           聊天消息列表
     * @param emitter            SSE发射器
     * @param onComplete         完成时的回调（可选），参数为完整的生成文本
     */
    public static void streamChat(StreamingChatModel streamingChatModel,
                                   List<ChatMessage> messages,
                                   SseEmitter emitter,
                                   java.util.function.Consumer<String> onComplete) {
        StringBuilder fullContent = new StringBuilder();

        streamingChatModel.chat(messages, new StreamingChatResponseHandler() {
            @Override
            public void onPartialResponse(String partialResponse) {
                try {
                    fullContent.append(partialResponse);
                    // JSON 编码：确保 \n 等特殊字符被转义为 \\n，不会破坏 SSE 协议
                    String jsonEncoded = objectMapper.writeValueAsString(partialResponse);
                    emitter.send(SseEmitter.event().data(jsonEncoded));
                } catch (IOException e) {
                    log.warn("[SSE] 发送数据失败，客户端可能已断开: {}", e.getMessage());
                }
            }

            @Override
            public void onCompleteResponse(ChatResponse completeResponse) {
                try {
                    emitter.send(SseEmitter.event().data("[DONE]"));
                    emitter.complete();
                    if (onComplete != null) {
                        onComplete.accept(fullContent.toString());
                    }
                    log.debug("[SSE] 流式生成完成，总长度: {}", fullContent.length());
                } catch (IOException e) {
                    log.warn("[SSE] 发送完成标记失败: {}", e.getMessage());
                }
            }

            @Override
            public void onError(Throwable error) {
                log.error("[SSE] AI生成异常", error);
                emitter.completeWithError(error);
            }
        });
    }
}
