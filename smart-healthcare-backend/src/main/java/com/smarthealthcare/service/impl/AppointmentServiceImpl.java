package com.smarthealthcare.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.smarthealthcare.common.BusinessException;
import com.smarthealthcare.entity.BizAppointment;
import com.smarthealthcare.entity.BizSchedule;
import com.smarthealthcare.mapper.BizAppointmentMapper;
import com.smarthealthcare.mapper.BizScheduleMapper;
import com.smarthealthcare.service.AppointmentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class AppointmentServiceImpl implements AppointmentService {

    private final BizScheduleMapper scheduleMapper;
    private final BizAppointmentMapper appointmentMapper;
    private final RedisTemplate<String, String> redisTemplate;

    private static final String LOCK_KEY_PREFIX = "schedule_lock:";

    @Override
    @Transactional
    public BizAppointment book(Long patientId, Long scheduleId) {
        // 查询排班信息
        BizSchedule schedule = scheduleMapper.selectById(scheduleId);
        if (schedule == null || schedule.getStatus() == 0) {
            throw new BusinessException("该排班不存在或已停诊");
        }
        if (schedule.getLeftQuota() <= 0) {
            throw new BusinessException("该时段号源已满，请选择其他时段");
        }

        // 校验预约规则: 同一患者同一天同一科室最多预约1次
        int count = appointmentMapper.countByPatientDeptDate(
                patientId, schedule.getDeptId(), schedule.getScheduleDate().toString());
        if (count >= 1) {
            throw new BusinessException("您当天在该科室已有预约，请勿重复预约");
        }

        // 使用乐观锁扣减号源
        int deducted = scheduleMapper.deductQuota(scheduleId, schedule.getVersion());
        if (deducted <= 0) {
            throw new BusinessException("号源扣减失败，可能已被他人抢先预约，请刷新重试");
        }

        // 生成排号
        LambdaQueryWrapper<BizAppointment> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BizAppointment::getScheduleId, scheduleId);
        long queueCount = appointmentMapper.selectCount(wrapper);

        // 创建挂号记录
        BizAppointment appointment = new BizAppointment();
        appointment.setPatientId(patientId);
        appointment.setScheduleId(scheduleId);
        appointment.setDoctorId(schedule.getDoctorId());
        appointment.setDeptId(schedule.getDeptId());
        appointment.setScheduleDate(schedule.getScheduleDate());
        appointment.setTimeSlot(schedule.getTimeSlot());
        appointment.setQueueNumber((int) queueCount + 1);
        appointment.setStatus(1); // 已预约
        appointment.setFee(schedule.getFee());

        appointmentMapper.insert(appointment);

        log.info("患者[{}]成功预约排班[{}], 排号[{}]", patientId, scheduleId, appointment.getQueueNumber());
        return appointment;
    }

    @Override
    @Transactional
    public void cancel(Long appointmentId, Long patientId) {
        BizAppointment appointment = appointmentMapper.selectById(appointmentId);
        if (appointment == null || !appointment.getPatientId().equals(patientId)) {
            throw new BusinessException("预约记录不存在");
        }
        // 校验取消时限（距就诊2小时内不可取消）
        // 简化处理：直接标记已取消，并恢复号源
        appointment.setStatus(4);
        appointment.setCancelTime(LocalDateTime.now());
        appointmentMapper.updateById(appointment);

        // 恢复号源
        BizSchedule schedule = scheduleMapper.selectById(appointment.getScheduleId());
        if (schedule != null) {
            schedule.setLeftQuota(schedule.getLeftQuota() + 1);
            scheduleMapper.updateById(schedule);
        }
    }

    @Override
    public List<BizAppointment> listByPatient(Long patientId) {
        return appointmentMapper.selectByPatientId(patientId);
    }

    @Override
    public List<BizAppointment> listTodayByDoctor(Long doctorId) {
        return appointmentMapper.selectTodayByDoctorId(doctorId);
    }

    @Override
    public void updateStatus(Long appointmentId, Integer status) {
        BizAppointment appointment = appointmentMapper.selectById(appointmentId);
        if (appointment != null) {
            appointment.setStatus(status);
            appointmentMapper.updateById(appointment);
        }
    }
}
