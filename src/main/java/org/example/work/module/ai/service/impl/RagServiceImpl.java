package org.example.work.module.ai.service.impl;

import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.Metadata;
import dev.langchain4j.data.document.splitter.DocumentSplitters;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingStore;
import org.example.work.module.ai.service.RagService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Service
public class RagServiceImpl implements RagService {

    private static final Logger log = LoggerFactory.getLogger(RagServiceImpl.class);

    private final EmbeddingModel embeddingModel;
    private final EmbeddingStore<TextSegment> embeddingStore;

    public RagServiceImpl(
            @Autowired(required = false) EmbeddingModel embeddingModel,
            @Autowired(required = false) EmbeddingStore<TextSegment> embeddingStore) {
        this.embeddingModel = embeddingModel;
        this.embeddingStore = embeddingStore;
    }

    private boolean isRagEnabled() {
        return embeddingModel != null && embeddingStore != null;
    }

    @Override
    public void ingestDocuments() {
        if (!isRagEnabled()) {
            log.info("未配置向量模型，跳过 RAG 文档加载");
            return;
        }

        try {
            PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
            Resource[] resources = resolver.getResources("classpath:rag-documents/*.txt");

            for (Resource resource : resources) {
                String content = resource.getContentAsString(StandardCharsets.UTF_8);
                String fileName = resource.getFilename();
                log.info("加载 RAG 文档: {}", fileName);

                Document document = Document.from(content, Metadata.from("source", fileName));
                List<TextSegment> segments = DocumentSplitters.recursive(300, 30).split(document);

                for (TextSegment segment : segments) {
                    var embedding = embeddingModel.embed(segment.text()).content();
                    embeddingStore.add(embedding, segment);
                }
                log.info("文档 {} 已分割为 {} 个片段并嵌入", fileName, segments.size());
            }
        } catch (IOException e) {
            log.warn("加载 RAG 文档失败: {}", e.getMessage());
        }
    }

    @Override
    public void addDocument(String text, String source) {
        if (!isRagEnabled()) {
            log.warn("未配置向量模型，无法添加 RAG 文档");
            return;
        }

        Document document = Document.from(text, Metadata.from("source", source));
        List<TextSegment> segments = DocumentSplitters.recursive(300, 30).split(document);

        for (TextSegment segment : segments) {
            var embedding = embeddingModel.embed(segment.text()).content();
            embeddingStore.add(embedding, segment);
        }
        log.info("手动添加文档 [{}]，分割为 {} 个片段", source, segments.size());
    }
}
