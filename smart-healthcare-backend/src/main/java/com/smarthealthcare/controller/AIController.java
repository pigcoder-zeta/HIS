package com.smarthealthcare.controller;

import com.smarthealthcare.ai.AIService;
import com.smarthealthcare.common.Result;
import com.smarthealthcare.dto.AITriageRequest;
import com.smarthealthcare.dto.AITriageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.Map;

@Tag(name = "AI服务接口", description = "智能导诊、AI病历生成")
@RestController
@RequestMapping("/api/v1/ai")
@RequiredArgsConstructor
public class AIController {

    private final AIService aiService;

    @Operation(summary = "AI智能导诊（同步）")
    @PostMapping("/triage")
    public Result<AITriageResponse> triage(@Valid @RequestBody AITriageRequest request) {
        return Result.success(aiService.triage(request.getSymptomDescription()));
    }

    @Operation(summary = "AI智能导诊（SSE流式）")
    @PostMapping(value = "/triage/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter triageStream(@Valid @RequestBody AITriageRequest request) {
        return aiService.triageStream(request.getSymptomDescription());
    }

    @Operation(summary = "AI辅助生成结构化病历")
    @PostMapping("/medical-record/generate")
    public Result<Map<String, String>> generateMedicalRecord(@RequestBody Map<String, String> body) {
        String doctorNotes = body.getOrDefault("notes", "");
        return Result.success(aiService.generateMedicalRecord(doctorNotes));
    }
}
