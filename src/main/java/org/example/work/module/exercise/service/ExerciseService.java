package org.example.work.module.exercise.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.example.work.module.exercise.dto.ExerciseGenerateRequest;
import org.example.work.module.exercise.entity.Exercise;

import java.util.List;

public interface ExerciseService {

    /** AI 生成习题 */
    List<Exercise> generate(ExerciseGenerateRequest request);

    /** 分页查询 */
    Page<Exercise> page(int current, int size, Long lessonPlanId, Long courseId);

    /** 更新 */
    void update(Exercise exercise);

    /** 删除 */
    void delete(Long id);
}
