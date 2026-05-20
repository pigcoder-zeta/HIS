package com.smarthealthcare.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.smarthealthcare.common.BusinessException;
import com.smarthealthcare.entity.BizExamAppointment;
import com.smarthealthcare.entity.BizExamPackage;
import com.smarthealthcare.mapper.BizExamAppointmentMapper;
import com.smarthealthcare.mapper.BizExamPackageMapper;
import com.smarthealthcare.service.ExamService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ExamServiceImpl implements ExamService {

    private final BizExamPackageMapper packageMapper;
    private final BizExamAppointmentMapper appointmentMapper;

    @Override
    public List<BizExamPackage> listPackages() {
        return packageMapper.selectList(
                new LambdaQueryWrapper<BizExamPackage>()
                        .eq(BizExamPackage::getStatus, 1));
    }

    @Override
    @Transactional
    public BizExamAppointment book(Long patientId, Long packageId, String date) {
        BizExamPackage examPackage = packageMapper.selectById(packageId);
        if (examPackage == null || examPackage.getStatus() == 0) {
            throw new BusinessException("体检套餐不存在或已停用");
        }

        BizExamAppointment appointment = new BizExamAppointment();
        appointment.setPatientId(patientId);
        appointment.setPackageId(packageId);
        appointment.setAppointmentDate(LocalDate.parse(date));
        appointment.setStatus(1); // 已预约
        appointment.setFee(examPackage.getPrice());
        appointmentMapper.insert(appointment);

        return appointment;
    }

    @Override
    public List<BizExamAppointment> listByPatient(Long patientId) {
        return appointmentMapper.selectList(
                new LambdaQueryWrapper<BizExamAppointment>()
                        .eq(BizExamAppointment::getPatientId, patientId)
                        .orderByDesc(BizExamAppointment::getCreateTime));
    }

    @Override
    @Transactional
    public void cancel(Long appointmentId, Long patientId) {
        BizExamAppointment appointment = appointmentMapper.selectById(appointmentId);
        if (appointment == null || !appointment.getPatientId().equals(patientId)) {
            throw new BusinessException("体检预约记录不存在");
        }
        appointment.setStatus(4); // 已取消
        appointmentMapper.updateById(appointment);
    }
}
