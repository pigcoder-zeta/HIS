package com.smarthealthcare.service.impl;

import com.smarthealthcare.common.BusinessException;
import com.smarthealthcare.dto.MedicalRecordDTO;
import com.smarthealthcare.entity.BizMedicalRecord;
import com.smarthealthcare.mapper.BizMedicalRecordMapper;
import com.smarthealthcare.service.MedicalRecordService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class MedicalRecordServiceImpl implements MedicalRecordService {

    private final BizMedicalRecordMapper recordMapper;

    @Override
    @Transactional
    public BizMedicalRecord createOrUpdate(Long doctorId, MedicalRecordDTO dto) {
        BizMedicalRecord record;
        // 检查是否有关联的挂号记录已有草稿病历
        if (dto.getAppointmentId() != null) {
            List<BizMedicalRecord> existing = recordMapper.selectList(
                    new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<BizMedicalRecord>()
                            .eq(BizMedicalRecord::getAppointmentId, dto.getAppointmentId())
                            .eq(BizMedicalRecord::getStatus, 0));
            if (!existing.isEmpty()) {
                record = existing.get(0);
            } else {
                record = new BizMedicalRecord();
                record.setAppointmentId(dto.getAppointmentId());
            }
        } else {
            record = new BizMedicalRecord();
        }

        record.setPatientId(dto.getPatientId());
        record.setDoctorId(doctorId);
        record.setChiefComplaint(dto.getChiefComplaint());
        record.setPresentIllness(dto.getPresentIllness());
        record.setPhysicalExamination(dto.getPhysicalExamination());
        record.setAuxiliaryExam(dto.getAuxiliaryExam());
        record.setDiagnosis(dto.getDiagnosis());
        record.setTreatmentPlan(dto.getTreatmentPlan());
        record.setStatus(0); // 草稿

        if (record.getId() == null) {
            recordMapper.insert(record);
        } else {
            recordMapper.updateById(record);
        }

        return record;
    }

    @Override
    @Transactional
    public BizMedicalRecord signAndArchive(Long recordId, Long doctorId) {
        BizMedicalRecord record = recordMapper.selectById(recordId);
        if (record == null) {
            throw new BusinessException("病历不存在");
        }
        if (!record.getDoctorId().equals(doctorId)) {
            throw new BusinessException("您无权签名此病历");
        }
        if (record.getStatus() != 0) {
            throw new BusinessException("该病历不是草稿状态，无法签名");
        }

        record.setStatus(1); // 已签名
        record.setSignedTime(LocalDateTime.now());
        record.setSignatureCode(UUID.randomUUID().toString().replace("-", "").substring(0, 16));
        recordMapper.updateById(record);

        log.info("医生[{}]签名病历[{}]", doctorId, recordId);
        return record;
    }

    @Override
    public BizMedicalRecord getById(Long id) {
        return recordMapper.selectById(id);
    }

    @Override
    public List<BizMedicalRecord> listByPatient(Long patientId) {
        return recordMapper.selectByPatientId(patientId);
    }

    @Override
    @Transactional
    public void requestUnlock(Long recordId, Long doctorId) {
        BizMedicalRecord record = recordMapper.selectById(recordId);
        if (record == null) {
            throw new BusinessException("病历不存在");
        }
        // 只有签名超过24小时的病历才能申请解锁
        if (record.getSignedTime() != null &&
                record.getSignedTime().plusHours(24).isAfter(LocalDateTime.now())) {
            throw new BusinessException("病历签名未满24小时，暂不可申请解锁");
        }
        record.setStatus(3); // 已锁定
        recordMapper.updateById(record);
        log.info("病历[{}]已申请解锁", recordId);
    }
}
