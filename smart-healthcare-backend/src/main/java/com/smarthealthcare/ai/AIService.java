package com.smarthealthcare.ai;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.smarthealthcare.common.BusinessException;
import com.smarthealthcare.dto.AITriageResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Map;

/**
 * AI服务 - 智能导诊与病历生成
 * 支持 OpenAI / 智谱GLM / 文心一言 等大模型
 */
@Slf4j
@Service
public class AIService {

    @Value("${ai.llm.api-key}")
    private String apiKey;

    @Value("${ai.llm.api-url}")
    private String apiUrl;

    @Value("${ai.llm.model}")
    private String model;

    private static final String TRIAGE_SYSTEM_PROMPT = """
            你是一个专业、严谨的医院分诊机器人。患者会向你描述症状。
            请你根据症状，推荐最合适的1-2个就诊科室，并给出简短的就诊建议。
            你的输出必须是一个严格的JSON格式，不要包含任何其他多余的解释文字。
            格式如下：
            { "departments": ["推荐科室名称1", "推荐科室名称2"], "advice": "你的就诊建议" }
            可推荐的科室包括：内科、神经内科、心血管内科、外科、骨科、儿科、妇产科、耳鼻喉科、眼科、急诊科。
            """;

    private static final String MEDICAL_RECORD_PROMPT = """
            你是一个专业的医疗记录助手。请将以下医生口述或碎片化文字转换为标准的SOAP格式病历。
            请提取以下四个部分的内容：
            1. S (Subjective - 主观资料): 患者主诉及现病史
            2. O (Objective - 客观资料): 查体结果
            3. A (Assessment - 评估): 初步诊断
            4. P (Plan - 计划): 治疗方案或检查计划
            
            输出格式必须为严格JSON：
            {
                "chiefComplaint": "主诉",
                "presentIllness": "现病史",
                "physicalExamination": "查体结果",
                "diagnosis": "初步诊断",
                "treatmentPlan": "治疗方案"
            }
            """;

    /**
     * AI智能导诊 - 同步返回
     */
    public AITriageResponse triage(String symptomDescription) {
        String userMessage = "患者症状描述：" + symptomDescription + "\n请根据以上症状推荐科室。";

        String response = callLLM(TRIAGE_SYSTEM_PROMPT, userMessage);
        // 提取纯净JSON（去除markdown代码块等包裹）
        String cleanJson = extractJson(response);
        try {
            JSONObject json = JSON.parseObject(cleanJson);
            AITriageResponse result = new AITriageResponse();
            result.setDepartments(json.getList("departments", String.class));
            result.setAdvice(json.getString("advice"));
            return result;
        } catch (Exception e) {
            log.error("AI导诊响应解析失败: {}", response, e);
            // 降级方案：返回默认建议
            AITriageResponse fallback = new AITriageResponse();
            fallback.setDepartments(List.of("内科", "急诊科"));
            fallback.setAdvice("建议您前往医院进行详细检查，医生会根据您的具体情况给出专业诊断。");
            return fallback;
        }
    }

    /**
     * AI智能导诊 - SSE流式返回
     */
    public SseEmitter triageStream(String symptomDescription) {
        SseEmitter emitter = new SseEmitter(30000L); // 30秒超时

        String userMessage = "患者症状描述：" + symptomDescription + "\n请根据以上症状推荐科室。";

        // 在线程池中异步执行
        new Thread(() -> {
            try {
                String response = callLLM(TRIAGE_SYSTEM_PROMPT, userMessage);
                // 提取纯净JSON后再流式发送，确保前端能正确解析
                String cleanJson = extractJson(response);
                for (int i = 0; i < cleanJson.length(); i += 5) {
                    String chunk = cleanJson.substring(i, Math.min(i + 5, cleanJson.length()));
                    emitter.send(SseEmitter.event().data(chunk));
                    Thread.sleep(30);
                }
                emitter.send(SseEmitter.event().data("[DONE]"));
                emitter.complete();
            } catch (Exception e) {
                log.error("AI导诊流式输出异常", e);
                try {
                    emitter.send(SseEmitter.event().data("{\"departments\":[\"内科\",\"急诊科\"],\"advice\":\"服务暂时不可用，请稍后重试\"}"));
                    emitter.send(SseEmitter.event().data("[DONE]"));
                    emitter.complete();
                } catch (IOException ex) {
                    emitter.completeWithError(ex);
                }
            }
        }).start();

        return emitter;
    }

    /**
     * AI辅助生成结构化电子病历
     */
    public Map<String, String> generateMedicalRecord(String doctorNotes) {
        String response = callLLM(MEDICAL_RECORD_PROMPT, doctorNotes);
        // 提取纯净JSON（去除markdown代码块等包裹）
        String cleanJson = extractJson(response);
        try {
            JSONObject json = JSON.parseObject(cleanJson);
            return Map.of(
                    "chiefComplaint", json.getString("chiefComplaint") != null ? json.getString("chiefComplaint") : "",
                    "presentIllness", json.getString("presentIllness") != null ? json.getString("presentIllness") : "",
                    "physicalExamination", json.getString("physicalExamination") != null ? json.getString("physicalExamination") : "",
                    "diagnosis", json.getString("diagnosis") != null ? json.getString("diagnosis") : "",
                    "treatmentPlan", json.getString("treatmentPlan") != null ? json.getString("treatmentPlan") : ""
            );
        } catch (Exception e) {
            log.error("AI病历生成解析失败: {}", response, e);
            throw new BusinessException("AI病历生成失败，请手动填写");
        }
    }

