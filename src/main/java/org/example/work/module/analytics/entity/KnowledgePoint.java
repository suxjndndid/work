package org.example.work.module.analytics.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.example.work.common.BaseEntity;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("knowledge_point")
public class KnowledgePoint extends BaseEntity {

    private Long courseId;
    private String name;
    private Long parentId;
    private Integer sortOrder;
    private String description;
}
