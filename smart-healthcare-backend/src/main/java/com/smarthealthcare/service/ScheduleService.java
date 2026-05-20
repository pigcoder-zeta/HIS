package com.smarthealthcare.service;

import com.smarthealthcare.dto.ScheduleDTO;
import com.smarthealthcare.entity.BizSchedule;

import java.time.LocalDate;
import java.util.List;

public interface ScheduleService {

    List<BizSchedule> listByDeptAndWeek(Long deptId, LocalDate startDate, LocalDate endDate);

    void createSchedule(ScheduleDTO dto);

    void batchCreate(List<ScheduleDTO> schedules);

    void cancelSchedule(Long scheduleId);

    BizSchedule getById(Long id);
}
