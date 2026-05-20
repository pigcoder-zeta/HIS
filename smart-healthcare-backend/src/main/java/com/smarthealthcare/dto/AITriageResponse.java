package com.smarthealthcare.dto;

import lombok.Data;
import java.util.List;

@Data
public class AITriageResponse {

    private List<String> departments;
    private String advice;
}
