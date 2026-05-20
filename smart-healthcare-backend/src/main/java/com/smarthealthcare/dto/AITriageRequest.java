package com.smarthealthcare.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AITriageRequest {

    @NotBlank(message = "症状描述不能为空")
    private String symptomDescription;
}