    /**
     * 调用大模型API
     */
    private String callLLM(String systemPrompt, String userMessage) {
        // 如果API密钥未配置，返回模拟数据
        if (apiKey == null || apiKey.isBlank() || "your-api-key-here".equals(apiKey)) {
            log.warn("AI API密钥未配置，返回模拟数据");
            return simulateResponse(userMessage);
        }

        try {
            Map<String, Object> requestBody = Map.of(
                    "model", model,
                    "messages", List.of(
                            Map.of("role", "system", "content", systemPrompt),
                            Map.of("role", "user", "content", userMessage)
                    ),
                    "temperature", 0.3,
                    "max_tokens", 1000
            );

            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(apiUrl))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + apiKey)
                    .POST(HttpRequest.BodyPublishers.ofString(JSON.toJSONString(requestBody)))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            JSONObject json = JSON.parseObject(response.body());
            return json.getJSONArray("choices")
                    .getJSONObject(0)
                    .getJSONObject("message")
                    .getString("content");
        } catch (Exception e) {
            log.error("AI API调用失败", e);
            return simulateResponse(userMessage);
        }
    }

    /**
     * 从LLM响应中提取纯净JSON
     * 处理常见的LLM输出格式问题：
     * - markdown代码块包裹 (```json ... ```)
     * - 前后多余的文字说明
     * - 中文引号/转义问题
     */
    private String extractJson(String raw) {
        if (raw == null || raw.isBlank()) {
            return "{}";
        }
        String content = raw.trim();

        // 1. 去除markdown代码块标记 ```json ... ``` 或 ``` ... ```
        if (content.startsWith("```")) {
            int firstNewline = content.indexOf('\n');
            if (firstNewline > 0) {
                String afterOpen = content.substring(firstNewline + 1);
                int lastBacktick = afterOpen.lastIndexOf("```");
                if (lastBacktick > 0) {
                    content = afterOpen.substring(0, lastBacktick).trim();
                } else {
                    content = afterOpen.trim();
                }
            }
        }

        // 2. 尝试找到第一个 { 和最后一个 } 提取JSON对象
        int firstBrace = content.indexOf('{');
        int lastBrace = content.lastIndexOf('}');
        if (firstBrace >= 0 && lastBrace > firstBrace) {
            content = content.substring(firstBrace, lastBrace + 1);
        }

        // 3. 清理常见的格式问题：中文引号替换为转义
        content = content.replace('\u201C', '"').replace('\u201D', '"'); // " "
        content = content.replace('\u2018', '\'').replace('\u2019', '\''); // ' '

        return content.trim();
    }

    /**
     * 模拟AI响应（用于无API密钥时的演示）
     */
    private String simulateResponse(String userMessage) {
        String lower = userMessage.toLowerCase();

        if (lower.contains("头") && (lower.contains("晕") || lower.contains("痛"))) {
            if (lower.contains("恶心") || lower.contains("耳鸣")) {
                return "{\"departments\":[\"神经内科\",\"耳鼻喉科\"],\"advice\":\"您的症状可能与神经系统或内耳问题相关，建议优先挂神经内科排查，如排除神经问题后可就诊耳鼻喉科。\"}";
            }
            return "{\"departments\":[\"神经内科\",\"内科\"],\"advice\":\"头痛可能由多种原因引起，建议就诊神经内科进行详细检查。\"}";
        }
        if (lower.contains("拉肚子") || lower.contains("腹泻") || lower.contains("肚子")) {
            return "{\"departments\":[\"内科\",\"急诊科\"],\"advice\":\"腹痛腹泻症状建议就诊内科，如伴有发热或脱水请及时前往急诊科。\"}";
        }
        if (lower.contains("咳") || lower.contains("发烧") || lower.contains("感冒")) {
            return "{\"departments\":[\"内科\",\"儿科\"],\"advice\":\"呼吸道症状建议就诊内科（成人）或儿科（儿童），请根据年龄选择。\"}";
        }
        if (lower.contains("骨") || lower.contains("关节") || lower.contains("扭伤")) {
            return "{\"departments\":[\"骨科\",\"外科\"],\"advice\":\"骨骼关节问题建议就诊骨科，如为急性外伤可前往急诊外科。\"}";
        }
        if (lower.contains("眼") || lower.contains("视力")) {
            return "{\"departments\":[\"眼科\"],\"advice\":\"眼部问题建议就诊眼科进行专业检查。\"}";
        }
        if (lower.contains("妇") || lower.contains("月经") || lower.contains("孕")) {
            return "{\"departments\":[\"妇产科\"],\"advice\":\"妇科相关问题建议就诊妇产科。\"}";
        }

        // 默认返回
        return "{\"departments\":[\"内科\",\"急诊科\"],\"advice\":\"根据您的症状描述，建议优先就诊内科进行初步诊断。如有急症请直接前往急诊科。\"}";
    }
}
