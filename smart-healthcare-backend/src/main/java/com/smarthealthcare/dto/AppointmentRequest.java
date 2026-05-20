package com.smarthealthcare.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AppointmentRequest {

    @NotNull(message = "排班ID不能为空")
    private Long scheduleId;

    @NotNull(message = "患者ID不能为空")
    private Long patientId;
}
