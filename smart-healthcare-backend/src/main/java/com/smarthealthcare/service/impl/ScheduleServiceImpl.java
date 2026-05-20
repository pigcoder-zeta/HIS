package com.smarthealthcare.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.smarthealthcare.common.BusinessException;
import com.smarthealthcare.dto.ScheduleDTO;
import com.smarthealthcare.entity.BizSchedule;
import com.smarthealthcare.mapper.BizScheduleMapper;
import com.smarthealthcare.service.ScheduleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ScheduleServiceImpl implements ScheduleService {

    private final BizScheduleMapper scheduleMapper;

    @Override
    public List<BizSchedule> listByDeptAndWeek(Long deptId, LocalDate startDate, LocalDate endDate) {
        return scheduleMapper.selectByDeptAndDateRange(deptId,
                startDate.toString(), endDate.toString());
    }

    @Override
    @Transactional
    public void createSchedule(ScheduleDTO dto) {
        // 检查是否存在冲突排班
        LambdaQueryWrapper<BizSchedule> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BizSchedule::getDoctorId, dto.getDoctorId())
                .eq(BizSchedule::getScheduleDate, dto.getScheduleDate())
                .eq(BizSchedule::getTimeSlot, dto.getTimeSlot())
                .eq(BizSchedule::getStatus, 1);
        if (scheduleMapper.selectCount(wrapper) > 0) {
            throw new BusinessException("该医生在当前时段已有排班，不可重复安排");
        }

        BizSchedule schedule = new BizSchedule();
        schedule.setDoctorId(dto.getDoctorId());
        schedule.setDeptId(dto.getDeptId());
        schedule.setScheduleDate(dto.getScheduleDate());
        schedule.setTimeSlot(dto.getTimeSlot());
        schedule.setTotalQuota(dto.getTotalQuota());
        schedule.setLeftQuota(dto.getTotalQuota());
        schedule.setStatus(1);
        scheduleMapper.insert(schedule);
    }

    @Override
    @Transactional
    public void batchCreate(List<ScheduleDTO> schedules) {
        // 智能排班：批量创建，跳过冲突
        int successCount = 0;
        for (ScheduleDTO dto : schedules) {
            try {
                createSchedule(dto);
                successCount++;
            } catch (BusinessException e) {
                log.warn("排班冲突跳过: doctorId={}, date={}, slot={}, reason={}",
                        dto.getDoctorId(), dto.getScheduleDate(), dto.getTimeSlot(), e.getMessage());
            }
        }
        log.info("批量排班完成: 成功{}条, 跳过{}条", successCount, schedules.size() - successCount);
    }

    @Override
    @Transactional
    public void cancelSchedule(Long scheduleId) {
        BizSchedule schedule = scheduleMapper.selectById(scheduleId);
        if (schedule == null) {
            throw new BusinessException("排班不存在");
        }
        schedule.setStatus(0);
        scheduleMapper.updateById(schedule);
    }

    @Override
    public BizSchedule getById(Long id) {
        return scheduleMapper.selectById(id);
    }
}
