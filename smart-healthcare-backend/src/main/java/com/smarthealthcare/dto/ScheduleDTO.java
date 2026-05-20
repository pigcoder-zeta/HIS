package com.smarthealthcare.dto;

import lombok.Data;
import java.time.LocalDate;

@Data
public class ScheduleDTO {

    private Long doctorId;
    private Long deptId;
    private LocalDate scheduleDate;
    private Integer timeSlot;
    private Integer totalQuota;
}
