package org.example.work.module.ai.service;

public interface RagService {

    /** 加载 resources/rag-documents/ 下的所有文档到向量库 */
    void ingestDocuments();

    /** 手动添加文档到向量库 */
    void addDocument(String text, String source);
}
