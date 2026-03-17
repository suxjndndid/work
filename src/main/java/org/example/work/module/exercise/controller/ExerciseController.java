package org.example.work.module.exercise.controller;

import jakarta.validation.Valid;
import org.example.work.common.Result;
import org.example.work.module.exercise.dto.ExerciseGenerateRequest;
import org.example.work.module.exercise.entity.Exercise;
import org.example.work.module.exercise.service.ExerciseService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/exercise")
public class ExerciseController {

    private final ExerciseService exerciseService;

    public ExerciseController(ExerciseService exerciseService) {
        this.exerciseService = exerciseService;
    }

    @GetMapping("/list")
    public Result<?> list(@RequestParam(defaultValue = "1") int current,
                          @RequestParam(defaultValue = "10") int size,
                          @RequestParam(required = false) Long lessonPlanId,
                          @RequestParam(required = false) Long courseId) {
        return Result.ok(exerciseService.page(current, size, lessonPlanId, courseId));
    }

    @PostMapping("/generate")
    public Result<List<Exercise>> generate(@Valid @RequestBody ExerciseGenerateRequest request) {
        return Result.ok(exerciseService.generate(request));
    }

    @PutMapping("/{id}")
    public Result<?> update(@PathVariable Long id, @RequestBody Exercise exercise) {
        exercise.setId(id);
        exerciseService.update(exercise);
        return Result.ok();
    }

    @DeleteMapping("/{id}")
    public Result<?> delete(@PathVariable Long id) {
        exerciseService.delete(id);
        return Result.ok();
    }
}
